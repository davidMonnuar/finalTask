package it.unibo.qactor.robot.action;
import java.util.concurrent.Callable;

import it.unibo.iot.executors.baseRobot.IBaseRobot;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactor.robot.IRobotTimedCommand;
import it.unibo.qactor.robot.RobotSysKb;
import it.unibo.qactors.QActorUtils;
import it.unibo.qactors.action.ActorTimedAction;
import it.unibo.qactors.akka.QActor;
 
 
public class RobotMoveActionTimed extends ActorTimedAction{
private IBaseRobot robot;
private IRobotTimedCommand command;

	public RobotMoveActionTimed(String name, QActor myactor, IBaseRobot robot, IRobotTimedCommand tcommand, 
			boolean cancompensate, String terminationEvId, String[] alarms,
			IOutputEnvView outEnvView, int maxduration) throws Exception {
		super(QActorUtils.getNewName(name), myactor.getQActorContext(), cancompensate, terminationEvId,  alarms, outEnvView, maxduration);  
		this.robot    	= robot;
		this.command	= tcommand;
// 		println("	%%% RobotMoveActionTimed CREATED " + alarms    );
	}
	
 
 	@Override
 	protected Callable<String> getActionBodyAsCallable(){
 		return new Callable<String>(){
			@Override
			public String call() throws Exception {
				execRobotAction();
				return "play done";
			}		
		};		
 	}
 	
	protected void execRobotAction() throws Exception {
// 		println("	%%% RobotMoveActionTimed execTheAction  ");
 		robot.execute(command.getRobotBaseCommand());
//		println("	%%% RobotMoveActionTimed " + getName() + " STARTS  §§§  ");
		try {
			Thread.sleep(maxduration);
		} catch (Exception e) {
//			println("	%%% RobotMoveActionTimed " + getName() + " interrupted §§§  ");
		} 
 		robot.execute(RobotSysKb.STOP);
// 		println("	%%% RobotMoveActionTimed execTheAction done timeRemained=" + timeRemained);
 	}

	@Override
	protected String getApplicationResult() throws Exception {
 		String s = "robotMove(restTime("+timeRemained+"))";
//		println("	%%% RobotMoveActionTimed getApplicationResult " + s);
		return s;
	}

}
