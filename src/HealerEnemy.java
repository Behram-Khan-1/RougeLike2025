import java.awt.*;
import java.util.List;

public class HealerEnemy extends BaseEnemy {
    public HealerEnemy(int x, int y) {
        super(x, y, 120, 2);
        setCoinValue(3);
    }

    @Override
    public void update(Player player, List<BaseEnemy> allEnemies) {
        BaseEnemy target = getLowestHealthAlly(allEnemies);
        if (target != null) {
            double dist = getPosition().distance(target.getPosition());
            // If close enough, heal
            if (dist < 100) {
                if (target.health < target.maxHealth) {
                    // Heal at 2 HP per second (assuming 60 FPS)
                    target.health += 3.0/60.0 ;
                    System.out.println(1.0/60.0);
                    
                    if (target.health > target.maxHealth) {
                        target.health = target.maxHealth;
                    }
                }
            } else {
                // Move closer to the ally
                moveWithCollision(target.x, target.y, allEnemies);
            }
        }
    }

    // Overload for wall-aware navigation
    public void update(Player player, List<BaseEnemy> allEnemies, java.util.List<Wall> walls) {
        BaseEnemy target = getLowestHealthAlly(allEnemies);
        if (target != null) {
            double dist = getPosition().distance(target.getPosition());
            // If close enough, heal
            if (dist < 100) {
                if (target.health < target.maxHealth) {
                    // Heal at 2 HP per second (assuming 60 FPS)
                    target.health += 3.0/60.0 ;
                    System.out.println(1.0/60.0);
                    if (target.health > target.maxHealth) {
                        target.health = target.maxHealth;
                    }
                }
            } else {
                // Move closer to the ally
                moveWithCollision(target.x, target.y, allEnemies, walls);
            }
        }
    }

    private BaseEnemy getLowestHealthAlly(List<BaseEnemy> allEnemies) {
        BaseEnemy lowest = null;
        double minRatio = 1.0;
        for (BaseEnemy e : allEnemies) {
            if (e == this || !e.isAlive() || e.health >= e.maxHealth)
                continue;
            double ratio = e.health / (double) e.maxHealth;
            if (ratio < minRatio) {
                minRatio = ratio;
                lowest = e;
            }
        }
        return lowest;
    }

    @Override
    public java.util.List<Bullet> shoot(Player player) {
        return null; // Healer does not shoot
    }

    @Override
    public void draw(Graphics g, Camera camera) {
        if (!isAlive())
            return;
        int screenX = camera.getScreenX(x);
        int screenY = camera.getScreenY(y);
        g.setColor(Color.GREEN);
        g.fillRect(screenX, screenY, 20, 20);
        g.setColor(Color.WHITE);
        g.drawString("+", screenX + 5, screenY + 15);
        g.setColor(Color.GREEN);
        int healthBarWidth = (int) ((health / (double) maxHealth) * 20);
        g.fillRect(screenX, screenY - 5, healthBarWidth, 3);
    }
}
