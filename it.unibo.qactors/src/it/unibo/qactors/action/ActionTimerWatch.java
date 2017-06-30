package it.unibo.qactors.action;
import it.unibo.contactEvent.interfaces.IEventItem;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactors.QActorUtils;
 
/*
 *  
 */
public class ActionTimerWatch {
protected int execTime ;
protected String toutEventId;
protected IOutputEnvView outEnvView ;
protected String name;
protected ActorTimedAction action;
protected Thread myself;

	public ActionTimerWatch (String name, IOutputEnvView outEnvView, int execTime , ActorTimedAction action ){
		this.name        		= name;
		this.outEnvView  		= outEnvView;
 		this.execTime    		= execTime;
		this.action    			= action;
 		doJob();
 	}
	
  	protected void doJob()   { 
 			myself = new Thread(){
 				public void run(){
 			 		try {
 			 			String m = "tout("+execTime+")";
 			      		IEventItem ev = QActorUtils.buildEventItem(  name, "timeOut", m  );
// 			 			outEnvView.addOutput("ActionTimerWatch " + name + " STARTS " +  action    );
 			 			Thread.sleep( execTime  ); //BLOCK all the computation if executed in preStart
//			 			outEnvView.addOutput("ActionTimerWatch " + name + " ENDS  " +   execTime   );
 			  			action.setInterruptEvent(ev);	 
 			 		} catch (Exception e) {
// 			 			outEnvView.addOutput( "	+++ ActionTimerWatch " + name + " interrupted " );
 			  		} 		
				}
 			};
 			myself.start();  			
	}

  	public void stop(){
//  		outEnvView.addOutput( "	+++ ActionTimerWatch " + name + " stop " );
  		myself.interrupt();
  	}
 
}
