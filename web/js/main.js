// ── Progress helpers ──────────────────────────────────────────
const PROGRESS_KEY = 'mahjong_progress';

function loadProgress() {
    try {
        const raw = localStorage.getItem(PROGRESS_KEY);
        if (raw) return JSON.parse(raw);
    } catch (_) {}
    return { maxLevel: 0, lastLevel: 0, totalScore: 0, scores: {} };
}

function saveProgress(data) {
    localStorage.setItem(PROGRESS_KEY, JSON.stringify(data));
}

window.loadProgress = loadProgress;
window.saveProgress = saveProgress;

// ── Boot Scene (inline) ──────────────────────────────────────
class BootScene extends Phaser.Scene {
    constructor() {
        super({ key: 'BootScene' });
    }

    preload() {
        // Progress bar
        const w = this.cameras.main.width;
        const h = this.cameras.main.height;
        const barW = 300, barH = 20;
        const barX = (w - barW) / 2;
        const barY = h / 2;

        const bg = this.add.graphics();
        bg.fillStyle(0x333355, 1);
        bg.fillRect(barX, barY, barW, barH);

        const fill = this.add.graphics();
        const loadText = this.add.text(w / 2, barY - 30, '載入中...', {
            fontSize: '18px', color: '#ffffff'
        }).setOrigin(0.5);

        this.load.on('progress', (v) => {
            fill.clear();
            fill.fillStyle(0xffd700, 1);
            fill.fillRect(barX, barY, barW * v, barH);
        });

        this.load.on('complete', () => {
            bg.destroy();
            fill.destroy();
            loadText.destroy();
        });

        // Load all tile images
        const path = 'assets/tiles/';

        // Manzu (萬) 1-9
        for (let i = 1; i <= 9; i++) {
            this.load.image(`tile_${i}m`, `${path}tile_${i}m.png`);
        }
        // Souzu (索) 1-9
        for (let i = 1; i <= 9; i++) {
            this.load.image(`tile_${i}s`, `${path}tile_${i}s.png`);
        }
        // Pinzu (筒) 1-9
        for (let i = 1; i <= 9; i++) {
            this.load.image(`tile_${i}p`, `${path}tile_${i}p.png`);
        }
        // Jihai (字) 1-7
        for (let i = 1; i <= 7; i++) {
            this.load.image(`tile_${i}z`, `${path}tile_${i}z.png`);
        }
        // Flowers (花)
        ['mei', 'lan', 'zhu', 'ju'].forEach(f => {
            this.load.image(`tile_f_${f}`, `${path}tile_f_${f}.png`);
        });
        // Seasons (季)
        ['spring', 'summer', 'autumn', 'winter'].forEach(s => {
            this.load.image(`tile_s_${s}`, `${path}tile_s_${s}.png`);
        });
    }

    create() {
        this.scene.start('MenuScene');
    }
}

// ── Phaser Config ────────────────────────────────────────────
const config = {
    type: Phaser.AUTO,
    width: 480,
    height: 800,
    parent: 'game-container',
    backgroundColor: '#1a1a2e',
    scale: {
        mode: Phaser.Scale.FIT,
        autoCenter: Phaser.Scale.CENTER_BOTH
    },
    scene: [
        BootScene,
        window.MenuScene,
        window.LevelSelectScene,
        window.GameScene
    ]
};

const game = new Phaser.Game(config);
