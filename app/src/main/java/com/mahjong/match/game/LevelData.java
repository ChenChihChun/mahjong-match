package com.mahjong.match.game;

/**
 * 100 level layouts. Each position is {z, y, x} in half-tile units.
 * Tile at (z, y, x) occupies a 2x2 footprint: x to x+1, y to y+1.
 * Free if: not covered (no tile at z+1 overlapping) AND (left OR right is open).
 *
 * Difficulty tiers:
 *   1-20:  Easy   (~36-72 tiles, 1-2 layers)
 *  21-50:  Medium (~80-108 tiles, 2-3 layers)
 *  51-80:  Hard   (~120-140 tiles, 3-4 layers)
 *  81-100: Expert (144 tiles, 4-5 layers, complex shapes)
 */
public class LevelData {

    public static int[][] getLayout(int level) {
        switch (level) {
            // ── EASY (1-20) ─────────────────────────────────────────────────
            case 1:  return cross(3);
            case 2:  return cross(4);
            case 3:  return diamond(3);
            case 4:  return line(18);
            case 5:  return line(24);
            case 6:  return square(4);
            case 7:  return square(5);
            case 8:  return cross(5);
            case 9:  return diamond(4);
            case 10: return line(36);
            case 11: return checkerboard(4, 4);
            case 12: return checkerboard(4, 5);
            case 13: return frame(4, 6);
            case 14: return frame(5, 7);
            case 15: return pyramid(3);
            case 16: return pyramid(4);
            case 17: return staircase(4);
            case 18: return staircase(5);
            case 19: return zigzag(5);
            case 20: return zigzag(6);
            // ── MEDIUM (21-50) ──────────────────────────────────────────────
            case 21: return pyramid(5);
            case 22: return turtle(1);
            case 23: return turtle(2);
            case 24: return diamond(5);
            case 25: return doubleCross(4);
            case 26: return frame(6, 8);
            case 27: return checkerboard(5, 6);
            case 28: return staircase(6);
            case 29: return twoLayer(cross(4));
            case 30: return twoLayer(diamond(4));
            case 31: return arrowRight();
            case 32: return arrowLeft();
            case 33: return hourglass();
            case 34: return spiral();
            case 35: return twoLayer(square(5));
            case 36: return twoLayer(checkerboard(4, 5));
            case 37: return columns(6);
            case 38: return columns(8);
            case 39: return flower4();
            case 40: return flower4b();
            case 41: return twoLayer(pyramid(4));
            case 42: return twoLayer(pyramid(5));
            case 43: return bridge();
            case 44: return castle();
            case 45: return twoLayer(cross(5));
            case 46: return twoLayer(frame(5, 7));
            case 47: return snake();
            case 48: return twoLayer(zigzag(6));
            case 49: return tripleRow(10);
            case 50: return tripleRow(12);
            // ── HARD (51-80) ────────────────────────────────────────────────
            case 51: return turtle(3);
            case 52: return threeLayer(cross(4));
            case 53: return threeLayer(diamond(4));
            case 54: return threeLayer(square(4));
            case 55: return dragon();
            case 56: return spider();
            case 57: return threeLayer(pyramid(4));
            case 58: return fortress();
            case 59: return threeLayer(checkerboard(4, 5));
            case 60: return twoLayer(turtle(2));
            case 61: return twoLayer(arrowRight());
            case 62: return twoLayer(hourglass());
            case 63: return twoLayer(spiral());
            case 64: return threeLayer(frame(5, 7));
            case 65: return twoLayer(bridge());
            case 66: return twoLayer(castle());
            case 67: return twoLayer(snake());
            case 68: return threeLayer(zigzag(6));
            case 69: return twoLayer(tripleRow(10));
            case 70: return twoLayer(tripleRow(12));
            case 71: return twoLayer(flower4());
            case 72: return threeLayer(pyramid(5));
            case 73: return twoLayer(dragon());
            case 74: return twoLayer(spider());
            case 75: return twoLayer(fortress());
            case 76: return fullTurtle();
            case 77: return threeLayer(turtle(2));
            case 78: return twoLayer(doubleCross(4));
            case 79: return threeLayer(bridge());
            case 80: return threeLayer(castle());
            // ── EXPERT (81-100) ─────────────────────────────────────────────
            case 81: return fourLayer(cross(4));
            case 82: return fourLayer(diamond(4));
            case 83: return fourLayer(square(4));
            case 84: return fourLayer(pyramid(4));
            case 85: return fullTurtleClassic();
            case 86: return threeLayer(dragon());
            case 87: return threeLayer(spider());
            case 88: return threeLayer(fortress());
            case 89: return fourLayer(frame(5, 7));
            case 90: return twoLayer(fullTurtle());
            case 91: return threeLayer(flower4());
            case 92: return fourLayer(zigzag(6));
            case 93: return fourLayer(tripleRow(10));
            case 94: return threeLayer(doubleCross(4));
            case 95: return fourLayer(bridge());
            case 96: return fourLayer(castle());
            case 97: return threeLayer(fullTurtle());
            case 98: return fiveLayer(cross(3));
            case 99: return fiveLayer(diamond(3));
            case 100: return grandmaster();
            default: return turtle(1);
        }
    }

