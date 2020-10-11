CREATE TABLE users
(
    user_name VARCHAR(10) NOT NULL COMMENT 'ユーザー名',
    in_home_flag TINYINT NOT NULL COMMENT '在宅フラグ',
    ip_address varchar(15) NOT NULL COMMENT '現在の IP アドレス',
    PRIMARY KEY (user_name)
) COMMENT 'ユーザー情報';
