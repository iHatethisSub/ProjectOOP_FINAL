public class Player {
    private String name;
    private int score;

    public Player(String name) {
        this.name = name;
        this.score = 0;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int score) {
        this.score += score;
    }

    // เพิ่มใหม่: รีเซ็ตคะแนนเป็น 0 ก่อนเริ่มเกมรอบใหม่
    public void resetScore() {
        this.score = 0;
    }
}
