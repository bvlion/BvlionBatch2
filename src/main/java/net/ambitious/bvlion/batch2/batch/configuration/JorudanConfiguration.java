package net.ambitious.bvlion.batch2.batch.configuration;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.ambitious.bvlion.batch2.entity.JorudanDataEntity;
import net.ambitious.bvlion.batch2.entity.TwitterChannelsDataEntity;
import net.ambitious.bvlion.batch2.mapper.ExecTimeMapper;
import net.ambitious.bvlion.batch2.mapper.HolidayMapper;
import net.ambitious.bvlion.batch2.mapper.JorudanDataMapper;
import net.ambitious.bvlion.batch2.mapper.TwitterChannelsMapper;
import net.ambitious.bvlion.batch2.util.AccessUtil;
import net.ambitious.bvlion.batch2.util.AppParams;
import net.ambitious.bvlion.batch2.util.SlackHttpPost;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import twitter4j.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Configuration
@EnableBatchProcessing
@EnableScheduling
@RequiredArgsConstructor
public class JorudanConfiguration {
	private static final String JORUDAN_ICON = "https://pbs.twimg.com/profile_images/753471803/JorudanLive-Icon.png";

	@NonNull
	private final JobLauncher jobLauncher;

	@NonNull
	private final JobBuilderFactory jobBuilderFactory;

	@NonNull
	private final StepBuilderFactory stepBuilderFactory;

	@NonNull
	private final SqlSessionFactory sqlSessionFactory;

	@NonNull
	private final JorudanDataMapper mapper;

	@NonNull
	private final AppParams appParams;

	@NonNull
	private final HolidayMapper holidayMapper;

	@NonNull
	private final TwitterChannelsMapper twitterChannelsMapper;

	@NonNull
	private final ExecTimeMapper execTimeMapper;

	@Scheduled(fixedDelay = 20 * 1000)
	public void check() {
		this.twitterChannelsMapper.selectEnableChannels(1).forEach(entity -> {
			try {
				this.jobLauncher.run(job(entity), new JobParameters(
						Stream.of(new JobParameter(new Date()))
								.collect(Collectors.toMap(
										d -> "exec_date_" + entity.getSearchValue(), d -> d
								)))
				);
			} catch (Exception e) {
				if (e instanceof DeadlockLoserDataAccessException) {
					log.warn(e.getMessage());
					return;
				}
				final var message = "BatchでExceptionが発生したようです。";
				AccessUtil.exceptionPost(message, log, getClass(), e, appParams);
			}
		});
	}

	private Job job(TwitterChannelsDataEntity entity) {
		return this.jobBuilderFactory.get("jorudanJob").start(step(entity)).build();
	}

	private Step step(TwitterChannelsDataEntity entity) {
		return this.stepBuilderFactory.get("jorudanStep")
				.<JorudanDataEntity, JorudanDataEntity>chunk(100)
				.reader(reader(entity.getSearchValue())).processor(processor(entity)).writer(writer())
				.build();
	}

	private static ItemReader<JorudanDataEntity> reader(String hashTag) {
		var twitter = new TwitterFactory().getInstance();
		var query = new Query();
		query.setQuery("from:jorudanlive AND #" + hashTag);
		QueryResult result;
		try {
			result = twitter.search(query);
		} catch (TwitterException e) {
			return new ListItemReader<>(Collections.emptyList());
		}
		return new ListItemReader<>(
				result.getTweets().stream()
						.map(tweet -> new JorudanDataEntity(tweet.getText()))
						.sorted(Comparator.comparing(JorudanDataEntity::getPostedDate))
						.collect(Collectors.toList())
		);
	}

	private MyBatisBatchItemWriter<JorudanDataEntity> writer() {
		var writer = new MyBatisBatchItemWriter<JorudanDataEntity>();
		writer.setStatementId("net.ambitious.bvlion.batch2.mapper.JorudanDataMapper.jorudan_data_insert");
		writer.setSqlSessionFactory(this.sqlSessionFactory);
		return writer;
	}

	private ItemProcessor<JorudanDataEntity, JorudanDataEntity> processor(TwitterChannelsDataEntity entity) {
		return new JorudanItemProcessor(this.mapper.jorudanDataSelect(), entity, holidayMapper.isHoliday());
	}

	private class JorudanItemProcessor implements ItemProcessor<JorudanDataEntity, JorudanDataEntity> {

		private List<JorudanDataEntity> entityList;

		private TwitterChannelsDataEntity entity;

		private boolean isHoliday;

		private JorudanItemProcessor(List<JorudanDataEntity> entityList, TwitterChannelsDataEntity entity, boolean isHoliday) {
			this.entity = entity;
			this.isHoliday = isHoliday;
			this.entityList = entityList;
		}

		@Override
		public JorudanDataEntity process(JorudanDataEntity item) throws Exception {

			var isPostExec = new AtomicBoolean(true);
			this.entityList.stream()
					.map(JorudanDataEntity::getUrl)
					.filter(item.getUrl()::equals)
					.forEach(s -> isPostExec.set(false));

			if (!isPostExec.get()) {
				return null;
			}

			if (AccessUtil.isExecTime(isHoliday, execTimeMapper.selectExecTimes())) {
				var details = item.getDetail().split("〕");
				var section = details[0]
						.substring(1)
						.replaceAll("（.*）", "")
						.replace("〜", "から")
						+ "の区間";
			var state = details[1].split("／")[0];
			var googleHomeMessage = entity.getSearchValue() + "の" + section + "で"
						+ (state.equals("止まってる") ? state : state + "の") + "ようです。";
				AccessUtil.postGoogleHome(googleHomeMessage, log, JorudanConfiguration.class, appParams);
			}

			var message = new StringBuilder();
			message.append(item.getDetail());
			if (StringUtils.isNoneBlank(item.getDescription())) {
				message.append("\n");
				message.append(item.getDescription());
			}
			message.append("\n");
			message.append("\n");
			message.append(item.getUrl());
			new SlackHttpPost(
					entity.getSlackChannel(),
					entity.getSearchValue() + "-"
							+ FastDateFormat.getInstance("MM/dd HH:mm").format(item.getPostedDate()),
					message.toString(),
					JORUDAN_ICON
			).send(appParams);

			return item;
		}
	}
}
