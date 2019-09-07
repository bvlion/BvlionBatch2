package net.ambitious.bvlion.batch2.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
public class TimerEntity {
    private int dispNumber;
    private int behaviorType;
    private int airconType;
    private float temperature;
    private boolean monStarted;
    private boolean tueStarted;
    private boolean wedStarted;
    private boolean thuStarted;
    private boolean friStarted;
    private boolean satStarted;
    private boolean sunStarted;
    private boolean holidayDecision;
    private boolean enable;
    private Date doExecTime;
    private String doExecTimeString;
}
