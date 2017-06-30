package it.unibo.qactors.action;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactors.QActorContext;
import it.unibo.qactors.akka.QActor;
 


public class ActionUtil {
	
	/*
	 * Could be called by several actors at 'the same time' => run in mutual exclusion
	 */
//	public static synchronized IActorAction buildSoundAction( IOutputEnvView outEnvView, int duration,  String answerEvent, String fName) throws Exception{
//		String terminationEvId = IActorAction.endBuiltinEvent+nameCount++;
// 		IActorAction action = new ActionSoundTimed(
// 				"sound",  terminationEvId, new String[]{}, outEnvView, duration, fName );  
//  		return action;
//  	}

	public static synchronized IActorAction buildSoundActionTimed(  QActor actor, QActorContext ctx, IOutputEnvView outEnvView, int duration, 
					String terminationEvId, String fName, String[] alarms) throws Exception{
  		IActorAction action = new ActionSoundTimed("sound", actor, ctx, terminationEvId,  alarms, outEnvView, duration, fName );  
  		return action;
  	}
	
	public static synchronized IActorAction buildFiboActionTimed( QActor actor, QActorContext ctx, IOutputEnvView outEnvView, int n, int duration, 
			String terminationEvId, String[] alarms) throws Exception{
		IActorAction action = new ActionFibonacciTimed("fibo", actor, ctx, n, false, terminationEvId,  alarms, outEnvView, duration  );  
		return action;
	}

//	public static synchronized IActorAction buildSoundActionTimed( 
//			IOutputEnvView outEnvView, int duration,  String answerEvent, String fName, String[] alarms) throws Exception{
//		String terminationEvId = QActorUtils.getNewName(IActorAction.endBuiltinEvent);//IActorAction.endBuiltinEvent+nameCount++;
// 		IActorAction action = new ActionSoundTimed(
// 				"sound", terminationEvId, alarms, outEnvView, duration, fName );  
//  		return action;
//  	}
}
	 
 
