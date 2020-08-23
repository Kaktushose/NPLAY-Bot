package de.kaktushose.nrtv.discord.frameworks.reactionwaiter;

/**
 * All emotes that can be used.
 *
 * @author Kaktushose
 * @version 1.0.0
 * @since 1.0.0
 *
 */

public enum EmoteType {

    THUMBSUP("\uD83D\uDC4D"),
    THUMBSDOWN("\uD83D\uDC4E"),
    XP("\uD83C\uDF1F"),
    COINS("\uD83D\uDCB0"),
    CHECKMARK("\u2705"),
    REDCROSS("\u274C"),
    PREMIUM("\u2B50"),
    DJ("\uD83C\uDFB5"),
    NICKNAME("\uD83D\uDC68\uD83C\uDFFD"),
    BOOSTER("\uD83D\uDCB0"),
    XPBOOSTER("\uD83C\uDF1F"),
    ONE("1️⃣"),
    TWO("2️⃣"),
    THREE("3️⃣"),
    FOUR("4️⃣"),
    FIVE("5️⃣"),
    CANCEL("\u274C"),
    BACK("◀️"),
    FORTH("▶️"),
    FLAG("\uD83D\uDEA9"),
    DIAMOND("\uD83D\uDC8E"),
    EVENT("\uD83C\uDF89");

    EmoteType(String name) {
        this.name = name;
    }

    /**
     * Returns the suitable emote for the given number. Only supports Integers in range from 0 to 4, inclusive.
     *
     * @param i the Integer that will be represented as an emote
     * @return the EmoteType representing the number
     */

    public static EmoteType getNumber(int i) {
        switch (i) {
            case 0:
                return ONE;
            case 1:
                return TWO;
            case 2:
                return THREE;
            case 3:
                return FOUR;
            case 4:
                return FIVE;
            default:
                return null;
        }
    }

    /**
     * the unicode of the emote
     */
    public final String name;

}

