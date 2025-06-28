import java.awt.*;
import java.util.List;
// Add this import for cover manager
// import DiamondCoverManager;

public class SquareEnemy extends BaseEnemy {
    private BaseEnemy currentCoverDiamond = null;
    private Double fixedCoverAngle = null;
    private Point roamTarget = null;
    private long roamEndTime = 0;
    private boolean scouting = false;

    public SquareEnemy(int x, int y) {
        super(x, y, 100, 2);
        this.attackRange = 200; // Default for SquareEnemy
    }

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
        long now = System.currentTimeMillis();
        // If hiding behind diamond, check if player is in range
        if (bestDiamond != null) {
            // Assign cover if not already assigned
            if (currentCoverDiamond != bestDiamond) {
                if (currentCoverDiamond != null) coverManager.removeCover(currentCoverDiamond);
                coverManager.assignCover(bestDiamond, this);
                currentCoverDiamond = bestDiamond;
            }
            double playerDist = player.getPosition().distance(x, y);
            if (playerDist < attackRange) {
                state = AIState.CHASE;
                // Use cover logic: keep diamond between self and player
                double dx = bestDiamond.getPosition().x - player.x;
                double dy = bestDiamond.getPosition().y - player.y;
                double baseAngle = Math.atan2(dy, dx);
                double angleOffset = Math.toRadians((Math.random() - 0.5) * 60);
                double coverAngle = baseAngle + angleOffset;
                int offsetX = (int)(Math.cos(coverAngle) * 30);
                int offsetY = (int)(Math.sin(coverAngle) * 30);
                moveWithCollision(bestDiamond.getPosition().x + offsetX, bestDiamond.getPosition().y + offsetY, allEnemies);
                fixedCoverAngle = coverAngle;
            } else {
                state = AIState.ROAMING;
                // If diamond is roaming/scouting, follow its target
                Point diamondTarget = null;
                if (bestDiamond instanceof DiamondEnemy) {
                    try {
                        java.lang.reflect.Field f = bestDiamond.getClass().getDeclaredField("roamTarget");
                        f.setAccessible(true);
                        diamondTarget = (Point)f.get(bestDiamond);
                    } catch (Exception ex) { diamondTarget = null; }
                }
                if (diamondTarget != null) {
                    moveWithCollision(diamondTarget.x, diamondTarget.y, allEnemies);
                } else {
                    // fallback: keep diamond between self and player
                    double dx = bestDiamond.getPosition().x - player.x;
                    double dy = bestDiamond.getPosition().y - player.y;
                    double baseAngle = Math.atan2(dy, dx);
                    double angleOffset = Math.toRadians((Math.random() - 0.5) * 60);
                    double coverAngle = baseAngle + angleOffset;
                    int offsetX = (int)(Math.cos(coverAngle) * 30);
                    int offsetY = (int)(Math.sin(coverAngle) * 30);
                    moveWithCollision(bestDiamond.getPosition().x + offsetX, bestDiamond.getPosition().y + offsetY, allEnemies);
                    fixedCoverAngle = coverAngle;
                }
            }
            return;
        } else if (currentCoverDiamond != null) {
            coverManager.removeCover(currentCoverDiamond);
            currentCoverDiamond = null;
            fixedCoverAngle = null;
        }
        // Roaming/scouting logic
        double playerDist = player.getPosition().distance(x, y);
        if (playerDist < attackRange) {
            state = AIState.CHASE;
            roamTarget = null;
            scouting = false;
            // Only move toward player if outside attack range (keep distance)
            if (playerDist > attackRange - 10) { // 10px buffer
                int wiggleX = (int)((Math.random() - 0.5) * 5);
                int wiggleY = (int)((Math.random() - 0.5) * 5);
                // Move to edge of attack range, not directly on player
                double dx = player.x - x;
                double dy = player.y - y;
                double len = Math.sqrt(dx * dx + dy * dy);
                if (len > 0) {
                    dx /= len;
                    dy /= len;
                    int targetX = player.x - (int)(dx * (attackRange - 15));
                    int targetY = player.y - (int)(dy * (attackRange - 15));
                    moveWithCollision(targetX + wiggleX, targetY + wiggleY, allEnemies);
                }
            }
            // else: stay in place and shoot
            return;
        }
        if (state == AIState.CHASE && playerDist >= attackRange) {
            state = AIState.SCOUTING;
            double angle = Math.random() * 2 * Math.PI;
            int scoutX = player.x + (int)(Math.cos(angle) * attackRange);
            int scoutY = player.y + (int)(Math.sin(angle) * attackRange);
            scoutX = Math.max(0, Math.min(scoutX, 2000-20));
            scoutY = Math.max(0, Math.min(scoutY, 1500-20));
            roamTarget = new Point(scoutX, scoutY);
            roamEndTime = now + 5000;
            scouting = true;
        }
        if (state == AIState.SCOUTING && roamTarget != null) {
            moveWithCollision(roamTarget.x, roamTarget.y, allEnemies);
            if (now > roamEndTime || getPosition().distance(roamTarget) < 10) {
                roamTarget = null;
                scouting = false;
                state = AIState.ROAMING;
            }
            return;
        }
        if (state != AIState.ROAMING || roamTarget == null || now > roamEndTime || getPosition().distance(roamTarget) < 10) {
            int roamX = (int)(Math.random() * (2000-20));
            int roamY = (int)(Math.random() * (1500-20));
            roamTarget = new Point(roamX, roamY);
            roamEndTime = now + 5000;
            state = AIState.ROAMING;
        }
        moveWithCollision(roamTarget.x, roamTarget.y, allEnemies);
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
