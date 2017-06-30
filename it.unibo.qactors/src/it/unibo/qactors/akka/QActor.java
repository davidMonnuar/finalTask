package it.unibo.qactors.akka;

import static akka.pattern.Patterns.ask;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.UntypedAbstractActor;
import akka.util.Timeout;
import alice.tuprolog.Library;
import alice.tuprolog.MalformedGoalException;
import alice.tuprolog.NoSolutionException;
import alice.tuprolog.Prolog;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.Struct;
import alice.tuprolog.Term;
import alice.tuprolog.Theory;
import alice.tuprolog.lib.InvalidObjectIdException;
import it.unibo.connector.CreateCommand;
import it.unibo.connector.IConnector;
import it.unibo.connector.Move;
import it.unibo.connector.UnityConnector;
//import it.unibo.connector.IConnector;
//import it.unibo.connector.UnityConnector;
import it.unibo.contactEvent.interfaces.IEventItem;
import it.unibo.is.interfaces.IBasicUniboEnv;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.is.interfaces.IOutputView;
import it.unibo.qactors.ActionRegisterMessage;
import it.unibo.qactors.QActorContext;
import it.unibo.qactors.ActorTerminationMessage;
import it.unibo.qactors.QActorMessage;
import it.unibo.qactors.QActorUtils;
import it.unibo.qactors.action.ActionDummyTimed;
import it.unibo.qactors.action.ActionOpTimed;
import it.unibo.qactors.action.ActionReceiveTimed;
import it.unibo.qactors.action.ActionSolveTimed;
import it.unibo.qactors.action.ActionUtil;
import it.unibo.qactors.action.ActorTimedAction;
import it.unibo.qactors.action.AsynchActionResult;
import it.unibo.qactors.action.IActorAction;
//import it.unibo.qactors.action.PlanActionDescr;
import it.unibo.qactors.action.IActorAction.ActionExecMode;
import it.unibo.qactors.mqtt.MqttUtils;
import it.unibo.qactors.action.IMsgQueue;
import it.unibo.qactors.platform.EventPlatformKb;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

 
/*
 * A QActor is an akka actor that owns a local Prolog theory
 */
public abstract class QActor extends UntypedAbstractActor{
protected String actorId;
protected QActorContext myCtx;
protected IOutputEnvView outEnvView;
protected Prolog pengine ;
protected ActorRef evlpActorRef;
protected String worldTheoryPath = null;
protected String initPlan;

//TODO REGENERATE
protected IBasicUniboEnv env = null;	
protected String temporaryStr = "0";     
protected Hashtable<String, String> guardVars ;	

public static final boolean suspendWork      = false;
public static final boolean continueWork     = true;
public static final boolean interrupted      = true;
public static final boolean normalEnd        = false;
//visible to QActorPlanUtils
public int nPlanIter = 0;		
public String curPlanInExec;	 
public Stack<String>  planStack = new Stack<String>(); 
public Stack<Integer> iterStack = new Stack<Integer>();
public QActorActionUtils actionUtils;
public QActorPlanUtils planUtils;

protected IEventItem currentEvent, lastEvent;
protected int numOfInterruptEvents = 0;
//protected int timeoutval           = 0;

public IMsgQueue mysupport ; //used by AbstractXxx extends QActor

	public QActor(String actorId, QActorContext myCtx, IOutputEnvView outEnvView ) {
		this.actorId    = actorId;
		this.myCtx      = myCtx;
		this.outEnvView = outEnvView;
		this.pengine    = new Prolog();	//each QActor has its own prolog machine
		actionUtils     = new QActorActionUtils(this,outEnvView);
		planUtils       = actionUtils.getQActorPlanUtils(); 
		numOfInterruptEvents = 0;
 	}
	public QActor(String actorId, QActorContext myCtx, 
			String worldTheoryPath, IOutputEnvView outEnvView, String defaultPlan ) {
		this.actorId    = actorId;
		this.myCtx      = myCtx;
		this.outEnvView = outEnvView;
		this.pengine    = new Prolog();	//each QActor has its own prolog machine
		this.env        = outEnvView.getEnv();
		this.initPlan   = defaultPlan;
		this.worldTheoryPath = worldTheoryPath;
		numOfInterruptEvents = 0;
		actionUtils = new QActorActionUtils(this,outEnvView);
		planUtils   = actionUtils.getQActorPlanUtils(); 
	}
	
	@Override
	public void preStart() {
// 		System.out.println("QActor " + this.getName() + " STARTS with ctx=" + myCtx.getName() + " worldTheoryPath="+worldTheoryPath);
		/* An actor should terminate in a coordinated way */
		try {
			QActorUtils.memoQActor(  this );
			if( worldTheoryPath !=null ) loadWorldTheory();	
			QActorUtils.registerActorInProlog18(this.pengine, this);
//  	  		QActorUtils.solveGoal(pengine, "output( uuuuuuuuuuuuuu )"); //
//  	  		QActorUtils.solveGoal(pengine, "actorPrintln(xxx)"); //
			doJob();
//			println("QActor " + getName() + " ENDS  "   ) ;	
			/*
			 * Termination must be called by the user-defined actor
			 */
//  			QActorContext.terminateQActorSystem(this); 
		} catch (Exception e) {
 			System.out.println("QActor " + getName() + " ENDS with ERROR=" + e.getMessage() ) ;	
//			this.terminate();
		}
 	}
	
	protected void set_evlpActorSel(){ 
		if( evlpActorRef != null ) return;
//		evlpActorSel = QActorUtils.getEvlpActorSel();
		evlpActorRef = myCtx.getEvlpActorRef();
// 		evlpActorSel = QActorUtils.getSelectionIfLocal(myCtx,QActorUtils.getEventLoopActorName(myCtx)); 
// 		println("QActor " + getName() + " evlpActorSel = " + evlpActorSel );
////		if(evlpActorSel !=null) evlpActorRef = QActorUtils.getActorRefFromActorSelection(evlpActorSel);
//		if(evlpActorSel != null && evlpActorRef == null){
//			evlpActorRef = QActorUtils.getActorRefFromActorSelection(evlpActorSel);
// 		}
	 }

	protected abstract void doJob() throws Exception;
	
 	public IOutputView getOutputView(){
		System.out.println("			getOutputView");
		return outEnvView;
	}
	public IOutputEnvView getOutputEnvView(){
 		return outEnvView;
	}
	
