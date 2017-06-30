package it.unibo.qactors.akka;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import alice.tuprolog.Prolog;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.Struct;
import alice.tuprolog.Term;
import alice.tuprolog.Var;
import it.unibo.contactEvent.interfaces.IEventItem;
import it.unibo.contactEvent.interfaces.ILocalTime;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactors.QActorContext;
import it.unibo.qactors.QActorMessage;
import it.unibo.qactors.QActorUtils;
import it.unibo.qactors.action.ActionDummyTimed;
import it.unibo.qactors.action.ActionReceiveTimed;
import it.unibo.qactors.action.AsynchActionResult;
import it.unibo.qactors.action.IActorAction;
import it.unibo.qactors.action.IMsgQueue;
import it.unibo.qactors.platform.EventItem;
import it.unibo.qactors.platform.LocalTime;
//import it.unibo.qactors.action.PlanActionDescr;
import it.unibo.qactors.action.IActorAction.ActionExecMode;

public class QActorPlanUtils {

	public static final boolean continueWork = true;
	public static final boolean suspendWork  = false;
	public static final boolean interrupted  = true;
	public static final boolean normalEnd    = false;

	protected Prolog pengine;
	protected QActor actor;
	protected QActorContext myCtx;
	protected IOutputEnvView outEnvView;
	protected QActorActionUtils actionUtils;
	// protected int actor.nPlanIter = 0;
	// protected Stack<String> planStack = new Stack<String>();
	// protected Stack<Integer> iterStack = new Stack<Integer>();

//	protected Vector<PlanActionDescr> curPlan;
 	protected QActorMessage currentMessage = null;
	protected IEventItem currentExternalEvent = null; // see executeActionAsFSM
	protected AsynchActionResult lastActionResult;

	/*
	 * PACKAGE VISIBILITY
	 */
	// public String actor.curPlanInExec = "dummy";
	// protected Hashtable<String, Vector<PlanActionDescr> > planTable;

	public QActorPlanUtils(QActor actor, QActorActionUtils actionUtils,IOutputEnvView outEnvView) {
		this.actor = actor;
		this.outEnvView = outEnvView;
		this.actionUtils = actionUtils;
		pengine = actor.getPrologEngine();
		myCtx   = actor.getQActorContext();
	}

	protected void println(String msg) {
		outEnvView.addOutput(msg);
	}

	protected String getNextPlanTodo(String eva, String[] events, String[] plans) {
		for (int i = 0; i < events.length; i++) {
			if (events[i].equals(eva))
				return plans[i];
		}
		return null;
	}

//	public void buildPlanTable(String planFilePath) {
//		try {
//			println("		### QactorPlanUtils buildPlanTable " + planFilePath);
//			if (planFilePath == null)
//				return;
//			actor.planTable = new Hashtable<String, Vector<PlanActionDescr>>();
//			InputStreamReader isr = openPlanFile(planFilePath);
//			BufferedReader bufferedIn = new BufferedReader(isr);
//			String lineStr = bufferedIn.readLine().trim();
//			while (lineStr != null) {
//				if (lineStr.length() > 0) {
//					if (lineStr.startsWith("plan(")) {
//						PlanActionDescr pa = PlanActionDescr
//								.createDescr(lineStr);
//						String planOfAction = pa.getPlanName();
//						insertInPlanTable(planOfAction, pa);
//					}
//				}
//				lineStr = bufferedIn.readLine();
//			}
//			bufferedIn.close();
//			// System.out.println("buildPlanTable done " + planFilePath);
//		} catch (Exception e) {
//			println("ERROR " + e.getMessage());
//			// bufferedIn.close();
//			// throw e;
//		}
//	}

	protected InputStreamReader openPlanFile(String planFilePath)
			throws Exception {
		// println(" --- QActorPlanUtils openPlanFile " + planFilePath);
//		actor.planTable = new Hashtable<String, Vector<PlanActionDescr>>();
		InputStreamReader isr = new InputStreamReader(new FileInputStream(
				planFilePath));
		return isr;
	}

//	protected void insertInPlanTable(String planOfAction, PlanActionDescr pa) {
//		Vector<PlanActionDescr> plan = actor.planTable.get(planOfAction);
//		if (plan == null) {
//			plan = new Vector<PlanActionDescr>();
//			actor.planTable.put(planOfAction, plan);
//		}
//		System.out.println("		### QActorPlanUtils added pa=" + pa.getDefStringRep() + " in " + planOfAction);
//		plan.add(pa);
//	}

