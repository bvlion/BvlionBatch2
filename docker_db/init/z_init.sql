INSERT INTO `auto_increments` (`ai_value`, `ai_type`)
VALUES
	(1, 1),
	(2, 1),
	(3, 1),
	(4, 1);

INSERT INTO `mp3` (`file_name`, `song_name`, `song_url`, `volume`)
VALUES
	(1, 'パーフェクトワールド', '', 40),
	(2, 'シンクロニシティ', '', 40),
	(3, '限界突破サバイバー', '', 40),
	(4, 'フラッシュ', '', 40),
	(5, 'レーザービーム', '', 40),
	(6, 'パーフェクトヒューマン', '', 40),
	(7, '恋', '', 40),
	(8, '希望一縷', '', 40),
	(9, '前前前世', '', 40),
	(10, 'メリクリ', '', 40),
	(11, 'サイレントマジョリティ', '', 40),
	(12, '渡月橋', '', 40),
	(13, 'エアーマンが倒せない', '', 40),
	(14, '全然前世', '', 40),
	(15, 'エキサイト', '', 40),
	(16, '夢見る少女じゃいられない', '', 40),
	(17, '恋心', '', 40),
	(18, 'いつかのメリークリスマス', '', 40),
	(19, 'デザイアー', '', 40),
	(20, '冒険者たち', '', 40),
	(21, '雪の花', '', 40),
	(22, '月の雫', '', 40),
	(23, '月光', '', 40),
	(24, 'ディケイド', '', 40),
	(25, 'ライフイズショータイム', '', 40),
	(26, '鎧武乃風', '', 40),
	(27, '我ら思う', '', 40),
	(28, '脳漿炸裂ガール', '', 40),
	(29, 'ギプス', '', 40),
	(30, '革命デュアリズム', '', 40),
	(31, 'Hot limit', '', 40),
	(32, 'お正月メドレー', '', 40),
	(33, '春の海', '', 40),
	(34, 'HT', '', 40),
	(35, 'WHITE BREATH', '', 40),
	(36, 'FF8', '', 40),
	(37, '夢', '', 40),
	(38, 'wait for you', '', 40),
	(39, '少年', '', 40),
	(40, '忘却の空', '', 40),
	(41, '花火', '', 40),
	(42, 'チルドレンコード', '', 40),
	(43, 'ワタシ至上主義', '', 40),
	(44, '蛍火', '', 40),
	(45, 'ポーカー フェイス', '', 40),
	(46, '中だ', '', 40),
	(47, 'レモンピアノ', '', 40),
	(48, 'レモン', '', 40),
	(49, 'タスク駒レモン', '', 40),
	(50, 'シンデレラガール', '', 40),
	(51, '初恋', '', 40),
	(52, 'USA', '', 40),
	(53, 'イフ', '', 40),
	(54, 'Perfect Sky', '', 40),
	(55, 'フィクション', '', 40),
	(56, '君のとなり', '', 40),
	(57, '鬼太郎', '', 40),
	(58, 'オーバー クォーター', '', 40),
	(59, 'クリスマス洋楽', '', 40),
	(60, 'ディズニークリスマス', '', 40),
	(61, '打ち上げ花火', '', 40),
	(62, 'ルーザー', '', 40),
	(63, 'Get Wild', '', 40),
	(64, '桜ノ雨', '', 40),
	(65, '桜カラー', '', 40),
	(66, 'ライオン', '', 40),
	(67, 'キセキ', '', 40),
	(68, '千本桜', '', 40),
	(69, 'ランドメドレー', '', 40),
	(70, 'リメンバーミー', '', 40),
	(71, '親愛', '', 40),
	(72, 'RememberMe', '', 40),
	(73, '灰色と青', '', 40),
	(74, 'ハッピーバースデー', '', 40),
	(75, 'PHANTOM MINDS', '', 40),
	(76, 'メタノイア', '', 40),
	(77, 'ピアノジブリ', '', 40),
	(78, 'ヒプマイ', 'https://youtu.be/kJ-SE6dhjAg', 40),
	(79, '馬と鹿', 'https://www.youtube.com/watch?v=ptnYBctoexk', 40),
	(80, 'フラミンゴ', 'https://www.youtube.com/watch?v=Uh6dkL1M9DM', 40),
	(81, '渡り鳥', 'https://www.youtube.com/watch?v=O_DLtVuiqhI', 40),
	(82, 'アイネクライネ', 'https://www.youtube.com/watch?v=-EKxzId_Sj4', 40),
	(83, 'ターニングアップ', 'https://youtu.be/PhSdewBIQsc', 40),
	(84, '令和', 'https://youtu.be/Kj3gxZbNocw', 40),
	(85, 'ねむり 1', 'https://www.youtube.com/watch?v=h4w9MMrVwUM', 25),
	(86, 'ねむり 2', 'https://www.youtube.com/watch?v=AeZ_OlC7658', 25),
	(87, 'イグナイト', 'https://m.youtube.com/watch?v=_uxpHq3inUA', 40),
	(88, 'ねむり 3', 'https://youtu.be/qjWzjxIk6I8', 25),
	(89, 'ねむり 4', 'https://youtu.be/AIDT_3xtdjk', 15);