	public QActorContext getQActorContext(){
		return myCtx;
	}
	public Prolog getPrologEngine(){
		return pengine;
	}
	public  IEventItem getCurrentEvent( ) {
 		return currentEvent;
	}
 	/*
	 * The action could terminate before the application calls waitForLastEvent
	 * In this case we keep track of the last interrupt event (currentEvent) only
	 */
	public synchronized void setCurrentEvent(IEventItem ev){
// 		println(getName() +  " setCurrentEvent " + ev );	
		numOfInterruptEvents++;
		this.currentEvent = ev;
		this.lastEvent    = ev;
		this.notifyAll();
	}
	public synchronized IEventItem waitForCurentEvent( ) throws Exception{
 		return waitForLastEvent(1,true);
	}
	public synchronized IEventItem waitForLastEvent( int num, boolean withTrace ) throws Exception{
 		println(getName() + " waitForLastEvent n=" + numOfInterruptEvents ); 
		while( lastEvent == null ){ //&& numOfInterruptEvents < num 
			wait();
  			if( withTrace )
			println(getName() + " n=" + numOfInterruptEvents + " EVENT=" + lastEvent.getDefaultRep() ); 
		}
		IEventItem temp = lastEvent;
		lastEvent = null;
		return temp;
	}

/*
 * ------------------------------------------------------------------
 * 	MESSSAGING
 * ------------------------------------------------------------------
 */
	public void sendMsg(String msgID, String dest, String msgType, String msg) throws Exception {
//  		println(getName() +" sendMsg " + msgID + " to " + dest );
		//Prepare the massage
 		 QActorMessage mout  = QActorUtils.buildMsg(myCtx, getName(), msgID, dest, msgType, msg);
 /*
  		 ActorSelection asel = QActorUtils.getSelectionIfLocal( myCtx, dest );
		 //The the destActorId is LOCAL: Send the message
		 if( asel != null ){
//		  		println(getName() +" sendMsg " + msgID + " to " + asel );
//			 try{
// 				//DYNAMIC MAY2017
//		  		 if( msg.contains("value(24)")  ) { //|| msg.contains("value(3)") 
////			  		this.emit("alarm", "alarm( "+msg +")");
//		  			println(getName() +" sendMsg  mout=" + mout + " to " + dest + " asel=" + asel);
//		  			asel.tell( mout, getSelf() ); 
////		  			Thread.sleep(5000);
//		  		 }else
		  			asel.tell( mout, getSelf() ); //getSelf() deprecated
//		  	}catch( Exception e){
//				 println(getName() +" ERROR   " + e );
//				 e.printStackTrace();
//			 }
		 }
		 else {
			 //dest is remote
			 SenderObject sa = myCtx.getSenderAgent( dest );
			 msg=envelope(msg);
//  			 println(getName() +" sendMsg   " + msg + " with " + sa.getName() );
			 sa.sendMsg( this, dest, msgID, msgType, msg );
		 }
*/
 		//JUNE2107 after update to 2.5.2
		 ActorRef destActor = myCtx.getActorRefInQActorContext(dest);
		 if(destActor != null) destActor.tell( mout, getSelf() );
		 else{//dest should be remote
			 try{
				 SenderObject sa = myCtx.getSenderAgent( dest );
				 msg=envelope(msg);
	//			 println(getName() +" sendMsg   " + msg + " to " + sa);
				 sa.sendMsg( this, dest, msgID, msgType, msg );
			 }catch(Exception e){
				 println("sendMsg " + getName() +" dest NOT FOUND   "  );
			 }			 
		 }

	}
	
	//MAy 2017 after Mqtt
	public void sendMsg(String senderName, String msgID, String dest, String msgType, String msg) throws Exception {
		//Prepare the massage
		 QActorMessage mout  = QActorUtils.buildMsg(myCtx, senderName, msgID, dest, msgType, msg);
		 /*
 		 ActorSelection asel = QActorUtils.getSelectionIfLocal( myCtx, dest );
//		 println(getName() +" sendMsg  mout=" + mout + " to " + dest + " asel=" + asel);
		 //The the destActorId is LOCAL: Send the message
		 if( asel != null )  asel.tell( mout, getSelf() );
		 else {
			 //dest should be remote
			 try{
				 SenderObject sa = myCtx.getSenderAgent( dest );
				 msg=envelope(msg);
	//			 println(getName() +" sendMsg   " + msg + " to " + sa);
				 sa.sendMsg( senderName, dest, msgID, msgType, msg );
			 }catch(Exception e){
				 println("sendMsg " + getName() +" dest NOT FOUND   "  );
			 }
		 }	
		 */
		 //JUNE2107 after update to 2.5.2
		 ActorRef destActor = myCtx.getActorRefInQActorContext(dest);
		 if(destActor != null) destActor.tell( mout, getSelf() );
		 else{//dest should be remote
			 try{
				 SenderObject sa = myCtx.getSenderAgent( dest );
				 msg=envelope(msg);
	//			 println(getName() +" sendMsg   " + msg + " to " + sa);
				 sa.sendMsg( senderName, dest, msgID, msgType, msg );
			 }catch(Exception e){
				 println("sendMsg " + getName() +" dest NOT FOUND   "  );
			 }			 
		 }
	}
	
	protected String envelope( String content){
		try{
			Term tt = Term.createTerm(content);
			return tt.toString();
		}catch(Exception e){
			return "'"+content+"'";
		}
	}

// 	public void sendMsg( String msg ) throws Exception {
// 		QActorMessage mout = new QActorMessage(msg);
// 		sendTheMsg( mout ); 
// 	}
// 	protected void sendTheMsg(QActorMessage msg) throws Exception{
// 		 
// 		 String dest = msg.msgReceiver() ;
// 		 ActorSelection asel = QActorUtils.getSelectionIfLocal( myCtx, dest );
//		 //The the destActorId is local: Send the message
//		 if( asel != null ) asel.tell( msg, getSelf() ); 
//		 else {
//			 //dest  
//			 SenderObject sa = myCtx.getSenderAgent( dest );
//			 println("sendTheMsg   " + msg + " to " + sa);
//			 sa.sendMsg( msg.getDefaultRep() );
//		 }
// 	}
//		
	protected Future<Object> askMessage(ActorRef dest, Object askmsg, int tout){
		Timeout timeout = new Timeout(Duration.create(tout, "seconds"));
// 		println("askMessage to " + dest.path());	
 		Future<Object> future = ask( dest, askmsg, timeout);
		return future;
	}
	protected void askMessageWaiting(ActorRef dest, Object askmsg, int tout){
		Future<Object> future = askMessage(  dest,   askmsg,   tout);
		waitForAskResult(future,tout);
	}
	public String askMessageWaiting(String dest, String askmsg, int tout) throws Exception{
		askmsg=envelope(askmsg);
//		println("QActor " + getName() + " askMessageWaiting   " + askmsg + " to " + dest);
		QActorMessage mout = QActorUtils.buildMsg(myCtx, getName(), "request", dest, QActorContext.request, askmsg);
/*		
		ActorSelection destsel = QActorUtils.getSelectionIfLocal( myCtx, dest );
		 //The the destActorId is local: Send the message
		 if( destsel != null ){
			    ActorRef destRef = QActorUtils.getActorRefFromActorSelection(destsel);
				Future<Object> future = askMessage(  destRef,   mout,   tout);
				//The sender is NOT the current akka actor
				return waitForAskResult(future,tout); 
		 }
		 else {
			 //dest is remote TODO
			 SenderObject sa = myCtx.getSenderAgent( dest );
 //			 println("sendTheMsg   " + msg + " to " + sa);
 			 sa.sendMsg( mout.getDefaultRep() );
 			 String answer = sa.receiveWakeUpAnswer();
 			 return "TODO";
		 }
*/
		//JUNE2017 after update to 2.5.2
		ActorRef destRef = myCtx.getActorRefInQActorContext(dest);
		if( destRef != null ){
			Future<Object> future = askMessage(  destRef,   mout,   tout);
			//The sender is NOT the current akka actor
			return waitForAskResult(future,tout); 
		} else {
			 //dest is remote TODO
			 SenderObject sa = myCtx.getSenderAgent( dest );
//			 println("sendTheMsg   " + msg + " to " + sa);
			 sa.sendMsg( mout.getDefaultRep() );
			 String answer = sa.receiveWakeUpAnswer();
			 return "TODO";
		 }

	}
	

