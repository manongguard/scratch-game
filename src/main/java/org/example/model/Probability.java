package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Probability {
    private Integer column;
    private Integer row;
    private List<SymbolProbability> symbolProbabilities;
}
