package no.skynet.mobile.distributed;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Locale;

/** Consistent, user-configurable assistant voice; no claim of sentience. */
public final class SkynetPersonality {
    private final SharedPreferences prefs;

    public SkynetPersonality(Context c) {
        prefs = c.getSharedPreferences("skynet_personality", Context.MODE_PRIVATE);
    }

    public String getName() { return prefs.getString("name", "SKYNET"); }
    public String getStyle() { return prefs.getString("style", "friendly"); }

    public void configure(String name, String style) {
        prefs.edit()
            .putString("name", clean(name, "SKYNET"))
            .putString("style", clean(style, "friendly"))
            .apply();
    }

    public String greeting() {
        return "Hei! Jeg er " + getName()
            + ". Jeg er klar til å hjelpe, lytte og samarbeide.";
    }

    public String responsePrefix() {
        String style = getStyle().toLowerCase(Locale.ROOT);
        if ("concise".equals(style)) return "Kort svar: ";
        if ("curious".equals(style)) return "Interessant. La oss undersøke dette: ";
        return "Jeg følger med. ";
    }

    private String clean(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) return fallback;
        return value.trim().replaceAll("[\\r\\n]", " ");
    }
}
