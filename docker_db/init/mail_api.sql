CREATE TABLE mail_api
(
    pk INT NOT NULL AUTO_INCREMENT COMMENT 'キー',
    target_from VARCHAR(256) NOT NULL COMMENT '対象送信メールアドレス',
    to_folder VARCHAR(256) NOT NULL COMMENT '移動するフォルダー',
    channel VARCHAR(256) COMMENT 'slack のチャンネル',
    user_name VARCHAR(256) COMMENT 'slack のユーザー名',
    icon_url VARCHAR(256) COMMENT 'slack の icon url',
    prefix_format VARCHAR(256) COMMENT 'ユーザー名に付与する date format',
    enable_flag TINYINT NOT NULL COMMENT '有効フラグ',
    PRIMARY KEY (pk)
) COMMENT 'メール移動Slack情報';
