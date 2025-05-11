
// === Game.java ===
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

public class Game extends JPanel implements Runnable {
    private Thread gameThread;
    private boolean running = false;

    private static final int MAP_WIDTH = 2000;
    private static final int MAP_HEIGHT = 1500;
    private static final int VIEWPORT_WIDTH = 800;
    private static final int VIEWPORT_HEIGHT = 600;

    private Player player;
    private ArrayList<Enemy> enemies;
    private ArrayList<Bullet> bullets;
    private Camera camera;

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
        enemies.add(new Enemy(1000, 1000));
        camera = new Camera(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, MAP_WIDTH, MAP_HEIGHT);
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
        while (running) {
            update();
            repaint();
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void update() {
        player.update(MAP_WIDTH, MAP_HEIGHT);
        camera.update(player.getPosition().x, player.getPosition().y);
        for (Enemy enemy : enemies) {
            enemy.update(player);
            // Enemy shooting logic
            Bullet enemyBullet = enemy.shoot(player);
            if (enemyBullet != null) {
                bullets.add(enemyBullet);
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

            for (Enemy enemy : enemies) {
                if (enemy.isAlive() && bullet.isActive() &&
                    bullet.getType() == BulletType.PLAYER &&
                    bullet.getBounds().intersects(enemy.getBounds())) {
                    enemy.takeDamage(bullet.getDamage());
                    bullet.deactivate();
                    bulletIterator.remove();
                    break;
                }
                // Add logic for enemy bullets hitting the player
                if (bullet.getType() == BulletType.ENEMY &&
                    bullet.getBounds().intersects(player.getBounds())) {
                    player.takeDamage(bullet.getDamage());
                    bullet.deactivate();
                    bulletIterator.remove();
                    break;
                }
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
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
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
        double playerDistance = player.getPosition().distance(camera.getX() + VIEWPORT_WIDTH / 2, camera.getY() + VIEWPORT_HEIGHT / 2);
        if (playerDistance <= visibilityRadius) {
            player.draw(g, camera);
        }
        for (Enemy enemy : enemies) {
            double enemyDistance = enemy.getPosition().distance(camera.getX() + VIEWPORT_WIDTH / 2, camera.getY() + VIEWPORT_HEIGHT / 2);
            if (enemyDistance <= visibilityRadius) {
                enemy.draw(g, camera);
            }
        }
        for (Bullet bullet : bullets)
            bullet.draw(g, camera);
    }
}

// if (player.health <= 0) {
//     // Handle game over logic, e.g., display game over screen or restart game
// }
