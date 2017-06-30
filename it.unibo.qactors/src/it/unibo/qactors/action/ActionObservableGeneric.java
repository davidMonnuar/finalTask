package it.unibo.qactors.action;
import it.unibo.contactEvent.interfaces.IContactEventPlatform;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactors.QActorContext;
import it.unibo.qactors.QActorUtils;
import it.unibo.qactors.platform.EventPlatformKb;
import java.util.Calendar;
import java.util.concurrent.Future;

/*
 * -------------------------------------------------------------------------
 * 					ActionObservableGeneric
 * Author: AN DISI
 * Goal:   define a TERMINATING action that emits a (unique) event when it ends
 * Usage:  
 * 		activate 		starts the action (Callable) as an asynchronous operation
 *  	getActionRep:	returns a representation of the action definition
 *  	getActionAndResultRep: returns a representation of the action and of its result
 *  	getExecTime:	return the execution time of the action
 * 
 * Implementation details:
 * 		Callable<T>, Future<T>, SituatedSysKb.executorManyThread
 * -------------------------------------------------------------------------
 */
public abstract class ActionObservableGeneric<T> implements IObservableActionGeneric<T> {
	protected static int actionCount = 1;
	
 	public Thread myself;
 	
	protected String name;
	protected IOutputEnvView outEnvView;
	protected String terminationEvId;
	protected long tStart  = 0;
	protected long durationMillis = -1;	
	protected IContactEventPlatform platform ;
 	protected QActorContext ctx;
	protected T result;
	
	public ActionObservableGeneric(
			String name, QActorContext ctx, String terminationEvId, IOutputEnvView outEnvView) throws Exception{
		this.name 			 = name.trim();
		this.ctx			 = ctx;
 		this.terminationEvId = terminationEvId.trim();
		this.outEnvView 	 = outEnvView;
	}
	/*
	 * 1) ACTIVATE THE   ACTION
	 */	
	public T execSynch() throws Exception   { 
 		Future<T> fResult = execASynch();
// 	    println("	%%% ActionObservableGeneric " + getName() + " waits for termination " + terminationEvId);
		T fut = fResult.get();	//forces the caller to wait		
//  	    println("	%%% ActionObservableGeneric " + getName() + " RESULT " + fut);
		return fut;
	}
	public Future<T> execASynch() throws Exception {
// 	    println("	%%% ActionObservableGeneric " + getName() + " ACTIVATED with terminationEvId=" + terminationEvId);
		Future<T> fResult = EventPlatformKb.manyThreadexecutor.submit(this);	//should invoke call
		return fResult;
	}
	/*
	 * 2) Entry point for the Executor
	 */
	@Override
	public T call() throws Exception {
//  		println("			%%% ActionObservableGeneric call " + getName()   );
		startOfAction(); 
//       	println("	%%% ActionObservableGeneric going to execTheAction " + getName()   );
		execTheAction();
		result = endActionInternal();
// 		println("	%%% ActionObservableGeneric " + getName() +" DONE"   );
		return result;
	}
	protected void startOfAction() throws Exception{
		tStart = Calendar.getInstance().getTimeInMillis();
 		myself = Thread.currentThread();	
//  		println("	%%% ActionObservableGeneric " + getName() + " set myself=" + myself   );
	}
	/*
	 * TO BE DEFINED BY THE APPLICATION DESIGNER
	 */
	protected abstract void execTheAction() throws Exception; 
	protected abstract T endOfAction() throws Exception; 
	public abstract String getResultRep();
	
	/*
	 * Calculate action execution time
	 */
	protected T endActionInternal() throws Exception{
		evalDuration();
		T res = endOfAction();		
		if(terminationEvId != null && terminationEvId.length()>0) {
			//System.out.println(" %%% ActionObservableGeneric " + getName() + " emits " + terminationEvId + "  " + res.toString() )	;
			emitEvent( terminationEvId, res.toString() );
		}
		return res;
	}
    protected void evalDuration(){
		if( durationMillis == -1 ){
			 long tEnd = Calendar.getInstance().getTimeInMillis();
			 durationMillis =  tEnd - tStart ;	
//  			 println("%%% ActionObservableGeneric duration="  +  durationMillis);
		}    	
    }
	protected void emitEvent(String event, String res) throws Exception{
//      	println("%%% ActionObservableGeneric " + getName() + " result=" + res + " EMITS " + event + " exectime=" +  durationMillis);
		String outS = "action_result(" + res + ", execTime(" + durationMillis + "))" ;
     	QActorUtils.raiseEvent((it.unibo.qactors.QActorContext) ctx, getName(), event, outS);    
	} 

/*
 * --------------------------------------
 * METHODS
 * --------------------------------------
 */
	public String getName(){
		return name;
	}
	public long getExecTime(){
		return durationMillis;
	}
	public String getTerminationEventId(){
		return this.terminationEvId;
	}	
	protected void println( String msg ){
		if(outEnvView != null) outEnvView.addOutput(msg);
		else System.out.println(msg);
	}
}