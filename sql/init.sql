-- ============================================
-- Memo Pad 数据库初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS memopad
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE memopad;

-- -------------------------------------------
-- 用户表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS admin (
  id       INT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50)  NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 默认用户：admin / 123456
INSERT INTO admin (username, password) VALUES ('admin', '123456')
  ON DUPLICATE KEY UPDATE username = username;

-- -------------------------------------------
-- 笔记分组表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS note_group (
  id   INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------
-- 笔记表
-- -------------------------------------------
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
