// === Enemy.java ===
import java.awt.*;

public class Enemy {
    int x, y, speed = 2;
    int health = 100; // Starting health
    AIState state = AIState.IDLE;
    
    private long lastShotTime = 0;
    private static final long SHOOT_COOLDOWN = 1000; // 1 second cooldown

    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void takeDamage(int damage) {
        health -= damage;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 20, 20);
    }

    public Bullet shoot(Player player) {
        if (!isAlive()) return null;

        if(state != AIState.CHASE) return null;
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime < SHOOT_COOLDOWN) {
            return null;
        }
        
        // Calculate direction to player
        double dx = player.x - x;
        double dy = player.y - y;
        double length = Math.sqrt(dx * dx + dy * dy);
        
        // Normalize the direction vector
        if (length > 0) {
            dx /= length;
            dy /= length;
        }
        
        lastShotTime = currentTime;
        return new Bullet(x + 10, y + 10, dx, dy, BulletType.ENEMY);
    }

    public void update(Player player) {
        if (!isAlive()) return;
        
        double dist = player.getPosition().distance(x, y);

        if (dist < 200) {
            state = AIState.CHASE;
        } else {
            state = AIState.IDLE;
        }

        if (state == AIState.CHASE) {

            if(dist < 100) {
                return;
            }
            if (player.x > x) x += speed;
            if (player.x < x) x -= speed;
            if (player.y > y) y += speed;
            if (player.y < y) y -= speed;
            
        }
    }

    public void draw(Graphics g, Camera camera) {
        if (!isAlive()) return;
        
        int screenX = camera.getScreenX(x);
        int screenY = camera.getScreenY(y);
        
        g.setColor(Color.RED);
        g.fillRect(screenX, screenY, 20, 20);
        
        // Draw health bar
        g.setColor(Color.GREEN);
        int healthBarWidth = (int)((health / 100.0) * 20);
        g.fillRect(screenX, screenY - 5, healthBarWidth, 3);
    }

    public Point getPosition() {
        return new Point(x, y);
    }
}