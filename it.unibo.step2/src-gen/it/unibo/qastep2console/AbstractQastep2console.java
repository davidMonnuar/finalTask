/* Generated by AN DISI Unibo */ 
package it.unibo.qastep2console;
import alice.tuprolog.Struct;
import alice.tuprolog.Term;
import it.unibo.qactors.QActorContext;
import it.unibo.qactors.ActorTerminationMessage;
import it.unibo.qactors.QActorMessage;
import it.unibo.qactors.QActorUtils;
import it.unibo.contactEvent.interfaces.IEventItem;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactors.action.ActionReceiveTimed;
import it.unibo.qactors.action.AsynchActionResult;
import it.unibo.qactors.action.IActorAction;
import it.unibo.qactors.action.IActorAction.ActionExecMode;
import it.unibo.qactors.action.IMsgQueue;
import it.unibo.qactors.akka.QActor;


//REGENERATE AKKA: QActor instead QActorPlanned
public abstract class AbstractQastep2console extends QActor { 
	protected AsynchActionResult aar = null;
	protected boolean actionResult = true;
	protected alice.tuprolog.SolveInfo sol;
	//protected IMsgQueue mysupport ;  //defined in QActor
	protected String planFilePath    = null;
	protected String terminationEvId = "default";
	protected String parg="";
	protected boolean bres=false;
	protected IActorAction  action;
	
