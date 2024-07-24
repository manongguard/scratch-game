package org.example.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

public enum GroupToMultiply {

    SAME_SYMBOLS("same_symbols"),
    VERTICALLY_LINEAR_SYMBOLS("vertically_linear_symbols"),
    HORIZONTALLY_LINEAR_SYMBOLS("horizontally_linear_symbols"),
    LEFT_TO_RIGHT_DIAGONALLY_LINEAR_SYMBOLS("ltr_diagonally_linear_symbols"),
    RIGHT_TO_LEFT_DIAGONALLY_LINEAR_SYMBOLS("rtl_diagonally_linear_symbols");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    GroupToMultiply(String value) {
        this.value = value;
    }

    @JsonCreator
    public static GroupToMultiply fromValue(String value){
        for(GroupToMultiply groupToMultiply: GroupToMultiply.values()){
            if(Objects.equals(groupToMultiply.value,value)){
                return groupToMultiply;
            }
        }
        throw new IllegalArgumentException("Unknown whenToMultiply type: " + value);
    }
}
