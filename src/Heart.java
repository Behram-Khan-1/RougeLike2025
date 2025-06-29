import java.awt.*;

public class Heart {
    public int x, y;
    public static final int SIZE = 16;
    public boolean collected = false;
    public Heart(int x, int y) { this.x = x; this.y = y; }
    public Rectangle getBounds() { return new Rectangle(x, y, SIZE, SIZE); }
    public void draw(Graphics g, Camera camera) {
        int sx = camera.getScreenX(x);
        int sy = camera.getScreenY(y);
        g.setColor(Color.PINK);
        int[] xPoints = {sx + SIZE/2, sx, sx + SIZE};
        int[] yPoints = {sy, sy + SIZE, sy + SIZE};
        g.fillOval(sx, sy, SIZE/2, SIZE/2);
        g.fillOval(sx + SIZE/2, sy, SIZE/2, SIZE/2);
        g.fillPolygon(xPoints, yPoints, 3);
        g.setColor(Color.RED);
        g.drawOval(sx, sy, SIZE/2, SIZE/2);
        g.drawOval(sx + SIZE/2, sy, SIZE/2, SIZE/2);
        g.drawPolygon(xPoints, yPoints, 3);
    }
}
