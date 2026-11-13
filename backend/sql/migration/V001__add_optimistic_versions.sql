-- 在目标数据库执行；仅添加版本字段，可重复执行。执行前备份现有数据。
DELIMITER $$
DROP PROCEDURE IF EXISTS haoxiang_add_optimistic_versions$$
CREATE PROCEDURE haoxiang_add_optimistic_versions()
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='haoxiang_mall_order' AND column_name='version') THEN
    ALTER TABLE haoxiang_mall_order ADD COLUMN version BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='haoxiang_mall_coupon_user' AND column_name='version') THEN
    ALTER TABLE haoxiang_mall_coupon_user ADD COLUMN version BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号';
  END IF;
END$$
CALL haoxiang_add_optimistic_versions()$$
DROP PROCEDURE haoxiang_add_optimistic_versions$$
DELIMITER ;
