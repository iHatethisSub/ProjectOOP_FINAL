import java.io.*;
import java.net.Socket;
import javax.swing.SwingUtilities;   // เพิ่มใหม่: ใช้ส่งงานไปอัปเดตหน้าจอ

public class GameClient {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    // เพิ่มใหม่: เก็บหน้าต่างหลักไว้ เพื่อสั่งให้หน้าจอเปลี่ยนตามข้อความจาก Server
    private GameFrame frame;

    // แก้ไข: รับ GameFrame เข้ามาตอนสร้าง
    public GameClient(GameFrame frame) {
        this.frame = frame;
    }

    // แก้ไข: รับ ip กับ port จากหน้า login (เดิมกำหนด localhost:5000 ตายตัว) และคืน true/false ว่าต่อได้ไหม
    public boolean connect(String ip, int port) {
        try {
            socket = new Socket(ip, port);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            System.out.println("Connected to server");
            return true;

        } catch (IOException e) {
            System.out.println("Cannot connect to server: " + e.getMessage());
            return false;
        }
    }


    public void receiveMessage() {
        try {
            String message;

            while ((message = in.readLine()) != null) {
                final String[] data = message.split("#", 2);

                // เพิ่มใหม่: Thread นี้ห้ามแก้หน้าจอตรง ๆ ต้องฝากให้ Thread ของหน้าจอ (invokeLater) ทำแทน
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        handleMessage(data);
                    }
                });
            }

        } catch (IOException e) {
            System.out.println("Disconnected from server");
        }
    }

    // เพิ่มใหม่: แยกว่าข้อความที่ได้รับคืออะไร แล้วสั่ง GameFrame ให้เปลี่ยนหน้าจอ
    private void handleMessage(String[] data) {
        String value = "";
        if (data.length > 1) {
            value = data[1];
        }

        switch (data[0]) {
            case "JOIN":
                frame.addPlayer(value);
                break;

            case "LEAVE":
                frame.removePlayer(value);
                break;

            case "ROUND":
                frame.setRound(value);
                break;

            case "HINT":
                frame.setHint(value);
                break;

            case "LENGTH":
                frame.setWordLength(value);
                break;

            case "WRONG":
                frame.showMessage(value + " ตอบผิด ลองใหม่อีกครั้ง");
                break;

            case "CORRECT":
                frame.showCorrect(value);
                break;

            case "ANSWER":
                frame.showAnswer(value);
                break;

            case "SCORE":
                frame.showScore(value);
                break;

            case "GAMEOVER":
                frame.gameOver();
                break;

            case "RESULT":
                frame.addResult(value);
                break;

            case "ERROR":
                frame.showError(value);
                break;
        }
    }

    public void startReceiving() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                receiveMessage();
            }
        });

        thread.start();
    }


    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }


    public void disconnect() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }


}
