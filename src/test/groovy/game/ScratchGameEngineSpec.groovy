package game


import com.fasterxml.jackson.databind.ObjectMapper
import game.config.CustomPrettyPrinter
import game.config.GameConfig
import game.config.Symbol
import game.config.WinCombination
import spock.lang.Specification

import java.security.SecureRandom

class ScratchGameEngineSpec extends Specification {

  static ObjectMapper objectMapper = new ObjectMapper()

  def setupSpec() {
    objectMapper.setDefaultPrettyPrinter(new CustomPrettyPrinter())
  }

  String readJsonFile(String filePath) {
    InputStream inputStream = getClass().getResourceAsStream("/$filePath")

    if (inputStream == null) {
      throw new RuntimeException("File not found: $filePath")
    }

    // Read the content of the file into a string
    StringBuilder resultStringBuilder = new StringBuilder()
    try (Scanner scanner = new Scanner(inputStream)) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine()
        resultStringBuilder.append(line).append("\n")
      }
    }

    return resultStringBuilder.toString()
  }

  def "Should work correctly with full configuration"() {
    def config = objectMapper.readValue(readJsonFile("config-example.json"), GameConfig)
    def game = new GameEngine(config, new SecureRandom())

    when:
    game.generateMatrix()
    def result = game.play(100)

    then:
    print objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result)
    result.matrix().length == 3
    result.matrix()[0].length == 3
  }

  def "Should work correctly with full configuration 4x4"() {
    def config = objectMapper.readValue(readJsonFile("config-example4x4.json"), GameConfig)
    def game = new GameEngine(config, new SecureRandom())

    when:
    game.generateMatrix()
    def result = game.play(100)

    then:
    print objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result)
    result.matrix().length == 4
    result.matrix()[0].length == 4
  }

  def "Game should apply only the highest paying combination per group"() {
    given:
    def config = new GameConfig()
    config.setRows(3)
    config.setColumns(3)
    config.setSymbols([
        "A"  : new Symbol(rewardMultiplier: 50, type: "standard"),
        "B"  : new Symbol(rewardMultiplier: 25, type: "standard"),
        "C"  : new Symbol(rewardMultiplier: 10, type: "standard"),
        "10x": new Symbol(rewardMultiplier: 10, type: "bonus", impact: "multiply_reward")
    ])
    config.setWinCombinations([
        "same_symbol_3_times"      : new WinCombination(rewardMultiplier: 1, when: "same_symbols", count: 3, group: "same_symbols"),
        "same_symbol_4_times"      : new WinCombination(rewardMultiplier: 1.5, when: "same_symbols", count: 4, group: "same_symbols"),
        "same_symbols_horizontally": new WinCombination(rewardMultiplier: 2, when: "linear_symbols", group: "horizontally_linear_symbols",
            coveredAreas: [["0:0", "0:1", "0:2"]])
    ])
    def game = new GameEngine(config, new SecureRandom())

    game.matrix = [["A", "A", "A"],
                   ["A", "10x", "C"],
                   ["B", "B", "B"]]
    when:
    def result = game.play(100)

    then:
    result.reward == 175000 // ((100 * 50 * 1.5 * 2) + (100 * 25 * 1 ))*10
    result.appliedWinningCombinations == ["A": ["same_symbol_4_times", "same_symbols_horizontally"], "B": ["same_symbol_3_times"]]
    result.appliedBonusSymbol == "10x"
  }


  def "Test from assignment should work"() {
    given:
    def config = new GameConfig()
    config.setRows(3)
    config.setColumns(3)
    config.setSymbols([
        "A"    : new Symbol(rewardMultiplier: 5, type: "standard"),
        "B"    : new Symbol(rewardMultiplier: 3, type: "standard"),
        "+1000": new Symbol(extra: 1000, type: "bonus", impact: "extra_bonus")
    ])
    config.setWinCombinations([
        "same_symbol_3_times"    : new WinCombination(rewardMultiplier: 1, when: "same_symbols", count: 3, group: "same_symbols"),
        "same_symbol_5_times"    : new WinCombination(rewardMultiplier: 5, when: "same_symbols", count: 4, group: "same_symbols"),
        "same_symbols_vertically": new WinCombination(rewardMultiplier: 2, when: "linear_symbols", group: "vertically_linear_symbols",
            coveredAreas: [
                ["0:0", "1:0", "2:0"],
                ["0:1", "1:1", "2:1"],
                ["0:2", "1:2", "2:2"]
            ]),

    ])
    def game = new GameEngine(config, new SecureRandom())

    game.matrix = [["A", "A", "B"],
                   ["A", "+1000", "B"],
                   ["A", "A", "B"]]
    when:
    def result = game.play(100)

    then:
    print objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result)

    result.reward == 6600
    result.appliedWinningCombinations == ["A": ["same_symbol_5_times", "same_symbols_vertically"], "B": ["same_symbol_3_times", "same_symbols_vertically"]]
    result.appliedBonusSymbol == "+1000"
  }

  def "Should apply only one combination of a group"() {
    given:
    def config = new GameConfig()
    config.setRows(3)
    config.setColumns(3)
    config.setSymbols([
        "A"   : new Symbol(rewardMultiplier: 50, type: "standard"),
        "+500": new Symbol(extra: 500, type: "bonus", impact: "extra_bonus")
    ])
    config.setWinCombinations([
        "same_symbol_3_times"                  : new WinCombination(rewardMultiplier: 1, when: "same_symbols", count: 3, group: "same_symbols"),
        "same_symbol_4_times"                  : new WinCombination(rewardMultiplier: 1.5, when: "same_symbols", count: 4, group: "same_symbols"),
        "same_symbol_5_times"                  : new WinCombination(rewardMultiplier: 2, when: "same_symbols", count: 4, group: "same_symbols"),
        "same_symbol_6_times"                  : new WinCombination(rewardMultiplier: 3, when: "same_symbols", count: 4, group: "same_symbols"),
        "same_symbol_7_times"                  : new WinCombination(rewardMultiplier: 5, when: "same_symbols", count: 4, group: "same_symbols"),
        "same_symbol_8_times"                  : new WinCombination(rewardMultiplier: 10, when: "same_symbols", count: 4, group: "same_symbols"),
        "same_symbol_9_times"                  : new WinCombination(rewardMultiplier: 20, when: "same_symbols", count: 4, group: "same_symbols"),

        "same_symbols_horizontally"            : new WinCombination(rewardMultiplier: 2, when: "linear_symbols", group: "horizontally_linear_symbols",
            coveredAreas: [
                ["0:0", "0:1", "0:2"],
                ["1:0", "1:1", "1:2"],
                ["2:0", "2:1", "2:2"]
            ]),
        "same_symbols_vertically"              : new WinCombination(rewardMultiplier: 2, when: "linear_symbols", group: "vertically_linear_symbols",
            coveredAreas: [
                ["0:0", "1:0", "2:0"],
                ["0:1", "1:1", "2:1"],
                ["0:2", "1:2", "2:2"]
            ]),

        "same_symbols_diagonally_left_to_right": new WinCombination(rewardMultiplier: 5, when: "linear_symbols", group: "ltr_diagonally_linear_symbols",
            coveredAreas: [
                ["0:0", "1:1", "2:2"]
            ]),
        "same_symbols_diagonally_right_to_left": new WinCombination(rewardMultiplier: 5, when: "linear_symbols", group: "rtl_diagonally_linear_symbols",
            coveredAreas: [
                ["0:2", "1:1", "2:0"]
            ])
    ])

    and:
    def game = new GameEngine(config, new SecureRandom())

    game.matrix = [["A", "A", "A"],
                   ["A", "A", "A"],
                   ["A", "+500", "A"]]
    when:
    def result = game.play(1)

    then:
    // (1 * 50 * 20 * 2 * 2 * 5 * 5) + 500
    result.reward == 100500
    result.appliedWinningCombinations == ["A": ["same_symbol_9_times",
                                                "same_symbols_horizontally",
                                                "same_symbols_vertically",
                                                "same_symbols_diagonally_left_to_right",
                                                "same_symbols_diagonally_right_to_left"]]
    result.appliedBonusSymbol == "+500"
  }
}
