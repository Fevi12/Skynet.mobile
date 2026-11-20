package no.skynet.mobile.intelligence;

import java.util.Locale;

public final class StrategicMind {
    private StrategicMind() {}

    public static String analyze(String input) {
        String q = input == null ? "" : input.trim();
        String s = q.toLowerCase(Locale.ROOT);

        StringBuilder r = new StringBuilder();

        r.append("SKYNET STRATEGIC MIND\n");
        r.append("==============================\n");
        r.append("FELLES STRATEGISK ANALYSE\n\n");

        r.append("1. MAKT OG INTERESSER\n");
        r.append("Inspirert av Machiavelli.\n");
        r.append("- Identifiser aktører og interesser.\n");
        r.append("- Kartlegg maktforhold og avhengigheter.\n");
        r.append("- Skill offentlig retorikk fra dokumenterte handlinger.\n\n");

        r.append("2. STRATEGI OG KONFLIKT\n");
        r.append("Inspirert av Sun Tzu og Clausewitz.\n");
        r.append("- Vurder situasjon og strategisk kontekst.\n");
        r.append("- Identifiser friksjon og usikkerhet.\n");
        r.append("- Se etter muligheter for forebygging.\n\n");

        r.append("3. PSYKOLOGI OG PÅVIRKNING\n");
        r.append("Inspirert av CIA-analyse og Edward Bernays.\n");
        r.append("- Undersøk informasjonskilder.\n");
        r.append("- Identifiser mulige påvirkningsforsøk.\n");
        r.append("- Se etter gjentatte fortellinger og følelsesappeller.\n");
        r.append("- Skill påvirkningshypoteser fra bevis.\n\n");

        r.append("4. ØKONOMISK INTELLIGENS\n");
        r.append("Inspirert av Friedman og Akerlof.\n");
        r.append("- Undersøk økonomiske insentiver.\n");
        r.append("- Identifiser informasjonsasymmetri.\n");
        r.append("- Vurder risiko for adverse selection.\n");
        r.append("- Undersøk hvem som har mest informasjon.\n\n");

        r.append("5. RISIKO OG TIDLIG VARSLING\n");

        boolean risk =
            s.contains("trussel") ||
            s.contains("krise") ||
            s.contains("angrep") ||
            s.contains("risiko") ||
            s.contains("konflikt");

        r.append("Risikosignal: ")
         .append(risk ? "TEMA IDENTIFISERT" : "IKKE IDENTIFISERT")
         .append("\n");

        r.append("- Hva er observert?\n");
        r.append("- Hva er bare antatt?\n");
        r.append("- Hvilke indikatorer kan bekrefte utviklingen?\n");
        r.append("- Hvilke indikatorer kan avkrefte hypotesen?\n");
        r.append("- Hvilke forebyggende tiltak er mulige?\n\n");

        r.append("6. BESLUTNINGSMODELL\n");
        r.append("A. Definer situasjonen.\n");
        r.append("B. Samle verifiserbare opplysninger.\n");
        r.append("C. Lag alternative hypoteser.\n");
        r.append("D. Vurder sannsynlighet og konsekvens.\n");
        r.append("E. Prioriter forebyggende tiltak.\n");
        r.append("F. Evaluer nye observasjoner.\n\n");

        r.append("7. KONKLUSJON\n");
        r.append("Dette er en regelbasert analysemodul.\n");
        r.append("Den har ikke tilgang til skjulte etterretningskilder.\n");
        r.append("Ingen sannsynlighet eller trussel er verifisert\n");
        r.append("uten faktiske data og uavhengig dokumentasjon.\n");

        return r.toString();
    }
}