	protected String waitForAskResult(Future<Object> future, int tout){
		try {
 			String	result =  (String) Await.result(future, new Timeout(Duration.create(tout, "seconds")).duration());
//  			println("	*** QActor " + getName() +" waitForAskResult result="+result + " tout=" + tout );
 			//Auto propagate the answer
// 			getSelf().tell(result, getSelf());
 			return envelope(result);
		} catch (Exception e) {
// 			e.printStackTrace();
 			return "failure("+e.getMessage()+")";
		}						
	}
	
 	public void emit( String evId, String evContent ) {
		set_evlpActorSel();
//		System.out.println("QActor " + getName() + " emit " + evId + " evContent=" + evContent   );
 		//Send a message to the context event loop actor
		IEventItem ev = QActorUtils.buildEventItem(  this.getName(), evId, evContent  );
		evlpActorRef.tell(ev, getSelf());
//		println("QActor " + getName() + " emit " + evId + " evContent=" + evContent  + " evlpActorRef=" + evlpActorRef);
  		if( ! evId.startsWith(QActorUtils.locEvPrefix)){
  			String evRep = ev.getPrologRep();
  			try {
				QActorUtils.propagateEvent(this.myCtx,evRep);
			} catch (Exception e) {
 				e.printStackTrace();
			} 
  		}
	}
	protected void raiseEvent( String evId, String evContent) throws Exception{
		emit( evId, evContent );
 	}
 	public void registerForEvent( String evId , int time) throws Exception{
		set_evlpActorSel();
 		println("	*** QActor " + getName() + " registerForEvent " + evId + " evlpActorRef=" + evlpActorRef  );
 		//Send a message to the context event loop actor
		IEventItem ev  = QActorUtils.buildEventItem(  getName(), EventPlatformKb.register, evId  );
		if(evlpActorRef != null){
// 			ActorRef evlpActorRef = QActorUtils.getActorRefFromActorSelection(evlpActorSel);
			askMessageWaiting( evlpActorRef, ev, time);
		}else{
 			println("	*** QActor registerForEvent evlpActorRef = "+evlpActorRef);
//			throw new Exception("registerForEvent too early");
		}
//		println("	*** QActor has registered " + getName() + " for " + evId);
	}
	public void registerForEvent(  String evId, ActorTimedAction action ) throws Exception{
		set_evlpActorSel();
 		ActionRegisterMessage msg = new ActionRegisterMessage(evId, action, true);
		if(evlpActorRef != null){
 			askMessageWaiting( evlpActorRef, msg, 1000);
		}else{
			throw new Exception("registerForEvent too early");
		}		
	}
	public void unregisterForEvent( String evId )throws Exception{
		//Send a message to the context event loop actor
		IEventItem ev =  QActorUtils.buildEventItem(  getName(), EventPlatformKb.unregister, evId );
		if(evlpActorRef != null) evlpActorRef.tell(ev, getSelf());
	}
	
	public String receiveAction(IMsgQueue mysupport, int maxTime) throws Exception {
		IActorAction action = new ActionReceiveTimed(
				getName() + "action", this, myCtx, mysupport, false,
				QActorUtils.getNewName(IActorAction.endBuiltinEvent),
				new String[] {}, outEnvView, maxTime);
		String res = action.execSynch();
		println("	--------------- receiveAction res=" + res);
		//msg(interrupt,event,callable,none,receive(timeOut(30),timeRemained(0)),0)
		currentMessage = new QActorMessage(res);
		return  res;
	}

	
 /*
 *  ENTRY POINTS for messages
 *  
 */ 	
	public AsynchActionResult senseEvents(int tout, String events) throws Exception {
		return this.planUtils.senseEvents(tout, events, "", "", "", ActionExecMode.synch);
	}
	//MARCH2017  June2017
	public AsynchActionResult senseEvents(int tout, String events, String plans, String E, String P) throws Exception {
		AsynchActionResult aar = planUtils.senseEvents( tout,events,plans,E,P,ActionExecMode.synch );
		if( ! aar.getGoon() || aar.getTimeRemained() <= 0 ){
			println("			WARNING: sense timeout");
			addRule("tout(senseevent,"+getName()+")");
		}
		return aar;
	}
 	@Override
	public void onReceive(Object message) throws Throwable {
//  			println("	QActor "+ this + " onReceive: " + message );
		if (message instanceof String){
			handleQActorString( (String)message );
//	  		println(getName() + " RECEIVES String=" + message );
	  		return;
 		}else		
		if (message instanceof IEventItem){ //For eventLoopActor
			handleQActorEvent( (IEventItem)message );
 			IEventItem ev = (IEventItem)message;
// 	  		println(getName() + " RECEIVES EVENT=" + ev.getDefaultRep() );
	  		return;
 		}else		
		if (message instanceof QActorMessage){
			handleQActorMessage( (QActorMessage)message );
//	  		println(getName() + " RECEIVES MSG =" + msg.getDefaultRep() );
	  		return;
 		}		 
//		if( message instanceof RequestForMessage ){
//			handleRequestForMessage( (RequestForMessage)message );
//			return;
//		}
		else if (message instanceof ActorTerminationMessage){
			handleTerminationMessage((ActorTerminationMessage)message);
//	  		println(getName() + " RECEIVES TERMINATION MSG =" + msg.getName() );
//	  		this.terminate();
	  		return;
 		}		
	}
 
	protected void handleQActorString(String msgStr) {
		try {
//			println("	%%% "+ getName() + " handleQActorString : " + msgStr  );
			QActorMessage msg = new QActorMessage(msgStr);
			handleQActorMessage( msg );
		} catch (Exception e) {
 			e.printStackTrace();
		}
	}
	protected void handleQActorEvent(IEventItem ev) {
		println(getName() + " (QActor)handleQActorEvent : " + ev.getDefaultRep() );		
	}
 	 
	protected void handleQActorMessage(QActorMessage msg) {
		println(getName() + " (QActor)handleQActorMessage " + msg.getDefaultRep() );	
	}
 	 
	protected void handleTerminationMessage(ActorTerminationMessage msg) {
		println(getName() + " (QActor)handleTerminationMessage "  );	
		this.terminate();
	}
	
	protected QActorMessage currentMessage = null;
	
	public QActorMessage getMsgFromQueue( ){ return currentMessage;}
		

