package net.ambitious.bvlion.batch2.batch.configuration;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.ambitious.bvlion.batch2.util.AccessUtil;
import net.ambitious.bvlion.batch2.util.AppParams;
import net.ambitious.bvlion.batch2.util.SlackHttpPost;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Configuration
@EnableBatchProcessing
@EnableScheduling
@RequiredArgsConstructor
public class HoroscopeConfiguration {
	@NonNull
	private final JobLauncher jobLauncher;

	@NonNull
	private final JobBuilderFactory jobBuilderFactory;

	@NonNull
	private final StepBuilderFactory stepBuilderFactory;

	@NonNull
	private final AppParams appParams;

	private Step step() {
		return this.stepBuilderFactory.get("horoscopeNotificationStep").tasklet((contribution, chunkContext) -> {

			new SlackHttpPost(
					"horoscope-api",
					"horoscope-api-" + AccessUtil.getNow("yyyyMMdd"),
					getHoroscopeMessage(),
					"https://4s.ambitious-i.net/icon/1434076.png"
			).send(appParams);

			return RepeatStatus.FINISHED;
		}).build();
	}

	private Job job() {
		return this.jobBuilderFactory.get("horoscopeNotificationJob").start(step()).build();
	}

	@Scheduled(cron = "25 30 7 * * *", zone = "Asia/Tokyo")
	public void check() throws JobExecutionAlreadyRunningException, JobRestartException,
			JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		this.jobLauncher.run(job(), new JobParameters(
				Stream.of(new JobParameter(new Date()))
						.collect(Collectors.toMap(d -> "exec_date", d -> d)))
		);
	}

	private static String getHoroscopeMessage() {
		String today = AccessUtil.getNow("yyyy/MM/dd");
		StringBuilder message = new StringBuilder();

		Request request = Request.Get("http://api.jugemkey.jp/api/horoscope/free/" + today);
		Response res = null;
		try {
			res = request.execute();
			JSONObject json = new JSONObject(new String(res.returnContent().asBytes(), StandardCharsets.UTF_8));
			JSONObject horoscope = json.getJSONObject("horoscope");
			JSONArray todayData = horoscope.getJSONArray(today);
			for (int i = 0; i < todayData.length(); i++) {
				JSONObject object = todayData.getJSONObject(i);
				if ("双子座".equals(object.getString("sign"))) {
					message.append(today);
					message.append("の双子座の運勢は第");
					message.append(object.getInt("rank"));
					message.append("位！\\n");
					message.append(object.getString("content"));
					message.append("\\n");
					message.append("ラッキーカラーは「");
					message.append(object.getString("color"));
					message.append("」、");
					message.append("ラッキーアイテムは「");
					message.append(object.getString("item"));
					message.append("」だよ。\\n\\n");
					message.append("金運：");
					message.append(object.getInt("money"));
					message.append("\\n");
					message.append("仕事運：");
					message.append(object.getInt("job"));
					message.append("\\n");
					message.append("恋愛運：");
					message.append(object.getInt("love"));
					message.append("\\n");
					message.append("総合評価：");
					message.append(object.getInt("total"));
					break;
				}
			}
		} catch (IOException | JSONException e) {
			log.error("Horoscope Get Error", e);
		} finally {
			if (res != null) {
				res.discardContent();
			}
		}

		if (message.length() == 0) {
			message.append(today);
			message.append("の双子座の運勢は取得できませんでした(´･ω･`)");
		}

		return message.toString();
	}
}
