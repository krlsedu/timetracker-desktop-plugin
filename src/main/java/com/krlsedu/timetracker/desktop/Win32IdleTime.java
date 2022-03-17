package com.krlsedu.timetracker.desktop;

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.win32.StdCallLibrary;

import java.util.Arrays;
import java.util.List;

/**
 * Utility method to retrieve the idle time on Windows and sample code to test it.
 * JNA shall be present in your classpath for this to work (and compile).
 * @author ochafik
 */
public class Win32IdleTime {
	public interface Kernel32 extends StdCallLibrary {
		Kernel32 INSTANCE = Native.loadLibrary("kernel32", Kernel32.class);

		int GetTickCount();
	}

	public interface User32 extends StdCallLibrary {
		User32 INSTANCE = Native.loadLibrary("user32", User32.class);

		void GetLastInputInfo(LASTINPUTINFO result);


		class LASTINPUTINFO extends Structure {
			public int cbSize = 8;

			/// Tick count of when the last input event was received.
			public int dwTime;

			@Override
			protected List<String> getFieldOrder() {
				return Arrays.asList("cbSize", "dwTime");
			}
		}
	}
	
	/**
	 * Get the amount of milliseconds that have elapsed since the last input event
	 * (mouse or keyboard)
	 * @return idle time in milliseconds
	 */
	public static int getIdleTimeMillisWin32() {
		User32.LASTINPUTINFO lastInputInfo = new User32.LASTINPUTINFO();
		User32.INSTANCE.GetLastInputInfo(lastInputInfo);
		return Kernel32.INSTANCE.GetTickCount() - lastInputInfo.dwTime;
	}
}