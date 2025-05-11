// === Camera.java ===
import java.awt.Point;

public class Camera {
    private float x, y;
    private float targetX, targetY;
    private float smoothness = 0.1f;
    private int viewportWidth, viewportHeight;
    private int mapWidth, mapHeight;

    public Camera(int viewportWidth, int viewportHeight, int mapWidth, int mapHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }

    public void update(int targetX, int targetY) {
        // Calculate target position to center the player
        this.targetX = targetX - viewportWidth / 2.0f;
        this.targetY = targetY - viewportHeight / 2.0f;

        // Smooth movement using linear interpolation
        x += (this.targetX - x) * smoothness;
        y += (this.targetY - y) * smoothness;

        // Clamp camera position to map boundaries
        x = Math.max(0, Math.min(x, mapWidth - viewportWidth));
        y = Math.max(0, Math.min(y, mapHeight - viewportHeight));
    }

    public int getScreenX(int worldX) {
        return (int)(worldX - x);
    }

    public int getScreenY(int worldY) {
        return (int)(worldY - y);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}