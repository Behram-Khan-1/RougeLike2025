import java.util.ArrayList;
import java.util.Iterator;
import java.awt.Rectangle;

public class BulletManager {
    private ArrayList<Bullet> bullets;

    public BulletManager() {
        bullets = new ArrayList<>();
    }

    /**
     * Updates all bullets, handles collisions with walls, enemies, and player, and manages coin drops.
     * @param player The player object
     * @param enemies The list of enemies
     * @param walls The list of walls
     * @param coins The list of coins (for coin drops)
     * @param game The game instance (for score updates)
     */
    public void updateBullets(Player player, ArrayList<BaseEnemy> enemies, ArrayList<Wall> walls, ArrayList<Coin> coins, Game game) {
        Iterator<Bullet> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();

            // Update bullet position, but check for wall collision after moving
            if (!bullet.update()) {
                bulletIterator.remove();
                continue;
            }

            // Check collision with walls (block all bullets)
            boolean hitWall = false;
            Rectangle bulletRect = bullet.getBounds();
            for (Wall wall : walls) {
                if (bulletRect.intersects(wall.getBounds())) {
                    hitWall = true;
                    break;
                }
            }
            if (hitWall) {
                bullet.deactivate();
                bulletIterator.remove();
                continue;
            }

            for (BaseEnemy enemy : enemies) {
                if (enemy.isAlive() && bullet.isActive() &&
                        bullet.getType() == BulletType.PLAYER &&
                        bullet.getBounds().intersects(enemy.getBounds())) {
                    int dmg = bullet.getDamage();
                    boolean crit = false;
                    if (Math.random() * 100 < bullet.getCritChance()) {
                        crit = true;
                        dmg = (int)Math.round(dmg * bullet.getCritMultiplier());
                    }
                    enemy.takeDamage(dmg);
                    if (crit) {
                        System.out.println("CRIT! Player bullet dealt " + dmg + " damage (x" + bullet.getCritMultiplier() + ")");
                    }
                    if (!enemy.isAlive()) {
                        game.addScore(10); // Add score for killing enemy
                        // Drop coin(s) on enemy death
                        coins.add(new Coin(enemy.getPosition().x, enemy.getPosition().y, enemy.getCoinValue()));
                    }
                    bullet.deactivate();
                    bulletIterator.remove();
                    break;
                }
                // Add logic for enemy bullets hitting the player
                if (bullet.getType() == BulletType.ENEMY &&
                        bullet.getBounds().intersects(player.getBounds())) {
                    // Play hero hurt sound
                    SoundManager.playSound("assets/sounds/hero hurt.wav");
                    player.takeDamage(bullet.getDamage());
                    bullet.deactivate();
                    bulletIterator.remove();
                    break;
                }
            }
        }
    }

    public ArrayList<Bullet> getBullets() {
        return bullets;
    }

    public void addBullet(Bullet bullet) {
        bullets.add(bullet);
    }

    public void addBullets(java.util.List<Bullet> newBullets) {
        bullets.addAll(newBullets);
    }

    public void removeInactive() {
        bullets.removeIf(b -> !b.update());
    }

    public Iterator<Bullet> iterator() {
        return bullets.iterator();
    }

    public void clear() {
        bullets.clear();
    }
}