    public static String getLevelName(int level) {
        if (level <= 20)  return "初級 " + level;
        if (level <= 50)  return "中級 " + (level - 20);
        if (level <= 80)  return "高級 " + (level - 50);
        return "大師 " + (level - 80);
    }

    public static String getDifficultyLabel(int level) {
        if (level <= 20)  return "簡單";
        if (level <= 50)  return "中等";
        if (level <= 80)  return "困難";
        return "大師";
    }

    // ── Layout Generators ────────────────────────────────────────────────────

    // Cross/Plus shape
    private static int[][] cross(int arm) {
        java.util.List<int[]> pos = new java.util.ArrayList<>();
        int cx = arm * 2, cy = arm * 2;
        // Horizontal bar
        for (int x = 0; x <= arm * 4; x += 2) pos.add(new int[]{0, cy, x});
        // Vertical bar (skip center already added)
        for (int y = 0; y <= arm * 4; y += 2) {
            if (y != cy) pos.add(new int[]{0, y, cx});
        }
        return ensureEven(pos);
    }

    private static int[][] diamond(int r) {
        java.util.List<int[]> pos = new java.util.ArrayList<>();
        int cx = r * 2, cy = r * 2;
        for (int y = 0; y <= r * 4; y += 2) {
            int dist = Math.abs(y - cy);
            int hw = (r * 2 - dist);
            for (int x = cx - hw; x <= cx + hw; x += 2) {
                pos.add(new int[]{0, y, x});
            }
        }
        return ensureEven(pos);
    }

    private static int[][] line(int count) {
        java.util.List<int[]> pos = new java.util.ArrayList<>();
        for (int x = 0; x < count * 2; x += 2) pos.add(new int[]{0, 0, x});
        return ensureEven(pos);
    }

    private static int[][] square(int size) {
        java.util.List<int[]> pos = new java.util.ArrayList<>();
        for (int y = 0; y < size * 2; y += 2)
            for (int x = 0; x < size * 2; x += 2)
                pos.add(new int[]{0, y, x});
        return ensureEven(pos);
    }

    private static int[][] checkerboard(int rows, int cols) {
        java.util.List<int[]> pos = new java.util.ArrayList<>();
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if ((r + c) % 2 == 0) pos.add(new int[]{0, r * 2, c * 2});
        return ensureEven(pos);
    }

    private static int[][] frame(int rows, int cols) {
        java.util.List<int[]> pos = new java.util.ArrayList<>();
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (r == 0 || r == rows-1 || c == 0 || c == cols-1)
                    pos.add(new int[]{0, r * 2, c * 2});
        return ensureEven(pos);
    }

    private static int[][] pyramid(int base) {
        java.util.List<int[]> pos = new java.util.ArrayList<>();
        for (int layer = 0; layer < base; layer++) {
            int width = base - layer;
            int xOffset = layer;
            for (int x = 0; x < width; x++)
                pos.add(new int[]{layer, layer, (xOffset + x) * 2});
        }
        return ensureEven(pos);
    }

    private static int[][] staircase(int steps) {
        java.util.List<int[]> pos = new java.util.ArrayList<>();
        for (int s = 0; s < steps; s++) {
            for (int y = s; y < steps; y++)
                pos.add(new int[]{0, y * 2, s * 2});
        }
        return ensureEven(pos);
    }

    private static int[][] zigzag(int len) {
        java.util.List<int[]> pos = new java.util.ArrayList<>();
        for (int i = 0; i < len; i++) {
            int y = (i % 2 == 0) ? 0 : 2;
            pos.add(new int[]{0, y, i * 2});
        }
        return ensureEven(pos);
    }

