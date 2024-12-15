package com.hangout.core.auth_api.entity;

import java.util.HashMap;
import java.util.Map;

public enum Action {
    LOGIN(0), RENEW_TOKEN(1), PREMATURE_TOKEN_RENEW(2), LOGOUT(3), TRUSTED_SESSION_START(4);

    private Integer code;
    private static Map<Integer, Action> map = new HashMap<>();

    private Action(Integer code) {
        this.code = code;
    }

    static {
        for (Action ac : Action.values()) {
            map.put(ac.code, ac);
        }
    }

    public static Action valueOf(Integer code) {
        return map.get(code);
    }

    public Integer getCode() {
        return code;
    }
}