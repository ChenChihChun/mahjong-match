package com.mahjong.match.game;

public class Tile {
    // Tile types
    public static final int TYPE_CHAR_1  = 0;
    public static final int TYPE_CHAR_2  = 1;
    public static final int TYPE_CHAR_3  = 2;
    public static final int TYPE_CHAR_4  = 3;
    public static final int TYPE_CHAR_5  = 4;
    public static final int TYPE_CHAR_6  = 5;
    public static final int TYPE_CHAR_7  = 6;
    public static final int TYPE_CHAR_8  = 7;
    public static final int TYPE_CHAR_9  = 8;
    public static final int TYPE_BAMB_1  = 9;
    public static final int TYPE_BAMB_2  = 10;
    public static final int TYPE_BAMB_3  = 11;
    public static final int TYPE_BAMB_4  = 12;
    public static final int TYPE_BAMB_5  = 13;
    public static final int TYPE_BAMB_6  = 14;
    public static final int TYPE_BAMB_7  = 15;
    public static final int TYPE_BAMB_8  = 16;
    public static final int TYPE_BAMB_9  = 17;
    public static final int TYPE_CIRC_1  = 18;
    public static final int TYPE_CIRC_2  = 19;
    public static final int TYPE_CIRC_3  = 20;
    public static final int TYPE_CIRC_4  = 21;
    public static final int TYPE_CIRC_5  = 22;
    public static final int TYPE_CIRC_6  = 23;
    public static final int TYPE_CIRC_7  = 24;
    public static final int TYPE_CIRC_8  = 25;
    public static final int TYPE_CIRC_9  = 26;
    public static final int TYPE_WIND_E  = 27; // 東
    public static final int TYPE_WIND_S  = 28; // 南
    public static final int TYPE_WIND_W  = 29; // 西
    public static final int TYPE_WIND_N  = 30; // 北
    public static final int TYPE_DRAG_Z  = 31; // 中
    public static final int TYPE_DRAG_F  = 32; // 發
    public static final int TYPE_DRAG_B  = 33; // 白
    public static final int TYPE_FLOWER  = 34; // 花 (梅蘭菊竹)
    public static final int TYPE_SEASON  = 35; // 季 (春夏秋冬)

    public static final int TOTAL_TYPES = 36;

    // Flower sub-types
    public static final int FLOWER_MEI  = 0; // 梅
    public static final int FLOWER_LAN  = 1; // 蘭
    public static final int FLOWER_JU   = 2; // 菊
    public static final int FLOWER_ZHU  = 3; // 竹
    // Season sub-types
    public static final int SEASON_SPRING = 0; // 春
    public static final int SEASON_SUMMER = 1; // 夏
    public static final int SEASON_AUTUMN = 2; // 秋
    public static final int SEASON_WINTER = 3; // 冬

    // Position in half-tile units
    public int x, y, z;
    public int type;
    public int subType; // for flower/season
    public boolean removed = false;
    public boolean selected = false;
    public boolean highlighted = false; // hint

    public Tile(int z, int y, int x, int type) {
        this.z = z;
        this.y = y;
        this.x = x;
        this.type = type;
        this.subType = 0;
    }

    public boolean matches(Tile other) {
        if (this.type == TYPE_FLOWER && other.type == TYPE_FLOWER) return true;
        if (this.type == TYPE_SEASON && other.type == TYPE_SEASON) return true;
        return this.type == other.type;
    }

    public String getLabel() {
        switch (type) {
            case TYPE_CHAR_1: return "一\n萬";
            case TYPE_CHAR_2: return "二\n萬";
            case TYPE_CHAR_3: return "三\n萬";
            case TYPE_CHAR_4: return "四\n萬";
            case TYPE_CHAR_5: return "五\n萬";
            case TYPE_CHAR_6: return "六\n萬";
            case TYPE_CHAR_7: return "七\n萬";
            case TYPE_CHAR_8: return "八\n萬";
            case TYPE_CHAR_9: return "九\n萬";
            case TYPE_BAMB_1: return "1\n竹";
            case TYPE_BAMB_2: return "2\n竹";
            case TYPE_BAMB_3: return "3\n竹";
            case TYPE_BAMB_4: return "4\n竹";
            case TYPE_BAMB_5: return "5\n竹";
            case TYPE_BAMB_6: return "6\n竹";
            case TYPE_BAMB_7: return "7\n竹";
            case TYPE_BAMB_8: return "8\n竹";
            case TYPE_BAMB_9: return "9\n竹";
            case TYPE_CIRC_1: return "①";
            case TYPE_CIRC_2: return "②";
            case TYPE_CIRC_3: return "③";
            case TYPE_CIRC_4: return "④";
            case TYPE_CIRC_5: return "⑤";
            case TYPE_CIRC_6: return "⑥";
            case TYPE_CIRC_7: return "⑦";
            case TYPE_CIRC_8: return "⑧";
            case TYPE_CIRC_9: return "⑨";
            case TYPE_WIND_E: return "東";
            case TYPE_WIND_S: return "南";
            case TYPE_WIND_W: return "西";
            case TYPE_WIND_N: return "北";
            case TYPE_DRAG_Z: return "中";
            case TYPE_DRAG_F: return "發";
            case TYPE_DRAG_B: return "白";
            case TYPE_FLOWER:
                switch(subType) {
                    case FLOWER_MEI: return "梅";
                    case FLOWER_LAN: return "蘭";
                    case FLOWER_JU:  return "菊";
                    default:         return "竹";
                }
            case TYPE_SEASON:
                switch(subType) {
                    case SEASON_SPRING: return "春";
                    case SEASON_SUMMER: return "夏";
                    case SEASON_AUTUMN: return "秋";
                    default:            return "冬";
                }
            default: return "?";
        }
    }

    public int getLabelColor() {
        if (type >= TYPE_CHAR_1 && type <= TYPE_CHAR_9) return 0xFF1A1A8C; // dark blue
        if (type >= TYPE_BAMB_1 && type <= TYPE_BAMB_9) return 0xFF1A6B1A; // dark green
        if (type >= TYPE_CIRC_1 && type <= TYPE_CIRC_9) return 0xFF8C1A1A; // dark red
        if (type == TYPE_WIND_E || type == TYPE_WIND_S || type == TYPE_WIND_W || type == TYPE_WIND_N) return 0xFF444444;
        if (type == TYPE_DRAG_Z) return 0xFFCC0000; // 中 red
        if (type == TYPE_DRAG_F) return 0xFF00AA00; // 發 green
        if (type == TYPE_DRAG_B) return 0xFF222222; // 白 dark
        if (type == TYPE_FLOWER) return 0xFFCC6600; // orange
        if (type == TYPE_SEASON) return 0xFF6600CC; // purple
        return 0xFF000000;
    }
}
