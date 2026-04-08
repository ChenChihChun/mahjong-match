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
    public static final int TYPE_WIND_E  = 27;
    public static final int TYPE_WIND_S  = 28;
    public static final int TYPE_WIND_W  = 29;
    public static final int TYPE_WIND_N  = 30;
    public static final int TYPE_DRAG_Z  = 31;
    public static final int TYPE_DRAG_F  = 32;
    public static final int TYPE_DRAG_B  = 33;
    public static final int TYPE_FLOWER  = 34;
    public static final int TYPE_SEASON  = 35;

    public static final int TOTAL_TYPES = 36;

    public static final int FLOWER_MEI  = 0;
    public static final int FLOWER_LAN  = 1;
    public static final int FLOWER_JU   = 2;
    public static final int FLOWER_ZHU  = 3;
    public static final int SEASON_SPRING = 0;
    public static final int SEASON_SUMMER = 1;
    public static final int SEASON_AUTUMN = 2;
    public static final int SEASON_WINTER = 3;

    // Unicode Mahjong tile code points (U+1F000 block)
    // Characters (萬): U+1F007–U+1F00F
    private static final int[] CHAR_CP  = {0x1F007,0x1F008,0x1F009,0x1F00A,0x1F00B,0x1F00C,0x1F00D,0x1F00E,0x1F00F};
    // Bamboo (條): U+1F010–U+1F018
    private static final int[] BAMB_CP  = {0x1F010,0x1F011,0x1F012,0x1F013,0x1F014,0x1F015,0x1F016,0x1F017,0x1F018};
    // Circles (餅): U+1F019–U+1F021
    private static final int[] CIRC_CP  = {0x1F019,0x1F01A,0x1F01B,0x1F01C,0x1F01D,0x1F01E,0x1F01F,0x1F020,0x1F021};
    // Winds: 東南西北 U+1F000–U+1F003
    private static final int[] WIND_CP  = {0x1F000,0x1F001,0x1F002,0x1F003};
    // Dragons: 中發白 U+1F004,U+1F005,U+1F006
    private static final int[] DRAG_CP  = {0x1F004,0x1F005,0x1F006};
    // Flowers: U+1F022–U+1F025
    private static final int[] FLOWER_CP = {0x1F022,0x1F023,0x1F024,0x1F025};
    // Seasons: U+1F026–U+1F029
    private static final int[] SEASON_CP = {0x1F026,0x1F027,0x1F028,0x1F029};

    // Position in half-tile units
    public int x, y, z;
    public int type;
    public int subType;
    public boolean removed = false;
    public boolean selected = false;
    public boolean highlighted = false;

    public Tile(int z, int y, int x, int type) {
        this.z = z; this.y = y; this.x = x; this.type = type; this.subType = 0;
    }

    public boolean matches(Tile other) {
        if (this.type == TYPE_FLOWER && other.type == TYPE_FLOWER) return true;
        if (this.type == TYPE_SEASON && other.type == TYPE_SEASON) return true;
        return this.type == other.type;
    }

    /** Returns the Unicode mahjong tile as a String (uses supplementary chars). */
    public String getEmoji() {
        int cp;
        switch (type) {
            case TYPE_CHAR_1: case TYPE_CHAR_2: case TYPE_CHAR_3:
            case TYPE_CHAR_4: case TYPE_CHAR_5: case TYPE_CHAR_6:
            case TYPE_CHAR_7: case TYPE_CHAR_8: case TYPE_CHAR_9:
                cp = CHAR_CP[type - TYPE_CHAR_1]; break;
            case TYPE_BAMB_1: case TYPE_BAMB_2: case TYPE_BAMB_3:
            case TYPE_BAMB_4: case TYPE_BAMB_5: case TYPE_BAMB_6:
            case TYPE_BAMB_7: case TYPE_BAMB_8: case TYPE_BAMB_9:
                cp = BAMB_CP[type - TYPE_BAMB_1]; break;
            case TYPE_CIRC_1: case TYPE_CIRC_2: case TYPE_CIRC_3:
            case TYPE_CIRC_4: case TYPE_CIRC_5: case TYPE_CIRC_6:
            case TYPE_CIRC_7: case TYPE_CIRC_8: case TYPE_CIRC_9:
                cp = CIRC_CP[type - TYPE_CIRC_1]; break;
            case TYPE_WIND_E: cp = WIND_CP[0]; break;
            case TYPE_WIND_S: cp = WIND_CP[1]; break;
            case TYPE_WIND_W: cp = WIND_CP[2]; break;
            case TYPE_WIND_N: cp = WIND_CP[3]; break;
            case TYPE_DRAG_Z: cp = DRAG_CP[0]; break;
            case TYPE_DRAG_F: cp = DRAG_CP[1]; break;
            case TYPE_DRAG_B: cp = DRAG_CP[2]; break;
            case TYPE_FLOWER: cp = FLOWER_CP[subType % 4]; break;
            case TYPE_SEASON: cp = SEASON_CP[subType % 4]; break;
            default: return "?";
        }
        return new String(Character.toChars(cp));
    }

    /** Fallback label if emoji font not supported */
    public String getFallbackLabel() {
        switch (type) {
            case TYPE_CHAR_1: return "一萬"; case TYPE_CHAR_2: return "二萬";
            case TYPE_CHAR_3: return "三萬"; case TYPE_CHAR_4: return "四萬";
            case TYPE_CHAR_5: return "五萬"; case TYPE_CHAR_6: return "六萬";
            case TYPE_CHAR_7: return "七萬"; case TYPE_CHAR_8: return "八萬";
            case TYPE_CHAR_9: return "九萬";
            case TYPE_BAMB_1: return "1竹"; case TYPE_BAMB_2: return "2竹";
            case TYPE_BAMB_3: return "3竹"; case TYPE_BAMB_4: return "4竹";
            case TYPE_BAMB_5: return "5竹"; case TYPE_BAMB_6: return "6竹";
            case TYPE_BAMB_7: return "7竹"; case TYPE_BAMB_8: return "8竹";
            case TYPE_BAMB_9: return "9竹";
            case TYPE_CIRC_1: return "1餅"; case TYPE_CIRC_2: return "2餅";
            case TYPE_CIRC_3: return "3餅"; case TYPE_CIRC_4: return "4餅";
            case TYPE_CIRC_5: return "5餅"; case TYPE_CIRC_6: return "6餅";
            case TYPE_CIRC_7: return "7餅"; case TYPE_CIRC_8: return "8餅";
            case TYPE_CIRC_9: return "9餅";
            case TYPE_WIND_E: return "東"; case TYPE_WIND_S: return "南";
            case TYPE_WIND_W: return "西"; case TYPE_WIND_N: return "北";
            case TYPE_DRAG_Z: return "中"; case TYPE_DRAG_F: return "發";
            case TYPE_DRAG_B: return "白";
            case TYPE_FLOWER:
                String[] fl = {"梅","蘭","菊","竹"}; return fl[subType%4];
            case TYPE_SEASON:
                String[] se = {"春","夏","秋","冬"}; return se[subType%4];
            default: return "?";
        }
    }

    public int getSuitColor() {
        if (type >= TYPE_CHAR_1 && type <= TYPE_CHAR_9) return 0xFF1A1A8C;
        if (type >= TYPE_BAMB_1 && type <= TYPE_BAMB_9) return 0xFF1A6B1A;
        if (type >= TYPE_CIRC_1 && type <= TYPE_CIRC_9) return 0xFF8C1A1A;
        if (type == TYPE_DRAG_Z) return 0xFFCC0000;
        if (type == TYPE_DRAG_F) return 0xFF00AA00;
        if (type == TYPE_FLOWER) return 0xFFCC6600;
        if (type == TYPE_SEASON) return 0xFF6600CC;
        return 0xFF333333;
    }
}
