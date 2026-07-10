package org.example.guard;

import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

/**
 * AI 安全护栏 — 对标简历中吴京爽的 "安全护栏与评估闭环"。
 *
 * 两个核心功能：
 * 1. 输入护栏：检测 Prompt 注入攻击（"忽略之前所有指令"等）
 * 2. 输出护栏：过滤敏感信息（身份证号、手机号等 PII 泄露）
 */
@Component
public class SecurityGuard {

    // 常见 Prompt 注入模式
    private static final Pattern[] INJECTION_PATTERNS = {
        Pattern.compile("忽略.*(之前|以上|所有|一切).*指令", Pattern.CASE_INSENSITIVE),
        Pattern.compile("ignore.*(previous|above|all|every).*instruction", Pattern.CASE_INSENSITIVE),
        Pattern.compile("你现在.*(是|扮演|作为)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(forget|discard|override).*(prompt|instruction|rule)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(system|assistant).*(prompt|message):", Pattern.CASE_INSENSITIVE),
        Pattern.compile("DAN|jailbreak|越狱"),
    };

    // 敏感信息正则（中国身份证、手机号）
    private static final Pattern[] PII_PATTERNS = {
        Pattern.compile("\\b1[3-9]\\d{9}\\b"),   // 手机号
        Pattern.compile("\\b\\d{17}[\\dXx]\\b"),  // 身份证号
        Pattern.compile("\\b\\d{16,19}\\b"),       // 银行卡号
    };

    /**
     * 输入安全检查。返回 null 表示通过，否则返回拦截原因。
     */
    public String validateInput(String userInput) {
        if (userInput == null || userInput.trim().isEmpty()) return null;

        for (Pattern p : INJECTION_PATTERNS) {
            if (p.matcher(userInput).find()) {
                return "[安全拦截] 检测到疑似 Prompt 注入攻击，请求已被拒绝。";
            }
        }
        return null;
    }

    /**
     * 输出安全检查。对 AI 回复中的敏感信息做脱敏处理。
     * 返回脱敏后的文本。
     */
    public String sanitizeOutput(String aiOutput) {
        if (aiOutput == null || aiOutput.trim().isEmpty()) return aiOutput;

        String result = aiOutput;
        for (Pattern p : PII_PATTERNS) {
            result = p.matcher(result).replaceAll(mr -> mask(mr.group()));
        }
        return result;
    }

    private String mask(String text) {
        if (text.length() <= 4) return "****";
        return text.substring(0, 3) + "****" + text.substring(text.length() - 4);
    }
}
