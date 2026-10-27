import java.util.ArrayList;
import java.util.List;

public class GameRoom {
    private List<ClientHandler> clients = new ArrayList<>();

    private static final int MAX_PLAYERS = 8;

    private String word;
    private String hint;

    private int round = 0;
    private int totalRounds = 5;

    private boolean gameStarted = false;
    private boolean roundEnded = false;

    public GameRoom() { }

    public synchronized void join(ClientHandler client, String name) {
        // เพิ่มใหม่: ถ้า client คนนี้ join ไปแล้ว ไม่ต้องทำซ้ำ
        if (client.getPlayer() != null) {
            return;
        }

        if (gameStarted) {
            client.send("ERROR#Game already started");
            return;
        }

        if (clients.size() >= MAX_PLAYERS) {
            client.send("ERROR#Room is full");
            return;
        }

        // เพิ่มใหม่: จัดชื่อให้เรียบร้อย (ตัด # ออก เพราะ # ใช้คั่นข้อความ)
        name = name.trim().replace("#", "");
        if (name.isEmpty()) {
            name = "Player";
        }

        // เพิ่มใหม่: ห้ามใช้ชื่อซ้ำกับคนที่อยู่ในห้อง
        for (ClientHandler c : clients) {
            if (c.getPlayer().getName().equalsIgnoreCase(name)) {
                client.send("ERROR#Name already used");
                return;
            }
        }

        Player player = new Player(name);

        client.setPlayer(player);

        // เพิ่มใหม่: บอกผู้เล่นใหม่ว่าในห้องมีใครอยู่ก่อนแล้วบ้าง
        for (ClientHandler c : clients) {
            client.send("JOIN#" + c.getPlayer().getName());
        }

        clients.add(client);

        broadcast("JOIN#" + name);
    }


    public synchronized void leave(ClientHandler client) {
        clients.remove(client);

        // เพิ่มใหม่: บอกทุกคนว่ามีคนออกจากห้อง
        if (client.getPlayer() != null) {
            broadcast("LEAVE#" + client.getPlayer().getName());
        }

        // เพิ่มใหม่: ถ้าไม่มีใครเหลือในห้อง ให้รีเซ็ตห้อง (ไม่งั้นจะ join ไม่ได้อีกเลย)
        if (clients.size() == 0) {
            gameStarted = false;
            round = 0;
        }
    }


    public synchronized void start(ClientHandler client) {
        if (gameStarted) {
            client.send("ERROR#Game already started");
            return;
        }

        // เพิ่มใหม่: ให้เฉพาะหัวหน้าห้อง (คนแรกในห้อง) เป็นคนกดเริ่ม
        if (clients.size() == 0 || clients.get(0) != client) {
            client.send("ERROR#Only the host can start the game");
            return;
        }

        if (clients.size() < 2) {
            client.send("ERROR#Need at least 2 players");
            return;
        }

        gameStarted = true;
        round = 0;

        // เพิ่มใหม่: รีเซ็ตคะแนนทุกคน เผื่อเล่นรอบที่ 2
        for (ClientHandler c : clients) {
            c.getPlayer().resetScore();
        }

        nextRound();
    }

    public synchronized void nextRound() {
        // เพิ่มใหม่: ถ้าเกมถูกรีเซ็ตไปแล้ว (ทุกคนออกหมด) ไม่ต้องเริ่มรอบใหม่
        if (!gameStarted) {
            return;
        }

        if (round >= totalRounds) {
            endGame();
            return;
        }

        round++;
        roundEnded = false;

        String[] selectedWord = new WordBank().getRandomWord();

        word = selectedWord[0];
        hint = selectedWord[1];

        broadcast("ROUND#" + round);
        broadcast("HINT#" + hint);
        broadcast("LENGTH#" + word.length());   // เพิ่มใหม่: บอกจำนวนตัวอักษร เพื่อให้ client วาดช่องได้
    }


    public void handleGuess(ClientHandler client, String answer) {
        synchronized (this) {
            if (!gameStarted || roundEnded) {
                return;
            }

            if (client.getPlayer() == null) {
                return;
            }

            if (answer.trim().equalsIgnoreCase(word)) {
                client.getPlayer().addScore(10);
                roundEnded = true;

                broadcast("CORRECT#" + client.getPlayer().getName());
                broadcast("ANSWER#" + word);
                broadcast("SCORE#" + client.getPlayer().getName() + "#" + client.getPlayer().getScore());
            } else {
                client.send("WRONG");
                return;
            }
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        nextRound();
    }


    public synchronized void endGame() {
        gameStarted = false;

        broadcast("GAMEOVER");

        List<Player> list = new ArrayList<>();

        for (ClientHandler client : clients) {
            Player player = client.getPlayer();

            if (player != null) {
                list.add(player);
            }
        }

        // เรียงคะแนนจากมากไปน้อย (Selection Sort ของเดิม)
        for (int i = 0; i < list.size(); i++) {
            int maxIndex = i;

            for (int j = i + 1; j < list.size(); j++) {
                if (list.get(j).getScore() > list.get(maxIndex).getScore()) {
                    maxIndex = j;
                }
            }

            Player temp = list.get(i);
            list.set(i, list.get(maxIndex));
            list.set(maxIndex, temp);
        }

        for (int i = 0; i < list.size(); i++) {
            Player player = list.get(i);

            // แก้ไข: เปลี่ยนจาก SCORE# เป็น RESULT# เพราะ SCORE# มีอยู่แล้วตอนตอบถูก (รูปแบบต่างกัน client จะแยกไม่ออก)
            broadcast("RESULT#" + (i + 1) + "#" + player.getName() + "#" + player.getScore());
        }
    }


    public synchronized void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.send(message);
        }
    }

}
