// === Player.java ===
import java.awt.*;

public class Player {
    int x, y, speed = 3;
    int health = 100; // Add health attribute

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update(int mapWidth, int mapHeight) {
        if (InputHandler.up) y = Math.max(0, y - speed);
        if (InputHandler.down) y = Math.min(mapHeight - 20, y + speed);
        if (InputHandler.left) x = Math.max(0, x - speed);
        if (InputHandler.right) x = Math.min(mapWidth - 20, x + speed);
    }

    public void draw(Graphics g, Camera camera) {

        g.setColor(Color.GREEN);
        int screenX = camera.getScreenX(x);
        int screenY = camera.getScreenY(y);
        g.fillRect(screenX, screenY, 20, 20);
        
        // Draw health bar
        g.setColor(Color.RED);
        // int healthBarWidth = (int)((health / 100.0) * 20);

        int barHeight = 5;
        int barPadding = 2; // space between player and health bar
        int healthBarWidth = (int)((health / 100.0) * 20);
        int healthBarY = screenY - barHeight - barPadding;
        
        // Draw health bar above the player
        g.setColor(Color.RED);
        g.fillRect(screenX, healthBarY, healthBarWidth, barHeight);
        
        

    }

    public Point getPosition() {
        return new Point(x, y);
    }
    public Rectangle getBounds() {
        return new Rectangle(x, y, 20, 20);
    }

    public Bullet shootAtMouse(Camera camera) {
        // Get mouse position in screen coordinates
        int mouseScreenX = InputHandler.mousePosition.x;
        int mouseScreenY = InputHandler.mousePosition.y;
        
        // Convert screen coordinates to world coordinates
        double worldMouseX = mouseScreenX + camera.getX();
        double worldMouseY = mouseScreenY + camera.getY();
        
        // Calculate direction from player to world mouse position
        double dx = worldMouseX - x;
        double dy = worldMouseY - y;
        double length = Math.sqrt(dx * dx + dy * dy);
        
        // Normalize the direction vector
        if (length > 0) {
            dx /= length;
            dy /= length;
        }
        
        // Create bullet at player's center position
        return new Bullet(x + 10, y + 10, dx, dy, BulletType.PLAYER);
    }



    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
           System.out.println("Player died");
        }
    }
}