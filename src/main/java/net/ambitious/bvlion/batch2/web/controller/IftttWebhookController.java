package net.ambitious.bvlion.batch2.web.controller;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.ambitious.bvlion.batch2.entity.Mp3Entity;
import net.ambitious.bvlion.batch2.enums.AutoIncrementEnum;
import net.ambitious.bvlion.batch2.mapper.AutoIncrementsMapper;
import net.ambitious.bvlion.batch2.mapper.HolidayMapper;
import net.ambitious.bvlion.batch2.mapper.Mp3Mapper;
import net.ambitious.bvlion.batch2.mapper.UserMapper;
import net.ambitious.bvlion.batch2.util.AccessUtil;
import net.ambitious.bvlion.batch2.util.AppParams;
import net.ambitious.bvlion.batch2.util.SlackBinaryPost;
import net.ambitious.bvlion.batch2.util.SlackHttpPost;
import net.ambitious.bvlion.batch2.web.exception.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Calendar;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ifttt/${url.ifttt}")
public class IftttWebhookController {

	@NonNull
	private final Mp3Mapper mp3Mapper;

	@NonNull
	private final AppParams appParams;

	@NonNull
	private final HolidayMapper holidayMapper;

	@NonNull
	private final UserMapper userMapper;

	@NonNull
	private final AutoIncrementsMapper autoIncrementsMapper;

	private final FirebaseDatabase database = FirebaseDatabase.getInstance();
	private final DatabaseReference ref = database.getReference("mp3");

	@Transactional
	@RequestMapping(value = "/holiday-setting/{holidayType}", method = RequestMethod.PUT)
	public String holidaySetting(@PathVariable int holidayType)  {
		var setDate = AccessUtil.getNextDate("yyyy-MM-dd");

		var cal = Calendar.getInstance(AccessUtil.TOKYO);
		cal.add(Calendar.DATE, 1);

		if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
			holidayType = 1;
		}

