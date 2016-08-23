-- -----------------------------------------------------
-- Schema burnaki
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `burnaki` ;

-- -----------------------------------------------------
-- Schema burnaki
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `burnaki` DEFAULT CHARACTER SET utf8 ;
USE `burnaki` ;

-- -----------------------------------------------------
-- Table `burnaki`.`burnination`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `burnaki`.`burnination` ;

CREATE TABLE IF NOT EXISTS `burnaki`.`burnination` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `tag` VARCHAR(35) CHARACTER SET 'utf8' NOT NULL,
  `start_date` DATETIME NOT NULL,
  `end_date` DATETIME NULL DEFAULT NULL,
  `meta_link` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  `room_id` INT(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC))
ENGINE = InnoDB
AUTO_INCREMENT = 30
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_unicode_ci;


-- -----------------------------------------------------
-- Table `burnaki`.`burnination_progress`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `burnaki`.`burnination_progress` ;

CREATE TABLE IF NOT EXISTS `burnaki`.`burnination_progress` (
  `burnination_id` BIGINT(20) NOT NULL,
  `progress_date` DATETIME NOT NULL,
  `total_questions` INT(11) NOT NULL,
  `closed` INT(11) NOT NULL,
  `roombad` INT(11) NOT NULL,
  `manually_deleted` INT(11) NOT NULL,
  `retagged` INT(11) NOT NULL,
  PRIMARY KEY (`burnination_id`, `progress_date`),
  INDEX `fk_burnination_progress_burnination_idx` (`burnination_id` ASC),
  CONSTRAINT `fk_burnination_progress_burnination`
    FOREIGN KEY (`burnination_id`)
    REFERENCES `burnaki`.`burnination` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_unicode_ci;


-- -----------------------------------------------------
-- Table `burnaki`.`burnination_question`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `burnaki`.`burnination_question` ;

CREATE TABLE IF NOT EXISTS `burnaki`.`burnination_question` (
  `burnination_id` BIGINT(20) NOT NULL,
  `question_id` INT(11) NOT NULL,
  `created_date` DATETIME NOT NULL,
  `close_vote_count` INT(11) NOT NULL DEFAULT '0',
  `reopen_vote_count` INT(11) NOT NULL DEFAULT '0',
  `delete_vote_count` INT(11) NOT NULL DEFAULT '0',
  `undelete_vote_count` INT(11) NOT NULL DEFAULT '0',
  `closed` TINYINT(1) NOT NULL DEFAULT '0',
  `roombad` TINYINT(1) NOT NULL DEFAULT '0',
  `manually_deleted` TINYINT(1) NOT NULL DEFAULT '0',
  `retagged` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`burnination_id`, `question_id`),
  INDEX `fk_burnination_questions_burnination_idx` (`burnination_id` ASC),
  CONSTRAINT `fk_burnination_questions_burnination`
    FOREIGN KEY (`burnination_id`)
    REFERENCES `burnaki`.`burnination` (`id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_unicode_ci;


-- -----------------------------------------------------
-- Table `burnaki`.`burnination_question_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `burnaki`.`burnination_question_history` ;

CREATE TABLE IF NOT EXISTS `burnaki`.`burnination_question_history` (
  `burnination_id` BIGINT(20) NOT NULL,
  `question_id` INT(11) NOT NULL,
  `event_date` DATETIME NOT NULL,
  `event_type` VARCHAR(45) CHARACTER SET 'utf8' NOT NULL,
  PRIMARY KEY (`burnination_id`, `question_id`, `event_date`),
  CONSTRAINT `fk_burnination_question_history_burnination`
    FOREIGN KEY (`burnination_id` , `question_id`)
    REFERENCES `burnaki`.`burnination_question` (`burnination_id` , `question_id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_unicode_ci;


-- -----------------------------------------------------
-- Table `burnaki`.`se_api_question_cache`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `burnaki`.`se_api_question_cache` ;

CREATE TABLE IF NOT EXISTS `burnaki`.`se_api_question_cache` (
  `id` INT(11) NOT NULL,
  `title` VARCHAR(250) CHARACTER SET 'utf8' NOT NULL,
  `share_link` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  `deleted_date` DATETIME NULL DEFAULT NULL,
  `created_date` DATETIME NOT NULL,
  `score` INT(11) NOT NULL,
  `answer_count` INT(11) NOT NULL,
  `locked` TINYINT(1) NOT NULL,
  `migrated` TINYINT(1) NOT NULL,
  `view_count` INT(11) NOT NULL,
  `comment_count` INT(11) NOT NULL,
  `closed_date` DATETIME NULL DEFAULT NULL,
  `closed_as_duplicate` TINYINT(1) NOT NULL,
  `answered` TINYINT(1) NOT NULL,
  `with_accepted_answer` TINYINT(1) NOT NULL,
  `close_vote_count` INT(11) NOT NULL,
  `reopen_vote_count` INT(11) NOT NULL,
  `tags` VARCHAR(255) CHARACTER SET 'utf8' NOT NULL,
  `last_edit_date` DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_unicode_ci;
