-- ============================================
-- Memo Pad v3 建表脚本（多用户数据隔离）
-- 使用方式：IDEA Database面板 → 连接 memopad → 右键 Run
-- ============================================

CREATE TABLE IF NOT EXISTS admin (
  id       INT PRIMARY KEY AUTO_INCREMENT,
  email    VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(200) NOT NULL,
  name     VARCHAR(100) DEFAULT NULL,
  avatar   VARCHAR(500) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS note_group (
  id      INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT          NOT NULL,
  name    VARCHAR(100) NOT NULL,
  INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS note (
  id          INT PRIMARY KEY AUTO_INCREMENT,
  user_id     INT          NOT NULL,
  title       VARCHAR(200) DEFAULT '无标题',
  content     TEXT,
  group_id    INT          DEFAULT NULL,
  deleted     TINYINT(1)   DEFAULT 0 COMMENT '0=正常 1=已删除',
  delete_time DATETIME     DEFAULT NULL,
  create_time DATETIME     DEFAULT NULL,
  update_time DATETIME     DEFAULT NULL,
  INDEX idx_user (user_id),
  FOREIGN KEY (group_id) REFERENCES note_group(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS conversation (
  id          INT PRIMARY KEY AUTO_INCREMENT,
  user_id     INT          NOT NULL,
  session_id  VARCHAR(36)  NOT NULL COMMENT '会话标识',
  role        VARCHAR(20)  NOT NULL COMMENT 'user/assistant/tool',
  content     TEXT         NOT NULL,
  create_time DATETIME     DEFAULT now(),
  INDEX idx_session (user_id, session_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS knowledge_doc (
  id          INT PRIMARY KEY AUTO_INCREMENT,
  user_id     INT          NOT NULL,
  file_name   VARCHAR(200) NOT NULL COMMENT '文档名称',
  content     MEDIUMTEXT   NOT NULL COMMENT '原始内容',
  chunk_count INT          DEFAULT 0  COMMENT '切分后的片段数',
  created_at  DATETIME     DEFAULT NOW(),
  INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS audit_log (
  id          INT PRIMARY KEY AUTO_INCREMENT,
  user_id     INT          NOT NULL,
  session_id  VARCHAR(36)  NOT NULL COMMENT '会话标识',
  tool_name   VARCHAR(100) NOT NULL COMMENT '工具名',
  params      TEXT         COMMENT '调用参数',
  success     TINYINT(1)   DEFAULT 1 COMMENT '0=失败 1=成功',
  elapsed_ms  INT          COMMENT '耗时(毫秒)',
  result      TEXT         COMMENT '结果摘要',
  created_at  DATETIME     DEFAULT NOW(),
  INDEX idx_session (user_id, session_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
