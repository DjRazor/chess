package ui;

import java.util.Scanner;

public class Repl {
    private final ChessClient client;
    public Repl() {
        client = new ChessClient();
    }
    public void run() {
        System.out.print("Welcome to the Chess Server. Please sign in or register for an account.\n");
        System.out.print(client.help());

        Scanner scanner = new Scanner(System.in);
        var input = "";
        while (!input.equals("quit")) {
            try {
                input = "quit";
            } catch (Throwable ex) {
                System.out.print(ex.toString());
            }
        }
    }
}
