// === Game.java ===
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
// Modular manager imports

// Modular enemy imports
// (No explicit imports needed for same-package classes)
import javax.sound.sampled.Clip;

public class Game extends JPanel implements Runnable {
    private WallManager wallManager;
    public WallManager getWallManager() {
        return wallManager;
    }
    private ChestManager chestManager;
    // Chest opening UI state
    private boolean chestUIActive = false;
    private String chestPowerupName = "";
    private String chestPowerupDesc = "";
    private long chestUITextTime = 0;
    private long lastChestSpawnTime = 0;
    private static final long CHEST_SPAWN_INTERVAL = 7000; // 10 seconds
    private static final double RARE_RATE = 0.20;
    private static final double SUPER_RARE_RATE = 0.10;
    // Debug flag
    private static final boolean DEBUG = false;
    // --- Enemy spawn timing (seconds) ---
    public static final int SQUARE_START = 0;
    public static final int DIAMOND_START = 30;
    public static final int CIRCLE_START = 60;
    public static final int HEALER_START = 60;
    public static final int DIAMOND_LOW_RATE_START = 40;

    // --- Spawn rates (frames) ---
    public static final int SQUARE_SPAWN_RATE = 300; // every 3s
    public static final int DIAMOND_SPAWN_RATE = 1300; // every 13s
    public static final int CIRCLE_SPAWN_RATE = 1300; // every 13s
    public static final int HEALER_SPAWN_RATE = 1050; // every 10.5s
    private int score = 0;
    private Thread gameThread;
    private boolean running = false;

    public static final int MAP_WIDTH = 2000;
    public static final int MAP_HEIGHT = 1500;
    public static final int VIEWPORT_WIDTH = 1200;
    public static final int VIEWPORT_HEIGHT = 1000;

    private Player player;
    private EnemyManager enemyManager;
    private BulletManager bulletManager;
    private ArrayList<Coin> coins;
    private Camera camera;

    private boolean gameOver = false;
    private Rectangle restartButtonBounds = new Rectangle();
    private boolean showStartScreen = true;
    private Rectangle startButtonBounds = new Rectangle();

    private DiamondCoverManager coverManager;

    // Enemy scaling configuration
    public double ENEMY_HP_SCALE = 1.0;
    public double ENEMY_DMG_SCALE = 1.0;
    public int ENEMY_SCALE_INTERVAL = 35; // seconds
    public double ENEMY_HP_PER_INTERVAL = 0.05; // +15% HP per interval
    public double ENEMY_DMG_PER_INTERVAL = 0.05; // +10% damage per interval
    // Per-type base values (can be changed)
    public int SQUARE_BASE_HP = 100;
    public double SQUARE_BASE_DMG = 5.0;
    public int DIAMOND_BASE_HP = 200;
    public double DIAMOND_BASE_DMG = 7.0;
    public int CIRCLE_BASE_HP = 60;
    public double CIRCLE_BASE_DMG = 7.0;
    public int HEALER_BASE_HP = 120;
    public double HEALER_BASE_DMG = 0.0;

    private UIManager uiManager;
    private PowerupManager powerupManager = new PowerupManager();
    private Clip bgMusic;

