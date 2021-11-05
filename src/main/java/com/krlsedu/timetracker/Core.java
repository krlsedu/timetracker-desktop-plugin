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
		new Thread(Core::tracker).start();
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
				WakaTimeCli.log.error(e);
				break;
			}
		} while (isAtivo());
		if (WakaTimeCli.isDebug()) {
			WakaTimeCli.log.info("Stooped");
		}
	}
	
	public static boolean isAtivo() {
		return ativo;
	}
	
	public static void setAtivo(boolean ativo) {
		Core.ativo = ativo;
	}
}
