package com.krlsedu.timetracker;

import com.krlsedu.timetracker.service.OfflineMode;
import org.apache.log4j.BasicConfigurator;

public class Monitor {
	
	public static void main(String[] args) throws Exception {
		for (String arg :
				args) {
			switch (arg){
				case "--offline":
					OfflineMode.setOn(true);
					break;
				case "--sync-errors":
					OfflineMode.syncErros();
					System.exit(0);
					break;
			}
		}
		BasicConfigurator.configure();
		Core.start();
	}
}