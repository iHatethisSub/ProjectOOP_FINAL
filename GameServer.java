import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class GameServer {
    private static final int PORT = 5000;

    public static void main(String[] args) {
        GameRoom room = new GameRoom();

        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("server started");

            while (true) {
                Socket socket = server.accept();
                System.out.println("Client connected: " + socket.getInetAddress().getHostAddress());
                ClientHandler client = new ClientHandler(socket, room);
                new Thread(client).start();
            }
        } catch (IOException e) {
            System.out.println("Error starting server: " + e.getMessage());
        }
    }
}