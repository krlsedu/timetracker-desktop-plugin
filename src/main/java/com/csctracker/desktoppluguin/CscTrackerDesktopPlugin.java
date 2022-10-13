package com.csctracker.desktoppluguin;


import com.csctracker.desktoppluguin.core.NotificationSync;
import com.csctracker.desktoppluguin.desktop.Core;
import com.csctracker.desktoppluguin.desktop.Tray;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class CscTrackerDesktopPlugin {

    public static void main(String[] args) {
        Tray.config();
        Core.start();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            Tray.notifyError(e.getMessage());
            Core.restart();
        });
        SpringApplication.run(CscTrackerDesktopPlugin.class, args);
        NotificationSync.notificationTracker();
    }
}
