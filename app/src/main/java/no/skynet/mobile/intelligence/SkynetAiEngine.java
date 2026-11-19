package no.skynet.mobile.intelligence;

import android.content.Context;

public final class SkynetAiEngine {
    private SkynetAiEngine() {}

    public static String run(String prompt, Context context) {
        StringBuilder result = new StringBuilder();

        result.append("SKYNET AI ENGINE\n");
        result.append("################################\n");
        result.append("HOVEDMOTOR: aktiv\n");
        result.append("INTELLIGENCE AI: tilkoblet\n");
        result.append("COGNITIVE PSYCHOLOGY AI: tilkoblet\n");
        result.append("SENTINEL_COLLECTOR: tilkoblet\n\n");

        result.append(IntelligenceAi.analyze(prompt));
        result.append("\n\n");
        result.append(CognitivePsychologyAi.analyze(prompt));
        result.append("\n\n");
        result.append(SentinelCollector.collect(context));

        result.append("\n\nSAMLET STATUS\n");
        result.append("Modulene har kjørt lokalt. Resultatene er ");
        result.append("regelbaserte analyser, ikke generert av en stor språkmodell.\n");

        return result.toString();
    }
}
