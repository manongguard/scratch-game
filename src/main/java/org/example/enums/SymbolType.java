package org.example.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

public enum SymbolType {

    STANDARD("standard"),
    BONUS("bonus");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    SymbolType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static SymbolType fromValue(String value){
        for(SymbolType symbolType: SymbolType.values()){
            if(Objects.equals(symbolType.value,value)){
                return symbolType;
            }
        }
        throw new IllegalArgumentException("Unknown symbol type: " + value);
    }
}