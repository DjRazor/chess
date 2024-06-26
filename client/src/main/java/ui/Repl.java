package ui;

import ui.websocket.NotificationHandler;

import java.util.Scanner;

public class Repl implements NotificationHandler {

    private final ChessClient client;
    public Repl(String serverURL) {
        client = new ChessClient(serverURL, this);
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

    @Override
    public void notify(String message) {
        System.out.println(message);
    }
    @Override
    public void loadGame(String message) {
        try {
            client.redraw();
            System.out.println(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
