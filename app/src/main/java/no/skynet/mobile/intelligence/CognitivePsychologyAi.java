package no.skynet.mobile.intelligence;

import java.util.Locale;

public final class CognitivePsychologyAi {
    private CognitivePsychologyAi() {}

    public static String analyze(String input) {
        String q = input == null ? "" : input.trim();
        String lower = q.toLowerCase(Locale.ROOT);

        StringBuilder out = new StringBuilder();
        out.append("COGNITIVE PSYCHOLOGY AI\n");
        out.append("==============================\n");
        out.append("TEKST SOM ANALYSERES\n");
        out.append(q.isEmpty() ? "(Ingen tekst oppgitt)" : q);
        out.append("\n\n");

        int absolute = count(lower,
                "alltid", "aldri", "alle", "ingen", "helt sikkert",
                "umulig", "garantert");

        int uncertainty = count(lower,
                "kanskje", "muligens", "tror", "usikker",
                "kan være", "mulig");

        int questions = 0;
        for (int i = 0; i < q.length(); i++) {
            if (q.charAt(i) == '?') questions++;
        }

        out.append("SPRÅKLIGE SIGNALER\n");
        out.append("- Absolutte uttrykk: ").append(absolute).append("\n");
        out.append("- Usikkerhetsuttrykk: ").append(uncertainty).append("\n");
        out.append("- Spørsmålstegn: ").append(questions).append("\n\n");

        out.append("KOGNITIVE REFLEKSJONSPUNKTER\n");
        if (absolute > 0) {
            out.append("- Undersøk om konklusjoner er mer bastante enn bevisene.\n");
        }
        if (uncertainty > 0) {
            out.append("- Skill mellom dokumentert usikkerhet og antakelser.\n");
        }
        if (questions == 0) {
            out.append("- Formuler gjerne et tydelig spørsmål eller mål.\n");
        }
        out.append("- Se etter alternative forklaringer.\n");
        out.append("- Vurder hvilke observasjoner som kan motbevise hypotesen.\n\n");

        out.append("BEGRENSNING\n");
        out.append("Dette analyserer bare teksten som er oppgitt. Det kan ikke ");
        out.append("fastslå personlighet, intensjoner, psykisk helse eller ");
        out.append("diagnoser. Språksignaler er ikke psykologiske bevis.\n");

        return out.toString();
    }

    private static int count(String text, String... terms) {
        int total = 0;
        for (String term : terms) {
            int from = 0;
            while ((from = text.indexOf(term, from)) >= 0) {
                total++;
                from += term.length();
            }
        }
        return total;
    }
}
