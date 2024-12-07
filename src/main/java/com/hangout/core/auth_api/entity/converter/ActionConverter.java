package com.hangout.core.auth_api.entity.converter;

import com.hangout.core.auth_api.entity.Action;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ActionConverter implements AttributeConverter<Action, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Action action) {
        if (action == null) {
            return null;
        }
        return action.getCode();
    }

    @Override
    public Action convertToEntityAttribute(Integer code) {
        if (code == null) {
            return null;
        }
        // return Stream.of(Action.values())
        // .filter(a -> a.getCode().equals(code))
        // .findFirst()
        // .orElseThrow(IllegalArgumentException::new);
        return Action.valueOf(code);
    }

}
