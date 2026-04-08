package com.mahjong.match;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.mahjong.match.game.GameEngine;
import com.mahjong.match.game.LevelData;
import com.mahjong.match.game.Tile;
import com.mahjong.match.ui.BoardView;

public class GameActivity extends AppCompatActivity implements GameEngine.GameCallback {

    private GameEngine engine = new GameEngine();
    private BoardView boardView;
    private TextView timerTv, moveTv, remainTv, levelTv;
    private Handler handler = new Handler();
    private int levelNum;

    private Runnable timerTick = new Runnable() {
        @Override
        public void run() {
            engine.tick();
            timerTv.setText(engine.getTimeString());
            if (engine.running) handler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        levelNum = getIntent().getIntExtra("level", 1);

        // Save last played level
        getSharedPreferences("progress", MODE_PRIVATE).edit()
                .putInt("last_level", levelNum).apply();

        buildUI();
        startGame();
    }

    private void buildUI() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF1A1A2E);

        // Top bar
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setBackgroundColor(0xFF16213E);
        topBar.setPadding(dp(12), dp(10), dp(12), dp(10));
        topBar.setGravity(Gravity.CENTER_VERTICAL);

        Button backBtn = new Button(this);
        backBtn.setText("✕");
        backBtn.setTextSize(16);
        backBtn.setTextColor(0xFFAAAAAA);
        backBtn.setBackgroundColor(Color.TRANSPARENT);
        backBtn.setPadding(0, 0, dp(8), 0);
        backBtn.setOnClickListener(v -> confirmExit());
        topBar.addView(backBtn);

        levelTv = makeStatLabel("", 0xFFFFD700);
        topBar.addView(levelTv);