INSERT INTO `dating` (`pk`, `target_date`, `message`)
VALUES
	(1, '20130320', '今日で付き合って%s日目だよ♪'),
	(2, '20190320', '今日で結婚して%s日目だよ♪'),
	(3, '0320', '今日は付き合い始めた＆結婚した記念日だよ♪'),
	(4, '0319', '明日は付き合い始めた＆結婚した記念日だよ♪');

INSERT INTO `exec_time` (`type`, `hours`, `minutes`)
VALUES
	(1, 6, 29),
	(2, 7, 36),
	(3, 6, 51),
	(4, 7, 34);

INSERT INTO `holidays` (`check_date`, `holiday`)
VALUES
	('2018-12-24', 1),
	('2018-12-25', 0),
	('2018-12-31', 1),
	('2019-01-01', 1),
	('2019-01-02', 1),
	('2019-01-03', 1),
	('2019-01-04', 1),
	('2019-01-05', 1),
	('2019-01-06', 1),
	('2019-01-07', 0),
	('2019-01-08', 0),
	('2019-01-09', 0),
	('2019-01-10', 0),
	('2019-01-11', 0),
	('2019-01-12', 1),
	('2019-01-13', 1),
	('2019-01-14', 1),
	('2019-01-15', 0),
	('2019-01-16', 0),
	('2019-01-17', 0),
	('2019-01-18', 0),
	('2019-01-19', 1),
	('2019-01-20', 1),
	('2019-01-21', 0),
	('2019-01-22', 0),
	('2019-01-23', 0),
	('2019-01-24', 0),
	('2019-01-25', 0),
	('2019-01-26', 1),
	('2019-01-27', 1),
	('2019-01-28', 0),
	('2019-01-29', 0),
	('2019-01-30', 0),
	('2019-01-31', 0),
	('2019-02-01', 0),
	('2019-02-02', 1),
	('2019-02-03', 1),
	('2019-02-04', 0),
	('2019-02-05', 0),
	('2019-02-06', 0),
	('2019-02-07', 0),
	('2019-02-08', 0),
	('2019-02-09', 1),
	('2019-02-10', 1),
	('2019-02-11', 1),
	('2019-02-12', 0),
	('2019-02-13', 0),
	('2019-02-14', 0),
	('2019-02-15', 0),
	('2019-02-16', 1),
	('2019-02-17', 1),
	('2019-02-18', 0),
	('2019-02-19', 0),
	('2019-02-20', 0),
	('2019-02-21', 0),
	('2019-02-22', 0),
	('2019-02-23', 1),
	('2019-02-24', 1),
	('2019-02-25', 0),
	('2019-02-26', 0),
	('2019-02-27', 0),
	('2019-02-28', 0),
	('2019-03-01', 0),
	('2019-03-02', 1),
	('2019-03-03', 1),
	('2019-03-04', 0),
	('2019-03-05', 0),
	('2019-03-06', 0),
	('2019-03-07', 0),
	('2019-03-08', 0),
	('2019-03-09', 1),
	('2019-03-10', 1),
	('2019-03-11', 0),
	('2019-03-12', 0),
	('2019-03-13', 0),
	('2019-03-14', 0),
	('2019-03-15', 0),
	('2019-03-16', 1),
	('2019-03-17', 1),
	('2019-03-18', 0),
	('2019-03-19', 0),
	('2019-03-20', 1),
	('2019-03-21', 1),
	('2019-03-22', 0),
	('2019-03-23', 1),
	('2019-03-24', 1),
	('2019-03-25', 0),
	('2019-03-26', 0),
	('2019-03-27', 0),
	('2019-03-28', 0),
	('2019-03-29', 0),
	('2019-03-30', 1),
	('2019-03-31', 1),
	('2019-04-01', 0),
	('2019-04-02', 0),
	('2019-04-03', 0),
	('2019-04-04', 0),
	('2019-04-05', 0),
	('2019-04-06', 0),
	('2019-04-07', 1),
	('2019-04-08', 0),
	('2019-04-09', 0),
	('2019-04-10', 0),
	('2019-04-11', 0),
	('2019-04-12', 0),
	('2019-04-13', 1),
	('2019-04-14', 0),
	('2019-04-15', 0),
	('2019-04-16', 0),
	('2019-04-17', 0),
	('2019-04-18', 0),
	('2019-04-19', 0),
	('2019-04-20', 1),
	('2019-04-21', 1),
	('2019-04-22', 0),
	('2019-04-23', 0),
	('2019-04-24', 0),
	('2019-04-25', 0),
	('2019-04-26', 0),
	('2019-04-27', 1),
	('2019-04-28', 1),
	('2019-04-29', 1),
	('2019-04-30', 1),
	('2019-05-01', 1),
	('2019-05-02', 1),
	('2019-05-03', 1),
	('2019-05-04', 1),
	('2019-05-05', 1),
	('2019-05-06', 1),
	('2019-05-07', 0),
	('2019-05-08', 0),
	('2019-05-09', 0),
	('2019-05-10', 0),
	('2019-05-11', 1),
	('2019-05-12', 1),
	('2019-05-13', 0),
	('2019-05-14', 0),
	('2019-05-15', 0),
	('2019-05-16', 0),
	('2019-05-17', 0),
	('2019-05-18', 1),
	('2019-05-19', 1),
	('2019-05-20', 0),
	('2019-05-21', 0),
	('2019-05-22', 0),
	('2019-05-23', 0),
	('2019-05-24', 0),
	('2019-05-25', 1),
	('2019-05-26', 1),
	('2019-05-27', 0),
	('2019-05-28', 0),
	('2019-05-29', 0),
	('2019-05-30', 0),
	('2019-05-31', 0),
	('2019-06-01', 1),
	('2019-06-02', 1),
	('2019-06-03', 0),
	('2019-06-04', 0),
	('2019-06-05', 0),
	('2019-06-06', 0),
	('2019-06-07', 0),
	('2019-06-08', 1),
	('2019-06-09', 1),
	('2019-06-10', 0),
	('2019-06-11', 0),
	('2019-06-12', 0),
	('2019-06-13', 0),
	('2019-06-14', 0),
	('2019-06-15', 1),
	('2019-06-16', 1),
	('2019-06-17', 0),
	('2019-06-18', 0),
	('2019-06-19', 1),
	('2019-06-20', 0),
	('2019-06-21', 0),
	('2019-06-22', 1),
	('2019-06-23', 1),
	('2019-06-24', 0),
	('2019-06-25', 0),
	('2019-06-26', 0),
	('2019-06-27', 0),
	('2019-06-28', 0),
	('2019-06-29', 1),
	('2019-06-30', 1),
	('2019-07-01', 0),
	('2019-07-02', 0),
	('2019-07-03', 0),
	('2019-07-04', 0),
	('2019-07-05', 0),
	('2019-07-06', 1),
	('2019-07-07', 1),
	('2019-07-08', 0),
	('2019-07-09', 0),
	('2019-07-10', 0),
	('2019-07-11', 0),
	('2019-07-12', 0),
	('2019-07-13', 1),
	('2019-07-14', 1),
	('2019-07-15', 1),
	('2019-07-16', 0),
	('2019-07-17', 0),
	('2019-07-18', 0),
	('2019-07-19', 0),
	('2019-07-20', 1),
	('2019-07-21', 1),
	('2019-07-22', 0),
	('2019-07-23', 0),
	('2019-07-24', 0),
	('2019-07-25', 0),
	('2019-07-26', 0),
	('2019-07-27', 1),
	('2019-07-28', 1),
	('2019-07-29', 0),
	('2019-07-30', 0),
	('2019-07-31', 0),
	('2019-08-01', 0),
	('2019-08-02', 0),
	('2019-08-03', 1),
	('2019-08-04', 1),
	('2019-08-05', 0),
	('2019-08-06', 0),
	('2019-08-07', 0),
	('2019-08-08', 0),
	('2019-08-09', 0),
	('2019-08-10', 1),
	('2019-08-11', 1),
	('2019-08-12', 1),
	('2019-08-13', 1),
	('2019-08-14', 1),
	('2019-08-15', 1),
	('2019-08-16', 1),
	('2019-08-17', 1),
	('2019-08-18', 1),
	('2019-08-19', 1),
	('2019-08-20', 0),
	('2019-08-21', 0),
	('2019-08-22', 0),
	('2019-08-23', 0),
	('2019-08-24', 1),
	('2019-08-25', 1),
	('2019-08-26', 0),
	('2019-08-27', 0),
	('2019-08-28', 0),
	('2019-08-29', 0),
	('2019-08-30', 0),
	('2019-08-31', 1),
	('2019-09-01', 1),
	('2019-09-02', 0),
	('2019-09-03', 0),
	('2019-09-04', 0),
	('2019-09-05', 0),
	('2019-09-06', 0),
	('2019-09-07', 1),
	('2019-09-08', 1),
	('2019-09-09', 0),
	('2019-09-10', 0),
	('2019-09-11', 0),
	('2019-09-12', 0),
	('2019-09-13', 0),
	('2019-09-14', 1),
	('2019-09-15', 1),
	('2019-09-16', 1),
	('2019-09-17', 0),
	('2019-09-18', 0),
	('2019-09-19', 0),
	('2019-09-20', 0),
	('2019-09-21', 1),
	('2019-09-22', 1),
	('2019-09-23', 1),
	('2019-09-24', 0),
	('2019-09-25', 0),
	('2019-09-26', 0),
	('2019-09-27', 0),
	('2019-09-28', 1),
	('2019-09-29', 1),
	('2019-09-30', 0),
	('2019-10-01', 0),
	('2019-10-02', 0),
	('2019-10-03', 0),
	('2019-10-04', 0),
	('2019-10-05', 1),
	('2019-10-06', 1),
	('2019-10-07', 0),
	('2019-10-08', 0),
	('2019-10-09', 0),
	('2019-10-10', 0),
	('2019-10-11', 0),
	('2019-10-12', 1),
	('2019-10-13', 1),
	('2019-10-14', 1),
	('2019-10-15', 0),
	('2019-10-16', 0),
	('2019-10-17', 0),
	('2019-10-18', 0),
	('2019-10-19', 1),
	('2019-10-20', 1),
	('2019-10-21', 0),
	('2019-10-22', 1),
	('2019-10-23', 0),
	('2019-10-24', 0),
	('2019-10-25', 0),
	('2019-10-26', 1),
	('2019-10-27', 1),
	('2019-10-28', 0),
	('2019-10-29', 0),
	('2019-10-30', 0),
	('2019-10-31', 0),
	('2019-11-01', 0),
	('2019-11-02', 1),
	('2019-11-03', 1),
	('2019-11-04', 1),
	('2019-11-05', 0),
	('2019-11-06', 0),
	('2019-11-07', 0),
	('2019-11-08', 0),
	('2019-11-09', 1),
	('2019-11-10', 1),
	('2019-11-11', 0),
	('2019-11-12', 0),
	('2019-11-13', 0),
	('2019-11-14', 0),
	('2019-11-15', 0),
	('2019-11-16', 1),
	('2019-11-17', 1),
	('2019-11-18', 0),
	('2019-11-19', 0),
	('2019-11-20', 0),
	('2019-11-21', 0),
	('2019-11-22', 0),
	('2019-11-23', 1),
	('2019-11-24', 1),
	('2019-11-25', 0),
	('2019-11-26', 0),
	('2019-11-27', 0),
	('2019-11-28', 0),
	('2019-11-29', 0),
	('2019-11-30', 1),
	('2019-12-01', 1),
	('2019-12-02', 0),
	('2019-12-03', 0),
	('2019-12-04', 0),
	('2019-12-05', 0),
	('2019-12-06', 0),
	('2019-12-07', 1),
	('2019-12-08', 0),
	('2019-12-09', 0),
	('2019-12-10', 0),
	('2019-12-11', 0),
	('2019-12-12', 0),
	('2019-12-13', 0),
	('2019-12-14', 0),
	('2019-12-15', 1),
	('2019-12-16', 0),
	('2019-12-17', 0),
	('2019-12-18', 0),
	('2019-12-19', 0),
	('2019-12-20', 0),
	('2019-12-21', 1),
	('2019-12-22', 1),
	('2019-12-23', 0),
	('2019-12-24', 0),
	('2019-12-25', 0),
	('2019-12-26', 0),
	('2019-12-27', 0),
	('2019-12-28', 1),
	('2019-12-29', 1),
	('2019-12-30', 1),
	('2019-12-31', 1),
	('2020-01-01', 1),
	('2020-01-02', 1),
	('2020-01-03', 1),
	('2020-01-04', 1),
	('2020-01-05', 1),
	('2020-01-06', 0),
	('2020-01-07', 0),
	('2020-01-08', 0),
	('2020-01-09', 0),
	('2020-01-10', 0),
	('2020-01-11', 1),
	('2020-01-12', 1),
	('2020-01-13', 1),
	('2020-01-14', 0),
	('2020-01-15', 0),
	('2020-01-16', 0),
	('2020-01-17', 0),
	('2020-01-18', 1),
	('2020-01-19', 1),
	('2020-01-20', 0),
	('2020-01-21', 0),
	('2020-01-22', 0),
	('2020-01-23', 0),
	('2020-01-24', 0),
	('2020-01-25', 1),
	('2020-01-26', 1),
	('2020-01-27', 0),
	('2020-01-28', 0),
	('2020-01-29', 0),
	('2020-01-30', 1),
	('2020-01-31', 0),
	('2020-02-01', 1),
	('2020-02-02', 1),
	('2020-02-03', 0),
	('2020-02-04', 0),
	('2020-02-05', 0),
	('2020-02-06', 0),
	('2020-02-07', 0),
	('2020-02-08', 1),
	('2020-02-09', 1),
	('2020-02-10', 0),
	('2020-02-11', 1),
	('2020-02-12', 0),
	('2020-02-13', 0),
	('2020-02-14', 0),
	('2020-02-15', 1),
	('2020-02-16', 1),
	('2020-02-17', 0),
	('2020-02-18', 0),
	('2020-02-19', 0),
	('2020-02-20', 0),
	('2020-02-21', 0),
	('2020-02-22', 1),
	('2020-02-23', 1),
	('2020-02-24', 1),
	('2020-02-25', 0),
	('2020-02-26', 0),
	('2020-02-27', 0),
	('2020-02-28', 0),
	('2020-02-29', 1),
	('2020-03-01', 1),
	('2020-03-02', 1),
	('2020-03-03', 0),
	('2020-03-04', 0),
	('2020-03-05', 0),
	('2020-03-06', 0),
	('2020-03-07', 1),
	('2020-03-08', 1),
	('2020-03-09', 0),
	('2020-03-10', 0),
	('2020-03-11', 0),
	('2020-03-12', 0),
	('2020-03-13', 0),
	('2020-03-14', 1),
	('2020-03-15', 1),
	('2020-03-16', 0),
	('2020-03-17', 0),
	('2020-03-18', 0),
	('2020-03-19', 0),
	('2020-03-20', 1),
	('2020-03-21', 1),
	('2020-03-22', 1),
	('2020-03-23', 0),
	('2020-03-24', 0);