		if (!holidayMapper.isSetHoliday()) {
			holidayMapper.setHoliday(setDate, holidayType);
		}
		return "Set Holiday " + setDate + " to " + holidayType;
	}

	@RequestMapping(value = "/play-music", method = RequestMethod.PUT)
	public void playMusicWebhook(@RequestBody Map<String, String> postData)  {
		final var songName = postData.get("data");

		if (StringUtils.isBlank(songName)) {
			throw new IllegalArgumentException("IFTTT play music：" + postData);
		}

		var files = mp3Mapper.allMp3Data();

		final var songNameJa = AccessUtil.convertEn2Ja(songName, log);

		var entity = files.stream().filter(mp3 -> hasSongName(mp3, songNameJa, songName))
				.findFirst().orElse(null);

		String message;
		if (entity == null) {
			message = songName + "、は まだ登録がないようでござるよ。";
		} else {
			message = String.format(appParams.getMp3format(), entity.getFileName());
		}

		AccessUtil.postGoogleHome(message, log, appParams, 40);
	}

	@RequestMapping(value = "/speak-text", method = RequestMethod.PUT)
	public String speakTextWebHook(@RequestParam("text") String text)  {
		AccessUtil.postGoogleHome(text, log, appParams, 45);
		return "{}";
	}

	@RequestMapping(value = "/google-home-test", method = RequestMethod.POST)
	public void googleHomesVoiceRecognitionCharacterStringCheck(@RequestBody Map<String, String> postData) {
		try {
			new SlackHttpPost(
					"youtube-dl",
					"Google Home",
					"Google Homeは「" + postData.get("text") + "」と認識しました。",
					"https://4s.ambitious-i.net/icon/GoogleHome.png")
					.send(appParams);
		} catch (IOException e) {
			log.warn("Slack送信エラー", e);
		}
	}

	@RequestMapping(value = "/ifttt-set-holiday", method = RequestMethod.POST)
	public String iftttSetHoliday(@RequestBody Map<String, String> postData) {
		final var dateType = postData.get("data");

		if (StringUtils.isBlank(dateType)) {
			throw new IllegalArgumentException("IFTTT set Holiday：" + postData);
		}

		if (dateType.equals("1")) {
			holidayMapper.setHoliday(AccessUtil.getNow("yyyy-MM-dd"), 1);

		}
		if (dateType.equals("2")) {
			holidayMapper.setHoliday(AccessUtil.getNextDate("yyyy-MM-dd"), 1);
		}

		return "{}";
	}

	@RequestMapping(value = "/slack-proxy-notification",
			method = RequestMethod.POST,
			produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String slackProxyNotification(
			@RequestParam("token") String token,
			@RequestParam("channel_name") String channelName,
			@RequestParam("user_name") String userName,
			@RequestParam("text") String text) {

		if (!channelName.equals("everyday-talk")
				|| !token.equals(appParams.getIftttProxyToken())
				|| StringUtils.isEmpty(text)
				|| StringUtils.isEmpty(userName)) {
			log.warn("slack-proxy-notification：固定パラメータなし");
			return "{}";
		}

		var fcmTokens = userMapper.otherFcmTokens(userName);
		fcmTokens.forEach(fcmToken -> AccessUtil.sendFcm(
				AccessUtil.createTokenMessage(
						fcmToken,
						"Slack代理通知",
						text.replace("&lt;", "<").replace("&gt;", ">"),
						userName,
						"slack-proxy"
				), appParams, log)
		);

		return "{}";
	}

	@RequestMapping(value = "/alarm-notification/{user}", method = RequestMethod.PUT)
	public void alarmNotification(@PathVariable String user) {
		var fcmToken = userMapper.targetUsersFcmToken(user);
		if (StringUtils.isBlank(fcmToken)) {
			throw new NotFoundException();
		}
		AccessUtil.sendFcm(AccessUtil.createTokenMessage(
				fcmToken,
				"empty",
				"empty",
				"empty",
				"alarm"
		), appParams, log);
	}

	@RequestMapping(value = "/youtube-notification/{fileName}", method = RequestMethod.PUT)
	public void youtubeNotification(@PathVariable int fileName) {
	    String songName = mp3Mapper.songName(fileName);
		try {
			new SlackHttpPost(
					"youtube-dl",
					"おうちサーバー",
					"『" + songName + "』で登録しました。\\n『登録した " + songName + " 流して』と言ってみてください。",
					"https://4s.ambitious-i.net/icon/youtube_icon.png")
					.send(appParams);

			new SlackBinaryPost.Builder()
					.channels("youtube-dl")
					.title(songName)
					.fileName(songName + ".mp3")
					.fileData(AccessUtil.getBinaryBytes(String.format(appParams.getMp3format(), fileName)))
					.build(appParams.getSlackToken()).post(appParams);
		} catch (IOException e) {
			log.warn("Slack送信エラー", e);
		}
	}

	@Transactional
	@RequestMapping(value = "/youtube-dl",
			method = RequestMethod.POST,
			produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String youtubeDl(
			@RequestParam("token") String token,
			@RequestParam("channel_name") String channelName,
			@RequestParam("user_name") String userName,
			@RequestParam("text") String text) {

		if (!channelName.equals("youtube-dl") || !token.equals(appParams.getIftttYoutubeToken())) {
			log.warn("youtube-dl：固定パラメータなし");
			return "{}";
		}

		if (userMapper.allUsers().stream().noneMatch(userName::equals)) {
			return "{}";
		}

		if (text.equals("list") || text.equals("リスト") || text.equals("一覧")) {
			return "{\"text\":\""
					+ mp3Mapper.allMp3Data().stream().map(Mp3Entity::getSongName)
					.collect(Collectors.joining("\\n"))
					+ "\"}";
		}

		log.info("youtube-dl: " + text);

		if (!text.contains("http")) {
			return "{\"text\":\"コメントにYoutubeのURLが含まれていないようです。\\n<曲名><URL>\\nで送信してください。\"}";
		}

		var texts = text.split("<");
		if (texts.length != 2) {
			return "{\"text\":\"入力値が不正なようです。\\n<曲名><URL>\\nで送信してください。\"}";
		}

		var songName = texts[0];
		final var url = texts[1].split("\\|")[0];

		if (mp3Mapper.hasSongName(songName)) {
			return "{\"text\":\"「" + songName + "」は既に存在しています。\"}";
		}

		if (songName.equals("ねむり")) {
			int nextValue = autoIncrementsMapper.nextValue(AutoIncrementEnum.SLEEP_MUSIC.getType());
			songName += " " + nextValue;
			autoIncrementsMapper.insert(nextValue, AutoIncrementEnum.SLEEP_MUSIC.getType());
		}

		var nextFileName = mp3Mapper.nextFileName();

		ref.child("name").setValueAsync(String.valueOf(nextFileName));
		ref.child("url").setValueAsync(url);

		mp3Mapper.mp3insert(nextFileName, songName, url);

		return "{}";
	}

	@RequestMapping(value = "/play-sleep-music", method = RequestMethod.PUT)
	public void playSleepMusicWebhook()  {
		AccessUtil.postGoogleHome(
				String.format(
						appParams.getMp3format(),
						mp3Mapper.fileName(
								"ねむり "
										+ autoIncrementsMapper.randomValue(
												AutoIncrementEnum.SLEEP_MUSIC.getType()
								)
						)
				),
				log,
				appParams,
				25
		);
	}

	private boolean hasSongName(Mp3Entity mp3, String songNameJa, String songName) {
		return mp3.getSongName().toLowerCase().replace(" ", "")
				.equals(songNameJa.toLowerCase().replace(" ", ""))
				|| mp3.getSongName().toLowerCase().replace(" ", "")
				.equals(songName.toLowerCase().replace(" ", ""));
	}
}
