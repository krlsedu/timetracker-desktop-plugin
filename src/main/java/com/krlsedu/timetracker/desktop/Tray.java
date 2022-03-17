
package com.krlsedu.timetracker.desktop;

import com.krlsedu.timetracker.core.TimeTrackerCore;

import javax.swing.*;
import java.awt.*;

/**
 * @author Carlos Eduardo Duarte Schwalm
 */
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
		
		ImageIcon offIcon = new ImageIcon(ClassLoader.getSystemResource("icon.png"));
		PopupMenu popup = new PopupMenu();
		icon = new TrayIcon(offIcon.getImage(), "TimeTracker-desktop-plugin", popup);
		icon.setImageAutoSize(true);
		try {
			tray.add(icon);
		} catch (AWTException e) {
			TimeTrackerCore.log.error(e);
			return;
		}
		
		MenuItem exit = new MenuItem("Exit");
		exit.addActionListener(e -> {
			tray.remove(icon);
			System.exit(0);
		});
		
		togleExecution = new MenuItem(Core.isAtivo() ? "Stop - monitoring" : "Start - monitoring");
		togleExecution.addActionListener(e -> {
			Core.alternStatus();
			togleLabel();
		});
		
		popup.add(togleExecution);
		popup.add(exit);
	}
	
	public static void togleLabel() {
		togleExecution.setLabel(Core.isAtivo() ? "Stop - monitoring" : "Start - monitoring");
	}
	
	public static void notifyInfo(String msg) {
		icon.displayMessage("TimeTracker desktop plugin", msg, TrayIcon.MessageType.INFO);
	}
	
	public static void notifyError(String msg) {
		icon.displayMessage("TimeTracker desktop plugin", msg, TrayIcon.MessageType.ERROR);
	}
}

