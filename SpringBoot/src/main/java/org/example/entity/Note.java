package org.example.entity;

import java.time.LocalDateTime;

public class Note {
    private Integer id;
    private Integer userId;
    private String title;
    private String content;
    private Integer groupId;
    private Integer deleted;
    private LocalDateTime deleteTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getGroupId() { return groupId; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
    public LocalDateTime getDeleteTime() { return deleteTime; }
    public void setDeleteTime(LocalDateTime deleteTime) { this.deleteTime = deleteTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
