import java.util.ArrayList;
import java.util.Iterator;

public class EnemyManager {
    private ArrayList<BaseEnemy> enemies;
    private int frameCount = 0;
    private int squareTimer = 0, diamondTimer = 0, circleTimer = 0, healerTimer = 0;
    private Game game;
    private int lastScaleStep = 0;

    public EnemyManager(Game game) {
        this.game = game;
        this.enemies = new ArrayList<>();
    }

    public ArrayList<BaseEnemy> getEnemies() {
        return enemies;
    }

    public void reset() {
        enemies.clear();
        frameCount = 0;
        squareTimer = 0;
        diamondTimer = 0;
        circleTimer = 0;
        healerTimer = 0;
        lastScaleStep = 0;
    }
    public void updateEnemies(Player player, DiamondCoverManager coverManager, ArrayList<Wall> walls, ArrayList<Bullet> bullets) {
        coverManager.clear();
        for (BaseEnemy enemy : enemies) {
            if (!enemy.isAlive()) continue;
            if (enemy instanceof SquareEnemy) {
                ((SquareEnemy) enemy).update(player, enemies, coverManager, walls);
            } else if (enemy instanceof DiamondEnemy) {
                ((DiamondEnemy) enemy).update(player, enemies, walls);
            } else if (enemy instanceof CircleEnemy) {
                ((CircleEnemy) enemy).update(player, enemies, walls);
            } else if (enemy instanceof HealerEnemy) {
                ((HealerEnemy) enemy).update(player, enemies, walls);
            } else {
                enemy.update(player, enemies);
            }
            java.util.List<Bullet> enemyBullets = enemy.shoot(player);
            if (enemyBullets != null) {
                bullets.addAll(enemyBullets);
            }
        }
    }

    public void addEnemy(BaseEnemy enemy) {
        enemies.add(enemy);
    }

