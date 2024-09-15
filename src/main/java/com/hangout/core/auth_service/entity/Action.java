package com.hangout.core.auth_service.entity;

public enum Action {
    LOGIN(0), HEART_BEAT(1), ROUTE_ACCESS(2), LOGOUT(3);

    private Integer code;

    private Action(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}