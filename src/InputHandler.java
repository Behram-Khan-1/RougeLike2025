import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Point;

public class InputHandler extends KeyAdapter {
    public static boolean up, down, left, right;
    public static boolean e = false, enter = false;
    public static int shootCooldown = 0;
    public static Point mousePosition = new Point(0, 0);
    public static boolean mousePressed = false;

    private MouseAdapter mouseAdapter = new MouseAdapter(){

        @Override
        public void mouseMoved(MouseEvent e) {
            mousePosition.setLocation(e.getX(), e.getY());
        }
        
        @Override
        public void mouseDragged(MouseEvent e) {
            mousePosition.setLocation(e.getX(), e.getY());
        }
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                mousePressed = true;
            }
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                mousePressed = false;
            }
        }
        
    };

    public MouseAdapter getMouseAdapter() {
        return mouseAdapter;
    }
    
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
                up = true;
                break;
            case KeyEvent.VK_S:
                down = true;
                break;
            case KeyEvent.VK_A:
                left = true;
                break;
            case KeyEvent.VK_D:
                right = true;
                break;
            case KeyEvent.VK_E:
                InputHandler.e = true;
                break;
            case KeyEvent.VK_ENTER:
                InputHandler.enter = true;
                break;
        }
    }

    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
                up = false;
                break;
            case KeyEvent.VK_S:
                down = false;
                break;
            case KeyEvent.VK_A:
                left = false;
                break;
            case KeyEvent.VK_D:
                right = false;
                break;
            case KeyEvent.VK_E:
                InputHandler.e = false;
                break;
            case KeyEvent.VK_ENTER:
                InputHandler.enter = false;
                break;
        }
    }
}