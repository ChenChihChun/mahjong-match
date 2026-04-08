package com.mahjong.match.game;

import java.util.List;

public class GameEngine {
    public Board board = new Board();
    public int levelNum;
    public long startTime;
    public long elapsedMs;
    public boolean running;
    public boolean completed;
    public boolean stuck;
    public int moveCount;
    public int hintCount;
    public int score;

    private Tile selectedTile;
    private GameCallback callback;

    public interface GameCallback {
        void onMatch(Tile a, Tile b);
        void onInvalidPair();
        void onComplete(int score, long timeMs, int moves);
        void onStuck();
        void onHint(Tile a, Tile b);
    }

    public void init(int levelNum, GameCallback callback) {
        this.levelNum = levelNum;
        this.callback = callback;
        this.moveCount = 0;
        this.hintCount = 0;
        this.score = 0;
        this.completed = false;
        this.stuck = false;
        this.selectedTile = null;
        this.elapsedMs = 0;

        int[][] positions = LevelData.getLayout(levelNum);
        long seed = levelNum * 12345L + 999L;
        board.build(positions, seed);
        startTime = System.currentTimeMillis();
        running = true;
    }

    public void tick() {
        if (running && !completed) {
            elapsedMs = System.currentTimeMillis() - startTime;
        }
    }

    public boolean selectTile(Tile tile) {
        if (!running || completed || tile.removed) return false;
        if (!board.isFree(tile)) return false;

        board.clearHighlights();

        if (selectedTile == null) {
            selectedTile = tile;
            tile.selected = true;
            return true;
        }

        if (selectedTile == tile) {
            selectedTile.selected = false;
            selectedTile = null;
            return true;
        }

        // Try to match
        if (selectedTile.matches(tile)) {
            selectedTile.removed = true;
            selectedTile.selected = false;
            tile.removed = true;
            moveCount++;

            Tile a = selectedTile;
            selectedTile = null;

            if (callback != null) callback.onMatch(a, tile);

            if (board.isComplete()) {
                completed = true;
                running = false;
                score = calcScore();
                if (callback != null) callback.onComplete(score, elapsedMs, moveCount);
            } else if (!board.hasValidMove()) {
                stuck = true;
                if (callback != null) callback.onStuck();
            }
            return true;
        } else {
            selectedTile.selected = false;
            if (callback != null) callback.onInvalidPair();
            selectedTile = tile;
            tile.selected = true;
            return false;
        }
    }

    public void showHint() {
        if (!running || completed) return;
        board.clearHighlights();
        board.clearSelection();
        selectedTile = null;

        Tile[] hint = board.findHint();
        if (hint != null) {
            hint[0].highlighted = true;
            hint[1].highlighted = true;
            hintCount++;
            if (callback != null) callback.onHint(hint[0], hint[1]);
        }
    }

    public void shuffle() {
        if (!running || completed) return;
        board.clearHighlights();
        board.clearSelection();
        selectedTile = null;

        // Re-assign tile types among remaining non-removed tiles
        List<Tile> remaining = new java.util.ArrayList<>();
        List<Integer> types = new java.util.ArrayList<>();
        List<Integer> subs = new java.util.ArrayList<>();
        for (Tile t : board.tiles) {
            if (!t.removed) {
                remaining.add(t);
                types.add(t.type);
                subs.add(t.subType);
            }
        }
        java.util.Collections.shuffle(types, new java.util.Random());
        java.util.Collections.shuffle(subs, new java.util.Random());
        // Re-pair: types must come in pairs for solvability
        // Simple approach: re-shuffle in pairs
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).type = types.get(i);
            remaining.get(i).subType = subs.get(i);
        }
        hintCount += 2; // penalty
    }

    private int calcScore() {
        // Base score by level
        int base = levelNum * 100;
        // Time bonus: under 60s = full, -1 per second over
        long secs = elapsedMs / 1000;
        int timeBonus = (int) Math.max(0, 300 - secs);
        // Hint penalty
        int hintPenalty = hintCount * 50;
        return Math.max(0, base + timeBonus - hintPenalty);
    }

    public String getTimeString() {
        long secs = elapsedMs / 1000;
        return String.format("%02d:%02d", secs / 60, secs % 60);
    }

    public Tile getSelectedTile() { return selectedTile; }
}
