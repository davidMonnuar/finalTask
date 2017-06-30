package it.unibo.qactors.action;
import java.util.concurrent.Callable;
import akka.actor.ActorRef;
import it.unibo.contactEvent.interfaces.IEventItem;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactors.ActionRegisterMessage;
import it.unibo.qactors.QActorContext;
import it.unibo.qactors.QActorUtils;
import it.unibo.qactors.akka.QActor;
import it.unibo.qactors.platform.EventPlatformKb;
 
public abstract class ActorTimedAction extends ActionObservableGeneric<String> implements IActorAction{
 	protected ActionTimerWatch timeWatch ;
	protected String[] alarms ;
	protected String actionTimedResult = "unknown";
	protected String toutevId ="";
	protected String suspendevent = null;
	protected IEventItem currentEvent;
	protected int maxduration;
	protected boolean workingActionSynchFlag = true;
	private Thread localExecutorThread    ;
	protected boolean cancompensate;
 	protected boolean suspended = false;
 	protected int timeRemained = 0;
	protected QActor myactor;	
	/*
	 * CONSTRUCTION
	 */
	public ActorTimedAction(String name,  QActor actor, QActorContext ctx, boolean cancompensate,
			String terminationEvId, String[] alarms, 
			IOutputEnvView outEnvView, int maxduration) throws Exception {
		super(name,  ctx, terminationEvId,  outEnvView );
		this.cancompensate = cancompensate;
  		this.maxduration   = maxduration;
 		this.alarms        = alarms;
 		myactor 		   = actor;
   	}	
	protected void initActorTimedAction() throws Exception{ //called at startOfAction
 		toutevId = QActorUtils.getNewName(getName()+"tout");
 		//REGISTER THE ACTION
 		registerTheAction();
 		//Create an actor timer related to the action   
  		String touth = QActorUtils.getNewName("toutEvh");
 		int timeExtension = 0;
   		timeWatch = new ActionTimerWatch(touth, outEnvView,maxduration+timeExtension,this);
  	}  
	
	protected abstract String getApplicationResult() throws Exception;
	protected abstract Callable<String> getActionBodyAsCallable();
	
	public boolean canBeCompensated(){
		return cancompensate;
	}
	@Override
	public boolean isSuspended(){
		return suspended;
	}
	protected void registerTheAction() throws Exception{
		for( int i=0; i<alarms.length; i++){
			if( alarms[i] == null ) break;		//defensive
			if( alarms[i].trim().length() > 0 ){
//				println("ActorTimedAction registerTheAction " + getName() + " for " + alarms[i]);
 				registerForEvent(  alarms[i], this );
			}
		}		
	}
	protected void unRegisterTheAction() throws Exception{
		ActorRef evlp    = ctx.getEvlpActorRef(); 
		IEventItem unregaction = QActorUtils.buildEventItem(  "system", EventPlatformKb.unregisterAction,  this.getName()  );
		evlp.tell(unregaction, evlp);
	}	
	public void registerForEvent(  String evId, ActorTimedAction action ) throws Exception{
  		ActionRegisterMessage msg = new ActionRegisterMessage(evId, action, true);
  		ActorRef evlp    = ctx.getEvlpActorRef();
		if( evlp != null){
			evlp.tell(msg, evlp);
 		}else{
			throw new Exception("registerForEvent too early");
		}		
	}
 	protected void startOfAction() throws Exception{
//  		println("	%%%  ActorTimedAction " + getName() + " startOfAction  "   );
 		suspended = false;
		super.startOfAction();
 		initActorTimedAction(); 
 	}
	
	protected  String endActionInternal() throws Exception{
		evalDuration();
		unRegisterTheAction();
		timeWatch.stop();
		suspended = true; 		//terminated!!!
         return super.endActionInternal();
  		 //unregister: NEVER MORE HERE
 	}	
	
