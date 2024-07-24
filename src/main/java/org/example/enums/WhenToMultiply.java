package org.example.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

public enum WhenToMultiply {

    SAME_SYMBOLS("same_symbols"),
    LINEAR_SYMBOLS("linear_symbols");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    WhenToMultiply(String value) {
        this.value = value;
    }

    @JsonCreator
    public static WhenToMultiply fromValue(String value){
        for(WhenToMultiply whenToMultiply: WhenToMultiply.values()){
            if(Objects.equals(whenToMultiply.value,value)){
                return whenToMultiply;
            }
        }
        throw new IllegalArgumentException("Unknown whenToMultiply type: " + value);
    }
}