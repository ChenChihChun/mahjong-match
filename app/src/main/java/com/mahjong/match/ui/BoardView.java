package com.mahjong.match.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;

import com.mahjong.match.game.Board;
import com.mahjong.match.game.Tile;

import java.util.ArrayList;
import java.util.List;

public class BoardView extends View {

    private Board board;
    private float tileW, tileH;
    private float offsetX, offsetY;
    private float layerShift;

    // Paints
    private final Paint bgPaint      = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tilePaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint coveredPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint edgePaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selPaint     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hintPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint emojiPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fallPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);

    public interface TileClickListener { void onTileClicked(Tile tile); }
    private TileClickListener listener;

    public BoardView(Context context) {
        super(context);
        initPaints();
        setOnTouchListener((v, e) -> {
            if (e.getAction() == MotionEvent.ACTION_UP) handleTouch(e.getX(), e.getY());
            return true;
        });
    }

    private void initPaints() {
        shadowPaint.setColor(0x55000000);
        shadowPaint.setStyle(Paint.Style.FILL);

        tilePaint.setStyle(Paint.Style.FILL);
        coveredPaint.setStyle(Paint.Style.FILL);
        coveredPaint.setColor(0xFFD4C49A);

        // 3D edge highlight (top/left lighter, bottom/right darker)
        edgePaint.setStyle(Paint.Style.FILL);

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1f);
        borderPaint.setColor(0xFF9A8060);

        selPaint.setStyle(Paint.Style.STROKE);
        selPaint.setStrokeWidth(4f);
        selPaint.setColor(0xFFFFD700);

        hintPaint.setStyle(Paint.Style.STROKE);
        hintPaint.setStrokeWidth(4f);
        hintPaint.setColor(0xFF00FF88);

        emojiPaint.setTextAlign(Paint.Align.CENTER);
        emojiPaint.setColor(Color.BLACK);

        fallPaint.setTextAlign(Paint.Align.CENTER);
        fallPaint.setFakeBoldText(true);
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
        if (board == null || getWidth() == 0 || getHeight() == 0) return;

        int maxX = 0, maxY = 0, maxZ = 0;
        for (Tile t : board.tiles) {
            if (!t.removed) {
                maxX = Math.max(maxX, t.x + 2);
                maxY = Math.max(maxY, t.y + 2);
                maxZ = Math.max(maxZ, t.z);
            }
        }
        if (maxX == 0) { maxX = 20; maxY = 16; }

        layerShift = 4f;
        float totalShift = maxZ * layerShift;
        float margin = 8f;
        float availW = getWidth()  - margin * 2 - totalShift;
        float availH = getHeight() - margin * 2 - totalShift;

        // half-unit size
        float su = Math.min(availW / (maxX + 1), availH / (maxY + 1));
        tileW = su * 2f;
        tileH = su * 2f;

        // Centre the board
        float boardPxW = maxX * su + tileW + totalShift;
        float boardPxH = maxY * su + tileH + totalShift;
        offsetX = (getWidth()  - boardPxW) / 2f + margin;
        offsetY = (getHeight() - boardPxH) / 2f + margin;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (board == null) return;

        canvas.drawColor(0xFF1A1A2E);

        float half = tileW / 2f;

        // Sort: lower layers first, then top-to-bottom, left-to-right
        List<Tile> sorted = new ArrayList<>(board.tiles);
        sorted.sort((a, b) -> {
            if (a.z != b.z) return Integer.compare(a.z, b.z);
            if (a.y != b.y) return Integer.compare(a.y, b.y);
            return Integer.compare(a.x, b.x);
        });

        for (Tile t : sorted) {
            if (t.removed) continue;
            boolean free = board.isFree(t);

            float px = offsetX + t.x * half + t.z * layerShift;
            float py = offsetY + t.y * half - t.z * layerShift; // layers go up visually

            drawTile(canvas, t, px, py, free);
        }
    }

    private void drawTile(Canvas canvas, Tile t, float px, float py, boolean free) {
        RectF rect = new RectF(px, py, px + tileW, py + tileH);
        float r = tileW * 0.1f; // corner radius

        // Drop shadow
        shadowPaint.setMaskFilter(null);
        canvas.drawRoundRect(new RectF(px+3, py+4, px+tileW+3, py+tileH+4), r, r, shadowPaint);

        // Tile body
        if (t.selected) {
            tilePaint.setColor(0xFFFFF0A0);
        } else if (t.highlighted) {
            tilePaint.setColor(0xFFE0FFE0);
        } else if (free) {
            tilePaint.setColor(0xFFF5EDD6); // ivory
        } else {
            tilePaint.setColor(0xFFD4C49A); // darker ivory when covered
        }
        canvas.drawRoundRect(rect, r, r, tilePaint);

        // 3D bevel: top+left light edge
        Paint lightEdge = new Paint(Paint.ANTI_ALIAS_FLAG);
        lightEdge.setColor(free ? 0xFFFFFFCC : 0xFFE8D8B0);
        lightEdge.setStyle(Paint.Style.STROKE);
        lightEdge.setStrokeWidth(tileW * 0.04f);
        canvas.drawLine(px+r, py+2, px+tileW-r, py+2, lightEdge); // top
        canvas.drawLine(px+2, py+r, px+2, py+tileH-r, lightEdge); // left

        // Bottom+right dark edge
        Paint darkEdge = new Paint(Paint.ANTI_ALIAS_FLAG);
        darkEdge.setColor(0xFF8B6E3A);
        darkEdge.setStyle(Paint.Style.STROKE);
        darkEdge.setStrokeWidth(tileW * 0.04f);
        canvas.drawLine(px+r, py+tileH-2, px+tileW-r, py+tileH-2, darkEdge); // bottom
        canvas.drawLine(px+tileW-2, py+r, px+tileW-2, py+tileH-r, darkEdge); // right

        // Border
        borderPaint.setColor(t.selected ? 0xFFFFD700 : t.highlighted ? 0xFF00CC66 : 0xFF9A8060);
        borderPaint.setStrokeWidth(t.selected || t.highlighted ? 3.5f : 1f);
        canvas.drawRoundRect(rect, r, r, borderPaint);

        // Tile content: emoji or fallback
        drawTileContent(canvas, t, px, py, free);
    }

    private void drawTileContent(Canvas canvas, Tile t, float px, float py, boolean free) {
        float cx = px + tileW / 2f;
        float cy = py + tileH / 2f;

        String emoji = t.getEmoji();
        float emojiSize = tileH * 0.62f;
        emojiPaint.setTextSize(emojiSize);

        // Try to draw emoji; it will render as the Unicode mahjong tile on supported devices
        float alpha = free ? 1f : 0.65f;
        emojiPaint.setAlpha((int)(255 * alpha));

        // Draw emoji centered
        Paint.FontMetrics fm = emojiPaint.getFontMetrics();
        float textY = cy - (fm.ascent + fm.descent) / 2f;
        canvas.drawText(emoji, cx, textY, emojiPaint);

        // Covered overlay dim
        if (!free && !t.selected && !t.highlighted) {
            Paint dim = new Paint();
            dim.setColor(0x33000000);
            dim.setStyle(Paint.Style.FILL);
            float r = tileW * 0.1f;
            canvas.drawRoundRect(new RectF(px, py, px+tileW, py+tileH), r, r, dim);
        }
    }

    private void handleTouch(float touchX, float touchY) {
        if (board == null || listener == null) return;

        float half = tileW / 2f;

        // Check top layers first
        List<Tile> sorted = new ArrayList<>(board.tiles);
        sorted.sort((a, b) -> Integer.compare(b.z, a.z));

        for (Tile t : sorted) {
            if (t.removed) continue;
            float px = offsetX + t.x * half + t.z * layerShift;
            float py = offsetY + t.y * half - t.z * layerShift;

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
