package game;

import game.config.CellProbability;
import game.config.GameConfig;
import game.config.GameResult;
import game.config.Symbol;
import game.config.WinCombination;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GameEngine {
  final Random random;
  final GameConfig config;

  String[][] matrix;

  public GameEngine(GameConfig config, SecureRandom random) {
    config.prepareCombinations();
    this.config = config;
    this.random = random;
  }

  public GameResult play(int bettingAmount) {

    Map<String, List<WinCombination>> winningCombinations = new HashMap<>();
    double reward = calculateReward(matrix, bettingAmount, winningCombinations);
    String appliedBonusSymbol = getAppliedBonusSymbol(matrix);

    Map<String, List<String>> collect = winningCombinations.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
        e -> e.getValue().stream().map(WinCombination::getCombinationName).toList()));
    return new GameResult(matrix, reward, collect, appliedBonusSymbol);
  }

  double calculateReward(String[][] matrix, int bettingAmount, Map<String, List<WinCombination>> appliedWinningCombinations) {
    if (config.getSameSymbolsCombinations() != null) {
      applySameSymbolCombinations(matrix, appliedWinningCombinations);
    }

    if (config.getLinearSymbolsCombinations() != null) {
      applyLinearSymbolCombinations(matrix, appliedWinningCombinations);
    }

    double reward = calculateCombinationsReward(bettingAmount, appliedWinningCombinations);

    if (reward > 0) {
      reward = applyBonusSymbol(matrix, reward);
    }

    return reward;
  }

  private String getAppliedBonusSymbol(String[][] matrix) {
    for (String[] row : matrix) {
      for (String symbol : row) {
        if (config.getSymbols().get(symbol).getType().equals("bonus")) {
          return symbol;
        }
      }
    }
    return null;
  }

  private void applySameSymbolCombinations(String[][] matrix, Map<String, List<WinCombination>> appliedWinningCombinations) {
    //apply same symbol combinations
    Map<String, Integer> standardSymbolsCount = new HashMap<>();
    for (String[] row : matrix) {
      for (String symbol : row) {
        if (isStandard(symbol)) {
          standardSymbolsCount.put(symbol, standardSymbolsCount.getOrDefault(symbol, 0) + 1);
        }
      }
    }

    standardSymbolsCount.forEach((symbol, amount) -> {
          WinCombination maxSameSymbolCombination = config.findMaxSameSymbolCombination(amount);
          if (maxSameSymbolCombination != null) {
            appliedWinningCombinations
                .computeIfAbsent(symbol, s -> new ArrayList<>())
                .add(maxSameSymbolCombination);
          }
        }
    );
  }

  private void applyLinearSymbolCombinations(String[][] matrix, Map<String, List<WinCombination>> appliedWinningCombinations) {
    for (WinCombination linearComb : config.getLinearSymbolsCombinations()) {
      for (List<String> area : linearComb.getCoveredAreas()) {
        String firstSymbol = null;
        boolean allSame = true;
        for (String cell : area) {
          String[] coords = cell.split(":");
          int row = Integer.parseInt(coords[0]);
          int col = Integer.parseInt(coords[1]);
          String symbol = matrix[row][col];
          if (firstSymbol == null) {
            firstSymbol = symbol;
          } else if (!symbol.equals(firstSymbol)) {
            allSame = false;
            break;
          }
        }
        storeCombination(appliedWinningCombinations, linearComb, allSame, firstSymbol);
      }
    }
  }

  private double calculateCombinationsReward(int bettingAmount, Map<String, List<WinCombination>> appliedWinningCombinations) {
    double reward = 0;
    for (Map.Entry<String, List<WinCombination>> entry : appliedWinningCombinations.entrySet()) {
      String symbol = entry.getKey();
      List<WinCombination> winningCombinations = entry.getValue();
      double symbolRewardMultiplier = config.getSymbols().get(symbol).getRewardMultiplier();

      double base = bettingAmount * symbolRewardMultiplier;
      Double reduce = winningCombinations.stream().map(WinCombination::getRewardMultiplier).reduce(1.0, (a, b) -> a * b);
      reward += base * reduce;
    }

    return reward;
  }

  private double applyBonusSymbol(String[][] matrix, double reward) {
    String bonusSymbol = getAppliedBonusSymbol(matrix);
    if (bonusSymbol != null) {
      Symbol bonus = config.getSymbols().get(bonusSymbol);
      if (bonus.getImpact().equals("multiply_reward")) {
        reward *= bonus.getRewardMultiplier();
      } else if (bonus.getImpact().equals("extra_bonus")) {
        reward += bonus.getExtra();
      }
    }
    return reward;
  }

  private boolean isStandard(String symbol) {
    return config.getSymbols().get(symbol).getType().equals("standard");
  }

  private void storeCombination(Map<String, List<WinCombination>> appliedWinningCombinations,
                                WinCombination linearComb,
                                boolean allSame,
                                String firstSymbol) {
    if (allSame && firstSymbol != null && isStandard(firstSymbol)) {
      List<WinCombination> existingCombinations = appliedWinningCombinations.computeIfAbsent(firstSymbol, s -> new ArrayList<>());
      Optional<WinCombination> any =
          existingCombinations.stream().filter(comb -> comb.getGroup().equals(linearComb.getGroup())).findAny();
      if (any.isEmpty()) {
        existingCombinations.add(linearComb);
      }
    }
  }

  public void generateMatrix() {
    matrix = new String[config.getRows()][config.getColumns()];

    // Fill matrix with standard symbols
    for (CellProbability cellProb : config.getProbabilities().getStandardSymbols()) {
      matrix[cellProb.getRow()][cellProb.getColumn()] = getRandomSymbol(cellProb.getSymbols());
    }

    // Add bonus symbol
    int bonusRow = random.nextInt(config.getRows());
    int bonusColumn = random.nextInt(config.getColumns());
    matrix[bonusRow][bonusColumn] = getRandomSymbol(config.getProbabilities().getBonusSymbols().getSymbols());
  }

  private String getRandomSymbol(Map<String, Integer> symbolProbabilities) {
    int totalWeight = symbolProbabilities.values().stream().mapToInt(Integer::intValue).sum();
    int randomWeight = random.nextInt(totalWeight);
    int currentWeight = 0;

    for (Map.Entry<String, Integer> entry : symbolProbabilities.entrySet()) {
      currentWeight += entry.getValue();
      if (randomWeight < currentWeight) {
        return entry.getKey();
      }
    }

    return null; // This should never happen
  }
}
