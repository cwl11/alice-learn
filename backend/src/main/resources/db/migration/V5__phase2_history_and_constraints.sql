-- 邮箱唯一
ALTER TABLE `user` ADD UNIQUE KEY uk_user_email (email);

-- 作文保存已生成的 AI 范文
ALTER TABLE essay ADD COLUMN sample_essay MEDIUMTEXT NULL AFTER status;

-- 阅读练习记录（一次提交 = 一条 attempt）
CREATE TABLE IF NOT EXISTS reading_attempt (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    passage_id BIGINT NOT NULL,
    total INT NOT NULL,
    correct_count INT NOT NULL,
    time_spent_sec INT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_reading_attempt_user (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE user_answer
    ADD COLUMN attempt_id BIGINT NULL AFTER question_id,
    ADD KEY idx_user_answer_attempt (attempt_id);
