package com.krlsedu.timetracker;

import com.krlsedu.timetracker.service.ApplicationDetailService;
import com.krlsedu.timetracker.service.WakaTimeCli;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

public class Core {
	
	private static final int WAIT_TIME = 100;
	private static boolean ativo = true;
	
	private Core() {
	}
	
	public static void start() {
		ativate();
		WakaTimeCli.init();
		new Thread(Core::tracker).start();
		Tray.togleLabel();
		if (isAtivo()) {
			Tray.notifyInfo("Plugin initiated!");
		}
	}
	
	private static void tracker() {
		if (WakaTimeCli.isDebug()) {
			WakaTimeCli.log.info("Initiated");
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
		if (WakaTimeCli.isDebug() && !isAtivo()) {
			WakaTimeCli.log.info("Stooped");
		}
	}
	
	public static void error(Exception e) {
		WakaTimeCli.log.error(e);
		WakaTimeCli.log.error(e.fillInStackTrace());
		WakaTimeCli.log.error(e.getLocalizedMessage());
		WakaTimeCli.log.error(e.getMessage());
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
		WakaTimeCli.stopQueue();
		Tray.togleLabel();
	}
	
	public static void restart() {
		stop();
		start();
	}
}
