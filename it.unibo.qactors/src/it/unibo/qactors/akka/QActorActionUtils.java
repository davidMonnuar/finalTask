package it.unibo.qactors.akka;

 
import alice.tuprolog.SolveInfo;
import alice.tuprolog.Struct;
import alice.tuprolog.Term;
import it.unibo.contactEvent.interfaces.IEventItem;
import it.unibo.contactEvent.interfaces.ILocalTime;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactors.QActorMessage;
import it.unibo.qactors.action.AsynchActionResult;
import it.unibo.qactors.action.IActorAction;
import it.unibo.qactors.action.IActorAction.ActionExecMode;
import it.unibo.qactors.platform.EventItem;
import it.unibo.qactors.platform.LocalTime;


public class QActorActionUtils {
	protected QActor actor;
	protected QActorPlanUtils planUtils;
	protected IOutputEnvView outEnvView;

//	protected IEventItem currentEvent = null;
//	protected IEventItem currentExternalEvent = null; // see executeActionAsFSM


	public QActorActionUtils(QActor actor, IOutputEnvView outEnvView) {
		this.actor		= actor;
		this.outEnvView = outEnvView;
		planUtils = new QActorPlanUtils(actor,  this, outEnvView);
	}
	
	public QActorPlanUtils getQActorPlanUtils(){
		return planUtils;
	}

	/*
	 * ----------------------------------------------------------- EXECUTION
	 * -----------------------------------------------------------
	 */
	public AsynchActionResult executeReactiveAction(IActorAction action, ActionExecMode mode, String[] evarray,
			String[] planarray) throws Exception {
//		println("	--- QActorActionUtils executeReactiveAction " + action + " mode=" + mode + " plans=" + planarray.length);
		
		//JUNE2017		
		if( actor.pengine.solve("retract(runPlanRunning).").isSuccess() ){ //WORKING runPlan
			actor.changePrologMachine();
		}		  	   
		
		if (mode == ActionExecMode.synch) action.execSynch();
		else action.execASynch();
// 		println("	--- QActorActionUtils executeReactiveAction " + action + " ends"  );
  		return evalActionResult(action, mode, evarray, planarray );
	}
	public AsynchActionResult executeReactiveReceive(IActorAction action,String[] evarray,
			String[] planarray) throws Exception {
//   		println("	--- QActorActionUtils executeReactiveReceive " + " plans=" + planarray[0]   );
		String res = action.execSynch();
		QActorMessage msg = new QActorMessage(res);
//  		println("	--- QActorActionUtils executeReactiveReceive " + res + " " + msg.getDefaultRep() );
		actor.currentMessage = msg;
    	return evalSynchActionResult(action,evarray, planarray );
	}

	protected AsynchActionResult evalActionResult(
			IActorAction action, ActionExecMode mode, String[] evarray, String[] planarray) throws Exception {
		/*
		 * If the action has been activated in asynch way, it is launched with a 'going' result (if checked)
		 * Thus executeReactiveAction returns the control.
		 * The action result can be perceived by the termination event handlers only 
		 */		
		if(mode==ActionExecMode.asynch){
			String res = action.getResultRep();
			return new AsynchActionResult(action, -1, 
					QActorPlanUtils.normalEnd, QActorPlanUtils.continueWork,
					res, action.getInterruptEvent());
		}
		else return evalSynchActionResult(action,  evarray,  planarray);
 	}
	
	protected AsynchActionResult evalSynchActionResult(
			IActorAction action, String[] evarray, String[] planarray) throws Exception {
//  		println("	--- QActorActionUtils evalSynchActionResult " + action.getResultRep() + " plans=" + planarray.length  );
  		long timeRemained;
		if( action.getExecTime() <= 0 ) timeRemained = action.getMaxDuration();
		else timeRemained = action.getMaxDuration()-action.getExecTime();
		if(timeRemained<=0 ){
			timeRemained=0;
		}		
//		println("	--- QActorActionUtils evalActionResult interrupt=" + interrupt +" timeRemained=" +  
//						timeRemained + " " + action.getMaxDuration() +"/"+action.getExecTime());
// 		println("		--- QActorActionUtils " + planarray.length + " evalActionResult "  );
		// println(" evalSynchActionResult eva=" + eva.getEventId() );
		/*
		 * The event that interrupts the action carries the 'local time'
		 * We create another interrupt event ev1 with the action execution time
		 */
		IEventItem interruptEv = action.getInterruptEvent();		 
		IEventItem ev1 = null;
		if( interruptEv != null ){ 
			ILocalTime actionExecTime = new LocalTime(action.getExecTime());
			ev1 = new EventItem( interruptEv.getEventId(), interruptEv.getMsg(), actionExecTime , interruptEv.getSubj());
		}
		if( ev1 != null) actor.currentEvent = ev1; //interrupt; // fundamental for senseevent
//		println("	--- QActorActionUtils evalActionResult planarray=" + planarray.length );
		if( planarray.length == 0 ){
			AsynchActionResult aar = afterAction(action, "", ev1, timeRemained );
			return aar;			
		}
		String interruptOrMessageId = null;
		interruptOrMessageId = (interruptEv==null) ? interruptOrMessageId : interruptEv.getEventId();
		String nextPlan = planUtils.getNextPlanTodo(interruptOrMessageId, evarray, planarray);
//  		println("	--- QActorActionUtils interruptOrMessageId=" + interruptOrMessageId + " nextPlan=" + nextPlan);
		AsynchActionResult aarr = afterAction(action, nextPlan, ev1, timeRemained );
		return aarr;
		
	}

