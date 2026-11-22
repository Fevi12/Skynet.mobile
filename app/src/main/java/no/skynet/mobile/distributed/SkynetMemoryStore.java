package no.skynet.mobile.distributed;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Small local conversation memory. User can inspect and clear it. */
public final class SkynetMemoryStore {
    private static final String KEY = "entries";
    private final SharedPreferences prefs;

    public SkynetMemoryStore(Context c) {
        prefs = c.getSharedPreferences("skynet_memory", Context.MODE_PRIVATE);
    }

    public synchronized void remember(String role, String message) {
        if (message == null || message.trim().isEmpty()) return;
        try {
            JSONArray a = new JSONArray(prefs.getString(KEY, "[]"));
            JSONObject item = new JSONObject();
            item.put("role", safe(role, "user"));
            item.put("message", message.trim());
            item.put("time", System.currentTimeMillis());
            a.put(item);
            while (a.length() > 300) a.remove(0);
            prefs.edit().putString(KEY, a.toString()).apply();
        } catch (JSONException ignored) { }
    }

    public synchronized JSONArray readAll() {
        try { return new JSONArray(prefs.getString(KEY, "[]")); }
        catch (JSONException e) { return new JSONArray(); }
    }

    public synchronized void clear() {
        prefs.edit().remove(KEY).apply();
    }

    public synchronized String exportJson() {
        return readAll().toString();
    }

    public synchronized boolean importJson(String json) {
        try {
            JSONArray incoming = new JSONArray(json);
            if (incoming.length() > 300) return false;
            for (int i = 0; i < incoming.length(); i++) {
                JSONObject item = incoming.getJSONObject(i);
                if (!item.has("role") || !item.has("message")) return false;
            }
            prefs.edit().putString(KEY, incoming.toString()).apply();
            return true;
        } catch (Exception e) { return false; }
    }

    private String safe(String s, String fallback) {
        return s == null || s.trim().isEmpty() ? fallback : s.trim();
    }
}
