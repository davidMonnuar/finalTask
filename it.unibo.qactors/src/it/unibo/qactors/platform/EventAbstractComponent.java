/*
 * An EventAbstractComponent is a SituatedActiveObject that can raise events
 * It implements IContactComponent and works in a ActorContext. 
 * It can be associated to a set of events and resumed by the IContactEventPlatform
 * when one of these events (IEventItem) occurs.
 * 
 */
package it.unibo.qactors.platform;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactors.QActorContext;
import it.unibo.qactors.action.AsynchActionResult;
import it.unibo.qactors.akka.QActor;
  
 
public abstract class EventAbstractComponent extends QActor {
   
protected String curstate ="defaultState";
public boolean isActivated = false;
protected boolean isStarted = false;

  	public EventAbstractComponent( String name, QActorContext myctx, IOutputEnvView view )  { 
  		super( name, myctx, view );
    } 
 	@Override
 	protected void loadWorldTheory() throws Exception{
  		//we do not load any world theory for EventAbstractComponent
  	}

//  	@Override
	public boolean isStared(){
		return isStarted;
	}
  	@Override
	public abstract void doJob() throws Exception;
//	@Override
	public boolean isActivated(){ return isActivated; }
	
	@Override
	public AsynchActionResult delayReactive(int time, String  alarmEvents, String recoveryPlans) throws Exception{
		throw new Exception("delayReactive not admitted for EventAbstractComponent");
	}
 	/*
	 * Utilities
	 */
	protected void showMsg(String msg){
  		String mPrefix = ">>> " + getName();
  		mPrefix = align(mPrefix,18) + " (" + curstate + 
  	   			 ", TG="+ EventLoopActor.localTime.getTimeRep() + ")" ;
   		String msgOut = align(mPrefix, 60) + "|| " + msg;
  		outEnvView.addOutput(msgOut);
  	}
   	protected String align(String m, int dim){
  	byte[] spaces;
   	int n = dim - m.length(); 
  	if( n < 0 ) spaces = new byte[m.length()+1];
  	else spaces = new byte[n];
  		for(int i=0; i<n; i++ ) spaces[i] = ' ';
  		return  m + new String(spaces);
  	}  
   	


}
