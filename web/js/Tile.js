/**
 * Tile.js — Mahjong Solitaire tile definitions and matching logic.
 * Ported from Java Tile.java.
 */
class Tile {
    // Tile type constants
    static TYPE_CHAR_1 = 0;
    static TYPE_CHAR_2 = 1;
    static TYPE_CHAR_3 = 2;
    static TYPE_CHAR_4 = 3;
    static TYPE_CHAR_5 = 4;
    static TYPE_CHAR_6 = 5;
    static TYPE_CHAR_7 = 6;
    static TYPE_CHAR_8 = 7;
    static TYPE_CHAR_9 = 8;
    static TYPE_BAMB_1 = 9;
    static TYPE_BAMB_2 = 10;
    static TYPE_BAMB_3 = 11;
    static TYPE_BAMB_4 = 12;
    static TYPE_BAMB_5 = 13;
    static TYPE_BAMB_6 = 14;
    static TYPE_BAMB_7 = 15;
    static TYPE_BAMB_8 = 16;
    static TYPE_BAMB_9 = 17;
    static TYPE_CIRC_1 = 18;
    static TYPE_CIRC_2 = 19;
    static TYPE_CIRC_3 = 20;
    static TYPE_CIRC_4 = 21;
    static TYPE_CIRC_5 = 22;
    static TYPE_CIRC_6 = 23;
    static TYPE_CIRC_7 = 24;
    static TYPE_CIRC_8 = 25;
    static TYPE_CIRC_9 = 26;
    static TYPE_WIND_E = 27;
    static TYPE_WIND_S = 28;
    static TYPE_WIND_W = 29;
    static TYPE_WIND_N = 30;
    static TYPE_DRAG_Z = 31;
    static TYPE_DRAG_F = 32;
    static TYPE_DRAG_B = 33;
    static TYPE_FLOWER = 34;
    static TYPE_SEASON = 35;

    static TOTAL_TYPES = 36;

    static FLOWER_MEI = 0;
    static FLOWER_LAN = 1;
    static FLOWER_JU = 2;
    static FLOWER_ZHU = 3;
    static SEASON_SPRING = 0;
    static SEASON_SUMMER = 1;
    static SEASON_AUTUMN = 2;
    static SEASON_WINTER = 3;

    constructor(z, y, x, type) {
        this.id = ++Tile._nextId;
        this.z = z;
        this.y = y;
        this.x = x;
        this.type = type;
        this.subType = 0;
        this.removed = false;
        this.selected = false;
        this.highlighted = false;
    }

    /**
     * Check if this tile matches another tile.
     * Flowers match any flower, seasons match any season,
     * otherwise types must be identical.
     */
    static matches(tileA, tileB) {
        if (tileA.type === Tile.TYPE_FLOWER && tileB.type === Tile.TYPE_FLOWER) return true;
        if (tileA.type === Tile.TYPE_SEASON && tileB.type === Tile.TYPE_SEASON) return true;
        return tileA.type === tileB.type;
    }

    /**
     * Returns the Phaser asset key for this tile's image.
     */
    static getAssetKey(type, subType) {
        if (type >= Tile.TYPE_CHAR_1 && type <= Tile.TYPE_CHAR_9) {
            return 'tile_' + (type - Tile.TYPE_CHAR_1 + 1) + 'm';
        }
        if (type >= Tile.TYPE_BAMB_1 && type <= Tile.TYPE_BAMB_9) {
            return 'tile_' + (type - Tile.TYPE_BAMB_1 + 1) + 's';
        }
        if (type >= Tile.TYPE_CIRC_1 && type <= Tile.TYPE_CIRC_9) {
            return 'tile_' + (type - Tile.TYPE_CIRC_1 + 1) + 'p';
        }
        if (type >= Tile.TYPE_WIND_E && type <= Tile.TYPE_WIND_N) {
            return 'tile_' + (type - Tile.TYPE_WIND_E + 1) + 'z';
        }
        if (type >= Tile.TYPE_DRAG_Z && type <= Tile.TYPE_DRAG_B) {
            return 'tile_' + (type - Tile.TYPE_DRAG_Z + 5) + 'z';
        }
        if (type === Tile.TYPE_FLOWER) {
            const flowerKeys = ['tile_f_mei', 'tile_f_lan', 'tile_f_zhu', 'tile_f_ju'];
            return flowerKeys[subType % 4];
        }
        if (type === Tile.TYPE_SEASON) {
            const seasonKeys = ['tile_s_spring', 'tile_s_summer', 'tile_s_autumn', 'tile_s_winter'];
            return seasonKeys[subType % 4];
        }
        return 'tile_1m'; // fallback
    }

    /**
     * Returns the suit color as a hex number (0xAARRGGBB format from Java).
     */
    getSuitColor() {
        const t = this.type;
        if (t >= Tile.TYPE_CHAR_1 && t <= Tile.TYPE_CHAR_9) return 0x1A1A8C;
        if (t >= Tile.TYPE_BAMB_1 && t <= Tile.TYPE_BAMB_9) return 0x1A6B1A;
        if (t >= Tile.TYPE_CIRC_1 && t <= Tile.TYPE_CIRC_9) return 0x8C1A1A;
        if (t === Tile.TYPE_DRAG_Z) return 0xCC0000;
        if (t === Tile.TYPE_DRAG_F) return 0x00AA00;
        if (t === Tile.TYPE_FLOWER) return 0xCC6600;
        if (t === Tile.TYPE_SEASON) return 0x6600CC;
        return 0x333333;
    }

    /**
     * Fallback label if emoji font not supported.
     */
    getFallbackLabel() {
        const labels = {
            0: '一萬', 1: '二萬', 2: '三萬', 3: '四萬', 4: '五萬',
            5: '六萬', 6: '七萬', 7: '八萬', 8: '九萬',
            9: '1竹', 10: '2竹', 11: '3竹', 12: '4竹', 13: '5竹',
            14: '6竹', 15: '7竹', 16: '8竹', 17: '9竹',
            18: '1餅', 19: '2餅', 20: '3餅', 21: '4餅', 22: '5餅',
            23: '6餅', 24: '7餅', 25: '8餅', 26: '9餅',
            27: '東', 28: '南', 29: '西', 30: '北',
            31: '中', 32: '發', 33: '白'
        };
        if (this.type === Tile.TYPE_FLOWER) {
            return ['梅', '蘭', '菊', '竹'][this.subType % 4];
        }
        if (this.type === Tile.TYPE_SEASON) {
            return ['春', '夏', '秋', '冬'][this.subType % 4];
        }
        return labels[this.type] || '?';
    }
}

Tile._nextId = 0;
window.Tile = Tile;
