package com.atchensong.common;

public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();
    private static ThreadLocal<String> roleLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    public static Long getCurrentId() {
        return threadLocal.get();
    }

    public static void setCurrentRole(String role) {
        roleLocal.set(role);
    }

    public static String getCurrentRole() {
        return roleLocal.get();
    }

    public static boolean isGuest() {
        return getCurrentId() == null;
    }

    public static void clear() {
        threadLocal.remove();
        roleLocal.remove();
    }
}
