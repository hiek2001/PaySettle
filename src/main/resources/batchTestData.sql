INSERT INTO paysystem.global_pricing (unit_price, type, currency, min_views, max_views, created_at, updated_at)
VALUES
    (1, 'VIDEO', 'KRW', 0, 999999, NOW(), NOW()),           -- 조회수 10만 미만
    (1.1, 'VIDEO', 'KRW', 100000, 499999, NOW(), NOW()),    -- 조회수 10만 이상 50만 미만
    (1.3, 'VIDEO', 'KRW', 500000, 999999, NOW(), NOW()),    -- 조회수 50만 이상 100만 미만
    (1.5, 'VIDEO', 'KRW', 1000000, NULL, NOW(), NOW());     -- 조회수 100만 이상

INSERT INTO paysystem.global_pricing (unit_price, type, currency, min_views, max_views, created_at, updated_at)
VALUES
    (10, 'AD', 'KRW', 0, 999999, NOW(), NOW()),          -- 조회수 10만 미만
    (12, 'AD', 'KRW', 100000, 499999, NOW(), NOW()),     -- 조회수 10만 이상 50만 미만
    (15, 'AD', 'KRW', 500000, 999999, NOW(), NOW()),     -- 조회수 50만 이상 100만 미만
    (20, 'AD', 'KRW', 1000000, NULL, NOW(), NOW());      -- 조회수 100만 이상
