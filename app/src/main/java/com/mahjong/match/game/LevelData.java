package com.mahjong.match.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 100 fixed-footprint levels.
 *
 * Hard constraints:
 *   - Every layout must fit within 18 × 26 half-units (9 × 13 tiles).
 *   - Tile size and canvas are fixed (see BoardView). Difficulty grows
 *     ONLY by stacking more layers and packing the footprint denser,
 *     never by spreading tiles across a larger area.
 *
 * Coordinate convention: positions are {z, y, x} in half-tile units.
 * A tile at (z, y, x) occupies x..x+1, y..y+1. Even-indexed positions
 * (0, 2, 4, …) give clean stacking; odd positions give brick-offset
 * stacking which covers four tiles below — handy for hard levels.
 */
public class LevelData {

    // Footprint bounds (half-units). Last legal tile sits at (W-2, H-2).
    public static final int W = 18; // 9 tiles wide
    public static final int H = 26; // 13 tiles tall

    public static int[][] getLayout(int level) {
        switch (level) {
            // ── EASY (1-20): single layer, sparse → moderate density ───────
            case 1:  return rect(2, 2, 7, 11);             // 4 tiles
            case 2:  return rect(2, 3, 6, 11);             // 6
            case 3:  return rect(3, 2, 7, 10);             // 6
            case 4:  return rect(2, 4, 5, 11);             // 8
            case 5:  return rect(3, 3, 6, 10);             // 9 → 8 (even)
            case 6:  return rect(2, 5, 4, 11);             // 10
            case 7:  return rect(3, 4, 5, 10);             // 12
            case 8:  return cross(2);                       // ~12
            case 9:  return rect(4, 3, 6, 9);              // 12
            case 10: return diamond(2);                     // ~12
            case 11: return rect(3, 5, 4, 10);             // 15 → 14
            case 12: return frame(4, 4);                    // 12
            case 13: return rect(4, 4, 5, 9);              // 16
            case 14: return cross(3);                       // ~20
            case 15: return diamond(3);                     // ~24
            case 16: return rect(4, 5, 4, 9);              // 20
            case 17: return checker(5, 5);                  // ~12
            case 18: return rect(5, 4, 5, 8);              // 20
            case 19: return frame(5, 5);                    // 16
            case 20: return rect(5, 5, 4, 8);              // 25 → 24

            // ── MEDIUM (21-50): 2 layers, medium-full footprint ────────────
            case 21: return stack(rect(4, 4, 5, 9), 2, 1);          // ~24
            case 22: return stack(rect(5, 4, 5, 8), 2, 1);          // ~30
            case 23: return stack(rect(5, 5, 4, 8), 2, 1);          // ~40
            case 24: return stack(diamond(3), 2, 1);                // ~36
            case 25: return stack(cross(3), 2, 1);                  // ~32
            case 26: return rect(6, 5, 4, 7);                       // 30
            case 27: return stack(rect(6, 5, 4, 7), 2, 1);          // ~48
            case 28: return rect(6, 6, 3, 7);                       // 36
            case 29: return stack(rect(6, 6, 3, 7), 2, 1);          // ~56
            case 30: return rect(7, 6, 3, 6);                       // 42
            case 31: return stack(rect(7, 6, 3, 6), 2, 1);          // ~64
            case 32: return diamond(4);                              // ~40
            case 33: return stack(diamond(4), 2, 1);                // ~60
            case 34: return cross(4);                                // ~28
            case 35: return stack(cross(4), 2, 1);                  // ~46
            case 36: return frame(6, 6);                             // 20
            case 37: return rect(7, 7, 2, 6);                       // 49 → 48
            case 38: return stack(rect(7, 7, 2, 6), 2, 1);          // ~74
            case 39: return pyramid(5);                              // ~55
            case 40: return rect(8, 6, 3, 5);                       // 48
            case 41: return stack(rect(8, 6, 3, 5), 2, 1);          // ~72
            case 42: return rect(8, 7, 2, 5);                       // 56
            case 43: return stack(rect(8, 7, 2, 5), 2, 1);          // ~84
            case 44: return checker(9, 13);                          // ~58
            case 45: return rect(9, 7, 2, 4);                       // 63 → 62
            case 46: return stack(rect(9, 7, 2, 4), 2, 1);          // ~94
            case 47: return cross(5);                                // ~44
            case 48: return diamond(5);                              // ~60
            case 49: return rect(10, 7, 2, 3);                      // 70
            case 50: return stack(rect(10, 7, 2, 3), 2, 1);         // ~106

            // ── HARD (51-80): 3-5 layers, dense full footprint ─────────────
            case 51: return stack(rect(5, 5, 4, 8), 3, 1);          // ~58
            case 52: return stack(rect(6, 6, 3, 7), 3, 1);          // ~80
            case 53: return stack(rect(7, 6, 3, 6), 3, 1);          // ~92
            case 54: return stack(rect(7, 7, 2, 6), 3, 1);          // ~108
            case 55: return stack(rect(8, 7, 2, 5), 3, 1);          // ~124
            case 56: return stack(rect(9, 7, 2, 4), 3, 1);          // ~140
            case 57: return stack(rect(10, 7, 2, 3), 3, 1);         // ~158
            case 58: return stack(diamond(4), 3, 1);                // ~80
            case 59: return stack(diamond(5), 3, 1);                // ~95
            case 60: return stack(cross(4), 3, 1);                  // ~64
            case 61: return stack(cross(5), 3, 1);                  // ~76
            case 62: return stack(rect(11, 7, 2, 2), 3, 1);         // ~170
            case 63: return stack(rect(11, 8, 1, 2), 3, 1);         // ~190
            case 64: return stack(rect(12, 8, 1, 1), 3, 1);         // ~210
            case 65: return stack(rect(5, 5, 4, 8), 4, 1);          // ~74
            case 66: return stack(rect(6, 6, 3, 7), 4, 1);          // ~100
            case 67: return stack(rect(7, 7, 2, 6), 4, 1);          // ~138
            case 68: return stack(rect(8, 7, 2, 5), 4, 1);          // ~158
            case 69: return stack(rect(9, 7, 2, 4), 4, 1);          // ~182
            case 70: return stack(rect(10, 7, 2, 3), 4, 1);         // ~206
            case 71: return brickStack(rect(7, 7, 2, 6), 3);        // brick offsets
            case 72: return brickStack(rect(8, 7, 2, 5), 3);
            case 73: return brickStack(rect(9, 7, 2, 4), 3);
            case 74: return stack(rect(11, 8, 1, 2), 4, 1);         // ~250
            case 75: return stack(rect(12, 8, 1, 1), 4, 1);         // ~270
            case 76: return stack(rect(7, 7, 2, 6), 5, 1);          // ~160
            case 77: return stack(rect(8, 7, 2, 5), 5, 1);          // ~185
            case 78: return stack(rect(9, 7, 2, 4), 5, 1);          // ~215
            case 79: return stack(rect(10, 7, 2, 3), 5, 1);         // ~245
            case 80: return stack(rect(11, 8, 1, 2), 5, 1);         // ~290

            // ── EXPERT (81-100): 5-8 layers, dense + brick overlaps ────────
            case 81:  return stack(rect(7, 7, 2, 6), 6, 1);          // ~180
            case 82:  return stack(rect(8, 7, 2, 5), 6, 1);          // ~210
            case 83:  return stack(rect(9, 7, 2, 4), 6, 1);          // ~245
            case 84:  return stack(rect(10, 7, 2, 3), 6, 1);         // ~280
            case 85:  return stack(rect(11, 8, 1, 2), 6, 1);         // ~330
            case 86:  return brickStack(rect(8, 7, 2, 5), 4);
            case 87:  return brickStack(rect(9, 7, 2, 4), 4);
            case 88:  return brickStack(rect(10, 7, 2, 3), 4);
            case 89:  return stack(rect(11, 8, 1, 2), 7, 1);         // ~360
            case 90:  return stack(rect(12, 8, 1, 1), 7, 1);         // ~380
            case 91:  return brickStack(rect(11, 8, 1, 2), 4);
            case 92:  return brickStack(rect(12, 8, 1, 1), 4);
            case 93:  return stack(rect(12, 9, 0, 1), 5, 1);         // full footprint
            case 94:  return stack(rect(12, 9, 0, 1), 6, 1);
            case 95:  return stack(rect(12, 9, 0, 1), 7, 1);
            case 96:  return stack(rect(13, 9, 0, 0), 5, 1);
            case 97:  return stack(rect(13, 9, 0, 0), 6, 1);
            case 98:  return stack(rect(13, 9, 0, 0), 7, 1);
            case 99:  return brickStack(rect(13, 9, 0, 0), 5);
            case 100: return grandmaster();

            default: return rect(2, 2, 7, 11);
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

    // ── Layout primitives ────────────────────────────────────────────────────

    /**
     * Rectangular grid of {@code rows × cols} tiles, anchored so its top-left
     * tile sits at half-unit ({@code x0}, {@code y0}). Layer 0.
     */
    private static int[][] rect(int rows, int cols, int x0, int y0) {
        return rectLayer(rows, cols, x0, y0, 0);
    }

    private static int[][] rectLayer(int rows, int cols, int x0, int y0, int z) {
        List<int[]> pos = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int x = x0 + c * 2;
                int y = y0 + r * 2;
                if (inBounds(x, y)) pos.add(new int[]{z, y, x});
            }
        }
        return ensureEven(pos);
    }

