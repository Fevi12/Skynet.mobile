package no.skynet.mobile.distributed;

import android.content.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Explicit local export/import. Backup files are user data, not an undeletable
 * persistence mechanism. Copy the export to a user-controlled safe location.
 */
public final class SkynetRecovery {
    private final Context app;
    private final SkynetMemoryStore memory;

    public SkynetRecovery(Context c) {
        app = c.getApplicationContext();
        memory = new SkynetMemoryStore(app);
    }

    public File exportMemory() throws Exception {
        File out = new File(app.getFilesDir(), "skynet-memory-backup.json");
        byte[] bytes = memory.exportJson().getBytes(StandardCharsets.UTF_8);
        try (FileOutputStream stream = new FileOutputStream(out)) {
            stream.write(bytes);
            stream.getFD().sync();
        }
        return out;
    }

    public boolean restoreMemory(File backup) {
        if (backup == null || !backup.isFile() || backup.length() > 2_000_000) return false;
        try (FileInputStream in = new FileInputStream(backup)) {
            byte[] bytes = new byte[(int) backup.length()];
            int offset = 0, count;
            while (offset < bytes.length &&
                   (count = in.read(bytes, offset, bytes.length - offset)) > 0) offset += count;
            return memory.importJson(new String(bytes, 0, offset, StandardCharsets.UTF_8));
        } catch (Exception e) { return false; }
    }
}