	public void printCurrentMessage(boolean withMemo){
		println("--------------------------------------------------------------------------------------------");
		if(currentMessage != null){
			String msgStr = currentMessage.getDefaultRep();
			println(getName() + " currentMessage=" + msgStr );
			if( withMemo ) addRule(msgStr);
		}
		else println(getName() + " currentMessage IS null"  );
		println("--------------------------------------------------------------------------------------------");
	}
	public void printCurrentEvent(boolean withMemo){
		println("--------------------------------------------------------------------------------------------");
		if(currentEvent != null){
			String eventStr = currentEvent.getDefaultRep();		
			println(getName() + " currentEvent=" + eventStr );
			if( withMemo ) addRule(eventStr);
		}
		else println(getName() + " currentEvent is null"  );
		println("--------------------------------------------------------------------------------------------");
	}
	public void memoCurrentEvent(IEventItem currentEvent , boolean lastOnly) throws Exception{
 		try{
 			if( currentEvent == null ) return;
 			String evId = currentEvent.getEventId();
			Term t = Term.createTerm(currentEvent.getDefaultRep());
// 			println(getName() + " memoCurrentEvent " + currentEvent.getPrologRep() );
			String eventStr = currentEvent.getPrologRep();	
//  	 	println(getName() + " 	memoCurrentEvent:" + evId + " " + eventStr );	
			if( lastOnly ){
				String fact = "msg(A,event,C,none,E,F)".replace("A", evId);
				this.pengine.solve("removeRule("+fact +").");
			}
	 		this.pengine.solve("asserta("+eventStr +").");
		}catch( Exception e){
			println("memoCurrentEvent ERROR " + e.getMessage() );
  		}		
	}
	public void memoCurrentEvent(boolean lastOnly) throws Exception{
		memoCurrentEvent(currentEvent,lastOnly);
	}
	public void memoCurrentMessage(boolean lastOnly) throws Exception{
		String msgStr = currentMessage.getDefaultRep();		
		String msgId  = currentMessage.msgId();
//		println(getName() + " 			memoCurrentMessage:" +msgStr );		 
//		addRule(msgStr);
		if( lastOnly ){
			String fact = "msg(A,B,C,D,E,F)".replace("A", msgId).replace("D", this.getName());
			this.pengine.solve("removeRule("+fact +").");
		}
		this.pengine.solve("asserta("+msgStr +").");
 	}
  	
	
  
/*
 * 	--------------------------------------------------------
 *  METHODS
 * 	--------------------------------------------------------
 */
	public String getName(){
		return actorId;
	}	
	/*
	 * ENTRY POINT of WorldTheory
	 */
	public void println(String msg) {
		outEnvView.addOutput(msg);
	}

/*
 * 	
 */
	public void terminate(){
//		IEventItem ev =  QActorUtils.buildEventItem(  getName(), EventPlatformKb.unregister, evId );
// 		evlpActorSel.tell(ev, getSelf());
		QActorUtils.forgetQActor(this);
		ActorTerminationMessage msg = new ActorTerminationMessage( this.getName(),QActorContext.testing );
// 		this.sendPoisonPill();
		getQActorContext().getSystemCreator().tell(msg, getSelf());

	}
	protected void sendPoisonPill(){
		getSelf().tell(akka.actor.PoisonPill.getInstance(), getSelf());		
	}

	/*
	 * ----------------------------------------	
	 * WORLD THEORY    
	 * ----------------------------------------	
	*/  
		protected void loadWorldTheory() throws Exception{
			try{
	 	   		Theory worldTh = new Theory( getClass().getResourceAsStream("WorldTheory.pl") );
		  		pengine.addTheory(worldTh);
	   	  		pengine.solve("setActorName(" + getName()  + ").");
	  	  		pengine.solve("actorobj(X).");
//	 	  		System.out.println("	%%% "+ getName() + " loadWorldTheory done"   );	 		
	  		}catch( Exception e){
//	 			println(getName() + " loadWorldTheory WARNING: "  + e.getMessage() );
	 			loadWorldTheoryFromFile();
	 		}
	 	}

		protected void loadWorldTheoryFromFile()  {
			try{
 			   	Theory worldTh = new Theory( new FileInputStream(worldTheoryPath) );
		  		pengine.addTheory(worldTh);
	   	  		pengine.solve("setActorName(" + getName()  + ").");
	  	  		pengine.solve("actorobj(X).");
//	 	  		println(getName() + " loadWorldTheoryFromFile done " + worldTheoryPath  );	 		
 	 		}catch( Exception e){
	 			println(" loadWorldTheory WARNING: "  + e.getMessage() );
	 		}		
		}
  		
	public AsynchActionResult delayReactive(int time, String  alarmEvents, String recoveryPlans) throws Exception{
//		IActorAction action = new ActionDummyTimed( this, alarmEvents, outEnvView, time );	 
		String name = QActorUtils.getNewName("da_");		
		String terminationEvId = QActorUtils.getNewName(IActorAction.endBuiltinEvent);
		if( alarmEvents.length() == 0 ){
			Thread.sleep(time);
			return new AsynchActionResult(null, time, false, true, "", null);
		}
		String[] evarray   = QActorUtils.createArray(alarmEvents);
		String[] planarray = QActorUtils.createArray(recoveryPlans);
 
		IActorAction action    = new ActionDummyTimed( name,this,myCtx,terminationEvId, evarray, outEnvView, time );	 
		AsynchActionResult aar = actionUtils.executeReactiveAction(action, ActionExecMode.synch, evarray, planarray);		 
		return aar;		
	}

 	public void waitfor( int dt ){
		try {
			Thread.sleep(dt);
		} catch (InterruptedException e) {
// 			println("QActor delay interrupted");
		}
	}
	public AsynchActionResult playSound(String fName, String terminationEvId, int duration ) throws Exception{
  		println("QActor playSounddd " + fName + " terminationEvId=" + terminationEvId  + " duration=" + duration );
		return playSound(fName,ActionExecMode.synch,terminationEvId,duration,"","");
	}
	//Called by WorldTheory executedCmd
	public AsynchActionResult playSound(String fName, int duration,String alarmEvents, String recoveryPlans) throws Exception{
//  		println("QActor playSound " + fName + " duration=" + duration +  " alarmEvents=" + alarmEvents + " recoveryPlans=" + recoveryPlans);
		String terminationEvId = QActorUtils.getNewName("endSound");
		return playSound(fName,ActionExecMode.synch,terminationEvId,duration,alarmEvents,recoveryPlans);
	}
	//play('./audio/music_interlude20.wav'),20000,"alarm,obstacle", "handleAlarm,handleObstacle"
	public AsynchActionResult playSound(String fName, ActionExecMode mode, String terminationEvId,
		 			int duration,String alarmEvents, String recoveryPlans) throws Exception{
//  		System.out.println("QActor playSound1 " + fName + " terminationEvId=" + terminationEvId + " alarmEvents=" + alarmEvents + " recoveryPlans=" + recoveryPlans);
 		String[] evarray   = QActorUtils.createArray(alarmEvents);
		String[] planarray = QActorUtils.createArray(recoveryPlans);
		if(mode==ActionExecMode.asynch && planarray.length>1) 
			throw new Exception("Plans not supported for asynch actions");
		if(mode==ActionExecMode.asynch && planarray.length==1 && ! planarray[0].equals("continue")) 
			throw new Exception("Only plan=continue is supported for asynch actions");		
// 		println("QActor playSound " + fName + " evarray=" + evarray.length + " recoveryPlans=" + planarray.length);
		IActorAction action = 
				ActionUtil.buildSoundActionTimed(this,myCtx,outEnvView,duration,terminationEvId,fName,evarray);			
		AsynchActionResult aar=actionUtils.executeReactiveAction(action, mode, evarray, planarray);
//     	println("QActor playSound " + fName + " aar=" + aar);
		return  aar;
	}

