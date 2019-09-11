CREATE TABLE twitter_channels
(
    image_type INT NOT NULL AUTO_INCREMENT COMMENT '取得タイプ',
    slack_channel VARCHAR(127) NOT NULL COMMENT 'Slack投稿チャンネル',
    search_value VARCHAR(127) NOT NULL COMMENT 'Twitter検索条件',
    enable_flag TINYINT NOT NULL COMMENT '有効フラグ',
    jorudan_flag TINYINT NOT NULL COMMENT 'ジョルダンフラグ',
    PRIMARY KEY (image_type)
) COMMENT '取得するTwitterチャンネル';
