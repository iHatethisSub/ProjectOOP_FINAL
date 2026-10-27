import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// GameFrame = หน้าต่างหลัก: มีปุ่ม ช่องพิมพ์ Login และต่อ Server
// ส่วนวาดหน้าจอ/เก็บข้อมูลที่แสดง แยกไปอยู่ใน GamePanel
public class GameFrame extends JFrame implements ActionListener {

    private JTextField txtAnswer;
    private JButton btnAnswer;
    private JButton btnStart;
    private JButton btnExit;
    String serverIp;
    int port;
    String playerName;

    private GameClient client;       // ตัวเชื่อมต่อกับ Server
    private GamePanel gamePanel;     // แยกออกมาเป็น Class ของตัวเอง (ไฟล์ GamePanel.java)

    public GameFrame() {
        setTitle("Word Dash");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        setResizable(false);

        getContentPane().setBackground(new Color(15, 17, 26));

        // แก้ไข: ใช้ GamePanel ที่แยกเป็นอีกไฟล์ (เดิมเป็น inner class GameDrawPanel)
        gamePanel = new GamePanel();
        gamePanel.setBounds(0, 0, 1200, 650);
        add(gamePanel);

        // ช่องรับคำตอบ
        txtAnswer = new JTextField();
        txtAnswer.setBounds(40, 690, 700, 40);
        txtAnswer.setFont(new Font("Tahoma", Font.PLAIN, 16));
        txtAnswer.setBackground(new Color(15, 17, 26));
        txtAnswer.setForeground(Color.WHITE);
        txtAnswer.setCaretColor(Color.WHITE);
        txtAnswer.addActionListener(this); // กด Enter ในช่องพิมพ์เพื่อส่งคำตอบ
        add(txtAnswer);

        // ปุ่มตอบ
        btnAnswer = new JButton("ANSWER");
        btnAnswer.setBounds(760, 690, 110, 40);
        btnAnswer.setBackground(new Color(243, 185, 74));
        btnAnswer.setForeground(Color.BLACK);
        btnAnswer.setFocusPainted(false);
        btnAnswer.addActionListener(this);
        add(btnAnswer);

        // ปุ่มเริ่มเกม
        btnStart = new JButton("START");
        btnStart.setBounds(880, 690, 110, 40);
        btnStart.setBackground(new Color(72, 207, 122));
        btnStart.setForeground(Color.BLACK);
        btnStart.setFocusPainted(false);
        btnStart.addActionListener(this);
        add(btnStart);

        // ปุ่มออกจากเกม
        btnExit = new JButton("EXIT GAME.");
        btnExit.setBounds(1000, 690, 140, 40);
        btnExit.setBackground(new Color(40, 44, 58));
        btnExit.setForeground(Color.WHITE);
        btnExit.setFocusPainted(false);
        btnExit.addActionListener(this);
        add(btnExit);

        // LOGIN ขึ้นมาก่อนเข้าเกม ถ้ากด Cancel ให้ปิดโปรแกรม
        // ถ้าต่อ Server ไม่ได้ ให้กรอกใหม่อีกครั้ง
        boolean connected = false;
        while (!connected) {
            if (!showJoinDialog()) {
                System.exit(0);
            }

            client = new GameClient(this);
            connected = client.connect(serverIp, port);

            if (!connected) {
                JOptionPane.showMessageDialog(null, "Cannot connect to Server Please try again.");
            }
        }

        setTitle("Word Dash - " + playerName);
        gamePanel.setPlayerName(playerName);   // เพิ่มใหม่: บอก GamePanel ว่าเราชื่ออะไร
        updateScreen();
        setVisible(true);

        // เริ่มรับข้อความจาก Server แล้วแจ้งว่าเราเข้าห้อง
        client.startReceiving();
        client.sendMessage("JOIN#" + playerName);
    }

