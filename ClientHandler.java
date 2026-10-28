import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final GameRoom room;
    private BufferedReader in;
    private PrintWriter out;
    private String playerName;
    private Player player;

    public ClientHandler(Socket socket, GameRoom room) {
        this.socket = socket;
        this.room = room;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            String message;

            while ((message = in.readLine()) != null) {
                String[] data = message.split("#", 2);

                switch (data[0]) {
                    case "JOIN":
                        if (data.length > 1) {
                            room.join(this, data[1]);
                        }
                        break;

                    case "START":
                        room.start(this);
                        break;

                    case "GUESS":
                        if (data.length > 1) {
                            room.handleGuess(this, data[1]);
                        }
                        break;
                }
            }

        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        } finally {                     // แก้ไข: วงเล็บเดิมปิดผิดที่ ทำให้ compile ไม่ผ่าน
            room.leave(this);
            close();
        }
    }

    public synchronized void send(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException ignored) {}
    }

    

    // เพิ่มใหม่: GameRoom เรียก client.setPlayer(...) แต่เดิมไม่มี method นี้
    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}
