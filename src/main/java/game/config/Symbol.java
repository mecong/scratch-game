package game.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Symbol {
  @JsonProperty("reward_multiplier")
  double rewardMultiplier;
  String type;
  Integer extra;
  String impact;

}
