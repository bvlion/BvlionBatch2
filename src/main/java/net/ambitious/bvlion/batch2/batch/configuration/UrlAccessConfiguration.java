package net.ambitious.bvlion.batch2.batch.configuration;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.ambitious.bvlion.batch2.mapper.RegularAccessUrlMapper;
import net.ambitious.bvlion.batch2.util.AccessUtil;
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

@Configuration
@EnableBatchProcessing
@EnableScheduling
@RequiredArgsConstructor
public class UrlAccessConfiguration {
	@NonNull
	private final JobLauncher jobLauncher;

	@NonNull
	private final JobBuilderFactory jobBuilderFactory;

	@NonNull
	private final StepBuilderFactory stepBuilderFactory;

	@NonNull
	private final RegularAccessUrlMapper regularAccessUrlMapper;

	private Step step() {
		return this.stepBuilderFactory.get("UrlAccessStep").tasklet((contribution, chunkContext) ->
				this.regularAccessUrlMapper.regularAccessUrls().stream().map(this::access).collect(Collectors.toList()).get(0)).build();
	}

	private RepeatStatus access(String url) {
		AccessUtil.accessGet(url, null, getClass());
		return RepeatStatus.FINISHED;
	}

	private Job job() {
		return this.jobBuilderFactory.get("UrlAccessJob").start(step()).build();
	}

	@Scheduled(cron = "15 3-58/5 * * * *", zone = "Asia/Tokyo")
	public void check() throws JobExecutionAlreadyRunningException, JobRestartException,
			JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		this.jobLauncher.run(job(), new JobParameters(
				Stream.of(new JobParameter(new Date()))
						.collect(Collectors.toMap(d -> "exec_date_url_access", d -> d)))
		);
	}
}