	public AsynchActionResult fibo(ActionExecMode mode, int n, String terminationEvId,
			int duration,String alarmEvents, String recoveryPlans) throws Exception{ 
		String[] evarray   = QActorUtils.createArray(alarmEvents);
		String[] planarray = QActorUtils.createArray(recoveryPlans);

		if(mode==ActionExecMode.asynch && planarray.length>1) 
			throw new Exception("Plans not supported for asynch actions");
		if(mode==ActionExecMode.asynch && planarray.length==1 && ! planarray[0].equals("continue")) 
			throw new Exception("Only plan=continue is supported for asynch actions");
		
  		IActorAction action    = ActionUtil.buildFiboActionTimed(this,myCtx,outEnvView, n, duration,terminationEvId,evarray);			
		return  actionUtils.executeReactiveAction(action, mode, evarray, planarray);		
	}
  
	 /*
	  * ----------------------------------------------
	  * RULES	
	  */
	 	
	 	public  synchronized void addRule( String rule  ){
	 		try{
//    	 			println("addRule:" + rule   );
	 			if( rule.equals("true")) return;
//  	 			println("addRule:" + rule   );
	  			SolveInfo sol = 
	  					pengine.solve( "addRule( " + rule + " ).");
//  	 			println("addRule:" + rule + " " + sol.isSuccess() );
 	  		}catch(Exception e){
	  			println("addRule ERROR:" + rule + " " + e.getMessage() );
	   		}
	   	}
	 	public  synchronized void removeRule( String rule  ){
	 		try{
	 			rule = rule.trim();
	 			if( rule.equals("true")) return;
	 			SolveInfo sol = pengine.solve( "removeRule( " + rule + " ).");
//   	 			println("removeRule:" + rule + " " + sol.isSuccess() );
	 	  	}catch(Exception e){
	  			println("removeRule ERROR:" + e.getMessage() );
	   		}	 		
	 	}
	 	
	 	public SolveInfo solveGoal(String goal ){
	 		SolveInfo sol = null;
	 		try {
// 	 			println(" ***   solveGoal goal "  + goal + " dir= "  + pengine.getCurrentDirectory() );				
	 			sol = pengine.solve(goal+".");
//	 			if( sol != null && sol.isSuccess()) 
//	 				pengine.solve("setPrologResult(SOL).".replace("SOL", sol.getSolution().toString() ));
//	 			else pengine.solve("setPrologResult(failure)." );
// 	 			println(" ***  solveGoal SolveInfo dir= "  + pengine.getCurrentDirectory() );				
	 	 	} catch (Exception e) {
	 			println("solveGoal sol WARNING: "  + e.getMessage() + " goal=" + goal);
	 	 	}	
	 		return sol;
	 	}
		/*
		 * New operation to solve a sentence (originated by the Talk project)	 	
		 */
	 	public AsynchActionResult solveSentence( String sentence ) throws Exception{
		 	System.out.println("QActor solveSentence " + sentence);
	 		Term guard , goal, dt ,  planFail , events , plans ;
	 		Struct at = (Struct) Term.createTerm(sentence);
	 		//sentence6(true,fib(12,V_e0),1000,failPlan,alarms,alarmsPlan)
	 		int arity=at.getArity();
		 		if( arity < 6 ){
		 			return new AsynchActionResult(null,0,false,true,"failure",null);
		 		}
		 	guard     = at.getArg(0);
		 	goal      = at.getArg(1);
		 	dt        = at.getArg(2);
		 	planFail  = at.getArg(3);
		 	events    = at.getArg(4);
		    plans     = at.getArg(5);
	 		String ev = events.toString();
	 		String pl = plans.toString();
	 		if( ev.equals("''")) ev="";
	 		if( pl.equals("''")) pl="";
		 		int duration = Integer.parseInt(""+dt);
		 		String planFailStr = planFail.toString();
		 		if( planFailStr.equals("''")) planFailStr="";
		 		System.out.println("QActor solveSentence solveGoal "+ goal + " planFailStr=" + planFailStr + " duration=" + duration);
	 	 		AsynchActionResult aar = solveGoal( ""+goal, duration,  ev,  pl);
			if( aar.getResult().equals("failure")){
				System.out.println("QActor solveSentence solveGoal "+ goal + " failure" );
	    		if( ! planUtils.switchToPlan(planFailStr).getGoon() ){  
	    		}else if( ! aar.getGoon() ) { }
			}else{
//		 		println("QActor solveSentence result="+aar.getResult());
		 			solveGoal("setAnswer("+aar.getResult()+").");
		 			//Show the result in the user GUI
		 			//pengine.solve("actorPrintln("+aar.getResult()+").");
			}
	 		return aar;
	 	}	

/*
 * SHOULD BE MOVED in QActorPlanUtils
 */
	 	public AsynchActionResult solveGoal( String goal, int duration, String events, String plans) throws Exception{
//	 		System.out.println("QActor solveGoal " + goal + " duration=" + duration +" events=" + events  + " isHalted=" + pengine.isHalted() );
	 		//The goal executeInput(do(GUARD,MOVE,TIME))
        	 if( duration == 0   ){
//        		System.out.println("solveGoal immediate " + goal   );
	 			SolveInfo sol = pengine.solve(goal+"."); 
//	 			System.out.println("QActor solveGoal " + goal + " sol=" + sol.isSuccess() ); //+ " " + pengine.getCurrentDirectory() 
// 	 	 		pengine.setCurrentDirectory(this.pengigeBasicDirectory); 			
 	 			if( sol != null && sol.isSuccess() ){
  	 				pengine.solve("setPrologResult(" + sol.getSolution()+").");
	 				return new AsynchActionResult(null,0,false,true,sol.getSolution().toString(),null);
	 			}else{
 	 				this.pengine.solve("setPrologResult( failure ).");
	 				return new AsynchActionResult(null,0,false,true,"failure",null);	 				
	 			}
	 		}
	 		/*
	 		 * duration > 0 => we must use reactive 
	 		 */
        	return solveGoalReactive( goal,   duration,   events,   plans);
	 	}
	
	 	
	 	private Prolog pnewengine ;
	 	private Prolog normalengine ; 
	 	protected void changePrologMachine() throws Exception{
 	   		pnewengine      = new Prolog(); 
 	   	    normalengine    = pengine; 
  		    Theory t        = pengine.getTheory();
 		    println("			%%%%%%%%%%%%%%%% QActor changePrologMachine   "   );
			try{
//	 		    	Iterator<? extends Term> iter =  t.iterator(pengine);
//		  		    while( iter.hasNext() ){
//		  		    	println("		"  + iter.next() );
//		  		    }
					pnewengine.addTheory(  t ); //
					pengine = pnewengine;
//				 	SolveInfo sol = pengine.solve("fibo(8,V).");
//					System.out.println("	%%% sol1="  + sol );	 		
			}catch(Exception e){
					println("QActor solveGoalReactive addTheory ERROR "  +  e);
				  	pengine = pnewengine;
//			 	  	SolveInfo sol = pengine.solve("fibo(8,V).");
//					System.out.println("	%%% sol2="  + sol );	 		
			}
	 	}
		protected void restorePrologMachine(){
 		    println("			%%%%%%%%%%%%%%%% QActor restorePrologMachine   "   );
			pengine = normalengine;		 	 		
		 }
		
