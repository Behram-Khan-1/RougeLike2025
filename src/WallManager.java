import java.util.ArrayList;

public class WallManager {
    private ArrayList<Wall> walls;

    public WallManager() {
        walls = new ArrayList<>();
    }

    public ArrayList<Wall> getWalls() {
        return walls;
    }

    public void addWall(Wall wall) {
        walls.add(wall);
    }

    public void clear() {
        walls.clear();
    }
}
