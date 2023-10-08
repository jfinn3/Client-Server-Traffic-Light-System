import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            String inputLine;

            System.out.println("Connected to server. Type 'EXIT' to quit.");

            while (true) {
                System.out.println("\nMenu:");
                System.out.println("1. Press the button to trigger a crossing sequence");
                System.out.println("2. Report the status of the Panel");
                System.out.println("or type 'exit' to exit the client.");
                System.out.print("Enter your choice: ");

                inputLine = userInput.readLine();
                if (inputLine.equalsIgnoreCase("EXIT")) {
                    out.println("EXIT");
                    break;
                }

                switch (inputLine) {
                    case "1":
                        out.println("PRESS_BUTTON");
                        break;

                    case "2":
                        out.println("GET_PANEL_STATUS");
                        String response = in.readLine();
                        if (response.startsWith("PANEL_STATUS:")) {
                            String panelStatus = response.substring("PANEL_STATUS:".length());
                            System.out.println("Panel status: " + panelStatus);
                        } else {
                            System.out.println("Invalid response from server.");
                        }
                        break;

                    default:
                        System.out.println("Invalid choice. Please try again.");
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
