package com.krlsedu.timetracker;

import com.krlsedu.timetracker.service.ApplicationDetailService;
import com.krlsedu.timetracker.service.ApplicationService;
import com.krlsedu.timetracker.service.ErrorService;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

public class Core {
	
	private static final int waitTime = 100;
	
	public static void start() {
		new Thread(Core::tracker).start();
		new Thread(Core::reSendErrors).start();
	}
	
	private static void tracker() {
		try {
			while (true) {
				Thread.sleep(waitTime);
				
				WinDef.HWND foregroundWindow = User32.INSTANCE.GetForegroundWindow();
				
				if (foregroundWindow == null) {
					continue;
				}
				
				ApplicationService.generateApplicationInfo(foregroundWindow);
				
				ApplicationDetailService.generateApplicationDetailInfo(foregroundWindow);
				
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private static void reSendErrors(){
		try {
			while (true) {
				ErrorService.reSendErrors();
				Thread.sleep(1000 * 60);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
}
