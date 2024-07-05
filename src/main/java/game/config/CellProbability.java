package game.config;

import java.util.Map;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CellProbability {
  int column;
  int row;
  Map<String, Integer> symbols;
}
