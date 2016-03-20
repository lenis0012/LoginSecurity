package com.lenis0012.bukkit.loginsecurity.modules.migration;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Migration {
    private static final String PROGRESS_FORMAT = "Migrating %s: %s [%s]";
    private long progressUpdateFrequency = 2000L;
    private long nextProgressUpdate = 0L;

    protected int entriedCompleted = 0;
    protected int entriesTotal = 0;

    public abstract boolean executeAutomatically();

    public abstract boolean canExecute();

    public abstract boolean execute();

    public abstract String getName();

    protected void progressUpdate(String status) {
        Logger logger = LoginSecurity.getInstance().getLogger();
        if(nextProgressUpdate <= System.currentTimeMillis()) {
            nextProgressUpdate = System.currentTimeMillis() + progressUpdateFrequency;
            double progress = (entriedCompleted / (double) entriesTotal) * 100.0;
            String progressText = String.valueOf((int) Math.round(progress)) + "%";
            logger.log(Level.INFO, String.format(PROGRESS_FORMAT, getName(), status, progressText));
        }
    }

    protected void log(String message) {
        Logger logger = LoginSecurity.getInstance().getLogger();
        logger.log(Level.INFO, "Migrating " + getName() + ": " + message);
    }
}
