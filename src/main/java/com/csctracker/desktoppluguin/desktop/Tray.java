
package com.csctracker.desktoppluguin.desktop;

import com.csctracker.desktoppluguin.core.ConfigFile;
import com.csctracker.desktoppluguin.core.SqlLitle;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

/**
 * @author Carlos Eduardo Duarte Schwalm
 */
@Slf4j
public class Tray {
	private static MenuItem togleExecution;
	private static TrayIcon icon;
	
	private Tray() {
	}
	
	public static void config() {

		if (!SystemTray.isSupported()) {
			return;
		}

		SystemTray tray = SystemTray.getSystemTray();

		ImageIcon offIcon = new ImageIcon(ConfigFile.getResourcesLocation() + "\\csctracker-desktop-plugin.png");
		PopupMenu popup = new PopupMenu();
		icon = new TrayIcon(offIcon.getImage(), "CscTracker-desktop-plugin", popup);
		icon.setImageAutoSize(true);
		try {
			tray.add(icon);
		} catch (AWTException e) {
			log.error(e.getMessage());
			return;
		}

		MenuItem exit = new MenuItem("Exit");
		exit.addActionListener(e -> {
			tray.remove(icon);
			System.exit(0);
		});

		MenuItem syncBkp = new MenuItem("Sync bkps");
		syncBkp.addActionListener(e -> {
			try {
				SqlLitle.syncBkps();
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		});

		MenuItem backupDb = new MenuItem("Backup errors");
		backupDb.addActionListener(e -> {
			try {
				SqlLitle.generateBackup();
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		});

		togleExecution = new MenuItem(Core.isAtivo() ? "Stop - monitoring" : "Start - monitoring");
		togleExecution.addActionListener(e -> {
			Core.alternStatus();
			togleLabel();
		});

		popup.add(togleExecution);
		popup.add(backupDb);
		popup.add(syncBkp);
		popup.add(exit);
	}
	
	public static void togleLabel() {
		togleExecution.setLabel(Core.isAtivo() ? "Stop - monitoring" : "Start - monitoring");
	}
	
	public static void notifyInfo(String msg) {
		icon.displayMessage("CscTracker desktop plugin", msg, TrayIcon.MessageType.INFO);
	}
	
	public static void notifyError(String msg) {
		icon.displayMessage("CscTracker desktop plugin", msg, TrayIcon.MessageType.ERROR);
	}
}

