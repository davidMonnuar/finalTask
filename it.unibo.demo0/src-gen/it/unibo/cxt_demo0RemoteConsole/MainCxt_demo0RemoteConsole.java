/* Generated by AN DISI Unibo */ 
package it.unibo.cxt_demo0RemoteConsole;
import it.unibo.qactors.QActorContext;
import java.io.InputStream;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.Term;
import alice.tuprolog.Var;
 
import it.unibo.is.interfaces.IBasicEnvAwt;
import it.unibo.is.interfaces.IIntent;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.system.SituatedSysKb;

public class MainCxt_demo0RemoteConsole   {
private IBasicEnvAwt env; 
private it.unibo.qactor.robot.RobotActor robot; 
 
 	
/*
* ----------------------------------------------
* MAIN
* ----------------------------------------------
*/
 
	public static void main(String[] args) throws Exception{
			IOutputEnvView outEnvView = SituatedSysKb.standardOutEnvView;
			it.unibo.qactors.QActorUtils.setRobotBase("nano0" );  
			it.unibo.is.interfaces.IBasicEnvAwt env=new it.unibo.baseEnv.basicFrame.EnvFrame( 
			"Env_cxt_demo0RemoteConsole",java.awt.Color.white , java.awt.Color.black );
		env.init();
		outEnvView = env.getOutputEnvView();
		    String webDir = null;
			QActorContext.initQActorSystem(
				"cxt_demo0remoteconsole", "./srcMore/it/unibo/cxt_demo0RemoteConsole/demo0sys.pl", 
				"./srcMore/it/unibo/cxt_demo0RemoteConsole/sysRules.pl", outEnvView,webDir, false);
 	}
 	
}
