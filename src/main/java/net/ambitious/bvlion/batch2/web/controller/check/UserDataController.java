package net.ambitious.bvlion.batch2.web.controller.check;

import com.google.firebase.database.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.ambitious.bvlion.batch2.entity.RealtimeSettingEntity;
import net.ambitious.bvlion.batch2.mapper.RealtimeSettingMapper;
import net.ambitious.bvlion.batch2.mapper.UserMapper;
import net.ambitious.bvlion.batch2.util.AccessUtil;
import net.ambitious.bvlion.batch2.util.AppParams;
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

	@Transactional(readOnly = true)
	@RequestMapping(value = "/realtime_setting/select", method = RequestMethod.GET)
	public RealtimeSettingEntity selectRealtimeSetting() {
		return this.realtimeSettingMapper.selectRealtimeSetting();
	}

	@Transactional
	@RequestMapping(value = "/fcm_register/{user}/{fcmId}", method = RequestMethod.POST)
	public int saveUserFcm(@PathVariable String user, @PathVariable String fcmId) {
		FirebaseDatabase database = FirebaseDatabase.getInstance();
		DatabaseReference ref = database.getReference("tokens/" + user);
		ref.setValueAsync(fcmId);
		return this.userMapper.fcmUpdate(user);
	}

	@Transactional
	@RequestMapping(value = "/status/{user}/{mode}/{ip}", method = RequestMethod.POST)
	public void saveUserStatus(@PathVariable String user, @PathVariable String ip, @PathVariable int mode) {
		// 在宅状況を更新
		this.userMapper.userModeUpdate(user, ip, mode);

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
			FirebaseDatabase database = FirebaseDatabase.getInstance();
			DatabaseReference ref = database.getReference("camera/mode");

			ref.setValueAsync(cameraMode);
			this.realtimeSettingMapper.updateMonitoringMode(cameraMode);

			AccessUtil.sendTopicMessage("empty", "empty", "monitor",
					appParams.getFirebaseFunctionUrl(), appParams.getFirebaseBasicAuth());
		}
	}

	@Transactional
	@RequestMapping(value = "/aircon/{mode}/{temps}", method = RequestMethod.POST)
	public void airconPost(
			@PathVariable int mode,
			@PathVariable int temp
	) {
		AccessUtil.airconRemoPost(
				mode,
				temp,
				appParams,
				realtimeSettingMapper
		);
	}
}
