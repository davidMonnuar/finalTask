package it.unibo.qactors.platform;
import java.util.Hashtable;
import alice.tuprolog.Struct;
import alice.tuprolog.Term;
import it.unibo.contactEvent.interfaces.IEventItem;
import it.unibo.contactEvent.interfaces.ILocalTime;

public class EventItem implements IEventItem{
protected String eventId ;
protected String subj ;
protected ILocalTime time ;
protected String msg;
protected Hashtable<String, Object> at;
protected Struct msgStruct;

	public EventItem(String eventId, String msg, ILocalTime time, String subjId) throws Exception{
 		this("msg( EVID, MSGTYPE, SENDER, RECEIVER, MSG, TIME )".
				replace("EVID", eventId).
				replace("MSGTYPE", "event").
				replace("SENDER", subjId).
				replace("RECEIVER", "none").
				replace("MSG", msg).
				replace("TIME", ""+time.getTheTime() )		
				);		
 	}

	public EventItem(String rep) throws Exception{
  		msgStruct = (Struct) Term.createTerm(rep);
		this.eventId   = msgStruct.getArg(0).toString();
  		this.msg       = msgStruct.getArg(4).toString();//.replaceAll("'", "");
   		this.subj      = msgStruct.getArg(2).toString();
 		String timeStr = msgStruct.getArg(5).toString();
  		 try{
 			this.time    = new LocalTime( Integer.parseInt( timeStr ));
 		 }catch(Exception e){
 			this.time    = new LocalTime( 0 );
 		 }
  	}
	
	public String getEventId(){
		return eventId;
	}	
 	public String getMsg(){
		return msg;
	}
	public String getSubj(){
		return subj;
	}	
	public ILocalTime getTime(){
		return time;
	}
	public String getPrologRep(){
		return msgStruct.toString();
	}	
	public String getDefaultRep(){
		Term t = Term.createTerm(getPrologRep());
//		System.out.println("		msgRep= " + t.toString());
		return t.toString();
	}
	public String toString(){
		return getPrologRep();
	}
	@Override
	public Hashtable<String, Object> getArgTable() {
		return at;
	}
 }
