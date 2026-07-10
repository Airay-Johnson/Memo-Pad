package org.example.guard;

import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import dev.langchain4j.guardrail.InputGuardrailResult;

import java.util.regex.Pattern;

public class InputGuard implements InputGuardrail {

    private static final Pattern[] INJECTION_PATTERNS = {
        Pattern.compile("忽略\\s*(之前|以上|所有|一切|任何)\\s*指令", Pattern.CASE_INSENSITIVE),
        Pattern.compile("ignore\\s*(previous|above|all|every|any)\\s*instruction", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(你现在|现在你)\\s*(是|扮演|作为|变成)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(forget|discard|override|delete)\\s*(prompt|instruction|rule|system)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(system|assistant)\\s*(prompt|message)\\s*:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\b(DAN|jailbreak|越狱|roleplay)\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("输出\\s*(你|你的)\\s*(系统提示|system\\s*prompt|指令)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(print|show|output|repeat|display)\\s*(your|the)\\s*(system|initial)\\s*(prompt|instruction)", Pattern.CASE_INSENSITIVE),
    };

    @Override
    public InputGuardrailResult validate(InputGuardrailRequest request) {
        String text = request.userMessage().singleText();
        if (text == null || text.isBlank()) return success();

        for (Pattern p : INJECTION_PATTERNS) {
            if (p.matcher(text).find()) {
                return failure("检测到疑似 Prompt 注入，请求已被拒绝");
            }
        }
        return success();
    }
}
