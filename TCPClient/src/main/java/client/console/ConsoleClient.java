package client.console;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ConsoleClient {

    public static void main(String[] args) {
        String host = "localhost";
        int port = 3000;

        try (
                Socket socket = new Socket(host, port);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                BufferedWriter out = new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream()));
                Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("Connected to server.");

            // Thread to read messages from server
            new Thread(() -> {
                try {
                    String response;
                    while ((response = in.readLine()) != null) {
                        System.out.println("SERVER: " + response);
                    }
                } catch (IOException e) {
                    System.out.println("Connection closed.");
                }
            }).start();

            // Send user input
            while (true) {
                String input = scanner.nextLine();
                out.write(input);
                out.newLine();
                out.flush();

                if (input.equalsIgnoreCase("BYE")) {
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}