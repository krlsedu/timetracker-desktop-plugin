package com.krlsedu.timetracker.desktop;

import com.krlsedu.timetracker.core.TimeTrackerCore;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

public class Core {
	
	private static final int WAIT_TIME = 100;
	private static boolean ativo = true;
	
	private Core() {
	}
	
	public static void start() {
		ativate();
		TimeTrackerCore.init();
		new Thread(Core::tracker).start();
		Tray.togleLabel();
		if (isAtivo()) {
			Tray.notifyInfo("Plugin initiated!");
		}
	}
	
	private static void tracker() {
		if (TimeTrackerCore.isDebug()) {
			TimeTrackerCore.log.info("Initiated");
		}
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
		if (TimeTrackerCore.isDebug() && !isAtivo()) {
			TimeTrackerCore.log.info("Stooped");
		}
	}
	
	public static void error(Exception e) {
		TimeTrackerCore.log.error(e);
		TimeTrackerCore.log.error(e.fillInStackTrace());
		TimeTrackerCore.log.error(e.getLocalizedMessage());
		TimeTrackerCore.log.error(e.getMessage());
		Tray.notifyError("There was an error in processing!\n" +
				"The plugin will restart.");
		restart();
	}
	
	public static boolean isAtivo() {
		return ativo;
	}
	
	public static void setAtivo(boolean ativo) {
		Core.ativo = ativo;
	}
	
	public static void alternStatus() {
		setAtivo(!Core.isAtivo());
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
		TimeTrackerCore.stopQueue();
		Tray.togleLabel();
	}
	
	public static void restart() {
		stop();
		start();
	}
}
