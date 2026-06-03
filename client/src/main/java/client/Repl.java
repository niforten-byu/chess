package client;

import ui.EscapeSequences;
import java.util.Scanner;

public class Repl {
    private final ChessClient client;

    public Repl(int port) {
        client = new ChessClient(port);
    }

    public void run() {
        System.out.println("Welcome to Duncan's Extravagant Chess Game! Type Help to get started.");

        Scanner scanner = new Scanner(System.in);
        var result = "";

        // infinite REPL loop for user's commands
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try { // run user's command
                result = client.inputHelper(line);
                System.out.print(result);
            }
            catch (Throwable e) { // user typed in a non-existent command
                System.out.print(e.getMessage());
            }
        }
        System.out.println();
    }

    private void printPrompt() {
        // prints out state in green to distinguish from other commands ex.[LOGGED_OUT] >>>
        System.out.print("\n" + EscapeSequences.SET_TEXT_COLOR_GREEN + "[" + client.getState() + "] >>> " + EscapeSequences.RESET_TEXT_COLOR);
    }
}
