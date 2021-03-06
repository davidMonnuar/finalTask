/* Generated by AN DISI Unibo */ 
package it.unibo.qrdemo2robot;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.Struct;
import alice.tuprolog.Term;
import it.unibo.qactors.QActorContext;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactors.action.ActionReceiveTimed;
import it.unibo.qactors.action.AsynchActionResult;
import it.unibo.qactors.action.IActorAction;
import it.unibo.qactors.action.IActorAction.ActionExecMode;
import it.unibo.iot.configurator.Configurator;
import it.unibo.iot.executors.baseRobot.IBaseRobot; 
import it.unibo.iot.models.sensorData.distance.IDistanceSensorData;
import it.unibo.iot.models.sensorData.impact.IImpactSensorData;
import it.unibo.iot.models.sensorData.line.ILineSensorData;
import it.unibo.iot.models.sensorData.magnetometer.IMagnetometerSensorData;
import it.unibo.iot.sensors.ISensor; 
import it.unibo.iot.sensors.ISensorObserver;
import it.unibo.iot.sensors.distanceSensor.DistanceSensor;
import it.unibo.iot.sensors.impactSensor.ImpactSensor;
import it.unibo.iot.sensors.lineSensor.LineSensor;
import it.unibo.iot.sensors.magnetometerSensor.MagnetometerSensor;
import it.unibo.qactors.action.IMsgQueue;
import it.unibo.qactors.QActorMessage;
import it.unibo.qactors.QActorUtils;


class QaRobotActor extends it.unibo.qactor.robot.RobotActor{
	public QaRobotActor(
		String name, QActorContext ctx, String worldTheoryPath,
			IOutputEnvView outEnvView, String baserobot, String defaultPlan )  throws Exception{
		super(name, ctx, "./srcMore/it/unibo/qrdemo2robot/plans.txt", worldTheoryPath,
		outEnvView, it.unibo.qactor.robot.RobotSysKb.setRobotBase(ctx, baserobot) , defaultPlan);
	}
}

public class AbstractQrdemo2robot extends QaRobotActor { 
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


