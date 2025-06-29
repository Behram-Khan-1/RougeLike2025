import java.util.ArrayList;

public class ChestManager {
    private ArrayList<Chest> chests;

    public ChestManager() {
        chests = new ArrayList<>();
    }

    public ArrayList<Chest> getChests() {
        return chests;
    }

    public void addChest(Chest chest) {
        chests.add(chest);
    }

    public void removeChest(Chest chest) {
        chests.remove(chest);
    }

    public void clear() {
        chests.clear();
    }
}
