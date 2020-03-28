CREATE TABLE mp3
(
    file_name INT NOT NULL COMMENT 'mp3ファイル名',
    song_name VARCHAR(256) NOT NULL COMMENT '曲名',
    song_url VARCHAR(256) NOT NULL COMMENT '曲URL',
    volume INT NOT NULL COMMENT '音量',
    PRIMARY KEY (file_name)
) COMMENT 'MP3情報';
