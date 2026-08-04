package hu.smartinventory.user.dto;

import hu.smartinventory.user.entity.Role;

public record RoleResponse(
        Long id,
        String code,
        String name,
        String description,
        boolean active
) {

    public static RoleResponse from(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getCode(),
                role.getName(),
                role.getDescription(),
                role.isActive()
        );
    }
}