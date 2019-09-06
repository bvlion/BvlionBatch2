CREATE TABLE timer_data
(
    disp_number TINYINT NOT NULL COMMENT '表示順',
    behavior_type TINYINT NOT NULL COMMENT '起動タイプ 1:エアコンON 2:エアコンOFF',
    aircon_type TINYINT NOT NULL COMMENT 'エアコンタイプ 1:冷房 2:除湿 3:暖房',
    temperature FLOAT NOT NULL COMMENT '起動温度',
    mon_started_flag TINYINT NOT NULL COMMENT '月曜日起動フラグ',
    tue_started_flag TINYINT NOT NULL COMMENT '火曜日起動フラグ',
    wed_started_flag TINYINT NOT NULL COMMENT '水曜日起動フラグ',
    thu_started_flag TINYINT NOT NULL COMMENT '木曜日起動フラグ',
    fri_started_flag TINYINT NOT NULL COMMENT '金曜日起動フラグ',
    sat_started_flag TINYINT NOT NULL COMMENT '土曜日起動フラグ',
    sun_started_flag TINYINT NOT NULL COMMENT '日曜日起動フラグ',
    holiday_decision_flag TINYINT NOT NULL COMMENT '祝日判定フラグ',
    enable_flag TINYINT NOT NULL COMMENT '有効フラグ',
    do_exec_time TIME NOT NULL COMMENT '実行時間',
    PRIMARY KEY (disp_number, behavior_type)
) COMMENT 'タイマー設定';
