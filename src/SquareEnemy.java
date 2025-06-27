import java.awt.*;
import java.util.List;

public class SquareEnemy extends BaseEnemy {
    public SquareEnemy(int x, int y) {
        super(x, y, 100, 2);
    }

    @Override
    public void update(Player player, List<BaseEnemy> allEnemies) {
        double dist = player.getPosition().distance(x, y);
        if (dist < 200) {
            state = AIState.CHASE;
        } else {
            state = AIState.IDLE;
        }
        if (state == AIState.CHASE && dist >= 100) {
            int wiggleX = (int)((Math.random() - 0.5) * 5);
            int wiggleY = (int)((Math.random() - 0.5) * 5);
            moveWithCollision(player.x + wiggleX, player.y + wiggleY, allEnemies);
        }
    }

    @Override
    public java.util.List<Bullet> shoot(Player player) {
        if (!isAlive() || state != AIState.CHASE) return null;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime < SHOOT_COOLDOWN) return null;
        lastShotTime = currentTime;
        java.util.ArrayList<Bullet> bullets = new java.util.ArrayList<>();
        double dx = player.x - x;
        double dy = player.y - y;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length > 0) {
            dx /= length;
            dy /= length;
        }
        bullets.add(new Bullet(x + 10, y + 10, dx, dy, BulletType.ENEMY));
        return bullets;
    }

    @Override
    public void draw(Graphics g, Camera camera) {
        if (!isAlive()) return;
        int screenX = camera.getScreenX(x);
        int screenY = camera.getScreenY(y);
        g.setColor(Color.RED);
        g.fillRect(screenX, screenY, 20, 20);
        g.setColor(Color.GREEN);
        int healthBarWidth = (int)((health / (double)maxHealth) * 20);
        g.fillRect(screenX, screenY - 5, healthBarWidth, 3);
    }
}