 	public void suspendAction(){
		evalDuration(); 
//		println("	%%% ActorTimedAction " + name + " suspendAction durationMillis=" + durationMillis  );
		suspended = true;		
  		if(currentEvent!=null){
// 	  		println("	%%% ActorTimedAction " + getName() + " suspendAction currentEventId="  + currentEvent.getEventId() + " myself=" + myself );
			if( currentEvent.getEventId().equals("timeOut")){	
				suspendevent ="timeOut("+durationMillis+")";
			}
			else suspendevent = "interrupted("+currentEvent.getEventId()+")";
		}else{
//	  		println("	%%% ActorTimedAction " + getName() + " suspendAction currentEvent NULL"   );
			suspendevent ="unknown";
		}
 		/*
  		 * WARNING:
  		 * Interruption requires the cooperation of the task being interrupted.
  		 */
   		if( myself != null ){
  			myself.interrupt();  
   		}
  		//myself is the thread in which the action is executing. If not sleepig, interrupt is lost 
 	}
 	
 	public void setInterruptEvent(IEventItem currentEvent){
		this.evalDuration();
 		this.currentEvent = currentEvent;
// 		println("	%%% ActorTimedAction setInterruptEvent " + getName() + " currentEvent= " + currentEvent.getDefaultRep());
		if( ! isSuspended() ) 
			suspendAction();  			
 	} 	
 	@Override
	public IEventItem getInterruptEvent(){  
		return currentEvent;
	}	
	protected  String getResult() throws Exception {
 		evalDuration(); 
// 	 	println( "	%%% ActorTimedAction " +  this.name + " getResult durationMillis=" + durationMillis +"/"+maxduration );
		timeRemained = (int) (maxduration - durationMillis) ;
		if( timeRemained < 0 ) timeRemained = 0;
  		return getApplicationResult() ;
	}	
	@Override
	public int getMaxDuration() {
 		return this.maxduration;
	}
	@Override
	public void setMaxDuration(int d) {
		maxduration =  d;		
	}
	@Override
	protected String endOfAction() throws Exception {
 		return getResult();
	}
 	@Override
	public String getResultRep() {
 		try {
// 			println( "	%%% ActorTimedAction " + getName() + " getResultRep"   );
			return getApplicationResult() ;
		} catch (Exception e) {
 			return "unknown";
		}
	}
	
	@Override
	public void execTheAction() throws Exception {
//		println("			%%% ActorTimedAction execTheAction "    );
 		Callable<String> f = getActionBodyAsCallable();
 		localExecutorThread=createLocalThreadExecutor( f );
  		localExecutorThread.start();
  		/*
  		 * The action is terminated after waitForActionEnd
  		 */
		waitForActionEnd();		
	}
	
	protected Thread createLocalThreadExecutor( Callable<String> f ){
		workingActionSynchFlag = true;
		return new Thread(){
		public void run(){
			 try{	  
// 				 println("	%%% ActorTimedAction local thread  f=" + f  );
				 String res = f.call();
// 				 println("	%%% ActorTimedAction local thread ENDS   "   );
				 resumeMyTimedAction(res);
 		   	 }catch(Exception e){
//				 println("	%%% ActorTimedAction local thread executor " + e.getMessage()   );
 			 }				
		 }//run
	    };
 	}
	protected synchronized void resumeMyTimedAction(String res){
		workingActionSynchFlag = false;
//		println("	%%% ActorTimedAction resumeMyTimedAction res=" + res  );
		this.notifyAll();
	}
	 
 
	public synchronized void waitForActionEnd(){
		try{
			while( workingActionSynchFlag ){
//	  			println("	%%% ActorTimedAction " + getName() +" WAITS "   );
				wait();		
			}
			this.evalDuration();
//  			println("	%%% ActorTimedAction CONTINUE exectime=" + this.durationMillis);
		}catch(InterruptedException e){
  			println("	%%% ActorTimedAction " + getName() + " waitForActionEnd INTERRUPTED "    );
 			this.suspended = true;
 			localExecutorThread.interrupt();  //useful for action that sleep, like sound. Otherwise they do not stop
 			try {
				Thread.sleep(100);  //give time to the interrupt to run
			} catch (InterruptedException e1) {
 				e1.printStackTrace();
			}
 			/*
 			 * WARNING: this stop could block resumeMyTimedAction
 			 */
 			localExecutorThread.stop();	//DEPRECATED but working
		}
	}
	
	
 }