	public AsynchActionResult execOtherPlan(IActorAction action,
			String nextPlan, long moveTimeNotDone, IEventItem eva)
			throws Exception {
		// throw new Exception("QActorPlanUtils execOtherPlan not implemented");
		if (nextPlan != null && nextPlan.length() > 0) {
			// println( " --- QActorPlanUtils execute via reflection: " +
			// nextPlan);
			Class noparams[] = {};
			boolean goon = true;
			if (nextPlan.equals("dummyPlan") || nextPlan.equals("noPlan") ){
//					|| actor.planTable == null) {
//				println("	--- QActorPlanUtils executes by reflection  "+ nextPlan + " moveTimeNotDone=" + moveTimeNotDone);
				actor.planStack.push(actor.curPlanInExec);
				actor.iterStack.push(actor.nPlanIter);
				actor.nPlanIter = 0;
				goon = actor.execByReflection(actor.getClass(), nextPlan);
				actor.curPlanInExec = actor.planStack.pop();
				actor.nPlanIter = actor.iterStack.pop();
			} 
			else {
//				println("	--- QActorPlanUtils execute via reflection2: " + nextPlan + 
//						" moveTimeNotDone=" + moveTimeNotDone + " " + eva.getDefaultRep());
				AsynchActionResult aar = switchToPlan(nextPlan);
				if (aar.getGoon()) { // all ok
					return new AsynchActionResult(action, moveTimeNotDone,
							normalEnd, continueWork, "afterPlanContinue", eva);
				} else
					return aar;
			}// execByReflection done
			if (goon == suspendWork) {
				return new AsynchActionResult(action, -1, interrupted,
						suspendWork, "afterPlanSuspend", eva);
			} else {
				return new AsynchActionResult(action, moveTimeNotDone,
						interrupted, continueWork, "", eva);
			}
		} else { // nextPlan.length()==0
			println(" --- --- QActorPlanUtils no next plan");
			return new AsynchActionResult(action, moveTimeNotDone, normalEnd,
					continueWork, "", eva);
		}
	}

	public AsynchActionResult switchToPlan(String planName) throws Exception {
		// println("		### QActorPlanUtils switchToPlan " + planName +
		// " planStack=" + planStack );
		if (planName.equals("dummyPlan") || planName.equals("continue"))
			return new AsynchActionResult(null, 0, normalEnd, continueWork, "",
					null);
		//actor.planStack.push(actor.curPlanInExec);
		actor.planStack.push(planName);
		actor.iterStack.push(actor.nPlanIter);
		actor.nPlanIter = 0; 
		actor.curPlanInExec = planName;

		// CODE GEN
		boolean goon = actor.execByReflection(actor.getClass(), planName);
		if (actor.planStack.size() > 0) {
			actor.curPlanInExec = actor.planStack.pop();
			actor.nPlanIter     = actor.iterStack.pop();
		}
//		 println("	--- QActorPlanUtils switchToPlan " + planName + " goon=" + goon + " result=" + lastActionResult);
		if (goon == suspendWork) {
			return new AsynchActionResult(null, -1, interrupted, suspendWork, "", null);
		} else {
			return new AsynchActionResult(null, 0, normalEnd, continueWork, "", null);
		}
	}

	public AsynchActionResult repeatPlan(int nPlanIter, int limit) throws Exception {
//		 println("		### QActorPlanUtils "+ actor.getName() + " repeatPlan " 
//				+ actor.curPlanInExec +"  nPlanIter=" +  nPlanIter + "/" + limit );
		if ( nPlanIter <= limit || limit == 0) {
 //			if (actor.planTable != null) { // interpreted => RECURSION
//				AsynchActionResult aar = executeThePlan(actor.curPlanInExec);
//				// println("		### QActorPlanUtils repeatPlan " +
//				// actor.curPlanInExec + " result " + aar);
//				return aar;
//				// CODE GEN: we do nothing since the while(true)
//			} else 
			{ // planTable == null
			// println("		### QActorPlanUtils repeatPlan actor.nPlanIter=" +
			// actor.nPlanIter + "/" + limit);
				return new AsynchActionResult(null, 0, normalEnd, continueWork,
						"", null);
			}
		}
		return new AsynchActionResult(null, 0, normalEnd, suspendWork, "", null);
	}

	protected AsynchActionResult resumeLastPlan() throws Exception {
		return new AsynchActionResult(null, 0, normalEnd, continueWork, "",
				null);
	}

	protected AsynchActionResult interruptPlan() throws Exception {
		// println("		### QActorPlanUtils interruptPlan " + actor.curPlanInExec);
		return new AsynchActionResult(null, 0, normalEnd, suspendWork, "", null);
	}

