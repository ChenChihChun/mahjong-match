package com.mahjong.match;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mahjong.match.game.LevelData;

public class LevelSelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("progress", MODE_PRIVATE);
        int maxCompleted = prefs.getInt("max_level", 0);
        int unlockedLevel = maxCompleted + 1;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF1A1A2E);

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setBackgroundColor(0xFF16213E);
        header.setPadding(dp(16), dp(16), dp(16), dp(16));
        header.setGravity(Gravity.CENTER_VERTICAL);

        Button back = new Button(this);
        back.setText("←");
        back.setTextSize(18);
        back.setTextColor(0xFFFFFFFF);
        back.setBackgroundColor(Color.TRANSPARENT);
        back.setOnClickListener(v -> finish());
        header.addView(back);

        TextView title = new TextView(this);
        title.setText("選擇關卡");
        title.setTextSize(22);
        title.setTextColor(0xFFFFD700);
        title.setPadding(dp(16), 0, 0, 0);
        header.addView(title);

        root.addView(header);

        // Difficulty sections
        ScrollView scroll = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(12), dp(12), dp(12), dp(24));

        addDifficultySection(content, "簡單  ⭐", 1, 20, unlockedLevel, prefs);
        addDifficultySection(content, "中等  ⭐⭐", 21, 50, unlockedLevel, prefs);
        addDifficultySection(content, "困難  ⭐⭐⭐", 51, 80, unlockedLevel, prefs);
        addDifficultySection(content, "大師  ⭐⭐⭐⭐", 81, 100, unlockedLevel, prefs);

        scroll.addView(content);
        LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        root.addView(scroll, scrollLp);

        setContentView(root);
    }

    private void addDifficultySection(LinearLayout parent, String label, int from, int to,
                                       int unlocked, SharedPreferences prefs) {
        // Section header
        TextView header = new TextView(this);
        header.setText(label);
        header.setTextSize(18);
        header.setTextColor(0xFF00D4FF);
        header.setPadding(dp(8), dp(16), dp(8), dp(8));
        parent.addView(header);

        // Grid of level buttons
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(5);
        grid.setPadding(0, 0, 0, dp(8));

        for (int level = from; level <= to; level++) {
            final int lvl = level;
            boolean completed = prefs.getInt("score_" + level, -1) >= 0;
            boolean locked = level > unlocked;

            Button btn = new Button(this);
            btn.setText(String.valueOf(level));
            btn.setTextSize(14);
            btn.setPadding(0, dp(12), 0, dp(12));

            if (completed) {
                btn.setTextColor(0xFF1A1A2E);
                btn.setBackgroundColor(0xFFFFD700);
            } else if (locked) {
                btn.setTextColor(0xFF555577);
                btn.setBackgroundColor(0xFF222244);
            } else {
                btn.setTextColor(0xFFFFFFFF);
                btn.setBackgroundColor(0xFF0F3460);
            }

            if (!locked) {
                btn.setOnClickListener(v -> {
                    Intent intent = new Intent(this, GameActivity.class);
                    intent.putExtra("level", lvl);
                    startActivity(intent);
                });
            } else {
                btn.setAlpha(0.5f);
            }

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            lp.setMargins(dp(4), dp(4), dp(4), dp(4));
            btn.setLayoutParams(lp);
            grid.addView(btn);
        }

        parent.addView(grid);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recreate to refresh completion status
        recreate();
    }

    private int dp(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
