package no.skynet.mobile;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.*;
import no.skynet.mobile.runtime.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class SkynetCoreActivity extends Activity {
    private SkynetEventBus bus;
    private SkynetOrchestrator core;
    private EditText prompt;
    private TextView result,events;

    private int dp(int n) {
        return (int)(n*getResources().getDisplayMetrics().density+.5f);
    }

    private TextView label(String s,int size) {
        TextView v=new TextView(this);
        v.setText(s); v.setTextColor(Color.WHITE);
        v.setTextSize(size); v.setPadding(dp(8),dp(8),dp(8),dp(8));
        return v;
    }

    private Button button(String s,Runnable r) {
        Button b=new Button(this);
        b.setText(s); b.setAllCaps(false);
        b.setOnClickListener(v->r.run());
        return b;
    }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);

        bus=new SkynetEventBus();
        core=new SkynetOrchestrator(this,bus);

        LinearLayout root=new LinearLayout(this);
        root.setOrientation(1);
        root.setPadding(dp(16),dp(12),dp(16),dp(16));
        root.setBackgroundColor(Color.rgb(5,12,22));

        root.addView(label("SKYNET • SKYNET CORE",25));
        root.addView(label("EVENT-DRIVEN INTELLIGENCE",13));
        root.addView(label("RUNTIME ACTIVE • TOOLS "+core.toolCount(),14));

        prompt=new EditText(this);
        prompt.setTextColor(Color.WHITE);
        prompt.setHintTextColor(Color.LTGRAY);
        prompt.setHint("Gi SKYNET en oppgave...");
        root.addView(prompt,new LinearLayout.LayoutParams(-1,dp(80)));

        root.addView(button("KJØR OPPGAVE",this::runTask));
        root.addView(button("ENHETSSTATUS",()->quick("status")));
        root.addView(button("LAGRINGSSTATUS",()->quick("lagring")));
        root.addView(button("HJELP",()->quick("hjelp")));

        root.addView(label("RESULTAT",17));
        result=label("SKYNET er klar.",14);
        result.setBackgroundColor(Color.rgb(17,29,43));
        root.addView(result);

        root.addView(label("SANNTIDS HENDELSER",17));
        events=label("Ingen hendelser.",12);
        events.setBackgroundColor(Color.rgb(17,29,43));

        ScrollView log=new ScrollView(this);
        log.addView(events);
        root.addView(log,new LinearLayout.LayoutParams(-1,0,1));

        root.addView(button("TILBAKE",this::finish));

        ScrollView page=new ScrollView(this);
        page.addView(root);
        setContentView(page);

        bus.subscribe(e->runOnUiThread(()->{
            String stamp=new SimpleDateFormat(
                "HH:mm:ss",Locale.ROOT).format(new Date(e.timestamp));
            events.setText(stamp+" "+e.type+" ["+
                e.source+"] "+e.detail+"\n"+events.getText());
        }));
    }

    private void quick(String s) {
        prompt.setText(s);
        runTask();
    }

    private void runTask() {
        String q=prompt.getText().toString();
        bus.publish("task_received","ui",q);

        SkynetOrchestrator.Result r=core.handle(q);

        result.setText(
            "INTENSJON: "+r.intent+
            "\nVERKTØY: "+(r.tool.isEmpty()?"Ingen":r.tool)+
            "\nUTFØRT: "+(r.executed?"JA":"NEI")+
            "\n\n"+r.text);
    }

    @Override protected void onDestroy() {
        if(bus!=null) bus.close();
        super.onDestroy();
    }
}
