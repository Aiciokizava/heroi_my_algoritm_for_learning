package programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.SuitableForAttackUnitsFinder;

import java.util.*;

/**
 * SuitableForAttackUnitsFinderImpl
 * --------------------------------
 * Определяет список готовых к атаке юнитов своей армии.
 *
 * Правила движка (по условию лабораторной):
 *     юнит может атаковать, если сделает один ШАГ ВПЕРЁД по оси Y
 *     и окажется на свободной клетке; сам удар случится уже из новой клетки;
 *     направление «вперёд» зависит от стороны:
 *       – army, начинающая слева (isLeftArmyTarget = true), идёт ВВЕРХ
 *         по экрану ➜ dy = -1;
 *       – правая army — ВНИЗ ➜ dy = +1.
 *
 * Алгоритм:
 *   1. Одним проходом формируем:
 *        a) список всех живых юнитов (all);
 *        b) HashSet «occupied» для быстрых O(1) проверок занятости клетки.
 *   2. Для каждого живого юнита проверяем клетку (x, y + dy):
 *        пусто? — юнит добавляется в result, иначе пропускаем.
 *
 * Сложность:
 *   • Время  O(U)   — U = кол-во живых юнитов в нашей армии.
 *   • Память O(U)   — HashSet<Long> occupied.
 *
 * Примечание по координатам:
 *   Поле у игры фиксированное (27 × 21), однако размеры в расчётах
 *   не нужны — достаточно проверять есть ли там юнит.
 *   Для compact-хэширования пары (x,y) используется приём «pack в long».
 */
public class SuitableForAttackUnitsFinderImpl implements SuitableForAttackUnitsFinder {

    @Override
    public List<Unit> getSuitableUnits(List<List<Unit>> unitsByRow,
                                       boolean isLeftArmyTarget) {

        // --- 0. Edge-case: пустой список строк ------------------------
        if (unitsByRow == null || unitsByRow.isEmpty())
            return Collections.emptyList();

        /* =============================================================
           1. Собираем данные о текущем расположении
        ============================================================= */
        Set<Long> occupied = new HashSet<>(); // все занятые клетки армии
        List<Unit> alive   = new ArrayList<>(); // живые юниты (чтобы не ходить ещё раз)

        for (List<Unit> row : unitsByRow) {          // перебираем строки
            if (row == null) continue;
            for (Unit u : row) {                     // перебираем юниты в строке
                if (u != null && u.isAlive()) {
                    alive.add(u);
                    occupied.add(pack(u.getxCoordinate(), u.getyCoordinate()));
                }
            }
        }

        /* =============================================================
           2. Определяем, кто может сделать шаг вперёд
        ============================================================= */
        int dy = isLeftArmyTarget ? -1 : +1; // куда сдвигаемся по Y

        List<Unit> result = new ArrayList<>();
        for (Unit u : alive) {
            int nx = u.getxCoordinate();      // X не меняется
            int ny = u.getyCoordinate() + dy; // Y + dir

            // Если клетки нет в occupied → она свободна
            if (!occupied.contains(pack(nx, ny))) {
                result.add(u);
            }
        }
        return result;
    }

    /* ----------------------------------------------------------------
       Упаковка пары координат (x,y) в одно 64-битное число:
       старшие 32 бита — X, младшие 32 — Y.
       Такой приём избавляет от создания объекта-ключа при
       каждой проверке занятости клетки.
     ---------------------------------------------------------------- */
    private static long pack(int x, int y) {
        return (((long) x) << 32) ^ (y & 0xffffffffL);
    }
}
