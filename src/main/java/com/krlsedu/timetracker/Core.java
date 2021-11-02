package com.krlsedu.timetracker;

import com.krlsedu.timetracker.service.ApplicationDetailService;
import com.krlsedu.timetracker.service.ErrorService;
import com.krlsedu.timetracker.service.OfflineMode;
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
		//new Thread(Core::reSendErrors).start();
	}
	
	private static void tracker() {
		try {
			do {
				Thread.sleep(WAIT_TIME);
				
				WinDef.HWND foregroundWindow = User32.INSTANCE.GetForegroundWindow();
				
				if (foregroundWindow == null) {
					continue;
				}
				
				//ApplicationService.generateApplicationInfo(foregroundWindow);
				
				ApplicationDetailService.generateApplicationDetailInfo(foregroundWindow);
				
			} while (true);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private static void reSendErrors(){
		try {
			do {
				if (!OfflineMode.isOn()) {
					ErrorService.reSendErrors();
				}
				Thread.sleep(1000 * 60 * 60);
			} while (true);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
