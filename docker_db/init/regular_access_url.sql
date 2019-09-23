CREATE TABLE regular_access_url
(
    pk INT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    access_url VARCHAR(511) NOT NULL COMMENT 'アクセス先URL',
    enable_flag TINYINT NOT NULL COMMENT '有効フラグ',
    PRIMARY KEY (pk)
) COMMENT '定期アクセスURL';

