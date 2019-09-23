package net.ambitious.bvlion.batch2.batch.configuration;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.ambitious.bvlion.batch2.enums.HolidayEnum;
import net.ambitious.bvlion.batch2.mapper.ExecTimeMapper;
import net.ambitious.bvlion.batch2.mapper.HolidayMapper;
import net.ambitious.bvlion.batch2.util.AccessUtil;
import net.ambitious.bvlion.batch2.util.AppParams;
import net.ambitious.bvlion.batch2.util.SlackHttpPost;
import org.apache.commons.lang3.StringUtils;
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

import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Configuration
@EnableBatchProcessing
@EnableScheduling
@RequiredArgsConstructor
public class TimeNotificationConfiguration {
	@NonNull
	private final JobLauncher jobLauncher;

	@NonNull
	private final JobBuilderFactory jobBuilderFactory;

	@NonNull
	private final StepBuilderFactory stepBuilderFactory;

	@NonNull
	private final AppParams appParams;

	@NonNull
	private final HolidayMapper holidayMapper;

	@NonNull
	private final ExecTimeMapper execTimeMapper;

	private Step step() {
		return this.stepBuilderFactory.get("TimeNotificationStep").tasklet((contribution, chunkContext) -> {
			if (AccessUtil.isExecTime(holidayMapper.isHoliday(), execTimeMapper.selectExecTimes())) {
				AccessUtil.postGoogleHome(
						"時刻は" + AccessUtil.getNow("H:mm") + "になりました。",
						log,
						appParams
				);
			}
			return RepeatStatus.FINISHED;
		}).build();
	}

	private Job job() {
		return this.jobBuilderFactory.get("TimeNotificationJob").start(step()).build();
	}

	@Scheduled(cron = "${scheduler.time.cron1}", zone = "Asia/Tokyo")
	public void check() throws JobExecutionAlreadyRunningException, JobRestartException,
			JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		this.jobLauncher.run(job(), new JobParameters(
				Stream.of(new JobParameter(new Date()))
						.collect(Collectors.toMap(d -> "exec_date1", d -> d)))
		);
	}

	@Scheduled(cron = "${scheduler.time.cron2}", zone = "Asia/Tokyo")
	public void special() throws JobExecutionAlreadyRunningException, JobRestartException,
			JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		this.jobLauncher.run(job(), new JobParameters(
				Stream.of(new JobParameter(new Date()))
						.collect(Collectors.toMap(d -> "exec_date2", d -> d)))
		);
	}

	@Scheduled(cron = "${scheduler.time.cron3}", zone = "Asia/Tokyo")
	public void temperatureDetection() throws JobExecutionAlreadyRunningException, JobRestartException,
			JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		this.jobLauncher.run(temperatureDetectionJob(), new JobParameters(
				Stream.of(new JobParameter(new Date()))
						.collect(Collectors.toMap(d -> "exec_date3", d -> d)))
		);
	}

	@Scheduled(cron = "${scheduler.time.cron4}", zone = "Asia/Tokyo")
	public void coopNotification() throws JobExecutionAlreadyRunningException, JobRestartException,
			JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		this.jobLauncher.run(this.jobBuilderFactory.get("coopNotificationJob").start(
				this.stepBuilderFactory.get("coopNotificationStep").tasklet((contribution, chunkContext) -> {
					var message = "明日はコープさんです。空き箱を出してください。";
					AccessUtil.postGoogleHome(
							message,
							log,
							appParams
					);
					new SlackHttpPost(
							"reminder",
							"ほぺたん(･ω･)",
							message,
							"http://mirai.coopnet.or.jp/event/area_info/chiba/img/hopetan_cafe_01.png"
					).send(appParams);
					return RepeatStatus.FINISHED;
				}).build()).build(),
				new JobParameters(
						Stream.of(new JobParameter(new Date())).collect(Collectors.toMap(d -> "exec_date4", d -> d))
				)
		);
	}

	@Scheduled(cron = "0 40 23 * * *", zone = "Asia/Tokyo")
	public void alarmNotification() throws JobExecutionAlreadyRunningException, JobRestartException,
			JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		this.jobLauncher.run(this.jobBuilderFactory.get("alarmNotificationJob").start(
				this.stepBuilderFactory.get("alarmNotificationStep").tasklet((contribution, chunkContext) -> {
					var status = holidayMapper.nextDayStatusCheck();
					String message = null;
					if (status == HolidayEnum.明日は休み.getStatus()) {
						message = "明日はお休みです。アラームの設定を解除して下さい。";
					}
					if (status == HolidayEnum.明日は仕事.getStatus()) {
						message = "明日はお仕事です。アラームを設定して下さい。";
					}
					if (StringUtils.isNotEmpty(message)) {
						AccessUtil.postGoogleHome(
								message,
								log,
								appParams
						);
						new SlackHttpPost(
								"reminder",
								"目覚ましキキ",
								message,
								"https://www.sanrio.co.jp/special/kikilala/twitter/advice/images/0104/kiki_moon0104.png"
						).send(appParams);
					}
					return RepeatStatus.FINISHED;
				}).build()).build(),
				new JobParameters(
						Stream.of(new JobParameter(new Date())).collect(Collectors.toMap(d -> "exec_date5", d -> d))
				)
		);
	}

	private Step temperatureDetectionStep() {
		return this.stepBuilderFactory.get("temperatureDetectionStep").tasklet((contribution, chunkContext) -> {
			if (!holidayMapper.isHoliday()) {
				AccessUtil.postGoogleHome(
						"おはようございます。検温は済んでいますか？",
						log,
						appParams
				);
			}
			return RepeatStatus.FINISHED;
		}).build();
	}

	private Job temperatureDetectionJob() {
		return this.jobBuilderFactory.get("temperatureDetectionJob").start(temperatureDetectionStep()).build();
	}
}
