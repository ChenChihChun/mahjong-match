/**
 * GameEngine.js — Mahjong Solitaire game state and logic.
 * Ported from Java GameEngine.java.
 */
class GameEngine {
    constructor() {
        this.board = new Board();
        this.levelNum = 1;
        this.startTime = 0;
        this.elapsedMs = 0;
        this.running = false;
        this.completed = false;
        this.stuck = false;
        this.moveCount = 0;
        this.hintCount = 0;
        this.score = 0;
        this.selectedTile = null;
        this.state = 'running';

        // Callbacks
        this.onMatch = null;        // (tileA, tileB) => {}
        this.onInvalidPair = null;  // () => {}
        this.onComplete = null;     // (score, timeMs, moves) => {}
        this.onStuck = null;        // () => {}
        this.onHint = null;         // (tileA, tileB) => {}
    }

    /**
     * Initialize the engine for a given level.
     * @param {number} levelNum - Level number (1-100)
     */
    init(levelNum) {
        this.levelNum = levelNum;
        this.moveCount = 0;
        this.hintCount = 0;
        this.score = 0;
        this.completed = false;
        this.stuck = false;
        this.selectedTile = null;
        this.elapsedMs = 0;
        this.state = 'running';

        const positions = LevelData.getLayout(levelNum);
        const seed = levelNum * 12345 + 999;
        this.board.build(positions, seed);
        this.startTime = Date.now();
        this.running = true;
    }

    /** Update elapsed time. Call each frame or periodically. */
    tick() {
        if (this.running && !this.completed) {
            this.elapsedMs = Date.now() - this.startTime;
        }
    }

    /**
     * Handle tile selection.
     * @param {Tile} tile
     * @returns {boolean} true if selection was accepted
     */
    selectTile(tile) {
        if (!this.running || this.completed || tile.removed) return false;
        if (!this.board.isFree(tile)) return false;

        this.board.clearHighlights();

        // No current selection → select this tile
        if (this.selectedTile === null) {
            this.selectedTile = tile;
            tile.selected = true;
            return true;
        }

        // Same tile → deselect
        if (this.selectedTile === tile) {
            this.selectedTile.selected = false;
            this.selectedTile = null;
            return true;
        }

        // Try to match
        if (Tile.matches(this.selectedTile, tile)) {
            this.selectedTile.removed = true;
            this.selectedTile.selected = false;
            tile.removed = true;
            this.moveCount++;

            const a = this.selectedTile;
            this.selectedTile = null;

            if (this.onMatch) this.onMatch(a, tile);

            if (this.board.isComplete()) {
                this.completed = true;
                this.running = false;
                this.state = 'completed';
                this.score = this._calcScore();
                if (this.onComplete) this.onComplete(this.score, this.elapsedMs, this.moveCount);
            } else if (!this.board.hasValidMove()) {
                this.stuck = true;
                this.state = 'stuck';
                if (this.onStuck) this.onStuck();
            }
            return true;
        } else {
            // No match — switch selection
            this.selectedTile.selected = false;
            if (this.onInvalidPair) this.onInvalidPair();
            this.selectedTile = tile;
            tile.selected = true;
            return false;
        }
    }

    /** Show a hint: highlight a matching pair among free tiles. */
    showHint() {
        if (!this.running || this.completed) return;
        this.board.clearHighlights();
        this.board.clearSelection();
        this.selectedTile = null;

        const hint = this.board.findHint();
        if (hint !== null) {
            hint[0].highlighted = true;
            hint[1].highlighted = true;
            this.hintCount++;
            if (this.onHint) this.onHint(hint[0], hint[1]);
        }
    }

    /** Shuffle remaining tiles (solvable reshuffle). Adds hint penalty. */
    shuffle() {
        if (!this.running || this.completed) return;
        this.board.clearHighlights();
        this.board.clearSelection();
        this.selectedTile = null;

        const remaining = [];
        for (const t of this.board.tiles) {
            if (!t.removed) remaining.push(t);
        }
        if (remaining.length === 0) return;

        // Reshuffle remaining tiles in a solvable way
        this.board.reshuffleSolvable(remaining, new PRNG(Date.now()));
        this.hintCount += 2; // penalty
    }

    /**
     * Calculate score.
     * level * 100 + max(0, 300 - seconds) - hints * 50, min 0
     */
    _calcScore() {
        const base = this.levelNum * 100;
        const secs = Math.floor(this.elapsedMs / 1000);
        const timeBonus = Math.max(0, 300 - secs);
        const hintPenalty = this.hintCount * 50;
        return Math.max(0, base + timeBonus - hintPenalty);
    }

    /** Get formatted time string MM:SS. */
    getTimeString() {
        const secs = Math.floor(this.elapsedMs / 1000);
        const m = Math.floor(secs / 60);
        const s = secs % 60;
        return String(m).padStart(2, '0') + ':' + String(s).padStart(2, '0');
    }

    /** Get the currently selected tile (or null). */
    getSelectedTile() {
        return this.selectedTile;
    }
}

window.GameEngine = GameEngine;
