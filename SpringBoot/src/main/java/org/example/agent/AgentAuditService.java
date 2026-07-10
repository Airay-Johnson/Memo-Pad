package org.example.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AgentAuditService {

    private static final Logger log = LoggerFactory.getLogger("AGENT_AUDIT");

    public void logToolCall(String toolName, String params, long startMs, long endMs, boolean success, String result) {
        long elapsed = endMs - startMs;
        log.info("[{}] tool={} params={} elapsed={}ms success={} result={}",
            selectStatus(success, elapsed), toolName, sanitize(params), elapsed, success, truncate(result));
    }

    public long now() {
        return System.currentTimeMillis();
    }

    private String selectStatus(boolean success, long elapsed) {
        if (!success) return "FAILED";
        if (elapsed > 5000) return "SLOW";
        return "OK";
    }

    private String sanitize(String s) {
        if (s == null) return "-";
        if (s.length() > 200) return s.substring(0, 200) + "...";
        return s;
    }

    private String truncate(String s) {
        if (s == null) return "-";
        if (s.length() > 100) return s.substring(0, 100) + "...";
        return s;
    }
}
