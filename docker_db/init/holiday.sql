CREATE TABLE holidays
(
    check_date DATE NOT NULL COMMENT '対象日付',
    holiday TINYINT NOT NULL COMMENT '祝日フラグ',
    PRIMARY KEY (check_date)
) COMMENT '祝日一覧';

