CREATE TABLE IF NOT EXISTS `user` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    target_score DECIMAL(3,1) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS writing_task (
    id BIGINT NOT NULL AUTO_INCREMENT,
    task_type VARCHAR(20) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    image_url VARCHAR(500) NULL,
    difficulty VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS essay (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    content MEDIUMTEXT NOT NULL,
    word_count INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_essay_user (user_id),
    KEY idx_essay_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS essay_review (
    id BIGINT NOT NULL AUTO_INCREMENT,
    essay_id BIGINT NOT NULL,
    overall_score DECIMAL(3,1) NOT NULL,
    ta_score DECIMAL(3,1) NOT NULL,
    cc_score DECIMAL(3,1) NOT NULL,
    lr_score DECIMAL(3,1) NOT NULL,
    gra_score DECIMAL(3,1) NOT NULL,
    feedback_json JSON NULL,
    sample_essay MEDIUMTEXT NULL,
    comment TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_review_essay (essay_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS word (
    id BIGINT NOT NULL AUTO_INCREMENT,
    word VARCHAR(80) NOT NULL,
    phonetic VARCHAR(80) NULL,
    meaning VARCHAR(255) NOT NULL,
    example VARCHAR(500) NULL,
    category VARCHAR(40) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_word (word),
    KEY idx_word_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_word (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    word_id BIGINT NOT NULL,
    familiarity TINYINT NOT NULL DEFAULT 0,
    next_review_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_word (user_id, word_id),
    KEY idx_user_word_review (user_id, next_review_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reading_passage (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content MEDIUMTEXT NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reading_question (
    id BIGINT NOT NULL AUTO_INCREMENT,
    passage_id BIGINT NOT NULL,
    question_type VARCHAR(40) NOT NULL,
    question TEXT NOT NULL,
    options_json JSON NULL,
    answer VARCHAR(200) NOT NULL,
    explanation VARCHAR(500) NULL,
    PRIMARY KEY (id),
    KEY idx_question_passage (passage_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_answer (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer VARCHAR(200) NOT NULL,
    is_correct TINYINT(1) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_answer_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ai_call_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    scene VARCHAR(40) NOT NULL,
    prompt_tokens INT NULL,
    completion_tokens INT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_ai_log_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
