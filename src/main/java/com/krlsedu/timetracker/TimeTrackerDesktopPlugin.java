package com.krlsedu.timetracker;


import com.krlsedu.timetracker.desktop.Core;
import com.krlsedu.timetracker.desktop.Tray;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TimeTrackerDesktopPlugin {

    public static void main(String[] args) {
        Tray.config();
        Core.start();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            Tray.notifyError(e.getMessage());
            Core.restart();
        });
        SpringApplication.run(TimeTrackerDesktopPlugin.class, args);
    }
}