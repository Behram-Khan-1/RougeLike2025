import java.awt.*;

public class Coin {
    public int x, y;
    public int value;
    private boolean collected = false;
    private static final int SIZE = 14;

    public Coin(int x, int y, int value) {
        this.x = x;
        this.y = y;
        this.value = value;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, SIZE, SIZE);
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        collected = true;
    }

    public void draw(Graphics g, Camera camera) {
        if (collected) return;
        int sx = camera.getScreenX(x);
        int sy = camera.getScreenY(y);
        g.setColor(Color.YELLOW);
        g.fillOval(sx, sy, SIZE, SIZE);
        g.setColor(Color.ORANGE);
        g.drawOval(sx, sy, SIZE, SIZE);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 10));
        g.drawString("$", sx + 4, sy + 11);
    }
}
