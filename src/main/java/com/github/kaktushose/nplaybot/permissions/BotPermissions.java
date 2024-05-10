package com.github.kaktushose.nplaybot.permissions;

import java.util.*;

public class BotPermissions {

    public static final String USER = "USER";
    public static final String MODIFY_USER_BALANCE = "MODIFY_USER_BALANCE";
    public static final String MODIFY_RANK_SETTINGS = "MODIFY_RANK_SETTINGS";
    public static final String MANAGE_EVENTS = "MANAGE_EVENTS";
    public static final String MODIFY_USER_PERMISSIONS = "MODIFY_USER_PERMISSIONS";
    public static final String BOT_ADMINISTRATOR = "BOT_ADMINISTRATOR";
    public static final String BOT_OWNER = "BOT_OWNER";

    @SuppressWarnings("PointlessBitwiseExpression")
    private static final Map<String, Integer> permissionMapping = new LinkedHashMap<>() {{
        put(USER, 1 << 0);
        put(MODIFY_USER_BALANCE, 1 << 1);
        put(MODIFY_RANK_SETTINGS, 1 << 2);
        put(MANAGE_EVENTS, 1 << 3);
        put(MODIFY_USER_PERMISSIONS, 1 << 4);
        put(BOT_ADMINISTRATOR, 1 << 5);
        put(BOT_OWNER, 1 << 8);
    }};


    public static int compute(Set<String> permissions) {
        int computedPermissions = 0;
        for (String permission : permissions) {
            computedPermissions = computedPermissions | getPermissionValue(permission);
        }
        return computedPermissions;
    }

    public static boolean hasPermissions(Set<String> permissions, int userPermission) {
        if (userPermission == getPermissionValue(BOT_OWNER)) {
            return true;
        }
        return (userPermission & compute(permissions)) != 0;
    }

    public static int combine(Collection<Integer> permissions) {
        int result = 0;
        for (int permission : permissions) {
            result |= permission;
        }
        return result;
    }

    public static Map<String, Integer> permissionsMapping() {
        return new LinkedHashMap<>(permissionMapping);
    }

    public static int getPermissionValue(String permission) {
        return permissionMapping.getOrDefault(permission, 0);
    }

    public static Set<Integer> getRawPermissionsValues(int permissions) {
        Set<Integer> result = new HashSet<>();
        permissionMapping.forEach((name, value) -> {
            if (hasPermissions(Set.of(name), permissions)) {
                result.add(value);
            }
        });
        return result;
    }

    public static String listPermissions(int permissions) {
        StringBuilder result = new StringBuilder();
        permissionMapping.forEach((name, value) -> {
            if (hasPermissions(Set.of(name), permissions)) {
                result.append(String.format("`%s`", name)).append("\n");
            }
        });
        return result.toString();
    }
}
