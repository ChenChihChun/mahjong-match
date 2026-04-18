class LevelSelectScene extends Phaser.Scene {
    constructor() {
        super({ key: 'LevelSelectScene' });
    }

    create() {
        const w = this.cameras.main.width;   // 480
        const h = this.cameras.main.height;  // 800
        const progress = window.loadProgress();

        // ── Scrollable container ─────────────────────────────
        this.container = this.add.container(0, 0);

        // ── Back button (fixed UI, not in container) ─────────
        const backBg = this.add.graphics();
        backBg.fillStyle(0x0f3460, 1);
        backBg.fillRoundedRect(10, 10, 90, 40, 8);
        const backText = this.add.text(55, 30, '← 返回', {
            fontSize: '18px', color: '#ffffff'
        }).setOrigin(0.5);
        const backZone = this.add.zone(55, 30, 90, 40).setInteractive({ useHandCursor: true });
        backZone.on('pointerdown', () => this.scene.start('MenuScene'));

        // ── Sections ─────────────────────────────────────────
        const sections = [
            { label: '初級 ⭐ (1-20)',           start: 1,  end: 20  },
            { label: '中級 ⭐⭐ (21-50)',        start: 21, end: 50  },
            { label: '困難 ⭐⭐⭐ (51-80)',      start: 51, end: 80  },
            { label: '大師 ⭐⭐⭐⭐ (81-100)',   start: 81, end: 100 },
        ];

        const cols = 7;
        const btnSize = 50;
        const gap = 10;
        const gridW = cols * (btnSize + gap) - gap;
        const startX = (w - gridW) / 2;
        let curY = 70;

        for (const sec of sections) {
            // Section header
            this.container.add(
                this.add.text(w / 2, curY, sec.label, {
                    fontSize: '20px', color: '#ffd700', fontStyle: 'bold'
                }).setOrigin(0.5)
            );
            curY += 40;

            for (let lvl = sec.start; lvl <= sec.end; lvl++) {
                const col = (lvl - sec.start) % cols;
                const row = Math.floor((lvl - sec.start) / cols);
                const bx = startX + col * (btnSize + gap) + btnSize / 2;
                const by = curY + row * (btnSize + gap) + btnSize / 2;

                const completed = progress.scores && progress.scores[lvl] !== undefined;
                const unlocked = lvl === 1 || progress.maxLevel >= lvl - 1;

                let bgColor, textColor, alpha;
                if (completed) {
                    bgColor = 0xffd700;
                    textColor = '#1a1a2e';
                    alpha = 1;
                } else if (unlocked) {
                    bgColor = 0x0f3460;
                    textColor = '#ffffff';
                    alpha = 1;
                } else {
                    bgColor = 0x222244;
                    textColor = '#555566';
                    alpha = 0.5;
                }

                const btnGfx = this.add.graphics();
                btnGfx.fillStyle(bgColor, alpha);
                btnGfx.fillRoundedRect(bx - btnSize / 2, by - btnSize / 2, btnSize, btnSize, 8);
                this.container.add(btnGfx);

                const btnLabel = this.add.text(bx, by, `${lvl}`, {
                    fontSize: '18px', color: textColor, fontStyle: 'bold'
                }).setOrigin(0.5).setAlpha(alpha);
                this.container.add(btnLabel);

                if (unlocked) {
                    const btnZone = this.add.zone(bx, by, btnSize, btnSize)
                        .setInteractive({ useHandCursor: true });
                    this.container.add(btnZone);

                    const hoverColor = completed ? 0xffe555 : 0x1a5fa8;
                    btnZone.on('pointerover', () => {
                        btnGfx.clear();
                        btnGfx.fillStyle(hoverColor, 1);
                        btnGfx.fillRoundedRect(bx - btnSize / 2, by - btnSize / 2, btnSize, btnSize, 8);
                    });
                    btnZone.on('pointerout', () => {
                        btnGfx.clear();
                        btnGfx.fillStyle(bgColor, alpha);
                        btnGfx.fillRoundedRect(bx - btnSize / 2, by - btnSize / 2, btnSize, btnSize, 8);
                    });

                    const capturedLvl = lvl;
                    btnZone.on('pointerdown', () => {
                        this.scene.start('GameScene', { level: capturedLvl });
                    });
                }
            }

            const totalRows = Math.ceil((sec.end - sec.start + 1) / cols);
            curY += totalRows * (btnSize + gap) + 20;
        }

        this.contentHeight = curY + 40;

        // ── Drag scrolling ───────────────────────────────────
        this.dragStartY = 0;
        this.containerStartY = 0;
        this.scrollVelocity = 0;

        this.input.on('pointerdown', (pointer) => {
            this.dragStartY = pointer.y;
            this.containerStartY = this.container.y;
            this.scrollVelocity = 0;
        });

        this.input.on('pointermove', (pointer) => {
            if (!pointer.isDown) return;
            const dy = pointer.y - this.dragStartY;
            this.container.y = this._clampScroll(this.containerStartY + dy);
        });

        this.input.on('pointerup', (pointer) => {
            this.scrollVelocity = pointer.velocity.y * 0.3;
        });
    }

    update() {
        if (Math.abs(this.scrollVelocity) > 0.5) {
            this.container.y = this._clampScroll(this.container.y + this.scrollVelocity);
            this.scrollVelocity *= 0.92;
        }
    }

    _clampScroll(y) {
        const h = this.cameras.main.height;
        const minY = Math.min(0, h - this.contentHeight);
        return Phaser.Math.Clamp(y, minY, 0);
    }
}

window.LevelSelectScene = LevelSelectScene;
