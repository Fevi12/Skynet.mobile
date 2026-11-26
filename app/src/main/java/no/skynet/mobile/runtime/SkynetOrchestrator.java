package no.skynet.mobile.runtime;

import android.content.Context;
import java.util.Locale;

public final class SkynetOrchestrator {

    public static final class Result {
        public final String intent,tool,text;
        public final boolean executed;

        Result(String i,String t,String x,boolean e) {
            intent=i; tool=t; text=x; executed=e;
        }
    }

    private final SkynetToolRegistry registry;
    private final SkynetEventBus bus;

    public SkynetOrchestrator(Context c,SkynetEventBus b) {
        registry=new SkynetToolRegistry(c);
        bus=b;
    }

    public Result handle(String input) {
        String q=input==null?"":input.trim();
        String s=q.toLowerCase(new Locale("nb","NO"));

        if(s.isEmpty())
            return new Result("empty","","Skriv en oppgave.",false);

        String tool=null;

        if(s.contains("status") || s.contains("android") ||
           s.contains("enhet")) tool="device_status";
        else if(s.contains("lagring") || s.contains("minneplass"))
            tool="memory_status";
        else if(s.contains("klokke") || s.contains("tid"))
            tool="clock";
        else if(s.equals("hjelp") || s.contains("hva kan du"))
            tool="help";

        if(tool==null) {
            bus.publish("task_unmatched","orchestrator",q);
            return new Result("conversation","",
                "Oppgave mottatt. En språkmodell eller et godkjent verktøy må konfigureres for denne oppgaven. Ingen handling utført.",
                false);
        }

        bus.publish("tool_requested","orchestrator",tool);
        String answer=registry.execute(tool,q);
        bus.publish("tool_completed",tool,answer);

        return new Result(tool,tool,answer,true);
    }

    public int toolCount() { return registry.size(); }
}