        View spacer1 = new View(this);
        spacer1.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f));
        topBar.addView(spacer1);

        timerTv = makeStatLabel("00:00", 0xFF00D4FF);
        topBar.addView(timerTv);

        View spacer2 = new View(this);
        spacer2.setLayoutParams(new LinearLayout.LayoutParams(dp(16), 1));
        topBar.addView(spacer2);

        moveTv = makeStatLabel("0步", 0xFFFFFFFF);
        topBar.addView(moveTv);

        View spacer3 = new View(this);
        spacer3.setLayoutParams(new LinearLayout.LayoutParams(dp(16), 1));
        topBar.addView(spacer3);

        remainTv = makeStatLabel("", 0xFFFF9900);
        topBar.addView(remainTv);

        root.addView(topBar);

        // Board
        boardView = new BoardView(this);
        LinearLayout.LayoutParams boardLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        root.addView(boardView, boardLp);

        // Bottom controls
        LinearLayout bottomBar = new LinearLayout(this);
        bottomBar.setOrientation(LinearLayout.HORIZONTAL);
        bottomBar.setBackgroundColor(0xFF16213E);
        bottomBar.setPadding(dp(12), dp(8), dp(12), dp(8));
        bottomBar.setGravity(Gravity.CENTER);

        Button hintBtn = makeControlButton("💡 提示");
        hintBtn.setOnClickListener(v -> {
            engine.showHint();
            boardView.refresh();
        });

        Button shuffleBtn = makeControlButton("🔀 重排");
        shuffleBtn.setOnClickListener(v -> {
            engine.shuffle();
            boardView.refresh();
            updateStats();
        });

        Button restartBtn = makeControlButton("↺ 重來");
        restartBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("重新開始")
                    .setMessage("確定要重新開始這一關？")
                    .setPositiveButton("確定", (d, w) -> startGame())
                    .setNegativeButton("取消", null)
                    .show();
        });

        bottomBar.addView(hintBtn);
        addSpacer(bottomBar, dp(16));
        bottomBar.addView(shuffleBtn);
        addSpacer(bottomBar, dp(16));
        bottomBar.addView(restartBtn);

        root.addView(bottomBar);
        setContentView(root);
    }

    private void startGame() {
        engine.init(levelNum, this);
        boardView.setBoard(engine.board);
        boardView.setTileClickListener(tile -> {
            engine.selectTile(tile);
            boardView.refresh();
            updateStats();
        });
        levelTv.setText("第 " + levelNum + " 關");
        updateStats();
        handler.removeCallbacks(timerTick);
        handler.post(timerTick);
    }

    private void updateStats() {
        moveTv.setText(engine.moveCount + "步");
        remainTv.setText(engine.board.getRemainingCount() + "牌");
    }

    // GameCallback
    @Override
    public void onMatch(Tile a, Tile b) {
        updateStats();
        boardView.refresh();
    }

    @Override
    public void onInvalidPair() {
        // Brief visual feedback via toast
        Toast.makeText(this, "不配對", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onComplete(int score, long timeMs, int moves) {
        handler.removeCallbacks(timerTick);
        saveProgress(score);

        String msg = String.format("🎉 完成！\n\n得分：%d\n時間：%s\n步數：%d",
                score, engine.getTimeString(), moves);

        new AlertDialog.Builder(this)
                .setTitle("第 " + levelNum + " 關完成！")
                .setMessage(msg)
                .setPositiveButton("下一關", (d, w) -> {
                    if (levelNum < 100) {
                        levelNum++;
                        getSharedPreferences("progress", MODE_PRIVATE).edit()
                                .putInt("last_level", levelNum).apply();
                        startGame();
                    } else {
                        Toast.makeText(this, "恭喜通關全部100關！", Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .setNeutralButton("選關卡", (d, w) -> finish())
                .setNegativeButton("再玩一次", (d, w) -> startGame())
                .setCancelable(false)
                .show();
    }

    @Override
    public void onStuck() {
        handler.removeCallbacks(timerTick);
        new AlertDialog.Builder(this)
                .setTitle("無法繼續")
                .setMessage("沒有可消除的牌了。\n要重排牌局或重新開始嗎？")
                .setPositiveButton("重排", (d, w) -> {
                    engine.shuffle();
                    boardView.refresh();
                    engine.running = true;
                    handler.post(timerTick);
                })
                .setNeutralButton("重新開始", (d, w) -> startGame())
                .setNegativeButton("退出", (d, w) -> finish())
                .setCancelable(false)
                .show();
    }

    @Override
    public void onHint(Tile a, Tile b) {
        boardView.refresh();
        Toast.makeText(this, "提示：綠色高亮的牌可配對", Toast.LENGTH_SHORT).show();
    }

    private void saveProgress(int score) {
        SharedPreferences prefs = getSharedPreferences("progress", MODE_PRIVATE);
        int prev = prefs.getInt("score_" + levelNum, -1);
        int maxLevel = prefs.getInt("max_level", 0);
        int totalScore = prefs.getInt("total_score", 0);

        SharedPreferences.Editor ed = prefs.edit();
        if (score > prev) ed.putInt("score_" + levelNum, score);
        if (levelNum > maxLevel) {
            ed.putInt("max_level", levelNum);
            totalScore += score;
            ed.putInt("total_score", totalScore);
        }
        ed.apply();
    }

    private void confirmExit() {
        new AlertDialog.Builder(this)
                .setTitle("離開遊戲")
                .setMessage("確定要離開這一關？")
                .setPositiveButton("離開", (d, w) -> finish())
                .setNegativeButton("繼續遊戲", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(timerTick);
    }

    private TextView makeStatLabel(String text, int color) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(14);
        tv.setTextColor(color);
        tv.setGravity(Gravity.CENTER_VERTICAL);
        return tv;
    }

    private Button makeControlButton(String text) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextSize(14);
        btn.setTextColor(0xFFFFFFFF);
        btn.setBackgroundColor(0xFF0F3460);
        btn.setPadding(dp(16), dp(8), dp(16), dp(8));
        return btn;
    }

    private void addSpacer(LinearLayout parent, int width) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(width, 1));
        parent.addView(v);
    }

    private int dp(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
