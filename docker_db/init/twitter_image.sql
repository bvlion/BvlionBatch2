CREATE TABLE twitter_image (
    image_type TINYINT NOT NULL COMMENT '1:ぐでたま等',
    media_url VARCHAR(127) NOT NULL COMMENT 'URL',
    text VARCHAR(1024) NOT NULL COMMENT 'メッセージ',
    posted_date DATETIME NOT NULL COMMENT '投稿日',
    PRIMARY KEY (media_url)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'Twitter画像投稿情報';
