package programs;

import com.battle.heroes.army.Army;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SimulateBattleImplTest {

    @Test
    void simulateDoesNotCrashOnEmptyArmies() {
        SimulateBattleImpl sim = new SimulateBattleImpl();
        Army emptyLeft  = new Army();
        Army emptyRight = new Army();

        assertDoesNotThrow(() ->
                sim.simulate(emptyLeft, emptyRight)
        );
    }
}
