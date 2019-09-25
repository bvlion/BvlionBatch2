package net.ambitious.bvlion.batch2.batch.configuration;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.ambitious.bvlion.batch2.entity.TwitterChannelsDataEntity;
import net.ambitious.bvlion.batch2.entity.TwitterImageEntity;
import net.ambitious.bvlion.batch2.mapper.TwitterChannelsMapper;
import net.ambitious.bvlion.batch2.mapper.TwitterImageMapper;
import net.ambitious.bvlion.batch2.util.AccessUtil;
import net.ambitious.bvlion.batch2.util.AppParams;
import net.ambitious.bvlion.batch2.util.SlackBinaryPost;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import twitter4j.Query;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableBatchProcessing
@EnableScheduling
@RequiredArgsConstructor
public class TwitterImageConfiguration {

	@NonNull
	private final JobLauncher jobLauncher;

	@NonNull
	private final JobBuilderFactory jobBuilderFactory;

	@NonNull
	private final StepBuilderFactory stepBuilderFactory;

	@NonNull
	private final SqlSessionFactory sqlSessionFactory;

	@NonNull
	private final TwitterImageMapper mapper;

	@NonNull
	private final AppParams appParams;

	@NonNull
	private final TwitterChannelsMapper twitterChannelsMapper;

	@Scheduled(fixedRate = 2 * 60 * 1000)
	public void check() {
		this.twitterChannelsMapper.selectEnableChannels(0).forEach(value -> {
			try {
				this.jobLauncher.run(job(value), new JobParameters(
						Stream.of(new JobParameter(new Date()))
								.collect(Collectors.toMap(d -> "exec_date_" + value.getImageType(), d -> d)))
				);
			} catch (JobExecutionAlreadyRunningException | JobRestartException
					| JobParametersInvalidException | JobInstanceAlreadyCompleteException e) {
				e.printStackTrace();
			}
		});
	}

	private Job job(TwitterChannelsDataEntity value) {
		return this.jobBuilderFactory.get("twitterImageJob").start(step(value)).build();
	}

	private Step step(TwitterChannelsDataEntity value) {
		return this.stepBuilderFactory.get("twitterImageStep")
				.<TwitterImageEntity, TwitterImageEntity>chunk(2)
				.reader(reader(value)).processor(processor(value)).writer(writer())
				.build();
	}

	private static ItemReader<TwitterImageEntity> reader(TwitterChannelsDataEntity value) {
		var twitter = new TwitterFactory().getInstance();
		var query = new Query();
		query.setQuery(value.getSearchValue());
		try {
			return new ListItemReader<>(
					twitter.search(query).getTweets().stream()
							.filter(tweet -> !tweet.getText().split(" https")[0].contains("RT @"))
							.map(TwitterImageEntity::createTwitterImageEntities)
							.flatMap(Collection::stream)
							.sorted(Comparator.comparing(TwitterImageEntity::getPostedDate))
							.collect(Collectors.toList())
			);
		} catch (TwitterException e) {
			return new ListItemReader<>(new ArrayList<>());
		}
	}

	private MyBatisBatchItemWriter<TwitterImageEntity> writer() {
		var writer = new MyBatisBatchItemWriter<TwitterImageEntity>();
		writer.setStatementId("net.ambitious.bvlion.batch2.mapper.TwitterImageMapper.twitter_image_data_insert");
		writer.setSqlSessionFactory(this.sqlSessionFactory);
		return writer;
	}

	private ItemProcessor<TwitterImageEntity, TwitterImageEntity> processor(TwitterChannelsDataEntity value) {
		return new TwitterImageProcessor(this.mapper.twitterImageDataSelect(value.getImageType()), value);
	}

	private class TwitterImageProcessor implements ItemProcessor<TwitterImageEntity, TwitterImageEntity> {

		private List<String> entityList;
		private TwitterChannelsDataEntity twitterImageEnum;

		private TwitterImageProcessor(List<String> entityList, TwitterChannelsDataEntity twitterImageEnum) {
			this.entityList = entityList;
			this.twitterImageEnum = twitterImageEnum;
		}

		@Override
		public TwitterImageEntity process(TwitterImageEntity item) throws Exception {
			if (StringUtils.isBlank(item.getMediaUrl())) {
				return null;
			}

			var isPostExec = new AtomicBoolean(true);
			this.entityList.stream().filter(item.getMediaUrl()::equals).forEach(s -> isPostExec.set(false));

			if (!isPostExec.get()) {
				return null;
			}

			if (appParams.isProduction()) {
				new SlackBinaryPost.Builder()
						.channels(this.twitterImageEnum.getSlackChannel())
						.title(item.getText())
						.fileName(FastDateFormat.getInstance("yyyyMMddHHmmss").format(Calendar.getInstance(AccessUtil.TOKYO)) + ".png")
						.fileData(AccessUtil.getBinaryBytes(item.getMediaUrl()))
						.build(appParams.getSlackToken()).post(appParams);
			}
			item.setImageType(this.twitterImageEnum.getImageType());

			return item;
		}
	}
}
