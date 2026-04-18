/**
 * Board.js — Mahjong Solitaire board logic.
 * Ported from Java Board.java.
 */
class Board {
    constructor() {
        this.tiles = [];
    }

    /**
     * Build a guaranteed-solvable board using backwards simulation.
     * @param {number[][]} positions - Array of [z, y, x] positions
     * @param {number} seed - Random seed for deterministic generation
     */
    build(positions, seed) {
        this.tiles = [];

        let n = positions.length;
        if (n % 2 !== 0) n--;

        // Create all tiles with placeholder type
        for (let i = 0; i < n; i++) {
            const pos = positions[i];
            const tile = new Tile(pos[0], pos[1], pos[2], 0);
            this.tiles.push(tile);
        }

        // Try solvable generation up to 20 times with different sub-seeds
        for (let attempt = 0; attempt < 20; attempt++) {
            const pool = this._buildTypePool(Math.floor(n / 2), new PRNG(seed + attempt * 3));
            if (this._assignSolvable([...this.tiles], pool, new PRNG(seed + attempt * 7919))) {
                return;
            }
        }
        // Fallback: random assignment
        this._assignRandom(n, seed);
    }

    /**
     * Reshuffle remaining tiles in a solvable way.
     * @param {Tile[]} remainingTiles
     * @param {PRNG} rng
     */
    reshuffleSolvable(remainingTiles, rng) {
        if (remainingTiles.length < 2) return;

        // Collect type pool from current remaining tiles
        const typePool = remainingTiles.map(t => [t.type, t.subType]);
        Board._shuffle(typePool, rng);

        // Try solvable assignment up to 20 times
        for (let attempt = 0; attempt < 20; attempt++) {
            const pool = [...typePool.map(tp => [...tp])];
            const rng2 = new PRNG(rng.next());
            Board._shuffle(pool, rng2);
            const rng3 = new PRNG(rng.next());
            if (this._assignSolvable([...remainingTiles], pool, rng3)) {
                return;
            }
        }
        // Fallback: just assign in shuffled order
        this._assignPoolToTiles(remainingTiles, typePool, rng);
    }

    /**
     * Core solvable assignment algorithm.
     * Simulates game play: repeatedly finds free tiles and assigns them pair types.
     */
    _assignSolvable(tileList, typePool, rng) {
        // Reset type assignments for simulation
        for (const t of tileList) {
            t.type = 0;
            t.removed = false;
        }

        const working = [...tileList];
        let poolIdx = 0;

        while (working.length >= 2) {
            const free = [];
            for (const t of working) {
                if (this.isFree(t)) free.push(t);
            }

            if (free.length < 2) {
                // Stuck — reset and signal failure
                for (const t of tileList) t.removed = false;
                return false;
            }

            Board._shuffle(free, rng);
            const a = free[0];
            const b = free[1];

            const tp = typePool[poolIdx % typePool.length];
            a.type = tp[0]; a.subType = tp[1];
            b.type = tp[0]; b.subType = tp[1];
            poolIdx++;

            // Mark removed to simulate this pair being matched
            a.removed = true;
            b.removed = true;
            working.splice(working.indexOf(a), 1);
            working.splice(working.indexOf(b), 1);
        }

        // Success — reset removed flags
        for (const t of tileList) t.removed = false;
        return true;
    }

    /** Assign type pool to tiles directly (fallback). */
    _assignPoolToTiles(tileList, typePool, rng) {
        const shuffled = [...tileList];
        Board._shuffle(shuffled, rng);
        for (let i = 0; i < shuffled.length && i < typePool.length; i++) {
            shuffled[i].type = typePool[i][0];
            shuffled[i].subType = typePool[i][1];
        }
        for (const t of tileList) t.removed = false;
    }

    /** Random assignment fallback for build(). */
    _assignRandom(n, seed) {
        const rng = new PRNG(seed + 999999);
        const typePool = this._buildTypePool(Math.floor(n / 2), rng);
        const selected = [];
        for (const tp of typePool) {
            selected.push([...tp]);
            selected.push([...tp]);
        }
        Board._shuffle(selected, rng);
        for (let i = 0; i < this.tiles.length && i < selected.length; i++) {
            this.tiles[i].type = selected[i][0];
            this.tiles[i].subType = selected[i][1];
        }
    }

