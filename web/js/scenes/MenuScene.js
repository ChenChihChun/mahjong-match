class MenuScene extends Phaser.Scene {
    constructor() {
        super({ key: 'MenuScene' });
    }

    create() {
        const w = this.cameras.main.width;   // 480
        const h = this.cameras.main.height;  // 800

        // ── Title ────────────────────────────────────────────
        this.add.text(w / 2, 160, '麻將消消樂', {
            fontSize: '52px',
            fontFamily: 'serif',
            color: '#ffd700',
            fontStyle: 'bold',
            stroke: '#aa8800',
            strokeThickness: 2
        }).setOrigin(0.5);

        this.add.text(w / 2, 220, 'Mahjong Solitaire', {
            fontSize: '20px',
            color: '#888899'
        }).setOrigin(0.5);

        // ── Stats ────────────────────────────────────────────
        const progress = window.loadProgress();
        const completed = Object.keys(progress.scores).length;

        this.add.text(w / 2, 300, `已完成: ${completed} 關 | 總分: ${progress.totalScore}`, {
            fontSize: '18px',
            color: '#aaaacc'
        }).setOrigin(0.5);

        // ── Buttons ──────────────────────────────────────────
        this._createButton(w / 2, 420, 260, 56, '▶  開始遊戲', '#0f3460', '#1a5fa8', () => {
            this.scene.start('LevelSelectScene');
        });

        if (progress.lastLevel > 0) {
            this._createButton(w / 2, 500, 260, 56,
                `↩  繼續 第${progress.lastLevel}關`, '#2d4a22', '#3d6a32', () => {
                    this.scene.start('GameScene', { level: progress.lastLevel });
                });
        }

        // ── Version ──────────────────────────────────────────
        this.add.text(w / 2, h - 40, 'v3.0 · 100 關', {
            fontSize: '14px',
            color: '#555566'
        }).setOrigin(0.5);
    }

    _createButton(x, y, width, height, label, color, hoverColor, callback) {
        const colorNum = Phaser.Display.Color.HexStringToColor(color).color;
        const hoverNum = Phaser.Display.Color.HexStringToColor(hoverColor).color;

        const bg = this.add.graphics();
        bg.fillStyle(colorNum, 1);
        bg.fillRoundedRect(x - width / 2, y - height / 2, width, height, 12);

        const text = this.add.text(x, y, label, {
            fontSize: '22px',
            color: '#ffffff',
            fontStyle: 'bold'
        }).setOrigin(0.5);

        const zone = this.add.zone(x, y, width, height).setInteractive({ useHandCursor: true });

        zone.on('pointerover', () => {
            bg.clear();
            bg.fillStyle(hoverNum, 1);
            bg.fillRoundedRect(x - width / 2, y - height / 2, width, height, 12);
        });

        zone.on('pointerout', () => {
            bg.clear();
            bg.fillStyle(colorNum, 1);
            bg.fillRoundedRect(x - width / 2, y - height / 2, width, height, 12);
        });

        zone.on('pointerdown', callback);

        return { bg, text, zone };
    }
}

window.MenuScene = MenuScene;
