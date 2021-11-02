package com.krlsedu.timetracker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.krlsedu.timetracker.ConfigFile;
import com.krlsedu.timetracker.model.ApplicationDetail;
import com.krlsedu.timetracker.model.Heartbeat;
import org.apache.log4j.Logger;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class WakaTimeCli {
	public static final Logger log = Logger.getLogger(WakaTimeCli.class);
	private static final int QUEUE_TIMEOUT_SECONDS = 30;
	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private static final ConcurrentLinkedQueue<Heartbeat> heartbeatsQueue = new ConcurrentLinkedQueue<>();
	public static boolean debug = false;
	public static BigDecimal lastTime = new BigDecimal(0);
	private static ObjectMapper objectMapper = null;
	
	private WakaTimeCli() {
	}
	
	public static void init() {
		Applications.init();
		setupQueueProcessor();
	}
	
	private static String[] buildCliCommand(Heartbeat heartbeat, ArrayList<Heartbeat> extraHeartbeats) {
		ArrayList<String> cmds = new ArrayList<>();
		cmds.add(Dependencies.getCLILocation());
		cmds.add("--entity");
		cmds.add(heartbeat.entity);
		cmds.add("--time");
		cmds.add(heartbeat.timestamp.toPlainString());
		cmds.add("--key");
		cmds.add(ConfigFile.getApiKey());
		if (heartbeat.project != null) {
			cmds.add("--project");
			cmds.add(heartbeat.project);
		}
		if (heartbeat.language != null) {
			cmds.add("--alternate-language");
			cmds.add(heartbeat.language);
		}
		if (heartbeat.entityType != null) {
			cmds.add("--entity-type");
			cmds.add(heartbeat.entityType);
		}
		if (heartbeat.category != null) {
			cmds.add("--category");
			cmds.add(heartbeat.category);
		}
		cmds.add("--plugin");
		cmds.add("Desktop" + "/" + "11" + " " + "desktop-wakatime/" + "0.1");
		if (heartbeat.isWrite)
			cmds.add("--write");
		if (extraHeartbeats.size() > 0)
			cmds.add("--extra-heartbeats");
		return cmds.toArray(new String[cmds.size()]);
	}
	
	private static void sendHeartbeat(final Heartbeat heartbeat, final ArrayList<Heartbeat> extraHeartbeats) {
		final String[] cmds = buildCliCommand(heartbeat, extraHeartbeats);
		log.debug("Executing CLI: " + Arrays.toString(obfuscateKey(cmds)));
		try {
			Process proc = Runtime.getRuntime().exec(cmds);
			if (!extraHeartbeats.isEmpty()) {
				String json = toJSON(extraHeartbeats);
				log.debug(json);
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
		scheduler.scheduleAtFixedRate(handler, delay, delay, java.util.concurrent.TimeUnit.SECONDS);
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
	
	//TODO
	public static void appendHeartbeat(final ApplicationDetail applicationDetail) {
		Heartbeat h = new Heartbeat();
		h.entity = applicationDetail.getActivityDetail();
		h.entityType = "app";
		h.timestamp = getCurrentTimestamp(applicationDetail);
		h.isWrite = false;
		h.category = "browsing";
		h.project = applicationDetail.getName();
		if (isSendHeartbeat(h)) {
			heartbeatsQueue.add(h);
		}
	}
	
	public static BigDecimal getCurrentTimestamp(ApplicationDetail applicationDetail) {
		return new BigDecimal(String.valueOf(applicationDetail.getDateEnd().getTime() / 1000.0)).setScale(4, BigDecimal.ROUND_HALF_UP);
	}
	
	public static boolean isSendHeartbeat(Heartbeat heartbeat) {
		return Applications.sendHeartbeat(heartbeat);
	}
	
	public static ObjectMapper getObjectMapper() {
		if (objectMapper == null) {
			objectMapper = new ObjectMapper();
		}
		return objectMapper;
	}
	
}
