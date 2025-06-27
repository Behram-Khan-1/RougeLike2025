import java.util.HashMap;
import java.util.Map;

public class DiamondCoverManager {
    // Map diamond enemy to the enemy hiding behind it
    private Map<BaseEnemy, BaseEnemy> coverMap = new HashMap<>();

    public boolean isDiamondAvailable(BaseEnemy diamond) {
        return !coverMap.containsKey(diamond);
    }

    public void assignCover(BaseEnemy diamond, BaseEnemy hider) {
        coverMap.put(diamond, hider);
    }

    public void removeCover(BaseEnemy diamond) {
        coverMap.remove(diamond);
    }

    public BaseEnemy getHider(BaseEnemy diamond) {
        return coverMap.get(diamond);
    }

    public void clear() {
        coverMap.clear();
    }
}
