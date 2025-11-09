import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
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
private static void configureFlow() {

    boolean back = false;

    while (!back) {

        System.out.println();
        System.out.println("Configuration Menu:");
        System.out.println("1. Difficulty (" + difficulty + ")");
        System.out.println("2. Best Of (" + bestof + ")");
        System.out.println("3. Allow ties to count as rounds (" + allowTiesToCount + ")");
        System.out.println("4. Display round details (" + displayRoundDetails + ")");
        System.out.println("5. Allow Undo (" + allowUndo + ")");
        System.out.println("6. Save file name (" + saveFileName + ")");
        System.out.println("0. Back");

        System.out.print("Select option: ");
        String opt = scanner.nextLine().trim();

        switch (opt) {

            case "1":
                selectDifficulty();
                break;

            case "2":
                System.out.print("Enter default best-of number (odd): ");
                try {
                    int v = Integer.parseInt(scanner.nextLine().trim());
                    if (v <= 0) v = 3;
                    if (v % 2 == 0) v++;
                    bestof = v;
                    System.out.println("Best of set to " + bestof);
                } catch (Exception e) {
                    System.out.println("Invalid input.");
                }
                break;
            case "3":
    allowTiesToCount = !allowTiesToCount;
    System.out.println("Allow ties to count set to " + allowTiesToCount);
    break;

case "4":
    displayRoundDetails = !displayRoundDetails;
    System.out.println("Display round details set to " + displayRoundDetails);
    break;

case "5":
    allowUndo = !allowUndo;
    System.out.println("Allow undo set to " + allowUndo);
    break;

case "6":
    System.out.print("Enter save file name: ");
    String fn = scanner.nextLine().trim();
    if (!fn.isEmpty()) saveFileName = fn;
    System.out.println("Save file name set to " + saveFileName);
    break;

case "0":
    back = true;
    break;

default:
    System.out.println("Invalid option.");
    break;
            }}private static void selectDifficulty() {

    System.out.println("Select Difficulty:");
    System.out.println("1. Easy");
    System.out.println("2. Medium");
    System.out.println("3. Hard");
    System.out.println("4. Custom");
    System.out.print("Choice: ");

    String c = scanner.nextLine().trim();

    switch (c) {
        case "1":
            difficulty = Difficulty.EASY;
            break;
        case "2":
            difficulty = Difficulty.MEDIUM;
            break;
        case "3":
            difficulty = Difficulty.HARD;
            break;
        case "4":
            difficulty = Difficulty.CUSTOM;
            break;
        default:
            System.out.println("Invalid choice; keeping previous difficulty.");
            break;
    }

    System.out.println("Difficulty set to " + difficulty);
}
private static void showProfile() {

    System.out.println();
    System.out.println("Profile Summary:");
    System.out.println("Name: " + profile.name);
    System.out.println("Total Games: " + profile.totalGames);
    System.out.println("Wins: " + profile.wins);
    System.out.println("Losses: " + profile.losses);
    System.out.println("Draws: " + profile.draws);
    System.out.printf("Win Rate: %.2f%%\n", profile.winRate());
    System.out.println("Total Time Played: " + (profile.totalTimePlayedMillis / 1000.0) + " seconds");

    if (!profile.history.isEmpty()) {
        System.out.println("Recent match summary: " + profile.history.get(profile.history.size() - 1));
    }
}
private static void showHistory() {

    System.out.println();
    System.out.println("History (most recent first):");

    List<String> hist = new ArrayList<>(profile.history);
    Collections.reverse(hist);

    if (hist.isEmpty()) {
        System.out.println("No history available.");
        return;
    }

    int idx = 1;
    for (String h : hist) {
        System.out.println(idx + ". " + h);
        idx++;
    }
}

private static void saveProfileInteractive() {

    System.out.print("Enter filename to save profile or press Enter to use default (" + saveFileName + "): ");
    String fn = scanner.nextLine().trim();

    if (!fn.isEmpty()) saveFileName = fn;

    boolean ok = saveProfile(profile);
    if (ok)
        System.out.println("Profile saved to " + saveFileName);
    else
        System.out.println("Failed to save profile.");
}
private static void saveProfileInteractive() {

    System.out.print("Enter filename to save profile or press Enter to use default (" + saveFileName + "): ");
    String fn = scanner.nextLine().trim();

    if (!fn.isEmpty()) saveFileName = fn;

    boolean ok = saveProfile(profile);
    if (ok)
        System.out.println("Profile saved to " + saveFileName);
    else
        System.out.println("Failed to save profile.");
}

private static boolean saveProfile(PlayerProfile p) {
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFileName))) {
        oos.writeObject(p);
        return true;
    } catch (Exception e) {
        return false;
    }
}

private static void loadProfileInteractive() {

    System.out.print("Enter filename to load profile or press Enter to use default (" + saveFileName + "): ");
    String fn = scanner.nextLine().trim();

    if (!fn.isEmpty()) saveFileName = fn;

    PlayerProfile p = loadProfileFromFile(saveFileName);

    if (p != null) {
        profile = p;
        System.out.println("Profile loaded successfully for " + profile.name);
    } else {
        System.out.println("Failed to load profile.");
    }
}

private static PlayerProfile loadProfileFromFile(String fn) {

    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fn))) {

        Object obj = ois.readObject();

        if (obj instanceof PlayerProfile) {
            return (PlayerProfile) obj;
        }

        return null;

    } catch (Exception e) {
        return null;
    }
}

private static void resetProfile() {

    System.out.print("Are you sure you want to reset your profile? (yes/no): ");
    String ans = scanner.nextLine().trim().toLowerCase();

    if (ans.equals("yes") || ans.equals("y")) {
        profile = new PlayerProfile(profile.name);
        System.out.println("Profile reset.");
    } else {
        System.out.println("Reset cancelled.");
    }
}
}

private static void showHelp() {

    System.out.println();
    System.out.println("Help & Instructions:");
    System.out.println("Rock-Paper-Scissors Pro supports:");
    System.out.println("- Multiple match lengths (best of)");
    System.out.println("- Difficulty levels: Easy, Medium, Hard, Custom");
    System.out.println("- Profile saving and loading");
    System.out.println("- Undo of last round during a match (if enabled)");
    System.out.println("- Display of recent history and statistics");
    System.out.println("Gameplay tip: Rock beats Scissors, Paper beats Rock, Scissors beats Paper.");
}

static void showRules() {

    System.out.println("\nGame Rules:");
    System.out.println("1. Rock beats Scissors");
    System.out.println("2. Paper beats Rock");
    System.out.println("3. Scissors beats Paper");
    System.out.println("4. You can choose 1 for Rock, 2 for Paper, or 3 for Scissors.");
    System.out.println("5. Type 'exit' anytime to quit the game.\n");
}
