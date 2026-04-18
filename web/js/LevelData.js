/**
 * LevelData.js — 100 fixed-footprint levels for Mahjong Solitaire.
 * Ported from Java LevelData.java.
 *
 * Coordinate convention: positions are [z, y, x] in half-tile units.
 * A tile at (z, y, x) occupies x..x+1, y..y+1.
 */
class LevelData {
    // Footprint bounds (half-units). Last legal tile sits at (W-2, H-2).
    static W = 18; // 9 tiles wide
    static H = 26; // 13 tiles tall

    /**
     * Get the layout for a given level (1-100).
     * Returns array of [z, y, x] positions.
     */
    static getLayout(level) {
        if (level === 100) return LevelData._grandmaster();
        // Every level is a deterministic randomised blob shape
        let baseTarget = 78 + (level - 1);              // 78 -> 176
        if (baseTarget > 150) baseTarget = 150;
        let layers = 4 + Math.floor((level - 1) / 9);   // 4 -> 15
        if (layers > 8) layers = 8;
        return LevelData._randomLayered(
            (0xC0FFEE ^ Math.imul(level, 0x9E3779B1)) >>> 0,
            baseTarget,
            layers
        );
    }

    static getLevelName(level) {
        if (level <= 20) return '初級 ' + level;
        if (level <= 50) return '中級 ' + (level - 20);
        if (level <= 80) return '高級 ' + (level - 50);
        return '大師 ' + (level - 80);
    }

    static getDifficultyLabel(level) {
        if (level <= 20) return '普通';
        if (level <= 50) return '困難';
        if (level <= 80) return '專家';
        return '大師';
    }

    // ── Layout primitives ─────────────────────────────────────────────────

    static _rect(rows, cols, x0, y0) {
        return LevelData._rectLayer(rows, cols, x0, y0, 0);
    }

    static _rectLayer(rows, cols, x0, y0, z) {
        const pos = [];
        for (let r = 0; r < rows; r++) {
            for (let c = 0; c < cols; c++) {
                const x = x0 + c * 2;
                const y = y0 + r * 2;
                if (LevelData._inBounds(x, y)) pos.push([z, y, x]);
            }
        }
        return LevelData._ensureEven(pos);
    }

    /** Centred plus / cross shape with the given arm length (in tiles). */
    static _cross(arm) {
        const pos = [];
        let cx = Math.floor(LevelData.W / 2) - 1; // 8
        let cy = Math.floor(LevelData.H / 2) - 1; // 12
        cx -= cx % 2;
        cy -= cy % 2;
        for (let i = -arm; i <= arm; i++) {
            const x = cx + i * 2;
            if (LevelData._inBounds(x, cy)) pos.push([0, cy, x]);
            if (i !== 0) {
                const y = cy + i * 2;
                if (LevelData._inBounds(cx, y)) pos.push([0, y, cx]);
            }
        }
        return LevelData._ensureEven(LevelData._dedup(pos));
    }

    /** Centred diamond of given radius (in tiles). */
    static _diamond(r) {
        const pos = [];
        const cx = 8, cy = 12;
        for (let dy = -r; dy <= r; dy++) {
            const span = r - Math.abs(dy);
            for (let dx = -span; dx <= span; dx++) {
                const x = cx + dx * 2;
                const y = cy + dy * 2;
                if (LevelData._inBounds(x, y)) pos.push([0, y, x]);
            }
        }
        return LevelData._ensureEven(LevelData._dedup(pos));
    }

    /** Hollow rectangular frame rows x cols, centred. */
    static _frame(rows, cols) {
        const pos = [];
        let x0 = Math.floor(LevelData.W / 2) - cols;
        let y0 = Math.floor(LevelData.H / 2) - rows;
        if (x0 < 0) x0 = 0;
        if (y0 < 0) y0 = 0;
        x0 -= x0 % 2;
        y0 -= y0 % 2;
        for (let r = 0; r < rows; r++) {
            for (let c = 0; c < cols; c++) {
                if (r === 0 || r === rows - 1 || c === 0 || c === cols - 1) {
                    const x = x0 + c * 2;
                    const y = y0 + r * 2;
                    if (LevelData._inBounds(x, y)) pos.push([0, y, x]);
                }
            }
        }
        return LevelData._ensureEven(LevelData._dedup(pos));
    }