	protected AsynchActionResult afterAction(IActorAction action, String nextPlan, IEventItem ev, long timeNotDone)
			throws Exception {
		boolean interrupted = timeNotDone==0 || ev != null;
//		println("	--- QActorActionUtils afterAction action=" + action + " nextPlan=" + nextPlan + " timeNotDone=" + timeNotDone+ " ev="+ev);
		if (ev != null && nextPlan != null && nextPlan.length() > 0) {
			if(! nextPlan.equals("continue") ) { // There is a nextPlan
				return planUtils.execOtherPlan(action, nextPlan, timeNotDone, ev);
			} else { // continue
				return new AsynchActionResult(action, timeNotDone, 
						interrupted?QActorPlanUtils.interrupted:QActorPlanUtils.normalEnd,
						QActorPlanUtils.continueWork,
						 action.getResultRep()  , ev);
			}
		} else { // There is NO nextPlan
 			String res = action.getResultRep();
//			long tr    = action.getMaxDuration() - action.getExecTime();
// 	println("		--- QActorActionUtils afterAction res=" + res + " tr=" + tr + " timeNotDone=" + timeNotDone);
			//boolean susp = ( ev != null ) ? QActorPlanUtils.interrupted : QActorPlanUtils.normalEnd;
 			return new AsynchActionResult(action, //tr, 
 					timeNotDone,
 					interrupted?QActorPlanUtils.interrupted:QActorPlanUtils.normalEnd,
					QActorPlanUtils.continueWork,
 					res,ev);
		}
	}

	/*
	 * =========================================================================
	 * =============== executeActionAsFSM creates a TaskActionFSMExecutor that
	 * implements a FSM that executes the given action by reacting to
	 * alarmEvents
	 * =========================================================================
	 * ===============
	 */

//	public AsynchActionResult executeActionAsFSM(IActorAction action, String alarmEvents,
//			String recoveryPlans, ActionExecMode mode) throws Exception {
// println( "	+++++++++++++++ QActorActionUtils   executeActionAsFSM " + action.getClass().getName() + " mode=" + mode);
//		// //action.getActionRep()
//		TaskActionFSMExecutoResult res = new ActorActionExecutorFSM(action, outEnvView, actor)
//				.executeActionFSM(alarmEvents, recoveryPlans, mode);
//		// println( "QActor "+ getName() + " executeActionAsFSM res event=" +
//		// res.getEventItem().getDefaultRep() );
//		// Synchronous execution
//		if (mode == ActionExecMode.synch) {
//			String nextPlan = res.getPlanTodo();
//			currentEvent = res.getEventItem();
//			if (!currentEvent.getEventId().startsWith(QActorUtils.locEvPrefix))
//				currentExternalEvent = currentEvent;
//			// println( "QActor "+ getName() + " executeActionAsFSM nextPlan=" +
//			// nextPlan + " recoveryPlans=" + recoveryPlans);
//			if (nextPlan == null && recoveryPlans.indexOf("continue") >= 0) { // Action
//																				// (sense)
//																				// timeout
//				return new AsynchActionResult(action, res.getMoveTimeNotDone(), planUtils.normalEnd,
//						planUtils.continueWork, action.getResultRep() + ",toCheck", res.getEventItem());
//			}
//			if (nextPlan != null && nextPlan.length() > 0) {
//				if (!nextPlan.equals("continue")) {
//					// println( "QActor "+ getName() + " executeActionAsFSM A
//					// nextPlan=" + nextPlan + " " + res.getMoveTimeNotDone() );
//					return planUtils.execOtherPlan(action, nextPlan, res.getMoveTimeNotDone(), currentEvent);
//				} else { // continue
//					// println( "QActor "+ getName() + " executeActionAsFSM B
//					// nextPlan=" + nextPlan );
//					return new AsynchActionResult(action, res.getMoveTimeNotDone(), planUtils.normalEnd,
//							planUtils.continueWork, action.getResultRep() + ",continue", res.getEventItem());
//				}
//			} else { // There is NO nextPlan
//				boolean goon = !action.isSuspended();// &&
//														// (res.getMoveTimeNotDone()
//														// > 0); //not suspended
//														// and not time elapsed
//				// println( "QActor "+ getName() + " executeActionAsFSM action="
//				// + action.getActionName() +
//				// " suspended=" + action.isSuspended() + " time=" +
//				// res.getEventItem().getTime().getTimeRep() );
//				return new AsynchActionResult(action, action.getMaxDuration() - action.getExecTime(),
//						planUtils.normalEnd, goon, "ar(" + action.getResultRep() + ",noPlan)", res.getEventItem());
//			}
//		}
//		// Asynchronous execution terminates immediately
//		else {
//			return new AsynchActionResult(action, 0, planUtils.normalEnd, planUtils.continueWork, "actionAsynchDone",
//					res.getEventItem());
//		}
//	}