	protected AsynchActionResult executeThePlan(String planName)
			throws Exception {
 		 println("		### QActorPlanUtils executeThePlan " + planName  );
//		if (actor.planTable == null)
			return new AsynchActionResult(null, 0, normalEnd, continueWork, "",
					null);
//		curPlan = actor.planTable.get(planName);
//		 println("		### QActorPlanUtils executeThePlan curPlan=" + curPlan  );
//		if (curPlan != null) {
//			actor.curPlanInExec = planName;
//			// TO AVOID RECURSION
//			Iterator<PlanActionDescr> it = curPlan.iterator();
//			while (it.hasNext()) {
//				PlanActionDescr action = it.next();
//				lastActionResult = executePlanAction(action);
//				// println("		### QActorPlanUtils executeThePlan " +
//				// action.getCommand() + " lastActionResult= " +
//				// lastActionResult );
//				if (!lastActionResult.getGoon()) {
//					// println("		### QActorPlanUtils executeThePlan " + planName
//					// + " " +action.getCommand() + " breaks" );
//					return lastActionResult;
//				}
//			}
//			return lastActionResult;
//		} else{
//			return new AsynchActionResult(null, 0, normalEnd, continueWork, "",
//					null); // no plan => continue your work (STRANGE)
//		}
	}

	/*
	 * The execution based on an interpreter takes advantage form a
	 * PlanActionDescr In a code-based approach we do not have any plan
	 * description but only the code
	 */
//	public AsynchActionResult executePlanAction(PlanActionDescr pa)
//			throws Exception {
//		 println("		### QActorPlanUtils executePlanAction " +  pa.getDefStringRep() );
//		/*
//		 * 1) Guard evaluation. If the guard is true: 2) Guard variable bindings
//		 * 3) Action execution
//		 */
//		Struct gt = (Struct) Term.createTerm(pa.getGuard()); // guard(volatile,domove(M))
//		String guardType = gt.getArg(0).toString();
//		String guardBody = gt.getArg(1).toString();
//		List<Var> guardVars = QActorUtils.evalTheGuard(actor, guardBody,
//				guardType);
//		// println("executePlanAction guard=" + guardBody + " of type=" +
//		// guardType + " guardVars=" + guardVars );
//		if (guardVars == null)
//			return new AsynchActionResult(null, 0, normalEnd, continueWork, "",
//					null);
//		bindVars(pa, guardVars);
//		// println("executePlanAction guard var list " + guardVars);
//		AsynchActionResult aar = executeAction(pa);
//		// RESTORE original args
//		pa.resetArgs();
//		return aar;
//	}

//	protected void bindVars(PlanActionDescr pa, List<Var> guardVars) {
//		Iterator<Var> it = guardVars.iterator();
//		while (it.hasNext()) {
//			Var v = it.next();
//			String varName = v.getOriginalName();
//			String varValue = v.getTerm().toString();
//			// println("### QActorPlanUtils bindVars varOriginaleName=" + varName
//			// + " varValue=" + varValue);
//			// SUBSTITUTE
//			pa.setInDuration(varName, varValue);
//			pa.setInCommand(varName, varValue);
//			pa.setInArgs(varName, varValue);
//		}
//	}

