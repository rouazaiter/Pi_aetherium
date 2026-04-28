package tn.esprit.aetherium.skillhubbackend.services.implementations;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SentimentAnalysisServiceImpl {

    // Positive word lexicon (English + French + Arabic)
    private static final List<String> POSITIVE_WORDS = List.of(
        // English
        "good","great","excellent","amazing","awesome","fantastic","wonderful","brilliant",
        "love","like","enjoy","happy","glad","pleased","thankful","grateful","appreciate",
        "perfect","best","beautiful","nice","helpful","useful","interesting","impressive",
        "well done","congratulations","bravo","superb","outstanding","magnificent","splendid",
        "insightful","informative","clear","concise","accurate","correct","right","true",
        "agree","absolutely","definitely","certainly","yes","exactly","indeed","of course",
        "recommend","share","follow","subscribe","support","encourage","motivate","inspire",
        // French
        "bien","très bien","excellent","magnifique","parfait","super","génial","bravo",
        "merci","sympa","agréable","utile","intéressant","clair","précis","correct",
        "j'aime","j'adore","félicitations","impressionnant","remarquable","formidable",
        // Arabic
        "ممتاز","رائع","جميل","مفيد","شكرا","أحسنت","بارك الله","جيد","صحيح","أتفق",
        "مثير","مثير للاهتمام","واضح","دقيق","مذهل","رائع جدا"
    );

    // Negative word lexicon (English + French + Arabic)
    private static final List<String> NEGATIVE_WORDS = List.of(
        // English
        "bad","terrible","awful","horrible","disgusting","hate","dislike","boring","useless",
        "wrong","incorrect","false","misleading","confusing","unclear","poor","worst","ugly",
        "stupid","dumb","idiot","waste","trash","garbage","spam","fake","lie","lying",
        "disagree","never","no","not","don't","doesn't","won't","can't","shouldn't",
        "disappointed","frustrating","annoying","irritating","pathetic","ridiculous","absurd",
        "fail","failure","error","mistake","problem","issue","bug","broken","crash",
        // French
        "mauvais","terrible","horrible","nul","inutile","faux","incorrect","ennuyeux",
        "déçu","frustrant","agaçant","pathétique","ridicule","absurde","problème","erreur",
        "je n'aime pas","déteste","pas bien","pas correct","pas utile","dommage",
        // Arabic
        "سيء","فظيع","مروع","مملل","خاطئ","غلط","لا أتفق","مشكلة","خطأ","فشل",
        "لا يعجبني","مزعج","محبط","سخيف","غير مفيد","غير صحيح"
    );

    // Intensifiers that boost score
    private static final List<String> INTENSIFIERS = List.of(
        "very","really","so","extremely","absolutely","totally","completely","highly",
        "très","vraiment","absolument","complètement","جداً","للغاية"
    );

    public enum Sentiment { POSITIVE, NEGATIVE, NEUTRAL }

    public record SentimentResult(Sentiment sentiment, int score, double confidence) {}

    public SentimentResult analyze(String text) {
        if (text == null || text.isBlank()) {
            return new SentimentResult(Sentiment.NEUTRAL, 0, 1.0);
        }

        String lower = text.toLowerCase();
        String[] words = lower.split("[\\s,\\.!?;:'\"-]+");

        int score = 0;
        boolean nextIntensified = false;

        for (String word : words) {
            if (INTENSIFIERS.contains(word)) {
                nextIntensified = true;
                continue;
            }

            int multiplier = nextIntensified ? 2 : 1;
            nextIntensified = false;

            if (POSITIVE_WORDS.contains(word)) score += multiplier;
            else if (NEGATIVE_WORDS.contains(word)) score -= multiplier;
        }

        // Also check multi-word phrases
        for (String phrase : POSITIVE_WORDS) {
            if (phrase.contains(" ") && lower.contains(phrase)) score += 2;
        }
        for (String phrase : NEGATIVE_WORDS) {
            if (phrase.contains(" ") && lower.contains(phrase)) score -= 2;
        }

        // Negation detection: "not good" → flip
        if (lower.matches(".*(not|never|no|n't)\\s+\\w+.*")) {
            score = -score;
        }

        Sentiment sentiment;
        if (score > 0) sentiment = Sentiment.POSITIVE;
        else if (score < 0) sentiment = Sentiment.NEGATIVE;
        else sentiment = Sentiment.NEUTRAL;

        double confidence = Math.min(1.0, Math.abs(score) * 0.25 + 0.5);

        return new SentimentResult(sentiment, score, confidence);
    }
}
