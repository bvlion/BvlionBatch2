CREATE TABLE users
(
    user_name VARCHAR(10) NOT NULL COMMENT 'ユーザー名',
    in_home_flag TINYINT NOT NULL COMMENT '在宅フラグ',
    PRIMARY KEY (user_name)
) COMMENT 'ユーザー情報';
