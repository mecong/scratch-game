package game.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WinCombination {
  @JsonProperty("reward_multiplier")
  double rewardMultiplier;
  String when;
  Integer count;
  String group;
  @JsonProperty("covered_areas")
  List<List<String>> coveredAreas;

  String combinationName;
}
