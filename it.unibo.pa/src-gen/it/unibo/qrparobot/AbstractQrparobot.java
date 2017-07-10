/* Generated by AN DISI Unibo */ 
package it.unibo.qrparobot;
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
		super(name, ctx, "./srcMore/it/unibo/qrparobot/plans.txt", worldTheoryPath,
		outEnvView, it.unibo.qactor.robot.RobotSysKb.setRobotBase(ctx, baserobot) , defaultPlan);
	}
}

public class AbstractQrparobot extends QaRobotActor { 
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


	public AbstractQrparobot(String actorId, QActorContext myCtx, IOutputEnvView outEnvView ,String baserobot)  throws Exception{
		super(actorId, myCtx,  
		"./srcMore/it/unibo/qrparobot/WorldTheory.pl",
		setTheEnv( outEnvView ) ,baserobot , "init");		
		this.planFilePath = "./srcMore/it/unibo/qrparobot/plans.txt";
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
    		temporaryStr = "\"#######ReceiveCmd Robot########\"";
    		println( temporaryStr );  
    		if( ! checkInMsgQueue() ){
    			//ReceiveMsg
    					 aar  = planUtils.receiveMsg(mysupport,
    					 "cmd" ,"MSGTYPE", 
    					 "qapaconsole",this.getName(), 
    					 "cmd(start)","MSGNUM", 20000, "" , "");	//could block
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
    					 if( ! planUtils.switchToPlan("reciveSonarADistance").getGoon() ) break; 
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
    public boolean reciveSonarADistance() throws Exception{	//public to allow reflection
    try{
    	int nPlanIter = 0;
    	//curPlanInExec =  "reciveSonarADistance";
    	boolean returnValue = suspendWork;		//MARCHH2017
    while(true){
    	curPlanInExec =  "reciveSonarADistance";	//within while since it can be lost by switchlan
    	nPlanIter++;
    		temporaryStr = "\"#######SonarADistance Robot########\"";
    		println( temporaryStr );  
    		if( ! checkInMsgQueue() ){
    			//ReceiveMsg
    					 aar  = planUtils.receiveMsg(mysupport,
    					 "sonar" ,"MSGTYPE", 
    					 "qapasonar",this.getName(), 
    					 "sonara","MSGNUM", 20000, "" , "");	//could block
    					//println(getName() + " plan " + curPlanInExec  +  " interrupted=" + aar.getInterrupted() + " action goon="+aar.getGoon());
    					if( aar.getInterrupted() ){
    						curPlanInExec   = "reciveSonarADistance";
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
    			String parg="distA(S)";
    			/* AddRule */
    			parg = updateVars(Term.createTerm("sonar(SONARNAME,TARGETNAME,DISTANCE)"),  Term.createTerm("sonar(sonara,qrparobot,S)"), 
    				    		  					Term.createTerm(currentMessage.msgContent()), parg);
    			if( parg != null ) addRule(parg);	    		  					
    		}//onMsg
    		if( currentMessage.msgId().equals("sonar") ){
    			String parg = "";
    			/* SwitchPlan */
    			parg =  updateVars(  Term.createTerm("sonar(SONARNAME,TARGETNAME,DISTANCE)"), Term.createTerm("sonar(sonara,qrparobot,D)"), 
    				    		  					Term.createTerm(currentMessage.msgContent()), parg);
    				if( parg != null ){
    					 if( ! planUtils.switchToPlan("startRobot").getGoon() ) break; 
    				}//else println("guard  fails");  //parg is null when there is no guard (onEvent)
    		}if( planUtils.repeatPlan(nPlanIter,0).getGoon() ) continue;
    break;
    }//while
    return returnValue;
    }catch(Exception e){
       //println( getName() + " plan=reciveSonarADistance WARNING:" + e.getMessage() );
       QActorContext.terminateQActorSystem(this); 
       return false;  
    }
    }
    public boolean reciveSonarBDistance() throws Exception{	//public to allow reflection
    try{
    	int nPlanIter = 0;
    	//curPlanInExec =  "reciveSonarBDistance";
    	boolean returnValue = suspendWork;		//MARCHH2017
    while(true){
    	curPlanInExec =  "reciveSonarBDistance";	//within while since it can be lost by switchlan
    	nPlanIter++;
    		temporaryStr = "\"#######SonarBDistance Robot########\"";
    		println( temporaryStr );  
    		if( ! checkInMsgQueue() ){
    			//ReceiveMsg
    					 aar  = planUtils.receiveMsg(mysupport,
    					 "sonar" ,"MSGTYPE", 
    					 "qapasonar",this.getName(), 
    					 "sonarb","MSGNUM", 20000, "" , "");	//could block
    					//println(getName() + " plan " + curPlanInExec  +  " interrupted=" + aar.getInterrupted() + " action goon="+aar.getGoon());
    					if( aar.getInterrupted() ){
    						curPlanInExec   = "reciveSonarBDistance";
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
    			String parg="distB(F)";
    			/* PHead */
    			parg =  updateVars( Term.createTerm("sonar(SONARNAME,TARGETNAME,DISTANCE)"), Term.createTerm("sonar(sonarb,qrparobot,F)"), 
    				    		  					Term.createTerm(currentMessage.msgContent()), parg);
    				if( parg != null ) {
    				    aar = QActorUtils.solveGoal(this,myCtx,pengine,parg,"",outEnvView,86400000);
    					//println(getName() + " plan " + curPlanInExec  +  " interrupted=" + aar.getInterrupted() + " action goon="+aar.getGoon());
    					if( aar.getInterrupted() ){
    						curPlanInExec   = "reciveSonarBDistance";
    						if( aar.getTimeRemained() <= 0 ) addRule("tout(demo,"+getName()+")");
    						if( ! aar.getGoon() ) break;
    					} 			
    					if( aar.getResult().equals("failure")){
    						if( ! aar.getGoon() ) break;
    					}else if( ! aar.getGoon() ) break;
    				}
    		}if( (guardVars = QActorUtils.evalTheGuard(this, " !?uguale(A,B)" )) != null ){
    		if( ! planUtils.switchToPlan("stopRobot").getGoon() ) break;
    		}
    		if( planUtils.repeatPlan(nPlanIter,0).getGoon() ) continue;
    break;
    }//while
    return returnValue;
    }catch(Exception e){
       //println( getName() + " plan=reciveSonarBDistance WARNING:" + e.getMessage() );
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
    		//if( ! execRobotMove("startRobot","forward",100,0,0, "" , "") ) break;
    		    aar = execRobotMove("startRobot","forward",100,0,0, "" , "");
    		    if( aar.getInterrupted() ){
    		    	curPlanInExec   = "startRobot";
    		    	if( ! aar.getGoon() ) break;
    		    } 			
    		if( ! planUtils.switchToPlan("reciveSonarBDistance").getGoon() ) break;
    break;
    }//while
    return returnValue;
    }catch(Exception e){
       //println( getName() + " plan=startRobot WARNING:" + e.getMessage() );
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
    /* 
    * ------------------------------------------------------------
    * SENSORS
    * ------------------------------------------------------------
    */
    protected void initSensorSystem(){		
    	try {
    		String goal = "consult( \"./src/it/unibo/qrparobot/sensorTheory.pl\" )";
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
    Composed component motors
    */
    protected void addSensorObservers(){
    	for (ISensor<?> sensor : Configurator.getInstance().getSensors()) {
    		//println( "qrparobot sensor= "  + sensor.getDefStringRep() );
    		//println( "qrparobot sensor class= "  + sensor.getClass().getName() );
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

