package no.skynet.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.RectF;
import android.view.View;
import android.widget.*;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.LinkProperties;
import android.net.LinkAddress;
import java.net.InetAddress;

public final class GodsEyeActivity extends Activity {
    int nodes;
    String network = "Ukjent";
    String ip = "Ikke tilgjengelig";
    TextView info;
    Radar radar;

    int dp(float n) {
        return (int)(n * getResources().getDisplayMetrics().density + .5f);
    }

    void refreshData() {
        nodes = getSharedPreferences("skynet_nodes", MODE_PRIVATE)
            .getAll().size();

        network = "Ikke bekreftet";
        ip = "Ikke tilgjengelig";

        try {
            ConnectivityManager cm =
                (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
            Network n = cm == null ? null : cm.getActiveNetwork();
            NetworkCapabilities c = cm == null || n == null
                ? null : cm.getNetworkCapabilities(n);

            if (c != null) {
                if (c.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    network = "Wi-Fi";
                } else if (c.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    network = "Mobildata";
                } else if (c.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    network = "Ethernet";
                } else {
                    network = "Annen nettverkstype";
                }

                if (c.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    network += " • Internett-kapasitet registrert";
                }
            }

            LinkProperties lp = cm == null || n == null
                ? null : cm.getLinkProperties(n);

            if (lp != null) {
                for (LinkAddress link : lp.getLinkAddresses()) {
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
        root.setPadding(dp(16), dp(12), dp(16), dp(16));
        root.setBackgroundColor(Color.rgb(5, 12, 22));

        TextView title = new TextView(this);
        title.setText("GOD'S EYE VIEW");
        title.setTextColor(Color.WHITE);
        title.setTextSize(25);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("SKYNET 2.1 • LOCAL NETWORK INTELLIGENCE");
        subtitle.setTextColor(Color.CYAN);
        root.addView(subtitle);

        radar = new Radar(this);
        root.addView(radar, new LinearLayout.LayoutParams(-1, 0, 1));

        info = new TextView(this);
        info.setTextColor(Color.WHITE);
        info.setTextSize(15);
        info.setPadding(dp(12), dp(12), dp(12), dp(12));
        info.setBackgroundColor(Color.rgb(17, 29, 43));
        root.addView(info);
        updateInfo();

        Button refresh = new Button(this);
        refresh.setText("Oppdater nettverksdata");
        refresh.setOnClickListener(v -> {
            refreshData();
            updateInfo();
            radar.invalidate();
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

    @Override protected void onResume() {
        super.onResume();
        refreshData();
        if (info != null) updateInfo();
        if (radar != null) radar.invalidate();
    }

    void updateInfo() {
        if (info == null) return;
        info.setText(
            "SYSTEM STATUS: ACTIVE\n\n" +
            "Nettverk: " + network + "\n" +
            "Lokal IP: " + ip + "\n" +
            "Registrerte SKYNET-noder: " + nodes +
            "\n\nKun faktiske lokale målinger vises."
        );
    }

    final class Radar extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Handler handler = new Handler(Looper.getMainLooper());
        private float angle = 0f;

        private final Runnable animation = new Runnable() {
            @Override public void run() {
                angle = (angle + 3f) % 360f;
                invalidate();
                handler.postDelayed(this, 50);
            }
        };

        Radar(android.content.Context c) {
            super(c);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        @Override protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            handler.removeCallbacks(animation);
            handler.post(animation);
        }

        @Override protected void onDetachedFromWindow() {
            handler.removeCallbacks(animation);
            super.onDetachedFromWindow();
        }

        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);

            float x = getWidth() / 2f;
            float y = getHeight() / 2f;
            float r = Math.min(getWidth(), getHeight()) * .38f;

            c.drawColor(Color.rgb(5, 12, 22));

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1));
            p.setColor(Color.rgb(0, 160, 180));

            for (int i = 1; i <= 4; i++) {
                c.drawCircle(x, y, r * i / 4f, p);
            }

            c.drawLine(x-r, y, x+r, y, p);
            c.drawLine(x, y-r, x, y+r, p);

            // Radarens roterende sveip
            p.setStrokeWidth(dp(3));
            p.setColor(Color.CYAN);
            p.setAlpha(210);

            c.save();
            c.rotate(angle, x, y);
            c.drawLine(x, y, x, y-r, p);
            c.restore();

            // Kort etterglød bak sveipet
            p.setStrokeWidth(dp(5));
            p.setColor(Color.rgb(0, 190, 170));
            p.setAlpha(100);

            RectF oval = new RectF(x-r, y-r, x+r, y+r);
            c.drawArc(oval, angle-35f, 35f, false, p);

            p.setStyle(Paint.Style.FILL);
            p.setAlpha(255);
            p.setColor(Color.CYAN);
            c.drawCircle(x, y, dp(9), p);

            p.setTextSize(dp(12));
            p.setColor(Color.CYAN);
            c.drawText("LOCAL NETWORK RADAR", dp(12), dp(24), p);

            p.setColor(Color.WHITE);
            c.drawText("NODES: " + nodes, dp(12), dp(44), p);
        }
    }
}