    /** Build a shuffled type pool with the given number of pairs. */
    _buildTypePool(pairs, rng) {
        const pool = [];
        // 34 regular types (0..33), each appears twice
        for (let t = 0; t <= 33; t++) {
            pool.push([t, 0]);
            pool.push([t, 0]);
        }
        // Flowers (4 unique subtypes)
        pool.push([Tile.TYPE_FLOWER, Tile.FLOWER_MEI]);
        pool.push([Tile.TYPE_FLOWER, Tile.FLOWER_LAN]);
        pool.push([Tile.TYPE_FLOWER, Tile.FLOWER_JU]);
        pool.push([Tile.TYPE_FLOWER, Tile.FLOWER_ZHU]);
        // Seasons (4 unique subtypes)
        pool.push([Tile.TYPE_SEASON, Tile.SEASON_SPRING]);
        pool.push([Tile.TYPE_SEASON, Tile.SEASON_SUMMER]);
        pool.push([Tile.TYPE_SEASON, Tile.SEASON_AUTUMN]);
        pool.push([Tile.TYPE_SEASON, Tile.SEASON_WINTER]);

        Board._shuffle(pool, rng);

        // Return exactly 'pairs' entries (cycle if board is larger than standard)
        const result = [];
        for (let i = 0; i < pairs; i++) {
            result.push(pool[i % pool.length]);
        }
        return result;
    }

    // ── Free / blocked / covered checks ───────────────────────────────────

    /**
     * A tile is "free" if:
     * 1. Not removed
     * 2. Not covered (no tile on top overlapping it)
     * 3. Free on left OR free on right (not sandwiched)
     */
    isFree(tile) {
        if (tile.removed) return false;
        if (this._isCovered(tile)) return false;
        const blockedLeft = this._isBlockedLeft(tile);
        const blockedRight = this._isBlockedRight(tile);
        return !blockedLeft || !blockedRight;
    }

    /** Public version of isCovered so the renderer can check. */
    hasCoverAbove(tile) {
        return this._isCovered(tile);
    }

    /** Tile B covers tile A if: B.z == A.z+1 AND |B.x - A.x| <= 1 AND |B.y - A.y| <= 1 */
    _isCovered(a) {
        for (const b of this.tiles) {
            if (b.removed || b === a) continue;
            if (b.z === a.z + 1 && Math.abs(b.x - a.x) <= 1 && Math.abs(b.y - a.y) <= 1) {
                return true;
            }
        }
        return false;
    }

    _isBlockedLeft(a) {
        for (const b of this.tiles) {
            if (b.removed || b === a) continue;
            if (b.z === a.z && b.x === a.x - 2 && Math.abs(b.y - a.y) <= 1) {
                return true;
            }
        }
        return false;
    }

    _isBlockedRight(a) {
        for (const b of this.tiles) {
            if (b.removed || b === a) continue;
            if (b.z === a.z && b.x === a.x + 2 && Math.abs(b.y - a.y) <= 1) {
                return true;
            }
        }
        return false;
    }

    /** Get all free (selectable) tiles. */
    getFreeTiles() {
        const free = [];
        for (const t of this.tiles) {
            if (!t.removed && this.isFree(t)) free.push(t);
        }
        return free;
    }

    /** Count of non-removed tiles. */
    getRemainingCount() {
        let count = 0;
        for (const t of this.tiles) {
            if (!t.removed) count++;
        }
        return count;
    }

    /** Are all tiles removed? */
    isComplete() {
        return this.getRemainingCount() === 0;
    }

    /** Returns a matching pair [tileA, tileB] from free tiles, or null if stuck. */
    findHint() {
        const free = this.getFreeTiles();
        for (let i = 0; i < free.length; i++) {
            for (let j = i + 1; j < free.length; j++) {
                if (Tile.matches(free[i], free[j])) {
                    return [free[i], free[j]];
                }
            }
        }
        return null;
    }

    /** Is there at least one valid move? */
    hasValidMove() {
        return this.findHint() !== null;
    }

    /** Clear all highlight flags. */
    clearHighlights() {
        for (const t of this.tiles) t.highlighted = false;
    }

    /** Clear all selection flags. */
    clearSelection() {
        for (const t of this.tiles) t.selected = false;
    }

    // ── Utility ───────────────────────────────────────────────────────────

    /** Fisher-Yates shuffle using seeded PRNG. */
    static _shuffle(arr, rng) {
        for (let i = arr.length - 1; i > 0; i--) {
            const j = rng.nextInt(i + 1);
            const tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
    }
}

/**
 * Simple seedable PRNG (mulberry32).
 * Replaces Java's java.util.Random for deterministic generation.
 */
class PRNG {
    constructor(seed) {
        // Convert to unsigned 32-bit integer
        this._state = seed >>> 0;
        if (this._state === 0) this._state = 1;
    }

    /** Returns a raw 32-bit unsigned integer. */
    next() {
        let z = (this._state += 0x6D2B79F5) >>> 0;
        z = Math.imul(z ^ (z >>> 15), z | 1) >>> 0;
        z = (z ^ (z + Math.imul(z ^ (z >>> 7), z | 61))) >>> 0;
        return (z ^ (z >>> 14)) >>> 0;
    }

    /** Returns an integer in [0, bound). */
    nextInt(bound) {
        if (bound <= 0) return 0;
        return this.next() % bound;
    }

    /** Returns a float in [0, 1). */
    nextFloat() {
        return this.next() / 4294967296;
    }
}

window.Board = Board;
window.PRNG = PRNG;
