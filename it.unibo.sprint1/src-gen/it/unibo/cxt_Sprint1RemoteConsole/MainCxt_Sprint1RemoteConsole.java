/* Generated by AN DISI Unibo */ 
package it.unibo.cxt_Sprint1RemoteConsole;
import it.unibo.qactors.QActorContext;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.system.SituatedSysKb;
public class MainCxt_Sprint1RemoteConsole  {
  
//MAIN
public static QActorContext initTheContext() throws Exception{
	IOutputEnvView outEnvView = SituatedSysKb.standardOutEnvView;
	it.unibo.is.interfaces.IBasicEnvAwt env=new it.unibo.baseEnv.basicFrame.EnvFrame( 
		"Env_cxt_Sprint1RemoteConsole",java.awt.Color.white , java.awt.Color.black );
	env.init();
	outEnvView = env.getOutputEnvView();
	String webDir = null;
	return QActorContext.initQActorSystem(
		"cxt_sprint1remoteconsole", "./srcMore/it/unibo/cxt_Sprint1RemoteConsole/sprint1.pl", 
		"./srcMore/it/unibo/cxt_Sprint1RemoteConsole/sysRules.pl", outEnvView,webDir,false);
}
public static void main(String[] args) throws Exception{
	QActorContext ctx = initTheContext();
} 	
}