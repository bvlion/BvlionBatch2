CREATE TABLE weather_searches (
    area_name VARCHAR(127) NOT NULL COMMENT '地域名',
    pc_url VARCHAR(511) NOT NULL COMMENT 'スクレイピング先URL',
    mobile_url VARCHAR(511) NOT NULL COMMENT '確認用スマホURL',
    user_agent VARCHAR(511) NOT NULL COMMENT 'アクセスユーザーエージェント',
    enable_flag TINYINT NOT NULL COMMENT '有効フラグ',
    PRIMARY KEY (area_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '天気情報';
