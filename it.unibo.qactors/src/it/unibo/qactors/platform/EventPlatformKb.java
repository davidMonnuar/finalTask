package it.unibo.qactors.platform;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

public class EventPlatformKb {
  	public static ExecutorService  manyThreadexecutor 	= null;//Executors.newScheduledThreadPool(20);
 
 	public static final String endOfJob 		= "endOfJob";
	public static final String activate 		= "activate";
	public static final String register 		= "register";
	public static final String unregister 	    = "unregister";
	public static final String unregisterAll 	= "unregisterAll";
	public static final String getLastEvent 	= "getLastEvent";
	public static final String unregisterAction = "unregisterAction";
	public static final String raiseEvent  		 = "raiseEvent";
 
	public static final String sensorEvent   = "sensorEvent";
	public static final String streamEvent   = "streamEvent";
	public static final String endOfStream   = "endOfStream";
 	
 	public static String getTime(long exectime) { 
 		long hr = 0;
		long min = 0;
		long sec = 0;
		long msec = 0;
		if (exectime < 1000) {
			msec = exectime;
		} else if (exectime < 60 * 1000) {
			sec = exectime / 1000;
			msec = exectime - sec * 1000;
		}
		return exectime + "|" + hr + ":" + min + ":" + sec + ":" + msec;
	}
	
}
