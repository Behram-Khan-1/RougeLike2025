import java.awt.*;

public class Wall {
    public int x, y, width, height;


    public Wall(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public void draw(Graphics g, Camera camera) {
        int sx = camera.getScreenX(x);
        int sy = camera.getScreenY(y);
        g.setColor(Color.RED);
        g.fillRect(sx, sy, width, height);
        g.setColor(Color.DARK_GRAY);
        g.drawRect(sx, sy, width, height);
    }
}
