package net.ambitious.bvlion.batch2.web.controller;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.ambitious.bvlion.batch2.enums.HolidayEnum;
import net.ambitious.bvlion.batch2.enums.TimerDateEnum;
import net.ambitious.bvlion.batch2.mapper.ExecTimeMapper;
import net.ambitious.bvlion.batch2.mapper.HolidayMapper;
import net.ambitious.bvlion.batch2.mapper.TimerDataMapper;
import net.ambitious.bvlion.batch2.util.AccessUtil;
import net.ambitious.bvlion.batch2.util.AppParams;
import net.ambitious.bvlion.batch2.util.SlackHttpPost;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Calendar;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/time/${url.ifttt}")
public class TimesController {

	@NonNull
	private final ExecTimeMapper execTimeMapper;

	@NonNull
	private final HolidayMapper holidayMapper;

	@NonNull
	private final TimerDataMapper timerDataMapper;

	@NonNull
	private final AppParams appParams;

	private final FirebaseDatabase database = FirebaseDatabase.getInstance();
	private final DatabaseReference ref = database.getReference("node/infrared");

	@RequestMapping(value = "/is/time-notification", method = RequestMethod.GET)
	public int isTimeNotification()  {
		return AccessUtil.isExecTime(holidayMapper.isHoliday(), execTimeMapper.selectExecTimes()) ? 1 : 0;
	}

	@RequestMapping(value = "/alarm-notification", method = RequestMethod.PUT)
	public void alarmNotification() throws IOException {
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
	}

	@RequestMapping(value = "/coop-notification", method = RequestMethod.PUT)
	public void coopNotification() throws IOException {
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
	}

	@RequestMapping(value = "/temperature-detection", method = RequestMethod.PUT)
	public void temperatureDetection() {
		if (!holidayMapper.isHoliday()) {
			AccessUtil.postGoogleHome(
					"おはようございます。検温は済んでいますか？",
					log,
					appParams
			);
		}
	}

	@RequestMapping(value = "/timer", method = RequestMethod.PUT)
	public void timer() {
		var cal = Calendar.getInstance(AccessUtil.TOKYO);
		timerDataMapper.selectExecTimerSetting(
				TimerDateEnum.columnName(cal.get(Calendar.DAY_OF_WEEK)),
				FastDateFormat.getInstance("HH:mm", AccessUtil.TOKYO).format(cal) + ":00"
		).stream().map(entity -> {
			if (entity.isHolidayDecision() && holidayMapper.isHoliday()) {
				return null;
			}
			switch (entity.getBehaviorType()) {
				case 1: // エアコンON
					var param = new StringBuilder("\" timer" + System.currentTimeMillis() + " … ");
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
		}).filter(StringUtils::isNotEmpty).forEach(ref::setValueAsync);
	}
}