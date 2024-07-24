package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BonusCheck {
    private boolean hasBonusSymbolAlready;
    double bonusProbability;
}
