import java.awt.*;
import java.util.List;
// Add this import for cover manager
// import DiamondCoverManager;

public class SquareEnemy extends BaseEnemy {
    public SquareEnemy(int x, int y) {
        super(x, y, 100, 2);
    }


    private BaseEnemy currentCoverDiamond = null;
    private Double fixedCoverAngle = null;

    // Main update method with cover manager
    public void update(Player player, List<BaseEnemy> allEnemies, DiamondCoverManager coverManager) {
        double dist = player.getPosition().distance(x, y);
        boolean lowHP = health < 0.3 * maxHealth;
        BaseEnemy nearestHealer = null;
        double minHealerDist = Double.MAX_VALUE;
        for (BaseEnemy e : allEnemies) {
            if (e != this && e instanceof HealerEnemy && e.isAlive()) {
                double d = getPosition().distance(e.getPosition());
                if (d < minHealerDist) {
                    minHealerDist = d;
                    nearestHealer = e;
                }
            }
        }
        if (lowHP && nearestHealer != null) {
            // FLEE: Move toward healer
            double dx = nearestHealer.getPosition().x - x;
            double dy = nearestHealer.getPosition().y - y;
            double len = Math.sqrt(dx * dx + dy * dy);
            int tx = x, ty = y;
            if (len > 1) {
                dx /= len;
                dy /= len;
                tx = x + (int)(dx * speed);
                ty = y + (int)(dy * speed);
            }
            moveWithCollision(tx, ty, allEnemies);
            // Remove cover if fleeing
            if (currentCoverDiamond != null && coverManager != null) {
                coverManager.removeCover(currentCoverDiamond);
                currentCoverDiamond = null;
                fixedCoverAngle = null;
            }
            return;
        }
        // Always try to hide behind a diamond if possible
        BaseEnemy bestDiamond = null;
        double minCoverDist = Double.MAX_VALUE;
        for (BaseEnemy e : allEnemies) {
            if (e != this && e instanceof DiamondEnemy && e.isAlive() && coverManager.isDiamondAvailable(e)) {
                double d = getPosition().distance(e.getPosition());
                if (d < 80 && d < minCoverDist) {
                    minCoverDist = d;
                    bestDiamond = e;
                }
            }
        }
        if (bestDiamond != null) {
            // Assign cover if not already assigned
            if (currentCoverDiamond != bestDiamond) {
                if (currentCoverDiamond != null) coverManager.removeCover(currentCoverDiamond);
                coverManager.assignCover(bestDiamond, this);
                currentCoverDiamond = bestDiamond;
            }
            // Always update cover angle to keep diamond between player and self
            double dx = bestDiamond.getPosition().x - player.x;
            double dy = bestDiamond.getPosition().y - player.y;
            double baseAngle = Math.atan2(dy, dx);
            // Add a small random offset to avoid stacking
            double angleOffset = Math.toRadians((Math.random() - 0.5) * 60);
            double coverAngle = baseAngle + angleOffset;
            int offsetX = (int)(Math.cos(coverAngle) * 30);
            int offsetY = (int)(Math.sin(coverAngle) * 30);
            moveWithCollision(bestDiamond.getPosition().x + offsetX, bestDiamond.getPosition().y + offsetY, allEnemies);
            fixedCoverAngle = coverAngle;
            return;
        } else if (currentCoverDiamond != null) {
            coverManager.removeCover(currentCoverDiamond);
            currentCoverDiamond = null;
            fixedCoverAngle = null;
        }
        // Only chase player if within range, otherwise idle
        double playerDist = player.getPosition().distance(x, y);
        if (playerDist < 200) {
            state = AIState.CHASE;
            // Only move toward player if not too close
            if (playerDist > 200) {
                int wiggleX = (int)((Math.random() - 0.5) * 5);
                int wiggleY = (int)((Math.random() - 0.5) * 5);
                moveWithCollision(player.x + wiggleX, player.y + wiggleY, allEnemies);
            }
            // else: stay in place and shoot
        } else {
            state = AIState.IDLE;
        }
    }

    // Implement the required abstract method for compatibility, but delegate to the main update
    @Override
    public void update(Player player, List<BaseEnemy> allEnemies) {
        // If you call this version, it will not use cover logic
        update(player, allEnemies, null);
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
        // System.out.println("SquareEnemy health: " + health);
    }

}
