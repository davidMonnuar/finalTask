package it.unibo.qactors.web;

import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactors.QActorContext;

public class EvhHttpAppl extends QActorHttpServer.EvhQActorHttpServer{

 	public EvhHttpAppl(QActorHttpServer qaserver, String name, QActorContext myctx,
			String eventId, IOutputEnvView view) throws Exception {
		qaserver.super(name, myctx, eventId, view);
 	}

	@Override
	public void doJob() throws Exception {
		println(" +++ " + getName() + " APPLICATION-LEVEL event.driven action");
 		super.doJob();
		println("EvhQActorHttpServer cmd=" + getCmd() );
		getWs().send("ccc=" + ccc++); 
	}
 
}
