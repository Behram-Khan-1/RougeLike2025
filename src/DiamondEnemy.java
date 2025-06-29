import java.awt.*;
import java.util.List;

public class DiamondEnemy extends BaseEnemy {
    private Point roamTarget = null;
    private long roamEndTime = 0;
    private boolean scouting = false;

    public DiamondEnemy(int x, int y) {
        super(x, y, 200, 1);
        this.attackRange = 180; // Default for DiamondEnemy
        setCoinValue(5);
    }

    @Override
    public void update(Player player, List<BaseEnemy> allEnemies) {
        double dist = player.getPosition().distance(x, y);
        long now = System.currentTimeMillis();
        // If player in range, chase
        if (dist < 200) {
            state = AIState.CHASE;
            roamTarget = null;
            scouting = false;
            if (dist >= 100) {
                int wiggleX = (int)((Math.random() - 0.5) * 5);
                int wiggleY = (int)((Math.random() - 0.5) * 5);
                moveWithCollision(player.x + wiggleX, player.y + wiggleY, allEnemies);
            }
            return;
        }
        // If just lost player, do a scouting move near player
        if (state == AIState.CHASE && dist >= 200) {
            state = AIState.SCOUTING;
            double angle = Math.random() * 2 * Math.PI;
            int scoutX = player.x + (int)(Math.cos(angle) * 200);
            int scoutY = player.y + (int)(Math.sin(angle) * 200);
            scoutX = Math.max(0, Math.min(scoutX, 2000-20));
            scoutY = Math.max(0, Math.min(scoutY, 1500-20));
            roamTarget = new Point(scoutX, scoutY);
            roamEndTime = now + 5000;
            scouting = true;
        }
        // Roaming or scouting
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
            // Pick new roam target
            int roamX = (int)(Math.random() * (2000-20));
            int roamY = (int)(Math.random() * (1500-20));
            roamTarget = new Point(roamX, roamY);
            roamEndTime = now + 5000;
            state = AIState.ROAMING;
        }
        // Move toward roam target
        moveWithCollision(roamTarget.x, roamTarget.y, allEnemies);
    }

    // Overload for wall-aware navigation
    public void update(Player player, List<BaseEnemy> allEnemies, java.util.List<Wall> walls) {
        double dist = player.getPosition().distance(x, y);
        long now = System.currentTimeMillis();
        // If player in range, chase
        if (dist < 200) {
            state = AIState.CHASE;
            roamTarget = null;
            scouting = false;
            if (dist >= 100) {
                int wiggleX = (int)((Math.random() - 0.5) * 5);
                int wiggleY = (int)((Math.random() - 0.5) * 5);
                moveWithCollision(player.x + wiggleX, player.y + wiggleY, allEnemies, walls);
            }
            return;
        }
        // If just lost player, do a scouting move near player
        if (state == AIState.CHASE && dist >= 200) {
            state = AIState.SCOUTING;
            double angle = Math.random() * 2 * Math.PI;
            int scoutX = player.x + (int)(Math.cos(angle) * 200);
            int scoutY = player.y + (int)(Math.sin(angle) * 200);
            scoutX = Math.max(0, Math.min(scoutX, 2000-20));
            scoutY = Math.max(0, Math.min(scoutY, 1500-20));
            roamTarget = new Point(scoutX, scoutY);
            roamEndTime = now + 5000;
            scouting = true;
        }
        // Roaming or scouting
        if (state == AIState.SCOUTING && roamTarget != null) {
            moveWithCollision(roamTarget.x, roamTarget.y, allEnemies, walls);
            if (now > roamEndTime || getPosition().distance(roamTarget) < 10) {
                roamTarget = null;
                scouting = false;
                state = AIState.ROAMING;
            }
            return;
        }
        if (state != AIState.ROAMING || roamTarget == null || now > roamEndTime || getPosition().distance(roamTarget) < 10) {
            // Pick new roam target
            int roamX = (int)(Math.random() * (2000-20));
            int roamY = (int)(Math.random() * (1500-20));
            roamTarget = new Point(roamX, roamY);
            roamEndTime = now + 5000;
            state = AIState.ROAMING;
        }
        // Move toward roam target
        moveWithCollision(roamTarget.x, roamTarget.y, allEnemies, walls);
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
        Bullet b = new Bullet(x + 10, y + 10, dx, dy, BulletType.ENEMY);
        b.setDamage(this.getDamage());
        bullets.add(b);
        System.out.println("DiamondEnemy bullet damage: " + this.getDamage());
        return bullets;
    }

    @Override
    public void draw(Graphics g, Camera camera) {
        if (!isAlive()) return;
        int screenX = camera.getScreenX(x);
        int screenY = camera.getScreenY(y);
        g.setColor(Color.BLUE);
        int[] dx = {10, 20, 10, 0};
        int[] dy = {0, 10, 20, 10};
        for (int i = 0; i < 4; i++) {
            dx[i] += screenX - 10;
            dy[i] += screenY - 10;
        }
        g.fillPolygon(dx, dy, 4);
        g.setColor(Color.GREEN);
        int healthBarWidth = (int)((health / (double)maxHealth) * 20);
        g.fillRect(screenX, screenY - 5, healthBarWidth, 3);
    }
}
