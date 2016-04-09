package com.lenis0012.bukkit.loginsecurity.modules.migration;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Migration {
    private static final String PROGRESS_FORMAT = "Migrating %s: %s [%s]";
    private long progressUpdateFrequency = 2000L;
    private long nextProgressUpdate = 0L;

    protected int entriesCompleted = 0;
    protected int entriesTotal = 0;

    public abstract boolean executeAutomatically();

    public abstract boolean canExecute();

    public abstract boolean execute();

    public abstract String getName();

    protected void progressUpdate(String status) {
        Logger logger = LoginSecurity.getInstance().getLogger();
        if(nextProgressUpdate <= System.currentTimeMillis()) {
            nextProgressUpdate = System.currentTimeMillis() + progressUpdateFrequency;
            double progress = (entriesCompleted / (double) entriesTotal) * 100.0;
            String progressText = String.valueOf((int) Math.round(progress)) + "%";
            logger.log(Level.INFO, String.format(PROGRESS_FORMAT, getName(), status, progressText));
        }
    }

    protected void log(String message) {
        Logger logger = LoginSecurity.getInstance().getLogger();
        logger.log(Level.INFO, "Migrating " + getName() + ": " + message);
    }

    protected void copyFile(File from, File to) throws IOException {
        copyFile(new FileInputStream(from), to);
    }

    protected void copyFile(InputStream from, File to) throws IOException {
        FileOutputStream output = null;
        to.mkdirs();
        try {
            output = new FileOutputStream(to);
            byte[] buffer = new byte[1024];
            int length;
            while((length = from.read(buffer)) != -1) {
                output.write(buffer, 0, length);
            }
        } finally {
            try {
                from.close();
            } catch(IOException e) {
            }
            if(output != null) {
                try {
                    output.close();
                } catch(IOException e) {
                }
            }
        }
    }
}
