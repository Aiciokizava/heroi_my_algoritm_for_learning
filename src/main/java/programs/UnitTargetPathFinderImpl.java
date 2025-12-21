package programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.Edge;
import com.battle.heroes.army.programs.UnitTargetPathFinder;

import java.util.*;

/**
 * Поиск кратчайшего пути юнит-к-цели по клеточному полю.
 *
 * Размер поля фиксирован движком: X принадлежит [0..26], Y принадлежит [0..20] (27*21).
 * Двигаться можно в 8 направлениях
 * Клетка считается непроходимой, если на ней живой юнит,
 *   КРОМЕ стартовой и целевой клетки (на них стоять разрешено).
 * Алгоритм — стандартный BFS.  Благодаря малому размеру поля
 *   время ≈ O(27*21) ≈ 600 операций, память ≈ тех же порядков.
 *
 * Результат — список Edge, идущий от старта к цели (без стартовой
 * клетки, но с клеткой цели).  Пустой список = пути нет.
 */
public class UnitTargetPathFinderImpl implements UnitTargetPathFinder {

    /* ------------------------------------------------------------------  конфигурация поля */
    private static final int WIDTH  = 27;   // X: 0..26
    private static final int HEIGHT = 21;   // Y: 0..20

    /* 8 направлений (dx,dy) — порядок неважен, лишь бы покрывали окружение */
    private static final int[] DX = {-1,-1,-1, 0, 0, 1, 1, 1};
    private static final int[] DY = {-1, 0, 1,-1, 1,-1, 0, 1};

    /* ------------------------------------------------------------------  публичный API */
    @Override
    public List<Edge> getTargetPath(Unit attacker,
                                    Unit target,
                                    List<Unit> allUnits) {

        /* -------- валидация входа -------- */
        if (attacker == null || target == null) return Collections.emptyList();

        int sx = attacker.getxCoordinate();
        int sy = attacker.getyCoordinate();
        int tx = target.getxCoordinate();
        int ty = target.getyCoordinate();

        if (!inBounds(sx, sy) || !inBounds(tx, ty)) return Collections.emptyList();
        if (sx == tx && sy == ty)                   return Collections.emptyList(); // уже на месте

        /* --------------------------------------------------------------------------
           1. строим «карту занятости» blocked[x][y] = true, если там стоит живой юнит
              (кроме стартовой и целевой клетки)
         -------------------------------------------------------------------------- */
        boolean[][] blocked = new boolean[WIDTH][HEIGHT];
        if (allUnits != null) {
            for (Unit u : allUnits) {
                if (u == null || !u.isAlive()) continue;
                int x = u.getxCoordinate();
                int y = u.getyCoordinate();
                if (!inBounds(x, y)) continue;
                if ((x == sx && y == sy) || (x == tx && y == ty)) continue; // старт / цель проходимы
                blocked[x][y] = true;
            }
        }

        /* --------------------------------------------------------------------------
           2. BFS от старта, запоминая «откуда пришли» в prevX/prevY
         -------------------------------------------------------------------------- */
        int[][] prevX = new int[WIDTH][HEIGHT];
        int[][] prevY = new int[WIDTH][HEIGHT];
        for (int[] row : prevX) Arrays.fill(row, -1);   // -1 → клетка ещё не посещена
        for (int[] row : prevY) Arrays.fill(row, -1);

        ArrayDeque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{sx, sy});
        prevX[sx][sy] = sx;  // любое ненегативное значение помечает «посещено»
        prevY[sx][sy] = sy;

        boolean reached = false;

        while (!queue.isEmpty() && !reached) {
            int[] cur = queue.pollFirst();
            int cx = cur[0], cy = cur[1];

            for (int d = 0; d < 8; d++) {
                int nx = cx + DX[d];
                int ny = cy + DY[d];
                if (!inBounds(nx, ny))          continue; // за пределами поля
                if (blocked[nx][ny])            continue; // занята
                if (prevX[nx][ny] != -1)        continue; // уже посещали

                prevX[nx][ny] = cx;
                prevY[nx][ny] = cy;

                if (nx == tx && ny == ty) {
                    reached = true;
                    break; // выход из цикла направлений
                }
                queue.addLast(new int[]{nx, ny});
            }
        }

        /* --------------------------------------------------------------------------
           3. если цели недостигнуты — пути нет
         -------------------------------------------------------------------------- */
        if (!reached) return Collections.emptyList();

        /* --------------------------------------------------------------------------
           4. восстанавливаем путь «от цели к старту», затем реверс
         -------------------------------------------------------------------------- */
        List<Edge> path = new ArrayList<>();
        for (int x = tx, y = ty; !(x == sx && y == sy); ) {
            path.add(new Edge(x, y));          // стартовая клетка в список не входит
            int px = prevX[x][y];
            int py = prevY[x][y];
            x = px;
            y = py;
        }
        Collections.reverse(path);
        return path;
    }

    /* true, если координаты внутри игрового поля */
    private static boolean inBounds(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
    }
}
