package no.skynet.mobile.intelligence;

import java.util.Locale;

public final class IntelligenceAi {
    private IntelligenceAi() {}

    public static String analyze(String input) {
        String q = input == null ? "" : input.trim();
        if (q.isEmpty()) q = "Ingen problemstilling oppgitt.";

        String lower = q.toLowerCase(Locale.ROOT);
        StringBuilder out = new StringBuilder();

        out.append("INTELLIGENCE AI\n");
        out.append("==============================\n");
        out.append("PROBLEMSTILLING\n").append(q).append("\n\n");

        out.append("ANALYTISK VURDERING\n");
        if (lower.contains("wifi") || lower.contains("nettverk")
                || lower.contains("bluetooth")) {
            out.append("- Tema: nettverk og enhetsstatus.\n");
            out.append("- Skill observerte måleverdier fra antakelser.\n");
        } else if (lower.contains("risiko") || lower.contains("trussel")) {
            out.append("- Tema: risikoanalyse.\n");
            out.append("- Vurder sannsynlighet, konsekvens og dokumentasjon separat.\n");
        } else {
            out.append("- Tema: generell problem- og informasjonsanalyse.\n");
        }

        out.append("\nANALYTISK METODE\n");
        out.append("1. Definer spørsmålet.\n");
        out.append("2. Skill fakta fra hypoteser.\n");
        out.append("3. Identifiser manglende informasjon.\n");
        out.append("4. Vurder alternative forklaringer.\n");
        out.append("5. Presenter konklusjon med usikkerhet.\n\n");

        out.append("KONFIDENS\n");
        out.append("Lav – ingen eksterne kilder eller uavhengige målinger er ")
           .append("verifisert av denne modulen.\n");

        out.append("\nVIKTIG\n");
        out.append("Dette er en lokal analytisk støttefunksjon, ikke en ");
        out.append("etterretningskilde eller en verifisert trusseldetektor.\n");

        return out.toString();
    }
}
