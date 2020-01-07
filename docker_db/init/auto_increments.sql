CREATE TABLE auto_increments
(
    ai_value INT NOT NULL COMMENT '連番',
    ai_type INT NOT NULL COMMENT 'タイプ',
    PRIMARY KEY (ai_value, ai_type)
) COMMENT '連番情報';
