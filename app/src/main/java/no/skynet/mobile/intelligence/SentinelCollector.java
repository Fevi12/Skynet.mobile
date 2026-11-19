package no.skynet.mobile.intelligence;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import java.util.Set;

public final class SentinelCollector {
    private SentinelCollector() {}

    public static String collect(Context context) {
        StringBuilder out = new StringBuilder();

        out.append("SENTINEL_COLLECTOR\n");
        out.append("==============================\n");
        out.append("Innsamling: lokal øyeblikksmåling\n");
        out.append("Android: ").append(Build.VERSION.RELEASE).append("\n\n");

        out.append("WI-FI\n");
        try {
            WifiManager wifi = (WifiManager)
                    context.getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);

            if (wifi == null || !wifi.isWifiEnabled()) {
                out.append("Wi-Fi er deaktivert eller utilgjengelig.\n");
            } else {
                WifiInfo info = wifi.getConnectionInfo();
                if (info == null) {
                    out.append("Tilkoblingsdata utilgjengelig.\n");
                } else {
                    out.append("Signal: ").append(info.getRssi())
                       .append(" dBm\n");
                    out.append("Frekvens: ").append(info.getFrequency())
                       .append(" MHz\n");
                    out.append("Linkhastighet: ").append(info.getLinkSpeed())
                       .append(" Mbps\n");
                }
            }
        } catch (Exception e) {
            out.append("Wi-Fi-data utilgjengelig.\n");
        }

        out.append("\nNETTVERK\n");
        try {
            ConnectivityManager cm = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Network active = cm == null ? null : cm.getActiveNetwork();
            NetworkCapabilities caps = cm == null || active == null
                    ? null : cm.getNetworkCapabilities(active);

            if (caps == null) {
                out.append("Aktivt nettverk ikke bekreftet.\n");
            } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                out.append("Transport: Wi-Fi\n");
            } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                out.append("Transport: mobilnett\n");
            } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                out.append("Transport: Ethernet\n");
            } else {
                out.append("Transport: annet eller ukjent\n");
            }

            out.append("Internettkapabilitet: ")
               .append(caps != null &&
                       caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                       ? "ja" : "ikke bekreftet")
               .append("\n");
        } catch (Exception e) {
            out.append("Nettverksstatus utilgjengelig.\n");
        }

        out.append("\nBLUETOOTH\n");
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

            if (adapter == null) {
                out.append("Bluetooth støttes ikke.\n");
            } else if (!adapter.isEnabled()) {
                out.append("Bluetooth er deaktivert.\n");
            } else if (Build.VERSION.SDK_INT >= 31 &&
                    context.checkSelfPermission(
                            Manifest.permission.BLUETOOTH_CONNECT)
                            != PackageManager.PERMISSION_GRANTED) {
                out.append("Krever Bluetooth-tillatelse fra brukeren.\n");
            } else {
                Set<BluetoothDevice> paired = adapter.getBondedDevices();
                out.append("Antall parede enheter: ")
                   .append(paired == null ? 0 : paired.size())
                   .append("\n");
            }
        } catch (SecurityException e) {
            out.append("Bluetooth-tillatelse mangler.\n");
        } catch (Exception e) {
            out.append("Bluetooth-data utilgjengelig.\n");
        }

        out.append("\nPERSONVERN\n");
        out.append("- Ingen skjult skanning.\n");
        out.append("- Ingen mikrofon- eller meldingsinnsamling.\n");
        out.append("- Ingen SSID, BSSID, MAC-adresser eller enhetsnavn.\n");
        out.append("- Ingen automatisk opplasting.\n");
        out.append("- Målingen utføres når brukeren ber om den.\n");

        return out.toString();
    }
}
