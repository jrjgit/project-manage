-- Drop all foreign key constraints. Data integrity is handled by code logic.
DROP PROCEDURE IF EXISTS drop_all_foreign_keys;
DELIMITER //
CREATE PROCEDURE drop_all_foreign_keys()
BEGIN
  DECLARE done INT DEFAULT FALSE;
  DECLARE tbl VARCHAR(255);
  DECLARE fk_name VARCHAR(255);
  DECLARE cur CURSOR FOR
    SELECT TABLE_NAME, CONSTRAINT_NAME
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND CONSTRAINT_TYPE = 'FOREIGN KEY';
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
  OPEN cur;
  read_loop: LOOP
    FETCH cur INTO tbl, fk_name;
    IF done THEN LEAVE read_loop; END IF;
    SET @s = CONCAT('ALTER TABLE `', tbl, '` DROP FOREIGN KEY `', fk_name, '`');
    PREPARE stmt FROM @s;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END LOOP;
  CLOSE cur;
END //
DELIMITER ;

CALL drop_all_foreign_keys();
DROP PROCEDURE drop_all_foreign_keys;