	public AbstractQrdemo2robot(String actorId, QActorContext myCtx, IOutputEnvView outEnvView ,String baserobot)  throws Exception{
		super(actorId, myCtx,  
		"./srcMore/it/unibo/qrdemo2robot/WorldTheory.pl",
		setTheEnv( outEnvView ) ,baserobot , "init");		
		this.planFilePath = "./srcMore/it/unibo/qrdemo2robot/plans.txt";
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
    		temporaryStr = "\"#######Start Robot########\"";
    		println( temporaryStr );  
    		if( ! planUtils.switchToPlan("receiveCmd").getGoon() ) break;
    break;
    }//while
    return returnValue;
    }catch(Exception e){
       //println( getName() + " plan=init WARNING:" + e.getMessage() );
       QActorContext.terminateQActorSystem(this); 
       return false;  
    }
    }
    public boolean receiveCmd() throws Exception{	//public to allow reflection
    try{
    	int nPlanIter = 0;
    	//curPlanInExec =  "receiveCmd";
    	boolean returnValue = suspendWork;		//MARCHH2017
    while(true){
    	curPlanInExec =  "receiveCmd";	//within while since it can be lost by switchlan
    	nPlanIter++;
    		if( ! checkInMsgQueue() ){
    			//ReceiveMsg
    					 aar  = planUtils.receiveMsg(mysupport,
    					 "cmd" ,"MSGTYPE", 
    					 "qademo2console",this.getName(), 
    					 "cmd(X)","MSGNUM", 20000, "" , "");	//could block
    					//println(getName() + " plan " + curPlanInExec  +  " interrupted=" + aar.getInterrupted() + " action goon="+aar.getGoon());
    					if( aar.getInterrupted() ){
    						curPlanInExec   = "receiveCmd";
    						if( aar.getTimeRemained() <= 0 ) addRule("tout(ReceiveMsg,"+getName()+")");
    						if( ! aar.getGoon() ) break;
    					} 			
    				    if( ! aar.getGoon() || aar.getTimeRemained() <= 0 ){
    				    	//println("	WARNING: receivemsg timeout " + aar.getTimeRemained());
    				    	addRule("tout(receivemsg,"+getName()+")");
    				    }
    		}
    		//onMsg
    		if( currentMessage.msgId().equals("cmd") ){
    			String parg = "";
    			/* SwitchPlan */
    			parg =  updateVars(  Term.createTerm("cmd(X)"), Term.createTerm("cmd(start)"), 
    				    		  					Term.createTerm(currentMessage.msgContent()), parg);
    				if( parg != null ){
    					 if( ! planUtils.switchToPlan("receiveSonarADistance").getGoon() ) break; 
    				}//else println("guard  fails");  //parg is null when there is no guard (onEvent)
    		}if( planUtils.repeatPlan(nPlanIter,0).getGoon() ) continue;
    break;
    }//while
    return returnValue;
    }catch(Exception e){
       //println( getName() + " plan=receiveCmd WARNING:" + e.getMessage() );
       QActorContext.terminateQActorSystem(this); 
       return false;  
    }
    }
    public boolean receiveSonarADistance() throws Exception{	//public to allow reflection
    try{
    	int nPlanIter = 0;
    	//curPlanInExec =  "receiveSonarADistance";
    	boolean returnValue = suspendWork;		//MARCHH2017
    while(true){
    	curPlanInExec =  "receiveSonarADistance";	//within while since it can be lost by switchlan
    	nPlanIter++;
    		if( ! checkInMsgQueue() ){
    			//ReceiveMsg
    					 aar  = planUtils.receiveMsg(mysupport,
    					 "sonar" ,"MSGTYPE", 
    					 "qademo2sonara",this.getName(), 
    					 "sonar(sonara,qrdemo2robot,D)","MSGNUM", 20000, "" , "");	//could block
    					//println(getName() + " plan " + curPlanInExec  +  " interrupted=" + aar.getInterrupted() + " action goon="+aar.getGoon());
    					if( aar.getInterrupted() ){
    						curPlanInExec   = "receiveSonarADistance";
    						if( aar.getTimeRemained() <= 0 ) addRule("tout(ReceiveMsg,"+getName()+")");
    						if( ! aar.getGoon() ) break;
    					} 			
    				    if( ! aar.getGoon() || aar.getTimeRemained() <= 0 ){
    				    	//println("	WARNING: receivemsg timeout " + aar.getTimeRemained());
    				    	addRule("tout(receivemsg,"+getName()+")");
    				    }
    		}
    		//onMsg
    		if( currentMessage.msgId().equals("sonar") ){
    			String parg = "distdia(F)";
    			/* Print */
    			parg =  updateVars( Term.createTerm("sonar(SONARNAME,TARGETNAME,DISTANCE)"), Term.createTerm("sonar(sonara,qrdemo2robot,F)"), 
    				    		  					Term.createTerm(currentMessage.msgContent()), parg);
    				if( parg != null ) println( parg );  
    		}//onMsg
    		if( currentMessage.msgId().equals("sonar") ){
    			String parg="distA(S)";
    			/* AddRule */
    			parg = updateVars(Term.createTerm("sonar(SONARNAME,TARGETNAME,DISTANCE)"),  Term.createTerm("sonar(sonara,qrdemo2robot,S)"), 
    				    		  					Term.createTerm(currentMessage.msgContent()), parg);
    			if( parg != null ) addRule(parg);	    		  					
    		}//onMsg
    		if( currentMessage.msgId().equals("sonar") ){
    			String parg = "";
    			/* SwitchPlan */
    			parg =  updateVars(  Term.createTerm("sonar(SONARNAME,TARGETNAME,DISTANCE)"), Term.createTerm("sonar(sonara,qrdemo2robot,D)"), 
    				    		  					Term.createTerm(currentMessage.msgContent()), parg);
    				if( parg != null ){
    					 if( ! planUtils.switchToPlan("startRobot").getGoon() ) break; 
    				}//else println("guard  fails");  //parg is null when there is no guard (onEvent)
    		}if( planUtils.repeatPlan(nPlanIter,0).getGoon() ) continue;
    break;
    }//while
    return returnValue;
    }catch(Exception e){
       //println( getName() + " plan=receiveSonarADistance WARNING:" + e.getMessage() );
       QActorContext.terminateQActorSystem(this); 
       return false;  
    }
    }
    public boolean receiveSonarBDistance() throws Exception{	//public to allow reflection
    try{
    	int nPlanIter = 0;
    	//curPlanInExec =  "receiveSonarBDistance";
    	boolean returnValue = suspendWork;		//MARCHH2017
    while(true){
    	curPlanInExec =  "receiveSonarBDistance";	//within while since it can be lost by switchlan
    	nPlanIter++;
    		if( ! checkInMsgQueue() ){
    			//ReceiveMsg
    					 aar  = planUtils.receiveMsg(mysupport,
    					 "sonar" ,"MSGTYPE", 
    					 "qademo2sonarb",this.getName(), 
    					 "sonar(sonarb,qrdemo2robot,D)","MSGNUM", 20000, "alarm,obstacle" , "stopRobot,handleObstacle");	//could block
    					//println(getName() + " plan " + curPlanInExec  +  " interrupted=" + aar.getInterrupted() + " action goon="+aar.getGoon());
    					if( aar.getInterrupted() ){
    						curPlanInExec   = "receiveSonarBDistance";
    						if( aar.getTimeRemained() <= 0 ) addRule("tout(ReceiveMsg,"+getName()+")");
    						if( ! aar.getGoon() ) break;
    					} 			
    				    if( ! aar.getGoon() || aar.getTimeRemained() <= 0 ){
    				    	//println("	WARNING: receivemsg timeout " + aar.getTimeRemained());
    				    	addRule("tout(receivemsg,"+getName()+")");
    				    }
    		}
    		//onMsg
    		if( currentMessage.msgId().equals("sonar") ){
    			String parg = "distinizialedib(F)";
    			/* Print */
    			parg =  updateVars( Term.createTerm("sonar(SONARNAME,TARGETNAME,DISTANCE)"), Term.createTerm("sonar(sonarb,qrdemo2robot,F)"), 
    				    		  					Term.createTerm(currentMessage.msgContent()), parg);
    				if( parg != null ) println( parg );  
    		}//onMsg
    		if( currentMessage.msgId().equals("sonar") ){
    			String parg="distB(F)";
    			/* PHead */
    			parg =  updateVars( Term.createTerm("sonar(SONARNAME,TARGETNAME,DISTANCE)"), Term.createTerm("sonar(sonarb,qrdemo2robot,F)"), 
    				    		  					Term.createTerm(currentMessage.msgContent()), parg);
    				if( parg != null ) {
    				    aar = QActorUtils.solveGoal(this,myCtx,pengine,parg,"",outEnvView,86400000);
    					//println(getName() + " plan " + curPlanInExec  +  " interrupted=" + aar.getInterrupted() + " action goon="+aar.getGoon());
    					if( aar.getInterrupted() ){
    						curPlanInExec   = "receiveSonarBDistance";
    						if( aar.getTimeRemained() <= 0 ) addRule("tout(demo,"+getName()+")");
    						if( ! aar.getGoon() ) break;
    					} 			
    					if( aar.getResult().equals("failure")){
    						if( ! aar.getGoon() ) break;
    					}else if( ! aar.getGoon() ) break;
    				}
    		}if( (guardVars = QActorUtils.evalTheGuard(this, " !?minore(B,A)" )) != null ){
    		temporaryStr = "lt(A,B)";
    		temporaryStr = QActorUtils.substituteVars(guardVars,temporaryStr);
    		println( temporaryStr );  
    		}
    		if( (guardVars = QActorUtils.evalTheGuard(this, " ??minore(B,A)" )) != null ){
    		if( ! planUtils.switchToPlan("rightRobot").getGoon() ) break;
    		}
    		if( (guardVars = QActorUtils.evalTheGuard(this, " !?maggiore(B,A)" )) != null ){
    		temporaryStr = "gt(A,B)";
    		temporaryStr = QActorUtils.substituteVars(guardVars,temporaryStr);
    		println( temporaryStr );  
    		}
    		if( (guardVars = QActorUtils.evalTheGuard(this, " ??maggiore(B,A)" )) != null ){
    		if( ! planUtils.switchToPlan("leftRobot").getGoon() ) break;
    		}
    		if( (guardVars = QActorUtils.evalTheGuard(this, " !?uguale(A,B)" )) != null ){
    		temporaryStr = "eq(A,B)";
    		temporaryStr = QActorUtils.substituteVars(guardVars,temporaryStr);
    		println( temporaryStr );  
    		}
    		if( (guardVars = QActorUtils.evalTheGuard(this, " ??uguale(A,B)" )) != null ){
    		if( ! planUtils.switchToPlan("stopRobot").getGoon() ) break;
    		}
    		if( planUtils.repeatPlan(nPlanIter,0).getGoon() ) continue;
    break;
    }//while
    return returnValue;
    }catch(Exception e){
       //println( getName() + " plan=receiveSonarBDistance WARNING:" + e.getMessage() );
       QActorContext.terminateQActorSystem(this); 
       return false;  
    }
    }
    public boolean valueSonarBDistance() throws Exception{	//public to allow reflection
    try{
    	int nPlanIter = 0;
    	//curPlanInExec =  "valueSonarBDistance";
    	boolean returnValue = suspendWork;		//MARCHH2017
    while(true){
    	curPlanInExec =  "valueSonarBDistance";	//within while since it can be lost by switchlan
    	nPlanIter++;
    		if( ! checkInMsgQueue() ){
    			//ReceiveMsg
    					 aar  = planUtils.receiveMsg(mysupport,
    					 "sonar" ,"MSGTYPE", 
    					 "qademo2sonarb",this.getName(), 
    					 "sonar(sonarb,qrdemo2robot,D)","MSGNUM", 20000, "alarm,obstacle" , "stopRobot,stopRobot");	//could block
    					//println(getName() + " plan " + curPlanInExec  +  " interrupted=" + aar.getInterrupted() + " action goon="+aar.getGoon());
    					if( aar.getInterrupted() ){
    						curPlanInExec   = "valueSonarBDistance";
    						if( aar.getTimeRemained() <= 0 ) addRule("tout(ReceiveMsg,"+getName()+")");
    						if( ! aar.getGoon() ) break;
    					} 			
    				    if( ! aar.getGoon() || aar.getTimeRemained() <= 0 ){
    				    	//println("	WARNING: receivemsg timeout " + aar.getTimeRemained());
    				    	addRule("tout(receivemsg,"+getName()+")");
    				    }
    		}
    		//onMsg
    		if( currentMessage.msgId().equals("sonar") ){
    			String parg = "distdib(F)";
    			/* Print */
    			parg =  updateVars( Term.createTerm("sonar(SONARNAME,TARGETNAME,DISTANCE)"), Term.createTerm("sonar(sonarb,qrdemo2robot,F)"), 
    				    		  					Term.createTerm(currentMessage.msgContent()), parg);
    				if( parg != null ) println( parg );  
    		}//onMsg
    		if( currentMessage.msgId().equals("sonar") ){
    			String parg="distB(F)";
    			/* PHead */
    			parg =  updateVars( Term.createTerm("sonar(SONARNAME,TARGETNAME,DISTANCE)"), Term.createTerm("sonar(sonarb,qrdemo2robot,F)"), 
    				    		  					Term.createTerm(currentMessage.msgContent()), parg);
    				if( parg != null ) {
    				    aar = QActorUtils.solveGoal(this,myCtx,pengine,parg,"",outEnvView,86400000);
    					//println(getName() + " plan " + curPlanInExec  +  " interrupted=" + aar.getInterrupted() + " action goon="+aar.getGoon());
    					if( aar.getInterrupted() ){
    						curPlanInExec   = "valueSonarBDistance";
    						if( aar.getTimeRemained() <= 0 ) addRule("tout(demo,"+getName()+")");
    						if( ! aar.getGoon() ) break;
    					} 			
    					if( aar.getResult().equals("failure")){
    						if( ! aar.getGoon() ) break;
    					}else if( ! aar.getGoon() ) break;
    				}
    		}if( (guardVars = QActorUtils.evalTheGuard(this, " !?uguale(A,B)" )) != null ){
    		temporaryStr = "ug(A,B)";
    		temporaryStr = QActorUtils.substituteVars(guardVars,temporaryStr);
    		println( temporaryStr );  
    		}
    		if( (guardVars = QActorUtils.evalTheGuard(this, " ??uguale(A,B)" )) != null ){
    		if( ! planUtils.switchToPlan("stopRobot").getGoon() ) break;
    		}
    		if( planUtils.repeatPlan(nPlanIter,0).getGoon() ) continue;
    break;
    }//while
    return returnValue;
    }catch(Exception e){
       //println( getName() + " plan=valueSonarBDistance WARNING:" + e.getMessage() );
       QActorContext.terminateQActorSystem(this); 
       return false;  
    }
    }
    public boolean startRobot() throws Exception{	//public to allow reflection
    try{
    	int nPlanIter = 0;
    	//curPlanInExec =  "startRobot";
    	boolean returnValue = suspendWork;		//MARCHH2017
    while(true){
    	curPlanInExec =  "startRobot";	//within while since it can be lost by switchlan
    	nPlanIter++;
    		temporaryStr = "\"#######Robot go to point B#######\"";
    		println( temporaryStr );  
    		//forward
    		//if( ! execRobotMove("startRobot","forward",60,0,0, "alarm,obstacle" , "stopRobot,handleObstacle") ) break;
    		    aar = execRobotMove("startRobot","forward",60,0,0, "alarm,obstacle" , "stopRobot,handleObstacle");
    		    if( aar.getInterrupted() ){
    		    	curPlanInExec   = "startRobot";
    		    	if( ! aar.getGoon() ) break;
    		    } 			
    		if( ! planUtils.switchToPlan("receiveSonarBDistance").getGoon() ) break;
    break;
    }//while
    return returnValue;
    }catch(Exception e){
       //println( getName() + " plan=startRobot WARNING:" + e.getMessage() );
       QActorContext.terminateQActorSystem(this); 
       return false;  
    }
    }
    public boolean leftRobot() throws Exception{	//public to allow reflection
    try{
    	int nPlanIter = 0;
    	//curPlanInExec =  "leftRobot";
    	boolean returnValue = suspendWork;		//MARCHH2017
    while(true){
    	curPlanInExec =  "leftRobot";	//within while since it can be lost by switchlan
    	nPlanIter++;
    		//left
    		//if( ! execRobotMove("leftRobot","left",60,0,1500, "" , "") ) break;
    		    aar = execRobotMove("leftRobot","left",60,0,1500, "" , "");
    		    if( aar.getInterrupted() ){
    		    	curPlanInExec   = "leftRobot";
    		    	if( ! aar.getGoon() ) break;
    		    } 			
    		temporaryStr = "\"#######Robot LEFT#######\"";
    		println( temporaryStr );  
    		//forward
    		//if( ! execRobotMove("leftRobot","forward",60,0,0, "alarm,obstacle" , "stopRobot,stopRobot") ) break;
    		    aar = execRobotMove("leftRobot","forward",60,0,0, "alarm,obstacle" , "stopRobot,stopRobot");
    		    if( aar.getInterrupted() ){
    		    	curPlanInExec   = "leftRobot";
    		    	if( ! aar.getGoon() ) break;
    		    } 			
    		if( ! planUtils.switchToPlan("valueSonarBDistance").getGoon() ) break;
    break;
    }//while
    return returnValue;
    }catch(Exception e){
       //println( getName() + " plan=leftRobot WARNING:" + e.getMessage() );
       QActorContext.terminateQActorSystem(this); 
       return false;  
    }
    }
    public boolean rightRobot() throws Exception{	//public to allow reflection
    try{
    	int nPlanIter = 0;
    	//curPlanInExec =  "rightRobot";
    	boolean returnValue = suspendWork;		//MARCHH2017
    while(true){
    	curPlanInExec =  "rightRobot";	//within while since it can be lost by switchlan
    	nPlanIter++;
    		//right
    		//if( ! execRobotMove("rightRobot","right",60,0,1500, "" , "") ) break;
    		    aar = execRobotMove("rightRobot","right",60,0,1500, "" , "");
    		    if( aar.getInterrupted() ){
    		    	curPlanInExec   = "rightRobot";
    		    	if( ! aar.getGoon() ) break;
    		    } 			
    		temporaryStr = "\"#######Robot RIGHT#######\"";
    		println( temporaryStr );  
    		//forward
    		//if( ! execRobotMove("rightRobot","forward",60,0,0, "alarm,obstacle" , "stopRobot,stopRobot") ) break;
    		    aar = execRobotMove("rightRobot","forward",60,0,0, "alarm,obstacle" , "stopRobot,stopRobot");
    		    if( aar.getInterrupted() ){
    		    	curPlanInExec   = "rightRobot";
    		    	if( ! aar.getGoon() ) break;
    		    } 			
    		if( ! planUtils.switchToPlan("valueSonarBDistance").getGoon() ) break;
    break;
    }//while
    return returnValue;
    }catch(Exception e){
       //println( getName() + " plan=rightRobot WARNING:" + e.getMessage() );
       QActorContext.terminateQActorSystem(this); 
       return false;  
    }
    }
    public boolean stopRobot() throws Exception{	//public to allow reflection
    try{
    	int nPlanIter = 0;
    	//curPlanInExec =  "stopRobot";
    	boolean returnValue = suspendWork;		//MARCHH2017
    while(true){
    	curPlanInExec =  "stopRobot";	//within while since it can be lost by switchlan
    	nPlanIter++;
    		//stop
    		//if( ! execRobotMove("stopRobot","stop",0,0,0, "" , "") ) break;
    		    aar = execRobotMove("stopRobot","stop",0,0,0, "" , "");
    		    if( aar.getInterrupted() ){
    		    	curPlanInExec   = "stopRobot";
    		    	if( ! aar.getGoon() ) break;
    		    } 			
    		temporaryStr = "\"#######Robot Stop#######\"";
    		println( temporaryStr );  
    		//delay
    		aar = delayReactive(100000,"" , "");
    		if( aar.getInterrupted() ) curPlanInExec   = "stopRobot";
    		if( ! aar.getGoon() ) break;
    		println( "#######Robot End#######" );
    		//QActorContext.terminateQActorSystem(this); 
    break;
    }//while
    return returnValue;
    }catch(Exception e){
       //println( getName() + " plan=stopRobot WARNING:" + e.getMessage() );
       QActorContext.terminateQActorSystem(this); 
       return false;  
    }
    }
    public boolean handleObstacle() throws Exception{	//public to allow reflection
    try{
    	int nPlanIter = 0;
    	//curPlanInExec =  "handleObstacle";
    	boolean returnValue = suspendWork;		//MARCHH2017
    while(true){
    	curPlanInExec =  "handleObstacle";	//within while since it can be lost by switchlan
    	nPlanIter++;
    		temporaryStr = "\"#######!!!WARNING OBSTACLE!!!#######\"";
    		println( temporaryStr );  
    		//stop
    		//if( ! execRobotMove("handleObstacle","stop",0,0,0, "" , "") ) break;
    		    aar = execRobotMove("handleObstacle","stop",0,0,0, "" , "");
    		    if( aar.getInterrupted() ){
    		    	curPlanInExec   = "handleObstacle";
    		    	if( ! aar.getGoon() ) break;
    		    } 			
    		//delay
    		aar = delayReactive(3000,"alarm" , "stopRobot");
    		if( aar.getInterrupted() ) curPlanInExec   = "handleObstacle";
    		if( ! aar.getGoon() ) break;
    		//senseEvent
    		aar = planUtils.senseEvents( 500,"obstacle","continue",
    		"" , "",ActionExecMode.synch );
    		if( ! aar.getGoon() || aar.getTimeRemained() <= 0 ){
    			//println("			WARNING: sense timeout");
    			addRule("tout(senseevent,"+getName()+")");
    		}
    		//onEvent
    		if( currentEvent.getEventId().equals("obstacle") ){
    		 		String parg = "";
    		 		/* SwitchPlan */
    		 		parg =  updateVars(  Term.createTerm("obstacle(X)"), Term.createTerm("obstacle(D)"), 
    		 			    		  					Term.createTerm(currentEvent.getMsg()), parg);
    		 			if( parg != null ){
    		 				 if( ! planUtils.switchToPlan("handleFixedObstacle").getGoon() ) break; 
    		 			}//else println("guard  fails");  //parg is null when there is no guard (onEvent)
    		 }
    		if( ! planUtils.switchToPlan("handleMobileObstacle").getGoon() ) break;
    break;
    }//while
    return returnValue;
    }catch(Exception e){
       //println( getName() + " plan=handleObstacle WARNING:" + e.getMessage() );
       QActorContext.terminateQActorSystem(this); 
       return false;  
    }
    }
    public boolean handleMobileObstacle() throws Exception{	//public to allow reflection
    try{
    	int nPlanIter = 0;
    	//curPlanInExec =  "handleMobileObstacle";
    	boolean returnValue = suspendWork;		//MARCHH2017
    while(true){
    	curPlanInExec =  "handleMobileObstacle";	//within while since it can be lost by switchlan
    	nPlanIter++;
    		temporaryStr = "\"#######!!!MOBILE OBSTACLE!!!#######\"";
    		println( temporaryStr );  
    		if( ! planUtils.switchToPlan("startRobot").getGoon() ) break;
    break;
    }//while
    return returnValue;
    }catch(Exception e){
       //println( getName() + " plan=handleMobileObstacle WARNING:" + e.getMessage() );
       QActorContext.terminateQActorSystem(this); 
       return false;  
    }
    }
    public boolean handleFixedObstacle() throws Exception{	//public to allow reflection
    try{
    	int nPlanIter = 0;
    	//curPlanInExec =  "handleFixedObstacle";
    	boolean returnValue = suspendWork;		//MARCHH2017
    while(true){
    	curPlanInExec =  "handleFixedObstacle";	//within while since it can be lost by switchlan
    	nPlanIter++;
    		temporaryStr = "\"#######!!!FIXED OBSTACLE!!!#######\"";
    		println( temporaryStr );  
    		parg = "checkAdist(125)";
    		//tout=1 day (24 h)
    		//aar = solveGoalReactive(parg,86400000,"","");
    		//genCheckAar(m.name)»		
    		QActorUtils.solveGoal(parg,pengine );
    		if( (guardVars = QActorUtils.evalTheGuard(this, " !?minore(A,MAX)" )) != null ){
    		temporaryStr = "lt(A,MAX)";
    		temporaryStr = QActorUtils.substituteVars(guardVars,temporaryStr);
    		println( temporaryStr );  
    		}
    		if( (guardVars = QActorUtils.evalTheGuard(this, " ??minore(A,MAX)" )) != null ){
    		if( ! planUtils.switchToPlan("avoidFixedObstacleRight").getGoon() ) break;
    		}
    		if( (guardVars = QActorUtils.evalTheGuard(this, " !?maggiore(A,MAX)" )) != null ){
    		temporaryStr = "gt(A,MAX)";
    		temporaryStr = QActorUtils.substituteVars(guardVars,temporaryStr);
    		println( temporaryStr );  
    		}
    		if( (guardVars = QActorUtils.evalTheGuard(this, " ??maggiore(A,MAX)" )) != null ){
    		if( ! planUtils.switchToPlan("avoidFixedObstacleLeft").getGoon() ) break;
    		}
    		if( (guardVars = QActorUtils.evalTheGuard(this, " !?uguale(A,MAX)" )) != null ){
    		temporaryStr = "eq(A,MAX)";
    		temporaryStr = QActorUtils.substituteVars(guardVars,temporaryStr);
    		println( temporaryStr );  
    		}
    		if( (guardVars = QActorUtils.evalTheGuard(this, " ??uguale(A,MAX)" )) != null ){
    		if( ! planUtils.switchToPlan("avoidFixedObstacleLeft").getGoon() ) break;
    		}
    break;
    }//while
    return returnValue;
    }catch(Exception e){
       //println( getName() + " plan=handleFixedObstacle WARNING:" + e.getMessage() );
       QActorContext.terminateQActorSystem(this); 
       return false;  
    }
    }
    public boolean avoidFixedObstacleLeft() throws Exception{	//public to allow reflection
    try{
    	int nPlanIter = 0;
    	//curPlanInExec =  "avoidFixedObstacleLeft";
    	boolean returnValue = suspendWork;		//MARCHH2017
    while(true){
    	curPlanInExec =  "avoidFixedObstacleLeft";	//within while since it can be lost by switchlan
    	nPlanIter++;
    		temporaryStr = "\"#######!!!TRY AVOID OBSTACLE ON THE LEFT!!!#######\"";
    		println( temporaryStr );  
    		//left
    		//if( ! execRobotMove("avoidFixedObstacleLeft","left",60,0,1500, "" , "") ) break;
    		    aar = execRobotMove("avoidFixedObstacleLeft","left",60,0,1500, "" , "");
    		    if( aar.getInterrupted() ){
    		    	curPlanInExec   = "avoidFixedObstacleLeft";
    		    	if( ! aar.getGoon() ) break;
    		    } 			
    		//forward
    		//if( ! execRobotMove("avoidFixedObstacleLeft","forward",60,0,2500, "alarm,obstacle" , "stopRobot,changeToRight") ) break;
    		    aar = execRobotMove("avoidFixedObstacleLeft","forward",60,0,2500, "alarm,obstacle" , "stopRobot,changeToRight");
    		    if( aar.getInterrupted() ){
    		    	curPlanInExec   = "avoidFixedObstacleLeft";
    		    	if( ! aar.getGoon() ) break;
    		    } 			
    		//right
    		//if( ! execRobotMove("avoidFixedObstacleLeft","right",60,0,1500, "" , "") ) break;
    		    aar = execRobotMove("avoidFixedObstacleLeft","right",60,0,1500, "" , "");
    		    if( aar.getInterrupted() ){
    		    	curPlanInExec   = "avoidFixedObstacleLeft";
    		    	if( ! aar.getGoon() ) break;
    		    } 			
    		//senseEvent
    		aar = planUtils.senseEvents( 2000,"obstacle","continue",
    		"" , "",ActionExecMode.synch );
    		if( ! aar.getGoon() || aar.getTimeRemained() <= 0 ){
    			//println("			WARNING: sense timeout");
    			addRule("tout(senseevent,"+getName()+")");
    		}
    		//onEvent
    		if( currentEvent.getEventId().equals("obstacle") ){
    		 		//println("WARNING: variable substitution not yet implmented " ); 
    		 		if( planUtils.repeatPlan(nPlanIter,0).getGoon() ) continue;
    		 }
    		if( ! planUtils.switchToPlan("startRobot").getGoon() ) break;
    break;
    }//while
    return returnValue;
    }catch(Exception e){
       //println( getName() + " plan=avoidFixedObstacleLeft WARNING:" + e.getMessage() );
       QActorContext.terminateQActorSystem(this); 
       return false;  
    }
    }
    public boolean avoidFixedObstacleRight() throws Exception{	//public to allow reflection
    try{
    	int nPlanIter = 0;
    	//curPlanInExec =  "avoidFixedObstacleRight";
    	boolean returnValue = suspendWork;		//MARCHH2017
    while(true){
    	curPlanInExec =  "avoidFixedObstacleRight";	//within while since it can be lost by switchlan
    	nPlanIter++;
    		temporaryStr = "\"#######!!!TRY AVOID OBSTACLE ON THE RIGHT!!!#######\"";
    		println( temporaryStr );  
    		//right
    		//if( ! execRobotMove("avoidFixedObstacleRight","right",60,0,1500, "" , "") ) break;
    		    aar = execRobotMove("avoidFixedObstacleRight","right",60,0,1500, "" , "");
    		    if( aar.getInterrupted() ){
    		    	curPlanInExec   = "avoidFixedObstacleRight";
    		    	if( ! aar.getGoon() ) break;
    		    } 			
    		//forward
    		//if( ! execRobotMove("avoidFixedObstacleRight","forward",60,0,2500, "alarm,obstacle" , "stopRobot,changeToLeft") ) break;
    		    aar = execRobotMove("avoidFixedObstacleRight","forward",60,0,2500, "alarm,obstacle" , "stopRobot,changeToLeft");
    		    if( aar.getInterrupted() ){
    		    	curPlanInExec   = "avoidFixedObstacleRight";
    		    	if( ! aar.getGoon() ) break;
    		    } 			
    		//left
    		//if( ! execRobotMove("avoidFixedObstacleRight","left",60,0,1500, "" , "") ) break;
    		    aar = execRobotMove("avoidFixedObstacleRight","left",60,0,1500, "" , "");
    		    if( aar.getInterrupted() ){
    		    	curPlanInExec   = "avoidFixedObstacleRight";
    		    	if( ! aar.getGoon() ) break;
    		    } 			
    		//senseEvent
    		aar = planUtils.senseEvents( 2000,"obstacle","continue",
    		"" , "",ActionExecMode.synch );
    		if( ! aar.getGoon() || aar.getTimeRemained() <= 0 ){
    			//println("			WARNING: sense timeout");
    			addRule("tout(senseevent,"+getName()+")");
    		}
    		//onEvent
    		if( currentEvent.getEventId().equals("obstacle") ){
    		 		//println("WARNING: variable substitution not yet implmented " ); 
    		 		if( planUtils.repeatPlan(nPlanIter,0).getGoon() ) continue;
    		 }
    		if( ! planUtils.switchToPlan("startRobot").getGoon() ) break;
    break;
    }//while
    return returnValue;
    }catch(Exception e){
       //println( getName() + " plan=avoidFixedObstacleRight WARNING:" + e.getMessage() );
       QActorContext.terminateQActorSystem(this); 
       return false;  
    }
    }
    public boolean changeToLeft() throws Exception{	//public to allow reflection
    try{
    	int nPlanIter = 0;
    	//curPlanInExec =  "changeToLeft";
    	boolean returnValue = suspendWork;		//MARCHH2017
    while(true){
    	curPlanInExec =  "changeToLeft";	//within while since it can be lost by switchlan
    	nPlanIter++;
    		temporaryStr = "\"#######!!!CHANGE ON LEFT!!!#######\"";
    		println( temporaryStr );  
    		//delay
    		aar = delayReactive(2000,"alarm" , "stopRobot");
    		if( aar.getInterrupted() ) curPlanInExec   = "changeToLeft";
    		if( ! aar.getGoon() ) break;
    		//left
    		//if( ! execRobotMove("changeToLeft","left",60,0,1700, "" , "") ) break;
    		    aar = execRobotMove("changeToLeft","left",60,0,1700, "" , "");
    		    if( aar.getInterrupted() ){
    		    	curPlanInExec   = "changeToLeft";
    		    	if( ! aar.getGoon() ) break;
    		    } 			
    		if( ! planUtils.switchToPlan("avoidFixedObstacleLeft").getGoon() ) break;
    break;
    }//while
    return returnValue;
    }catch(Exception e){
       //println( getName() + " plan=changeToLeft WARNING:" + e.getMessage() );
       QActorContext.terminateQActorSystem(this); 
       return false;  
    }
    }
    public boolean changeToRight() throws Exception{	//public to allow reflection
    try{
    	int nPlanIter = 0;
    	//curPlanInExec =  "changeToRight";
    	boolean returnValue = suspendWork;		//MARCHH2017
    while(true){
    	curPlanInExec =  "changeToRight";	//within while since it can be lost by switchlan
    	nPlanIter++;
    		temporaryStr = "\"#######!!!CHANGE ON RIGHT!!!#######\"";
    		println( temporaryStr );  
    		//delay
    		aar = delayReactive(2000,"alarm" , "stopRobot");
    		if( aar.getInterrupted() ) curPlanInExec   = "changeToRight";
    		if( ! aar.getGoon() ) break;
    		//right
    		//if( ! execRobotMove("changeToRight","right",60,0,1700, "" , "") ) break;
    		    aar = execRobotMove("changeToRight","right",60,0,1700, "" , "");
    		    if( aar.getInterrupted() ){
    		    	curPlanInExec   = "changeToRight";
    		    	if( ! aar.getGoon() ) break;
    		    } 			
    		if( ! planUtils.switchToPlan("avoidFixedObstacleRight").getGoon() ) break;
    break;
    }//while
    return returnValue;
    }catch(Exception e){
       //println( getName() + " plan=changeToRight WARNING:" + e.getMessage() );
       QActorContext.terminateQActorSystem(this); 
       return false;  
    }
    }
    /* 
    * ------------------------------------------------------------
    * SENSORS
    * ------------------------------------------------------------
    */
    protected void initSensorSystem(){		
    	try {
    		String goal = "consult( \"./src/it/unibo/qrdemo2robot/sensorTheory.pl\" )";
    		SolveInfo sol = QActorUtils.solveGoal( goal ,pengine );
    		if( ! sol.isSuccess() ){
    			//println( "avatar initSensorSystem attempt to load sensorTheory "  );
    			goal = "consult( \"./sensorTheory.pl\" )";
    			QActorUtils.solveGoal( pengine, goal  );
    			//println( "avatar initSensorSystem= "  +  aar.getResult() );
    		}
    		addSensorObservers();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    /*
    //COMPONENTS
     RobotComponent motorleft 
     RobotComponent motorright 
    sensor distanceFront  todo  
    Composed component motors
    */
    protected void addSensorObservers(){
    	for (ISensor<?> sensor : Configurator.getInstance().getSensors()) {
    		//println( "qrdemo2robot sensor= "  + sensor.getDefStringRep() );
    		//println( "qrdemo2robot sensor class= "  + sensor.getClass().getName() );
        	if( sensor instanceof DistanceSensor){
        		DistanceSensor sensorDistance  = (DistanceSensor) sensor;
        		ISensorObserver<IDistanceSensorData> obs = new SensorObserver<IDistanceSensorData>(this,outEnvView);
        //		println( "avatar add observer to  "  + sensorDistance.getDefStringRep() );
        		sensorDistance.addObserver(  obs  ) ;
        	}
        	if( sensor instanceof LineSensor){
        		LineSensor sensorLine = (LineSensor) sensor;
         		ISensorObserver<ILineSensorData> obs = new SensorObserver<ILineSensorData>(this,outEnvView);
        //		println( "avatar add observer to  "  + sensorLine.getDefStringRep() );
        		sensorLine.addObserver(  obs  ) ;
        	}
         	if( sensor instanceof MagnetometerSensor){
        		MagnetometerSensor sensorMagneto = (MagnetometerSensor) sensor;
         		ISensorObserver<IMagnetometerSensorData> obs = new SensorObserver<IMagnetometerSensorData>(this,outEnvView);
        //		println( "avatar add observer to  "  + sensorMagneto.getDefStringRep() );
        		sensorMagneto.addObserver(  obs  ) ;
        	}
    		if( sensor instanceof ImpactSensor){
    			ImpactSensor sensorImpact = (ImpactSensor) sensor;
    			ISensorObserver<IImpactSensorData> obs = new SensorObserver<IImpactSensorData>(this,outEnvView);
    	//		println( "avatar add observer to  "  + sensorMagneto.getDefStringRep() );
    			sensorImpact.addObserver(  obs  ) ;
    		}
    	}		
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

