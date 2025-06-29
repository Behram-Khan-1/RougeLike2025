import java.awt.*;
import java.util.List;

public class CircleEnemy extends BaseEnemy {
    private Point roamTarget = null;
    private long roamEndTime = 0;
    private boolean scouting = false;

    public CircleEnemy(int x, int y) {
        super(x, y, 60, 2);
        this.attackRange = 300; // Default for CircleEnemy
        setCoinValue(4);
    }

    @Override
    public void update(Player player, List<BaseEnemy> allEnemies) {
        double dist = player.getPosition().distance(x, y);
        long now = System.currentTimeMillis();
        // If player in range, chase
        if (dist < 350) {
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
        if (state == AIState.CHASE && dist >= 350) {
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
        if (dist < 350) {
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
        if (state == AIState.CHASE && dist >= 350) {
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
        double angle = Math.atan2(dy, dx);
        double[] angles = {angle, angle + Math.toRadians(25), angle - Math.toRadians(25)};
        for (double a : angles) {
            double ddx = Math.cos(a);
            double ddy = Math.sin(a);
            Bullet b = new Bullet(x + 10, y + 10, ddx, ddy, BulletType.ENEMY);
            b.setDamage(this.getDamage());
            bullets.add(b);
            System.out.println("CircleEnemy bullet damage: " + this.getDamage());
        }
        return bullets;
    }

    @Override
    public void draw(Graphics g, Camera camera) {
        if (!isAlive()) return;
        int screenX = camera.getScreenX(x);
        int screenY = camera.getScreenY(y);
        g.setColor(Color.MAGENTA);
        g.fillOval(screenX, screenY, 20, 20);
        g.setColor(Color.GREEN);
        int healthBarWidth = (int)((health / (double)maxHealth) * 20);
        g.fillRect(screenX, screenY - 5, healthBarWidth, 3);
    }
}
