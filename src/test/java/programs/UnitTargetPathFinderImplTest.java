package programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.Edge;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UnitTargetPathFinderImplTest {

    private final UnitTargetPathFinderImpl pf = new UnitTargetPathFinderImpl();

    @Test
    void pathExistsOnEmptyField() {
        Unit a = TestHelpers.newUnit("A","S",0,0,10,1,1);
        Unit b = TestHelpers.newUnit("B","S",3,3,10,1,1);

        List<Edge> path = pf.getTargetPath(a,b,List.of(a,b));
        assertFalse(path.isEmpty());
        Edge last = path.get(path.size()-1);
        assertEquals(3, last.getX());
        assertEquals(3, last.getY());
    }

    @Test
    void noPathWhenCompletelyBlocked() {
        Unit a = TestHelpers.newUnit("A","S",0,0,10,1,1);
        Unit t = TestHelpers.newUnit("T","S",2,2,10,1,1);

        List<Unit> walls = List.of(
                TestHelpers.newUnit("W1","W",0,1,1,0,0),
                TestHelpers.newUnit("W2","W",0,2,1,0,0),
                TestHelpers.newUnit("W3","W",1,0,1,0,0),
                TestHelpers.newUnit("W4","W",1,1,1,0,0),
                TestHelpers.newUnit("W5","W",1,2,1,0,0),
                TestHelpers.newUnit("W6","W",2,0,1,0,0),
                TestHelpers.newUnit("W7","W",2,1,1,0,0)
                // клетку (2,2) – цель – не блокируем
        );

        List<Unit> all = new java.util.ArrayList<>(walls);
        all.add(a);
        all.add(t);

        List<Edge> path = pf.getTargetPath(a, t, all);
        assertTrue(path.isEmpty(), "Пути не должно существовать");
    }

    private static List<Unit> concat(Unit a, Unit b, List<Unit> others) {
        List<Unit> all = new java.util.ArrayList<>(others);
        all.add(a);
        all.add(b);
        return all;
    }
}
