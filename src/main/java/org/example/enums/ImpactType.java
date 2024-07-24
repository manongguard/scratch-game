package org.example.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

public enum ImpactType {

    MULTIPLY_REWARD("multiply_reward"),
    EXTRA_BONUS("extra_bonus"),
    MISS("miss");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    ImpactType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static ImpactType fromValue(String value){
        for(ImpactType impactType: ImpactType.values()){
            if(Objects.equals(impactType.value,value)){
                return impactType;
            }
        }
        throw new IllegalArgumentException("Unknown impact type: " + value);
    }
}
