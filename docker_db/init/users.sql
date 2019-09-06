CREATE TABLE users
(
    user_name VARCHAR(10) NOT NULL COMMENT 'ユーザー名',
    fcm_id VARCHAR(256) NOT NULL COMMENT 'FCMのID',
    in_home_flag TINYINT NOT NULL COMMENT '在宅フラグ',
    PRIMARY KEY (user_name)
) COMMENT 'ユーザー情報';
