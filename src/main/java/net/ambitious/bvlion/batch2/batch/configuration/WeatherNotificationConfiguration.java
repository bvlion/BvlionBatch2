package net.ambitious.bvlion.batch2.batch.configuration;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.ambitious.bvlion.batch2.batch.component.WeatherNotificationTasklet;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
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
public class WeatherNotificationConfiguration {

	@NonNull
	private final JobLauncher jobLauncher;

	@NonNull
	private final JobBuilderFactory jobBuilderFactory;

	@NonNull
	private final StepBuilderFactory stepBuilderFactory;

	@NonNull
	private final WeatherNotificationTasklet weatherNotificationTasklet;

	private Step step() {
		return this.stepBuilderFactory.get("weatherNotificationStep").tasklet(weatherNotificationTasklet).build();
	}

	private Job job() {
		return this.jobBuilderFactory.get("weatherNotificationJob").start(step()).build();
	}

	@Scheduled(cron = "${scheduler.time.cron5}", zone = "Asia/Tokyo")
	public void check() throws JobExecutionAlreadyRunningException, JobRestartException,
			JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		this.jobLauncher.run(job(), new JobParameters(
				Stream.of(new JobParameter(new Date()))
						.collect(Collectors.toMap(d -> "exec_date", d -> d)))
		);
	}
}
