package com.manage.managesystem.util;

import com.manage.managesystem.enums.SystemRoleEnum;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class RoleCodeUtils {
    private static final Set<String> LEGACY_USER_ROLE_CODES = Set.of(
            SystemRoleEnum.USER.name(),
            "PROJECT_MANAGER",
            "TEAM_MEMBER",
            "READ_ONLY"
    );

    private RoleCodeUtils() {
    }

    public static List<String> normalizeSystemRoles(List<String> roleCodes) {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        if (roleCodes == null) {
            return new ArrayList<>(normalized);
        }
        for (String roleCode : roleCodes) {
            if (roleCode == null || roleCode.isBlank()) {
                continue;
            }
            if (SystemRoleEnum.SYS_ADMIN.name().equals(roleCode)) {
                normalized.add(SystemRoleEnum.SYS_ADMIN.name());
                continue;
            }
            if (LEGACY_USER_ROLE_CODES.contains(roleCode)) {
                normalized.add(SystemRoleEnum.USER.name());
            }
        }
        return new ArrayList<>(normalized);
    }

    public static boolean hasBusinessUserRole(List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return false;
        }
        return roleCodes.stream().anyMatch(LEGACY_USER_ROLE_CODES::contains);
    }
}
