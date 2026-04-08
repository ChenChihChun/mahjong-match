package com.mahjong.match.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import com.mahjong.match.game.Board;
import com.mahjong.match.game.Tile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BoardView extends View {

    private Board board;
    private float tileW, tileH;
    private float offsetX, offsetY;
    private float shadowOffset;

    private Paint tilePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint subTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint selectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint hintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint freePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public interface TileClickListener {
        void onTileClicked(Tile tile);
    }
    private TileClickListener listener;

    public BoardView(Context context) {
        super(context);
        initPaints();
        setOnTouchListener((v, e) -> {
            if (e.getAction() == MotionEvent.ACTION_UP) {
                handleTouch(e.getX(), e.getY());
            }
            return true;
        });
    }

    private void initPaints() {
        shadowPaint.setColor(0x66000000);
        shadowPaint.setStyle(Paint.Style.FILL);

        tilePaint.setStyle(Paint.Style.FILL);

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1.5f);
        borderPaint.setColor(0xFF888888);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        subTextPaint.setTextAlign(Paint.Align.CENTER);

        selectedPaint.setStyle(Paint.Style.STROKE);
        selectedPaint.setStrokeWidth(3f);
        selectedPaint.setColor(0xFFFFD700);

        hintPaint.setStyle(Paint.Style.STROKE);
        hintPaint.setStrokeWidth(3f);
        hintPaint.setColor(0xFF00FF88);

        freePaint.setStyle(Paint.Style.FILL);
        freePaint.setColor(0xFFF5F0E8);
    }

    public void setBoard(Board board) {
        this.board = board;
        calculateTileSize();
        invalidate();
    }

    public void setTileClickListener(TileClickListener l) { this.listener = l; }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        calculateTileSize();
    }

    private void calculateTileSize() {
        if (board == null || getWidth() == 0) return;

        int maxX = 0, maxY = 0, maxZ = 0;
        for (Tile t : board.tiles) {
            maxX = Math.max(maxX, t.x + 2);
            maxY = Math.max(maxY, t.y + 2);
            maxZ = Math.max(maxZ, t.z);
        }

        float shadow = maxZ * 3;
        float availW = getWidth() * 0.95f - shadow;
        float availH = getHeight() * 0.95f - shadow;

        // Each tile is 2 half-units wide/tall
        float scaleX = availW / (maxX + 1);
        float scaleY = availH / (maxY + 1);
        float halfUnit = Math.min(scaleX, scaleY);

        tileW = halfUnit * 2;
        tileH = halfUnit * 2;
        shadowOffset = 3f;

        float boardW = maxX * halfUnit + tileW + shadow;
        float boardH = maxY * halfUnit + tileH + shadow;
        offsetX = (getWidth() - boardW) / 2f;
        offsetY = (getHeight() - boardH) / 2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (board == null) return;

        canvas.drawColor(0xFF1A1A2E);

        // Sort tiles: draw bottom layers first, then higher layers (painter's algorithm)
        List<Tile> sorted = new ArrayList<>(board.tiles);
        sorted.sort((a, b) -> {
            if (a.z != b.z) return Integer.compare(a.z, b.z);
            if (a.y != b.y) return Integer.compare(a.y, b.y);
            return Integer.compare(a.x, b.x);
        });

        float half = tileW / 2f;

        for (Tile t : sorted) {
            if (t.removed) continue;

            float px = offsetX + t.x * half;
            float py = offsetY + t.y * half;
            float sx = px + t.z * shadowOffset;
            float sy = py + t.z * shadowOffset;

            // Shadow
            canvas.drawRoundRect(new RectF(sx+2, sy+2, sx+tileW+2, sy+tileH+2), 4, 4, shadowPaint);

            // Tile background
            boolean free = board.isFree(t);
            if (t.selected) {
                tilePaint.setColor(0xFFFFEE99);
            } else if (t.highlighted) {
                tilePaint.setColor(0xFFCCFFCC);
            } else if (free) {
                tilePaint.setColor(0xFFF5F0E8);
            } else {
                tilePaint.setColor(0xFFCCBB99); // covered = darker
            }
            canvas.drawRoundRect(new RectF(sx, sy, sx+tileW, sy+tileH), 5, 5, tilePaint);

            // Border
            if (t.selected) {
                canvas.drawRoundRect(new RectF(sx, sy, sx+tileW, sy+tileH), 5, 5, selectedPaint);
            } else if (t.highlighted) {
                canvas.drawRoundRect(new RectF(sx, sy, sx+tileW, sy+tileH), 5, 5, hintPaint);
            } else {
                canvas.drawRoundRect(new RectF(sx, sy, sx+tileW, sy+tileH), 5, 5, borderPaint);
            }

            // Tile face
            drawTileLabel(canvas, t, sx, sy);
        }
    }

    private void drawTileLabel(Canvas canvas, Tile t, float x, float y) {
        String label = t.getLabel();
        int color = t.getLabelColor();
        if (!board.isFree(t)) color = 0xFF999999; // dim if not free

        float cx = x + tileW / 2f;
        float cy = y + tileH / 2f;

        if (label.contains("\n")) {
            // Two-line label (e.g. "一\n萬")
            String[] parts = label.split("\n");
            float fontSize = tileH * 0.3f;
            textPaint.setTextSize(fontSize);
            textPaint.setColor(color);
            canvas.drawText(parts[0], cx, cy - fontSize * 0.1f, textPaint);
            subTextPaint.setTextSize(fontSize * 0.75f);
            subTextPaint.setColor(color);
            subTextPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(parts[1], cx, cy + fontSize * 0.85f, subTextPaint);
        } else {
            float fontSize = tileH * 0.42f;
            // Circle tiles: slightly smaller
            if (label.length() == 1 && label.charAt(0) >= '①' && label.charAt(0) <= '⑨') {
                fontSize = tileH * 0.50f;
            }
            textPaint.setTextSize(fontSize);
            textPaint.setColor(color);
            canvas.drawText(label, cx, cy + fontSize * 0.35f, textPaint);
        }
    }

    private void handleTouch(float touchX, float touchY) {
        if (board == null || listener == null) return;

        float half = tileW / 2f;

        // Check from top layer down
        List<Tile> sorted = new ArrayList<>(board.tiles);
        sorted.sort((a, b) -> Integer.compare(b.z, a.z));

        for (Tile t : sorted) {
            if (t.removed) continue;

            float px = offsetX + t.x * half + t.z * shadowOffset;
            float py = offsetY + t.y * half + t.z * shadowOffset;

            if (touchX >= px && touchX <= px + tileW &&
                touchY >= py && touchY <= py + tileH) {
                listener.onTileClicked(t);
                invalidate();
                return;
            }
        }
    }

    public void refresh() { invalidate(); }
}
