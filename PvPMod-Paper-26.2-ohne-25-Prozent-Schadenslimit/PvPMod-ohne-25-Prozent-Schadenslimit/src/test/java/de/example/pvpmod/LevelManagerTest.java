package de.example.pvpmod;

import de.example.pvpmod.data.PlayerData;
import de.example.pvpmod.manager.LevelManager;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LevelManagerTest {
    private final LevelManager levels = new LevelManager();

    @Test void heartTableBoundaries() {
        assertEquals(0.5, levels.hearts(-10), 0.000001);
        assertEquals(1.0, levels.hearts(-9), 0.000001);
        assertEquals(10.0, levels.hearts(0), 0.000001);
        assertEquals(16.0, levels.hearts(20), 0.000001);
        assertEquals(20.0, levels.hearts(30), 0.000001);
    }

    @Test void everyHeartLevelMatchesTheSpecifiedTable() {
        double[] expected = {0.5,1,2,3,4,5,6,7,8,9,10,10,10.5,10.5,11,11,11.5,11.5,12,12,12.5,12.5,13,13,13.5,13.5,14,14,14.5,14.5,16,16,17,17,17,18,18,18,19,19,20};
        for (int level = -10; level <= 30; level++) {
            assertEquals(expected[level + 10], levels.hearts(level), 0.000001, "Level " + level);
        }
    }

    @Test void heartsAreBoundedAndMonotonic() {
        double last = 0;
        for (int level = -10; level <= 30; level++) {
            double hearts = levels.hearts(level);
            assertTrue(hearts >= last);
            assertTrue(hearts <= 20);
            last = hearts;
        }
        assertEquals(0.5, levels.hearts(-100), 0.000001);
        assertEquals(20, levels.hearts(100), 0.000001);
    }

    @Test void multipliersMatchSpecification() {
        assertEquals(1.0, levels.damageMultiplier(0), 0.000001);
        assertEquals(2.0, levels.damageMultiplier(30), 0.000001);
        assertEquals(1.0, levels.xpMultiplier(0), 0.000001);
        assertEquals(5.0, levels.xpMultiplier(30), 0.000001);
        assertEquals(0.0, levels.damageReduction(0), 0.000001);
        assertEquals(0.05, levels.damageReduction(30), 0.000001);
        assertTrue(levels.damageMultiplier(-1) < 1);
        assertTrue(levels.xpMultiplier(-1) < 1);
        assertEquals(0.0, levels.damageReduction(-10), 0.000001);
    }

    @Test void allLevelBasedValuesClampOutsideConfiguredRange() {
        assertEquals(levels.damageMultiplier(-10), levels.damageMultiplier(-100), 0.000001);
        assertEquals(levels.damageMultiplier(30), levels.damageMultiplier(100), 0.000001);
        assertEquals(levels.xpMultiplier(-10), levels.xpMultiplier(-100), 0.000001);
        assertEquals(levels.xpMultiplier(30), levels.xpMultiplier(100), 0.000001);
        assertEquals(levels.damageReduction(-10), levels.damageReduction(-100), 0.000001);
        assertEquals(levels.damageReduction(30), levels.damageReduction(100), 0.000001);
    }

    @Test void addAndRemoveCannotOverflowOrEscapeBounds() {
        PlayerData data = new PlayerData(UUID.randomUUID(), "Tester");
        assertEquals(30, levels.add(data, Integer.MAX_VALUE));
        assertEquals(-10, levels.remove(data, Integer.MAX_VALUE));
        assertEquals(30, levels.set(data, Integer.MAX_VALUE));
        assertEquals(-10, levels.set(data, Integer.MIN_VALUE));
    }
}
