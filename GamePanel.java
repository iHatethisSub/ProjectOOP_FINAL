import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

// GamePanel = หน้าที่ "เก็บข้อมูลที่จะแสดง" และ "วาดหน้าจอ" เท่านั้น (ไม่มีปุ่ม ไม่ต่อ Server)
public class GamePanel extends JPanel {

    // หน้าจอที่มีทั้งหมด (ให้ GameFrame เรียกใช้ได้)
    public static final int LOBBY = 0;
    public static final int GAME = 1;
    public static final int RESULT = 2;

    private int screen = LOBBY;      // หน้าจอที่กำลังแสดงอยู่
    private String playerName = "";  // ชื่อของเรา

    // ข้อมูลที่ได้รับจาก Server เอาไว้ใช้วาดหน้าจอ
    private ArrayList<String> players = new ArrayList<String>();      // ชื่อผู้เล่นในห้อง (คนแรก = หัวหน้าห้อง)
    private ArrayList<String> resultLines = new ArrayList<String>();  // บรรทัดอันดับตอนจบเกม
    private String round = "";        // รอบที่
    private String hint = "";         // คำใบ้
    private int wordLength = 0;       // จำนวนตัวอักษรของคำตอบ
    private String answerWord = "";   // เฉลย (มีค่าตอนจบรอบ)
    private String message = "";      // ข้อความสถานะ เช่น ใครตอบถูก

    public GamePanel() {
        setOpaque(false); // ให้พื้นหลังใสเพื่อโชว์สีพื้นหลังของ JFrame
    }

    // ================= method ให้ GameFrame เรียก เพื่อเปลี่ยนข้อมูล =================

    public void setPlayerName(String name) {
        playerName = name;
    }

    public int getScreen() {
        return screen;
    }

    public void setScreen(int newScreen) {
        screen = newScreen;
        repaint();
    }

    public void addPlayer(String name) {
        players.add(name);
        repaint();
    }

    public void removePlayer(String name) {
        players.remove(name);
        repaint();
    }

    public int getPlayerCount() {
        return players.size();
    }

    // เราเป็นหัวหน้าห้องหรือไม่ (คนแรกในรายชื่อ)
    public boolean isHost() {
        if (players.size() > 0 && players.get(0).equals(playerName)) {
            return true;
        }
        return false;
    }

    // เริ่มรอบใหม่ ล้างข้อมูลรอบเก่า แล้วไปหน้า game
    public void newRound(String r) {
        round = r;
        hint = "";
        wordLength = 0;
        answerWord = "";
        message = "";
        screen = GAME;
        repaint();
    }

    public void setHint(String h) {
        hint = h;
        repaint();
    }

    public void setWordLength(int n) {
        wordLength = n;
        repaint();
    }

    public void setAnswerWord(String word) {
        answerWord = word;
        repaint();
    }

    public void setMessage(String text) {
        message = text;
        repaint();
    }

    public void addMessage(String text) {
        message = message + text;
        repaint();
    }

    public void clearResult() {
        resultLines.clear();
        repaint();
    }

    public void addResultLine(String line) {
        resultLines.add(line);
        repaint();
    }

    // ================= วาดหน้าจอ =================

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int centerX = getWidth() / 2; // จุดกึ่งกลางหน้าจอ

