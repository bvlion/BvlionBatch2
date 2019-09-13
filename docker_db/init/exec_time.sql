CREATE TABLE exec_time
(
    type INT NOT NULL COMMENT '1:from 2:to',
    hours INT NOT NULL COMMENT '時',
    minutes INT NOT NULL COMMENT '分',
    PRIMARY KEY (type)
) COMMENT 'お知らせ実行時間';
