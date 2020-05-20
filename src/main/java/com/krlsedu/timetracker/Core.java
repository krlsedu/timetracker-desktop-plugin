package com.krlsedu.timetracker;

import com.krlsedu.timetracker.service.ApplicationDetailService;
import com.krlsedu.timetracker.service.ApplicationService;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

public class Core {
	
	private static final int waitTime = 100;
	
	public static void start() throws Exception {
		
		while (true) {
			Thread.sleep(waitTime);
			
			WinDef.HWND foregroundWindow = User32.INSTANCE.GetForegroundWindow();
			
			if (foregroundWindow == null) {
				continue;
			}
			
			ApplicationService.generateApplicationInfo(foregroundWindow);
			
			ApplicationDetailService.generateApplicationDetailInfo(foregroundWindow);
			
		}
	}
	
	
}
