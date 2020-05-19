package com.krlsedu.timetracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krlsedu.timetracker.model.Application;
import com.krlsedu.timetracker.model.ApplicationDetail;
import com.krlsedu.timetracker.service.*;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Core {
	
	private static final int waitTime = 100;
	
	public static void start() throws Exception {
		
		while (true) {
			Thread.sleep(waitTime);
//			Integer idleTime = Win32IdleTime.getIdleTimeMillisWin32();
			//Implementar tempo ocioso
			WinDef.HWND foregroundWindow = User32.INSTANCE.GetForegroundWindow();
			
			if (foregroundWindow == null) {
				continue;
			}
			
			ApplicationService.generateApplication(foregroundWindow);
			
			ApplicationDetailService.generateApplicationDetail(foregroundWindow);
			
		}
	}
	
	
}
