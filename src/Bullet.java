// === Bullet.java ===
import java.awt.*;

public class Bullet {
    double x, y;
    double dx, dy;
    int speed = 5;
    int damage = 20; // Damage per bullet
    boolean active = true;
    double distanceTraveled = 0;
    double maxRange = 300;

    BulletType type; // 

    public Bullet(double x, double y, double dx, double dy, BulletType type) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.type = type; // 👈 set bullet type
    }

    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, 5, 5);
    }
    public BulletType getType() {
        return type;
    }

    public boolean update() {
        if (!active) return false;
        
        x += dx * speed;
        y += dy * speed;
        
        distanceTraveled += speed;
        return distanceTraveled < maxRange;
    }

    public void draw(Graphics g, Camera camera) {
        if (!active) return;
        
        int screenX = camera.getScreenX((int)x);
        int screenY = camera.getScreenY((int)y);
        
        g.setColor(Color.YELLOW);
        g.fillOval(screenX, screenY, 5, 5);
    }

    public void deactivate() {
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    public int getDamage() {
        return damage;
    }

    public boolean checkCollision(Player player) {
        if (active && getBounds().intersects(new Rectangle(player.x, player.y, 20, 20))) {
            player.takeDamage(damage);
            deactivate();
            return true;
        }
        return false;
    }
}