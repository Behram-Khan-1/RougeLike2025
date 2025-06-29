import java.awt.*;

public class UIManager {
    private Game game;

    public UIManager(Game game) {
        this.game = game;
    }

    public void drawStats(Graphics g) {
        // Example: Draw player stats window
        int statsX = 20, statsY = 60;
        g.setColor(new Color(30,30,30,200));
        g.fillRoundRect(statsX-10, statsY-30, 180, 90, 18, 18);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Player Stats", statsX, statsY);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("Health: " + game.getPlayer().getHealth(), statsX, statsY + 25);
        g.drawString("Damage: " + (int)Math.round(game.getPlayer().getPlayerDamage()), statsX, statsY + 45);
        double shotsPerSec = 60.0 / game.getPlayer().getCurrentShootCooldown();
        g.drawString(String.format("Atk Speed: %.2f/s", shotsPerSec), statsX, statsY + 65);
        g.drawString(String.format("Crit: %.0f%% x%.2f", game.getPlayer().getCritChance(), game.getPlayer().getCritMultiplier()), statsX, statsY + 85);
    }

    public void drawScore(Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, 24));
        String scoreText = "Score: " + game.getScore();
        FontMetrics fmScore = g.getFontMetrics();
        int scoreX = game.getViewportWidth() - fmScore.stringWidth(scoreText) - 20;
        int scoreY = 40;
        g.setColor(Color.YELLOW);
        g.drawString(scoreText, scoreX, scoreY);
        // Draw coins below score
        String coinsText = "Coins: " + game.getPlayer().getCoins();
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(Color.WHITE);
        g.drawString(coinsText, scoreX, scoreY + 30);
    }

    // Add more UI drawing methods as needed (powerup UI, game over, etc)
}
