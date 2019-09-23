package net.ambitious.bvlion.batch2.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class RealtimeSettingEntity {
    private int displayNumber;
    private int startedFlag;
    private float temperature;
    private boolean monitoringCameraStarted;
}
