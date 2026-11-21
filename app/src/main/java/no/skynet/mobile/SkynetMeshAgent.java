package no.skynet.mobile;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SkynetMeshAgent {
    private final Context context;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ServerSocket server;
    private Thread worker;
    private NsdManager nsd;
    private NsdManager.RegistrationListener registration;

    public SkynetMeshAgent(Context c) {
        context = c.getApplicationContext();
    }

    public synchronized boolean start() {
        if (running.get()) return true;
        try {
            server = new ServerSocket(0);
            running.set(true);
            worker = new Thread(() -> {
                while (running.get()) {
                    try (Socket s = server.accept()) {
                        s.setSoTimeout(3000);
                        BufferedReader in = new BufferedReader(
                            new InputStreamReader(s.getInputStream()));
                        String request = in.readLine();
                        String body = request != null &&
                            request.startsWith("GET /health ")
                            ? "{\"service\":\"SKYNET\",\"status\":\"ready\",\"protocol\":1}"
                            : "{\"error\":\"not_found\"}";
                        byte[] data = body.getBytes(StandardCharsets.UTF_8);
                        OutputStream out = s.getOutputStream();
                        out.write(("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: "
                            + data.length + "\r\nConnection: close\r\n\r\n")
                            .getBytes(StandardCharsets.UTF_8));
                        out.write(data);
                        out.flush();
                    } catch (Exception ignored) {
                        if (!running.get()) break;
                    }
                }
            }, "SKYNET-Mesh");
            worker.setDaemon(true);
            worker.start();

            nsd = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
            if (nsd == null) { stop(); return false; }

            NsdServiceInfo info = new NsdServiceInfo();
            info.setServiceName("SKYNET-" + android.os.Build.MODEL.replaceAll("[^A-Za-z0-9_-]", ""));
            info.setServiceType("_skynet._tcp.");
            info.setPort(server.getLocalPort());

            registration = new NsdManager.RegistrationListener() {
                public void onServiceRegistered(NsdServiceInfo s) {}
                public void onRegistrationFailed(NsdServiceInfo s, int e) { stop(); }
                public void onServiceUnregistered(NsdServiceInfo s) {}
                public void onUnregistrationFailed(NsdServiceInfo s, int e) {}
            };
            nsd.registerService(info, NsdManager.PROTOCOL_DNS_SD, registration);
            return true;
        } catch (Exception e) {
            stop();
            return false;
        }
    }

    public synchronized void stop() {
        running.set(false);
        if (nsd != null && registration != null) {
            try { nsd.unregisterService(registration); }
            catch (Exception ignored) {}
        }
        registration = null;
        if (server != null) try { server.close(); }
        catch (Exception ignored) {}
        server = null;
        worker = null;
    }
}
