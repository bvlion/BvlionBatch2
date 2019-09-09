package net.ambitious.bvlion.batch2.batch.component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.ambitious.bvlion.batch2.mapper.HolidayMapper;
import net.ambitious.bvlion.batch2.util.AccessUtil;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.TimeZone;

@Slf4j
@Component
@RequiredArgsConstructor
public class CheckHolidayTasklet implements Tasklet {

	@NonNull
	private final HolidayMapper holidayMapper;

	private static final String GOOGLE_HOLIDAY_CHECK_URL
			= "https://www.googleapis.com/calendar/v3/calendars/japanese__ja%%40holiday.calendar.google.com/events"
			+ "?key=AIzaSyBTxx6po40TjVUCK8HD-tARV7CjU0dkbpk"
			+ "&timeMax=%sT00%%3A00%%3A00Z"
			+ "&timeMin=%sT00%%3A00%%3A00Z";

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		final String format = "yyyy-MM-dd";
		int holiday = 0;

		Request request = Request.Get(
				String.format(GOOGLE_HOLIDAY_CHECK_URL,
						AccessUtil.getNextDate(format, 2),
						AccessUtil.getNextDate(format)
				)
		);
		Response res = null;
		try {
			res = request.execute();
			JSONObject json = new JSONObject(new String(res.returnContent().asBytes(), StandardCharsets.UTF_8));
			JSONArray array = json.getJSONArray("items");
			holiday = array.length() > 0 ? 1 : 0;
		} catch (IOException e) {
			log.error("Holiday Get Error", e);
		} finally {
			if (res != null) {
				res.discardContent();
			}
		}

		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"));
		cal.add(Calendar.DATE, 1);

		if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
			holiday = 1;
		}

		if (!holidayMapper.isSetHoliday()) {
			holidayMapper.setHoliday(AccessUtil.getNextDate(format), holiday);
		}

		return RepeatStatus.FINISHED;
	}
}