			protected static IOutputEnvView setTheEnv(IOutputEnvView outEnvView ){
				return outEnvView;
			}
	
	
		public AbstractQastep2console(String actorId, QActorContext myCtx, IOutputEnvView outEnvView )  throws Exception{
			super(actorId, myCtx,  
			"./srcMore/it/unibo/qastep2console/WorldTheory.pl",
			setTheEnv( outEnvView )  , "init");		
			this.planFilePath = "./srcMore/it/unibo/qastep2console/plans.txt";
			//Plan interpretation is done in Prolog
			//if(planFilePath != null) planUtils.buildPlanTable(planFilePath);
	 	}
		@Override
		protected void doJob() throws Exception {
			String name  = getName().replace("_ctrl", "");
			mysupport = (IMsgQueue) QActorUtils.getQActor( name ); 
	 		initSensorSystem();
			boolean res = init();
			//println(getName() + " doJob " + res );
			QActorContext.terminateQActorSystem(this);
		} 
		/* 
		* ------------------------------------------------------------
		* PLANS
		* ------------------------------------------------------------
		*/
	    public boolean init() throws Exception{	//public to allow reflection
	    try{
	    	int nPlanIter = 0;
	    	//curPlanInExec =  "init";
	    	boolean returnValue = suspendWork;		//MARCHH2017
	    while(true){
	    	curPlanInExec =  "init";	//within while since it can be lost by switchlan
	    	nPlanIter++;
	    		temporaryStr = "\"#######Start Remote Console########\"";
	    		println( temporaryStr );  
	    		//parg = "actorOp(addCmd)"; //JUNE2017
	    		parg = "addCmd";
	    		//ex solveGoalReactive JUNE2017
	    		aar = actorOpExecuteReactive(parg,3600000,"","");
	    		//println(getName() + " plan " + curPlanInExec  +  " interrupted=" + aar.getInterrupted() + " action goon="+aar.getGoon());
	    		if( aar.getInterrupted() ){
	    			curPlanInExec   = "init";
	    			if( aar.getTimeRemained() <= 0 ) addRule("tout(actorOp,"+getName()+")");
	    			if( ! aar.getGoon() ) break;
	    		} 			
	    		else{
	    		//Store actorOpDone with the result
	    		 	String gg = "storeActorOpResult( X, Y )".replace("X", parg).replace("Y",aar.getResult() );
	    		 	//System.out.println("actorOpExecute gg=" + gg );
	    			 	 	pengine.solve(gg+".");			
	    		}
	    		
	    		if( ! planUtils.switchToPlan("sendUserCommands").getGoon() ) break;
	    break;
	    }//while
	    return returnValue;
	    }catch(Exception e){
	       //println( getName() + " plan=init WARNING:" + e.getMessage() );
	       QActorContext.terminateQActorSystem(this); 
	       return false;  
	    }
	    }
	    public boolean sendUserCommands() throws Exception{	//public to allow reflection
	    try{
	    	int nPlanIter = 0;
	    	//curPlanInExec =  "sendUserCommands";
	    	boolean returnValue = suspendWork;		//MARCHH2017
	    while(true){
	    	curPlanInExec =  "sendUserCommands";	//within while since it can be lost by switchlan
	    	nPlanIter++;
	    		//senseEvent
	    		aar = planUtils.senseEvents( 100000,"local_inputcmd","continue",
	    		"" , "",ActionExecMode.synch );
	    		if( ! aar.getGoon() || aar.getTimeRemained() <= 0 ){
	    			//println("			WARNING: sense timeout");
	    			addRule("tout(senseevent,"+getName()+")");
	    		}
	    		printCurrentEvent(false);
	    		//onEvent
	    		if( currentEvent.getEventId().equals("local_inputcmd") ){
	    		 		String parg="cmd(start)";
	    		 		/* SendDispatch */
	    		 		parg = updateVars(Term.createTerm("usercmd(X)"),  Term.createTerm("usercmd(start)"), 
	    		 			    		  					Term.createTerm(currentEvent.getMsg()), parg);
	    		 		if( parg != null ) sendMsg("cmd","qrstep2robot", QActorContext.dispatch, parg ); 
	    		 }
	    		//onEvent
	    		if( currentEvent.getEventId().equals("local_inputcmd") ){
	    		 		String parg="alarm(X)";
	    		 		/* RaiseEvent */
	    		 		parg = updateVars(Term.createTerm("usercmd(X)"),  Term.createTerm("usercmd(alarm)"), 
	    		 			    		  					Term.createTerm(currentEvent.getMsg()), parg);
	    		 		if( parg != null ) emit( "alarm", parg );
	    		 }
	    		//onEvent
	    		if( currentEvent.getEventId().equals("local_inputcmd") ){
	    		 		String parg="cmd(robotforward)";
	    		 		/* SendDispatch */
	    		 		parg = updateVars(Term.createTerm("usercmd(X)"),  Term.createTerm("usercmd(rforward)"), 
	    		 			    		  					Term.createTerm(currentEvent.getMsg()), parg);
	    		 		if( parg != null ) sendMsg("cmd","qrstep2robot", QActorContext.dispatch, parg ); 
	    		 }
	    		//onEvent
	    		if( currentEvent.getEventId().equals("local_inputcmd") ){
	    		 		String parg="cmd(robotright)";
	    		 		/* SendDispatch */
	    		 		parg = updateVars(Term.createTerm("usercmd(X)"),  Term.createTerm("usercmd(right)"), 
	    		 			    		  					Term.createTerm(currentEvent.getMsg()), parg);
	    		 		if( parg != null ) sendMsg("cmd","qrstep2robot", QActorContext.dispatch, parg ); 
	    		 }
	    		//onEvent
	    		if( currentEvent.getEventId().equals("local_inputcmd") ){
	    		 		String parg="cmd(robotleft)";
	    		 		/* SendDispatch */
	    		 		parg = updateVars(Term.createTerm("usercmd(X)"),  Term.createTerm("usercmd(left)"), 
	    		 			    		  					Term.createTerm(currentEvent.getMsg()), parg);
	    		 		if( parg != null ) sendMsg("cmd","qrstep2robot", QActorContext.dispatch, parg ); 
	    		 }
	    		//onEvent
	    		if( currentEvent.getEventId().equals("local_inputcmd") ){
	    		 		String parg="cmd(robotstop)";
	    		 		/* SendDispatch */
	    		 		parg = updateVars(Term.createTerm("usercmd(X)"),  Term.createTerm("usercmd(stop)"), 
	    		 			    		  					Term.createTerm(currentEvent.getMsg()), parg);
	    		 		if( parg != null ) sendMsg("cmd","qrstep2robot", QActorContext.dispatch, parg ); 
	    		 }
	    		//onEvent
	    		if( currentEvent.getEventId().equals("local_inputcmd") ){
	    		 		String parg="mode(user)";
	    		 		/* RaiseEvent */
	    		 		parg = updateVars(Term.createTerm("usercmd(X)"),  Term.createTerm("usercmd(user)"), 
	    		 			    		  					Term.createTerm(currentEvent.getMsg()), parg);
	    		 		if( parg != null ) emit( "switch_mode", parg );
	    		 }
	    		//onEvent
	    		if( currentEvent.getEventId().equals("local_inputcmd") ){
	    		 		String parg="mode(auto)";
	    		 		/* RaiseEvent */
	    		 		parg = updateVars(Term.createTerm("usercmd(X)"),  Term.createTerm("usercmd(autonomus)"), 
	    		 			    		  					Term.createTerm(currentEvent.getMsg()), parg);
	    		 		if( parg != null ) emit( "switch_mode", parg );
	    		 }
	    		if( planUtils.repeatPlan(nPlanIter,0).getGoon() ) continue;
	    break;
	    }//while
	    return returnValue;
	    }catch(Exception e){
	       //println( getName() + " plan=sendUserCommands WARNING:" + e.getMessage() );
	       QActorContext.terminateQActorSystem(this); 
	       return false;  
	    }
	    }
	    protected void initSensorSystem(){
	    	//doing nothing in a QActor
	    }
	    
	 
		/* 
		* ------------------------------------------------------------
		* APPLICATION ACTIONS
		* ------------------------------------------------------------
		*/
		/* 
		* ------------------------------------------------------------
		* QUEUE  
		* ------------------------------------------------------------
		*/
		    protected void getMsgFromInputQueue(){
	//	    	println( " %%%% getMsgFromInputQueue" ); 
		    	QActorMessage msg = mysupport.getMsgFromQueue(); //blocking
	//	    	println( " %%%% getMsgFromInputQueue continues with " + msg );
		    	this.currentMessage = msg;
		    }
	  }
	