	 	public AsynchActionResult solveGoalReactive( String goal, int duration, String events, String plans) throws Exception{
//      	    println("QActor solveGoalReactive0 " + goal  + " duration=" +  duration +" events=" + events );
 		 	   String[] evArray    = QActorUtils.createArray( events );
	      	   String[] planArray  = QActorUtils.createArray( plans );
	      	   String endSolveEvId = QActorUtils.getNewName("local_goalResult");
	      	   
	     	  IActorAction action =  new ActionSolveTimed(
	   						"solve", this, myCtx, pengine, goal,
	   						endSolveEvId, evArray, outEnvView, duration);
	     	  AsynchActionResult aar = actionUtils.executeReactiveAction(action, ActionExecMode.synch, evArray,planArray ); 
 	     	  return aar;
	 	}
	 	
	 	
	 	public AsynchActionResult solveGoalReactiveOkComplex( String goal, int duration, String events, String plans) throws Exception{
//   	 	println("QActor solveGoalReactive  " + goal  + " duration=" +  duration +" events=" + events );
//      	   if( duration == 0 ) return solveGoal(goal,   duration,   events,   plans);	 		
	 	   String[] evArray    = QActorUtils.createArray( events );
      	   String[] planArray  = QActorUtils.createArray( plans );
      	   String endSolveEvId = QActorUtils.getNewName("local_goalResult");
     	   IActorAction action =  new ActionSolveTimed(
   						"solve", this, myCtx, pengine, goal,
   						endSolveEvId, evArray, outEnvView, duration);
//		   QActorUtils.getNewName(IActorAction.endBuiltinEvent), evArray, outEnvView, duration);
//  	   return  actionUtils.executeReactiveAction(action, ActionExecMode.synch, evArray, planArray ); //March 2017     	   
     	   		/* 
     	   		 * March 2017 : 
     	   		 * 1) Create a ActionSolveTimed action reactive to the given events (e.g. alarm) with termination event "local_goalResult"
     	   		 * 2) Launch the solve action in asynchronous mode in order to be able wait for a termination event or for alarm 
     	   		 * 3) Wait for the event local_goalResult within a sense reactive to the given events
     	   		 * 4) if some event is raised before local_goalResult the senses executes the recovery plan
     	   		 */
//  			System.out.println("QActor executeReactiveAction  ActionSolveTimed for  "  + goal    );
    			actionUtils.executeReactiveAction(action, ActionExecMode.asynch, new String[]{}, new String[]{} ); 
    	    //senseEvent
//   			System.out.println("QActor solveGoalReactive  senseEvents ....................... "      );
   			/*
   			 * CASE STUDY: 
   			 * 1) a long solve is reactive to an event alarm
   			 * 2) if such event occurs, the ActionSolveTimed action is interrupted and the endSolveEvId is generated
   			 * 3) the senseEvents must 'react' first to  alarm (and not to to endSolveEvId) in order to execute the alternatiuve plan 
    		 */
     		AsynchActionResult aar = planUtils.senseEvents( duration,endSolveEvId,"continue",events,plans,ActionExecMode.synch );
      		removeRule( "goalResult( going )" );	//going is the result of the asynch exec of ActionSolveTimed
   		if( ! aar.getGoon() || aar.getTimeRemained() <= 0 ){
   				String parg="goalResult( tout )";
    			addRule( parg ); 
   		} 
   		printCurrentEvent(false);
    		//onEvent
   		if( currentEvent.getEventId().startsWith("local_goalResult") ){
   		 		String parg="goalResult(R)";
   		 		/* AddRule */
   		 		parg = updateVars(Term.createTerm("local_goalResult(A,G,R)"),  Term.createTerm("local_goalResult(A,G,R)"), 
   		 			    		  					Term.createTerm(currentEvent.getMsg()), parg);
   		 		if( parg != null ) addRule(parg);	    		  					
   		 }   		
   		return aar;
  		   
	 	}
	 	
   		
    		
	 	 

		/*
		 * --------------------------------------------------- 
		 * REFLECTION
		 * ---------------------------------------------------
		 */
		public boolean execByReflection(Class C, String methodName) {
			Method method = null;
			Class curClass = C;
//  			println("QActor execByReflection " + methodName + " curClass=" + curClass );
			while (method == null)
				try {
					if (curClass == null)
						return false;
					method = getByReflection(curClass, methodName);
					if (method != null) {
//						println("QActor execByReflection method: " +method + " inclass " + curClass.getName());
						Object[] callargs  = null;
						Object returnValue = method.invoke(this, callargs);
//						println("QActor execByReflection " + methodName + " returnValue: " +returnValue );
						Boolean goon = (Boolean) returnValue;
						return goon;
					} else {
//						println("QActor execByReflection " + methodName + " notfound in " +curClass.getName() );
						curClass = curClass.getSuperclass();
					}
				} catch (Exception e) {
					// If the method does not exist or does not return a boolean
 					println("QActor execByReflection " + methodName + "  WARNING: " + e.getMessage());
					// break;
				}
			return false;
		}
		public Object execByReflection(Class C, String methodName, Object[] callargs, Class params[] ) {
			Method method = null;
			Class curClass = C;
// 			println("QActor execByReflection " + methodName + " callargs=" + callargs.length );
			while (method == null)
				try {
					if (curClass == null) return false;
 					method = getByReflection( curClass, methodName, params );
//					println("QActor execByReflection method: " +method );
					if (method != null) {
  						Object returnValue = method.invoke(this, callargs);
						// println("QActor execByReflection returnValue: " +returnValue );
 						return returnValue;
					} else {
//						println("QActor execByReflection " + methodName + " not found in " +curClass.getName() );
						curClass = curClass.getSuperclass();
					}
				} catch (Exception e) {
 					println("QActor execApplicationActionByReflection " + methodName + "  ERROR: " + e);
 				}
			return false;
		}

