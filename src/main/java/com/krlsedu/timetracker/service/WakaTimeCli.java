package com.krlsedu.timetracker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.krlsedu.timetracker.ConfigFile;
import com.krlsedu.timetracker.model.ApplicationDetail;
import com.krlsedu.timetracker.model.Heartbeat;
import org.apache.log4j.Logger;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public class WakaTimeCli {
	public static final Logger log = Logger.getLogger(WakaTimeCli.class);
	public static final int QUEUE_TIMEOUT_SECONDS = 30;
	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private static final ConcurrentLinkedQueue<Heartbeat> heartbeatsQueue = new ConcurrentLinkedQueue<>();
	private static boolean debug = true;
	private static ObjectMapper objectMapper = null;
	private static ScheduledFuture<?> scheduledFixture;
	
	private WakaTimeCli() {
	}
	
	public static void init() {
		Applications.init();
		setupDebugging();
		setupQueueProcessor();
	}
	
	private static String[] buildCliCommand(Heartbeat heartbeat, ArrayList<Heartbeat> extraHeartbeats) {
		ArrayList<String> cmds = new ArrayList<>();
		cmds.add(Dependencies.getCLILocation());
		cmds.add("--entity");
		cmds.add(heartbeat.getEntity());
		cmds.add("--time");
		cmds.add(heartbeat.getTimestamp().toPlainString());
		cmds.add("--key");
		cmds.add(ConfigFile.getApiKey());
		if (heartbeat.getProject() != null) {
			cmds.add("--project");
			cmds.add(heartbeat.getProject());
		}
		if (heartbeat.getLanguage() != null) {
			cmds.add("--alternate-language");
			cmds.add(heartbeat.getLanguage());
		}
		if (heartbeat.getEntityType() != null) {
			cmds.add("--entity-type");
			cmds.add(heartbeat.getEntityType());
		}
		if (heartbeat.getCategory() != null) {
			cmds.add("--category");
			cmds.add(heartbeat.getCategory());
		}
		if (heartbeat.getHostName() != null) {
			cmds.add("--hostname");
			cmds.add(heartbeat.getHostName());
		}
		cmds.add("--plugin");
		cmds.add(heartbeat.getIdeName() + "/" + heartbeat.getIdeVersion() + " " + "desktop-wakatime/" + "0.1");
		if (heartbeat.isWrite())
			cmds.add("--write");
		if (!extraHeartbeats.isEmpty())
			cmds.add("--extra-heartbeats");
		return cmds.toArray(new String[cmds.size()]);
	}
	
	private static void sendHeartbeat(final Heartbeat heartbeat, final ArrayList<Heartbeat> extraHeartbeats) {
		final String[] cmds = buildCliCommand(heartbeat, extraHeartbeats);
		if (WakaTimeCli.debug) {
			log.debug("Executing CLI: " + Arrays.toString(obfuscateKey(cmds)));
		}
		try {
			Process proc = Runtime.getRuntime().exec(cmds);
			if (!extraHeartbeats.isEmpty()) {
				String json = toJSON(extraHeartbeats);
				
				if (WakaTimeCli.debug) {
					log.debug(json);
				}
				
				try {
					BufferedWriter stdin = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
					stdin.write(json);
					stdin.write("\n");
					try {
						stdin.flush();
						stdin.close();
					} catch (IOException e) { /* ignored because wakatime-cli closes pipe after receiving \n */ }
				} catch (IOException e) {
					log.warn(e);
				}
			}
			if (WakaTimeCli.debug) {
				BufferedReader stdout = new BufferedReader(new
						InputStreamReader(proc.getInputStream()));
				BufferedReader stderr = new BufferedReader(new
						InputStreamReader(proc.getErrorStream()));
				proc.waitFor();
				String s;
				while ((s = stdout.readLine()) != null) {
					log.debug(s);
				}
				while ((s = stderr.readLine()) != null) {
					log.debug(s);
				}
				log.debug("Command finished with return value: " + proc.exitValue());
			}
		} catch (Exception e) {
			log.warn(e);
		}
	}
	
	
	private static String[] obfuscateKey(String[] cmds) {
		ArrayList<String> newCmds = new ArrayList<>();
		String lastCmd = "";
		for (String cmd : cmds) {
			if ("--key".equals(lastCmd))
				newCmds.add(obfuscateKey(cmd));
			else
				newCmds.add(cmd);
			lastCmd = cmd;
		}
		return newCmds.toArray(new String[0]);
	}
	
	private static String obfuscateKey(String key) {
		String newKey = null;
		if (key != null) {
			newKey = key;
			if (key.length() > 4)
				newKey = "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXX" + key.substring(key.length() - 4);
		}
		return newKey;
	}
	
	private static String toJSON(ArrayList<Heartbeat> extraHeartbeats) throws JsonProcessingException {
		return getObjectMapper().writeValueAsString(extraHeartbeats);
	}
	
	private static void setupQueueProcessor() {
		final Runnable handler = WakaTimeCli::processHeartbeatQueue;
		long delay = QUEUE_TIMEOUT_SECONDS;
		scheduledFixture = scheduler.scheduleAtFixedRate(handler, delay, delay, java.util.concurrent.TimeUnit.SECONDS);
	}
	
	public static void stopQueue() {
		scheduledFixture.cancel(true);
	}
	
	private static void processHeartbeatQueue() {
		
		// get single heartbeat from queue
		Heartbeat heartbeat = heartbeatsQueue.poll();
		if (heartbeat == null)
			return;
		
		// get all extra heartbeats from queue
		ArrayList<Heartbeat> extraHeartbeats = new ArrayList<>();
		while (true) {
			Heartbeat h = heartbeatsQueue.poll();
			if (h == null)
				break;
			extraHeartbeats.add(h);
		}
		
		sendHeartbeat(heartbeat, extraHeartbeats);
	}
	
	public static void appendHeartbeat(final ApplicationDetail applicationDetail) {
		if (isSendHeartbeat(applicationDetail)) {
			heartbeatsQueue.add(applicationDetail.getHeartbeat());
		}
	}
	
	public static BigDecimal getCurrentTimestamp(ApplicationDetail applicationDetail) {
		return new BigDecimal(String.valueOf(applicationDetail.getDateEnd().getTime() / 1000.0)).setScale(4, RoundingMode.HALF_UP);
	}
	
	public static boolean isSendHeartbeat(ApplicationDetail heartbeat) {
		return Applications.checkApplicationDetail(heartbeat);
	}
	
	public static ObjectMapper getObjectMapper() {
		if (objectMapper == null) {
			objectMapper = new ObjectMapper();
		}
		return objectMapper;
	}
	
	public static void setupDebugging() {
		String debug = ConfigFile.get("settings", "debug");
		WakaTimeCli.debug = debug != null && debug.trim().equals("true");
	}
	
	public static boolean isDebug() {
		return debug;
	}
	
	public static void setDebug(boolean debug) {
		WakaTimeCli.debug = debug;
	}
}
