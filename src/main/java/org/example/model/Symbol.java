package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enums.ImpactType;
import org.example.enums.SymbolType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Symbol {
    private String symbol;
    private Double rewardMultiplier;
    private SymbolType type;
    private ImpactType impact;
    private Integer extra;
}
