package com.csctracker.desktoppluguin.desktop;

import com.csctracker.desktoppluguin.core.Configs;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Core {

    public static final int WAIT_TIME = 100;
    private static boolean ativo = true;

    private Core() {
    }

    public static void start() {
        ativate();
        com.csctracker.desktoppluguin.core.Core.init();
        new Thread(com.csctracker.desktoppluguin.desktop.Core::tracker).start();
        if (SystemInfo.isWindows()) {
            Tray.togleLabel();
        }
        if (isAtivo()) {
            if (SystemInfo.isWindows()) {
                Tray.notifyInfo("Plugin initiated!");
            }
        }
    }

    private static void tracker() {
        if (Configs.isDebug()) {
            log.info("Initiated");
        }
        if (!SystemInfo.isWindows()) {
            var linuxSystemInfo = new LinuxSystemInfo();
            linuxSystemInfo.start();
            do {
                try {
                    Thread.sleep(WAIT_TIME);
                    ApplicationDetailService.generateApplicationDetailInfoLinux(linuxSystemInfo);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                    error(e);
                    break;
                }
            } while (isAtivo());
        } else {
            do {
                try {
                    Thread.sleep(WAIT_TIME);
                    WinDef.HWND foregroundWindow = User32.INSTANCE.GetForegroundWindow();

                    if (foregroundWindow != null) {
                        ApplicationDetailService.generateApplicationDetailInfo(foregroundWindow);
                    }
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                    error(e);
                    break;
                }
            } while (isAtivo());
        }
        if (Configs.isDebug() && !isAtivo()) {
            log.info("Stooped");
        }
    }

    public static void error(Exception e) {
        log.error(e.getMessage());
        log.error(e.getLocalizedMessage());
        log.error(e.getMessage());
        Tray.notifyError("There was an error in processing!\n" +
                "The plugin will restart.");
        restart();
    }

    public static boolean isAtivo() {
        return ativo;
    }

    public static void setAtivo(boolean ativo) {
        com.csctracker.desktoppluguin.desktop.Core.ativo = ativo;
    }

    public static void alternStatus() {
        setAtivo(!com.csctracker.desktoppluguin.desktop.Core.isAtivo());
    }

    public static void ativate() {
        setAtivo(true);
    }

    public static void desativate() {
        setAtivo(false);
    }

    public static void stop() {
        desativate();
        ApplicationDetailService.clearAplicationDetail();
        com.csctracker.desktoppluguin.core.Core.stopQueue();
        Tray.togleLabel();
    }

    public static void restart() {
        stop();
        start();
    }
}
