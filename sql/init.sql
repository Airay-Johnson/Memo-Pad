-- ============================================
-- Memo Pad v3 数据库初始化脚本
-- 新增: 知识库文档表 (knowledge_doc)
-- ============================================

CREATE DATABASE IF NOT EXISTS memopad
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE memopad;

CREATE TABLE IF NOT EXISTS admin (
  id       INT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50)  NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO admin (username, password) VALUES ('admin', '123456')
  ON DUPLICATE KEY UPDATE username = username;

CREATE TABLE IF NOT EXISTS note_group (
  id   INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS note (
  id          INT PRIMARY KEY AUTO_INCREMENT,
  title       VARCHAR(200) DEFAULT '无标题',
  content     TEXT,
  group_id    INT          DEFAULT NULL,
  deleted     TINYINT(1)   DEFAULT 0 COMMENT '0=正常 1=已删除',
  delete_time DATETIME     DEFAULT NULL,
  create_time DATETIME     DEFAULT NULL,
  update_time DATETIME     DEFAULT NULL,
  FOREIGN KEY (group_id) REFERENCES note_group(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS conversation (
  id          INT PRIMARY KEY AUTO_INCREMENT,
  session_id  VARCHAR(36)  NOT NULL COMMENT '会话标识',
  role        VARCHAR(20)  NOT NULL COMMENT 'user/assistant/tool',
  content     TEXT         NOT NULL,
  create_time DATETIME     DEFAULT now(),
  INDEX idx_session (session_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===== RAG 知识库 =====
CREATE TABLE IF NOT EXISTS knowledge_doc (
  id          INT PRIMARY KEY AUTO_INCREMENT,
  file_name   VARCHAR(200) NOT NULL COMMENT '文档名称',
  content     MEDIUMTEXT   NOT NULL COMMENT '原始内容',
  chunk_count INT          DEFAULT 0  COMMENT '切分后的片段数',
  created_at  DATETIME     DEFAULT NOW()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
