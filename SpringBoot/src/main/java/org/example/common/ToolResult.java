package org.example.common;

/**
 * 工具执行结果。
 * success: 执行成功还是失败
 * content: 执行结果（以文字形式给 AI 阅读）
 */
public class ToolResult {

    private boolean success;
    private String content;

    public ToolResult() {}

    public ToolResult(boolean success, String content) {
        this.success = success;
        this.content = content;
    }



    /** 执行成功 */
    public static ToolResult ok(String content) {
        return new ToolResult(true, content);
    }

    /** 执行失败 */
    public static ToolResult fail(String reason) {
        return new ToolResult(false, reason);
    }

    // ---- getter / setter ----

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
