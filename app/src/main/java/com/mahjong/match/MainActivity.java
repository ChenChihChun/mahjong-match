package com.mahjong.match;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Clean up old APKs from previous update
        UpdateChecker.cleanOldApks(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF1A1A2E);
        root.setGravity(Gravity.CENTER);
        root.setPadding(48, 80, 48, 80);

        // Title
        TextView title = new TextView(this);
        title.setText("麻將消消樂");
        title.setTextSize(42);
        title.setTextColor(0xFFFFD700);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 12);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("Mahjong Solitaire");
        sub.setTextSize(16);
        sub.setTextColor(0xFFAAAAAA);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 60);
        root.addView(sub);

        // Stats
        SharedPreferences prefs = getSharedPreferences("progress", MODE_PRIVATE);
        int maxLevel = prefs.getInt("max_level", 0);
        int totalScore = prefs.getInt("total_score", 0);

        LinearLayout statsBox = new LinearLayout(this);
        statsBox.setOrientation(LinearLayout.HORIZONTAL);
        statsBox.setBackgroundColor(0xFF16213E);
        statsBox.setPadding(32, 20, 32, 20);
        statsBox.setGravity(Gravity.CENTER);

        TextView lvlTv = new TextView(this);
        lvlTv.setText("已完成\n" + maxLevel + " 關");
        lvlTv.setTextColor(0xFF00D4FF);
        lvlTv.setTextSize(16);
        lvlTv.setGravity(Gravity.CENTER);
        lvlTv.setPadding(0, 0, 40, 0);

        TextView scoreTv = new TextView(this);
        scoreTv.setText("總分\n" + totalScore);
        scoreTv.setTextColor(0xFFFFD700);
        scoreTv.setTextSize(16);
        scoreTv.setGravity(Gravity.CENTER);

        statsBox.addView(lvlTv);
        statsBox.addView(scoreTv);
        root.addView(statsBox);

        addSpacer(root, 50);

        // Play button
        Button playBtn = makeButton("▶  開始遊戲", 0xFF0F3460, 0xFFFFD700);
        playBtn.setOnClickListener(v -> startActivity(new Intent(this, LevelSelectActivity.class)));
        root.addView(playBtn);

        addSpacer(root, 16);

        // Continue button
        int lastLevel = prefs.getInt("last_level", 1);
        if (maxLevel > 0) {
            Button contBtn = makeButton("↩  繼續 第" + lastLevel + "關", 0xFF0A2A4A, 0xFF00D4FF);
            contBtn.setOnClickListener(v -> {
                Intent intent = new Intent(this, GameActivity.class);
                intent.putExtra("level", lastLevel);
                startActivity(intent);
            });
            root.addView(contBtn);
            addSpacer(root, 16);
        }

        // Update button
        Button updateBtn = makeButton("⬆  檢查更新", 0xFF1A1A3A, 0xFF888888);
        updateBtn.setOnClickListener(v -> {
            updateBtn.setText("⬆  檢查中...");
            updateBtn.setEnabled(false);
            UpdateChecker.checkForUpdate(this, (hasUpdate, latestVer, latestCode, url, notes, error) -> {
                updateBtn.setText("⬆  檢查更新");
                updateBtn.setEnabled(true);
                if (error != null) {
                    android.widget.Toast.makeText(this, "檢查失敗: " + error, android.widget.Toast.LENGTH_SHORT).show();
                } else if (hasUpdate) {
                    UpdateChecker.showUpdateDialog(this, latestVer, latestCode, url, notes);
                } else {
                    UpdateChecker.showNoUpdateDialog(this);
                }
            });
        });
        root.addView(updateBtn);

        // Version
        addSpacer(root, 24);
        TextView ver = new TextView(this);
        ver.setText("v" + UpdateChecker.getCurrentVersionName(this) + "  ·  100 關");
        ver.setTextSize(12);
        ver.setTextColor(0xFF555577);
        ver.setGravity(Gravity.CENTER);
        root.addView(ver);

        setContentView(root);

        // Auto-check for updates silently on startup
        UpdateChecker.checkForUpdate(this, (hasUpdate, latestVer, latestCode, url, notes, error) -> {
            if (hasUpdate) {
                UpdateChecker.showUpdateDialog(this, latestVer, latestCode, url, notes);
            }
        });
    }

    private Button makeButton(String text, int bg, int fg) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextSize(18);
        btn.setTextColor(fg);
        btn.setBackgroundColor(bg);
        btn.setPadding(0, 24, 0, 24);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btn.setLayoutParams(lp);
        return btn;
    }

    private void addSpacer(LinearLayout parent, int dp) {
        android.view.View v = new android.view.View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(dp)));
        parent.addView(v);
    }

    private int dp(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
