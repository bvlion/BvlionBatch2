package net.ambitious.bvlion.batch2.web.controller;

import com.google.firebase.database.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.ambitious.bvlion.batch2.entity.RealtimeSettingEntity;
import net.ambitious.bvlion.batch2.mapper.RealtimeSettingMapper;
import net.ambitious.bvlion.batch2.mapper.UserMapper;
import net.ambitious.bvlion.batch2.util.AccessUtil;
import net.ambitious.bvlion.batch2.util.AppParams;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserDataController {

	@NonNull
	private final UserMapper userMapper;

	@NonNull
	private final RealtimeSettingMapper realtimeSettingMapper;

	@NonNull
	private final AppParams appParams;

	private final FirebaseDatabase database = FirebaseDatabase.getInstance();
	private final DatabaseReference ref = database.getReference("camera/mode");

	@Transactional(readOnly = true)
	@RequestMapping(value = "/realtime_setting/select", method = RequestMethod.GET)
	public RealtimeSettingEntity selectRealtimeSetting() {
		return this.realtimeSettingMapper.selectRealtimeSetting();
	}

	@Transactional
	@RequestMapping(value = "/fcm_register/{user}/{fcmId}", method = RequestMethod.POST)
	public int saveUserFcm(@PathVariable String user, @PathVariable String fcmId) {
		return this.userMapper.fcmUpdate(user, fcmId);
	}

	@Transactional
	@RequestMapping(value = "/status/{user}/{mode}", method = RequestMethod.POST)
	public void saveUserStatus(@PathVariable String user, @PathVariable int mode) {
		// 在宅状況を更新
		this.userMapper.userModeUpdate(user, mode);

		// 在宅数取得
		int count = this.userMapper.userCount();
		// カメラ起動状態
		var realtimeSettingEntity = this.realtimeSettingMapper.selectRealtimeSetting();

		int cameraMode = -1;

		// 誰もおらず、起動していない場合は起動する
		if (count == 0 && !realtimeSettingEntity.isMonitoringCameraStarted()) {
			cameraMode = 1;
		}
		// 誰かいて、起動している状態であれば止める
		if (count > 0 && realtimeSettingEntity.isMonitoringCameraStarted()) {
			cameraMode = 0;
		}

		if (cameraMode > -1) {
			final var cameraModeValue = cameraMode;
			ref.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot snapshot) {
					ref.setValueAsync(cameraModeValue);
				}

				@Override
				public void onCancelled(DatabaseError error) { }
			});
			this.realtimeSettingMapper.updateMonitoringMode(cameraMode);

			AccessUtil.sendFcm(
					AccessUtil.createTopicMessage("empty", "empty", "monitor"),
					appParams,
					log
			);
		}
	}

	@Transactional
	@RequestMapping(value = "/aircon/{mode}/{temp}", method = RequestMethod.POST)
	public void saveAirconStatus(
			@PathVariable int mode,
			@PathVariable int temp,
			@RequestParam("text") String errorMessage
	) {
		String message;
		if (StringUtils.isEmpty(errorMessage)) {
			this.realtimeSettingMapper.updateAirconMode(mode, temp);
		}

		switch (mode) {
			case 0:
				message = "エアコンを停止させました。";
				break;
			case 1:
				message = String.format("冷房を%s度で起動させました。", temp);
				break;
			case 2:
				message = String.format("暖房を%s度で起動させました。", temp);
				break;
			case 3:
				message = "除湿を起動させました。";
				break;
			default:
				message = errorMessage;
		}

		AccessUtil.sendFcm(AccessUtil.createTopicMessage("エアコン起動情報", message, "aircon"), appParams, log);
	}
}