	/*
	 * ------------------------------------------- 
	 * Execution of basic actions
	 * -------------------------------------------
	 */
//	public AsynchActionResult executeAction(PlanActionDescr pa)
//			throws Exception {
//		// println("		### QActorPlanUtils executeAction  " + pa.getDefStringRep()
//		// );
//		String paType = pa.getType().toString();
//		String paCmd = pa.getCommand();
//		String paArgs = pa.getArgs();
//
//		// println("		&&& executeAction paType=" + paType + " paCmd="+ paCmd +
//		// " paArgs=" + paArgs ) ;
//		try {
//			if (paType.equals("solve")) {
//				// println("		&&& solve " + paCmd + " paArgs=" + paArgs ) ;
//				int actionMaxTime = Integer.parseInt(pa.getDuration());
//				lastActionResult = actor.solveGoal(paCmd, actionMaxTime,
//						pa.getEvents(), pa.getPlans());
//				// println("		&&& solve lastActionResult= " +
//				// lastActionResult.getResult() + " plans=" + paArgs) ;
//				if (lastActionResult.getResult().equals("failure")
//						&& paArgs.length() > 0) {
//					lastActionResult = switchToPlan(paArgs);
//				}
//				return lastActionResult;
//			}
//
//			else if (paType.equals("application")) {
//				// println("		### QActorPlanUtils execute application " + paCmd +
//				// " args " + paArgs.length() +" in " + getClass().getName() );
//				int actionMaxTime = Integer.parseInt(pa.getDuration());
//				String arg1, arg2;
//				if (paArgs.length() == 0) {
//					arg1 = "";
//					arg2 = "";
//				} else {
//					// println("		### QActorPlanUtils execute application " +
//					// paCmd + " args " + paArgs );
//					Struct targs = (Struct) Term.createTerm(paArgs);
//					arg1 = targs.getArg(0).toString();
//					arg2 = targs.getArg(1).toString();
//					// println("		### QActorPlanUtils execute application " +
//					// paCmd + " arg1=" + arg1 + " arg2=" + arg2 );
//				}
//				if (actionMaxTime == 0) {
//					boolean b = actor.execApplicationActionByReflection(
//							getClass(), paCmd, arg1, arg2);
//					if (b)
//						lastActionResult = new AsynchActionResult(null, 0,
//								normalEnd, continueWork, "", null);
//					else
//						lastActionResult = new AsynchActionResult(null, 0,
//								normalEnd, suspendWork, "", null);
//					return lastActionResult;
//				} else { // action with time a perhaps react
//							// TODO REACT akka
//							// lastActionResult =
//							// actionUtils.executeActionAsFSM(
//					// new it.unibo.qactors.action.ActionApplication(
//					// actor.getQActorContext(), new String[]{}, outEnvView,
//					// actionMaxTime , actor,
//					// paCmd, arg1,arg2 ), pa.getEvents(), pa.getPlans(),
//					// ActionExecMode.synch );
//					return lastActionResult;
//				}
//			} else if (paType.equals("forward")) {
//				// args('msg(mSGID,MSG)')
//				Struct st = (Struct) Term.createTerm(paArgs);
//				String msgId = st.getArg(0).toString();
//				String msg = st.getArg(1).toString();
//				if (paCmd.equals("replyToCaller"))
//					replyToCaller(msgId, msg);
//				else
//					forward(msgId, paCmd, msg);
//				lastActionResult = new AsynchActionResult(null, 0, normalEnd,
//						continueWork, "", null);
//				// println("		### QActorPlanUtils forward RESULT= " +
//				// lastActionResult );
//				return lastActionResult;
//			} else if (paType.equals("emit")) {
//				// println("		### QActorPlanUtils execute emit " + paCmd + " in "
//				// + this.getName() );
//				actor.raiseEvent(paCmd, paArgs);
//				lastActionResult = new AsynchActionResult(null, 0, normalEnd,
//						continueWork, "", null);
//				return lastActionResult;
//			} else if (paType.equals("basic")) {
//				if (paCmd.equals("print")) {
//					println(paArgs.replace("'", ""));
//				} else if (paCmd.equals("printCurrentEvent")) { // internal
//																// events are
//																// ignored
//					if (paArgs.equals("memo"))
//						actor.printCurrentEvent(true);
//					else
//						actor.printCurrentEvent(false);
//				} else if (paCmd.equals("printCurrentMessage")) {
//					if (paArgs.equals("memo"))
//						actor.printCurrentMessage(true);
//					else
//						actor.printCurrentMessage(false);
//				} else if (paCmd.equals("memoCurrentEvent")) { // internal
//																// events are
//																// ignored
//					actor.memoCurrentEvent(true);
//				} else if (paCmd.equals("memoCurrentMessage")) {
//					actor.memoCurrentMessage(true);
//				} else if (paCmd.equals("sound")) {
//					// println("		### QActorPlanUtils  sound " + paArgs ) ;
//					int time = Integer.parseInt(pa.getDuration());
//					// args(answerEvent, fileName )
//					Struct argsT = (Struct) Term.createTerm(paArgs);
//					// String answerEvent =
//					// argsT.getArg(0).toString().replace("'", ""); //TODO
//					// REMOVE
//					String fileName = argsT.getArg(1).toString()
//							.replace("'", "");
//					String terminationEvId = QActorUtils
//							.getNewName(IActorAction.endBuiltinEvent);
//					lastActionResult = actor.playSound(fileName,
//							ActionExecMode.synch, terminationEvId, time,
//							pa.getEvents(), pa.getPlans());
//					// println("		### QActorPlanUtils sound RESULT= " +
//					// lastActionResult );
//					return lastActionResult;
//				} else if (paCmd.equals("endplan")) {
//					println(paArgs);
//					lastActionResult = new AsynchActionResult(null, 0,
//							interrupted, suspendWork, "", null);
//					return lastActionResult;
//				} else if (paCmd.equals("switchplan")) {
//					return switchToPlan(paArgs);
//				} else if (paCmd.equals("repeatplan")) {
//					int limit = Integer.parseInt(paArgs);
//					actor.nPlanIter = actor.nPlanIter + 1;
//					lastActionResult = repeatPlan(limit);
//					// println("		### QActorPlanUtils repeatPlan RESULT= " +
//					// lastActionResult );
//					return lastActionResult;
//				} else if (paCmd.equals("resumeplan")) {
//					lastActionResult = this.resumeLastPlan();
//					// println("		### QActorPlanUtils resumeLastPlan RESULT= " +
//					// lastActionResult );
//					return lastActionResult;
//				} else if (paCmd.equals("interruptplan")) {
//					lastActionResult = this.interruptPlan();
//					// println("		### QActorPlanUtils interruptplan RESULT= " +
//					// lastActionResult );
//					return lastActionResult;
//				} else if (paCmd.equals("addrule")) {
//					actor.addRule(paArgs);
//				} else if (paCmd.equals("removerule")) {
//					actor.removeRule(paArgs);
//				} else if (paCmd.equals("receiveMsg")) {
//					int actionMaxTime = Integer.parseInt(pa.getDuration());
//					lastActionResult = receiveAMsg(actor.mysupport,
//							actionMaxTime);
//					return lastActionResult;
//				} else if (paCmd.equals("receiveTheMsg")) {
//					// println("		### QActorPlanUtils execute receiveTheMsg  " +
//					// pa.getDefStringRep() );
//					int time = Integer.parseInt(pa.getDuration());
//					// println(" +++ QActorPlanUtils receiveTheMsg " + paArgs );
//					lastActionResult = receiveMsg(actor.mysupport, paArgs,
//							time, pa.getEvents(), pa.getPlans());
//					// receiveMsg SET currentMessage using
//					// msg(none,none,none,none,none,0) if there is no msg
//					return lastActionResult;
//				} else if (paCmd.equals("msgselect")) {
//					lastActionResult = receiveMsgAndSwitch(actor.mysupport,
//							paArgs, pa.getEvents(), pa.getPlans(),
//							Integer.parseInt(pa.getDuration()));
//					return lastActionResult;
//				} else if (paCmd.equals("msgswitch")) {
//					Struct st = (Struct) Term.createTerm(paArgs);
//					String msgId = st.getArg(0).toString();
//					if (currentMessage.msgId().equals(msgId)) {
//						Term contentList = st.getArg(1); // should be a list of
//															// strings
//						// println("msgswitch contentList= " + contentList );
//						Term planList = st.getArg(2);
//						lastActionResult = msgswitch(contentList, planList);
//						return lastActionResult;
//					}// else println("msgswitch  " + currentMessage.msgId() );
//				} else if (paCmd.equals("eventswitch")) {
//					Struct st = (Struct) Term.createTerm(paArgs);
//					String eventId = st.getArg(0).toString();
//					// println("eventswitch " + eventId + " " +
//					// currentExternalEvent.getEventId());
//					if (currentExternalEvent != null
//							&& currentExternalEvent.getEventId()
//									.equals(eventId)) {
//						Term contentList = st.getArg(1); // should be a list of
//															// strings
//						// println("eventwitch contentList= " + contentList );
//						Term planList = st.getArg(2);
//						lastActionResult = eventswitch(contentList, planList);
//						return lastActionResult;
//					}
//				} else if (paCmd.equals("senseEvent")) {
//					Struct st = (Struct) Term.createTerm(paArgs);
//					String eventsListStr = st.getArg(0).toString().trim();
//					String events = eventsListStr.substring(1,
//							eventsListStr.length() - 1); // remove []
//					String planListStr = st.getArg(1).toString().trim();
//					String plans = planListStr.substring(1,
//							planListStr.length() - 1); // remove []
//					// println("senseEvent events=" + events + " plans="+plans +
//					// " time= " + pa.getDuration());
//					int tout = Integer.parseInt(pa.getDuration());
//					AsynchActionResult aar = senseEvents(tout, events, plans,
//							pa.getEvents(), pa.getPlans(), ActionExecMode.synch);
//					return aar;
//				} else if (paCmd.equals("delay")) {
//					String events = pa.getEvents();
//					String plans = pa.getPlans();
//					// println("		### QActorPlanUtils delay events=" + events +
//					// " plans="+plans + " time= " + pa.getDuration() );
//					AsynchActionResult aar = actor.delayReactive(
//							Integer.parseInt(pa.getDuration()), events, plans);
//					return aar;
//				}
//			}// basic
//			lastActionResult = new AsynchActionResult(null, 0, normalEnd,
//					continueWork, "", null);
//			// println("		### QActorPlanUtils " + paCmd + " RESULT= " +
//			// lastActionResult );
//			return lastActionResult;
//		} catch (Exception e) {
//			println("		### QActorPlanUtils executeAction ... " + paCmd
//					+ " ERROR " + e.getMessage());
//			e.printStackTrace();
//			throw e;
//		}
//	}

