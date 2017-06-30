package it.unibo.qactors.action;
import it.unibo.contactEvent.interfaces.IEventItem;
/*
 * Result of a IActorAction executed in asynchronous way
 * interruptEvent is the event (if any) that has interrupted the action
 */
public class AsynchActionResult implements IAsynchActionResult{
protected long timeRemained ;
protected  String result;
protected  boolean suspended;
protected  boolean goon;
protected IEventItem interruptEvent;
protected IActorAction action;

	public AsynchActionResult(IActorAction action, long time, 
			boolean suspended, boolean goon, String result, IEventItem interruptEvent){
		this.action			= action;
		this.timeRemained   = time;
		this.goon			= goon;
		this.suspended      = suspended;
		this.result 	    = result;
		this.interruptEvent	= interruptEvent;
	}
	public long getTimeRemained(){
		return (timeRemained >= 0) ? timeRemained  : 0 ;
	}
	public IActorAction getAction(){
		return action;
	}
	public String getResult(){
		return result;
	}
	public boolean getGoon(){
		return goon;
	}
	public boolean getInterrupted(){
		return suspended;
	}
	public IEventItem getEvent(){
		return interruptEvent;
	}
	public void  setResult(String result){
		this.result = result;;
	}
	public void  setGoon(boolean goon){
		this.goon = goon;;
	}
	@Override
	public String toString(){ 
		String actionOuts= action==null ? "-" : action.getName();
 		String execTime=""+action.getExecTime();
 		String maxTime =""+action.getMaxDuration();
 		String eventInterrupt = interruptEvent==null ?"event(none)":"event("+interruptEvent.getEventId()+")";
 		String resultStr      = "result("+(result.length()==0?"result(noresult)":result)+")";
		return "asynchActionResult(ACTION,RESULT,SUSPENDED,TIMES,GOON,EVENT)".
				replace("ACTION","action("+ actionOuts +")").
				replace("RESULT", resultStr).
				replace("SUSPENDED","suspended("+suspended+")").
				replace("TIMES","times(exec("+execTime+"),max("+maxTime+"))").
				replace("GOON","goon("+goon+")").
				replace("EVENT",eventInterrupt)
				;
	}
}