INSERT INTO `mail_api` (`pk`, `target_from`, `to_folder`, `channel`, `user_name`, `icon_url`, `prefix_format`, `enable_flag`)
VALUES
	(1, 'connpass', 'connpass', 'connpass-messages', 'connpass', 'https://connpass.com/static/img/apple_touch_icon.png', '-yyyy/MM/dd HH:mm', 1),
	(2, 'crashlytics', 'Fabric', 'server_api', 'Fabric', 'https://fabric.io/apple-touch-icon-fabric.png', '-yyyyMMdd', 1),
	(3, 'fabric', 'Fabric', 'server_api', 'Fabric', 'https://fabric.io/apple-touch-icon-fabric.png', '-yyyyMMdd', 1),
	(4, 'findy', '他技術', 'mail_api', 'Findy', 'https://pbs.twimg.com/profile_images/870884258314821633/4k-XqVIp_400x400.jpg', '-yyyy/MM/dd HH:mm', 1),
	(5, 'qiita', '他技術', 'other-tech', 'Qiita', 'https://cdn.qiita.com/assets/favicons/public/apple-touch-icon-ec5ba42a24ae923f16825592efdc356f.png', '-yyyy/MM/dd HH:mm', 1),
	(6, 'teratail', '他技術', 'other-tech', 'teratail', 'https://teratail.com/img/imgFacebookShare.png', '-yyyy/MM/dd HH:mm', 1),
	(7, 'paypal', 'paypal', NULL, NULL, NULL, NULL, 1),
	(8, 'domain', 'Trash', NULL, NULL, NULL, NULL, 1),
	(9, 'netapp', '他技術', NULL, NULL, NULL, NULL, 1),
	(10, 'doorkeeper', '他技術', NULL, NULL, NULL, NULL, 1),
	(11, 'codepen', '他技術', 'other-tech', 'CodePen', 'https://static.codepen.io/assets/social/facebook-default-05cf522ae1d4c215ae0f09d866d97413a2204b6c9339c6e7a1b96ab1d4a7340f.png', '-yyyyMMdd', 1),
	(12, 'heroku', '他技術', 'server_api', 'Heroku', 'https://www.herokucdn.com/images/og.png', NULL, 1),
	(13, 'newrelic', '既読', NULL, NULL, NULL, NULL, 1),
	(14, 'xserver.ne.jp', '既読', 'mail_api', 'XServer', 'https://www.xserver.ne.jp/img/common/apple-touch-icon-precomposed.png', NULL, 1),
	(15, 'paiza', '既読', 'mail_api', 'paiza', 'https://www.hrpro.co.jp/images/service/sp_service_photo00894_2_1.jpg', NULL, 1),
	(16, 'builds@circleci.com', '他技術', NULL, NULL, NULL, NULL, 1),
	(17, 'newsletter@circleci.com', '既読', 'mail_api', 'CircleCI', 'http://4s.ambitious-i.net/icon/circleci.png', '-yyyyMMdd', 1),
	(18, 'ecmail@ecml.coopdeli.jp', 'Trash', 'reminder', 'コープデリ', 'http://mirai.coopnet.or.jp/event/area_info/chiba/img/hopetan_cafe_01.png', '-yyyyMMdd', 1),
	(19, 'family-mail@ambitious-i.net', 'Trash', 'reminder', 'コープデリ', 'http://mirai.coopnet.or.jp/event/area_info/chiba/img/hopetan_cafe_01.png', '-yyyyMMdd', 1);

