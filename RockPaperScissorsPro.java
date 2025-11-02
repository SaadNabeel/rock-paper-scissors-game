import java.io.*;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class RockPaperScissorsPro {

    private static final Scanner scanner = new Scanner(System.in);
    private static final Random random = new Random();

    private enum Move { ROCK, PAPER, SCISSORS }
    private enum Outcome { WIN, LOSE, DRAW }
    private enum Difficulty { EASY, MEDIUM, HARD, CUSTOM }

    private static class PlayerProfile implements Serializable {
        String name;
        int totalGames;
        int wins;
        int losses;
        int draws;
        long totalTimePlayedMillis;
        List<String> history;
    PlayerProfile(String name) {
    this.name = name;
    this.totalGames = 0;
    this.wins = 0;
    this.losses = 0;
    this.draws = 0;
    this.totalTimePlayedMillis = 0;
    this.history = new ArrayList<>();
}

double winRate() {
    if (totalGames == 0) return 0.0;
    return (double) wins / totalGames * 100.0;
}

void addHistory(String record) {
    history.add(record);
    if (history.size() > 100) {
        history.remove(0);
    }
}
private static void mainMenuLoop() {

    boolean running = true;

    while (running) {

        System.out.println();
        System.out.println("Main Menu:");
        System.out.println("1. Play Match");
        System.out.println("2. Configure");
        System.out.println("3. Profile & Statistics");
        System.out.println("4. History");
        System.out.println("5. Save Profile");
        System.out.println("6. Load Profile");
        System.out.println("7. Reset Profile");
        System.out.println("8. Help");
        System.out.println("9. Exit");
        System.out.print("Select option: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1": playMatchFlow(); break;
            case "2": configureFlow(); break;
            case "3": showProfile(); break;
            case "4": showHistory(); break;
            case "5": saveProfileInteractive(); break;
            case "6": loadProfileInteractive(); break;
            case "7": resetProfile(); break;
            case "8": showHelp(); break;
            case "9": running = false; break;
            default: System.out.println("Invalid option. Try again."); break;
        }
    }
}
private static void shutdown() {

    System.out.println("Saving profile...");
    saveProfile(profile);
    System.out.println("Goodbye, " + profile.name + ".");
    scanner.close();
}
private static void playMatchFlow() {

    int choiceRounds = askBestof();
    bestof = choiceRounds;

    System.out.println("Starting best of " + bestof + " match. Difficulty: " + difficulty);

    long matchStart = System.currentTimeMillis();
    int neededToWin = bestof / 2 + 1;
    int playerScore = 0;
    int computerScore = 0;

    List<String> roundRecords = new ArrayList<>();
    List<int[]> undoSnapshots = new ArrayList<>();

    while (playerScore < neededToWin && computerScore < neededToWin) {

        undoSnapshots.add(new int[]{playerScore, computerScore});
        Move playerMove = getPlayerMove();

        if (playerMove == null) {
            if (!undoSnapshots.isEmpty() && allowUndo) {
                int[] snap = undoSnapshots.remove(undoSnapshots.size() - 1);
                playerScore = snap[0];
                computerScore = snap[1];
                System.out.println("Last round undone.");
                continue;
            } else {
                System.out.println("No action taken. Continue playing.");
                continue;
            }
        }
    }
}
Move computerMove = getComputerMove(playerMove, difficulty, playerScore, computerScore);

Outcome outcome = decideOutcome(playerMove, computerMove);

String roundSummary = formatRoundSummary(playerMove, computerMove, outcome);
roundRecords.add(roundSummary);

switch (outcome) {
    case WIN:
        playerScore++;
        profile.wins++;
        break;
    case LOSE:
        computerScore++;
        profile.losses++;
        break;
    case DRAW:
        profile.draws++;
        if (allowTiesToCount) {
            // both players get a point if configured
            playerScore++;
            computerScore++;
        }
        break;
}
profile.totalGames++;

if (displayRoundDetails) {
    System.out.println(roundSummary);
    System.out.println("Score -> " + profile.name + ": " + playerScore + " | Computer: " + computerScore);
} else {
    System.out.println("Round complete.");
}

String matchResult = playerScore > computerScore ? "Match Won" : "Match Lost";

long matchEnd = System.currentTimeMillis();
long duration = matchEnd - matchStart;
profile.totalTimePlayedMillis += duration;

StringBuilder record = new StringBuilder();
record.append("Match: bestOf=").append(bestof)
      .append(", difficulty=").append(difficulty)
      .append(", result=").append(matchResult)
      .append(", score=").append(playerScore).append("-").append(computerScore)
      .append(", rounds=").append(roundRecords.size())
      .append(", durationMillis=").append(duration);

profile.addHistory(record.toString());

System.out.println();
System.out.println("Match concluded: " + matchResult);
System.out.println("Final Score: " + profile.name + " " + playerScore + " Computer " + computerScore);
System.out.println("Match duration: " + (duration / 1000.0) + " seconds");
