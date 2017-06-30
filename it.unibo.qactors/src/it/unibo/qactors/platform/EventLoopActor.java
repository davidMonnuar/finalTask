package it.unibo.qactors.platform;

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import alice.tuprolog.SolveInfo;
import java.util.Hashtable;
import it.unibo.contactEvent.interfaces.IEventItem;
import it.unibo.contactEvent.interfaces.ILocalTime;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactors.ActionRegisterMessage;
import it.unibo.qactors.QActorContext;
import it.unibo.qactors.ActorTerminationMessage;
import it.unibo.qactors.QActorMessage;
import it.unibo.qactors.QActorUtils;
import it.unibo.qactors.action.ActorTimedAction;
import it.unibo.qactors.akka.QActor;

public class EventLoopActor extends QActor{
	public static ILocalTime localTime = new LocalTime(0);	
	private boolean testMode = false; 
	private IEventItem lastEvent = null;
	
	private Hashtable<String, Vector<ActorTimedAction>> actionTab = 
			new Hashtable<String, Vector<ActorTimedAction>>();  //WARNING could be accessed in concurrent way
	
	public EventLoopActor(String actorId, QActorContext myCtx, IOutputEnvView outEnvView) {
		super(actorId, myCtx, outEnvView);
		testMode = myCtx.testing();
 	}
	public static ILocalTime getLocalTime(){
		return localTime; 
	}

	@Override
	protected void doJob() throws Exception {
//		println("QActor " + getName() + " doJob "  ); 
	}
 
//	public IEventItem getLastEvent(){
//		return lastEvent;
//	}
// 	public void setLocalTime(int v){
//		localTime = new LocalTime( v );
//	}
	protected void incLocalTime(){
		long v    = localTime.getTheTime();
		localTime = new LocalTime( v + 1 );
//		println("                            time=" + localTime.getTheTime());
	}
	@Override
	public void onReceive(Object message) throws Throwable {
		incLocalTime();
//		if( testMode ) 
//		println(getName() + " RECEIVES MSG " + message + " from " + getSender() );		
 		super.onReceive(message); //executes handle
 		if (message instanceof ActionRegisterMessage){
 			ActionRegisterMessage msg = (ActionRegisterMessage) message;
 			if( msg.totegister ){
 				memoAction(msg.evId, msg.action);
 			}else {
 				removeAction( msg.evId, msg.action );
 			}
 		}
 	}

 	protected void memoAction(String evId, ActorTimedAction action){
//  		println("	%%% "+ getName() + " EventLoopActor memoAction " + evId + " action=" + action );
 		Vector<ActorTimedAction> v = actionTab.remove(evId);
 		Vector<ActorTimedAction> v1;
 		if( v == null ){
 			v1 = new Vector<ActorTimedAction>();
 		}else{
 			v1 = v;
 		}
		v1.add(action);
		actionTab.put(evId,v1);
 	}
 	protected void removeAction( String evId, ActorTimedAction action ){
 		Vector<ActorTimedAction> v = actionTab.remove(evId);
 		Iterator<ActorTimedAction> iter = v.iterator();
 		while( iter.hasNext() ){
 			ActorTimedAction a = iter.next();
 			if( a == action ){
 				v.remove(a);
 			}
 		}
 	}
 	protected synchronized void removeAnAction( String actionName ){
 		Set<String> skey = actionTab.keySet();
 		Iterator<String> iterKey = skey.iterator();
 		while( iterKey.hasNext() ){
 			Vector<ActorTimedAction> v     = actionTab.get( iterKey.next() );
 			Vector<ActorTimedAction> vtemp = new Vector<ActorTimedAction>(); 
 			//Copy the vector
			Iterator<ActorTimedAction> iter = v.iterator();
 	 		while( iter.hasNext() ){
 	 			vtemp.add( iter.next() );
  	 		}//while  			 
// 			println("	%%% "+ getName() + " vtemp= " + v  );	
 			Iterator<ActorTimedAction> iterV = vtemp.iterator();
 	 		while( iterV.hasNext() ){
 	 			ActorTimedAction a = iterV.next();
   	 			if( a.getName().equals(actionName) ){ 
//   		 			println("	%%% "+ getName() + " REMOVING  " + actionName  );	
   	 				v.remove(a); 
  	 			}
 	 		}//while 		
 	 	}//while 		
   	}
	
	@Override
	protected void handleQActorString(String msg) {
//		println("	%%% "+ getName() + " RECEIVES String " + msg  );	
		try {
			IEventItem ev = new EventItem(msg);
			handleQActorEvent(ev);
		} catch (Exception e) {
 		}
 	}
	@Override
	protected void handleQActorEvent(IEventItem ev) {
		try {
			localTime = new LocalTime( localTime.getTheTime()+1 );
// 			if( testMode ){
 				lastEvent = ev;
// 				println(getName() + " HANDLE EVENT " + ev.getDefaultRep() + " from " + ev.getSubj() );		
// 			}
			elab( ev );
		} catch (Exception e) {
 			e.printStackTrace();
		}
	}

	@Override
	protected void handleQActorMessage(QActorMessage msg) {
		println("	%%% "+ getName() + " RECEIVES QActorMessage " + msg.getDefaultRep() );	
	}

	@Override
	protected void handleTerminationMessage(ActorTerminationMessage msg) {
		println("	%%% "+ getName() + " RECEIVES ActorTerminationMessage "  );	
		this.terminate();
	}
	
	
	