INSERT INTO `realtime_setting` (`disp_number`, `started_flag`, `temperature`, `monitoring_camera_started_flag`)
VALUES
	(1, 0, 0, 0);

INSERT INTO `regular_access_url` (`pk`, `access_url`, `enable_flag`)
VALUES
	(1, 'https://www.ambitious-i.net', 1),
	(2, 'https://yokosmemorial.ambitious-i.net', 1),
	(3, 'https://satopedia.ambitious-i.net/%E3%83%A1%E3%82%A4%E3%83%B3%E3%83%9A%E3%83%BC%E3%82%B8', 1),
	(4, 'https://redmine101.dip.jp', 1),
	(5, 'https://realm-saver.ambitious-i.net', 0),
	(6, 'https://home.ambitious-i.net/timer', 1),
	(7, 'https://satoshi-mai-wedding.web.app/login', 1),
	(8, '', 0);

INSERT INTO `twitter_channels` (`image_type`, `slack_channel`, `search_value`, `enable_flag`, `jorudan_flag`)
VALUES
	(1, 'C0FF29MV5', 'gudetama_sanrio', 1, 0),
	(2, 'C0JK97LSW', 'toraripikun', 0, 0),
	(3, 'C0JK9GW1Y', 'shimanekko_', 0, 0),
	(4, 'C5QE3A22U', 'mou_kaeru', 0, 0),
	(5, 'CA1QV200J', 'k_r_r_l_l_', 1, 0),
	(6, 'CKY8GEKL2', 'torinosashimi', 1, 0),
	(7, 'CKY8GEKL2', 'debu_dori', 1, 0),
	(8, 'asakusa-line-bot', '都営浅草線', 0, 1),
	(9, 'hanzoumon-line-bot', '東京メトロ半蔵門線', 0, 1),
	(10, 'ooedo-line-bot', '都営大江戸線', 1, 1);

INSERT INTO `users` (`user_name`, `in_home_flag`)
VALUES
	('gudetama', 0),
	('mai', 1),
	('satoshi', 1);
