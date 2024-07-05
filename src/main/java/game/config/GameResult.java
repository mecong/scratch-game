package game.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GameResult(
    String[][] matrix,
    double reward,
    Map<String, List<String>> appliedWinningCombinations,
    String appliedBonusSymbol
) {

  public GameResult(String[][] matrix,
                    double reward,
                    Map<String, List<String>> appliedWinningCombinations,
                    String appliedBonusSymbol) {
    this.matrix = matrix;
    this.reward = reward;
    this.appliedWinningCombinations = appliedWinningCombinations.isEmpty() ? null : appliedWinningCombinations;
    this.appliedBonusSymbol = appliedBonusSymbol;
  }
}
