package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enums.GroupToMultiply;
import org.example.enums.WhenToMultiply;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WinningCombinations {
    private String combinationName;
    private Double rewardMultiplier;
    private WhenToMultiply when;
    private GroupToMultiply group;
    private String[][] coveredAreas;
    private Integer validCount;
}
