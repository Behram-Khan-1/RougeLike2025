
// === Game.java ===
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
// Modular enemy imports
// (No explicit imports needed for same-package classes)

import java.util.List;
import java.util.Random;

public class Game extends JPanel implements Runnable {
    

    private ArrayList<Chest> chests;
    // Chest opening UI state
    private boolean chestUIActive = false;
    private String chestPowerupName = "";
    private String chestPowerupDesc = "";
    private long chestUITextTime = 0;
    private long lastChestSpawnTime = 0;
    private static final long CHEST_SPAWN_INTERVAL = 1000; // 15 seconds
    private static final double RARE_RATE = 0.15;
    private static final double SUPER_RARE_RATE = 0.05;
    // Debug flag
    private static final boolean DEBUG = false;
    // --- Enemy spawn timing (seconds) ---
    private static final int SQUARE_START = 110;
    private static final int DIAMOND_START = 110;
    private static final int CIRCLE_START = 110;
    private static final int HEALER_START =110;
    private static final int DIAMOND_LOW_RATE_START = 40;

    // --- Spawn rates (frames) ---
    private static final int SQUARE_SPAWN_RATE = 120; // every 2s
    private static final int DIAMOND_SPAWN_RATE = 300; // every 5s (after 40s)
    private static final int CIRCLE_SPAWN_RATE = 240; // every 4s
    private static final int HEALER_SPAWN_RATE = 600; // every 10s
    private int score = 0;
    private Thread gameThread;
    private boolean running = false;

    private static final int MAP_WIDTH = 2000;
    private static final int MAP_HEIGHT = 1500;
    private static final int VIEWPORT_WIDTH = 1200;
    private static final int VIEWPORT_HEIGHT = 1000;

    private Player player;
    private ArrayList<BaseEnemy> enemies;
    private ArrayList<Bullet> bullets;
    private ArrayList<Coin> coins;
    private Camera camera;

    private boolean gameOver = false;
    private Rectangle restartButtonBounds = new Rectangle();

    private int frameCount = 0;
    private int squareTimer = 0, diamondTimer = 0, circleTimer = 0, healerTimer = 0;

    private DiamondCoverManager coverManager;