    /** Centred checkerboard cols x rows (only the dark squares). */
    static _checker(cols, rows) {
        const pos = [];
        let x0 = Math.floor(LevelData.W / 2) - cols;
        let y0 = Math.floor(LevelData.H / 2) - rows;
        if (x0 < 0) x0 = 0;
        if (y0 < 0) y0 = 0;
        x0 -= x0 % 2;
        y0 -= y0 % 2;
        for (let r = 0; r < rows; r++) {
            for (let c = 0; c < cols; c++) {
                if (((r + c) & 1) === 0) {
                    const x = x0 + c * 2;
                    const y = y0 + r * 2;
                    if (LevelData._inBounds(x, y)) pos.push([0, y, x]);
                }
            }
        }
        return LevelData._ensureEven(LevelData._dedup(pos));
    }

    /** Centred pyramid: base x base on layer 0, shrinking each layer. */
    static _pyramid(base) {
        const pos = [];
        const cx = 8, cy = 12;
        for (let z = 0; z < base; z++) {
            const side = base - z;
            let x0 = cx - (side - 1);
            let y0 = cy - (side - 1);
            x0 -= ((x0 % 2) + 2) % 2; // ensure even (handle negatives)
            y0 -= ((y0 % 2) + 2) % 2;
            for (let r = 0; r < side; r++) {
                for (let c = 0; c < side; c++) {
                    const x = x0 + c * 2;
                    const y = y0 + r * 2;
                    if (LevelData._inBounds(x, y)) pos.push([z, y, x]);
                }
            }
        }
        return LevelData._ensureEven(LevelData._dedup(pos));
    }

    // ── Stacking helpers ──────────────────────────────────────────────────

    /**
     * Stack layers copies of the base layout, each layer shrinking
     * inward by shrinkPerLayer tiles on every side.
     */
    static _stack(base, layers, shrinkPerLayer) {
        let minX = Infinity, minY = Infinity;
        let maxX = 0, maxY = 0;
        for (const p of base) {
            if (p[2] < minX) minX = p[2];
            if (p[2] > maxX) maxX = p[2];
            if (p[1] < minY) minY = p[1];
            if (p[1] > maxY) maxY = p[1];
        }

        const baseSet = new Set();
        for (const p of base) baseSet.add(LevelData._key(0, p[1], p[2]));

        const pos = [];
        for (const p of base) pos.push([0, p[1], p[2]]);

        for (let z = 1; z < layers; z++) {
            const shrink = z * shrinkPerLayer * 2;
            const lx0 = minX + shrink, lx1 = maxX - shrink;
            const ly0 = minY + shrink, ly1 = maxY - shrink;
            if (lx1 - lx0 < 2 || ly1 - ly0 < 2) break;
            for (const p of base) {
                const x = p[2], y = p[1];
                if (x >= lx0 && x <= lx1 && y >= ly0 && y <= ly1
                    && baseSet.has(LevelData._key(0, y, x))) {
                    pos.push([z, y, x]);
                }
            }
        }
        return LevelData._ensureEven(LevelData._dedup(pos));
    }

    /**
     * Brick-style stacking: each upper layer is offset by 1 half-unit.
     */
    static _brickStack(base, layers) {
        let minX = Infinity, minY = Infinity;
        let maxX = 0, maxY = 0;
        for (const p of base) {
            if (p[2] < minX) minX = p[2];
            if (p[2] > maxX) maxX = p[2];
            if (p[1] < minY) minY = p[1];
            if (p[1] > maxY) maxY = p[1];
        }

        const pos = [];
        for (const p of base) pos.push([0, p[1], p[2]]);

        for (let z = 1; z < layers; z++) {
            const offset = z & 1;
            const lx0 = minX + 1 + (z - 1);
            const lx1 = maxX - 1 - (z - 1);
            const ly0 = minY + 1 + (z - 1);
            const ly1 = maxY - 1 - (z - 1);
            for (let y = ly0; y <= ly1; y += 2) {
                for (let x = lx0; x <= lx1; x += 2) {
                    const xx = x + offset;
                    const yy = y + offset;
                    if (LevelData._inBounds(xx, yy)) pos.push([z, yy, xx]);
                }
            }
        }
        return LevelData._ensureEven(LevelData._dedup(pos));
    }

    // ── Randomised blob generator ─────────────────────────────────────────

