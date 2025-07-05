
import java.awt.*;
import java.util.List;

public abstract class BaseEnemy {
protected int x, y, speed;
protected float health, maxHealth;
protected AIState state = AIState.IDLE;
protected long lastShotTime = 0;
protected static final long SHOOT_COOLDOWN = 1000;
protected int attackRange = 200; // Default, can be overridden per enemy
protected int coinValue = 1; // Default, can be overridden per enemy
protected double damage = 10.0; // Default damage, can be overridden

    public int getAttackRange() {
        return attackRange;
    }

    public void setAttackRange(int range) {
        this.attackRange = range;
    }

    public int getCoinValue() {
        return coinValue;
    }

    public void setCoinValue(int value) {
        this.coinValue = value;
    }

    public int getDamage() {
    return (int)Math.round(damage);
    }

    public void setDamage(int dmg) {
    this.damage = dmg;
}
public void setDamage(double dmg) {
    this.damage = dmg;
    }

    public void setMaxHealth(float hp) {
        this.maxHealth = hp;
        this.health = Math.min(this.health, hp);
    }

    public BaseEnemy(int x, int y, int maxHealth, int speed) {
        this.x = x;
        this.y = y;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.speed = speed;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health < 0)
            health = 0;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 20, 20);
    }

    public Point getPosition() {
        return new Point(x, y);
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    // Set health to maxHealth
    public void setFullHealth() {
        this.health = this.maxHealth;
    }

    // Add this to BaseEnemy.java
    public float getHealth() {
        return health;
    }

    public abstract void update(Player player, List<BaseEnemy> allEnemies);

    public abstract java.util.List<Bullet> shoot(Player player);

    public abstract void draw(Graphics g, Camera camera);

    // Move towards (tx, ty) but avoid overlapping with other enemies
    protected void moveWithCollision(int tx, int ty, List<BaseEnemy> allEnemies) {
        // if (Math.abs(x - tx) < 2 && Math.abs(y - ty) < 2) return;

        int[] dx = { 0, speed, -speed, 0, 0, speed, -speed, speed, -speed };
        int[] dy = { 0, 0, 0, speed, -speed, speed, speed, -speed, -speed };
        int bestDir = -1;
        int minDist = Integer.MAX_VALUE;
        for (int i = 0; i < dx.length; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];
            Rectangle nextBounds = new Rectangle(nx, ny, 20, 20);
            boolean collision = false;
            for (BaseEnemy e : allEnemies) {
                if (e != this && e.isAlive()) {
                    Rectangle other = e.getBounds();
                    if (nextBounds.intersects(other) || nextBounds.getLocation().distance(other.getLocation()) < 18) {
                        collision = true;
                        break;
                    }
                }
            }
            if (!collision) {
                int dist = (nx - tx) * (nx - tx) + (ny - ty) * (ny - ty);
                if (dist < minDist) {
                    minDist = dist;
                    bestDir = i;
                }
            }
        }
        if (bestDir != -1) {
            x += dx[bestDir];
            y += dy[bestDir];
        }
    }

    // Move towards (tx, ty) but avoid overlapping with other enemies and walls
    protected void moveWithCollision(int tx, int ty, List<BaseEnemy> allEnemies, java.util.List<Wall> walls) {
        int[] dx = { 0, speed, -speed, 0, 0, speed, -speed, speed, -speed };
        int[] dy = { 0, 0, 0, speed, -speed, speed, speed, -speed, -speed };
        int bestDir = -1;
        int minDist = Integer.MAX_VALUE;
        for (int i = 0; i < dx.length; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];
            Rectangle nextBounds = new Rectangle(nx, ny, 20, 20);
            boolean collision = false;
            for (BaseEnemy e : allEnemies) {
                if (e != this && e.isAlive()) {
                    Rectangle other = e.getBounds();
                    if (nextBounds.intersects(other) || nextBounds.getLocation().distance(other.getLocation()) < 18) {
                        collision = true;
                        break;
                    }
                }
            }
            // Check wall collision
            if (!collision) {
                for (Wall wall : walls) {
                    if (nextBounds.intersects(wall.getBounds())) {
                        collision = true;
                        break;
                    }
                }
            }
            if (!collision) {
                int dist = (nx - tx) * (nx - tx) + (ny - ty) * (ny - ty);
                if (dist < minDist) {
                    minDist = dist;
                    bestDir = i;
                }
            }
        }
        if (bestDir != -1) {
            x += dx[bestDir];
            y += dy[bestDir];
        }
    }
}
