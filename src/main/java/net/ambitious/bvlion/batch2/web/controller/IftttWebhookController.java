package net.ambitious.bvlion.batch2.web.controller;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.ambitious.bvlion.batch2.entity.Mp3Entity;
import net.ambitious.bvlion.batch2.enums.AutoIncrementEnum;
import net.ambitious.bvlion.batch2.mapper.AutoIncrementsMapper;
import net.ambitious.bvlion.batch2.mapper.Mp3Mapper;
import net.ambitious.bvlion.batch2.mapper.UserMapper;
import net.ambitious.bvlion.batch2.util.AccessUtil;
import net.ambitious.bvlion.batch2.util.AppParams;
import net.ambitious.bvlion.batch2.util.SlackBinaryPost;
import net.ambitious.bvlion.batch2.util.SlackHttpPost;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
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
	private final UserMapper userMapper;

	@NonNull
	private final AutoIncrementsMapper autoIncrementsMapper;

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
		int volume = 40;
		if (entity == null) {
			message = songName + "、は まだ登録がないようでござるよ。";
		} else {
			message = String.format(appParams.getMp3format(), entity.getFileName());
			volume = entity.getVolume();
		}

		AccessUtil.postGoogleHome(message, log, appParams, volume);
	}

	@RequestMapping(value = "/speak-time", method = RequestMethod.PUT)
	public String speakTimeWebHook() {
		var text = "時刻は" + AccessUtil.getHm() + "です";
		AccessUtil.postGoogleHome(text, log, appParams, 45);
		return text;
	}

	@RequestMapping(value = "/speak-text", method = RequestMethod.PUT)
	public String speakTextWebHook(
			@RequestParam("text") String text,
			@RequestParam(value = "volume", required = false, defaultValue = "45") int volume
	) {
		AccessUtil.postGoogleHome(text, log, appParams, volume);
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

		AccessUtil.sendTokenMessage(
				userMapper.fcmSendUsers(userName),
				"Slack代理通知",
				text.replace("&lt;", "<").replace("&gt;", ">"),
				userName,
				"slack-proxy",
				appParams.getFirebaseFunctionUrl(),
				appParams.getFirebaseBasicAuth()
		);

		return "{}";
	}

	@RequestMapping(value = "/alarm-notification/{user}", method = RequestMethod.PUT)
	public void alarmNotification(@PathVariable String user) {
		AccessUtil.sendTokenMessage(
				List.of(user),
				"empty",
				"empty",
				"empty",
				"alarm",
				appParams.getFirebaseFunctionUrl(),
				appParams.getFirebaseBasicAuth()
		);
	}

	@RequestMapping(value = "/youtube-notification/{fileName}", method = RequestMethod.PUT)
	public void youtubeNotification(@PathVariable int fileName) {
	    String songName = mp3Mapper.selectSongName(fileName);
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

		int volume = 40;
		if (songName.equals("ねむり")) {
			int nextValue = autoIncrementsMapper.nextValue(AutoIncrementEnum.SLEEP_MUSIC.getType());
			songName += " " + nextValue;
			autoIncrementsMapper.insert(nextValue, AutoIncrementEnum.SLEEP_MUSIC.getType());
			volume = 25;
		}

		var nextFileName = mp3Mapper.nextFileName();

		FirebaseDatabase database = FirebaseDatabase.getInstance();
		DatabaseReference ref = database.getReference("mp3");
		ref.child("name").setValueAsync(String.valueOf(nextFileName));
		ref.child("url").setValueAsync(url);

		mp3Mapper.mp3insert(nextFileName, songName, url, volume);

		return "{}";
	}

	@RequestMapping(value = "/play-sleep-music", method = RequestMethod.PUT)
	public void playSleepMusicWebhook()  {
		var entity = mp3Mapper.selectDataFromSongName(
				"ねむり "
						+ autoIncrementsMapper.randomValue(
						AutoIncrementEnum.SLEEP_MUSIC.getType()
				)
		);
		AccessUtil.postGoogleHome(
				String.format(
						appParams.getMp3format(),
						entity.getFileName()
				),
				log,
				appParams,
				entity.getVolume()
		);
	}

	private boolean hasSongName(Mp3Entity mp3, String songNameJa, String songName) {
		return mp3.getSongName().toLowerCase().replace(" ", "")
				.equals(songNameJa.toLowerCase().replace(" ", ""))
				|| mp3.getSongName().toLowerCase().replace(" ", "")
				.equals(songName.toLowerCase().replace(" ", ""));
	}
}
