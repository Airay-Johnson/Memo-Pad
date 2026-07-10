package org.example.common;

public class UserContext {
    private static final ThreadLocal<Integer> holder = new ThreadLocal<>();

    public static void setUserId(Integer userId) {
        holder.set(userId);
    }

    public static Integer getUserId() {
        return holder.get();
    }

    public static void clear() {
        holder.remove();
    }
}