	/*
 *   		
 */
	public AsynchActionResult senseEvents(int tout, String events,
			String plans, String alarmEvents, String recoveryPlans,
			ActionExecMode mode) throws Exception {
		String mergedEvents = (alarmEvents.length() > 0) ? events + ","
				+ alarmEvents : events;
		String mergedPlans = (recoveryPlans.length() > 0) ? plans + ","
				+ recoveryPlans : plans;
//  		println(" --- QActorPlanUtils senseEvents mergedEvents=" +  mergedEvents + "  plans=" + mergedPlans );
		String name = QActorUtils.getNewName("dummya_");
		String terminationEvId = QActorUtils.getNewName(IActorAction.endBuiltinEvent);
		String[] evarray       = QActorUtils.createArray(mergedEvents);
		String[] planarray     = QActorUtils.createArray(mergedPlans);
		IActorAction action    = new ActionDummyTimed(name, actor, myCtx, terminationEvId, evarray, outEnvView, tout);		
		AsynchActionResult aar = actionUtils.executeReactiveAction(action,mode, evarray, planarray);
// 		println("	--- QActorPlanUtils senseEvents aar event= " + aar.getEvent().getDefaultRep() );
		//SEE for explanation of ev1 QActorActionUtils.evalSynchActionResult
		IEventItem evv            = aar.getEvent();		
//		println(" --- QActorPlanUtils senseEvents evv= " + evv);
		ILocalTime actionExecTime = new LocalTime(action.getExecTime());
		IEventItem ev1            = new EventItem( evv.getEventId(), evv.getMsg(), actionExecTime , evv.getSubj());
//		println(" --- QActorPlanUtils senseEvents ev1= " + ev1.getDefaultRep() );		 
		actor.setCurrentEvent( ev1 );
		return aar;
	}

