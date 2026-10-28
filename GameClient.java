import java.io.*;
import java.net.Socket;

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

    // แก้ไข: รับ ip กับ port จากหน้า login (เดิมกำหนด localhost:
    //  ตายตัว) และคืน true/false ว่าต่อได้ไหม
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
                String[] data = message.split("#", 2); //JOIN#Player1    = data[0] = join , data[1] = player1
                                                       // ใส่เลขสองเพราะป้องกันไม่ให้ split เกินสองส่วนเพราะ data เป็น array ที่เก็บข้อมูลสองส่วน
                try {
                    handleMessage(data);
                } catch (Exception e) {
                    System.out.println("Error handling message: " + e);
                }
            }

        } catch (IOException e) {
            System.out.println("Disconnected from server");
        }
    }

    // เพิ่มใหม่: แยกว่าข้อความที่ได้รับคืออะไร แล้วสั่ง GameFrame ให้เปลี่ยนหน้าจอ
    private void handleMessage(String[] data) {
        String value = "";
        if (data.length > 1) {
            value = data[1];//เช็คว่ามีข้อมูลอยู่หลัง # หรือไม่ ถ้ามีให้เก็บไว้ในตัวแปร value เพื่อส่งเช็คต่อ
        }

        switch (data[0]) {
            case "JOIN":
                frame.addPlayer(value);
                break;

            case "LEAVE":
                frame.removePlayer(value);
                break;

            case "ROUND":
                frame.setRound(value); // อัปเดตรอบที่กำลังเล่น
                break;

            case "HINT":
                frame.setHint(value);
                break;

            case "LENGTH":
                frame.setWordLength(value);// อัปเดตช่องตามจำนวนตัวอักษร
                break;

            case "WRONG":
                frame.showMessage(value + " ตอบผิด ลองใหม่อีกครั้ง");
                break;

            case "CORRECT":
                frame.showCorrect(value);//ส่งข้อความว่าใครตอบถูก
                break;

            case "ANSWER":
                frame.showAnswer(value);//สั่งให้เฉลยคำตอบ
                break;

            case "SCORE":
                frame.showScore(value);//สั่งให้อัพเดทคะแนนของผู้เล่น
                break;

            case "GAMEOVER":
                frame.gameOver();//แสดงหน้าจอจบเกม
                break;

            case "RESULT":
                frame.addResult(value);//สรุปคะแนนอันดับ
                break;

            case "ERROR":
                frame.showError(value);
                break;
        }
    }

    public void startReceiving() {
        Thread thread = new Thread(new Runnable() {//สร้าง thread ใหม่เพื่อรันแยกจากหน้าจอหลัก ที่ต้องแยกเพราะ
                                                   //receiveMessage() มีการวนลูป while เพื่อรอฟังข้อมูลอยู่ตลอดเวลา ถ้าไม่แยก Thread หน้าจอโปรแกรมจะ ค้างและกดอะไรไม่ได้เลย
            @Override
            public void run() {
                receiveMessage();
            }
        });

        thread.start();
    }


    public void sendMessage(String message) { //ส่งข้อความไปหา server โดย
        if (out != null) {//เช็คว่ามี printwriter พร้อมส่งหรือไม่
            out.println(message); //ถ้ามีให้ส่งข้อความไป
        }
    }


    public void disconnect() {
        try {
            if (socket != null) {//ถ้า socket ไม่เป็น null แสดงว่ามีการเชื่อมต่ออยู่
                socket.close();//ปิด socket
            }
        } catch (IOException e) {
            System.out.println("Error closing connection: " + e.getMessage());//ถ้าเกิด error ตอนปิดให้แสดงข้อความ + getMessage() เพื่อดูว่าเกิด error อะไร
        }
    }


}
