package com.hangout.core.auth_service.entity;

public enum Action {
    LOGIN(0), RENEW_TOKEN(1), PREMATURE_TOKEN_RENEW(2), LOGOUT(3);

    private Integer code;

    private Action(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}