	/*
	 * Sends the event to all the actors registered for it
	 */
	protected void elab(IEventItem event) throws Exception{
//   		println("	%%% "+ getName() + " elab " + event.getDefaultRep() );
  		//register
		if( event.getEventId().equals( EventPlatformKb.register) ){
			insertInEventWaitQueue( event.getSubj(), event  );
//			getSender().tell("register done for " + getSender(), getSelf()); //The sender is a temp/$a actor
			getSender().tell("done register for " + event.getMsg() , getSelf());
 			return;
		}
  		//getLastEvent
		if( event.getEventId().equals( EventPlatformKb.getLastEvent) ){
  //		println("	%%% "+ getName() + " getLastEvent " + event.getSubj()   );
			
			getSender().tell( lastEvent.getDefaultRep() , getSelf() );
 			return;
		}
  		//register
		if( event.getEventId().equals( EventPlatformKb.unregister) ){
			removeFromEventWaitQueue( event.getSubj(), event  );
 //			println("	%%% "+ getName() + " unregister " + event.getSubj()   );
 			return;
		}
		if( event.getEventId().equals( EventPlatformKb.unregisterAll) ){
			removeFromEventWaitQueue(  event.getMsg()  );
//			println("	%%% "+ getName() + " unregisterForAllEvents " + event.getMsg()  );
 			return;
		}
		if( event.getEventId().equals( EventPlatformKb.unregisterAction) ){
//			println("	%%% "+ getName() + " unregisterAction " + event.getMsg()  );
			removeAnAction(  event.getMsg()  );		
 			return;
		}
		elabEventWaitQueue(event);
		elabActionQueue(event);
	}
	
	public void insertInEventWaitQueue(String subj, IEventItem ev)  throws Exception {
		String goal = "assert( waiting(ACTOR,EVENT) ).".replace("ACTOR",subj).replace("EVENT",ev.getMsg());
 		pengine.solve(goal);
//		println("	%%% "+ getName() + " solved " + goal   );
 	}
	public void removeFromEventWaitQueue(String subj, IEventItem ev)  throws Exception {
		String goal = "retract( waiting(ACTOR,EVENT) ).".replace("ACTOR",subj).replace("EVENT",ev.getMsg());
 		pengine.solve(goal);
 	}
	public void removeFromEventWaitQueue(String subj )  throws Exception {
		String goal = "retract( waiting(ACTOR,EVENT) ).".replace("ACTOR",subj) ;
 		SolveInfo sol = pengine.solve(goal);
 		if( sol.isSuccess() ){
			println( "	%%% EventLoopActor remove "+ subj + " for " + sol.getVarValue("EVENT"));  
	 		while( pengine.hasOpenAlternatives()){
	 			sol = pengine.solveNext();
	 			if( sol.isSuccess() ) 
	 				println( "	%%% EventLoopActor remove "+ subj + " for " + sol.getVarValue("EVENT"));
	 		}
 		}
	}
	
	protected void elabEventWaitQueue(IEventItem ev) throws Exception {
  		String goal   = "waiting(ACTOR,EVENT).".replace("EVENT",ev.getEventId());//
 		SolveInfo sol = pengine.solve(goal);
		if( sol.isSuccess() ){
			String actorName = sol.getVarValue("ACTOR").toString();
 			println( "			%%% EventLoopActor actorName="  +  actorName + " evId="  +  ev.getEventId() );  
			/*
			ActorSelection sel = QActorUtils.getSelectionIfLocal(myCtx, actorName);
			//sel MUST be != null
			if( sel == null ){
				println( "			%%% EventLoopActor actorName="  +  actorName + " IS NOT AN ACTOR"  );
				return;
			}
			//Send the event to the actor as a message
			sel.tell(ev, getSelf() );
			while( sol.hasOpenAlternatives() ) {
				sol = pengine.solveNext();
				if( sol.isSuccess() ){
					actorName = sol.getVarValue("ACTOR").toString();
					sel = QActorUtils.getSelectionIfLocal(myCtx, actorName);
 					sel.tell(ev, getSelf() );						
				}
			}
			*/
			//JUNE2017 after update to 2.5.2
			ActorRef aref = myCtx.getActorRefInQActorContext(actorName);
			if( aref == null ){
				println( "			%%% EventLoopActor actorName="  +  actorName + " IS NOT AN ACTOR"  );
				return;
			}
			//Send the event to the actor as a message
			println( "			%%% EventLoopActor send "  +  ev + " to " + actorName + "  "  );
			aref.tell(ev, getSelf() );
			while( sol.hasOpenAlternatives() ) {
				sol = pengine.solveNext();
				if( sol.isSuccess() ){
					actorName = sol.getVarValue("ACTOR").toString();
					aref = myCtx.getActorRefInQActorContext(actorName);
					aref.tell(ev, getSelf() );						
				}
			}
			
		}
 	}//elabEventWaitQueue
	
	protected void elabActionQueue(IEventItem ev) throws Exception {
//  		println( "	%%% EventLoopActor elabActionQueue="  +  ev.getDefaultRep()  );
		Vector<ActorTimedAction> v = actionTab.get(ev.getEventId());
//		println("	%%% EventLoopActor v= " + v );
		if( v == null ) return;
 		Iterator<ActorTimedAction> iter = v.iterator();
 		while( iter.hasNext() ){
 			ActorTimedAction a = iter.next();
// 			println("	%%% EventLoopActor setInterruptEvent " + ev.getDefaultRep() + " in " + a );
 			a.setInterruptEvent(ev);	//inject  in action
  			if( ! a.isSuspended() ) a.suspendAction();  //the same job of ActionTimedEventHandler
   		}
	}
}
