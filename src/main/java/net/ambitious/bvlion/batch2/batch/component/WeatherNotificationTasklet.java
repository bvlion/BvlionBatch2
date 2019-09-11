package net.ambitious.bvlion.batch2.batch.component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.ambitious.bvlion.batch2.batch.configuration.WeatherNotificationConfiguration;
import net.ambitious.bvlion.batch2.mapper.WeatherSearchesMapper;
import net.ambitious.bvlion.batch2.util.AccessUtil;
import net.ambitious.bvlion.batch2.util.AppParams;
import net.ambitious.bvlion.batch2.util.SlackHttpPost;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherNotificationTasklet implements Tasklet {

	private static final String ICON_URL = "https://4s.ambitious-i.net/weather_img/%s.png";

	@NonNull
	private final AppParams appParams;

	@NonNull
	private final WeatherSearchesMapper weatherSearchesMapper;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		weatherSearchesMapper.selectWeatherSearchList().forEach(entity -> {
			try {
				Connection con = Jsoup.connect(entity.getPcUrl());
				con.userAgent(entity.getUserAgent());
				Document document = con.get();

				// 最高気温、最低気温、降水確率、コメント、アイコンのURLを取得
				String maxTmp = document.selectFirst(
						"#feature-history > table > tbody > tr:eq(0) > td:eq(1)"
				).html();
				String minTmp = document.selectFirst(
						"#feature-history > table > tbody > tr:eq(1) > td:eq(1)"
				).html();
				String rainyPercent = document.selectFirst(
						"#detail-day-night > div:eq(0) > div > div:eq(0) > div > div:eq(0) > span:eq(2)"
				).html().replace("降水", "降水確率");
				String comment = document.selectFirst(
						"#detail-day-night > div:eq(0) > div > div:eq(0) > div > div:eq(2)"
				).html();
				String realFeel = document.selectFirst(
						"#detail-day-night > div:eq(0) > div > div:eq(0) > div > div:eq(0) > span:eq(1)"
				).html().split(" ")[1];
				String[] wind = document.selectFirst(
						"#detail-day-night > div:eq(0) > div > div:eq(1) > div > div > ul > li:eq(1) > strong"
				).html().split(" ");
				String iconUrl = String.format(ICON_URL, document.selectFirst(
						"#detail-day-night > div:eq(0) > div > div:eq(0) > div > div:eq(1)"
				).className().split(" ")[1]);

				String message = AccessUtil.getNextDate() + "の" + entity.getAreaName() + "の天気は\\n"
						+ "*最高気温 " + maxTmp + "*\\n"
						+ "*最低気温 " + minTmp + "*\\n"
						+ "*" + rainyPercent + "*\\n"
						+ "風は *" + wind[0] + "に毎時" + wind[1] + "km* で吹き\\n"
						+ "*体感気温は" + realFeel + "* となる見込みです<(_ _)>\\n"
						+ "ザックリ言うと「" + comment + "」という感じです(･∀･)" + "\\n"
						+ "\\n"
						+ "詳細はこちら↓↓\\n"
						+ entity.getMobileUrl();

				new SlackHttpPost(
						"weather-api",
						"weather-api-" + AccessUtil.getNow("yyyyMMdd"),
						message,
						iconUrl
				).send(appParams);

				String googleHomeMessage = "エーシーシーユーウェザーによると、明日の" + entity.getAreaName() + "の天気は"
						+ "最高気温 " + maxTmp + "、"
						+ "最低気温 " + minTmp + "、"
						+ rainyPercent + "、"
						+ "風は" + wind[0] + "に毎時" + wind[1] + "kmで吹き、"
						+ "体感気温は" + realFeel + "となる見込みです。"
						+ "ザックリ言うと、" + comment.replace("所", "ところ") + "、という感じです。";
				AccessUtil.postGoogleHome(googleHomeMessage, log, WeatherNotificationConfiguration.class, appParams);
			} catch (IOException e) {
				log.warn("WeatherNotificationConfiguration Error", e);
			}
		});
		return RepeatStatus.FINISHED;
	}
}