    private static int[][] doubleCross(int arm) {
        java.util.List<int[]> pos = new java.util.ArrayList<>();
        int cx1 = arm, cy = arm * 2, cx2 = arm * 4;
        // Two crosses side by side
        for (int x = 0; x <= arm * 2; x += 2) pos.add(new int[]{0, cy, x});
        for (int y = 0; y <= arm * 4; y += 2) if (y != cy) pos.add(new int[]{0, y, cx1});
        for (int x = arm * 3; x <= arm * 5; x += 2) pos.add(new int[]{0, cy, x});
        for (int y = 0; y <= arm * 4; y += 2) if (y != cy) pos.add(new int[]{0, y, cx2});
        return ensureEven(dedup(pos));
    }

    private static int[][] arrowRight() {
        java.util.List<int[]> pos = new java.util.ArrayList<>();
        // Arrow pointing right
        int[][] pts = {
            {0,6,0},{0,4,2},{0,6,2},{0,8,2},{0,2,4},{0,4,4},{0,6,4},{0,8,4},{0,10,4},
            {0,0,6},{0,2,6},{0,4,6},{0,6,6},{0,8,6},{0,10,6},{0,12,6},
            {0,2,8},{0,4,8},{0,6,8},{0,8,8},{0,10,8},
            {0,4,10},{0,6,10},{0,8,10},
            {0,6,12}
        };
        for (int[] p : pts) pos.add(p);
        return ensureEven(pos);
    }

    private static int[][] arrowLeft() {
        int[][] right = arrowRight();
        int maxX = 0;
        for (int[] p : right) maxX = Math.max(maxX, p[2]);
        int[][] flipped = new int[right.length][3];
        for (int i = 0; i < right.length; i++) {
            flipped[i][0] = right[i][0];
            flipped[i][1] = right[i][1];
            flipped[i][2] = maxX - right[i][2];
        }
        return flipped;
    }

    private static int[][] hourglass() {
        java.util.List<int[]> pos = new java.util.ArrayList<>();
        int[][] rows = {{5,0},{4,0},{3,0},{2,0},{1,0},{0,4},{1,4},{2,4},{3,4},{4,4},{5,4}};
        // Top triangle
        for (int w = 5; w >= 1; w--) {
            int y = (5 - w) * 2;
            for (int x = (5-w)*2; x <= (5+w-1)*2; x += 2) pos.add(new int[]{0, y, x});
        }
        // Bottom triangle
        for (int w = 1; w <= 5; w++) {
            int y = (5 + (5 - w + 1)) * 2;
            for (int x = (5-w)*2; x <= (5+w-1)*2; x += 2) pos.add(new int[]{0, y, x});
        }
        // Middle single
        pos.add(new int[]{0, 10, 8});
        return ensureEven(dedup(pos));
    }

    private static int[][] spiral() {
        java.util.List<int[]> pos = new java.util.ArrayList<>();
        // 7x7 spiral
        boolean[][] used = new boolean[14][14];
        int x = 0, y = 0, dx = 1, dy = 0;
        int count = 0, total = 7 * 7;
        int cx = 0, cy = 0;
        for (int i = 0; i < total; i++) {
            pos.add(new int[]{0, cy, cx});
            used[cy][cx] = true;
            int nx = cx + dx * 2, ny = cy + dy * 2;
            if (nx < 0 || nx >= 14 || ny < 0 || ny >= 14 || used[ny][nx]) {
                int tmp = dx; dx = dy; dy = -tmp;
                nx = cx + dx * 2; ny = cy + dy * 2;
                if (nx < 0 || nx >= 14 || ny < 0 || ny >= 14 || used[ny][nx]) break;
            }
            cx = nx; cy = ny;
        }
        return ensureEven(pos);
    }

    private static int[][] columns(int count) {
        java.util.List<int[]> pos = new java.util.ArrayList<>();
        for (int c = 0; c < count; c++)
            for (int r = 0; r < 6; r++)
                pos.add(new int[]{0, r * 2, c * 3});
        return ensureEven(pos);
    }

    private static int[][] flower4() {
        java.util.List<int[]> pos = new java.util.ArrayList<>();
        // 4 petals + center
        int[][] centers = {{6,6},{0,6},{6,0},{12,6},{6,12}};
        for (int[] c : centers)
            for (int dy = -2; dy <= 2; dy += 2)
                for (int dx = -2; dx <= 2; dx += 2)
                    pos.add(new int[]{0, c[0]+dy, c[1]+dx});
        return ensureEven(dedup(pos));
    }

