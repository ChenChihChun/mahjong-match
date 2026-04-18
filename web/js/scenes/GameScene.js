class GameScene extends Phaser.Scene {
    constructor() {
        super({ key: 'GameScene' });
    }

    init(data) {
        this.level = data.level || 1;
        this.tileContainers = new Map(); // tile.id → Phaser.Container
        this.gameOver = false;
    }

    create() {
        const w = this.cameras.main.width;
        const h = this.cameras.main.height;

        // Save lastLevel
        const progress = window.loadProgress();
        progress.lastLevel = this.level;
        window.saveProgress(progress);

        // ── Engine + callbacks ────────────────────────────────
        this.engine = new GameEngine();

        this.engine.onMatch = (a, b) => {
            this._animateMatch(a, b);
        };

        this.engine.onInvalidPair = () => {
            this._refreshAllTileVisuals();
        };

        this.engine.onComplete = (score, timeMs, moves) => {
            this.gameOver = true;
            if (this.timerEvent) this.timerEvent.remove();
            this._showCompleteOverlay(score, timeMs, moves);
        };

        this.engine.onStuck = () => {
            this.gameOver = true;
            if (this.timerEvent) this.timerEvent.remove();
            this._showStuckOverlay();
        };

        this.engine.onHint = (a, b) => {
            this._refreshAllTileVisuals();
        };

        // Init level (builds board internally via LevelData + Board)
        this.engine.init(this.level);
        this.board = this.engine.board;

        // ── Top bar ──────────────────────────────────────────
        this._createTopBar(w);

        // ── Render tiles ─────────────────────────────────────
        this._renderAllTiles(w, h);

        // ── Bottom bar ───────────────────────────────────────
        this._createBottomBar(w, h);

        // ── Timer ────────────────────────────────────────────
        this.timerEvent = this.time.addEvent({
            delay: 500,
            loop: true,
            callback: () => {
                if (this.gameOver) return;
                this.engine.tick();
                this._updateTimerText();
            }
        });
    }

    // ── Top Bar ──────────────────────────────────────────────
    _createTopBar(w) {
        const barY = 22;

        // Close button
        const closeBg = this.add.graphics();
        closeBg.fillStyle(0x333355, 1);
        closeBg.fillRoundedRect(8, barY - 14, 40, 36, 6);
        this.add.text(28, barY + 4, '✕', {
            fontSize: '22px', color: '#ff6666'
        }).setOrigin(0.5);
        this.add.zone(28, barY + 4, 44, 40)
            .setInteractive({ useHandCursor: true })
            .on('pointerdown', () => this.scene.start('LevelSelectScene'));

        // Level title
        this.add.text(w / 2, barY + 4, `第 ${this.level} 關`, {
            fontSize: '22px', color: '#ffd700', fontStyle: 'bold'
        }).setOrigin(0.5);

        // Info row
        const infoY = barY + 36;
        this.timerText = this.add.text(55, infoY, '00:00', {
            fontSize: '16px', color: '#aaaacc'
        }).setOrigin(0.5);

        this.movesText = this.add.text(w / 2, infoY, '步數: 0', {
            fontSize: '16px', color: '#aaaacc'
        }).setOrigin(0.5);

        const remaining = this.board.getRemainingCount();
        this.remainText = this.add.text(w - 55, infoY, `剩餘: ${remaining}`, {
            fontSize: '16px', color: '#aaaacc'
        }).setOrigin(0.5);
    }

    // ── Tile Rendering ───────────────────────────────────────
    _renderAllTiles(gameW, gameH) {
        // Destroy old containers
        this.tileContainers.forEach(c => c.destroy());
        this.tileContainers.clear();

        const tiles = this.board.tiles.filter(t => !t.removed);
        if (tiles.length === 0) return;

        // Layout metrics
        const padding = 16;
        const topOffset = 80;
        const bottomOffset = 75;
        const availW = gameW - padding * 2;
        const availH = gameH - topOffset - bottomOffset;

        const boardW = 18; // half-units
        const maxZ = Math.max(...tiles.map(t => t.z), 0);
        const boardH = Math.max(...tiles.map(t => t.y), 0) + 2;

        let hw = availW / (boardW + maxZ * 0.5);
        let hh = hw * 1.4;

        // Scale down if needed vertically
        const neededH = boardH * hh + maxZ * 4;
        if (neededH > availH) {
            const scale = availH / neededH;
            hw *= scale;
            hh *= scale;
        }

        const tileW = hw * 2 * 0.90;
        const tileH = hh * 2 * 0.90;
        const offsetX = padding + (availW - boardW * hw) / 2;
        const offsetY = topOffset + (availH - boardH * hh) / 2;

        this._metrics = { hw, hh, tileW, tileH, offsetX, offsetY };

        // Render back to front (z asc, then y asc, then x asc)
        const sorted = [...tiles].sort((a, b) => {
            if (a.z !== b.z) return a.z - b.z;
            if (a.y !== b.y) return a.y - b.y;
            return a.x - b.x;
        });

        for (const tile of sorted) {
            this._createTileContainer(tile);
        }
    }

    _tileScreenPos(tile) {
        const { hw, hh, offsetX, offsetY } = this._metrics;
        return {
            sx: offsetX + tile.x * hw + tile.z * 4,
            sy: offsetY + tile.y * hh - tile.z * 4
        };
    }

    _createTileContainer(tile) {
        const { tileW, tileH } = this._metrics;
        const { sx, sy } = this._tileScreenPos(tile);

        const isFree = this.board.isFree(tile);
        const sideD = Math.max(3, Math.floor(tileW * 0.08));

        const container = this.add.container(sx, sy);
        container.setDepth(tile.z * 10000 + tile.y * 100 + tile.x);

        // Right side (3D)
        const rightSide = this.add.graphics();
        rightSide.fillStyle(isFree ? 0xc4b88a : 0xa09870, 1);
        rightSide.fillRect(tileW / 2, -tileH / 2 + sideD, sideD, tileH);
        container.add(rightSide);

        // Bottom side (3D)
        const bottomSide = this.add.graphics();
        bottomSide.fillStyle(isFree ? 0xb0a47a : 0x908060, 1);
        bottomSide.fillRect(-tileW / 2 + sideD, tileH / 2, tileW, sideD);
        container.add(bottomSide);

        // Face
        const face = this.add.graphics();
        this._drawFace(face, tile, tileW, tileH);
        container.add(face);
        container._face = face;

        // Tile image
        const imgKey = Tile.getAssetKey(tile.type, tile.subType);
        if (this.textures.exists(imgKey)) {
            const img = this.add.image(0, 0, imgKey);
            const imgScale = Math.min((tileW - sideD * 2) / img.width, (tileH - sideD * 2) / img.height) * 0.85;
            img.setScale(imgScale).setAlpha(isFree ? 1 : 0.7);
            container.add(img);
            container._img = img;
        }

        // Interaction (free tiles only)
        if (isFree) {
            container.setSize(tileW + sideD, tileH + sideD);
            container.setInteractive({ useHandCursor: true });
            container.on('pointerdown', () => {
                if (this.gameOver) return;
                this.engine.selectTile(tile);
                this._refreshAllTileVisuals();
                this._updateMovesText();
                this._updateRemainingText();
            });
        }

        container._tile = tile;
        container._tileW = tileW;
        container._tileH = tileH;
        container._sideD = sideD;
        this.tileContainers.set(tile.id, container);
    }

    _drawFace(gfx, tile, tileW, tileH) {
        let color;
        if (tile.selected)     color = 0xfff4b0;
        else if (tile.highlighted) color = 0xd4f5cc;
        else if (this.board.isFree(tile)) color = 0xfff8e4;
        else                   color = 0xe6d7ac;

        gfx.clear();
        gfx.fillStyle(color, 1);
        gfx.fillRoundedRect(-tileW / 2, -tileH / 2, tileW, tileH, 4);
        const borderColor = tile.selected ? 0xddaa00 : tile.highlighted ? 0x66cc44 : 0xaa9966;
        gfx.lineStyle(1.5, borderColor, 0.8);
        gfx.strokeRoundedRect(-tileW / 2, -tileH / 2, tileW, tileH, 4);
    }

    _refreshAllTileVisuals() {
        for (const [id, container] of this.tileContainers) {
            const tile = container._tile;
            if (tile.removed) {
                container.destroy();
                this.tileContainers.delete(id);
                continue;
            }
            const isFree = this.board.isFree(tile);
            const tileW = container._tileW;
            const tileH = container._tileH;
            const sideD = container._sideD;

            this._drawFace(container._face, tile, tileW, tileH);

            if (container._img) {
                container._img.setAlpha(isFree ? 1 : 0.7);
            }

            // Update interactivity
            if (isFree && !container.input) {
                container.setSize(tileW + sideD, tileH + sideD);
                container.setInteractive({ useHandCursor: true });
                container.on('pointerdown', () => {
                    if (this.gameOver) return;
                    this.engine.selectTile(tile);
                    this._refreshAllTileVisuals();
                    this._updateMovesText();
                    this._updateRemainingText();
                });
            } else if (!isFree && container.input) {
                container.disableInteractive();
            }
        }
    }

    // ── Match animation ───────────────────────────────────────
    _animateMatch(tile1, tile2) {
        const animate = (tile) => {
            const c = this.tileContainers.get(tile.id);
            if (!c) return;
            this.tileContainers.delete(tile.id);
            this.tweens.add({
                targets: c,
                scaleX: 0, scaleY: 0, alpha: 0,
                duration: 280,
                ease: 'Power2',
                onComplete: () => c.destroy()
            });
        };
        animate(tile1);
        animate(tile2);

        // Refresh after animation
        this.time.delayedCall(300, () => {
            this._refreshAllTileVisuals();
            this._updateRemainingText();
            this._updateMovesText();
        });
    }

    // ── Bottom bar ───────────────────────────────────────────
    _createBottomBar(w, h) {
        const barY = h - 40;
        const btnW = 110, btnH = 38, gap = 12;
        const totalW = btnW * 3 + gap * 2;
        const startX = (w - totalW) / 2 + btnW / 2;

        this._bottomBtn(startX, barY, btnW, btnH, '💡 提示', () => {
            if (this.gameOver) return;
            this.engine.showHint();
            this._refreshAllTileVisuals();
        });

        this._bottomBtn(startX + btnW + gap, barY, btnW, btnH, '🔀 重排', () => {
            if (this.gameOver) return;
            this.engine.shuffle();
            const { hw, hh, tileW, tileH, offsetX, offsetY } = this._metrics;
            this._renderAllTiles(this.cameras.main.width, this.cameras.main.height);
        });

        this._bottomBtn(startX + (btnW + gap) * 2, barY, btnW, btnH, '↺ 重來', () => {
            this.scene.restart({ level: this.level });
        });
    }

    _bottomBtn(x, y, bw, bh, label, cb) {
        const bg = this.add.graphics();
        const draw = (col) => {
            bg.clear();
            bg.fillStyle(col, 1);
            bg.fillRoundedRect(x - bw / 2, y - bh / 2, bw, bh, 8);
        };
        draw(0x333355);
        this.add.text(x, y, label, { fontSize: '15px', color: '#ffffff' }).setOrigin(0.5);
        const zone = this.add.zone(x, y, bw, bh).setInteractive({ useHandCursor: true });
        zone.on('pointerover', () => draw(0x444477));
        zone.on('pointerout', () => draw(0x333355));
        zone.on('pointerdown', cb);
    }

    // ── Overlays ─────────────────────────────────────────────
    _showCompleteOverlay(score, timeMs, moves) {
        const w = this.cameras.main.width;
        const h = this.cameras.main.height;

        // Save progress
        const progress = window.loadProgress();
        if (this.level >= progress.maxLevel) progress.maxLevel = this.level;
        const prev = progress.scores[this.level] || 0;
        if (score > prev) {
            progress.scores[this.level] = score;
            progress.totalScore = Object.values(progress.scores).reduce((a, b) => a + b, 0);
        }
        window.saveProgress(progress);

        const secs = Math.floor(timeMs / 1000);
        const mm = String(Math.floor(secs / 60)).padStart(2, '0');
        const ss = String(secs % 60).padStart(2, '0');

        const D = 50000;
        this.add.graphics().fillStyle(0x000000, 0.72).fillRect(0, 0, w, h).setDepth(D);
        const p = this.add.graphics().setDepth(D + 1);
        p.fillStyle(0x1a1a2e, 1).fillRoundedRect(40, h / 2 - 165, w - 80, 330, 16);
        p.lineStyle(2, 0xffd700, 1).strokeRoundedRect(40, h / 2 - 165, w - 80, 330, 16);

        const cy = h / 2 - 120;
        this.add.text(w / 2, cy,      '🎉 過關！',                { fontSize: '36px', color: '#ffd700', fontStyle: 'bold' }).setOrigin(0.5).setDepth(D + 2);
        this.add.text(w / 2, cy + 55, `分數: ${score}`,           { fontSize: '26px', color: '#ffffff' }).setOrigin(0.5).setDepth(D + 2);
        this.add.text(w / 2, cy + 95, `時間: ${mm}:${ss}  |  步數: ${moves}`, { fontSize: '16px', color: '#aaaacc' }).setOrigin(0.5).setDepth(D + 2);

        const btnY = cy + 155;
        if (this.level < 100)
            this._overlayBtn(w / 2 - 105, btnY, 95, 40, '下一關', 0x0f3460, D, () => this.scene.restart({ level: this.level + 1 }));
        this._overlayBtn(w / 2,           btnY, 95, 40, '選關卡', 0x333355, D, () => this.scene.start('LevelSelectScene'));
        this._overlayBtn(w / 2 + 105,     btnY, 95, 40, '再玩', 0x2d4a22, D, () => this.scene.restart({ level: this.level }));
    }

    _showStuckOverlay() {
        const w = this.cameras.main.width;
        const h = this.cameras.main.height;
        const D = 50000;

        this.add.graphics().fillStyle(0x000000, 0.72).fillRect(0, 0, w, h).setDepth(D);
        const p = this.add.graphics().setDepth(D + 1);
        p.fillStyle(0x1a1a2e, 1).fillRoundedRect(40, h / 2 - 120, w - 80, 240, 16);
        p.lineStyle(2, 0xff6666, 1).strokeRoundedRect(40, h / 2 - 120, w - 80, 240, 16);

        const cy = h / 2 - 70;
        this.add.text(w / 2, cy, '😵 無可消除配對', { fontSize: '26px', color: '#ff6666', fontStyle: 'bold' }).setOrigin(0.5).setDepth(D + 2);

        const btnY = cy + 70;
        this._overlayBtn(w / 2 - 90, btnY, 110, 40, '🔀 重排', 0x0f3460, D, () => {
            this.gameOver = false;
            this.engine.shuffle();
            // Dismiss overlay elements by restarting scene with same state
            this.scene.restart({ level: this.level });
        });
        this._overlayBtn(w / 2 + 90, btnY, 110, 40, '重新開始', 0x2d4a22, D, () => this.scene.restart({ level: this.level }));
        this._overlayBtn(w / 2, btnY + 55, 110, 40, '退出', 0x333355, D, () => this.scene.start('LevelSelectScene'));
    }

    _overlayBtn(x, y, bw, bh, label, color, baseD, cb) {
        this.add.graphics().fillStyle(color, 1).fillRoundedRect(x - bw / 2, y - bh / 2, bw, bh, 8).setDepth(baseD + 1);
        this.add.text(x, y, label, { fontSize: '15px', color: '#ffffff', fontStyle: 'bold' }).setOrigin(0.5).setDepth(baseD + 2);
        this.add.zone(x, y, bw, bh).setInteractive({ useHandCursor: true }).setDepth(baseD + 3).on('pointerdown', cb);
    }

    // ── UI Updates ───────────────────────────────────────────
    _updateTimerText() {
        this.timerText.setText(this.engine.getTimeString());
    }

    _updateMovesText() {
        this.movesText.setText(`步數: ${this.engine.moveCount}`);
    }

    _updateRemainingText() {
        this.remainText.setText(`剩餘: ${this.board.getRemainingCount()}`);
    }
}

window.GameScene = GameScene;
