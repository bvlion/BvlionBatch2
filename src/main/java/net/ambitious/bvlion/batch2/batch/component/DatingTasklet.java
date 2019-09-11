package net.ambitious.bvlion.batch2.batch.component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.ambitious.bvlion.batch2.mapper.DatingMapper;
import net.ambitious.bvlion.batch2.util.AccessUtil;
import net.ambitious.bvlion.batch2.util.AppParams;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatingTasklet implements Tasklet {

	@NonNull
	private AppParams appParams;

	@NonNull
	private DatingMapper datingMapper;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		Calendar now = Calendar.getInstance();

		String message = this.datingMapper.allDatings().stream()
				.map(value -> {
					if (value.getTargetDate().length() == 8) {
						try {
							Date anniversary = DateUtils.parseDate(value.getTargetDate(), "yyyyMMdd");
							long totalDays = TimeUnit.DAYS.convert(
									now.getTimeInMillis() - anniversary.getTime(), TimeUnit.MILLISECONDS
							) + 1;
							if (totalDays % 100 == 0) {
								return String.format(value.getMessage(), NumberFormat.getNumberInstance().format(totalDays));
							}
						} catch (ParseException e) {
							log.debug(value.getTargetDate(), e);
						}
					} else {
						if (FastDateFormat.getInstance("MMdd").format(new Date()).equals(value.getTargetDate())) {
							return value.getMessage();
						}
					}
					return "";
				}).collect(Collectors.joining("¥n"));

		if (message.length() > 0) {
			AccessUtil.sendFcm(
					AccessUtil.createTopicMessage("記念日通知", message + "おめでとう(*･ω･)ﾉ", "dating"),
					appParams,
					log
			);
		}
		return RepeatStatus.FINISHED;
	}
}
