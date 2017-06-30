package it.unibo.qactors.action;
import java.util.concurrent.Callable;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactors.QActorContext;
import it.unibo.qactors.QActorMessage;
import it.unibo.qactors.akka.QActor;

public class ActionReceiveTimed extends ActorTimedAction {
private QActorMessage myresult=null;
private IMsgQueue queue;

 	public ActionReceiveTimed(String name,  QActor actor,  QActorContext ctx,  IMsgQueue queue, boolean cancompensate,
			String terminationEvId, String[] alarms, IOutputEnvView outView,
			int maxduration) throws Exception {
		super(name, actor, ctx, cancompensate, terminationEvId, alarms, outView, maxduration);
		this.queue = queue;
//   		println("%%% ActionReceiveTimed CREATED " + name    );
  	}	
	@Override
	protected Callable<String> getActionBodyAsCallable() {
 		return new Callable<String>(){
			@Override
			public String call() throws Exception {
//  				println("	%%% ActionReceiveTimed queue " + queue    );
				myresult = queue.getMsgFromQueue();	//blocking but for maxduration only (TIMER)
//  				println("	%%% ActionReceiveTimed QUEUE myresult=" + myresult    );
				if( myresult == null ){			//TOUT or ALARM expired on queue	
//					println("	%%% ActionReceiveTimed  currentEvent=" + currentEvent    );
					return currentEvent.getDefaultRep();
				}
				return myresult.getDefaultRep();
			}		
		};
	}	
	@Override
	public String getApplicationResult() throws Exception {
  		if( this.suspendevent == null ){ 
			return myresult.getDefaultRep();		
		}else{
			String msg="receive("+suspendevent+",timeRemained("+timeRemained+"))";
			return "msg(interrupt,event,callable,none,MSG,0)".replace("MSG", msg);
		}
	}
 }