	public void forwardFromProlog(String msgId, String dest, String msg)
			throws Exception {
		dest = dest.replace("'", "");
		println(" forwardFromProlog  " + msgId + " to " + dest + " " + msg);
		actor.sendMsg(msgId, dest, QActorContext.dispatch, msg);
	}

	public void forward(String msgId, String dest, String msg)
			throws Exception {
		// println(getName() + " forward  " + msgId + " to " + dest );
		actor.sendMsg(msgId, dest, QActorContext.dispatch, msg);
	}

	public void replyToCaller(String msgId, String msg) throws Exception {
 		actor.replyToCaller(msgId, msg);
	}

	public void demand(String msgId, String dest, String msg)
			throws Exception {
		// println(getName() + " demand a request" );
		actor.sendMsg(msgId, dest, QActorContext.request, msg);
	}

	public AsynchActionResult msgswitch(Term contentList, Term planList)
			throws Exception {
		// println("		### QActorPlanUtils msgswitch " + contentList + " " +
		// planList + " currentMessage=" + this.currentMessage.getDefaultRep());
		String planTodo = msgContentToPlan(currentMessage.getDefaultRep(),
				contentList, planList);
		return switchToPlan(planTodo);
	}

	protected AsynchActionResult eventswitch(Term contentList, Term planList)
			throws Exception {
		// println("		### QActorPlanUtils eventswitch " + contentList + " " +
		// planList + " currentEvent=" + this.currentEvent.getPrologRep());
		String planTodo = msgContentToPlan(
				"event(" + currentExternalEvent.getMsg() + ")", contentList,
				planList);
		return switchToPlan(planTodo);
	}

	protected String msgContentToPlan(String msg, Term tmsgs, Term tplans)
			throws Exception {
		String goal = "msgContentToPlan(" + msg + " , " + tmsgs + "," + tplans
				+ ", RES)";
		// println("		### QActorPlanUtils goal " + goal );
		SolveInfo sol = actor.getQActorContext().getEngine().solve(goal + "."); // msgContentToPlan
																				// is
																				// defined
																				// in
																				// sysRule.pl
		if (sol.isSuccess()) {
			String planToDo = sol.getVarValue("RES").toString();
			// println("		### QActorPlanUtils planToDo " + planToDo );
			return planToDo;
		} else
			// inconsistent
			throw new Exception("receiveMsgAndSwitch inconsistent ");
	}

	public String receiveAction(IMsgQueue mysupport, int maxTime)
			throws Exception {
		IActorAction action = new ActionReceiveTimed(
				actor.getName() + "action", actor, myCtx, mysupport, false,
				QActorUtils.getNewName(IActorAction.endBuiltinEvent),
				new String[] {}, outEnvView, maxTime);
		String res = action.execSynch();
		println("	(QActorPlaUtils) receiveAction res=" + res);
		//msg(interrupt,event,callable,none,receive(timeOut(30),timeRemained(0)),0)
		actor.currentMessage = new QActorMessage(res);
		return  res;
	}
	public void receiveAction(IMsgQueue mysupport, String nextPlan, int maxTime)
			throws Exception {
		IActorAction action = new ActionReceiveTimed(
				actor.getName() + "action", actor, myCtx, mysupport, false,
				QActorUtils.getNewName(IActorAction.endBuiltinEvent),
				new String[] {}, outEnvView, maxTime);
		String res = action.execSynch();
		actor.currentMessage = new QActorMessage(res);
 	}

	public AsynchActionResult receiveAMsg(IMsgQueue mysupport, int tout)
			throws Exception {
		return receiveAMsg(mysupport, tout, "", "");
	}