	/*
	 * --------------------------------------------------- 
	 * REFLECTION
	 * ---------------------------------------------------
	 */
//	public boolean execByReflection(Class C, String methodName) {
//		Method method = null;
//		Class curClass = C;
//		println("QActor execByReflection " + methodName + " curClass=" + curClass );
//		while (method == null)
//			try {
//				if (curClass == null)
//					return false;
//				method = getByReflection(curClass, methodName);
//				if (method != null) {
//					// println("QActor execByReflection method: " +method + " in
//					// class " + curClass.getName());
//					Object[] callargs = null;
//					Object returnValue = method.invoke(this, callargs);
//					// println("QActor execByReflection " + methodName + "
//					// returnValue: " +returnValue );
//					Boolean goon = (Boolean) returnValue;
//					return goon;
//				} else {
//					// println("QActor execByReflection " + methodName + " not
//					// found in " +curClass.getName() );
//					curClass = curClass.getSuperclass();
//				}
//			} catch (Exception e) {
//				// If the method does not exist or does not return a boolean
//				// return false
//				println("QActor execByReflection " + methodName + "  WARNING: " + e.getMessage());
//				// break;
//			}
//		return false;
//	}

//	public Method getByReflection(Class C, String methodName) {
//		try {
//			Class noparams[] = {};
//			Method method = C.getDeclaredMethod(methodName, noparams);
//			return method;
//		} catch (Exception e) {
//			// println("QActor getByReflection ERROR: " + e.getMessage() );
//			return null;
//		}
//	}
//
//	public boolean execApplicationActionByReflection(Class C, String methodName, String arg1, String arg2) {
//		Method method = null;
//		Class curClass = C;
//		while (method == null)
//			try {
//				if (curClass == null)
//					return false;
//				method = getActionByReflection(curClass, methodName);
//				if (method != null) {
//					// println("QActor execByReflection method: " +method + " in
//					// class " + curClass.getName());
//					Object[] callargs = new Object[] { arg1, arg2 };
//					Object returnValue = method.invoke(this, callargs);
//					// println("QActor execByReflection returnValue: "
//					// +returnValue );
//					Boolean goon = (Boolean) returnValue;
//					return goon;
//				} else {
//					// println("QActor execByReflection " + methodName + " not
//					// found in " +curClass.getName() );
//					curClass = curClass.getSuperclass();
//				}
//			} catch (Exception e) {
//				// If the method does not exist or does not return a boolean
//				// return false
//				println("QActor execApplicationActionByReflection " + methodName + "  ERROR: " + e.getMessage());
//				// break;
//			}
//		return false;
//	}
//
//	public Method getActionByReflection(Class C, String methodName) {
//		try {
//			Class twoparams[] = { String.class, String.class };
//			Method method = C.getDeclaredMethod(methodName, twoparams);
//			return method;
//		} catch (Exception e) {
//			// println("QActor getByReflection ERROR: " + e.getMessage() );
//			return null;
//		}
//	}
	
	
	/*
	 * New operation to solve a sentence (originated by the Talk project)	 	
	 */
 	public AsynchActionResult solveSentence( String sentence ) throws Exception{
	 	System.out.println("QActorAction solveSentence " + sentence);
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
	 		System.out.println("QActorAction solveSentence solveGoal "+ goal + " planFailStr=" + planFailStr + " duration=" + duration);
 	 		AsynchActionResult aar = actor.solveGoal( ""+goal, duration,  ev,  pl);
//	 		AsynchActionResult aar = actor.solveGoal( ""+goal );
		if( aar.getResult().equals("failure")){
			System.out.println("QActorAction solveSentence solveGoal "+ goal + " failure" );
    		if( ! planUtils.switchToPlan(planFailStr).getGoon() ){  
    		}else if( ! aar.getGoon() ) { }
		}else{
//	 		println("QActor solveSentence result="+aar.getResult());
	 			actor.pengine.solve("setAnswer("+aar.getResult()+").");
	 			//Show the result in the user GUI
	 			//pengine.solve("actorPrintln("+aar.getResult()+").");
		}
 		return aar;
 	}	

	/*
	 * ----------------------------------------------------------- 
	 * UTILS
	 * -----------------------------------------------------------
	 */

	protected void println(String msg) {
		outEnvView.addOutput(msg);
	}
/*
 * 
 */
	public static String askMessageWaiting(QActor a, String dest, String askmsg, int tout) throws Exception{
		return a.askMessageWaiting(  dest,   askmsg,   tout);
 	}
	
 
}
