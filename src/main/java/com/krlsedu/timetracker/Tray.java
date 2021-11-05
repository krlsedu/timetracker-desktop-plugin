
package com.krlsedu.timetracker;

import com.krlsedu.timetracker.service.WakaTimeCli;

import javax.swing.*;
import java.awt.*;

/**
 * @author Carlos Eduardo Duarte Schwalm
 */
public class Tray {
	private Tray() {
	}
	
	public static void config() {
		
		if (!SystemTray.isSupported()) {
			return;
		}
		
		SystemTray tray = SystemTray.getSystemTray();
		
		ImageIcon offIcon = new ImageIcon(ClassLoader.getSystemResource("icon.png"));
		PopupMenu popup = new PopupMenu();
		TrayIcon icon = new TrayIcon(offIcon.getImage(), "WakaTime-desktop-plugin", popup);
		icon.setImageAutoSize(true);
		try {
			tray.add(icon);
		} catch (AWTException e) {
			WakaTimeCli.log.error(e);
			return;
		}
		
		MenuItem exit = new MenuItem("Exit");
		exit.addActionListener(e -> {
			tray.remove(icon);
			System.exit(0);
		});
		
		MenuItem togleExecution = new MenuItem(Core.isAtivo() ? "Stop - monitoring" : "Start - monitoring");
		togleExecution.addActionListener(e -> {
			Core.setAtivo(!Core.isAtivo());
			togleExecution.setLabel(Core.isAtivo() ? "Stop - monitoring" : "Start - monitoring");
		});
		
		popup.add(togleExecution);
		popup.add(exit);
	}
}