    private static int[][] flower4b() {
        // Larger flower
        java.util.List<int[]> pos = new java.util.ArrayList<>();
        int[][] centers = {{8,8},{0,8},{8,0},{16,8},{8,16}};
        for (int[] c : centers)
            for (int dy = -4; dy <= 4; dy += 2)
                for (int dx = -4; dx <= 4; dx += 2)
                    if (Math.abs(dy) + Math.abs(dx) <= 4)
                        pos.add(new int[]{0, c[0]+dy, c[1]+dx});
        return ensureEven(dedup(pos));
    }

    private static int[][] bridge() {
        java.util.List<int[]> pos = new java.util.ArrayList<>();
        // Two towers + bridge
        for (int y = 0; y < 8; y += 2) {
            pos.add(new int[]{0, y, 0}); pos.add(new int[]{0, y, 2});
        }
        for (int y = 0; y < 8; y += 2) {
            pos.add(new int[]{0, y, 12}); pos.add(new int[]{0, y, 14});
        }
        // Bridge top
        for (int x = 0; x <= 14; x += 2) pos.add(new int[]{0, 8, x});
        // Bridge bottom arches
        for (int x = 4; x <= 10; x += 2) pos.add(new int[]{0, 4, x});
        return ensureEven(dedup(pos));
    }

    private static int[][] castle() {
        java.util.List<int[]> pos = new java.util.ArrayList<>();
        // Outer walls
        for (int x = 0; x <= 12; x += 2) {
            pos.add(new int[]{0, 0, x});
            pos.add(new int[]{0, 12, x});
        }
        for (int y = 2; y <= 10; y += 2) {
            pos.add(new int[]{0, y, 0});
            pos.add(new int[]{0, y, 12});
        }
        // Inner ring
        for (int x = 4; x <= 8; x += 2) {
            pos.add(new int[]{0, 4, x});
            pos.add(new int[]{0, 8, x});
        }
        for (int y = 4; y <= 8; y += 2) {
            pos.add(new int[]{0, y, 4});
            pos.add(new int[]{0, y, 8});
        }
        // Center
        pos.add(new int[]{0, 6, 6});
        return ensureEven(dedup(pos));
    }

    private static int[][] snake() {
        java.util.List<int[]> pos = new java.util.ArrayList<>();
        int rows = 8, cols = 8;
        for (int r = 0; r < rows; r++) {
            if (r % 2 == 0) {
                for (int c = 0; c < cols; c++) pos.add(new int[]{0, r*2, c*2});
            } else {
                for (int c = cols-1; c >= 0; c--) pos.add(new int[]{0, r*2, c*2});
            }
        }
        return ensureEven(pos);
    }

