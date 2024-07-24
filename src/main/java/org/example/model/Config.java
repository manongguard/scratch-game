package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Config {
    private Integer columns;
    private Integer rows;
    private List<Symbol> symbols;
    private Probabilities probabilities;
    private List<WinningCombinations> winningCombinations;
}
