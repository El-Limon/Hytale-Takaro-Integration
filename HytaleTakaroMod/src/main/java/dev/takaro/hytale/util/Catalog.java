package dev.takaro.hytale.util;

import java.util.Locale;

/**
 * Catalogue hygiene rules shared by listItems and listEntities.
 *
 * <p>No Hytale imports, so this is unit-testable without the server jar.
 */
public final class Catalog {
    private Catalog() {
    }

    /**
     * True for codes that are developer scaffolding rather than real game content, e.g.
     * {@code Debug_Donut_Selector}, {@code Test_Block}, {@code Dev_Marker} (F10).
     */
    public static boolean isDeveloperAsset(String code) {
        if (code == null) {
            return false;
        }
        String lower = code.toLowerCase(Locale.ROOT);
        return lower.startsWith("debug_") || lower.startsWith("test_") || lower.startsWith("dev_");
    }

    /**
     * The i18n key Hytale itself uses for an NPC role's player-facing name (F18).
     *
     * <p>Found in {@code Server/Languages/en-US/server.lang} inside {@code Assets.zip} (and in
     * {@code bundledDefaults/server.lang} inside the server jar): 574 entries of the form
     * {@code npcRoles.Bat_Ice.name = Ice Bat}, {@code npcRoles.Cow_Calf.name = Calf}, which
     * I18nModule registers under the file-name prefix as {@code server.npcRoles.Bat_Ice.name}.
     * {@code BuilderRole.getDisplayNames()} does not carry them.
     */
    public static java.util.List<String> roleNameKeys(String code) {
        // I18nModule prefixes every key with the .lang file's own name, so the key that
        // server.lang's `npcRoles.Bat_Ice.name` is actually registered under at runtime is
        // `server.npcRoles.Bat_Ice.name` (the same way HelpCommand asks for
        // `server.commands.help.console.header` for a line written as
        // `commands.help.console.header`). The unprefixed form is tried too, in case a future
        // asset pack ships the table under a different file name.
        return java.util.List.of("server.npcRoles." + code + ".name", "npcRoles." + code + ".name");
    }

    /**
     * True for role templates that are engine scaffolding rather than creatures a server owner
     * would ever want in a Takaro entity list: the {@code Static}/{@code Static2..4} marker
     * roles, {@code Template} / {@code BlankTemplate}, and {@code Empty_Role}.
     */
    public static boolean isPlaceholderRole(String code) {
        if (code == null || code.isEmpty()) {
            return true;
        }
        if (code.equals("Template") || code.equals("BlankTemplate") || code.equals("Empty_Role")) {
            return true;
        }
        // Static, Static2, Static3, ...
        return code.startsWith("Static")
            && code.substring("Static".length()).chars().allMatch(Character::isDigit);
    }

    /**
     * Last-resort humanisation of a role id when the game has no translation for it:
     * {@code Rex_Cave} becomes {@code "Rex Cave"}, {@code trork_grunt} becomes
     * {@code "Trork Grunt"}. Deliberately does not try to reorder words - inventing
     * {@code "Cave Rex"} when the game never said so would be a guess dressed as a name.
     */
    public static String humanise(String code) {
        if (code == null || code.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        for (String word : code.split("[_\\-]+")) {
            if (word.isEmpty()) {
                continue;
            }
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                out.append(word.substring(1));
            }
        }
        return out.length() == 0 ? code : out.toString();
    }
}
