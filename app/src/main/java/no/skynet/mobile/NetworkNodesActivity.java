package no.skynet.mobile;

import android.app.Activity;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.net.InetAddress;
import java.util.Map;

/**
 * User-started discovery of devices explicitly advertising a SKYNET agent.
 * Uses Android NSD/mDNS only; it does not scan address ranges or ports.
 */
public final class NetworkNodesActivity extends Activity {
    private static final String TYPE = "_skynet._tcp.";
    private static final String PREFS = "skynet_nodes";
    private NsdManager nsd;
    private NsdManager.DiscoveryListener discovery;
    private boolean active;
    private LinearLayout results;
    private TextView state;

    private int dp(int n) {
        return (int) (n * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView label(String value, int size) {
        TextView v = new TextView(this);
        v.setText(value); v.setTextSize(size); v.setTextColor(0xffffffff);
        v.setPadding(dp(8), dp(8), dp(8), dp(8));
        return v;
    }

    private Button button(String value, Runnable action) {
        Button b = new Button(this); b.setText(value); b.setAllCaps(false);
        b.setOnClickListener(v -> action.run()); return b;
    }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        nsd = (NsdManager) getSystemService(Context.NSD_SERVICE);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(1); root.setPadding(dp(16), dp(12), dp(16), dp(16));
        root.setBackgroundColor(0xff050c16);
        root.addView(label("SKYNET • NETWORK NODES", 22));
        root.addView(label("Oppdag kun SKYNET-agenter som selv annonserer tjenesten på lokalnettet.", 14));
        state = label("Inaktiv. Ingen nettverksoppdagelse kjører.", 14);
        root.addView(state);
        root.addView(button("Start lokal oppdagelse", this::startDiscovery));
        root.addView(button("Stopp oppdagelse", this::stopDiscovery));
        root.addView(button("Tilbake", this::finish));
        root.addView(label("FUNNE NODER • LOKAL HISTORIKK", 17));
        ScrollView scroll = new ScrollView(this);
        results = new LinearLayout(this); results.setOrientation(1);
        scroll.addView(results);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        refresh();
    }

    private void startDiscovery() {
        if (active || nsd == null) return;
        discovery = new NsdManager.DiscoveryListener() {
            @Override public void onDiscoveryStarted(String type) {
                active = true;
                runOnUiThread(() -> state.setText("Oppdagelse aktiv: " + TYPE));
            }
            @Override public void onServiceFound(NsdServiceInfo service) {
                if (!TYPE.equals(service.getServiceType())) return;
                nsd.resolveService(service, new NsdManager.ResolveListener() {
                    @Override public void onResolveFailed(NsdServiceInfo s, int error) { }
                    @Override public void onServiceResolved(NsdServiceInfo s) {
                        InetAddress host = s.getHost();
                        if (host == null) return;
                        String name = s.getServiceName();
                        String value = name + " • " + host.getHostAddress() + ":" + s.getPort();
                        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                                .putString(name, value).apply();
                        runOnUiThread(NetworkNodesActivity.this::refresh);
                    }
                });
            }
            @Override public void onServiceLost(NsdServiceInfo service) {
                runOnUiThread(() -> state.setText("En annonsert node forsvant. Historikken beholdes."));
            }
            @Override public void onDiscoveryStopped(String type) {
                active = false;
                runOnUiThread(() -> state.setText("Oppdagelse stoppet. Historikken er lagret lokalt."));
            }
            @Override public void onStartDiscoveryFailed(String type, int error) {
                active = false; nsd.stopServiceDiscovery(this);
                runOnUiThread(() -> state.setText("Startfeil: " + error));
            }
            @Override public void onStopDiscoveryFailed(String type, int error) {
                nsd.stopServiceDiscovery(this);
                runOnUiThread(() -> state.setText("Stoppfeil: " + error));
            }
        };
        try { nsd.discoverServices(TYPE, NsdManager.PROTOCOL_DNS_SD, discovery); }
        catch (RuntimeException e) {
            active = false; state.setText("Oppdagelse utilgjengelig: " + e.getClass().getSimpleName());
        }
    }

    private void stopDiscovery() {
        if (!active || discovery == null || nsd == null) return;
        try { nsd.stopServiceDiscovery(discovery); }
        catch (RuntimeException e) { active = false; }
    }

    private void refresh() {
        if (results == null) return;
        results.removeAllViews();
        Map<String, ?> saved = getSharedPreferences(PREFS, MODE_PRIVATE).getAll();
        if (saved.isEmpty()) {
            results.addView(label("Ingen SKYNET-noder funnet. Andre enheter må ha en kompatibel agent installert og aktivert.", 14));
            return;
        }
        for (Map.Entry<String, ?> entry : saved.entrySet())
            results.addView(label("• " + entry.getValue(), 14));
    }

    @Override protected void onDestroy() {
        stopDiscovery();
        super.onDestroy();
    }
}
