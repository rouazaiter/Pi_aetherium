package com.education.platform.services.implementations;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ContentModerationServiceImpl {

    // Comprehensive banned word list (English + common variants)
    private static final List<String> BANNED_WORDS = List.of(
        "fuck", "f*ck", "fck", "fuuck", "fvck",
        "shit", "sh1t", "sh!t", "sht",
        "bitch", "b1tch", "b!tch",
        "asshole", "a**hole", "a55hole",
        "bastard", "b4stard",
        "damn", "d4mn",
        "crap", "cr4p",
        "dick", "d1ck",
        "pussy", "puss1",
        "cock", "c0ck",
        "whore", "wh0re",
        "slut", "sl*t",
        "nigger", "n1gger", "n!gger",
        "faggot", "f4ggot",
        "retard", "ret4rd",
        "idiot", "1diot",
        "moron", "mor0n",
        "stupid", "stup1d",
        "kill yourself", "kys",
        "hate", "h8",
        "rape", "r4pe",
        "terrorist", "terror",
        "nazi", "n4zi",
        "porn", "p0rn",
        "sex", "s3x",
        "nude", "nud3",
        "merde", "putain", "connard", "salope", "enculé", "bordel",
        "puta", "mierda", "cabron", "pendejo",
        "scheisse", "scheiße", "arschloch",
        "cazzo", "vaffanculo",
        "كلب", "حمار", "عاهرة", "كس", "زب", "لعنة"
    );

    // Leet-speak normalization map
    private static final List<String[]> LEET_MAP = List.of(
        new String[]{"@", "a"}, new String[]{"4", "a"},
        new String[]{"3", "e"}, new String[]{"1", "i"},
        new String[]{"!", "i"}, new String[]{"0", "o"},
        new String[]{"5", "s"}, new String[]{"$", "s"},
        new String[]{"7", "t"}, new String[]{"\\+", "t"},
        new String[]{"\\*", ""},  new String[]{"\\.", ""}
    );

    public record ModerationResult(boolean flagged, List<String> detectedWords) {}

    public ModerationResult check(String text) {
        if (text == null || text.isBlank()) return new ModerationResult(false, List.of());

        String normalized = normalize(text.toLowerCase());
        List<String> detected = new ArrayList<>();

        for (String word : BANNED_WORDS) {
            String normalizedWord = normalize(word.toLowerCase());
            // Use word boundary matching to avoid false positives
            Pattern pattern = Pattern.compile(
                "(?i)(^|[\\s,\\.!?;:'\"-])" + Pattern.quote(normalizedWord) + "([\\s,\\.!?;:'\"-]|$)"
            );
            if (pattern.matcher(normalized).find() || normalized.contains(normalizedWord)) {
                detected.add(word);
            }
        }

        return new ModerationResult(!detected.isEmpty(), detected);
    }

    private String normalize(String text) {
        String result = text;
        for (String[] entry : LEET_MAP) {
            result = result.replaceAll(entry[0], entry[1]);
        }
        // Remove repeated characters (e.g. "fuuuck" → "fuck")
        result = result.replaceAll("(.)\\1{2,}", "$1$1");
        return result;
    }
}
