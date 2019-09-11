package net.ambitious.bvlion.batch2.batch.configuration;

import com.google.firebase.database.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.ambitious.bvlion.batch2.entity.TimerEntity;
import net.ambitious.bvlion.batch2.enums.TimerDateEnum;
import net.ambitious.bvlion.batch2.mapper.HolidayMapper;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisCursorItemReader;
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
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Configuration
@EnableBatchProcessing
@EnableScheduling
@RequiredArgsConstructor
public class TimerConfiguration {

	@NonNull
	private final JobLauncher jobLauncher;

	@NonNull
	private final JobBuilderFactory jobBuilderFactory;

	@NonNull
	private final StepBuilderFactory stepBuilderFactory;

	@NonNull
	private final SqlSessionFactory sqlSessionFactory;

	@NonNull
	private final HolidayMapper holidayMapper;

	private final FirebaseDatabase database = FirebaseDatabase.getInstance();
	private final DatabaseReference ref = database.getReference("node/infrared");

	@Scheduled(cron = "0 * * * * *", zone = "Asia/Tokyo")
	public void check() throws JobParametersInvalidException, JobExecutionAlreadyRunningException,
			JobRestartException, JobInstanceAlreadyCompleteException {
		this.jobLauncher.run(job(), new JobParameters(
				Stream.of(new JobParameter(new Date()))
						.collect(Collectors.toMap(d -> "exec_date", d -> d)))
		);
	}

	private Job job() {
		return this.jobBuilderFactory.get("timerJob").start(step()).build();
	}

	private Step step() {
		return this.stepBuilderFactory.get("timerStep")
				.<TimerEntity, String>chunk(10)
				.reader(reader()).processor(processor()).writer(writer())
				.build();
	}

	private ItemReader<TimerEntity> reader() {
		Calendar cal = Calendar.getInstance();

		Map<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("do_exec_time", FastDateFormat.getInstance("HHmm").format(cal) + "00");
		parameterValues.put("exec_started_flag", TimerDateEnum.columnName(cal.get(Calendar.DAY_OF_WEEK)));

		MyBatisCursorItemReader<TimerEntity> reader = new MyBatisCursorItemReader<>();
		reader.setParameterValues(parameterValues);
		reader.setSqlSessionFactory(this.sqlSessionFactory);
		reader.setQueryId("net.ambitious.bvlion.batch2.mapper.TimerDataMapper.selectExecTimerSetting");

		return reader;
	}

	private ItemWriter<String> writer() {
		return items -> items.forEach(cmd ->
			ref.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot snapshot) {
					ref.setValueAsync(cmd);
				}

				@Override
				public void onCancelled(DatabaseError error) { }
			})
		);
	}

	private ItemProcessor<TimerEntity, String> processor() {
		return new TimerItemProcessor();
	}

	private class TimerItemProcessor implements ItemProcessor<TimerEntity, String> {

		@Override
		public String process(TimerEntity entity) {
			if (entity.isHolidayDecision() && holidayMapper.isHoliday()) {
				return null;
			}
			switch (entity.getBehaviorType()) {
				case 1: // エアコンON
					StringBuilder param = new StringBuilder("\" timer" + System.currentTimeMillis() + " … ");
					switch (entity.getAirconType()) {
						case 1: // 冷房
							param.append("aircon:cool");
							param.append((int) entity.getTemperature());
							break;
						case 2: // 除湿
							param.append("aircon:dry");
							break;
						case 3: // 暖房
							param.append("aircon:hot");
							param.append((int) entity.getTemperature());
							break;
						default:
							return null;
					}
					param.append(" … 1 \"");
					return param.toString();
				case 2: // エアコンOFF
					return "\" timer" + System.currentTimeMillis() + " … aircon:off … 1 \"";
				default:
					return null;
			}
		}
	}
}
