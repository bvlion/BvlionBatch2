CREATE TABLE dating
(
    pk INT NOT NULL COMMENT 'ソート用キー',
    target_date VARCHAR(10) NOT NULL COMMENT '対象日',
    message VARCHAR(256) NOT NULL COMMENT '出力メッセージ',
    PRIMARY KEY (pk)
) COMMENT '記念日メッセージ';