    boolean showJoinDialog() {
        JTextField ipField = new JTextField("localhost");
        JTextField portField = new JTextField("50101");
        JTextField nameField = new JTextField();

        // String = ป้ายข้อความ, JTextField = ช่องกรอก
        Object[] message = {
            "IP  Server:", ipField,
            "Port:", portField,
            "Name:", nameField
        };

        int result = JOptionPane.showConfirmDialog(null, message,
                "WORD DASH - เข้าร่วมเกม",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return false;
        }

        serverIp = ipField.getText().trim();
        port = Integer.parseInt(portField.getText().trim());

        // จัดชื่อแบบเดียวกับ Server (ตัดช่องว่าง ตัด # ถ้าว่างใช้ชื่อ Player)
        playerName = nameField.getText().trim().replace("#", "");
        if (playerName.isEmpty()) {
            playerName = "Player";
        }
        return true;
    }

    // จัดการ Event การกดปุ่ม
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == btnExit) {
            client.disconnect();
            System.exit(0);
        } else if (source == btnStart) {
            client.sendMessage("START");
        } else if (source == btnAnswer || source == txtAnswer) {
            String text = txtAnswer.getText().trim();
            if (!text.isEmpty()) {
                client.sendMessage("GUESS#" + text);
                txtAnswer.setText(""); // ล้างช่องพิมพ์
            }
        }
    }

    // ================= method ที่ GameClient เรียกเมื่อได้ข้อความจาก Server =================
    // แก้ไข: ส่งข้อมูลต่อให้ GamePanel เก็บและวาด

    // มีคนเข้าห้อง
    public void addPlayer(String name) {
        gamePanel.addPlayer(name);
        updateScreen();
    }

    // มีคนออกจากห้อง
    public void removePlayer(String name) {
        gamePanel.removePlayer(name);
        updateScreen();
    }

    // เริ่มรอบใหม่ (GamePanel จะล้างข้อมูลรอบเก่าและไปหน้า game)
    public void setRound(String r) {
        gamePanel.newRound(r);
        txtAnswer.setText("");
        updateScreen();
        txtAnswer.requestFocus();
    }

    public void setHint(String h) {
        gamePanel.setHint(h);
    }

    public void setWordLength(String n) {
        gamePanel.setWordLength(Integer.parseInt(n));
    }

    public void showMessage(String text) {
        gamePanel.setMessage(text);
    }

    public void showCorrect(String name) {
        gamePanel.setMessage(name + " Correct! +10 Points");
    }

    public void showAnswer(String word) {
        gamePanel.setAnswerWord(word);
    }

    // data มาในรูป "ชื่อ#คะแนนรวม"
    public void showScore(String data) {
        String[] parts = data.split("#");
        if (parts.length == 2) {
            gamePanel.addMessage(" (Total " + parts[1] + " Points)");
        }
    }

    // จบเกม ไปหน้า result แล้วรอรับอันดับ
    public void gameOver() {
        gamePanel.clearResult();
        gamePanel.setScreen(GamePanel.RESULT);
        updateScreen();
    }

    // data มาในรูป "อันดับ#ชื่อ#คะแนน"
    public void addResult(String data) {
        String[] parts = data.split("#");
        if (parts.length == 3) {
            gamePanel.addResultLine(parts[0] + ". " + parts[1] + "   " + parts[2] + " Points");
        }
    }

    public void showError(String text) {
        JOptionPane.showMessageDialog(this, text);

        // ถ้ายังไม่มีชื่อใครในห้องเลย แปลว่าเราเข้าห้องไม่สำเร็จ (ห้องเต็ม / ชื่อซ้ำ / เกมเริ่มแล้ว)
        if (gamePanel.getPlayerCount() == 0) {
            client.disconnect();
            System.exit(0);
        }
    }

    // เปิด/ปิดช่องพิมพ์กับปุ่มให้ตรงกับหน้าจอที่แสดง
    private void updateScreen() {
        boolean inGame = (gamePanel.getScreen() == GamePanel.GAME);

        txtAnswer.setVisible(inGame);
        btnAnswer.setVisible(inGame);
        btnStart.setVisible(!inGame && gamePanel.isHost());   // เฉพาะหัวหน้าห้องที่เห็นปุ่มเริ่มเกม

        if (gamePanel.getScreen() == GamePanel.RESULT) {
            btnStart.setText("Play Again");
        } else {
            btnStart.setText("START");
        }

        gamePanel.repaint();
    }

    public static void main(String[] args) {
        new GameFrame();
    }
}