	public AsynchActionResult receiveAMsg(IMsgQueue mysupport, int tout,
			String events, String plans) throws Exception {
		String[] evarray    = QActorUtils.createArray(events);
		String[] planarray  = QActorUtils.createArray(plans);
		IActorAction action = new ActionReceiveTimed(
				actor.getName() + "action", actor, myCtx, mysupport, false,
				QActorUtils.getNewName(IActorAction.endBuiltinEvent), evarray,
				outEnvView, tout);
		AsynchActionResult aar = actionUtils.executeReactiveReceive(action,evarray, planarray);
		return aar;
	}

	public AsynchActionResult receiveMsg(IMsgQueue mysupport, String msgId,
			String msgType, String sender, String receiver, String msg,
			String num, int tout, String events, String plans) throws Exception {
		Term t = Term.createTerm("msg( " + msgId + "," + msgType + "," + sender
				+ "," + receiver + "," + msg + "," + num + ")");
		return receiveMsg(mysupport, t.toString(), tout, events, plans);
	}

	public AsynchActionResult receiveMsg(IMsgQueue mysupport,
			String msgTermToReceive, int tout, String events, String plans)
			throws Exception {
		String[] evarray    = QActorUtils.createArray(events);
		String[] planarray  = QActorUtils.createArray(plans);
		IActorAction action = new ActionReceiveTimed(
				actor.getName() + "action", actor, myCtx, mysupport, false,
				QActorUtils.getNewName(IActorAction.endBuiltinEvent), evarray,
				outEnvView, tout);
 		AsynchActionResult aar = 
				actionUtils.executeReactiveReceive(action,evarray, planarray);
		return aar;
	}

 
	public AsynchActionResult receiveMsgAndSwitch(IMsgQueue mysupport,
			String msgselect, String events, String reactplans, int tout)
			throws Exception {
		// msgselect = msgselect( msgs, plans )
//		 println("	### receiveMsgAndSwitch  msgselect= " + msgselect + " events=" + events + " reactplans=" + reactplans );
		Struct st         = (Struct) Term.createTerm(msgselect);
		String msgListStr = st.getArg(0).toString().trim();
		String msgs       = msgListStr.substring(1, msgListStr.length() - 1); // remove[]
																		 
		String planList = st.getArg(1).toString().trim();
		String plans = planList.substring(1, planList.length() - 1); // remove
																		 
		return receiveMsgAndSwitch(mysupport, tout, msgs, plans, events, reactplans);
	}

	/*
	 * We could receive a msg not included in msgs We first look in the
	 * WorldTheory If no matching msg is found, we call receiveMsg/4 (that
	 * should fined something in the actor queue)
	 */

	public AsynchActionResult receiveMsgAndSwitch(IMsgQueue mysupport, int tout, String msgs,
			String plans, String events, String reactplans) throws Exception {
		AsynchActionResult aar = null;
		boolean msgFound = false;
//		println("		### QactorPlanUtils msgs="+ msgs + " plans=" + plans + " events=" + events + " reactplans=" + reactplans );
		/*
		 * First we look at the WordTheory
		 */
		String msg = checkInWorld(msgs);
		if (msg != null) {
			currentMessage = new QActorMessage(msg);
			return execThePlan(0, msg, msgs, plans);
		}
		/*
		 * If no msg is found in the WordTheory , we use receiveMsg/4 that looks
		 * (via ActionReceiveAsynch) at the WorldThery but does not find any
		 * wanted nmsg!!!
		 */
		while (!msgFound) {
//			 println("	§§§ QactorPlanUtils receiveMsgAndSwitch  msgs= " + msgs
//			 + " plans=" + plans + " events=" + events + " reactplans=" +
//			 reactplans );
//			println("	§§§ QactorPlanUtils receiveMsgAndSwitch  receiveMsg" );
			aar = receiveMsg(mysupport, "msg( MID, MSGTYPE, SENDER,"
					+ actor.getName() + ", CONTENT, SEQNUM  )", tout, events,
					reactplans);
			// println("receiveMsgAndSwitch receiveMsg aar.getEvent=  " +
			// aar.getEvent() );
			if (aar.getInterrupted() || aar.getTimeRemained() == 0) {
				/*
				 * We must remember to restore messages previously extracted
				 * form the WordTheory
				 */
				restoreTempMsgs();
				return aar;
			}
			// Here we have received a message
			msg = aar.getResult();
//			println("		### QactorPlanUtils  receiveMsgAndSwitch  msg= " + msg );
			QActorMessage foundMsg = new QActorMessage(msg);
			actor.currentMessage = foundMsg; //TO CHECK
			String curMsgId = foundMsg.msgId();
//			println("		### QActorPlanUtils receiveMsgAndSwitch received  " + curMsgId);
			if (!msgs.contains(curMsgId)) {
				/*
				 * We have found a message but it is not in the set of the
				 * expected messages, Thus, we store the msg in a temporary
				 * structure
				 */
				String mr = "tempmsg(" + msg + ")";
				// println("		### QActorPlanUtils store  " + mr);
				actor.addRule(mr);
			} else {
				currentMessage = foundMsg;
//				println("		### QActorPlanUtils has just received " + foundMsg.msgContent());
				msgFound = true;
			}
		}// while
		/*
		 * We have found an expected msg. Then we restore messages previously
		 * extracted form the WordTheory and execute the plan
		 */
		restoreTempMsgs();
		aar = execThePlan(aar.getTimeRemained(), msg, msgs, plans);//
		return aar;
	}

