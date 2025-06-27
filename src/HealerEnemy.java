import java.awt.*;
import java.util.List;

public class HealerEnemy extends BaseEnemy {
    public HealerEnemy(int x, int y) {
        super(x, y, 60, 2);
    }

    @Override
    public void update(Player player, List<BaseEnemy> allEnemies) {
        // 1. Find the nearest low-health ally (not self, below 80% health, within 300px)
        BaseEnemy targetAlly = null;
        double minDist = Double.MAX_VALUE;
        for (BaseEnemy e : allEnemies) {
            if (e != this && e.isAlive() && e.health < e.maxHealth * 0.8) {
                double d = getPosition().distance(e.getPosition());
                if (d < 300 && d < minDist) {
                    minDist = d;
                    targetAlly = e;
                }
            }
        }

        int tx = x, ty = y;
        if (targetAlly != null) {
            // Move toward the low-health ally
            double dx = targetAlly.getPosition().x - x;
            double dy = targetAlly.getPosition().y - y;
            double len = Math.sqrt(dx * dx + dy * dy);
            if (len > 1) {
                dx /= len;
                dy /= len;
                tx = x + (int)(dx * speed);
                ty = y + (int)(dy * speed);
            }
        } else {
            // Patrol: move randomly, but repel from other healers (stronger patrol)
            double repelX = (Math.random() - 0.5) * 2.0;
            double repelY = (Math.random() - 0.5) * 2.0;
            for (BaseEnemy e : allEnemies) {
                if (e != this && e instanceof HealerEnemy && e.isAlive()) {
                    double d = getPosition().distance(e.getPosition());
                    if (d < 50 && d > 0) {
                        repelX += (x - e.getPosition().x) / d;
                        repelY += (y - e.getPosition().y) / d;
                    }
                }
            }
            double len = Math.sqrt(repelX * repelX + repelY * repelY);
            if (len > 0.1) {
                repelX /= len;
                repelY /= len;
                tx = x + (int)(repelX * speed);
                ty = y + (int)(repelY * speed);
            }
        }
        moveWithCollision(tx, ty, allEnemies);

        // Heal nearby allies
        for (BaseEnemy e : allEnemies) {
            if (e != this && e.isAlive() && e.health < e.maxHealth && getPosition().distance(e.getPosition()) < 80) {
                e.health += 2.0 / 60.0; // 2 hp per second (assuming 60 FPS)
                if (e.health > e.maxHealth) e.health = e.maxHealth;
            }
        }
    }

    @Override
    public java.util.List<Bullet> shoot(Player player) {
        return null; // Healer does not shoot
    }

    @Override
    public void draw(Graphics g, Camera camera) {
        if (!isAlive()) return;
        int screenX = camera.getScreenX(x);
        int screenY = camera.getScreenY(y);
        g.setColor(Color.GREEN);
        g.fillRect(screenX, screenY, 20, 20);
        g.setColor(Color.WHITE);
        g.drawString("+", screenX + 5, screenY + 15);
        g.setColor(Color.GREEN);
        int healthBarWidth = (int)((health / (double)maxHealth) * 20);
        g.fillRect(screenX, screenY - 5, healthBarWidth, 3);
    }
}
