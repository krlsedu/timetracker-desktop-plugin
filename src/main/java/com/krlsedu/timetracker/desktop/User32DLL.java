package com.krlsedu.timetracker.desktop;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;

import static com.sun.jna.platform.win32.WinNT.PROCESS_QUERY_LIMITED_INFORMATION;

public class User32DLL {
	static {
		Native.register("user32");
	}
	
	private User32DLL(){}
	
	public static native WinDef.HWND GetForegroundWindow();
	
	public static native int GetWindowTextW(WinDef.HWND hWnd, char[] lpString, int nMaxCount);
	
	public static String getImageName(WinDef.HWND window) {
		// Get the process ID of the window
		IntByReference procId = new IntByReference();
		User32.INSTANCE.GetWindowThreadProcessId(window, procId);
		
		// Open the process to get permissions to the image name
		WinNT.HANDLE procHandle = Kernel32.INSTANCE.OpenProcess(
				PROCESS_QUERY_LIMITED_INFORMATION,
				false,
				procId.getValue()
		);
		
		// Get the image name
		char[] buffer = new char[4096];
		IntByReference bufferSize = new IntByReference(buffer.length);
		boolean success = Kernel32.INSTANCE.QueryFullProcessImageName(procHandle, 0, buffer, bufferSize);
		
		// Clean up: close the opened process
		Kernel32.INSTANCE.CloseHandle(procHandle);
		
		return success ? new String(buffer, 0, bufferSize.getValue()) : null;
	}
}