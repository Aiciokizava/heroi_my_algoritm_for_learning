package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.GeneratePreset;

import java.util.*;

/**
 * Генератор стартовой армии-пресета.
 *
 * Алгоритм состоит из двух независимых частей:
 *  1) Комплектация — выбираем, сколько экземпляров каждого типа можем
 *     себе позволить, чтобы уложиться в budget (maxPoints) и при этом
 *     максимизировать выгоду юнита – Attack/Cost, затем Health/Cost.
 *     Используется простой жадный отбор из отсортированного списка
 *     кандидатов.  Конечность множества (≤ 4 типа × 11 шт) гарантирует
 *     оптимальность такой жадности – назад возвращаться не нужно.
 *
 *       Сложность: O(T·MAX_PER_TYPE·log N) в худшем (сортировка),
 *       где T – число уникальных типов (≤ 4), N ≈ T·11 = 44.
 *       На практике < 1 мс.
 *
 *  2) Расстановка — юниты ставятся линиями Y:
 *        X=0 – Archers  (дальний бой, тыл)
 *        X=1 – Infantry (Pikeman/Swordsman)
 *        X=2 – Knights  (тяжёлый фронт)
 *     На каждой строке может стоять 0…3 юнита, порядок всегда
 *     слева–направо Archer→Infantry→Knight.  Так стрелки не погибают
 *     в нулевой ход, а строй остаётся компактным.
 *
 *       Сложность: O(N) – один проход по спискам.
 */
public final class GeneratePresetImpl implements GeneratePreset {

    /** Ограничение из задания — не более 11 копий каждого типа. */
    private static final int MAX_PER_TYPE = 11;

    // ------------------------------------------------------------------ public
    @Override
    public Army generate(List<Unit> prototypes, int maxPoints) {

        Army army = new Army();

        // Базовые проверки входных данных
        if (prototypes == null || prototypes.isEmpty() || maxPoints <= 0) {
            army.setUnits(Collections.emptyList());
            army.setPoints(0);
            return army;
        }

        /* ---------- Шаг 1. Уникальные эталоны каждого типа ---------- */
        // HashMap переопределяет старое значение только если putIfAbsent(),
        // поэтому берём самый первый попавшийся прототип.
        Map<String, Unit> protoByType = new HashMap<>();
        for (Unit u : prototypes) if (u != null) protoByType.putIfAbsent(u.getUnitType(), u);

        /* ---------- Шаг 2. Формируем массив кандидатов ---------- */
        record Cand(Unit p, double atkC, double hpC) {}
        List<Cand> cand = new ArrayList<>();

        for (Unit p : protoByType.values()) {
            double a = (double) p.getBaseAttack() / p.getCost();
            double h = (double) p.getHealth()     / p.getCost();
            for (int i = 0; i < MAX_PER_TYPE; i++) cand.add(new Cand(p, a, h));
        }

        // Самый выгодный должен идти первым, сортируем DESC по atkC, hpC.
        cand.sort((c1, c2) -> {
            int cmp = Double.compare(c2.atkC, c1.atkC);
            if (cmp != 0) return cmp;
            cmp = Double.compare(c2.hpC, c1.hpC);
            if (cmp != 0) return cmp;
            return Integer.compare(c1.p.getCost(), c2.p.getCost()); // дешёвый предпочтительнее
        });

        /* ---------- Шаг 3. Жадный выбор до исчерпания бюджета ---------- */
        Map<String,Integer> counter = new HashMap<>();
        List<Unit> picked          = new ArrayList<>();
        int         spent          = 0;

        boolean progress = true;
        // Крутимся, пока удаётся положить хоть одного нового юнита.
        while (progress) {
            progress = false;
            for (Cand c : cand) {
                Unit u      = c.p;
                String type = u.getUnitType();

                if (counter.getOrDefault(type, 0) >= MAX_PER_TYPE) continue;
                if (spent + u.getCost() > maxPoints)               continue;

                // Берём юнита.
                counter.merge(type, 1, Integer::sum);
                picked.add(u);
                spent  += u.getCost();
                progress = true;
                break; // начинаем новый проход с самого начала списка
            }
        }

        /* ---------- Шаг 4. Расстановка по координатам ---------- */
        List<Unit> archers  = filterByType(picked, "Archer");
        List<Unit> infantry = filterByType(picked, "Pikeman", "Swordsman");
        List<Unit> knights  = filterByType(picked, "Knight");

        List<Unit> placed = new ArrayList<>(picked.size());
        int row = 0; // = Y

        // Заполняем строками до тех пор, пока какой-то список не опустеет
        for (int a = 0,i = 0,k = 0; a < archers.size() || i < infantry.size() || k < knights.size(); row++) {
            if (a < archers.size())  placed.add(cloneWithCoords(archers.get(a++),  0, row));
            if (i < infantry.size()) placed.add(cloneWithCoords(infantry.get(i++), 1, row));
            if (k < knights.size())  placed.add(cloneWithCoords(knights.get(k++),  2, row));
        }

        army.setUnits(placed);
        army.setPoints(spent);
        return army;
    }

    /** Возвращает подсписок по типам. */
    private static List<Unit> filterByType(List<Unit> src, String... allowed) {
        Set<String> ok = Set.of(allowed);
        List<Unit> out = new ArrayList<>();
        for (Unit u : src) if (ok.contains(u.getUnitType())) out.add(u);
        return out;
    }

    /** Клонируем прототип, меняя только имя и координаты. */
    private static Unit cloneWithCoords(Unit p, int x, int y) {
        String type = p.getUnitType();
        return new Unit(
                type + ' ' + (y + 1),            // уникальное имя: «Knight 3»
                type,
                p.getHealth(),
                p.getBaseAttack(),
                p.getCost(),
                p.getAttackType(),
                new HashMap<>(p.getAttackBonuses()),
                new HashMap<>(p.getDefenceBonuses()),
                x, y
        );
    }
}
