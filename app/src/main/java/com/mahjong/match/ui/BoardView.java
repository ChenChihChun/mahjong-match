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
    private final Bitmap[] flowerBitmaps = new Bitmap[4];
    private final Bitmap[] seasonBitmaps = new Bitmap[4];
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
        int[] fl = {R.drawable.tile_f_mei, R.drawable.tile_f_lan, R.drawable.tile_f_ju, R.drawable.tile_f_zhu};
        int[] se = {R.drawable.tile_s_spring, R.drawable.tile_s_summer, R.drawable.tile_s_autumn, R.drawable.tile_s_winter};
        for (int i = 0; i < 4; i++) {
            flowerBitmaps[i] = BitmapFactory.decodeResource(ctx.getResources(), fl[i], opts);
            seasonBitmaps[i] = BitmapFactory.decodeResource(ctx.getResources(), se[i], opts);
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

    // Tiles are drawn at real mahjong proportion (~70:100). To keep vertical
    // neighbours from covering each other, the vertical grid step is stretched
    // by TILE_ASPECT so rows are spaced by the full face height.
    private static final float TILE_ASPECT = 1.40f;       // H / W
    private static final float SIDE_DEPTH_FRAC  = 0.12f;  // thickness of tile side

    // Extra pixels per half-unit in the Y direction (so rows don't overlap).
    private float vHalf;

    private void calculateTileSize() {
        if (board == null || getWidth() == 0 || getHeight() == 0) return;

        layerShift = 5f;
        float margin = 8f;
        float maxLayerShift = MAX_LAYERS * layerShift;

        // Horizontal budget: REF_W_HALF half-units.
        // Vertical budget: REF_H_HALF half-units, each stretched by TILE_ASPECT.
        float su = Math.min(
            (getWidth()  - margin * 2 - maxLayerShift) / REF_W_HALF,
            (getHeight() - margin * 2 - maxLayerShift) / (REF_H_HALF * TILE_ASPECT)
        );
        tileW = su * 2f;
        tileH = tileW * TILE_ASPECT;
        vHalf = su * TILE_ASPECT;

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
        float boardPixH = (maxY - minY) * vHalf + totalShift;
        offsetX = (getWidth()  - boardPixW) / 2f - minX * su;
        offsetY = (getHeight() - boardPixH) / 2f + totalShift - minY * vHalf;
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
            // Hide tiles that have a tile directly above — the one on top
            // should completely cover them, no peek-through edges.
            if (board.hasCoverAbove(t)) continue;
            boolean free = board.isFree(t);

            float px = offsetX + t.x * half + t.z * layerShift;
            float py = offsetY + t.y * vHalf - t.z * layerShift;

            drawTile(canvas, t, px, py, free);
        }
    }

    private void drawTile(Canvas canvas, Tile t, float px, float py, boolean free) {
        float r = tileW * 0.12f; // corner radius
        float depth = tileW * SIDE_DEPTH_FRAC;

        // Drop shadow (soft, offset down-right)
        shadowPaint.setColor(0x66000000);
        canvas.drawRoundRect(
            new RectF(px + depth*0.6f, py + depth*1.2f,
                      px + tileW + depth*1.4f, py + tileH + depth*1.4f),
            r, r, shadowPaint);

        // Side / thickness: a larger rounded rect behind the face, offset down-right.
        // This is what gives the Shanghai-mahjong "block" look.
        Paint sidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sidePaint.setStyle(Paint.Style.FILL);
        sidePaint.setColor(0xFFB89566); // warm beige side
        canvas.drawRoundRect(
            new RectF(px + depth, py + depth,
                      px + tileW + depth, py + tileH + depth),
            r, r, sidePaint);

        // Face rect (the flat top of the tile)
        RectF face = new RectF(px, py, px + tileW, py + tileH);

        // Face fill — vertical gradient for subtle sheen
        int topCol, botCol;
        if (t.selected) {
            topCol = 0xFFFFF4B0; botCol = 0xFFEFD66A;
        } else if (t.highlighted) {
            topCol = 0xFFE8FFE0; botCol = 0xFFB0E8A8;
        } else if (free) {
            topCol = 0xFFFFF8E4; botCol = 0xFFE8D9AE;
        } else {
            topCol = 0xFFE6D7AC; botCol = 0xFFC2AC78;
        }
        tilePaint.setShader(new LinearGradient(px, py, px, py + tileH,
            topCol, botCol, Shader.TileMode.CLAMP));
        canvas.drawRoundRect(face, r, r, tilePaint);
        tilePaint.setShader(null);

        // Inner highlight ring (bevel)
        float inset = Math.max(1.5f, tileW * 0.025f);
        RectF innerRect = new RectF(px + inset, py + inset,
                                    px + tileW - inset, py + tileH - inset);
        Paint lightEdge = new Paint(Paint.ANTI_ALIAS_FLAG);
        lightEdge.setColor(0xFFFFFFFF);
        lightEdge.setStyle(Paint.Style.STROKE);
        lightEdge.setStrokeWidth(tileW * 0.05f);
        lightEdge.setAlpha(140);
        canvas.drawLine(innerRect.left + r*0.5f, innerRect.top,
                        innerRect.right - r*0.5f, innerRect.top, lightEdge);
        canvas.drawLine(innerRect.left, innerRect.top + r*0.5f,
                        innerRect.left, innerRect.bottom - r*0.5f, lightEdge);

        Paint darkEdge = new Paint(Paint.ANTI_ALIAS_FLAG);
        darkEdge.setColor(0xFF7A5E2A);
        darkEdge.setStyle(Paint.Style.STROKE);
        darkEdge.setStrokeWidth(tileW * 0.05f);
        darkEdge.setAlpha(120);
        canvas.drawLine(innerRect.left + r*0.5f, innerRect.bottom,
                        innerRect.right - r*0.5f, innerRect.bottom, darkEdge);
        canvas.drawLine(innerRect.right, innerRect.top + r*0.5f,
                        innerRect.right, innerRect.bottom - r*0.5f, darkEdge);

        // Outer border (selection / hint accent)
        borderPaint.setColor(t.selected ? 0xFFFFD700
                            : t.highlighted ? 0xFF00CC66
                            : 0xFF8A6A3A);
        borderPaint.setStrokeWidth(t.selected || t.highlighted ? tileW * 0.05f : 1.2f);
        canvas.drawRoundRect(face, r, r, borderPaint);

        drawTileContent(canvas, t, px, py, free);
    }

    private void drawTileContent(Canvas canvas, Tile t, float px, float py, boolean free) {
        float cx = px + tileW / 2f;
        float cy = py + tileH / 2f;

        Bitmap bmp = tileBitmaps.get(t.type);
        if (bmp == null && t.type == Tile.TYPE_FLOWER) bmp = flowerBitmaps[t.subType % 4];
        if (bmp == null && t.type == Tile.TYPE_SEASON) bmp = seasonBitmaps[t.subType % 4];
        if (bmp != null) {
            float insetX = tileW * 0.06f;
            float insetY = tileH * 0.06f;
            RectF dst = new RectF(px + insetX, py + insetY, px + tileW - insetX, py + tileH - insetY);
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
            emojiPaint.setTextSize(tileW * 0.80f);
            Paint.FontMetrics fm = emojiPaint.getFontMetrics();
            float textY = cy - (fm.ascent + fm.descent) / 2f;
            canvas.drawText(label, cx, textY, emojiPaint);
        } else {
            // Two characters (e.g. 一萬, 1竹, 1餅) — stack them vertically so
            // each glyph stays large instead of squishing side-by-side.
            emojiPaint.setTextSize(tileW * 0.55f);
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

        // Check top layers first; within same layer prefer the visually-front tile
        // (larger y draws on top because faces overflow below their cell).
        List<Tile> sorted = new ArrayList<>(board.tiles);
        sorted.sort((a, b) -> {
            if (a.z != b.z) return Integer.compare(b.z, a.z);
            if (a.y != b.y) return Integer.compare(b.y, a.y);
            return Integer.compare(b.x, a.x);
        });

        for (Tile t : sorted) {
            if (t.removed) continue;
            if (board.hasCoverAbove(t)) continue;
            float px = offsetX + t.x * half + t.z * layerShift;
            float py = offsetY + t.y * vHalf - t.z * layerShift;

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