        // วาดตามหน้าจอที่แสดงอยู่
        if (screen == LOBBY) {
            drawLobby(g, centerX);
        } else if (screen == GAME) {
            drawGame(g, centerX);
        } else {
            drawResult(g, centerX);
        }
    }

    // ---------- หน้า lobby ----------
    private void drawLobby(Graphics g, int centerX) {
        // 1. หัวข้อ "ห้องรอผู้เล่น" (สีทอง)
        g.setFont(new Font("Tahoma", Font.BOLD, 30));
        g.setColor(new Color(243, 185, 74));
        drawCentered(g, "ห้องรอผู้เล่น", centerX, 160);

        // 2. จำนวนผู้เล่น (สีขาว)
        g.setFont(new Font("Tahoma", Font.PLAIN, 20));
        g.setColor(Color.WHITE);
        drawCentered(g, "ผู้เล่นในห้อง " + players.size() + " / 8 คน", centerX, 220);

        // 3. ข้อความเงื่อนไข (สีเทา)
        g.setFont(new Font("Tahoma", Font.PLAIN, 14));
        g.setColor(Color.GRAY);
        drawCentered(g, "ต้องมีผู้เล่นอย่างน้อย 2 คนจึงจะเริ่มเกมได้", centerX, 255);

        // 4. สถานะผู้เล่น (สีเขียว)
        g.setFont(new Font("Tahoma", Font.PLAIN, 18));
        g.setColor(new Color(74, 222, 128));
        if (isHost()) {
            drawCentered(g, "คุณเป็นหัวหน้าห้อง — กดปุ่ม \"เริ่มเกม\" ด้านล่างเมื่อทุกคนพร้อม", centerX, 330);
        } else {
            drawCentered(g, "รอหัวหน้าห้องกดเริ่มเกม...", centerX, 330);
        }

        // 5. รายชื่อผู้เล่นทุกคน (สีขาว)
        g.setFont(new Font("Tahoma", Font.PLAIN, 18));
        g.setColor(Color.WHITE);
        for (int i = 0; i < players.size(); i++) {
            String line = (i + 1) + ". " + players.get(i);
            if (i == 0) {
                line = line + " (หัวหน้าห้อง)";
            }
            if (players.get(i).equals(playerName)) {
                line = line + " (คุณ)";
            }
            drawCentered(g, line, centerX, 390 + i * 30);
        }
    }

    // ---------- หน้า game ----------
    private void drawGame(Graphics g, int centerX) {
        // รอบที่ (สีเทา)
        g.setFont(new Font("Tahoma", Font.PLAIN, 20));
        g.setColor(Color.GRAY);
        drawCentered(g, "รอบที่ " + round, centerX, 100);

        // จำนวนคนปัจจุบัน (สีเขียว)
        g.setFont(new Font("Tahoma", Font.PLAIN, 16));
        g.setColor(new Color(74, 222, 128));
        drawCentered(g, "ผู้เล่นในห้อง: " + players.size() + " คน", centerX, 135);

        // คำใบ้ (สีทอง)
        g.setFont(new Font("Tahoma", Font.BOLD, 28));
        g.setColor(new Color(243, 185, 74));
        drawCentered(g, "คำใบ้: " + hint, centerX, 240);

        // ช่องอักษร: วาดกรอบ 1 ช่องต่อ 1 ตัวอักษร
        int boxSize = 60;
        int gap = 10;
        int totalWidth = wordLength * boxSize + (wordLength - 1) * gap;
        int startX = centerX - totalWidth / 2;

        for (int i = 0; i < wordLength; i++) {
            int x = startX + i * (boxSize + gap);

            g.setColor(new Color(243, 185, 74));
            g.drawRoundRect(x, 300, boxSize, boxSize, 10, 10);

            // ถ้ามีเฉลยแล้ว (จบรอบ) ให้เขียนตัวอักษรลงในช่อง
            if (i < answerWord.length()) {
                g.setFont(new Font("Tahoma", Font.BOLD, 30));
                g.setColor(Color.WHITE);
                String letter = answerWord.substring(i, i + 1);
                FontMetrics metrics = g.getFontMetrics();
                int letterX = x + (boxSize - metrics.stringWidth(letter)) / 2;
                g.drawString(letter, letterX, 345);
            }
        }

        // ข้อความสถานะ เช่น ใครตอบถูก / ตอบผิด (สีเขียว)
        g.setFont(new Font("Tahoma", Font.PLAIN, 22));
        g.setColor(new Color(74, 222, 128));
        drawCentered(g, message, centerX, 450);
    }

    // ---------- หน้า result ----------
    private void drawResult(Graphics g, int centerX) {
        g.setFont(new Font("Tahoma", Font.BOLD, 30));
        g.setColor(new Color(243, 185, 74));
        drawCentered(g, "Game Results", centerX, 120);

        // อันดับ 1 สีทอง ที่เหลือสีขาว
        g.setFont(new Font("Tahoma", Font.PLAIN, 24));
        for (int i = 0; i < resultLines.size(); i++) {
            if (i == 0) {
                g.setColor(new Color(243, 185, 74));
            } else {
                g.setColor(Color.WHITE);
            }
            drawCentered(g, resultLines.get(i), centerX, 200 + i * 45);
        }

        g.setFont(new Font("Tahoma", Font.PLAIN, 16));
        g.setColor(Color.GRAY);
        if (isHost()) {
            drawCentered(g, "กดปุ่ม \"เล่นอีกครั้ง\" เพื่อเริ่มเกมใหม่", centerX, 600);
        } else {
            drawCentered(g, "รอหัวหน้าห้องเริ่มเกมใหม่...", centerX, 600);
        }
    }

    private void drawCentered(Graphics g, String text, int centerX, int y) {
        FontMetrics metrics = g.getFontMetrics();
        int textWidth = metrics.stringWidth(text);
        int x = centerX - (textWidth / 2);
        g.drawString(text, x, y);
    }
}
