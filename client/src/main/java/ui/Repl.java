package ui;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Repl {

    private final ChessClient client;
    public Repl(String serverURL) {
        client = new ChessClient(serverURL);
    }
    public void run() {
        System.out.println("\uD83D\uDC36 Welcome to the Chess Server. Please sign in or register for an account.\n");
        System.out.print(client.help());

        Scanner scanner = new Scanner(System.in);
        var input = "";
        while (!input.equals("quit")) {
            System.out.println();
            String line = scanner.nextLine();
            try {
                input = client.eval(line);
                System.out.print(input);
            } catch (Throwable ex) {
                System.out.print(ex.getMessage());
            }
        }
        System.out.println();
    }
}
