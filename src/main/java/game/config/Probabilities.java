package game.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Probabilities {
  @JsonProperty("standard_symbols")
  List<CellProbability> standardSymbols;
  @JsonProperty("bonus_symbols")
  BonusSymbolProbability bonusSymbols;
}