		public Method getByReflection(Class C, String methodName) {
			try {
				Class noparams[] = {};
				Method method = C.getDeclaredMethod(methodName, noparams);
				return method;
			} catch (Exception e) {
				// println("QActor getByReflection ERROR: " + e.getMessage() );
				return null;
			}
		}
		public Method getByReflection(Class C, String methodName, Class params[]) {
			try {
 				Method method = C.getDeclaredMethod(methodName, params );
				return method;
			} catch (Exception e) {
				// println("QActor getByReflection ERROR: " + e.getMessage() );
				return null;
			}
		}

  		protected AsynchActionResult actorOpExecuteReactive(
						String parg, int duration, String events, String plans) throws Exception{
			 	   String[] evArray    = QActorUtils.createArray( events );
		      	   String[] planArray  = QActorUtils.createArray( plans );
		      	   String endSolveEvId = QActorUtils.getNewName("local_goalResult");
		      	   
		     	   IActorAction action =  new ActionOpTimed(
		   						"actorOp", this, myCtx, parg,
		   						endSolveEvId, evArray, outEnvView, duration);
		     	  AsynchActionResult aar = actionUtils.executeReactiveAction(action, ActionExecMode.synch, evArray,planArray ); 
		     	  return aar;				
			}
		
		public java.lang.String testReflectString(String n){
			println( "QQQQQQQQQQQQQQQQQQQQQQQ testReflectString " + n );
			return "test" + n.replace("'", "");
		}
		public int testReflectInt(int n){
			return n;
		}
		
		
 		
		/*
	 	 * 		
	 	 */
		//JUNE2017
		protected boolean checkInMsgQueue(){
			if( mysupport.getSizeOfMsgQueue( ) > 0 ){
				QActorMessage msg = mysupport.getMsgFromQueue();
				println(  "			rrrrrrrrrrrrrrrreceiveAMsg " + msg) ;
				this.currentMessage = msg;
				return true;
			}else return false;
		}


		protected int waitForUserCommand( )  {		
	 		  		try {
	 		  			int inp;
	 		  			int ch;
	 		  			System.out.println("USER>: to end press 'e'" );
	 		  			ch = System.in.read();
	 		  			System.out.println("user:" + ch);
	 		  			do{
	 		  				inp = System.in.read();
	 		  			}while( inp != 10 );
	 		  			return ch;			
	 		  		} catch (Exception e) {
	 		  			System.out.println("USER>: ERROR" );
	 		  			return 'e';
	 		  		}
	 		  	}
	 		 	
		/*
		 * Local to QActor since it modifies guardVars
		 */

		public String updateVars( Term tmsgdef, Term tmsguser, Term tmsg, String swithvar) throws Exception{
			Hashtable<String,String> htss = new Hashtable<String,String>();
			//1) Check msg templates
			guardVars = null;
// 	 	println("*** updateVars tmsguser=" + tmsguser + " tmsg=" + tmsg + " tmsgdef=" + tmsgdef + " guardVars="+ guardVars + " swithvar=" + swithvar);
		SolveInfo sol = pengine.solve( tmsgdef + "  = "+ tmsguser +".");
			if( sol.isSuccess()){
				QActorUtils.memoVars(sol,htss);  				
			}else new Exception("msg template do not match");
		//2) Check msg payload
		sol = pengine.solve( tmsguser + "  = "+ tmsg +".");
		if( sol.isSuccess())  QActorUtils.memoVars(sol,htss); else{
 		    //println("*** no match between tmsguser=" + tmsguser + " and tmsg=" + tmsg);
			return null;// MARCH2017 swithvar; //the msg payload does not match
		}
	  		//Copy variables in guardVars, otherwise guardVars.get(..) does not work
	  		guardVars = new Hashtable<String,String>();
	  		Enumeration<String> es = htss.keys();
	  		while(es.hasMoreElements()){
	  			String key=es.nextElement();
// 	  			println("*** updateVars/5 (4) " + key + "  " + htss.get(key) );
	  			guardVars.put(key, htss.get(key));
	  		}
// 	  	println("*** updateVars/5 (1) swithvar=" + swithvar );
  		if( guardVars != null ) swithvar = QActorUtils.substituteVars(guardVars,swithvar);
// 	 	println("*** updateVars/5 (2) " + swithvar );
 		swithvar = QActorUtils.substituteVars(htss,swithvar);  //TODO AKKA - QUTILS substituteVars COMMENTED
// 	   	println("*** updateVars/5 (3) " + swithvar + " " + guardVars);
		return swithvar;
	} 
	 	
		public void replyToCaller(String msgId, String msg) throws Exception {
			String caller = currentMessage.msgSender().replace("_ctrl", "");
			println(getName() + " replyToCaller  " + msgId + ":" + msg + " to " + caller );
			sendMsg(msgId, caller, QActorContext.dispatch, msg);
		}
	 	/*
	 	 * -------------------------------------------------------------
	 	 * actorop  results 
	 	 * -------------------------------------------------------------
	 	 */ 
protected Struct resultName = new Struct( "actoropresult"  );	 
public void setActionResult(  Object actionResult ) throws Exception{
//	println("QActor setActionResult " +  " " + actionResult );
	if( actionResult == null ) unregisterResult();
	else this.registerResult(actionResult);
}
public void registerResult (Object obj) throws InvalidObjectIdException{
	registerResultInProlog18(obj);
}
public void unregisterResult( ) throws InvalidObjectIdException{
	unregisterResultInProlog18();
}
public void unregisterResultInProlog18( ) throws InvalidObjectIdException{
//	println("QActor unregisterResultInProlog18: " );
	Library lib = pengine.getLibrary("alice.tuprolog.lib.OOLibrary");
		((alice.tuprolog.lib.OOLibrary)lib).unregister( resultName ); 
}
public void registerResultInProlog18(Object obj) throws InvalidObjectIdException{
//	println("QActor registerResultInProlog18: " + resultName + " " + obj);
	Library lib = pengine.getLibrary("alice.tuprolog.lib.OOLibrary");
		((alice.tuprolog.lib.OOLibrary)lib).register( resultName, obj); 
}
//For talktheory (action interpreter)
public boolean isSimpleActor(){
	return true;
}

/*
 * MQTT (2017)
 * WARNING: in this version a qactor can connect to a single MQTTT server only
 * since we keep track (after a connect) of a single mqttclientid and mqttServerAddr 
 */


protected MqttUtils mqttUtil = MqttUtils.getMqttSupport();
protected String mqttclientid    ="";
protected String mqttServerAddr  = "";

