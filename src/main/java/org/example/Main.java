package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.example.enums.GroupToMultiply;
import org.example.enums.ImpactType;
import org.example.enums.SymbolType;
import org.example.enums.WhenToMultiply;
import org.example.model.BonusCheck;
import org.example.model.CalculationModel;
import org.example.model.Config;
import org.example.model.Output;
import org.example.model.Probabilities;
import org.example.model.Probability;
import org.example.model.Symbol;
import org.example.model.SymbolProbability;
import org.example.model.WinningCombinations;
import org.example.model.WinningModel;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Main {

    private final static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws ParseException {
        Options options = new Options();

        options.addOption(Option.builder("c")
                .longOpt("config")
                .argName("config")
                .hasArg().required().desc("config for the application")
                .build());

        options.addOption(Option.builder("b")
                .longOpt("betting-amount")
                .argName("betting-amount")
                .hasArg().required().desc("the betting amount")
                .build());

        //Create a parser
        CommandLineParser parser = new DefaultParser();

        //parse the options passed as command line arguments
        CommandLine cmd = parser.parse(options, args);

        try {

            JsonNode jsonConfig =
                    mapper.readValue(new File(cmd.getOptionValue("config")),
                            JsonNode.class);

            // need to get all configs first
            Config config = getConfig(jsonConfig);
            Output output = new Output();
            output.setMatrix(propagateMatrix(config.getRows(),
                    config.getColumns(), config.getProbabilities(), output));

            List<WinningModel> winningModels =
                    produceWinningModels(output.getMatrix(), config.getWinningCombinations());

            calculateReward(config, output, winningModels,
                    BigDecimal.valueOf(Double.parseDouble(cmd.getOptionValue(
                            "betting-amount"))));

            if (output.getReward().equals(BigDecimal.ZERO)) {
                output.setAppliedBonusSymbol(null);
                output.setAppliedWinningCombinations(null);
            }

            String outputString = mapper.writeValueAsString(output);

            System.out.println(outputString);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void calculateReward(Config config, Output output,
                                       List<WinningModel> models,
                                       BigDecimal bet) {

        final Map<String, List<String>> appliedWinningCombinations =
                new HashMap<>();

        output.setReward(BigDecimal.ZERO);
        CalculationModel calculationModel = new CalculationModel();

        models.stream().spliterator().forEachRemaining(winningModel -> {
            List<String> combinations = winningModel.getWinningCombinations();
            calculationModel.setReward(BigDecimal.ZERO);
            config.getSymbols().parallelStream()
                    .filter(symbol -> winningModel.getSymbol().equals(symbol.getSymbol()))
                    .forEach(symbol -> calculationModel.setReward(bet
                            .multiply(BigDecimal.valueOf(symbol.getRewardMultiplier()))));

            if (combinations != null) {
                appliedWinningCombinations.put(winningModel.getSymbol(), combinations);

                combinations.parallelStream().forEach(wc -> {
                    config.getWinningCombinations().forEach(cwc -> {
                        if (wc.equals(cwc.getCombinationName())) {
                            BigDecimal total = calculationModel.getReward()
                                    .multiply(BigDecimal.valueOf(cwc.getRewardMultiplier()));
                            calculationModel.setReward(total);
                        }
                    });
                });
            }

            output.setReward(output.getReward().add(calculationModel.getReward()));
        });

        config.getSymbols().parallelStream()
                .filter(symbol -> symbol.getType().equals(SymbolType.BONUS))
                .filter(symbol -> symbol.getSymbol().equals(output.getAppliedBonusSymbol()))
                .findFirst().ifPresent(symbol -> {
                    if (symbol.getImpact().equals(ImpactType.EXTRA_BONUS)) {
                        output.setReward(output.getReward().add(BigDecimal.valueOf(symbol.getExtra())));
                    } else if (symbol.getImpact().equals(ImpactType.MULTIPLY_REWARD)) {
                        output.setReward(output.getReward().multiply(BigDecimal.valueOf(symbol.getRewardMultiplier())));
                    }
                });
        output.setAppliedWinningCombinations(appliedWinningCombinations);
    }

    public static List<WinningModel> produceWinningModels(String[][] matrix,
                                                          List<WinningCombinations> winningCombinations) {
        List<WinningModel> winningModels = new ArrayList<>();

        for (int row = 0; row < matrix.length; row++) {
            for (int col = 0; col < matrix[row].length; col++) {
                int finalRow = row;
                int finalCol = col;
                final String coordinate = String.format("%s:%s", finalRow, finalCol);
                if (winningModels.stream().anyMatch(winningModel ->
                        winningModel.getSymbol().equals(matrix[finalRow][finalCol]))) {
                    winningModels.stream().filter(winningModel ->
                                    winningModel.getSymbol().equals(matrix[finalRow][finalCol]))
                            .forEach(winningModel -> {
                                winningModel.setCount(winningModel.getCount() + 1);
                                winningModel.getCoordinates()
                                        .add(coordinate);
                            });
                } else {
                    List<String> coords = new ArrayList<>();
                    coords.add(coordinate);
                    winningModels.add(new WinningModel(matrix[row][col], 1,
                            coords));
                }
            }
        }

        winningModels = winningModels.stream()
                .filter(winningModel -> winningModel.getCount() >= 3)
                .peek(winningModel -> {
                    winningCombinations.forEach(winningCombi -> {
                        if (null != winningCombi.getValidCount() &&
                                winningCombi.getValidCount() == winningModel.getCount()) {
                            if (null == winningModel.getWinningCombinations()) {
                                List<String> wc =
                                        new ArrayList<>();
                                winningModel.setWinningCombinations(wc);

                            }
                            winningModel.getWinningCombinations()
                                    .add(winningCombi.getCombinationName());
                        }

                        if (null != winningCombi.getCoveredAreas()) {
                            if (null == winningModel.getWinningCombinations()) {
                                List<String> wc =
                                        new ArrayList<>();
                                winningModel.setWinningCombinations(wc);
                            }

                            Arrays.stream(winningCombi.getCoveredAreas()).forEach(strings -> {
                                if (new HashSet<>(winningModel.getCoordinates()).containsAll(Arrays.stream(strings).toList())) {
                                    winningModel.getWinningCombinations().add(winningCombi.getCombinationName());
                                }
                            });
                        }
                    });
                })
                .collect(Collectors.toList());

        return winningModels;
    }

    public static String[][] propagateMatrix(int rows, int columns,
                                             Probabilities probabilities,
                                             Output output) {
        String[][] matrix = new String[rows][columns];
        final BonusCheck bonusCheck =
                new BonusCheck(false, 0.8);

        probabilities.getStandardProbability().forEach(probability -> {
            double randomValue = new Random().nextDouble();

            if (randomValue >= bonusCheck.getBonusProbability()
                    && !bonusCheck.isHasBonusSymbolAlready()) {

                final String randomSymbol = getRandomSymbol(
                        probabilities.getBonusProbability()
                                .getSymbolProbabilities());

                matrix[probability.getRow()][probability.getColumn()] = randomSymbol;
                bonusCheck.setHasBonusSymbolAlready(true);
                output.setAppliedBonusSymbol(randomSymbol);
            } else {
                matrix[probability.getRow()][probability.getColumn()] =
                        getRandomSymbol(probability.getSymbolProbabilities());
            }
        });
        return matrix;
    }

    public static String getRandomSymbol(List<SymbolProbability> symbolProbabilities) {
        double cumulativeProbability = 0.0;
        double randomValue = new Random().nextDouble();

        Double totalProbability =
                symbolProbabilities
                        .stream()
                        .mapToDouble(SymbolProbability::getProbability).sum();

        for (SymbolProbability symbol : symbolProbabilities) {
            cumulativeProbability += symbol.getProbability() / totalProbability;
            if (randomValue <= cumulativeProbability) {
                return symbol.getSymbol();
            }
        }

        // if somehow the for loop fails to return a symbol
        // return the highest probability symbol
        return symbolProbabilities.stream()
                .max(Comparator.comparingDouble(SymbolProbability::getProbability))
                .map(SymbolProbability::getSymbol)
                .orElse(null);
    }

    public static Config getConfig(JsonNode main) {
        Config config = new Config();

        // null check for optional columns
        if (null == main.get("columns") || main.get("columns").isNull()) {
            // set 3 as default number, can use final variable
            // as to not do a magic number
            // but let us just make the comment the document for it
            config.setColumns(3);
        } else {
            config.setColumns(main.get("columns").asInt());
        }

        // null check for optional columns
        if (null == main.get("rows") || main.get("rows").isNull()) {
            // set 3 as default number, can use final variable
            // as to not do a magic number
            // but let us just make the comment the document for it
            config.setRows(3);
        } else {
            config.setRows(main.get("rows").asInt());
        }

        config.setSymbols(getSymbols(main.get("symbols")));
        config.setProbabilities(getProbabilities(main.get("probabilities")));
        config.setWinningCombinations(getWinningCombinations(main.get("win_combinations")));

        return config;
    }

    public static List<Symbol> getSymbols(JsonNode symbolNode) {
        List<Symbol> symbols = new ArrayList<>();
        Iterator<String> fieldNames = symbolNode.fieldNames();

        fieldNames.forEachRemaining(fieldName -> {
            // add some null checks through ternary
            final Double rewardMultiplier =
                    null != symbolNode.get(fieldName).get("reward_multiplier")
                            ? symbolNode.get(fieldName).get(
                            "reward_multiplier").asDouble()
                            : null;
            final SymbolType type = SymbolType.fromValue(symbolNode.get(fieldName).get("type").asText());
            final ImpactType impact = null != symbolNode.get(fieldName).get("impact")
                    ? ImpactType.fromValue(symbolNode.get(fieldName).get("impact").asText())
                    : null;
            final Integer extra = null != symbolNode.get(fieldName).get("extra")
                    ? symbolNode.get(fieldName).get("extra").asInt()
                    : null;

            symbols.add(new Symbol(
                    fieldName,
                    rewardMultiplier,
                    type,
                    impact,
                    extra
            ));
        });
        return symbols;
    }

    public static Probabilities getProbabilities(JsonNode probabilityNode) {
        Probabilities probability = new Probabilities();

        List<Probability> standard = getStandardProbability(probabilityNode.get("standard_symbols"));
        Probability bonus = getBonusProbability(probabilityNode.get("bonus_symbols"));

        probability.setStandardProbability(standard);
        probability.setBonusProbability(bonus);

        return probability;
    }

    public static List<Probability> getStandardProbability(JsonNode standardSymbolNode) {
        List<Probability> standard = new ArrayList<>();
        List<JsonNode> objects = StreamSupport
                .stream(standardSymbolNode.spliterator()
                        , true)
                .toList();

        objects.forEach(node -> {
            Iterator<String> symbolFieldName = node.get("symbols").fieldNames();
            List<SymbolProbability> symbolProbabilities = new ArrayList<>();

            symbolFieldName.forEachRemaining(symbol ->
                    symbolProbabilities.add(
                            new SymbolProbability(
                                    symbol,
                                    node.get("symbols").get(symbol).asDouble()
                            )));

            standard.add(new Probability(
                    node.get("column").asInt(),
                    node.get("row").asInt(),
                    symbolProbabilities
            ));
        });
        return standard;
    }

    public static Probability getBonusProbability(JsonNode bonusSymbolNode) {
        Probability bonus = new Probability();
        JsonNode jsonNode = bonusSymbolNode.get("symbols");
        List<SymbolProbability> symbolProbabilities = new ArrayList<>();

        Iterator<String> symbolFieldName = jsonNode.fieldNames();
        symbolFieldName.forEachRemaining(symbol ->
                symbolProbabilities.add(new SymbolProbability(
                        symbol,
                        jsonNode.get(symbol).asDouble()
                )));

        bonus.setColumn(null);
        bonus.setRow(null);
        bonus.setSymbolProbabilities(symbolProbabilities);

        return bonus;
    }

    public static List<WinningCombinations> getWinningCombinations(JsonNode winningCombinationsNode) {
        List<WinningCombinations> winningCombinations = new ArrayList<>();
        Iterator<String> winningCombination =
                winningCombinationsNode.fieldNames();

        winningCombination.forEachRemaining(win -> {
            String[][] coveredAreas = null;
            // could be ternary
            if (null != winningCombinationsNode.get(win).get("covered_areas")) {
                coveredAreas =
                        mapper.convertValue(winningCombinationsNode.get(win)
                                .get("covered_areas"), String[][].class);
            }
            Integer validCount =
                    null != winningCombinationsNode.get(win).get("count") ?
                            winningCombinationsNode.get(win).get("count").asInt() :
                            null;

            winningCombinations.add(new WinningCombinations(
                    win,
                    winningCombinationsNode.get(win).get(
                            "reward_multiplier").asDouble(),
                    WhenToMultiply.fromValue(winningCombinationsNode.get(win).get(
                            "when").asText()),
                    GroupToMultiply.fromValue(winningCombinationsNode.get(win).get(
                            "group").asText()),
                    coveredAreas,
                    validCount
            ));
        });

        return winningCombinations;
    }

}