package com.csctracker.desktoppluguin.desktop;

import java.util.HashMap;
import java.util.Map;

public final class LangAndCodePage {
    private final static Map<String, String> languages = new HashMap<>();
    private final static Map<String, String> codePage = new HashMap<>();

    static {
        languages.put("0000", "Language Neutral");
        languages.put("0401", "Arabic");
        languages.put("0402", "Bulgarian");
        languages.put("0403", "Catalan");
        languages.put("0404", "Traditional Chinese");
        languages.put("0405", "Czech");
        languages.put("0406", "Danish");
        languages.put("0407", "German");
        languages.put("0408", "Greek");
        languages.put("0409", "U.S. English");
        languages.put("040A", "Castilian Spanish");
        languages.put("040B", "Finnish");
        languages.put("040C", "French");
        languages.put("040D", "Hebrew");
        languages.put("040E", "Hungarian");
        languages.put("040F", "Icelandic");
        languages.put("0410", "Italian");
        languages.put("0411", "Japanese");
        languages.put("0412", "Korean");
        languages.put("0413", "Dutch");
        languages.put("0414", "Norwegian ? Bokmal");
        languages.put("0810", "Swiss Italian");
        languages.put("0813", "Belgian Dutch");
        languages.put("0814", "Norwegian ? Nynorsk");
        languages.put("0415", "Polish");
        languages.put("0416", "Portuguese (Brazil)");
        languages.put("0417", "Rhaeto-Romanic");
        languages.put("0418", "Romanian");
        languages.put("0419", "Russian");
        languages.put("041A", "Croato-Serbian (Latin)");
        languages.put("041B", "Slovak");
        languages.put("041C", "Albanian");
        languages.put("041D", "Swedish");
        languages.put("041E", "Thai");
        languages.put("041F", "Turkish");
        languages.put("0420", "Urdu");
        languages.put("0421", "Bahasa");
        languages.put("0804", "Simplified Chinese");
        languages.put("0807", "Swiss German");
        languages.put("0809", "U.K. English");
        languages.put("080A", "Spanish (Mexico)");
        languages.put("080C", "Belgian French");
        languages.put("0C0C", "Canadian French");
        languages.put("100C", "Swiss French");
        languages.put("0816", "Portuguese (Portugal)");
        languages.put("081A", "Serbo-Croatian (Cyrillic)");

        codePage.put("0000", "7-bit ASCII");
        codePage.put("03A4", "Japan (Shift ? JIS X-0208)");
        codePage.put("03B5", "Korea (Shift ? KSC 5601)");
        codePage.put("03B6", "Taiwan (Big5)");
        codePage.put("04B0", "Unicode");
        codePage.put("04E2", "Latin-2 (Eastern European)");
        codePage.put("04E3", "Cyrillic");
        codePage.put("04E4", "Multilingual");
        codePage.put("04E5", "Greek");
        codePage.put("04E6", "Turkish");
        codePage.put("04E7", "Hebrew");
        codePage.put("04E8", "Arabic");
    }

    // prohibit instantiation
    private LangAndCodePage() {

    }

    public static void printTranslationInfo(String lang, String cp) {
        StringBuilder builder = new StringBuilder();
        builder.append("Language: ");
        builder.append(languages.get(lang));
        builder.append(" (");
        builder.append(lang);
        builder.append("); ");

        builder.append("CodePage: ");
        builder.append(codePage.get(cp));
        builder.append(" (");
        builder.append(cp);
        builder.append(");");
    }
}