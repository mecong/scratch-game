package game;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import game.config.CustomPrettyPrinter;
import game.config.GameConfig;
import game.config.GameResult;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;

public class ScratchGame {
  public static void main(String[] args) {
    if (argumentsAmountIsIncorrect(args)) {
      printUsage();
      return;
    }

    CommandLineArguments cmdArgs = parseCommandLineArguments(args);
    if (cmdArgs == null) {
      System.out.println("Invalid arguments");
      return;
    }

    try {
      GameConfig config = loadGameConfig(cmdArgs.configFile);
      GameResult result = playGame(config, cmdArgs.bettingAmount);
      printGameResult(result);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static boolean argumentsAmountIsIncorrect(String[] args) {
    return args.length != 4;
  }

  private static void printUsage() {
    System.out.println("Usage: java -jar scratch-game.jar --config <config-file> --betting-amount <amount>");
  }

  private static CommandLineArguments parseCommandLineArguments(String[] args) {
    String configFile = null;
    int bettingAmount = 0;

    for (int i = 0; i < args.length; i += 2) {
      if (args[i].equals("--config")) {
        configFile = args[i + 1];
      } else if (args[i].equals("--betting-amount")) {
        bettingAmount = Integer.parseInt(args[i + 1]);
      }
    }

    return inputIsIncorrect(configFile, bettingAmount) ? null : new CommandLineArguments(configFile, bettingAmount);
  }

  private static GameConfig loadGameConfig(String configFile) throws IOException {
    ObjectMapper objectMapper = createObjectMapper();
    return objectMapper.readValue(new File(configFile), GameConfig.class);
  }

  private static GameResult playGame(GameConfig config, int bettingAmount) {
    GameEngine gameEngine = new GameEngine(config, new SecureRandom());
    gameEngine.generateMatrix();
    return gameEngine.play(bettingAmount);
  }

  private static void printGameResult(GameResult result) throws IOException {
    ObjectMapper objectMapper = createObjectMapper();
    System.out.println(objectMapper.writeValueAsString(result));
  }

  private static boolean inputIsIncorrect(String configFile, int bettingAmount) {
    return configFile == null || bettingAmount == 0;
  }

  private static ObjectMapper createObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.setDefaultPrettyPrinter(new CustomPrettyPrinter());
    return objectMapper;
  }

  private record CommandLineArguments(String configFile, int bettingAmount) {
  }
}
