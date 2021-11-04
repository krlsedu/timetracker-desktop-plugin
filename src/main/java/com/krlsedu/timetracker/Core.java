package com.krlsedu.timetracker;

import com.krlsedu.timetracker.service.ApplicationDetailService;
import com.krlsedu.timetracker.service.WakaTimeCli;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

public class Core {
	
	private static final int WAIT_TIME = 100;
	
	private Core() {
	}
	
	public static void start() {
		WakaTimeCli.init();
		new Thread(Core::tracker).start();
	}
	
	private static void tracker() {
		
		do {
			try {
				Thread.sleep(WAIT_TIME);
				
				WinDef.HWND foregroundWindow = User32.INSTANCE.GetForegroundWindow();
				
				if (foregroundWindow != null) {
					ApplicationDetailService.generateApplicationDetailInfo(foregroundWindow);
				}
				
			} catch (Exception e) {
				Thread.currentThread().interrupt();
				break;
			}
		} while (true);
		
	}
}