    public Game() {
        wallManager = new WallManager();
        // Procedural wall generation
        int numWalls = 6 + (int) (Math.random() * 3); // 6-8 walls
        for (int i = 0; i < numWalls; i++) {
            int w = 60 + (int) (Math.random() * 80); // 60-140 px
            int h = 60 + (int) (Math.random() * 80);
            int x = 100 + (int) (Math.random() * (MAP_WIDTH - w - 200));
            int y = 100 + (int) (Math.random() * (MAP_HEIGHT - h - 200));
            wallManager.addWall(new Wall(x, y, w, h));
        }
        setPreferredSize(new Dimension(VIEWPORT_WIDTH, VIEWPORT_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        InputHandler input = new InputHandler();
        addKeyListener(input);
        addMouseListener(input.getMouseAdapter());
        addMouseMotionListener(input.getMouseAdapter());
        player = new Player(500, 500);
        enemyManager = new EnemyManager(this);
        bulletManager = new BulletManager();
        coins = new ArrayList<>();
        chestManager = new ChestManager();
        lastChestSpawnTime = System.currentTimeMillis();
    
        camera = new Camera(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, MAP_WIDTH, MAP_HEIGHT);
        coverManager = new DiamondCoverManager();
        uiManager = new UIManager(this);

        // Start background music
        String musicFile = Math.random() < 0.5 ?
        "assets/music/Music1.wav" :
        "assets/music/Music2.wav";
        bgMusic = SoundManager.playLoop(musicFile);


        // Mouse listener for start and restart button
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (showStartScreen && startButtonBounds.contains(e.getPoint())) {
                    showStartScreen = false;
                    // (Re)start background music if needed
                    if (bgMusic != null && !bgMusic.isRunning()) bgMusic.start();
                }
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
        if (r < SUPER_RARE_RATE)
            rarity = Chest.Rarity.SUPER_RARE;
        else if (r < RARE_RATE + SUPER_RARE_RATE)
            rarity = Chest.Rarity.RARE;
        // Pick random position not too close to player, not on walls, and within viewport
        int margin = 300; // Allow chests to spawn anywhere the player can walk
        int cx = 0, cy = 0;
        int attempts = 0;
        boolean valid = false;
        while (attempts < 20 && !valid) {
            cx = margin + (int) (Math.random() * (MAP_WIDTH - margin * 2 - 28));
            cy = margin + (int) (Math.random() * (MAP_HEIGHT - margin * 2 - 28));
            Rectangle chestRect = new Rectangle(cx, cy, 28, 28); // Assuming chest is 28x28
            valid = true;
            if (player.getPosition().distance(cx, cy) < 100) valid = false;
            for (Wall wall : wallManager.getWalls()) {
                if (wall.getBounds().intersects(chestRect)) { valid = false; break; }
            }
            attempts++;
        }
        if (valid && attempts <= 20) {
            chestManager.addChest(new Chest(cx, cy, rarity));
        }
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
            if (!showStartScreen) update();
            // Ensure enemies do not spawn on walls (handled in EnemyManager or here if needed)
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

    private int lastScaleStep = 0;

    public void updateEnemyBaseStats(int seconds) {
        int scaleSteps = seconds / ENEMY_SCALE_INTERVAL;
        if (scaleSteps > lastScaleStep) {
            // Only update if we've reached a new interval
            SQUARE_BASE_HP = (int) (SQUARE_BASE_HP * (1.0 + ENEMY_HP_PER_INTERVAL));
            SQUARE_BASE_DMG = (SQUARE_BASE_DMG * (1.0 + ENEMY_DMG_PER_INTERVAL));
            DIAMOND_BASE_HP = (int) (DIAMOND_BASE_HP * (1.0 + ENEMY_HP_PER_INTERVAL));
            DIAMOND_BASE_DMG = (DIAMOND_BASE_DMG * (1.0 + ENEMY_DMG_PER_INTERVAL));
            CIRCLE_BASE_HP = (int) (CIRCLE_BASE_HP * (1.0 + ENEMY_HP_PER_INTERVAL));
            CIRCLE_BASE_DMG = (CIRCLE_BASE_DMG * (1.0 + ENEMY_DMG_PER_INTERVAL));
            HEALER_BASE_HP = (int) (HEALER_BASE_HP * (1.0 + ENEMY_HP_PER_INTERVAL));
            HEALER_BASE_DMG = (int) (HEALER_BASE_DMG * (1.0 + ENEMY_DMG_PER_INTERVAL));
            lastScaleStep = scaleSteps;
        }
    }

    private void spawnEnemies() {
        // Ensure new enemies do not spawn on top of walls
        // This assumes enemyManager.update() is responsible for spawning new enemies
        // If you have custom spawn logic, add wall collision checks there
        // Example for random spawn (pseudo):
        // for (each enemy to spawn) {
        //   pick random x, y;
        //   if (not colliding with any wall) spawn;
        // }
        enemyManager.update();
    }

    private void update() {
        // Prevent player from moving through walls
        Rectangle playerRect = player.getBounds();
        for (Wall wall : wallManager.getWalls()) {
            if (playerRect.intersects(wall.getBounds())) {
                Rectangle wr = wall.getBounds();
                if (player.x + 20 > wr.x && player.x < wr.x + wr.width) {
                    if (player.y < wr.y)
                        player.y = wr.y - 20;
                    else
                        player.y = wr.y + wr.height;
                }
                if (player.y + 20 > wr.y && player.y < wr.y + wr.height) {
                    if (player.x < wr.x)
                        player.x = wr.x - 20;
                    else
                        player.x = wr.x + wr.width;
                }
            }
        }
        // Clamp player to map boundaries (keep always visible)
        int margin = 300; // margin so player is always inside screen
        player.x = Math.max(margin, Math.min(player.x, MAP_WIDTH - margin ));
        player.y = Math.max(margin, Math.min(player.y, MAP_HEIGHT - margin ));
        // Prevent enemies from moving through walls and clamp to map boundaries
        for (BaseEnemy enemy : enemyManager.getEnemies()) {
            Rectangle er = enemy.getBounds();
            for (Wall wall : wallManager.getWalls()) {
                if (er.intersects(wall.getBounds())) {
                    Rectangle wr = wall.getBounds();
                    if (enemy.getPosition().x + 20 > wr.x && enemy.getPosition().x < wr.x + wr.width) {
                        if (enemy.getPosition().y < wr.y)
                            enemy.getPosition().y = wr.y - 20;
                        else
                            enemy.getPosition().y = wr.y + wr.height;
                    }
                    if (enemy.getPosition().y + 20 > wr.y && enemy.getPosition().y < wr.y + wr.height) {
                        if (enemy.getPosition().x < wr.x)
                            enemy.getPosition().x = wr.x - 20;
                        else
                            enemy.getPosition().x = wr.x + wr.width;
                    }
                }
            }
            // Clamp enemy to map boundaries (keep always visible)
            int marginE = 300;
            enemy.getPosition().x = Math.max(marginE, Math.min(enemy.getPosition().x, MAP_WIDTH - marginE - 20));
            enemy.getPosition().y = Math.max(marginE, Math.min(enemy.getPosition().y, MAP_HEIGHT - marginE - 20));
        }
        // Chest despawn and flicker logic
        long now = System.currentTimeMillis();
        Iterator<Chest> chestItDespawn = chestManager.getChests().iterator();
        while (chestItDespawn.hasNext()) {
            Chest chest = chestItDespawn.next();
            long alive = now - chest.spawnTime;
            if (!chest.opened && alive > 30000) {
                chestItDespawn.remove();
            }
        }
        // Chest opening logic (no pause)
        Iterator<Chest> chestIt = chestManager.getChests().iterator();
        while (chestIt.hasNext()) {
            Chest chest = chestIt.next();
            if (!chest.opened && chest.getBounds().intersects(player.getBounds())) {
                if (InputHandler.e) {
                    int cost = Chest.getCoinCost(chest.rarity);
                    if (player.getCoins() >= cost) {
                        player.setCoins(player.getCoins() - cost);
                        SoundManager.playSound("assets/sounds/Find_Item.wav"); // Play before opening
                        chest.opened = true;
                        // --- Powerup logic ---
                        PowerupManager.PowerupType powerup = powerupManager.getRandomPowerup(chest.rarity);
                        switch (powerup) {
                            case MAX_HP_5:
                                player.increaseMaxHealth(5);
                                chestPowerupName = "+5 Max HP";
                                chestPowerupDesc = "Increase your max HP by 5.";
                                break;
                            case MAX_HP_7:
                                player.increaseMaxHealth(7);
                                chestPowerupName = "+7 Max HP";
                                chestPowerupDesc = "Increase your max HP by 7.";
                                break;
                            case MAX_HP_10:
                                player.increaseMaxHealth(10);
                                chestPowerupName = "+10 Max HP";
                                chestPowerupDesc = "Increase your max HP by 10.";
                                break;
                            case DAMAGE_10:
                                player.increasePlayerDamagePercent(10);
                                chestPowerupName = "+10% Damage";
                                chestPowerupDesc = "Increase your damage by 10%.";
                                break;
                            case DAMAGE_20:
                                player.increasePlayerDamagePercent(15);
                                chestPowerupName = "+15% Damage";
                                chestPowerupDesc = "Increase your damage by 15%.";
                                break;
                            case DAMAGE_30:
                                player.increasePlayerDamagePercent(20);
                                chestPowerupName = "+20% Damage";
                                chestPowerupDesc = "Increase your damage by 20%.";
                                break;
                            case REGEN_025:
                                player.addHealthRegen(0.25);
                                chestPowerupName = "+0.25 HP/sec Regen";
                                chestPowerupDesc = "Regenerate 0.25 HP per second.";
                                break;
                            case REGEN_05:
                                player.addHealthRegen(0.5);
                                chestPowerupName = "+0.5 HP/sec Regen";
                                chestPowerupDesc = "Regenerate 0.5 HP per second.";
                                break;
                            case REGEN_1:
                                player.addHealthRegen(1.0);
                                chestPowerupName = "+1 HP/sec Regen";
                                chestPowerupDesc = "Regenerate 1 HP per second.";
                                break;
                            case ATTACK_SPEED_5:
                                player.increaseAttackSpeedPercent(5);
                                chestPowerupName = "+5% Attack Speed";
                                chestPowerupDesc = "Shoot 5% faster.";
                                break;
                            case ATTACK_SPEED_10:
                                player.increaseAttackSpeedPercent(10);
                                chestPowerupName = "+10% Attack Speed";
                                chestPowerupDesc = "Shoot 10% faster.";
                                break;
                            case ATTACK_SPEED_20:
                                player.increaseAttackSpeedPercent(20);
                                chestPowerupName = "+20% Attack Speed";
                                chestPowerupDesc = "Shoot 20% faster.";
                                break;
                            case MULTISHOT_2:
                                player.grantMultishot2();
                                chestPowerupName = "Multishot 2";
                                chestPowerupDesc = "Shoot an extra bullet from your back.";
                                break;
                            case MULTISHOT_3:
                                player.grantMultishot3();
                                chestPowerupName = "Multishot 3";
                                chestPowerupDesc = "Shoot 3 bullets in a spread from your front.";
                                break;
                            case CRIT_CHANCE_15:
                                player.addCritChance(15);
                                chestPowerupName = "+15% Crit Chance";
                                chestPowerupDesc = "15% chance for critical hits.";
                                break;
                            case CRIT_CHANCE_30:
                                player.addCritChance(30);
                                chestPowerupName = "+30% Crit Chance";
                                chestPowerupDesc = "30% chance for critical hits.";
                                break;
                            case CRIT_MULT_1_5:
                                player.addCritMultiplier(0.5);
                                chestPowerupName = "Crit Multiplier +0.5x";
                                chestPowerupDesc = "Critical hits deal +0.5x more damage (stacks).";
                                break;
                            case CRIT_MULT_2_0:
                                player.addCritMultiplier(1.0);
                                chestPowerupName = "Crit Multiplier +1.0x";
                                chestPowerupDesc = "Critical hits deal +1.0x more damage (stacks).";
                                break;
                            default:
                                // For now, only implement health and damage powerups
                                chestPowerupName = "???";
                                chestPowerupDesc = "(Other powerups coming soon)";
                        }
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
        // --- Heart spawning logic (every 25 seconds) ---
        final long HEART_SPAWN_INTERVAL = 25000; // 25 seconds
        if (now - lastHeartSpawnTime > HEART_SPAWN_INTERVAL) {
            // Try up to 10 times to find a valid spawn location
            int attempts = 0;
            boolean spawned = false;
            while (attempts < 10 && !spawned) {
                int marginHeart = 300; // margin to avoid spawning too close to walls or player
                int hx = marginHeart + (int)(Math.random() * (MAP_WIDTH - 2 * marginHeart));
                int hy = marginHeart + (int)(Math.random() * (MAP_HEIGHT - 2 * marginHeart));
                Rectangle heartRect = new Rectangle(hx, hy, 20, 20);
                boolean collides = false;
                // Don't spawn on player
                if (player.getBounds().intersects(heartRect) || player.getPosition().distance(hx, hy) < 60) collides = true;
                // Don't spawn on walls
                for (Wall wall : wallManager.getWalls()) {
                    if (wall.getBounds().intersects(heartRect)) { collides = true; break; }
                }
                // Don't spawn on chests
                for (Chest chest : chestManager.getChests()) {
                    if (chest.getBounds().intersects(heartRect)) { collides = true; break; }
                }
                // Don't spawn on other hearts
                for (Heart h : hearts) {
                    if (!h.collected && new Rectangle(h.x, h.y, 20, 20).intersects(heartRect)) { collides = true; break; }
                }
                if (!collides) {
                    hearts.add(new Heart(hx, hy));
                    spawned = true;
                }
                attempts++;
            }
            lastHeartSpawnTime = now;
        }
        player.update(MAP_WIDTH, MAP_HEIGHT);
        camera.update(player.getPosition().x, player.getPosition().y);
        spawnEnemies();
        enemyManager.updateEnemies(player, coverManager, wallManager.getWalls(), bulletManager.getBullets());

        // Handle bullet updates and collisions via BulletManager
        bulletManager.updateBullets(player, enemyManager.getEnemies(), wallManager.getWalls(), coins, this);

        // Handle coin pickup
        Iterator<Coin> coinIterator = coins.iterator();
        while (coinIterator.hasNext()) {
            Coin coin = coinIterator.next();
            if (!coin.isCollected() && coin.getBounds().intersects(player.getBounds())) {
                coin.collect();
                player.addCoins(coin.value);
                // Play coin pickup sound
                SoundManager.playSound("assets/sounds/money.wav");
                coinIterator.remove();
            }
        }

        // Handle heart pickup
        Iterator<Heart> heartIterator = hearts.iterator();
        while (heartIterator.hasNext()) {
            Heart heart = heartIterator.next();
            if (!heart.collected && heart.getBounds().intersects(player.getBounds())) {
                heart.collected = true;
                player.setHealth(Math.min(player.getHealth() + Heart.HEAL_AMOUNT, player.getMaxHealth()));
                // Optional: play a sound effect for heart pickup
                SoundManager.playSound("assets/sounds/Heal.wav");
                heartIterator.remove();
            }
        }

        // Handle shooting
        if (InputHandler.mousePressed && InputHandler.shootCooldown <= 0) {
            java.util.List<Bullet> shots = player.shootAtMouseMulti(camera);
            for (Bullet b : shots) bulletManager.addBullet(b);
            // Play shooting sound
            SoundManager.playSound("assets/sounds/Fire.wav");
            InputHandler.shootCooldown = player.getCurrentShootCooldown();
        } else if (InputHandler.shootCooldown > 0) {
            InputHandler.shootCooldown--;
        }

        // Check for game over
        if (player.getHealth() <= 0) {
            player.setHealth(0); // Clamp health to 0
            gameOver = true;
            SoundManager.playSound("assets/sounds/Hero_Dies.wav");
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (showStartScreen) {
            // Draw info at top
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 22));
            g.drawString("ENEMIES:", 40, 50);
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            g.drawString("Square: Average enemy.", 40, 80);
            g.drawString("Circle: Fast, shoots 3 bullets.", 40, 110);
            g.drawString("Diamond: Tanky, squares hide behind it.", 40, 140);
            g.drawString("Healer: Heals enemies, doesn't attack.", 40, 170);
            g.setFont(new Font("Arial", Font.BOLD, 22));
            g.drawString("CHESTS:", 40, 210);
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            g.drawString("C = Common, R = Rare, S = Super Rare.", 40, 240);
            g.drawString("Open with coins dropped by enemies to get powerups.", 40, 270);
            g.drawString("Go on top of chest and press E to open.", 40, 300);
            g.setFont(new Font("Arial", Font.BOLD, 22));
            g.drawString("CONTROLS:", 40, 340);
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            g.drawString("Move: WASD", 40, 370);
            g.drawString("Open Chest: E", 40, 400);
            g.drawString("Shoot: Left Mouse Button", 40, 430);
            g.drawString("Pink Hearts: Pick them up to heal.", 40, 460);
            // Draw start button at bottom
            String btnText = "Start Game";
            g.setFont(new Font("Arial", Font.BOLD, 36));
            FontMetrics btnFM = g.getFontMetrics();
            int btnWidth = btnFM.stringWidth(btnText) + 40;
            int btnHeight = btnFM.getHeight() + 20;
            int btnX = (VIEWPORT_WIDTH - btnWidth) / 2;
            int btnY = (VIEWPORT_HEIGHT) / 2; // Just below center
            startButtonBounds.setBounds(btnX, btnY, btnWidth, btnHeight);
            g.setColor(Color.LIGHT_GRAY);
            g.fillRoundRect(btnX, btnY, btnWidth, btnHeight, 20, 20);
            g.setColor(Color.BLACK);
            g.drawRoundRect(btnX, btnY, btnWidth, btnHeight, 20, 20);
            int textX = btnX + (btnWidth - btnFM.stringWidth(btnText)) / 2;
            int textY = btnY + ((btnHeight - btnFM.getHeight()) / 2) + btnFM.getAscent();
            g.drawString(btnText, textX, textY);
            return;
        }

        // Draw walls
        for (Wall wall : wallManager.getWalls())
            wall.draw(g, camera);
        // Draw chest powerup UI at top center if active
        if (chestUIActive) {
            String title = "Powerup: " + chestPowerupName;
            String desc = chestPowerupDesc;
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(VIEWPORT_WIDTH / 2 - 250, 40, 500, 90);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 28));
            FontMetrics fm = g.getFontMetrics();
            int tx = VIEWPORT_WIDTH / 2 - fm.stringWidth(title) / 2;
            g.drawString(title, tx, 80);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            fm = g.getFontMetrics();
            int dx = VIEWPORT_WIDTH / 2 - fm.stringWidth(desc) / 2;
            g.drawString(desc, dx, 110);
        }

        // Draw score and coins using UIManager
        uiManager.drawScore(g);
        // Draw stats window using UIManager
        uiManager.drawStats(g);

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
        for (BaseEnemy enemy : enemyManager.getEnemies()) {
            if (!enemy.isAlive()) continue; // Do not draw stats for dead enemies
            double enemyDistance = enemy.getPosition().distance(camera.getX() + VIEWPORT_WIDTH / 2,
                    camera.getY() + VIEWPORT_HEIGHT / 2);
            if (enemyDistance <= visibilityRadius) {
                enemy.draw(g, camera);
                // --- DEBUG: Draw enemy HP and damage above each enemy ---
                g.setFont(new Font("Arial", Font.BOLD, 14));
                g.setColor(Color.WHITE);
                int ex = camera.getScreenX(enemy.getPosition().x);
                int ey = camera.getScreenY(enemy.getPosition().y);
                String stats = "HP: " + (int) enemy.getHealth() + "  DMG: " + enemy.getDamage();
                g.drawString(stats, ex - 10, ey - 10);
                // --- END DEBUG ---
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
                            g.drawOval(tx - 5, ty - 5, 10, 10);
                            g.drawLine(camera.getScreenX(enemy.getPosition().x),
                                    camera.getScreenY(enemy.getPosition().y), tx, ty);
                            g.setFont(new Font("Arial", Font.PLAIN, 12));
                            g.drawString("Target: (" + roamTarget.x + "," + roamTarget.y + ")", tx + 8, ty);
                        }
                    } catch (Exception e2) {
                        /* ignore if not present */ }
                }
            }
        }
        // Debug: draw player position and lines to chests
        if (DEBUG) {
            g.setColor(Color.WHITE);
            int px = camera.getScreenX(player.getPosition().x);
            int py = camera.getScreenY(player.getPosition().y);
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.drawString("Player: (" + player.getPosition().x + "," + player.getPosition().y + ")", px + 20, py - 10);

            // Draw lines from player to all chests
            g.setColor(Color.PINK);
            for (Chest chest : chestManager.getChests()) {
                if (!chest.opened) {
                    int cx = camera.getScreenX(chest.x);
                    int cy = camera.getScreenY(chest.y);
                    g.drawLine(px + 10, py + 10, cx + 14, cy + 14); // center of player to center of chest
                }
            }
        }

        for (Bullet bullet : bulletManager.getBullets())
            bullet.draw(g, camera);
        for (Coin coin : coins)
            coin.draw(g, camera);
        for (Chest chest : chestManager.getChests())
            chest.draw(g, camera);
        for (Chest chest : chestManager.getChests())
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

        // Draw hearts
        for (Heart heart : hearts) {
            if (!heart.collected) heart.draw(g, camera);
        }
    }

    // Resets all game state for a fresh restart
    private void restartGame() {
        player = new Player(500, 500);
        enemyManager.reset();
        bulletManager = new BulletManager();
        chestManager.clear(); // Clear all chests on restart
        hearts.clear(); // Clear all hearts on restart
        lastHeartSpawnTime = System.currentTimeMillis();
        // Reset enemy base stats
        SQUARE_BASE_HP = 100;
        SQUARE_BASE_DMG = 10.0;
        DIAMOND_BASE_HP = 200;
        DIAMOND_BASE_DMG = 6.0;
        CIRCLE_BASE_HP = 60;
        CIRCLE_BASE_DMG = 12.0;
        HEALER_BASE_HP = 120;
        HEALER_BASE_DMG = 0.0;
        enemyManager.getEnemies().add(new SquareEnemy(1000, 1000));
        enemyManager.getEnemies().add(new HealerEnemy(1100, 1100));
        camera = new Camera(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, MAP_WIDTH, MAP_HEIGHT);
        coverManager = new DiamondCoverManager();
        score = 0;
       
        gameOver = false;
        repaint();
    }

    private ArrayList<Heart> hearts = new ArrayList<>();
    private long lastHeartSpawnTime = System.currentTimeMillis();

    public Player getPlayer() {
        return player;
    }

    public int getViewportWidth() {
        return VIEWPORT_WIDTH;
    }

    public int getScore() {
        return score;
    }

    // Add score to the current score
    public void addScore(int value) {
        this.score += value;
    }
}

// if (player.health <= 0) {
// // Handle game over logic, e.g., display game over screen or restart game
// }