    public void update() {
        frameCount++;
        int seconds = frameCount / 60;
        int scaleSteps = seconds / game.ENEMY_SCALE_INTERVAL;
        if (scaleSteps > lastScaleStep) {
            game.SQUARE_BASE_HP = (int)(game.SQUARE_BASE_HP * (1.0 + game.ENEMY_HP_PER_INTERVAL));
            game.SQUARE_BASE_DMG = game.SQUARE_BASE_DMG * (1.0 + game.ENEMY_DMG_PER_INTERVAL);
            game.DIAMOND_BASE_HP = (int)(game.DIAMOND_BASE_HP * (1.0 + game.ENEMY_HP_PER_INTERVAL));
            game.DIAMOND_BASE_DMG = game.DIAMOND_BASE_DMG * (1.0 + game.ENEMY_DMG_PER_INTERVAL);
            game.CIRCLE_BASE_HP = (int)(game.CIRCLE_BASE_HP * (1.0 + game.ENEMY_HP_PER_INTERVAL));
            game.CIRCLE_BASE_DMG = game.CIRCLE_BASE_DMG * (1.0 + game.ENEMY_DMG_PER_INTERVAL);
            game.HEALER_BASE_HP = (int)(game.HEALER_BASE_HP * (1.0 + game.ENEMY_HP_PER_INTERVAL));
            game.HEALER_BASE_DMG = game.HEALER_BASE_DMG * (1.0 + game.ENEMY_DMG_PER_INTERVAL);
            lastScaleStep = scaleSteps;
        }
        // SQUARE
        if (seconds >= Game.SQUARE_START) {
            squareTimer++;
            if (squareTimer >= Game.SQUARE_SPAWN_RATE) {
                int attempts = 0;
                boolean spawned = false;
                while (attempts < 20 && !spawned) {
                    int ex = (int) (Math.random() * Game.MAP_WIDTH);
                    int ey = (int) (Math.random() * Game.MAP_HEIGHT);
                    java.awt.Rectangle enemyRect = new java.awt.Rectangle(ex, ey, 28, 28); // Assuming enemy size 28x28
                    boolean collides = false;
                    for (Wall wall : game.getWallManager().getWalls()) {
                        if (wall.getBounds().intersects(enemyRect)) { collides = true; break; }
                    }
                    if (!collides) {
                        SquareEnemy e = new SquareEnemy(ex, ey);
                        e.setMaxHealth(game.SQUARE_BASE_HP);
                        e.setFullHealth();
                        e.setDamage(game.SQUARE_BASE_DMG);
                        enemies.add(e);
                        spawned = true;
                    }
                    attempts++;
                }
                squareTimer = 0;
            }
        }
        // DIAMOND
        if (seconds >= Game.DIAMOND_START) {
            diamondTimer++;
            int rate = (seconds >= Game.DIAMOND_LOW_RATE_START) ? Game.DIAMOND_SPAWN_RATE : 600;
            if (diamondTimer >= rate) {
                int attempts = 0;
                boolean spawned = false;
                while (attempts < 20 && !spawned) {
                    int ex = (int) (Math.random() * Game.MAP_WIDTH);
                    int ey = (int) (Math.random() * Game.MAP_HEIGHT);
                    java.awt.Rectangle enemyRect = new java.awt.Rectangle(ex, ey, 28, 28);
                    boolean collides = false;
                    for (Wall wall : game.getWallManager().getWalls()) {
                        if (wall.getBounds().intersects(enemyRect)) { collides = true; break; }
                    }
                    if (!collides) {
                        DiamondEnemy e = new DiamondEnemy(ex, ey);
                        e.setMaxHealth(game.DIAMOND_BASE_HP);
                        e.setFullHealth();
                        e.setDamage(game.DIAMOND_BASE_DMG);
                        enemies.add(e);
                        spawned = true;
                    }
                    attempts++;
                }
                diamondTimer = 0;
            }
        }
        // CIRCLE
        if (seconds >= Game.CIRCLE_START) {
            circleTimer++;
            if (circleTimer >= Game.CIRCLE_SPAWN_RATE) {
                int attempts = 0;
                boolean spawned = false;
                while (attempts < 20 && !spawned) {
                    int ex = (int) (Math.random() * Game.MAP_WIDTH);
                    int ey = (int) (Math.random() * Game.MAP_HEIGHT);
                    java.awt.Rectangle enemyRect = new java.awt.Rectangle(ex, ey, 28, 28);
                    boolean collides = false;
                    for (Wall wall : game.getWallManager().getWalls()) {
                        if (wall.getBounds().intersects(enemyRect)) { collides = true; break; }
                    }
                    if (!collides) {
                        CircleEnemy e = new CircleEnemy(ex, ey);
                        e.setMaxHealth(game.CIRCLE_BASE_HP);
                        e.setFullHealth();
                        e.setDamage(game.CIRCLE_BASE_DMG);
                        enemies.add(e);
                        spawned = true;
                    }
                    attempts++;
                }
                circleTimer = 0;
            }
        }
        // HEALER
        if (seconds >= Game.HEALER_START) {
            healerTimer++;
            if (healerTimer >= Game.HEALER_SPAWN_RATE) {
                int attempts = 0;
                boolean spawned = false;
                while (attempts < 20 && !spawned) {
                    int ex = (int) (Math.random() * Game.MAP_WIDTH);
                    int ey = (int) (Math.random() * Game.MAP_HEIGHT);
                    java.awt.Rectangle enemyRect = new java.awt.Rectangle(ex, ey, 28, 28);
                    boolean collides = false;
                    for (Wall wall : game.getWallManager().getWalls()) {
                        if (wall.getBounds().intersects(enemyRect)) { collides = true; break; }
                    }
                    if (!collides) {
                        HealerEnemy e = new HealerEnemy(ex, ey);
                        e.setMaxHealth(game.HEALER_BASE_HP);
                        e.setFullHealth();
                        e.setDamage(game.HEALER_BASE_DMG);
                        enemies.add(e);
                        spawned = true;
                    }
                    attempts++;
                }
                healerTimer = 0;
            }
        }
    }

    public void removeDeadEnemies() {
        Iterator<BaseEnemy> it = enemies.iterator();
        while (it.hasNext()) {
            BaseEnemy enemy = it.next();
            if (!enemy.isAlive()) {
                it.remove();
            }
        }
    }
}
