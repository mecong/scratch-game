package game.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GameConfig {
  int columns;
  int rows;
  Map<String, Symbol> symbols;
  Probabilities probabilities;
  @JsonProperty("win_combinations")
  Map<String, WinCombination> winCombinations;

  List<WinCombination> sameSymbolsCombinations = new ArrayList<>();
  List<WinCombination> linearSymbolsCombinations = new ArrayList<>();

  public void prepareCombinations() {
    winCombinations.forEach((combinationName, combination) -> {
      combination.setCombinationName(combinationName);
      if (Objects.equals(combination.getWhen(), "same_symbols")) {
        sameSymbolsCombinations.add(combination);
      } else if (Objects.equals(combination.getWhen(), "linear_symbols")) {
        linearSymbolsCombinations.add(combination);
      }
    });

    sameSymbolsCombinations.sort(Comparator.comparingInt(WinCombination::getCount));
  }

  public WinCombination findMaxSameSymbolCombination(int amount) {
    for (int i = sameSymbolsCombinations.size() - 1; i >= 0; i--) {
      WinCombination combination = sameSymbolsCombinations.get(i);
      if (combination.getCount() <= amount) {
        return combination;
      }
    }
    return null;
  }
}
