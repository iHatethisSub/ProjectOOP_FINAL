import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

// GamePanel = เก็บข้อมูลที่จะแสดง และวาดหน้าจอเท่านั้น (ไม่มีปุ่ม ไม่ต่อ Server)
public class GamePanel extends JPanel {

    // หน้าจอที่มีทั้งหมด
    public static final int LOBBY = 0;
    public static final int GAME = 1;
    public static final int RESULT = 2;

    // สีที่ใช้ทั้งหมด
    private static final Color GOLD = new Color(243, 185, 74);
    private static final Color GREEN = new Color(74, 222, 128);
    private static final Color WHITE = Color.WHITE;

    // ฟอนต์ที่ใช้ทั้งหมด
    private static final Font TITLE_FONT = new Font("Tahoma", Font.BOLD, 30);
    private static final Font TEXT_FONT = new Font("Tahoma", Font.PLAIN, 20);

    private int screen = LOBBY;
    private String playerName = "";

    // ข้อมูลที่ได้รับจาก Server
    private ArrayList<String> players = new ArrayList<String>();      // คนแรก = หัวหน้าห้อง
    private ArrayList<String> resultLines = new ArrayList<String>();
    private String round = "";
    private String hint = "";
    private int wordLength = 0;
    private String answerWord = "";
    private String message = "";

    public GamePanel() {
        setOpaque(false);
    }

    // ================= method ให้ GameFrame เรียก =================

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
        return players.size() > 0 && players.get(0).equals(playerName);
    }

    // เริ่มรอบใหม่ ล้างข้อมูลรอบเก่า แล้วไปหน้า GAME
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
        int centerX = getWidth() / 2;

        if (screen == LOBBY) {
            drawLobby(g, centerX);
        } else if (screen == GAME) {
            drawGame(g, centerX);
        } else {
            drawResult(g, centerX);
        }
    }

    private void drawLobby(Graphics g, int centerX) {
        drawCentered(g, "Waiting Room", centerX, 160, TITLE_FONT, GOLD);
        drawCentered(g, "Players in room: " + players.size() + " / 8", centerX, 220, TEXT_FONT, WHITE);
        drawCentered(g, "At least 2 players are needed to start the game", centerX, 255, TEXT_FONT, Color.GRAY);

        if (isHost()) {
            drawCentered(g, "You are the host - press \"Start Game\" when everyone is ready", centerX, 330, TEXT_FONT, GREEN);
        } else {
            drawCentered(g, "Waiting for the host to start the game...", centerX, 330, TEXT_FONT, GREEN);
        }

        // รายชื่อผู้เล่น (บรรทัดละ 30 พิกเซล)
        for (int i = 0; i < players.size(); i++) {
            String line = (i + 1) + ". " + players.get(i);
            if (i == 0) {
                line = line + " (Host)";
            }
            if (players.get(i).equals(playerName)) {
                line = line + " (You)";
            }
            drawCentered(g, line, centerX, 390 + i * 30, TEXT_FONT, WHITE);
        }
    }

    private void drawGame(Graphics g, int centerX) {
        drawCentered(g, "Round " + round, centerX, 100, TEXT_FONT, Color.GRAY);
        drawCentered(g, "Players in room: " + players.size(), centerX, 135, TEXT_FONT, GREEN);
        drawCentered(g, "Hint: " + hint, centerX, 240, TITLE_FONT, GOLD);

        // ช่องตัวอักษร: 1 ช่องต่อ 1 ตัว (ช่องละ 60 พิกเซล เว้น 10)
        int boxSize = 60;
        int gap = 10;
        int totalWidth = wordLength * (boxSize + gap) - gap;
        int startX = centerX - totalWidth / 2;

        for (int i = 0; i < wordLength; i++) {
            int x = startX + i * (boxSize + gap);
            g.setColor(GOLD);
            g.drawRect(x, 300, boxSize, boxSize);

            // ถ้ามีเฉลยแล้ว ให้เขียนตัวอักษรลงในช่อง
            if (i < answerWord.length()) {
                String letter = answerWord.substring(i, i + 1);
                drawCentered(g, letter, x + boxSize / 2, 345, TITLE_FONT, WHITE);
            }
        }

        drawCentered(g, message, centerX, 450, TEXT_FONT, GREEN);
    }

    private void drawResult(Graphics g, int centerX) {
        drawCentered(g, "Game Results", centerX, 120, TITLE_FONT, GOLD);

        // อันดับ 1 สีทอง ที่เหลือสีขาว
        for (int i = 0; i < resultLines.size(); i++) {
            Color color = WHITE;
            if (i == 0) {
                color = GOLD;
            }
            drawCentered(g, resultLines.get(i), centerX, 200 + i * 45, TEXT_FONT, color);
        }

        if (isHost()) {
            drawCentered(g, "Press \"Play Again\" to start a new game", centerX, 600, TEXT_FONT, Color.GRAY);
        } else {
            drawCentered(g, "Waiting for the host to start a new game...", centerX, 600, TEXT_FONT, Color.GRAY);
        }
    }

    // วาดข้อความให้อยู่กึ่งกลางที่ centerX (ตั้งฟอนต์และสีให้ในตัว)
    private void drawCentered(Graphics g, String text, int centerX, int y, Font font, Color color) {
    g.setFont(font);
    g.setColor(color);
    int textWidth = g.getFontMetrics().stringWidth(text);
    g.drawString(text, centerX - textWidth / 2, y);
}
}