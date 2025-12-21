package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeneratePresetImplTest {

    private final GeneratePresetImpl gen = new GeneratePresetImpl();

    @Test
    void layoutIsKnightFrontArcherBack() {
        List<Unit> proto = List.of(
                TestHelpers.newUnit("Archer","Archer",0,0,20,7,20),
                TestHelpers.newUnit("Sword","Swordsman",0,0,30,9,40),
                TestHelpers.newUnit("Pike","Pikeman",0,0,30,8,30),
                TestHelpers.newUnit("Knight","Knight",0,0,50,11,50)
        );

        Army a = gen.generate(proto, 1500);
        assertTrue(a.getPoints() <= 1500);

        for (Unit u : a.getUnits()) {
            switch (u.getUnitType()) {
                case "Archer"   -> assertEquals(0, u.getxCoordinate());
                case "Pikeman", "Swordsman" -> assertEquals(1, u.getxCoordinate());
                case "Knight"   -> assertEquals(2, u.getxCoordinate());
                default         -> fail();
            }
        }
    }
}
