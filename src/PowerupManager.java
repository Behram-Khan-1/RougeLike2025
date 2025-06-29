import java.util.*;

public class PowerupManager {
    public enum PowerupType {
        MAX_HP_5, MAX_HP_7, MAX_HP_10,
        DAMAGE_10, DAMAGE_20, DAMAGE_30,
        REGEN_025, REGEN_05, REGEN_1,
        ATTACK_SPEED_5, ATTACK_SPEED_10, ATTACK_SPEED_20,
        MULTISHOT_2, MULTISHOT_3,
        CRIT_CHANCE_15, CRIT_CHANCE_30,
        CRIT_MULT_1_5, CRIT_MULT_2_0
    }

    public static class PowerupDrop {
        public final PowerupType type;
        public final double chance; // as percent (e.g., 40 for 40%)
        public PowerupDrop(PowerupType type, double chance) {
            this.type = type;
            this.chance = chance;
        }
    }

    // Drop tables for each chest rarity
    private static final List<PowerupDrop> NORMAL_DROPS = List.of(
        new PowerupDrop(PowerupType.MAX_HP_5, 40),
        new PowerupDrop(PowerupType.MAX_HP_7, 20),
        new PowerupDrop(PowerupType.MAX_HP_10, 5),
        new PowerupDrop(PowerupType.DAMAGE_10, 20),
        new PowerupDrop(PowerupType.DAMAGE_20, 10),
        new PowerupDrop(PowerupType.DAMAGE_30, 2),
        new PowerupDrop(PowerupType.REGEN_025, 15),
        new PowerupDrop(PowerupType.REGEN_05, 10),
        new PowerupDrop(PowerupType.REGEN_1, 1),
        new PowerupDrop(PowerupType.ATTACK_SPEED_5, 30),
        new PowerupDrop(PowerupType.ATTACK_SPEED_10, 15),
        new PowerupDrop(PowerupType.ATTACK_SPEED_20, 3)
    );
    private static final List<PowerupDrop> RARE_DROPS = List.of(
        new PowerupDrop(PowerupType.MAX_HP_5, 10),
        new PowerupDrop(PowerupType.MAX_HP_7, 25),
        new PowerupDrop(PowerupType.MAX_HP_10, 10),
        new PowerupDrop(PowerupType.DAMAGE_10, 10),
        new PowerupDrop(PowerupType.DAMAGE_20, 20),
        new PowerupDrop(PowerupType.DAMAGE_30, 5),
        new PowerupDrop(PowerupType.REGEN_025, 5),
        new PowerupDrop(PowerupType.REGEN_05, 20),
        new PowerupDrop(PowerupType.REGEN_1, 5),
        new PowerupDrop(PowerupType.ATTACK_SPEED_5, 10),
        new PowerupDrop(PowerupType.ATTACK_SPEED_10, 25),
        new PowerupDrop(PowerupType.ATTACK_SPEED_20, 5),
        new PowerupDrop(PowerupType.MULTISHOT_2, 10),
        new PowerupDrop(PowerupType.MULTISHOT_3, 5),
        new PowerupDrop(PowerupType.CRIT_CHANCE_15, 10),
        new PowerupDrop(PowerupType.CRIT_CHANCE_30, 5),
        new PowerupDrop(PowerupType.CRIT_MULT_1_5, 10),
        new PowerupDrop(PowerupType.CRIT_MULT_2_0, 5)
    );
    private static final List<PowerupDrop> SUPER_RARE_DROPS = List.of(
        new PowerupDrop(PowerupType.MAX_HP_7, 15),
        new PowerupDrop(PowerupType.MAX_HP_10, 20),
        new PowerupDrop(PowerupType.DAMAGE_20, 10),
        new PowerupDrop(PowerupType.DAMAGE_30, 15),
        new PowerupDrop(PowerupType.REGEN_05, 10),
        new PowerupDrop(PowerupType.REGEN_1, 15),
        new PowerupDrop(PowerupType.ATTACK_SPEED_10, 10),
        new PowerupDrop(PowerupType.ATTACK_SPEED_20, 15),
        new PowerupDrop(PowerupType.MULTISHOT_2, 10),
        new PowerupDrop(PowerupType.MULTISHOT_3, 20),
        new PowerupDrop(PowerupType.CRIT_CHANCE_15, 10),
        new PowerupDrop(PowerupType.CRIT_CHANCE_30, 15),
        new PowerupDrop(PowerupType.CRIT_MULT_1_5, 10),
        new PowerupDrop(PowerupType.CRIT_MULT_2_0, 15)
    );

    public PowerupType getRandomPowerup(Chest.Rarity rarity) {
        List<PowerupDrop> drops;
        switch (rarity) {
            case SUPER_RARE: drops = SUPER_RARE_DROPS; break;
            case RARE: drops = RARE_DROPS; break;
            default: drops = NORMAL_DROPS;
        }
        double roll = Math.random() * 100;
        double cumulative = 0;
        for (PowerupDrop drop : drops) {
            cumulative += drop.chance;
            if (roll < cumulative) return drop.type;
        }
        // fallback (should not happen)
        return drops.get(drops.size() - 1).type;
    }
}
