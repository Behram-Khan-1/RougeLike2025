import java.awt.*;
import java.util.List;

public class HealerEnemy extends BaseEnemy {
    public HealerEnemy(int x, int y) {
        super(x, y, 60, 2);
    }

    @Override
    public void update(Player player, List<BaseEnemy> allEnemies) {
        // Healer moves randomly or stays in place
        int wiggleX = (int)((Math.random() - 0.5) * 5);
        int wiggleY = (int)((Math.random() - 0.5) * 5);
        moveWithCollision(x + wiggleX, y + wiggleY, allEnemies);
        // Heal nearby enemies
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
