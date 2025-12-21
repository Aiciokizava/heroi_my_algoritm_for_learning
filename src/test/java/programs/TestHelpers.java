package programs;

import com.battle.heroes.army.Unit;

import java.util.HashMap;

final class TestHelpers {
    private TestHelpers() {}

    static Unit newUnit(String name, String type, int x, int y,
                        int hp, int atk, int cost) {
        return new Unit(
                name, type,
                hp, atk, cost,
                "melee",
                new HashMap<>(),
                new HashMap<>(),
                x, y
        );
    }
}
