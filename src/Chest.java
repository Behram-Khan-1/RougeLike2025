
import java.awt.*;

public class Chest {
    public enum Rarity { NORMAL, RARE, SUPER_RARE }
    public int x, y;
    public Rarity rarity;
    public boolean opened = false;
    public long spawnTime = System.currentTimeMillis();
    private static final int SIZE = 28;

    // Ability and cost per rarity
    public static int getCoinCost(Rarity rarity) {
        switch (rarity) {
            case SUPER_RARE: return 30;
            case RARE: return 15;
            default: return 5;
        }
    }
    public static String getAbilityName(Rarity rarity) {
        switch (rarity) {
            case SUPER_RARE: return "Super Power";
            case RARE: return "Rare Skill";
            default: return "Normal Boost";
        }
    }
    public static String getAbilityDescription(Rarity rarity) {
        switch (rarity) {
            case SUPER_RARE: return "Gain a super rare ability!";
            case RARE: return "Gain a rare ability!";
            default: return "Gain a normal ability.";
        }
    }

    public Chest(int x, int y, Rarity rarity) {
        this.x = x;
        this.y = y;
        this.rarity = rarity;
        this.spawnTime = System.currentTimeMillis();
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, SIZE, SIZE);
    }

    public void draw(Graphics g, Camera camera) {
        if (opened) return;
        long now = System.currentTimeMillis();
        long alive = now - spawnTime;
        // Flicker if older than 25s
        boolean flicker = (alive > 25000) && ((alive / 150) % 2 == 0);
        if (alive > 30000) return;
        if (flicker) return;
        int sx = camera.getScreenX(x);
        int sy = camera.getScreenY(y);
        Color color = Color.ORANGE;
        if (rarity == Rarity.RARE) color = Color.CYAN;
        if (rarity == Rarity.SUPER_RARE) color = Color.MAGENTA;
        g.setColor(color);
        g.fillRect(sx, sy, SIZE, SIZE);
        g.setColor(Color.BLACK);
        g.drawRect(sx, sy, SIZE, SIZE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        String label = "C";
        if (rarity == Rarity.RARE) label = "R";
        if (rarity == Rarity.SUPER_RARE) label = "S";
        g.drawString(label, sx + 8, sy + 19);
    }
}