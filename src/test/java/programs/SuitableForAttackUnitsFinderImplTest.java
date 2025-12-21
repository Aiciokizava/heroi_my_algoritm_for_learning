package programs;

import com.battle.heroes.army.Unit;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SuitableForAttackUnitsFinderImplTest {

    private final SuitableForAttackUnitsFinderImpl finder =
            new SuitableForAttackUnitsFinderImpl();

    @Test
    void returnsOnlyThoseWhoCanStepForward() {
        Unit u1 = TestHelpers.newUnit("u1","S",0,0,10,1,1);
        Unit u2 = TestHelpers.newUnit("u2","S",0,1,10,1,1);

        List<List<Unit>> rows = List.of(
                List.of(u1),
                List.of(u2)
        );

        List<Unit> res = finder.getSuitableUnits(rows, /*isLeftTarget=*/true);
        assertEquals(1, res.size());
        assertTrue(res.contains(u1));
    }
}
