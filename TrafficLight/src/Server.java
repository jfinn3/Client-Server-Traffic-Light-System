import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final int MAX_CLIENTS = 2;

    private List<ClientHandler> clientHandlers;
    private TrafficLight trafficLight;
    private PedestrianSignal pedestrianSignal;

    public Server() {
        clientHandlers = new ArrayList<>();
        trafficLight = new TrafficLight();
        pedestrianSignal = new PedestrianSignal();
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

    private void broadcastMessage(String message) {
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.sendMessage(message);
        }
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Server started. Listening for clients...");
            while (true) {
                if (clientHandlers.size() < MAX_CLIENTS) {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    clientHandlers.add(clientHandler);
                    clientHandler.start();
                    System.out.println("New client connected. Total clients: " + clientHandlers.size());
                } else {
                    // Reject new connections after reaching the maximum allowed clients
                    Socket clientSocket = serverSocket.accept();
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    out.println("Maximum number of clients reached. Try again later.");
                    clientSocket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class TrafficLight {
        private String state;

        public TrafficLight() {
            state = "Green";
        }

        public synchronized void setState(String newState) {
            state = newState;
            System.out.println("Traffic Light: " + state);
            broadcastMessage("TRAFFIC_LIGHT:" + state);
        }

        public synchronized String getState() {
            return state;
        }
    }

    private class PedestrianSignal {
        private String state;

        public PedestrianSignal() {
            state = "RedMan";
        }

        public synchronized void setState(String newState) {
            state = newState;
            System.out.println("Pedestrian Signal: " + state);
            broadcastMessage("PED_SIGNAL:" + state);
        }

        public synchronized String getState() {
            return state;
        }
    }

    private class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) throws IOException {
            clientSocket = socket;
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        @Override
        public void run() {
            try {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    handleClientInput(inputLine);
                }
            } catch (IOException e) {
                // Client disconnected
                System.out.println("Client disconnected.");
            } finally {
                try {
                    in.close();
                    out.close();
                    clientSocket.close();
                    clientHandlers.remove(this);
                    System.out.println("Client removed. Total clients: " + clientHandlers.size());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleClientInput(String input) {
            switch (input) {
                case "PRESS_BUTTON":
                    if (pedestrianSignal.getState().equals("RedMan")) {
                        pedestrianSignal.setState("GreenMan");
                        trafficLight.setState("Amber");
                        trafficLight.setState("Red");
                        pedestrianSignal.setState("GreenManFlashing");
                        try {
                            Thread.sleep(5000); // Simulating the flashing green time (5 seconds)
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        pedestrianSignal.setState("RedMan");
                        trafficLight.setState("AmberFlashing");
                        try {
                            Thread.sleep(5000); // Simulating the flashing amber time (5 seconds)
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        trafficLight.setState("Green");
                    } else {
                        System.out.println("Button press has no effect. Pedestrian signal is already in a crossing state.");
                    }
                    break;

                case "GET_PANEL_STATUS":
                    String panelStatus = "STANDBY";
                    if (pedestrianSignal.getState().equals("GreenMan") || pedestrianSignal.getState().equals("GreenManFlashing")) {
                        panelStatus = "WAITING";
                    } else if (pedestrianSignal.getState().equals("RedMan")) {
                        panelStatus = "OFF";
                    }
                    sendMessage("PANEL_STATUS:" + panelStatus);
                    break;

                case "EXIT":
                    try {
                        in.close();
                        out.close();
                        clientSocket.close();
                        clientHandlers.remove(this);
                        System.out.println("Client removed. Total clients: " + clientHandlers.size());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                default:
                    System.out.println("Unknown command from client: " + input);
                    break;
            }
        }
    }
}
