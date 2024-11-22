USE paysystem;

# Video 테이블 데이터 삽입
INSERT INTO `video` (`duration`,`video_views`, `video_url`) VALUES
                                                                    (1, 101, 'video.url/1'),
                                                                    (2, 102, 'video.url/2'),
                                                                    (3, 103, 'video.url/3'),
                                                                    (4, 104, 'video.url/4'),
                                                                    (5, 105, 'video.url/5'),
                                                                    (6, 106, 'video.url/6'),
                                                                    (7, 107, 'video.url/7'),
                                                                    (8, 108, 'video.url/8'),
                                                                    (9, 109, 'video.url/9'),
                                                                    (10, 110, 'video.url/10');

# Ad 테이블 데이터 삽입
INSERT INTO `ad` (`ad_id`, `ad_url`) VALUES
                                            (1, 'ad.url/1'),
                                            (2, 'ad.url/2'),
                                            (3, 'ad.url/3'),
                                            (4, 'ad.url/4'),
                                            (5, 'ad.url/5'),
                                            (6, 'ad.url/6'),
                                            (7, 'ad.url/7'),
                                            (8, 'ad.url/8'),
                                            (9, 'ad.url/9'),
                                            (10, 'ad.url/10');

# GlobalPricing 테이블 데이터 삽입 (조회수에 따른 정산 단가 표)
INSERT INTO `global_pricing` (`unit_price`, `type`, `currency`, `min_views`, `max_views`, `created_at`, `updated_at`)
VALUES
    (1, 'VIDEO', 'KRW', 0, 999999, NOW(), NOW()),           -- 조회수 10만 미만
    (1.1, 'VIDEO', 'KRW', 100000, 499999, NOW(), NOW()),    -- 조회수 10만 이상 50만 미만
    (1.3, 'VIDEO', 'KRW', 500000, 999999, NOW(), NOW()),    -- 조회수 50만 이상 100만 미만
    (1.5, 'VIDEO', 'KRW', 1000000, 0, NOW(), NOW()),     -- 조회수 100만 이상
    (10, 'AD', 'KRW', 0, 999999, NOW(), NOW()),          -- 조회수 10만 미만
    (12, 'AD', 'KRW', 100000, 499999, NOW(), NOW()),     -- 조회수 10만 이상 50만 미만
    (15, 'AD', 'KRW', 500000, 999999, NOW(), NOW()),     -- 조회수 50만 이상 100만 미만
    (20, 'AD', 'KRW', 1000000, 0, NOW(), NOW());      -- 조회수 100만 이상

