package no.skynet.mobile.distributed;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import java.util.Set;

/**
 * Opt-in local discovery:
 * - Wi-Fi: only devices explicitly advertising _skynet._tcp.
 * - Bluetooth: only devices already paired by the Android user.
 * No stealth pairing, remote installation, copying, or control.
 *
 * The Activity must request BLUETOOTH_CONNECT at runtime on Android 12+,
 * and the user must start/stop discovery explicitly.
 */
public final class SkynetDistributedCore {
    public interface Listener {
        void onWifiPeer(String name, String host, int port);
        void onBluetoothPairedDevice(String name, String address);
        void onStatus(String message);
    }

    private final Context app;
    private final Listener listener;
    private NsdManager nsd;
    private NsdManager.DiscoveryListener discovery;
    private boolean running;

    public SkynetDistributedCore(Context c, Listener l) {
        app = c.getApplicationContext();
        listener = l;
    }

    public synchronized void start() {
        if (running) return;
        running = true;
        report("SKYNET-enhetsoppdagelse startet.");
        listPairedBluetooth();
        startWifiDiscovery();
    }

    public synchronized void stop() {
        running = false;
        if (nsd != null && discovery != null) {
            try { nsd.stopServiceDiscovery(discovery); }
            catch (RuntimeException ignored) { }
        }
        discovery = null;
        report("SKYNET-enhetsoppdagelse stoppet.");
    }

    private void listPairedBluetooth() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) { report("Bluetooth støttes ikke."); return; }
        if (Build.VERSION.SDK_INT >= 31 &&
            app.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            report("Bluetooth-tillatelse mangler. Be brukeren gi tillatelse.");
            return;
        }
        try {
            Set<BluetoothDevice> devices = adapter.getBondedDevices();
            if (devices == null || devices.isEmpty()) {
                report("Ingen parede Bluetooth-enheter funnet.");
                return;
            }
            for (BluetoothDevice d : devices) {
                String name = d.getName();
                listener.onBluetoothPairedDevice(
                    name == null ? "Bluetooth-enhet" : name, d.getAddress());
            }
        } catch (SecurityException e) {
            report("Bluetooth-tilgang ble avslått.");
        }
    }

    private void startWifiDiscovery() {
        nsd = (NsdManager) app.getSystemService(Context.NSD_SERVICE);
        if (nsd == null) { report("Wi-Fi-tjenesteoppdagelse utilgjengelig."); return; }

        discovery = new NsdManager.DiscoveryListener() {
            @Override public void onDiscoveryStarted(String type) {
                report("Søker etter frivillig annonserte SKYNET-enheter på Wi-Fi.");
            }
            @Override public void onServiceFound(NsdServiceInfo service) {
                if (!"_skynet._tcp.".equals(service.getServiceType())) return;
                nsd.resolveService(service, new NsdManager.ResolveListener() {
                    @Override public void onResolveFailed(NsdServiceInfo s, int error) {
                        report("Kunne ikke slå opp SKYNET-tjeneste: " + error);
                    }
                    @Override public void onServiceResolved(NsdServiceInfo s) {
                        if (running && s.getHost() != null)
                            listener.onWifiPeer(s.getServiceName(),
                                s.getHost().getHostAddress(), s.getPort());
                    }
                });
            }
            @Override public void onServiceLost(NsdServiceInfo service) {
                report("En SKYNET-enhet er ikke lenger synlig.");
            }
            @Override public void onDiscoveryStopped(String type) { }
            @Override public void onStartDiscoveryFailed(String type, int error) {
                report("Wi-Fi-oppdagelse feilet: " + error);
                stop();
            }
            @Override public void onStopDiscoveryFailed(String type, int error) {
                report("Kunne ikke stoppe oppdagelsen: " + error);
            }
        };
        try { nsd.discoverServices("_skynet._tcp.", NsdManager.PROTOCOL_DNS_SD, discovery); }
        catch (RuntimeException e) { report("Kunne ikke starte Wi-Fi-oppdagelse."); }
    }

    private void report(String message) {
        if (listener != null) listener.onStatus(message);
    }
}
