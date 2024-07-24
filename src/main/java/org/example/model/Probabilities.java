package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Probabilities {

    private List<Probability> standardProbability;
    private Probability bonusProbability;
    private Double sumOfStandardProbability;
    private Double sumOfBonusProbability;

}
