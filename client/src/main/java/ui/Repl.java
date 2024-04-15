package ui;

import dataAccess.DataAccessException;
import ui.websocket.NotificationHandler;
import webSocketMessages.serverMessages.Error;
import webSocketMessages.serverMessages.LoadGame;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.serverMessages.ServerNotification;

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
//        if (serverMessage.getClass() == ServerNotification.class) {
//            System.out.println("made it to notify in repl");
//            System.out.println(((ServerNotification) serverMessage).getMessage());
//        }
//        else if (serverMessage.getClass() == LoadGame.class) {
//            try {
//                client.redraw();
//            } catch (DataAccessException ex) {
//                System.out.println("Repl LoadGame error: " + ex.getMessage());
//            }
//        }
//        else if (serverMessage.getClass() == Error.class) {
//            System.out.println(((Error) serverMessage).getErrorMsg());
//        }
    }
    @Override
    public void loadGame(String message) {
        try {
            client.redraw();
            System.out.println(message);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
