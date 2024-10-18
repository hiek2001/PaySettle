package org.project.paysystem.entity;

public enum UserRoleEnum {
    USER(Authority.USER),
    SELLER(Authority.SELLER);

    private final String authority;

    UserRoleEnum(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return authority;
    }

    public static class Authority{
        public static final String USER = "ROLE_USER";
        public static final String SELLER = "ROLE_SELLER";
    }
}