    /** Centred plus / cross shape with the given arm length (in tiles). */
    private static int[][] cross(int arm) {
        List<int[]> pos = new ArrayList<>();
        int cx = (W / 2) - 1; // 8
        int cy = (H / 2) - 1; // 12
        // Round to even half-units so coverage stays clean
        cx -= cx % 2;
        cy -= cy % 2;
        for (int i = -arm; i <= arm; i++) {
            int x = cx + i * 2;
            if (inBounds(x, cy)) pos.add(new int[]{0, cy, x});
            if (i != 0) {
                int y = cy + i * 2;
                if (inBounds(cx, y)) pos.add(new int[]{0, y, cx});
            }
        }
        return ensureEven(dedup(pos));
    }

    /** Centred diamond of given radius (in tiles). */
    private static int[][] diamond(int r) {
        List<int[]> pos = new ArrayList<>();
        int cx = 8, cy = 12;
        for (int dy = -r; dy <= r; dy++) {
            int span = r - Math.abs(dy);
            for (int dx = -span; dx <= span; dx++) {
                int x = cx + dx * 2;
                int y = cy + dy * 2;
                if (inBounds(x, y)) pos.add(new int[]{0, y, x});
            }
        }
        return ensureEven(dedup(pos));
    }

    /** Hollow rectangular frame {@code rows × cols}, centred. */
    private static int[][] frame(int rows, int cols) {
        List<int[]> pos = new ArrayList<>();
        int x0 = (W / 2) - cols;       // half-units (each tile = 2)
        int y0 = (H / 2) - rows;
        if (x0 < 0) x0 = 0;
        if (y0 < 0) y0 = 0;
        x0 -= x0 % 2; y0 -= y0 % 2;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (r == 0 || r == rows - 1 || c == 0 || c == cols - 1) {
                    int x = x0 + c * 2;
                    int y = y0 + r * 2;
                    if (inBounds(x, y)) pos.add(new int[]{0, y, x});
                }
            }
        }
        return ensureEven(dedup(pos));
    }

    /** Centred checkerboard {@code rows × cols} (only the dark squares). */
    private static int[][] checker(int cols, int rows) {
        List<int[]> pos = new ArrayList<>();
        int x0 = (W / 2) - cols;
        int y0 = (H / 2) - rows;
        if (x0 < 0) x0 = 0;
        if (y0 < 0) y0 = 0;
        x0 -= x0 % 2; y0 -= y0 % 2;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (((r + c) & 1) == 0) {
                    int x = x0 + c * 2;
                    int y = y0 + r * 2;
                    if (inBounds(x, y)) pos.add(new int[]{0, y, x});
                }
            }
        }
        return ensureEven(dedup(pos));
    }

    /** Centred pyramid: {@code base × base} on layer 0, shrinking each layer. */
    private static int[][] pyramid(int base) {
        List<int[]> pos = new ArrayList<>();
        int cx = 8, cy = 12;
        for (int z = 0; z < base; z++) {
            int side = base - z;
            int x0 = cx - (side - 1);
            int y0 = cy - (side - 1);
            x0 -= x0 % 2; y0 -= y0 % 2;
            for (int r = 0; r < side; r++) {
                for (int c = 0; c < side; c++) {
                    int x = x0 + c * 2;
                    int y = y0 + r * 2;
                    if (inBounds(x, y)) pos.add(new int[]{z, y, x});
                }
            }
        }
        return ensureEven(dedup(pos));
    }

    // ── Stacking helpers ─────────────────────────────────────────────────────

    /**
     * Stack {@code layers} copies of the base layout, each layer shrinking
     * inward by {@code shrinkPerLayer} tiles on every side. Higher layers
     * cover lower-layer tiles directly (clean stacking).
     */
    private static int[][] stack(int[][] base, int layers, int shrinkPerLayer) {
        // Find base bounding box
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = 0, maxY = 0;
        for (int[] p : base) {
            minX = Math.min(minX, p[2]); maxX = Math.max(maxX, p[2]);
            minY = Math.min(minY, p[1]); maxY = Math.max(maxY, p[1]);
        }

        Set<Long> baseSet = new HashSet<>();
        for (int[] p : base) baseSet.add(key(0, p[1], p[2]));

        List<int[]> pos = new ArrayList<>();
        for (int[] p : base) pos.add(new int[]{0, p[1], p[2]});

        for (int z = 1; z < layers; z++) {
            int shrink = z * shrinkPerLayer * 2; // 2 half-units per tile
            int lx0 = minX + shrink, lx1 = maxX - shrink;
            int ly0 = minY + shrink, ly1 = maxY - shrink;
            if (lx1 - lx0 < 2 || ly1 - ly0 < 2) break; // too small to keep going
            for (int[] p : base) {
                int x = p[2], y = p[1];
                if (x >= lx0 && x <= lx1 && y >= ly0 && y <= ly1
                        && baseSet.contains(key(0, y, x))) {
                    pos.add(new int[]{z, y, x});
                }
            }
        }
        return ensureEven(dedup(pos));
    }

    /**
     * Brick-style stacking: each upper layer is offset by 1 half-unit so that
     * one upper tile covers four lower tiles. Creates much harder unlock
     * dependencies than clean {@link #stack}.
     */
    private static int[][] brickStack(int[][] base, int layers) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = 0, maxY = 0;
        for (int[] p : base) {
            minX = Math.min(minX, p[2]); maxX = Math.max(maxX, p[2]);
            minY = Math.min(minY, p[1]); maxY = Math.max(maxY, p[1]);
        }

        List<int[]> pos = new ArrayList<>();
        for (int[] p : base) pos.add(new int[]{0, p[1], p[2]});

        // Build upper layers using offset half-positions
        for (int z = 1; z < layers; z++) {
            int offset = (z & 1); // alternate between 0 and 1 half-unit
            int lx0 = minX + 1 + (z - 1);
            int lx1 = maxX - 1 - (z - 1);
            int ly0 = minY + 1 + (z - 1);
            int ly1 = maxY - 1 - (z - 1);
            for (int y = ly0; y <= ly1; y += 2) {
                for (int x = lx0; x <= lx1; x += 2) {
                    int xx = x + offset;
                    int yy = y + offset;
                    if (inBounds(xx, yy)) pos.add(new int[]{z, yy, xx});
                }
            }
        }
        return ensureEven(dedup(pos));
    }

    // ── Grandmaster (level 100) ──────────────────────────────────────────────

    /**
     * Final boss: full 13×9 base, brick-offset layers all the way to z=7.
     * Maximum density the canvas allows.
     */
    private static int[][] grandmaster() {
        int[][] base = rect(13, 9, 0, 0);
        // Custom: clean stack 1-3, then brick 4-7
        List<int[]> pos = new ArrayList<>();
        for (int[] p : base) pos.add(p);

        // Layers 1..3 — clean shrinking stack
        for (int z = 1; z <= 3; z++) {
            int shrink = z * 2;
            for (int[] p : base) {
                int x = p[2], y = p[1];
                if (x >= shrink && x <= 16 - shrink && y >= shrink && y <= 24 - shrink) {
                    pos.add(new int[]{z, y, x});
                }
            }
        }
        // Layers 4..6 — brick offset
        for (int z = 4; z <= 6; z++) {
            int shrink = z;
            for (int y = shrink * 2; y <= 24 - shrink * 2; y += 2) {
                for (int x = shrink * 2; x <= 16 - shrink * 2; x += 2) {
                    int xx = x + 1;
                    int yy = y + 1;
                    if (inBounds(xx, yy)) pos.add(new int[]{z, yy, xx});
                }
            }
        }
        // Pinnacle on layer 7
        pos.add(new int[]{7, 12, 8});
        pos.add(new int[]{7, 12, 9});
        return ensureEven(dedup(pos));
    }

    // ── Utilities ────────────────────────────────────────────────────────────

    private static boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x + 2 <= W && y + 2 <= H;
    }

    private static long key(int z, int y, int x) {
        return ((long) z * 1000L + y) * 1000L + x;
    }

    private static int[][] ensureEven(List<int[]> pos) {
        if (pos.size() % 2 != 0 && !pos.isEmpty()) pos.remove(pos.size() - 1);
        return pos.toArray(new int[0][]);
    }

    private static List<int[]> dedup(List<int[]> pos) {
        List<int[]> result = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (int[] p : pos) {
            long k = key(p[0], p[1], p[2]);
            if (seen.add(k)) result.add(p);
        }
        return result;
    }
}
