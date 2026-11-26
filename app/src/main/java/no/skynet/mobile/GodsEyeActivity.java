package no.skynet.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.view.View;
import android.widget.*;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.LinkProperties;
import java.net.InetAddress;

public final class GodsEyeActivity extends Activity {
    int nodes;
    String network = "Ukjent";
    String ip = "Ikke tilgjengelig";
    TextView info;

    int dp(float n) {
        return (int)(n * getResources().getDisplayMetrics().density + .5f);
    }

    void refreshData() {
        nodes = getSharedPreferences("skynet_nodes", MODE_PRIVATE)
            .getAll().size();
        try {
            ConnectivityManager cm =
                (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
            Network n = cm == null ? null : cm.getActiveNetwork();
            NetworkCapabilities c = cm == null || n == null
                ? null : cm.getNetworkCapabilities(n);
            network = c != null &&
                c.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                ? "Tilkoblet" : "Ikke bekreftet";

            LinkProperties lp = cm == null || n == null
                ? null : cm.getLinkProperties(n);
            if (lp != null) {
                for (var link : lp.getLinkAddresses()) {
                    InetAddress a = link.getAddress();
                    if (!a.isLoopbackAddress() &&
                        a instanceof java.net.Inet4Address) {
                        ip = a.getHostAddress();
                        break;
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        refreshData();

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(1);
        root.setPadding(dp(16),dp(12),dp(16),dp(16));
        root.setBackgroundColor(Color.rgb(5,12,22));

        TextView title = new TextView(this);
        title.setText("GOD'S EYE VIEW");
        title.setTextColor(Color.WHITE);
        title.setTextSize(25);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("SKYNET • LOCAL NETWORK INTELLIGENCE");
        subtitle.setTextColor(Color.CYAN);
        root.addView(subtitle);

        root.addView(new Radar(this),
            new LinearLayout.LayoutParams(-1,0,1));

        info = new TextView(this);
        info.setTextColor(Color.WHITE);
        info.setTextSize(15);
        info.setPadding(dp(12),dp(12),dp(12),dp(12));
        info.setBackgroundColor(Color.rgb(17,29,43));
        root.addView(info);
        updateInfo();

        Button refresh = new Button(this);
        refresh.setText("Oppdater nettverksdata");
        refresh.setOnClickListener(v -> {
            refreshData();
            updateInfo();
            root.invalidate();
        });
        root.addView(refresh);

        Button openNodes = new Button(this);
        openNodes.setText("Åpne Network Nodes");
        openNodes.setOnClickListener(v ->
            startActivity(new android.content.Intent(
                this, NetworkNodesActivity.class)));
        root.addView(openNodes);

        Button back = new Button(this);
        back.setText("Tilbake");
        back.setOnClickListener(v -> finish());
        root.addView(back);

        setContentView(root);
    }

    void updateInfo() {
        info.setText(
            "SYSTEM STATUS: ACTIVE\n\n" +
            "Nettverk: " + network + "\n" +
            "Lokal IP: " + ip + "\n" +
            "Registrerte SKYNET-noder: " + nodes +
            "\n\nKun faktiske lokale målinger vises."
        );
    }

    final class Radar extends View {
        Paint p = new Paint(3);

        Radar(android.content.Context c) { super(c); }

        @Override protected void onDraw(Canvas c) {
            float x=getWidth()/2f, y=getHeight()/2f;
            float r=Math.min(getWidth(),getHeight())*.38f;
            c.drawColor(Color.rgb(5,12,22));

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1));
            p.setColor(Color.rgb(0,160,180));

            for(int i=1;i<=4;i++)
                c.drawCircle(x,y,r*i/4,p);

            c.drawLine(x-r,y,x+r,y,p);
            c.drawLine(x,y-r,x,y+r,p);

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.CYAN);
            c.drawCircle(x,y,dp(9),p);

            p.setTextSize(dp(12));
            c.drawText("LOCAL NETWORK RADAR",dp(12),dp(24),p);
            c.drawText("NODES: "+nodes,dp(12),dp(44),p);
        }
    }
}
