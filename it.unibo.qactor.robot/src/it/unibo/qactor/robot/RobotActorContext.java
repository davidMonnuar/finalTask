package it.unibo.qactor.robot;

import java.io.InputStream;

import it.unibo.iot.executors.baseRobot.IBaseRobot;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactors.QActorContext;

public class RobotActorContext extends QActorContext{
protected static IBaseRobot baseRobot;
	protected RobotActorContext(String name, IOutputEnvView outEnvView, InputStream sysKbStream,
			InputStream sysRulesStream, String webDir, boolean test) throws Exception {
		super(name, outEnvView, sysKbStream, sysRulesStream, webDir, test);
 	}
	public static RobotActorContext initQActorSystem(
			String name, String systemTheoryName, String systemRulesFile, 
			String baseRobotName,
			IOutputEnvView outEnvView, String webDir, boolean testing  ) throws Exception{
		baseRobot = RobotSysKb.setRobotBase(myself,  baseRobotName );
		return null;
	}

}
