// === Enemy.java ===
import java.awt.*;
import java.util.List;

public class Enemy {
    // ...existing code...
    public enum EnemyType {
        SQUARE,
        DIAMOND,
        CIRCLE,
        HEALER
    }
    int x, y, speed = 2;
    int health = 100; // Starting health
    int maxHealth = 100;
    AIState state = AIState.IDLE;
    EnemyType type;
    private Enemy healerTarget = null;
    
    private long lastShotTime = 0;
    private static final long SHOOT_COOLDOWN = 1000; // 1 second cooldown

    public Enemy(int x, int y) {
        this(x, y, EnemyType.SQUARE);
    }

    public Enemy(int x, int y, EnemyType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        switch (type) {
            case SQUARE:
                this.maxHealth = 100;
                this.health = 100;
                this.speed = 2;
                break;
            case DIAMOND:
                this.maxHealth = 200;
                this.health = 200;
                this.speed = 1;
                break;
            case CIRCLE:
                this.maxHealth = 80;
                this.health = 80;
                this.speed = 3;
                break;
            case HEALER:
                this.maxHealth = 60;
                this.health = 60;
                this.speed = 2;
                break;
        }
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health < 0) health = 0;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 20, 20);
    }

    public List<Bullet> shoot(Player player) {
        if (!isAlive()) return null;
        if (type == EnemyType.HEALER) return null;
        if (state != AIState.CHASE) return null;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime < SHOOT_COOLDOWN) {
            return null;
        }
        lastShotTime = currentTime;
        java.util.ArrayList<Bullet> bullets = new java.util.ArrayList<>();
        double dx = player.x - x;
        double dy = player.y - y;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length > 0) {
            dx /= length;
            dy /= length;
        }
        // SQUARE, DIAMOND: single bullet
        if (type == EnemyType.SQUARE || type == EnemyType.DIAMOND) {
            bullets.add(new Bullet(x + 10, y + 10, dx, dy, BulletType.ENEMY));
        } else if (type == EnemyType.CIRCLE) {
            // CIRCLE: 3 bullets, 1 at player, 2 at ±25 degrees
            double angle = Math.atan2(dy, dx);
            double[] angles = {angle, angle + Math.toRadians(25), angle - Math.toRadians(25)};
            for (double a : angles) {
                double ddx = Math.cos(a);
                double ddy = Math.sin(a);
                bullets.add(new Bullet(x + 10, y + 10, ddx, ddy, BulletType.ENEMY));
            }
        }
        return bullets;
    }

    public void update(Player player, List<Enemy> allEnemies) {
        if (!isAlive()) return;
        double dist = player.getPosition().distance(x, y);
        // FLEE if low HP and healer exists
        boolean lowHP = health < 0.3 * maxHealth;
        Enemy nearestHealer = null;
        double minHealerDist = Double.MAX_VALUE;
        for (Enemy e : allEnemies) {
            if (e != this && e.type == EnemyType.HEALER && e.isAlive()) {
                double d = getPosition().distance(e.getPosition());
                if (d < minHealerDist) {
                    minHealerDist = d;
                    nearestHealer = e;
                }
            }
        }
        if (lowHP && nearestHealer != null) {
            state = AIState.FLEE;
            healerTarget = nearestHealer;
        } else if (dist < 200) {
            state = AIState.CHASE;
            healerTarget = null;
        } else {
            state = AIState.IDLE;
            healerTarget = null;
        }

        if (state == AIState.FLEE && healerTarget != null) {
            // Move towards healer with wiggle
            int tx = healerTarget.x;
            int ty = healerTarget.y;
            int wiggleX = (int)((Math.random() - 0.5) * 5); // -2 to +2
            int wiggleY = (int)((Math.random() - 0.5) * 5);
            moveWithCollision(tx + wiggleX, ty + wiggleY, allEnemies);
        } else if (state == AIState.CHASE) {
            if (dist < 100) {
                return;
            }
            // If diamond nearby, use as cover
            if (type != EnemyType.DIAMOND) {
                Enemy cover = null;
                double minCoverDist = Double.MAX_VALUE;
                for (Enemy e : allEnemies) {
                    if (e != this && e.type == EnemyType.DIAMOND && e.isAlive()) {
                        double d = getPosition().distance(e.getPosition());
                        if (d < 60 && d < minCoverDist) {
                            minCoverDist = d;
                            cover = e;
                        }
                    }
                }
                if (cover != null) {
                    // Move behind diamond (relative to player) with random offset
                    double dx = cover.x - player.x;
                    double dy = cover.y - player.y;
                    double baseAngle = Math.atan2(dy, dx);
                    double angleOffset = Math.toRadians((Math.random() - 0.5) * 60); // -30 to +30 deg
                    double angle = baseAngle + angleOffset;
                    int offsetX = (int)(Math.cos(angle) * 30);
                    int offsetY = (int)(Math.sin(angle) * 30);
                    moveWithCollision(cover.x + offsetX, cover.y + offsetY, allEnemies);
                    return;
                }
            }
            // Move towards player with wiggle
            int wiggleX = (int)((Math.random() - 0.5) * 5);
            int wiggleY = (int)((Math.random() - 0.5) * 5);
            moveWithCollision(player.x + wiggleX, player.y + wiggleY, allEnemies);
        }
    }

    // Move towards (tx, ty) but avoid overlapping with other enemies
    private void moveWithCollision(int tx, int ty, java.util.List<Enemy> allEnemies) {
        // Try direct and alternate moves
        int[] dx = {0, speed, -speed, 0, 0, speed, -speed, speed, -speed};
        int[] dy = {0, 0, 0, speed, -speed, speed, speed, -speed, -speed};
        // 0: direct, 1: right, 2: left, 3: down, 4: up, 5: down-right, 6: down-left, 7: up-right, 8: up-left
        int bestDir = -1;
        int minDist = Integer.MAX_VALUE;
        for (int i = 0; i < dx.length; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];
            Rectangle nextBounds = new Rectangle(nx, ny, 20, 20);
            boolean collision = false;
            for (Enemy e : allEnemies) {
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
    
        // Healer heals nearby enemies
        if (type == EnemyType.HEALER && isAlive()) {
            for (Enemy e : allEnemies) {
                if (e != this && e.isAlive() && e.health < e.maxHealth && getPosition().distance(e.getPosition()) < 80) {
                    e.health += 2.0 / 60.0; // 2 hp per second (assuming 60 FPS)
                    if (e.health > e.maxHealth) e.health = e.maxHealth;
                }
            }
        }
    }

    public void draw(Graphics g, Camera camera) {
        if (!isAlive()) return;
        int screenX = camera.getScreenX(x);
        int screenY = camera.getScreenY(y);
        switch (type) {
            case SQUARE:
                g.setColor(Color.RED);
                g.fillRect(screenX, screenY, 20, 20);
                break;
            case DIAMOND:
                g.setColor(Color.BLUE);
                int[] dx = {10, 20, 10, 0};
                int[] dy = {0, 10, 20, 10};
                for (int i = 0; i < 4; i++) {
                    dx[i] += screenX - 10;
                    dy[i] += screenY - 10;
                }
                g.fillPolygon(dx, dy, 4);
                break;
            case CIRCLE:
                g.setColor(Color.MAGENTA);
                g.fillOval(screenX, screenY, 20, 20);
                break;
            case HEALER:
                g.setColor(Color.GREEN);
                g.fillRect(screenX, screenY, 20, 20);
                g.setColor(Color.WHITE);
                g.drawString("+", screenX + 5, screenY + 15);
                break;
        }
        // Draw health bar
        g.setColor(Color.GREEN);
        int healthBarWidth = (int)((health / (double)maxHealth) * 20);
        g.fillRect(screenX, screenY - 5, healthBarWidth, 3);
    }

    public Point getPosition() {
        return new Point(x, y);
    }
    public EnemyType getType() {
        return type;
    }
    public int getMaxHealth() {
        return maxHealth;
    }
    public void setHealerTarget(Enemy healer) {
        this.healerTarget = healer;
    }
}