	protected void restoreTempMsgs() throws Exception {
		SolveInfo sol;
		while (true) {
			sol = pengine.solve("retract( tempmsg(R) ).");
			if (sol.isSuccess()) {
				Term msg = sol.getVarValue("R");
				println("		### QActorPlanUtils restoring " + msg);
				actor.addRule(msg.toString());
			} else
				break;
		}
	}

	public AsynchActionResult execThePlan(long trest, String msg, String msgs,
			String plans) throws Exception {
		String planToDo = null;
		Term tmsgs  = Term.createTerm("[" + msgs + "]");
		Term tplans = Term.createTerm("[" + plans + "]");
//		println("		### QActorPlanUtils execThePlan tmsgs  " + tmsgs + " tplans=" + tplans + " msg=" + msg );
		// Check if the received message is in the message set
		planToDo = msgToPlan(msg, tmsgs, tplans);
		// println("execThePlan planToDo=" + planToDo + " msg=" + msg +
		// " trest=" + trest);
		if (planToDo.equals("dummyPlan")) {
			// throw new Exception("receiveMsgAndSwitch " );
			return null;
		}
		// We execute the plan todo
		return executeThePlanTodo(planToDo, trest);
	}

	protected String msgToPlan(String msg, Term tmsgs, Term tplans)
			throws Exception {
		String goal = "checkMsg(" + msg + " , " + tmsgs + "," + tplans
				+ ", RES)";
		SolveInfo sol = actor.getQActorContext().getEngine().solve(goal + "."); // checkMsg
																				// is
																				// defined
																				// in
																				// sysRule.pl
		if (sol.isSuccess()) {
			String planToDo = sol.getVarValue("RES").toString();
			return planToDo;
		} else
			// inconsistent
			throw new Exception("receiveMsgAndSwitch inconsistent ");
	}

	/*
	 * Check if one of the msgs is in the WorldTheory
	 */
	protected String checkInWorld(String msgs) throws Exception {
		String[] msga = msgs.split(",");
		for (int i = 0; i < msga.length; i++) {
			// println("--- checkAMsgInWorld checking " + msga[i] );
			String mt = "msg( MID, MSGTYPE, SENDER, RECEIVER, CONTENT, SEQNUM  )"
					.replace("MID", msga[i]);
			String b = checkAMsgInWorld(mt);
			// println("--- checkAMsgInWorld " + msgs + " found " + b );
			if (b != null) {
				// println("--- checkAMsgInWorld found in WorldTheory " +
				// msga[i] );
				return b;
			}
		}
		return null;
	}

	protected String checkAMsgInWorld(String msgTermStrToReceive)
			throws Exception {
		Term termToReceive = Term.createTerm(msgTermStrToReceive);
//		println("--- checkAMsgInWorld termToReceive " + termToReceive );
		SolveInfo sol = pengine.solve(msgTermStrToReceive + ".");
//		println("---  checkAMsgInWorld " + termToReceive + " sol=" + sol.isSuccess() );
		if (sol.isSuccess()) {
			// println("---  checkAMsgInWorld removing " + sol.getSolution() );
			pengine.solve("removeRule( " + sol.getSolution() + " ).");
			return sol.getSolution().toString();
		}
		return null;
	}

	protected AsynchActionResult executeThePlanTodo(String planToDo,
			long timeRemained) throws Exception {
		//Plan interpretation is done in Prolog
//		if (actor.planTable != null) {
//			AsynchActionResult aaar = executeThePlan(planToDo);
//			// TOCHECK
//			return aaar; // new
//							// AsynchActionResult(null,aar.getTimeRemained(),interrupted,continueWork,"",
//							// null);
//		}// exec by code
		boolean goon = actor.execByReflection(actor.getClass(), planToDo);
		if (goon == suspendWork) {
			return new AsynchActionResult(null, -1, interrupted, suspendWork,
					"", null);
		} else {
			return new AsynchActionResult(null, timeRemained, interrupted,
					continueWork, "", null);
		}
	}

}