    /**
     * Grows a random connected blob of even-aligned tile cells starting at the
     * board centre, until target tiles are placed or no frontier cells remain.
     */
    static _randomBlob(rng, target) {
        const cells = [];
        const placed = new Set();
        const dirs = [[-2, 0], [2, 0], [0, -2], [0, 2]];

        const cx = 8, cy = 12;
        cells.push([cy, cx]);
        placed.add(cy * 100 + cx);

        const frontier = [];
        for (const d of dirs) frontier.push([cy + d[0], cx + d[1]]);

        while (cells.length < target && frontier.length > 0) {
            const maxIdx = Math.min(frontier.length, 6 + Math.floor(cells.length / 3));
            const idx = rng.nextInt(maxIdx);
            const pick = frontier.splice(idx, 1)[0];
            const y = pick[0], x = pick[1];
            if (x < 0 || y < 0 || x + 2 > LevelData.W || y + 2 > LevelData.H) continue;
            const k = y * 100 + x;
            if (placed.has(k)) continue;
            placed.add(k);
            cells.push([y, x]);
            for (const d of dirs) {
                const ny = y + d[0], nx = x + d[1];
                const nk = ny * 100 + nx;
                if (!placed.has(nk)) frontier.push([ny, nx]);
            }
        }
        return cells;
    }

    /**
     * Build a random blob base then stack organic upper layers.
     * Each upper layer keeps only interior cells whose 4 orthogonal
     * neighbours all exist, then removes a random fraction.
     */
    static _randomLayered(seed, baseTarget, maxLayers) {
        const rng = new PRNG(seed);
        const base = LevelData._randomBlob(rng, baseTarget);

        const pos = [];
        let prev = new Set();
        for (const c of base) {
            const y = c[0], x = c[1];
            pos.push([0, y, x]);
            prev.add(y * 100 + x);
        }

        for (let z = 1; z < maxLayers; z++) {
            const candidates = [];
            for (const k of prev) {
                const y = Math.floor(k / 100), x = k % 100;
                if (prev.has((y - 2) * 100 + x)
                    && prev.has((y + 2) * 100 + x)
                    && prev.has(y * 100 + (x - 2))
                    && prev.has(y * 100 + (x + 2))) {
                    candidates.push([y, x]);
                }
            }
            if (candidates.length < 4) break;

            // Shuffle candidates
            for (let i = candidates.length - 1; i > 0; i--) {
                const j = rng.nextInt(i + 1);
                const tmp = candidates[i];
                candidates[i] = candidates[j];
                candidates[j] = tmp;
            }

            // Keep most of the interior on low layers, fewer on high layers
            const keepFrac = Math.max(0.35, 0.90 - z * 0.08);
            let keep = Math.max(4, Math.round(candidates.length * keepFrac));
            keep = Math.min(keep, candidates.length);

            const next = new Set();
            for (let i = 0; i < keep; i++) {
                const c = candidates[i];
                const y = c[0], x = c[1];
                next.add(y * 100 + x);
                pos.push([z, y, x]);
            }
            prev = next;
            if (prev.size === 0) break;
        }
        return LevelData._ensureEven(pos);
    }

    // ── Grandmaster (level 100) ───────────────────────────────────────────

    /**
     * Final boss: full 13x9 base, clean stack 1-3, brick 4-6, pinnacle at 7.
     */
    static _grandmaster() {
        const base = LevelData._rect(13, 9, 0, 0);
        const pos = [];
        for (const p of base) pos.push([...p]);

        // Layers 1..3 — clean shrinking stack
        for (let z = 1; z <= 3; z++) {
            const shrink = z * 2;
            for (const p of base) {
                const x = p[2], y = p[1];
                if (x >= shrink && x <= 16 - shrink && y >= shrink && y <= 24 - shrink) {
                    pos.push([z, y, x]);
                }
            }
        }
        // Layers 4..6 — brick offset
        for (let z = 4; z <= 6; z++) {
            const shrink = z;
            for (let y = shrink * 2; y <= 24 - shrink * 2; y += 2) {
                for (let x = shrink * 2; x <= 16 - shrink * 2; x += 2) {
                    const xx = x + 1;
                    const yy = y + 1;
                    if (LevelData._inBounds(xx, yy)) pos.push([z, yy, xx]);
                }
            }
        }
        // Pinnacle on layer 7
        pos.push([7, 12, 8]);
        pos.push([7, 12, 9]);
        return LevelData._ensureEven(LevelData._dedup(pos));
    }

    // ── Utilities ─────────────────────────────────────────────────────────

    static _inBounds(x, y) {
        return x >= 0 && y >= 0 && x + 2 <= LevelData.W && y + 2 <= LevelData.H;
    }

    static _key(z, y, x) {
        return (z * 1000 + y) * 1000 + x;
    }

    static _ensureEven(pos) {
        if (pos.length % 2 !== 0 && pos.length > 0) pos.pop();
        return pos;
    }

    static _dedup(pos) {
        const result = [];
        const seen = new Set();
        for (const p of pos) {
            const k = LevelData._key(p[0], p[1], p[2]);
            if (!seen.has(k)) {
                seen.add(k);
                result.push(p);
            }
        }
        return result;
    }
}

window.LevelData = LevelData;
