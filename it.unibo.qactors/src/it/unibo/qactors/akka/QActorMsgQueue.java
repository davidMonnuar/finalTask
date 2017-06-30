package it.unibo.qactors.akka;

import akka.actor.UntypedActor;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactors.QActorContext;
import it.unibo.qactors.QActorMessage;

public class QActorMsgQueue  extends UntypedActor {
	protected String actorId;
	protected QActorContext myCtx;
	protected IOutputEnvView outEnvView;
	
	public QActorMsgQueue (String actorId, QActorContext myCtx, IOutputEnvView outEnvView ){
		this.actorId    = actorId;
		this.myCtx      = myCtx;
		this.outEnvView = outEnvView;
	}
	@Override
	public void onReceive(Object message) throws Throwable {
		if (message instanceof QActorMessage){
//			handleQActorMessage( (QActorMessage)message );
			QActorMessage msg = (QActorMessage)message;
	  		println(actorId + " RECEIVES MSG =" + msg.getDefaultRep() );
	  		return;
 		}		
 		
	}

	public void println(String msg) {
		outEnvView.addOutput(msg);
	}

}
