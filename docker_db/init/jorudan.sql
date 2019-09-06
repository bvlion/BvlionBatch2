CREATE TABLE jorudan (
    posted_date DATETIME NOT NULL COMMENT 'ユーザーID',
    detail VARCHAR(256) NOT NULL COMMENT '情報',
    description VARCHAR(1024) COMMENT '詳細',
    url VARCHAR(255) NOT NULL COMMENT 'URL',
    PRIMARY KEY (url)
) COMMENT 'ジョルダンライブ投稿情報';