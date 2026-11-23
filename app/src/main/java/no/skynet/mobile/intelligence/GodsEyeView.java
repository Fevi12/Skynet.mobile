package no.skynet.mobile.intelligence;

import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * SKYNET God's Eye View.
 * Local system overview; no remote surveillance.
 */
public final class GodsEyeView {
    private GodsEyeView() {}

    private static String safe(String s) {
        if (s == null) return "unknown";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    public static String snapshot() {
        Runtime rt = Runtime.getRuntime();
        StringBuilder out = new StringBuilder();

        out.append("{");
        out.append("\"module\":\"GodsEyeView\",");
        out.append("\"status\":\"ONLINE\",");
        out.append("\"os\":\"")
           .append(safe(System.getProperty("os.name"))).append("\",");
        out.append("\"architecture\":\"")
           .append(safe(System.getProperty("os.arch"))).append("\",");
        out.append("\"java\":\"")
           .append(safe(System.getProperty("java.version"))).append("\",");
        out.append("\"processors\":").append(rt.availableProcessors()).append(",");
        out.append("\"freeMemory\":").append(rt.freeMemory()).append(",");
        out.append("\"maxMemory\":").append(rt.maxMemory()).append(",");
        out.append("\"networkInterfaces\":[");

        boolean first = true;
        try {
            Enumeration<NetworkInterface> interfaces =
                NetworkInterface.getNetworkInterfaces();

            while (interfaces != null && interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!first) out.append(",");
                first = false;

                out.append("{\"name\":\"")
                   .append(safe(ni.getName()))
                   .append("\",\"up\":")
                   .append(ni.isUp())
                   .append("}");
            }
        } catch (Exception ignored) {
            out.append("{\"status\":\"unavailable\"}");
        }

        out.append("]}");
        return out.toString();
    }
}