    private static int[][] tripleRow(int cols) {
        java.util.List<int[]> pos = new java.util.ArrayList<>();
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < cols; c++)
                pos.add(new int[]{0, r*2, c*2});
        return ensureEven(pos);
    }

    // Classic turtle layout (simplified)
    private static int[][] turtle(int variant) {
        java.util.List<int[]> pos = new java.util.ArrayList<>();
        // Body: 10x6 grid with some holes
        for (int r = 0; r < 6; r++)
            for (int c = 0; c < 10; c++)
                pos.add(new int[]{0, r*2+2, c*2+2});
        // Head
        pos.add(new int[]{0, 6, 22}); pos.add(new int[]{0, 6, 24});
        // Tail
        pos.add(new int[]{0, 6, 0});
        // Legs
        pos.add(new int[]{0, 0, 4}); pos.add(new int[]{0, 0, 6});
        pos.add(new int[]{0, 0, 14}); pos.add(new int[]{0, 0, 16});
        pos.add(new int[]{0, 12, 4}); pos.add(new int[]{0, 12, 6});
        pos.add(new int[]{0, 12, 14}); pos.add(new int[]{0, 12, 16});
        if (variant >= 2) {
            // Layer 2 center
            for (int r = 1; r < 5; r++)
                for (int c = 1; c < 9; c++)
                    pos.add(new int[]{1, r*2+2, c*2+2});
        }
        return ensureEven(pos);
    }

    private static int[][] fullTurtle() {
        java.util.List<int[]> pos = new java.util.ArrayList<>();
        // Layer 0: base 14x8
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 14; c++)
                pos.add(new int[]{0, r*2, c*2});
        // Layer 1: 10x6 center
        for (int r = 1; r < 7; r++)
            for (int c = 1; c < 13; c++)
                pos.add(new int[]{1, r*2, c*2});
        // Layer 2: 6x4
        for (int r = 2; r < 6; r++)
            for (int c = 3; c < 11; c++)
                pos.add(new int[]{2, r*2, c*2});
        // Layer 3: center pile
        for (int r = 3; r < 5; r++)
            for (int c = 5; c < 9; c++)
                pos.add(new int[]{3, r*2, c*2});
        // Top single
        pos.add(new int[]{4, 7, 13});
        return ensureEven(pos);
    }

    private static int[][] fullTurtleClassic() {
        // 144-tile classic turtle
        java.util.List<int[]> pos = new java.util.ArrayList<>();
        // Layer 0 (ground): 12 rows x vary
        int[][] layer0 = {
            {2,0},{2,2},{2,4},{2,6},{2,8},{2,10},{2,12},{2,14},{2,16},{2,18},{2,20},{2,22},
            {4,0},{4,2},{4,4},{4,6},{4,8},{4,10},{4,12},{4,14},{4,16},{4,18},{4,20},{4,22},
            {6,0},{6,2},{6,4},{6,6},{6,8},{6,10},{6,12},{6,14},{6,16},{6,18},{6,20},{6,22},
            {8,0},{8,2},{8,4},{8,6},{8,8},{8,10},{8,12},{8,14},{8,16},{8,18},{8,20},{8,22},
            {10,0},{10,2},{10,4},{10,6},{10,8},{10,10},{10,12},{10,14},{10,16},{10,18},{10,20},{10,22},
            {12,0},{12,2},{12,4},{12,6},{12,8},{12,10},{12,12},{12,14},{12,16},{12,18},{12,20},{12,22},
            {6,-2},{8,-2},{6,24},{8,24}  // tail + head
        };
        for (int[] p : layer0) pos.add(new int[]{0, p[0], p[1]});
        // Layer 1: 8x4 center
        for (int r = 3; r <= 11; r += 2)
            for (int c = 3; c <= 19; c += 2)
                pos.add(new int[]{1, r, c});
        // Layer 2: 4x3
        for (int r = 5; r <= 9; r += 2)
            for (int c = 7; c <= 15; c += 2)
                pos.add(new int[]{2, r, c});
        // Layer 3: 2x2
        for (int r = 6; r <= 8; r += 2)
            for (int c = 10; c <= 12; c += 2)
                pos.add(new int[]{3, r, c});
        // Top
        pos.add(new int[]{4, 7, 11});
        return ensureEven(pos);
    }

    private static int[][] dragon() {
        java.util.List<int[]> pos = new java.util.ArrayList<>();
        // Dragon body (S-curve)
        int[][] body = {
            {0,0},{0,2},{0,4},{0,6},{0,8},{0,10},
            {2,10},{4,10},{4,8},{4,6},{4,4},{4,2},
            {6,2},{8,2},{8,4},{8,6},{8,8},{8,10},{8,12},
            {10,12},{12,12},{12,10},{12,8},{12,6}
        };
        for (int[] b : body) {
            pos.add(new int[]{0, b[0], b[1]});
            pos.add(new int[]{0, b[0]+1, b[1]});
        }
        // Head
        pos.add(new int[]{0, 0, 12}); pos.add(new int[]{0, 1, 12});
        pos.add(new int[]{0, 0, 14}); pos.add(new int[]{0, 1, 14});
        return ensureEven(dedup(pos));
    }

    private static int[][] spider() {
        java.util.List<int[]> pos = new java.util.ArrayList<>();
        // Center body
        for (int r = 4; r <= 8; r += 2)
            for (int c = 4; c <= 8; c += 2)
                pos.add(new int[]{0, r, c});
        // 8 legs
        int[][] legs = {{0,0},{0,2},{0,4},{0,6},{0,12},{0,14},{0,12,12},{14,0},{14,2},{14,4},{14,6},{14,12}};
        // 4 diagonal legs
        for (int i = 0; i <= 3; i++) pos.add(new int[]{0, i*2, i*2});
        for (int i = 0; i <= 3; i++) pos.add(new int[]{0, i*2, 12-i*2});
        for (int i = 0; i <= 3; i++) pos.add(new int[]{0, 8+i*2, i*2});
        for (int i = 0; i <= 3; i++) pos.add(new int[]{0, 8+i*2, 12-i*2});
        return ensureEven(dedup(pos));
    }

    private static int[][] fortress() {
        java.util.List<int[]> pos = new java.util.ArrayList<>();
        // Outer + inner walls + towers
        int size = 10;
        for (int x = 0; x <= size*2; x += 2) {
            pos.add(new int[]{0, 0, x});
            pos.add(new int[]{0, size*2, x});
        }
        for (int y = 2; y <= size*2-2; y += 2) {
            pos.add(new int[]{0, y, 0});
            pos.add(new int[]{0, y, size*2});
        }
        // Towers (corner 2x2)
        int[][] corners = {{0,0},{0,size*2-2},{size*2-2,0},{size*2-2,size*2-2}};
        for (int[] c : corners) {
            pos.add(new int[]{1, c[0], c[1]});
            pos.add(new int[]{1, c[0], c[1]+2});
            pos.add(new int[]{1, c[0]+2, c[1]});
            pos.add(new int[]{1, c[0]+2, c[1]+2});
        }
        // Inner fort
        for (int x = 6; x <= 14; x += 2) {
            pos.add(new int[]{0, 6, x});
            pos.add(new int[]{0, 14, x});
        }
        for (int y = 8; y <= 12; y += 2) {
            pos.add(new int[]{0, y, 6});
            pos.add(new int[]{0, y, 14});
        }
        pos.add(new int[]{0, 10, 10}); // center
        return ensureEven(dedup(pos));
    }

    // Layer stacking helpers
    private static int[][] twoLayer(int[][] base) {
        return stackLayers(base, 2);
    }

    private static int[][] threeLayer(int[][] base) {
        return stackLayers(base, 3);
    }

    private static int[][] fourLayer(int[][] base) {
        return stackLayers(base, 4);
    }

    private static int[][] fiveLayer(int[][] base) {
        return stackLayers(base, 5);
    }

    private static int[][] stackLayers(int[][] base, int layers) {
        java.util.List<int[]> pos = new java.util.ArrayList<>();
        // Find bounding box of base layer
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = 0, maxY = 0;
        for (int[] p : base) {
            minX = Math.min(minX, p[2]); maxX = Math.max(maxX, p[2]);
            minY = Math.min(minY, p[1]); maxY = Math.max(maxY, p[1]);
        }
        int cx = (minX + maxX) / 2, cy = (minY + maxY) / 2;

        for (int layer = 0; layer < layers; layer++) {
            // Each higher layer is smaller (shrink from edges)
            int shrink = layer * 2;
            for (int[] p : base) {
                if (p[2] >= minX + shrink && p[2] <= maxX - shrink &&
                    p[1] >= minY + shrink && p[1] <= maxY - shrink) {
                    pos.add(new int[]{layer, p[1], p[2]});
                }
            }
        }
        return ensureEven(pos);
    }

    // ── Grandmaster level (level 100) ────────────────────────────────────────
    private static int[][] grandmaster() {
        java.util.List<int[]> pos = new java.util.ArrayList<>();
        // Massive 144-tile expert layout combining multiple shapes

        // Base layer: full grid 16x8
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 16; c++)
                pos.add(new int[]{0, r*2, c*2});

        // Layer 1: center 12x6
        for (int r = 1; r < 7; r++)
            for (int c = 2; c < 14; c++)
                pos.add(new int[]{1, r*2, c*2});

        // Layer 2: center 8x4
        for (int r = 2; r < 6; r++)
            for (int c = 4; c < 12; c++)
                pos.add(new int[]{2, r*2, c*2});

        // Layer 3: center 4x2
        for (int r = 3; r < 5; r++)
            for (int c = 6; c < 10; c++)
                pos.add(new int[]{3, r*2, c*2});

        // Pinnacle
        pos.add(new int[]{4, 7, 15});

        return ensureEven(pos);
    }

    // ── Utilities ────────────────────────────────────────────────────────────

    private static int[][] ensureEven(java.util.List<int[]> pos) {
        if (pos.size() % 2 != 0 && !pos.isEmpty()) pos.remove(pos.size() - 1);
        return pos.toArray(new int[0][]);
    }

    private static java.util.List<int[]> dedup(java.util.List<int[]> pos) {
        java.util.List<int[]> result = new java.util.ArrayList<>();
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (int[] p : pos) {
            String key = p[0] + "," + p[1] + "," + p[2];
            if (seen.add(key)) result.add(p);
        }
        return result;
    }
}
