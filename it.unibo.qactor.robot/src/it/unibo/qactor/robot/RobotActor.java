/*
 * A RobotActor is a QActor that provides the operation:  
 * 		long execute(String command, int speed, int angle, int millisec, String  evId)
 * 
 * 
 */
package it.unibo.qactor.robot;

import it.unibo.iot.executors.baseRobot.IBaseRobot;
import it.unibo.iot.models.commands.baseRobot.IBaseRobotCommand;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactor.robot.action.RobotMoveActionTimed;
import it.unibo.qactor.robot.action.RobotTimedCommand;
import it.unibo.qactor.robot.utils.RobotActorCmdUtils;
import it.unibo.qactor.robot.web.CmdUilInterpreter;
import it.unibo.qactors.QActorContext;
import it.unibo.qactors.QActorUtils;
import it.unibo.qactors.action.AsynchActionResult;
import it.unibo.qactors.action.IActorAction.ActionExecMode;
import it.unibo.qactors.action.IActorAction;
import it.unibo.qactors.akka.QActor;

  
public class RobotActor extends QActor implements IRobotActor {
	private static int nameCount = 1;
	protected IBaseRobot baseRobot ;  
	protected CmdUilInterpreter cmdInterpreter ;
 	
 	public RobotActor(String name, QActorContext ctx, String planPath,String worldTheoryPath,
 			IOutputEnvView outView, IBaseRobot robot, String initPlan ) throws Exception{
		super( name,ctx,worldTheoryPath, outView, initPlan );
		//super starts the actor before setting baseRobot
		this.initPlan  = initPlan;
		this.baseRobot    = robot; //RobotSysKb.getRobotBase();
		cmdInterpreter = new CmdUilInterpreter();
  		println("CREATED RobotActor with baseRobot=" + baseRobot   );
		RobotSysKb.setRobotActor(this);
  	}

	@Override
	protected void doJob() throws Exception {
//		buildPlanTable();
//		executeThePlan("normal");
 	} 	
 	@Override
	public void terminate() {
		println("Robot " + getName() + " terminate  "  );
   	}
	@Override
	public IBaseRobot getBaseRobot() {
		return baseRobot;
	} 	
    public boolean isSimpleActor()  {
    	return false;
    }

	
	protected String buildArgString(String arg){
		return arg;
	}
	public boolean execCmdGui(String guicmd){ 
		cmdInterpreter.execute(guicmd.replace("'", ""));
		return true;
	}
	public boolean execRobotMove( String curPlanName, 
			   String command, int speed, int angle, int moveTime, String  events, String plans) throws Exception{
//		println("%%% RobotActo execRobotMove=" +  command  + " baseRobot=" + baseRobot);
	AsynchActionResult aar;
//	long tmove = moveTime;
	if( moveTime == 0){	//execute a baserobot command
			IBaseRobotCommand robotCommand = 
					RobotActorCmdUtils.createRobotCommandFromRepString("robotCommand("+command+" , "+ speed+ " , " + angle +" )");
//   			println("%%% RobotActor robotCommand=" +  robotCommand.getDefStringRep() + " baseRobot=" + baseRobot);
			baseRobot.execute(robotCommand);
  			return true;
	}
	int tmove = moveTime;
	do{
  		println("%%% RobotActor execRobotMove time= " +  tmove + " " + moveTime );
		aar = execute(command, speed, angle, tmove , events, plans);
  		println("%%% RobotActor execRobotMove aar=" +  aar  );
 		println("%%% RobotActor execRobotMove TimeRemained=" +  aar.getTimeRemained() + " " + aar.getGoon() );
		if( ! aar.getGoon() ) return true;
		tmove = (int) aar.getTimeRemained();   
	}while(tmove>0);  
	return( aar.getGoon() )  ;
	}	
/*
 * Called by  prolog  
 */
  	@Override
	public AsynchActionResult execute(String command, int speed, int angle, int moveTime, 
			String  events, String plans) throws Exception{
   		IBaseRobotCommand robotCommand = RobotActorCmdUtils.buildRobotCommand(command, speed, angle);
//     	println("%%% RobotActor execute " + robotCommand.getDefStringRep() + " moveTime=" + moveTime + " events=" + events + " plans=" + plans);
  		if( moveTime == 0 ){ 
  			baseRobot.execute(robotCommand);
  			return new AsynchActionResult(null, 0, normalEnd, continueWork, "", null);
  		}
  		String endOfMoveEvent = QActorUtils.getNewName(QActorUtils.locEvPrefix+"end");
    	IRobotTimedCommand tcommand = new RobotTimedCommand(robotCommand,moveTime,endOfMoveEvent);
 		String[] evarray   = QActorUtils.createArray(events);
		String[] planarray = QActorUtils.createArray(plans);
 		IActorAction moveAction = new RobotMoveActionTimed( 
 				"robotMoveTimed", this, baseRobot, tcommand, 
  				false, endOfMoveEvent, evarray ,outEnvView, moveTime);
   		return actionUtils.executeReactiveAction( moveAction,ActionExecMode.synch,evarray,planarray );
	}
  	


  	
  	protected boolean isACommand(String command){
  		boolean b = 
  				command.toLowerCase().equals(  RobotSysKb.forwardCommand ) ||
 				command.toLowerCase().equals(  RobotSysKb.backwardCommand ) ||
 				command.toLowerCase().equals(  RobotSysKb.rightCommand ) ||
 				command.toLowerCase().equals(  RobotSysKb.leftCommand ) ||
 				command.toLowerCase().equals(  RobotSysKb.stopCommand );
 			;
  		return b;
  	}
 
 
/*
 * =====================================================
 */
// 	@Override
//	public void setEnv(IBasicUniboEnv env) {
//		this.env = env;
//		if( env != null ) this.outEnvView = env.getOutputView()
//		outEnvView.addOutput("setEnv done");
//	}
 
	/*
	 * Make visible sendMsg
	 */
	public void sendMsg(String msgID, String destActorId, String msgType, String msg) throws Exception{
		super.sendMsg(msgID, destActorId, msgType, msg) ;
 	}
	/*
	 * executeAction of a robot first checks for a  ActorActionType.move
	 * and calls the superclass if it is not a move
	 */
//  	@Override
//	public AsynchActionResult executeAction(PlanActionDescr pa) throws Exception {
//// 		println("%%% RobotActor  executeAction " + pa.getDefStringRep() );
//		if( pa.getType().equals( ActorActionType.move  ) ){
//			int speed    = Integer.parseInt( pa.getArgs() );
//			int moveTime = Integer.parseInt( pa.getDuration() );
//			AsynchActionResult aar = execute( pa.getCommand(), speed, 0, moveTime, pa.getEvents(), pa.getPlans() );
////			println("RobotActor executeAction result = " + aar );
//			return aar;
//		}
//		else return super.executeAction(  pa );
//  
//	}
// 	public List<Var> solveTheGoal( String goal  ) throws Exception{
//	 		println("RobotActor solveTheGoal goal=" + goal + " pengine=" + pengine);
// 		SolveInfo sol = pengine.solve(goal+".");
// 		if( sol.isSuccess() ) return sol.getBindingVars();
// 		else{
// 			return null;
// 		}
// 	}



 
}
