package com.mahjong.match.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;

import com.mahjong.match.R;
import com.mahjong.match.game.Board;
import com.mahjong.match.game.Tile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final Map<Integer, Bitmap> tileBitmaps = new HashMap<>();
    private final Paint bmpPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);

    public BoardView(Context context) {
        super(context);
        initPaints();
        loadTileBitmaps(context);
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

    private void loadTileBitmaps(Context ctx) {
        int[][] map = {
            {Tile.TYPE_CHAR_1, R.drawable.tile_1m}, {Tile.TYPE_CHAR_2, R.drawable.tile_2m},
            {Tile.TYPE_CHAR_3, R.drawable.tile_3m}, {Tile.TYPE_CHAR_4, R.drawable.tile_4m},
            {Tile.TYPE_CHAR_5, R.drawable.tile_5m}, {Tile.TYPE_CHAR_6, R.drawable.tile_6m},
            {Tile.TYPE_CHAR_7, R.drawable.tile_7m}, {Tile.TYPE_CHAR_8, R.drawable.tile_8m},
            {Tile.TYPE_CHAR_9, R.drawable.tile_9m},
            {Tile.TYPE_BAMB_1, R.drawable.tile_1s}, {Tile.TYPE_BAMB_2, R.drawable.tile_2s},
            {Tile.TYPE_BAMB_3, R.drawable.tile_3s}, {Tile.TYPE_BAMB_4, R.drawable.tile_4s},
            {Tile.TYPE_BAMB_5, R.drawable.tile_5s}, {Tile.TYPE_BAMB_6, R.drawable.tile_6s},
            {Tile.TYPE_BAMB_7, R.drawable.tile_7s}, {Tile.TYPE_BAMB_8, R.drawable.tile_8s},
            {Tile.TYPE_BAMB_9, R.drawable.tile_9s},
            {Tile.TYPE_CIRC_1, R.drawable.tile_1p}, {Tile.TYPE_CIRC_2, R.drawable.tile_2p},
            {Tile.TYPE_CIRC_3, R.drawable.tile_3p}, {Tile.TYPE_CIRC_4, R.drawable.tile_4p},
            {Tile.TYPE_CIRC_5, R.drawable.tile_5p}, {Tile.TYPE_CIRC_6, R.drawable.tile_6p},
            {Tile.TYPE_CIRC_7, R.drawable.tile_7p}, {Tile.TYPE_CIRC_8, R.drawable.tile_8p},
            {Tile.TYPE_CIRC_9, R.drawable.tile_9p},
            {Tile.TYPE_WIND_E, R.drawable.tile_1z}, {Tile.TYPE_WIND_S, R.drawable.tile_2z},
            {Tile.TYPE_WIND_W, R.drawable.tile_3z}, {Tile.TYPE_WIND_N, R.drawable.tile_4z},
            {Tile.TYPE_DRAG_B, R.drawable.tile_5z}, // 白
            {Tile.TYPE_DRAG_F, R.drawable.tile_6z}, // 發
            {Tile.TYPE_DRAG_Z, R.drawable.tile_7z}, // 中
        };
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        for (int[] pair : map) {
            Bitmap b = BitmapFactory.decodeResource(ctx.getResources(), pair[1], opts);
            if (b != null) tileBitmaps.put(pair[0], b);
        }
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

    // Fixed reference footprint — every level uses the same tile size.
    // Layouts are designed in LevelData to fit within these half-tile bounds.
    // 18 half-units = 9 tiles wide, 26 half-units = 13 tiles tall.
    private static final float REF_W_HALF = 18f;
    private static final float REF_H_HALF = 26f;
    private static final float MAX_LAYERS  = 8f;

    private void calculateTileSize() {
        if (board == null || getWidth() == 0 || getHeight() == 0) return;

        layerShift = 4f;
        float margin = 8f;
        float maxLayerShift = MAX_LAYERS * layerShift;

        // Fixed tile size: derived from reference footprint, NOT from current level extent.
        // This guarantees identical tile size across every level.
        float su = Math.min(
            (getWidth()  - margin * 2 - maxLayerShift) / REF_W_HALF,
            (getHeight() - margin * 2 - maxLayerShift) / REF_H_HALF
        );
        tileW = su * 2f;
        tileH = su * 2f;

        // Centre the actual extent of this level inside the canvas.
        int maxX = 0, maxY = 0, maxZ = 0;
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        for (Tile t : board.tiles) {
            if (!t.removed) {
                maxX = Math.max(maxX, t.x + 2);
                maxY = Math.max(maxY, t.y + 2);
                maxZ = Math.max(maxZ, t.z);
                minX = Math.min(minX, t.x);
                minY = Math.min(minY, t.y);
            }
        }
        if (minX == Integer.MAX_VALUE) { minX = 0; minY = 0; }

        float totalShift = maxZ * layerShift;
        float boardPixW = (maxX - minX) * su + totalShift;
        float boardPixH = (maxY - minY) * su + totalShift;
        offsetX = (getWidth()  - boardPixW) / 2f - minX * su;
        offsetY = (getHeight() - boardPixH) / 2f + totalShift - minY * su;
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

        Bitmap bmp = tileBitmaps.get(t.type);
        if (bmp != null) {
            float inset = tileW * 0.10f;
            RectF dst = new RectF(px + inset, py + inset, px + tileW - inset, py + tileH - inset);
            // Preserve aspect ratio (mahjim tiles are taller than wide).
            float bw = bmp.getWidth(), bh = bmp.getHeight();
            float dw = dst.width(), dh = dst.height();
            float scale = Math.min(dw / bw, dh / bh);
            float w = bw * scale, h = bh * scale;
            float left = dst.centerX() - w / 2f;
            float top  = dst.centerY() - h / 2f;
            RectF fitted = new RectF(left, top, left + w, top + h);
            bmpPaint.setAlpha(free ? 255 : 165);
            canvas.drawBitmap(bmp, null, fitted, bmpPaint);
            if (!free && !t.selected && !t.highlighted) {
                Paint dim = new Paint();
                dim.setColor(0x33000000);
                dim.setStyle(Paint.Style.FILL);
                float r = tileW * 0.1f;
                canvas.drawRoundRect(new RectF(px, py, px+tileW, py+tileH), r, r, dim);
            }
            return;
        }

        // Draw the tile label ourselves with bold coloured text + drop shadow
        // for a uniform 3-D look across every tile (Unicode mahjong glyphs
        // render inconsistently — only U+1F004 中 is in the emoji set on most
        // Android fonts; the rest are plain outline characters).
        String label = t.getFallbackLabel();
        int color = t.getSuitColor();
        float alpha = free ? 1f : 0.65f;
        int alphaInt = (int) (255 * alpha);

        emojiPaint.setColor(color);
        emojiPaint.setAlpha(alphaInt);
        emojiPaint.setFakeBoldText(true);
        emojiPaint.setShadowLayer(tileW * 0.06f, 1.5f, 2f, 0x55000000);

        if (label.length() <= 1) {
            // Single huge character (中, 發, 白, 東, 南, 西, 北, 梅, 春, …)
            emojiPaint.setTextSize(tileH * 0.78f);
            Paint.FontMetrics fm = emojiPaint.getFontMetrics();
            float textY = cy - (fm.ascent + fm.descent) / 2f;
            canvas.drawText(label, cx, textY, emojiPaint);
        } else {
            // Two characters (e.g. 一萬, 1竹, 1餅) — stack them vertically so
            // each glyph stays large instead of squishing side-by-side.
            emojiPaint.setTextSize(tileH * 0.52f);
            Paint.FontMetrics fm = emojiPaint.getFontMetrics();
            float lineH = fm.descent - fm.ascent;
            float baselineOffset = -(fm.ascent + fm.descent) / 2f;
            float topY = cy - lineH * 0.32f + baselineOffset;
            float botY = cy + lineH * 0.32f + baselineOffset;
            canvas.drawText(label.substring(0, 1), cx, topY, emojiPaint);
            canvas.drawText(label.substring(1), cx, botY, emojiPaint);
        }

        emojiPaint.clearShadowLayer();

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
