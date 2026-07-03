package org.example.entity;

/**
 * AI 可调用的工具类型。
 * 每个枚举值包含：
 * - description：告诉 AI 这个工具是干什么的
 * - paramNames：这个工具需要哪些参数（null表示不需要参数）
 */
public enum ToolType {

    LIST_NOTES("获取用户所有笔记的列表，返回标题和ID，不需要参数", null),
    READ_NOTE("读取指定笔记的完整内容，需要笔记ID", "noteId"),
    CREATE_NOTE("创建一条新笔记，需要标题和内容", "title", "content"),
    UPDATE_NOTE("修改已有笔记的标题或内容，需要笔记ID，title和content至少传一个", "noteId", "title", "content"),
    DELETE_NOTE("删除指定笔记（移至回收站），需要笔记ID", "noteId"),
    SEARCH_NOTE("按关键词搜索笔记，匹配标题和内容，需要关键词", "keyword"),
    MOVE_NOTE("把笔记移动到某个分组，需要笔记ID和分组ID", "noteId", "groupId"),
    LIST_GROUPS("获取所有分组的列表，返回名称和ID，不需要参数", null),
    ;

    private final String description;
    private final String[] paramNames;

    ToolType(String description, String... paramNames) {
        this.description = description;
        this.paramNames = paramNames;
    }

    public String getDescription() {
        return description;
    }

    public String[] getParamNames() {
        return paramNames;
    }
}