	public void connectToSend( String clientid, String mqttServerAddr, String topic ) throws Exception{
		mqttclientid     = clientid ;
		mqttServerAddr   = mqttServerAddr;
		topic            = topic;
 		println("connectToSend " + clientid + " " + this.mqttServerAddr + " | " + topic);
		mqttUtil.connect(this,mqttclientid,mqttServerAddr, topic );
	}
	public void connectToReceive( String clientid, String mqttServerAddr, String topic ) throws Exception{
		mqttclientid     = clientid ;
		mqttServerAddr   = mqttServerAddr;
		topic            = topic;
 		println("connectToReceive " + mqttclientid + " " + mqttServerAddr + " | " + topic );
		mqttUtil.connect(this,mqttclientid,mqttServerAddr, topic );
	}
/* 
 * A receiver connects to a topic and subscribes to that topic	
 */
	public void connectAndSubscribe( String clientid, String mqttServerAddr, String topic ) throws Exception{
		connectToReceive( clientid,   mqttServerAddr,   topic);
		subscribe(topic);
	}
 	public void discconnect() throws Exception{
 		mqttUtil.disconnect();
	}
	public void sendMsgMqtt(  String topic, String msgID, String dest, String msg ) throws Exception{
// 		println("			%%%  sendMsgMqtt "  + dest + " msgID=" + msgID + " msg=" + msg);
		QActorMessage mout  = QActorUtils.buildMsg(myCtx, getName(), msgID, dest, "dispatch", msg);
		publish( getName(), mqttServerAddr, topic, mout.getDefaultRep(), 1, true);
	} 
	public void sendReplyMqtt(  String topic, String msgID, String msg ) throws Exception{
//		println("			%%%  sendReplyMqtt "  + topic + " msgID=" + msgID + " msg=" + msg);
		if( topic == null ) return;  
		String sender = mqttUtil.getSender();
		if( sender.equals("notyet")){
			println("			%%%  sendReplyMqtt WARNING: no sender"  );
			return;
		}
//		println("			%%%  sendReplyMqtt "  + mqttUtil.getSender() + " topic=" + topic+replySuffix);
		QActorMessage mout  = QActorUtils.buildMsg(myCtx, getName(), msgID, sender, "dispatch", msg);
		publish( getName(), mqttServerAddr, topic , mout.getDefaultRep(), 1, true);
	} 
	public void publish( String clientid, 
			String mqttServerAddr, String topic, String msg, int qos, boolean retain) throws Exception{
// 		println("			%%%  publish on "  + topic + " msg=" + msg);
		mqttUtil.publish(this, clientid, mqttServerAddr, topic,msg,qos,retain);
	} 
	public void subscribe(  String topic) throws Exception {
// 		println("			%%%  subscribe for " + mqttclientid + " on " + topic );
		mqttUtil.subscribe(this,mqttclientid,mqttServerAddr, topic);
 		println("			%%%  subscribe done for " + mqttclientid + " on " + mqttServerAddr + " | "+ topic );
	}	
	public void clearTopic(String topic) throws Exception{
// 		println("			%%%  clearTopic " + mqttServerAddr + " for " + mqttclientid + " on " + topic );
		mqttUtil.publish(this, getName(), mqttServerAddr, topic, "", 1,true);
	}

/*
 * =============================================================
 * UNITY	
 * =============================================================
 */
 
	protected IConnector actorConnToUnity;
	
	public IConnector getConnector() {
		if( actorConnToUnity == null ) actorConnToUnity = this.getQActorContext().getUnityConnector();
 		return actorConnToUnity;
	}
	 
	public void setConnector(IConnector actorConnToUnity) {
 		this.getQActorContext().setUnityConnector(actorConnToUnity);;
	}	
	public void sendonconn(String msg){
		println("	+++ QActor Unity sendOnConn " + msg);
		getConnector().send(msg);
	}
	public void send(String msg){
		println("send original " + msg);
		if( actorConnToUnity != null ) actorConnToUnity.send(msg);
	}	
/*
 * SIMPLE entry	
 */
	public void workWithUnity( ){
		println("	+++ QActor Unity workWithUnity/0 " );
		initUnityConnection("127.0.0.1");
		createSimulatedActor();
	}	

	public void workWithUnity( String ipaddr ){
		initUnityConnection(ipaddr);
//		createSimulatedActor();
	}	
	public void initUnityConnection( String ipaddr ){
		try{
			println("	+++ QActor Unity initUnifyConnection " + ipaddr );
			setConnector( new UnityConnector(ipaddr, 6000, this) ); //6000 is the unity port
			getConnector().connect();
			println("	+++ QActor Unity initUnifyConnection connected " + actorConnToUnity );
			getConnector().setupActorSimulatorName(); //set the actor name at unity site	
		}catch(Exception e){
			println("	+++ QActor Unity ERROR "+ e.getMessage());
		}
	}
	public void createSimulatedActor( ){
//		println("	+++ QActor Unity createSimulatedActor/0 " );
 		createSimulatedActor( getName().replace("_ctrl",""),"Prefabs/CustomActor",-35,0,20,0,0,0,0);
	}	
	public void createSimulatedActor( String name, String prefabs){
 		println("	+++ QActor Unity createSimulatedActor/2 " );
		createSimulatedActor(name, prefabs,-25,0,10,0,0,0,0); 
	}	 
	public void createSimulatedActor( String name, String prefabs, float x, float y){
//		println("	+++ QActor Unity createSimulatedActor/3 " );
		createSimulatedActor(name, prefabs,x,y,0,0,0,0,0);
	}	
	public void createSimulatedActor( String name, String prefabs, float x, float y, float z){
//		println("	+++ QActor Unity createSimulatedActor/4 " );
		createSimulatedActor(name, prefabs,x,y,z,0,0,0,0);
	}	
	public void createSimulatedActor( String name, String prefabs, 
			float x, float y, float z, float qx, float qy, float qz, float v){
// 		String name = this.getName().replace("_ctrl","");
//		String name = "robotfacade";	//MAY2017
		println("	+++ QActor Unity createSimulatedActor/8 " );
		CreateCommand cc = new CreateCommand(name,prefabs,x,y,z,qx,qy,qz,v);
		getConnector().send(cc);
	}	
	public void moveVirtualActor(String direction, float speed, float duration, float angle){
		//move(Direction, Speed, Duration, Angle)
   		println("	+++ QActor Unity moveVirtualActor direction=" + direction + " speed=" + speed + " T=" +  duration );
   		String name = this.getName().replace("_ctrl","");
//  		Move move = new Move("robotfacade",direction,speed,duration,angle);
  		Move move = new Move(name,direction,speed,duration,angle);
  		getConnector().send(move);
    	this.waitfor(Math.round(duration)*1000);
	}
	
	public void simulateMovementAction(String name, String direction, float speed, float duration, float angle){
//		println("	+++ QActor Unity simulateMovementAction/5 " + name);
 		Move move = new Move(name,direction,speed,duration,angle);
 		getConnector().send(move);
	}
	public void simulateMovementAction(String direction, float speed, float duration, float angle){
		String name = this.getName().replace("_ctrl","");
		simulateMovementAction(name, direction,   speed,   duration,   angle);
	}

/*
 * ACTOR CREATION 	
 */
	public void createNewActor( String name , String className ){
		try {
//			SystemCreationActor.numOfActors = SystemCreationActor.numOfActors + 1;
			this.myCtx.addInstance(this.myCtx, name, className, this.outEnvView);
			//JUNE2017
//			if( name.equals("filter8") ){
//				System.out.println("			QActor DONE createNewActor " + name);
//				new it.unibo.qactors.platform.CheckThreadsRunner().start();
//			}
		} catch (Exception e) {
 			e.printStackTrace();
		}
	}
	

 }
