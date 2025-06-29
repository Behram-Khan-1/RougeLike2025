// === Player.java ===
import java.awt.*;

public class Player {
    int x, y, speed = 3;
    private int health = 100; // Add health attribute
    private int maxHealth = 100;
    private int coins = 0;
    private double playerDamage = 20.0;
    private double healthRegen = 0.0;
    private int baseShootCooldown = 15;
    private double attackSpeedMultiplier = 1.0;

    private boolean hasMultishot2 = false;
    private boolean hasMultishot3 = false;

    private double critChance = 0.0; // percent
    private double critMultiplier = 1.0;

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = Math.min(health, maxHealth);
    }

    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
        if (health > maxHealth) health = maxHealth;
    }
    public void increaseMaxHealth(int amount) {
        this.maxHealth += amount;
        this.health += amount; // Optionally heal by the same amount
    }

    public int getCoins() { return coins; }
    public void addCoins(int amount) { coins += amount; }
    public void setCoins(int amount) { coins = amount; }

    public double getPlayerDamage() { return playerDamage; }
    public void setPlayerDamage(double dmg) { this.playerDamage = dmg; }
    public void increasePlayerDamagePercent(double percent) {
        this.playerDamage *= (1.0 + percent / 100.0);
    }

    public double getHealthRegen() { return healthRegen; }
    public void addHealthRegen(double amount) { this.healthRegen += amount; }

    public int getBaseShootCooldown() { return baseShootCooldown; }
    public void setBaseShootCooldown(int cooldown) { this.baseShootCooldown = cooldown; }
    public double getAttackSpeedMultiplier() { return attackSpeedMultiplier; }
    public void increaseAttackSpeedPercent(double percent) {
        this.attackSpeedMultiplier *= (1.0 + percent / 100.0);
    }
    public int getCurrentShootCooldown() {
        return (int)Math.max(1, Math.round(baseShootCooldown / attackSpeedMultiplier));
    }

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update(int mapWidth, int mapHeight) {
        if (health <= 0) return; // Prevent movement if dead
        // Health regen
        if (health < maxHealth && healthRegen > 0) {
            health += healthRegen / 60.0; // Regen per frame (assuming 60 FPS)
            if (health > maxHealth) health = maxHealth;
        }
        if (InputHandler.up) y = Math.max(0, y - speed);
        if (InputHandler.down) y = Math.min(mapHeight - 20, y + speed);
        if (InputHandler.left) x = Math.max(0, x - speed);
        if (InputHandler.right) x = Math.min(mapWidth - 20, x + speed);
    }

    public void draw(Graphics g, Camera camera) {

        g.setColor(Color.GREEN);
        int screenX = camera.getScreenX(x);
        int screenY = camera.getScreenY(y);
        g.fillRect(screenX, screenY, 20, 20);
        
        // Draw health bar
        g.setColor(Color.RED);
        // int healthBarWidth = (int)((health / 100.0) * 20);

        int barHeight = 5;
        int barPadding = 2; // space between player and health bar
        int healthBarWidth = (int)((health / (double)maxHealth) * 20);
        int healthBarY = screenY - barHeight - barPadding;
        
        // Draw health bar above the player
        g.setColor(Color.RED);
        g.fillRect(screenX, healthBarY, healthBarWidth, barHeight);
        
        

    }

    public Point getPosition() {
        return new Point(x, y);
    }
    public Rectangle getBounds() {
        return new Rectangle(x, y, 20, 20);
    }

    public Bullet shootAtMouse(Camera camera) {
        // Get mouse position in screen coordinates
        int mouseScreenX = InputHandler.mousePosition.x;
        int mouseScreenY = InputHandler.mousePosition.y;
        
        // Convert screen coordinates to world coordinates
        double worldMouseX = mouseScreenX + camera.getX();
        double worldMouseY = mouseScreenY + camera.getY();
        
        // Calculate direction from player to world mouse position
        double dx = worldMouseX - x;
        double dy = worldMouseY - y;
        double length = Math.sqrt(dx * dx + dy * dy);
        
        // Normalize the direction vector
        if (length > 0) {
            dx /= length;
            dy /= length;
        }
        
        // Create bullet at player's center position
        Bullet b = new Bullet(x + 10, y + 10, dx, dy, BulletType.PLAYER);
        b.setDamage((int)Math.round(playerDamage));
        return b;
    }

    public java.util.List<Bullet> shootAtMouseMulti(Camera camera) {
        java.util.List<Bullet> bullets = new java.util.ArrayList<>();
        // Get mouse position in screen coordinates
        int mouseScreenX = InputHandler.mousePosition.x;
        int mouseScreenY = InputHandler.mousePosition.y;
        double worldMouseX = mouseScreenX + camera.getX();
        double worldMouseY = mouseScreenY + camera.getY();
        double dx = worldMouseX - x;
        double dy = worldMouseY - y;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length > 0) {
            dx /= length;
            dy /= length;
        }
        // Main bullet (front)
        Bullet main = new Bullet(x + 10, y + 10, dx, dy, BulletType.PLAYER);
        main.setDamage((int)Math.round(playerDamage));
        bullets.add(main);
        // Multishot 3: 3 bullets in a spread (front)
        if (hasMultishot3) {
            double spread = Math.PI / 10; // ~18 degrees
            for (int i = -1; i <= 1; i += 2) {
                double angle = Math.atan2(dy, dx) + i * spread;
                double sdx = Math.cos(angle);
                double sdy = Math.sin(angle);
                Bullet b = new Bullet(x + 10, y + 10, sdx, sdy, BulletType.PLAYER);
                b.setDamage((int)Math.round(playerDamage));
                bullets.add(b);
            }
        }
        // Multishot 2: 1 bullet from back
        if (hasMultishot2) {
            double backAngle = Math.atan2(dy, dx) + Math.PI;
            double bdx = Math.cos(backAngle);
            double bdy = Math.sin(backAngle);
            Bullet back = new Bullet(x + 10, y + 10, bdx, bdy, BulletType.PLAYER);
            back.setDamage((int)Math.round(playerDamage));
            bullets.add(back);
        }
        // For each bullet, set crit info
        for (Bullet b : bullets) {
            b.setCritChance(critChance);
            b.setCritMultiplier(critMultiplier);
        }
        return bullets;
    }

    public void grantMultishot2() { hasMultishot2 = true; }
    public void grantMultishot3() { hasMultishot3 = true; }
    public boolean hasMultishot2() { return hasMultishot2; }
    public boolean hasMultishot3() { return hasMultishot3; }

    public double getCritChance() { return critChance; }
    public double getCritMultiplier() { return critMultiplier; }
    public void addCritChance(double percent) {
        this.critChance += percent;
        if (this.critChance > 100.0) this.critChance = 100.0;
    }
    public void addCritMultiplier(double add) {
        this.critMultiplier += add;
    }

    public void takeDamage(int damage) {
        if (health <= 0) return;
        health -= damage;
        if (health < 0) health = 0;
        if (health == 0) {
            // Play death sound
            SoundManager.playSound("assets/sounds/hero die.wav");
            System.out.println("Player died");
        } else {
            // Play hurt sound
            SoundManager.playSound("assets/sounds/hero hurt.wav");
        }
    }
}