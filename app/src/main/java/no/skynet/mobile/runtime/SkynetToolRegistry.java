package no.skynet.mobile.runtime;

import android.content.Context;
import android.os.Build;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SkynetToolRegistry {
    public interface Tool {
        String execute(String argument);
    }

    private final Map<String,Tool> tools = new LinkedHashMap<>();

    public SkynetToolRegistry(Context context) {
        Context app=context.getApplicationContext();

        register("device_status", a ->
            "Android "+Build.VERSION.RELEASE+
            " • SDK "+Build.VERSION.SDK_INT+
            " • "+Build.MODEL);

        register("memory_status", a ->
            "Ledig app-lagring: "+
            (app.getFilesDir().getUsableSpace()/(1024*1024))+" MB");

        register("clock", a ->
            java.text.DateFormat.getDateTimeInstance()
                .format(new java.util.Date()));

        register("help", a ->
            "Tilgjengelige verktøy: device_status, memory_status, clock, help.");
    }

    public void register(String name, Tool tool) {
        if (name == null || !name.matches("[a-z_]{2,32}") ||
            tool == null) throw new IllegalArgumentException("Invalid tool");
        tools.put(name,tool);
    }

    public String execute(String name,String argument) {
        Tool t=tools.get(name);
        if(t==null) return "AVVIST: Verktøy ikke tillatt.";
        try { return t.execute(argument==null?"":argument); }
        catch(RuntimeException e) {
            return "VERKTØYFEIL: "+e.getClass().getSimpleName();
        }
    }

    public int size() { return tools.size(); }
}
