package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.PrintBattleLog;
import com.battle.heroes.army.programs.SimulateBattle;

import java.util.*;

/**
 * Простая симуляция боя
 *
 * Логика:
 *  1. Пока в обеих армиях есть живые юниты – идёт раунд.
 *  2. В начале раунда собирается очередь всех живых юнитов
 *     обеих армий и сортируется по baseAttack DESC
 *     (сильнейшие ходят первыми).  O(N log N) на раунд.
 *  3. Каждый юнит из очереди, если ещё жив, вызывает свою
 *     embedded-программу {@code UnitProgram.attack()}, получая
 *     ссылку на цель и нанося урон.
 *
 *  Конец боя наступает, когда в одной из армий нет живых юнитов:
 *  это проверяется перед каждым ударом – как только противников
 *  не осталось, метод возвращается.
 *
 *  Сложность: O(R · N log N), где
 *      N – суммарное кол-во юнитов,
 *      R – число полных раундов (обычно < N).
 *
 *  Память: O(N) – сама очередь.
 */
public class SimulateBattleImpl implements SimulateBattle {

    /** Ссылка передаётся игрой через рефлексию. */
    private PrintBattleLog printBattleLog;
    public void setPrintBattleLog(PrintBattleLog log) { this.printBattleLog = log; }

    @Override
    public void simulate(Army player, Army computer) throws InterruptedException {
        if (player == null || computer == null) return;

        // Главный цикл пока у обеих армий есть юниты
        while (hasAlive(player) && hasAlive(computer)) {

            /* Шаг 1. Собираем и сортируем очередь. */
            List<Unit> queue = buildQueue(player, computer);

            /* Шаг 2. Каждому по-ходу. */
            for (Unit attacker : queue) {
                if (!attacker.isAlive()) continue;

                // Если противников больше нет – бой окончен.
                if (!hasEnemy(attacker, player, computer)) return;

                Unit target = attacker.getProgram().attack();
                if (printBattleLog != null) printBattleLog.printBattleLog(attacker, target);
            }
            // конец раунда, цикл while соберёт новую очередь.
        }
    }

    /** true, если в армии остался хотя бы один живой юнит. */
    private static boolean hasAlive(Army a) {
        if (a == null || a.getUnits() == null) return false;
        for (Unit u : a.getUnits()) if (u.isAlive()) return true;
        return false;
    }

    /** Собираем всех живых юнитов и сортируем по «силе удара». */
    private static List<Unit> buildQueue(Army player, Army computer) {
        List<Unit> q = new ArrayList<>();
        if (player   != null && player.getUnits()   != null)
            for (Unit u : player.getUnits())   if (u.isAlive()) q.add(u);
        if (computer != null && computer.getUnits() != null)
            for (Unit u : computer.getUnits()) if (u.isAlive()) q.add(u);

        q.sort(Comparator.comparingInt(Unit::getBaseAttack).reversed());
        return q;
    }

    /** Проверяем, остались ли живые противники. */
    private static boolean hasEnemy(Unit u, Army player, Army computer) {
        boolean inPlayer   = contains(player, u);
        boolean inComputer = contains(computer, u);
        return (inPlayer   && hasAlive(computer)) ||
                (inComputer && hasAlive(player));
    }

    private static boolean contains(Army a, Unit u) {
        if (a == null || a.getUnits() == null) return false;
        for (Unit x : a.getUnits()) if (x == u) return true;
        return false;
    }
}