    public Game() {
        setPreferredSize(new Dimension(VIEWPORT_WIDTH, VIEWPORT_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        InputHandler input = new InputHandler();
        addKeyListener(input);
        addMouseListener(input.getMouseAdapter());
        addMouseMotionListener(input.getMouseAdapter());
        player = new Player(500, 500);
        enemies = new ArrayList<>();
        bullets = new ArrayList<>();
        coins = new ArrayList<>();
        chests = new ArrayList<>();
        lastChestSpawnTime = System.currentTimeMillis();

                enemies.add(new SquareEnemy(1000, 1000));
        // enemies.add(new SquareEnemy(1200, 1000));
        // enemies.add(new SquareEnemy(1300, 1000));
        enemies.add(new DiamondEnemy(1050, 1000));
        // enemies.add(new CircleEnemy(1250, 1000));
        // enemies.add(new CircleEnemy(1150, 1000));
        // enemies.add(new HealerEnemy(1100, 1100));
        camera = new Camera(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, MAP_WIDTH, MAP_HEIGHT);
        coverManager = new DiamondCoverManager();

        // Mouse listener for restart button
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (gameOver && restartButtonBounds.contains(e.getPoint())) {
                    restartGame();
                }
            }
        });
    }

    private void spawnChest() {
        // Pick rarity
        double r = Math.random();
        Chest.Rarity rarity = Chest.Rarity.NORMAL;
        if (r < SUPER_RARE_RATE) rarity = Chest.Rarity.SUPER_RARE;
        else if (r < RARE_RATE + SUPER_RARE_RATE) rarity = Chest.Rarity.RARE;
        // Pick random position not too close to player, and within viewport
        int margin = 60;
        int cx, cy;
        do {
            cx = (int)(camera.getX() + margin + Math.random() * (VIEWPORT_WIDTH - 2 * margin - 28));
            cy = (int)(camera.getY() + margin + Math.random() * (VIEWPORT_HEIGHT - 2 * margin - 28));
        } while (player.getPosition().distance(cx, cy) < 100);
        chests.add(new Chest(cx, cy, rarity));
    }

    public void start() {
        JFrame frame = new JFrame("Roguelike AI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void run() {
        final int targetFPS = 60;
        final long frameDuration = 1000 / targetFPS;
        while (running) {
            long frameStart = System.currentTimeMillis();
            update();
            repaint();
            long frameEnd = System.currentTimeMillis();
            long elapsed = frameEnd - frameStart;
            long sleepTime = frameDuration - elapsed;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void spawnEnemies() {
        frameCount++;
        int seconds = frameCount / 60;
        // SQUARE
        if (seconds >= SQUARE_START) {
            squareTimer++;
            if (squareTimer >= SQUARE_SPAWN_RATE) {
                enemies.add(new SquareEnemy((int) (Math.random() * MAP_WIDTH), (int) (Math.random() * MAP_HEIGHT)));
                squareTimer = 0;
            }
        }
        // DIAMOND
        if (seconds >= DIAMOND_START) {
            diamondTimer++;
            int rate = (seconds >= DIAMOND_LOW_RATE_START) ? DIAMOND_SPAWN_RATE : 600; // slower before 40s
            if (diamondTimer >= rate) {
                enemies.add(new DiamondEnemy((int) (Math.random() * MAP_WIDTH), (int) (Math.random() * MAP_HEIGHT)));
                diamondTimer = 0;
            }
        }
        // CIRCLE
        if (seconds >= CIRCLE_START) {
            circleTimer++;
            if (circleTimer >= CIRCLE_SPAWN_RATE) {
                enemies.add(new CircleEnemy((int) (Math.random() * MAP_WIDTH), (int) (Math.random() * MAP_HEIGHT)));
                circleTimer = 0;
            }
        }
        // HEALER
        if (seconds >= HEALER_START) {
            healerTimer++;
            if (healerTimer >= HEALER_SPAWN_RATE) {
                enemies.add(new HealerEnemy((int) (Math.random() * MAP_WIDTH), (int) (Math.random() * MAP_HEIGHT)));
                healerTimer = 0;
            }
        }
    }

    private void update() {
        // Chest despawn and flicker logic
        long now = System.currentTimeMillis();
        Iterator<Chest> chestItDespawn = chests.iterator();
        while (chestItDespawn.hasNext()) {
            Chest chest = chestItDespawn.next();
            long alive = now - chest.spawnTime;
            if (!chest.opened && alive > 30000) {
                chestItDespawn.remove();
            }
        }
        // Chest opening logic (no pause)
        Iterator<Chest> chestIt = chests.iterator();
        while (chestIt.hasNext()) {
            Chest chest = chestIt.next();
            if (!chest.opened && chest.getBounds().intersects(player.getBounds())) {
                if (InputHandler.e) {
                    int cost = Chest.getCoinCost(chest.rarity);
                    if (player.getCoins() >= cost) {
                        player.setCoins(player.getCoins() - cost);
                        chest.opened = true;
                        chestPowerupName = Chest.getAbilityName(chest.rarity);
                        chestPowerupDesc = Chest.getAbilityDescription(chest.rarity);
                        chestUIActive = true;
                        chestUITextTime = System.currentTimeMillis();
                        chestIt.remove(); // Remove chest from world
                    }
                    InputHandler.e = false;
                }
            }
        }
        // Hide UI after 2 seconds or on Enter
        if (chestUIActive && (InputHandler.enter || System.currentTimeMillis() - chestUITextTime > 2000)) {
            chestUIActive = false;
            chestPowerupName = "";
            chestPowerupDesc = "";
            InputHandler.enter = false;
        }
        if (gameOver) {
            return;
        }
        // Chest spawning logic (runs every frame)
        if (now - lastChestSpawnTime > CHEST_SPAWN_INTERVAL) {
            spawnChest();
            lastChestSpawnTime = now;
        }
        player.update(MAP_WIDTH, MAP_HEIGHT);
        camera.update(player.getPosition().x, player.getPosition().y);
        spawnEnemies();
        coverManager.clear();
        for (BaseEnemy enemy : enemies) {
            // Use coverManager for SquareEnemy and similar
            if (enemy instanceof SquareEnemy) {
                ((SquareEnemy) enemy).update(player, enemies, coverManager);
            } else {
                enemy.update(player, enemies);
            }
            // Enemy shooting logic
            java.util.List<Bullet> enemyBullets = enemy.shoot(player);
            if (enemyBullets != null) {
                bullets.addAll(enemyBullets);
            }
        }

        // Handle bullet updates and collisions
        Iterator<Bullet> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            if (!bullet.update()) {
                bulletIterator.remove();
                continue;
            }

            for (BaseEnemy enemy : enemies) {
                if (enemy.isAlive() && bullet.isActive() &&
                        bullet.getType() == BulletType.PLAYER &&
                        bullet.getBounds().intersects(enemy.getBounds())) {
                    enemy.takeDamage(bullet.getDamage());
                    if (!enemy.isAlive()) {
                        score += 100; // Add score for killing enemy
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
                    // player.takeDamage(bullet.getDamage());
                    bullet.deactivate();
                    bulletIterator.remove();
                    break;
                }
            }
        }

        // Handle coin pickup
        Iterator<Coin> coinIterator = coins.iterator();
        while (coinIterator.hasNext()) {
            Coin coin = coinIterator.next();
            if (!coin.isCollected() && coin.getBounds().intersects(player.getBounds())) {
                coin.collect();
                player.addCoins(coin.value);
                coinIterator.remove();
            }
        }

        // Handle shooting
        if (InputHandler.mousePressed && InputHandler.shootCooldown <= 0) {
            bullets.add(player.shootAtMouse(camera));
            InputHandler.shootCooldown = 15;
        } else if (InputHandler.shootCooldown > 0) {
            InputHandler.shootCooldown--;
        }

        bullets.removeIf(b -> !b.update());

        // Check for game over
        if (player.getHealth() <= 0) {
            player.setHealth(0); // Clamp health to 0
            gameOver = true;
        }
    }

    public void paintComponent(Graphics g) {
        long now = System.currentTimeMillis();
        super.paintComponent(g);
        // Draw chest powerup UI at top center if active
        if (chestUIActive) {
            String title = "Powerup: " + chestPowerupName;
            String desc = chestPowerupDesc;
            g.setColor(new Color(0,0,0,180));
            g.fillRect(VIEWPORT_WIDTH/2-250, 40, 500, 90);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 28));
            FontMetrics fm = g.getFontMetrics();
            int tx = VIEWPORT_WIDTH/2 - fm.stringWidth(title)/2;
            g.drawString(title, tx, 80);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            fm = g.getFontMetrics();
            int dx = VIEWPORT_WIDTH/2 - fm.stringWidth(desc)/2;
            g.drawString(desc, dx, 110);
        }

        // Draw score at top right
        g.setFont(new Font("Arial", Font.BOLD, 24));
        String scoreText = "Score: " + score;
        FontMetrics fmScore = g.getFontMetrics();
        int scoreX = VIEWPORT_WIDTH - fmScore.stringWidth(scoreText) - 20;
        int scoreY = 40;
        g.setColor(Color.YELLOW);
        g.drawString(scoreText, scoreX, scoreY);

        // Draw coins below score
        String coinsText = "Coins: " + player.getCoins();
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(Color.WHITE);
        g.drawString(coinsText, scoreX, scoreY + 30);

        // Draw stats window on left
        int statsX = 20, statsY = 60;
        g.setColor(new Color(30,30,30,200));
        g.fillRoundRect(statsX-10, statsY-30, 180, 90, 18, 18);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Player Stats", statsX, statsY);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("Health: " + player.getHealth(), statsX, statsY + 25);
        g.drawString("Damage: 10", statsX, statsY + 45); // Placeholder for damage
        // Draw background or map grid
        g.setColor(Color.DARK_GRAY);
        for (int x = 0; x < MAP_WIDTH; x += 50) {
            int screenX = camera.getScreenX(x);
            if (screenX >= 0 && screenX <= VIEWPORT_WIDTH) {
                g.drawLine(screenX, 0, screenX, VIEWPORT_HEIGHT);
            }
        }
        for (int y = 0; y < MAP_HEIGHT; y += 50) {
            int screenY = camera.getScreenY(y);
            if (screenY >= 0 && screenY <= VIEWPORT_HEIGHT) {
                g.drawLine(0, screenY, VIEWPORT_WIDTH, screenY);
            }
        }

        // Draw game objects with camera offset
        double visibilityRadius = 400.0; // Define the visibility radius
        double playerDistance = player.getPosition().distance(camera.getX() + VIEWPORT_WIDTH / 2,
                camera.getY() + VIEWPORT_HEIGHT / 2);
        if (playerDistance <= visibilityRadius) {
            player.draw(g, camera);
        }
        for (BaseEnemy enemy : enemies) {
            double enemyDistance = enemy.getPosition().distance(camera.getX() + VIEWPORT_WIDTH / 2,
                    camera.getY() + VIEWPORT_HEIGHT / 2);
            if (enemyDistance <= visibilityRadius) {
                enemy.draw(g, camera);
                // Debug: draw enemy roam/scout target if enabled
                if (DEBUG) {
                    try {
                        java.lang.reflect.Field roamField = enemy.getClass().getDeclaredField("roamTarget");
                        roamField.setAccessible(true);
                        java.awt.Point roamTarget = (java.awt.Point) roamField.get(enemy);
                        if (roamTarget != null) {
                            g.setColor(Color.CYAN);
                            int tx = camera.getScreenX(roamTarget.x);
                            int ty = camera.getScreenY(roamTarget.y);
                            g.drawOval(tx-5, ty-5, 10, 10);
                            g.drawLine(camera.getScreenX(enemy.getPosition().x), camera.getScreenY(enemy.getPosition().y), tx, ty);
                            g.setFont(new Font("Arial", Font.PLAIN, 12));
                            g.drawString("Target: ("+roamTarget.x+","+roamTarget.y+")", tx+8, ty);
                        }
                    } catch (Exception ex) { /* ignore if not present */ }
                }
            }
        }
        // Debug: draw player position and lines to chests
        if (DEBUG) {
            g.setColor(Color.WHITE);
            int px = camera.getScreenX(player.getPosition().x);
            int py = camera.getScreenY(player.getPosition().y);
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.drawString("Player: ("+player.getPosition().x+","+player.getPosition().y+")", px+20, py-10);

            // Draw lines from player to all chests
            g.setColor(Color.PINK);
            for (Chest chest : chests) {
                if (!chest.opened) {
                    int cx = camera.getScreenX(chest.x);
                    int cy = camera.getScreenY(chest.y);
                    g.drawLine(px + 10, py + 10, cx + 14, cy + 14); // center of player to center of chest
                }
            }
        }

        for (Bullet bullet : bullets)
            bullet.draw(g, camera);
        for (Coin coin : coins)
            coin.draw(g, camera);
        for (Chest chest : chests)
            chest.draw(g, camera);
        for (Chest chest : chests)
            chest.draw(g, camera);

        // Draw Game Over text if game is over
        if (gameOver) {
            String msg = "GAME OVER";
            g.setFont(new Font("Arial", Font.BOLD, 64));
            FontMetrics fm = g.getFontMetrics();
            int x = (VIEWPORT_WIDTH - fm.stringWidth(msg)) / 2;
            int y = (VIEWPORT_HEIGHT - fm.getHeight()) / 2 + fm.getAscent();
            g.setColor(Color.RED);
            g.drawString(msg, x, y);

            // Draw Restart button
            String btnText = "Restart";
            g.setFont(new Font("Arial", Font.BOLD, 36));
            FontMetrics btnFM = g.getFontMetrics();
            int btnWidth = btnFM.stringWidth(btnText) + 40;
            int btnHeight = btnFM.getHeight() + 20;
            int btnX = (VIEWPORT_WIDTH - btnWidth) / 2;
            int btnY = y + 40;
            restartButtonBounds.setBounds(btnX, btnY, btnWidth, btnHeight);
            g.setColor(Color.LIGHT_GRAY);
            g.fillRoundRect(btnX, btnY, btnWidth, btnHeight, 20, 20);
            g.setColor(Color.BLACK);
            g.drawRoundRect(btnX, btnY, btnWidth, btnHeight, 20, 20);
            int textX = btnX + (btnWidth - btnFM.stringWidth(btnText)) / 2;
            int textY = btnY + ((btnHeight - btnFM.getHeight()) / 2) + btnFM.getAscent();
            g.drawString(btnText, textX, textY);
        }

    }
    // Resets all game state for a fresh restart
    private void restartGame() {
        player = new Player(500, 500);
        enemies = new ArrayList<>();
        bullets = new ArrayList<>();
        enemies.add(new SquareEnemy(1000, 1000));
        enemies.add(new HealerEnemy(1100, 1100));
        camera = new Camera(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, MAP_WIDTH, MAP_HEIGHT);
        coverManager = new DiamondCoverManager();
        score = 0;
        frameCount = 0;
        squareTimer = 0;
        diamondTimer = 0;
        circleTimer = 0;
        healerTimer = 0;
        gameOver = false;
        repaint();
    }
}

// if (player.health <= 0) {
// // Handle game over logic, e.g., display game over screen or restart game
// }
