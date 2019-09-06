CREATE TABLE mp3
(
    file_name INT NOT NULL COMMENT 'mp3ファイル名',
    song_name VARCHAR(256) NOT NULL COMMENT '曲名',
    PRIMARY KEY (file_name)
) COMMENT 'MP3情報';
