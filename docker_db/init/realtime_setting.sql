CREATE TABLE realtime_setting
(
    disp_number INT NOT NULL COMMENT 'PK',
    started_flag INT NOT NULL COMMENT '起動フラグ（0:停止, 1:冷房, 2:暖房, 3:除湿）',
    temperature FLOAT NOT NULL COMMENT '起動温度',
    monitoring_camera_started_flag TINYINT NOT NULL COMMENT '監視カメラ起動フラグ',
    PRIMARY KEY (disp_number)
) COMMENT '現在の設定';
