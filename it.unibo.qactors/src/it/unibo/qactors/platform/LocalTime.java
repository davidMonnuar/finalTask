package it.unibo.qactors.platform;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import it.unibo.contactEvent.interfaces.ILocalTime;
public class LocalTime implements ILocalTime{
	protected long time = 0;
	
	public LocalTime(long time)  {
		try {
			setTime(time);
		} catch (Exception e) {
 			e.printStackTrace();
		}
	}
	
	protected void setTime(long time) throws Exception{
		if( time < 0 ) throw new Exception("negative time not allowed");
		else this.time = time;
	}

	public long getTheTime() {
		return time;
	}
	
	public String getTimeRep( ) {
// 		long hr = 0;
//		long min = 0;
//		long sec = 0;
//		long msec = 0;
//		if (time < 1000) {
//			msec = time;
//		} else if (time < 60 * 1000) {
//			sec = time / 1000;
//			msec = time - sec * 1000;
//		}
//		return time + "|" + hr + ":" + min + ":" + sec + ":" + msec;
		
 		Date date = new Date(time);
// 		Format format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Format format = new SimpleDateFormat("HH:mm:ss");
		return format.format(date);
 	}
	
}
