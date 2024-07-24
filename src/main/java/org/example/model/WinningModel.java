package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WinningModel {

    public WinningModel(String symbol, int count, List<String> coordinates){
        this.symbol = symbol;
        this.count = count;
        this.coordinates = coordinates;
        this.winningCombinations = null;
    }

    private String symbol;
    private int count;
    private List<String> coordinates;
    private List<String> winningCombinations;

}
