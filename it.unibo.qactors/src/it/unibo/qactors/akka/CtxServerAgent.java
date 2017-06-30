package it.unibo.qactors.akka;

import java.util.Vector;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.is.interfaces.protocols.IConnInteraction;
import it.unibo.qactors.QActorContext;
import it.unibo.qactors.QActorUtils;
import it.unibo.supports.FactoryProtocol;

public class CtxServerAgent extends UntypedAbstractActor{
  	public static  Vector<IConnInteraction> receiversTable = new  Vector<IConnInteraction>();
  	public static String endOfSystem = "endOfSystem";  	
  	private String autoMsgName="work";	
	protected String name;
 	protected QActorContext ctx;
	protected IOutputEnvView outEnvView;
	protected int port;
	protected IConnInteraction conn;
  	protected FactoryProtocol factoryP;
    	
	public CtxServerAgent(String name, QActorContext ctx, IOutputEnvView outEnvView,  int port){
		this.name = name;
		this.ctx 	= ctx;
 		this.port   = port;
		this.outEnvView = outEnvView;
		//println("	=== CtxServerAgent CREATED on port " + port);
	}
	@Override
	public void preStart() {
		getSelf().tell(autoMsgName, getSelf());
 	}
	/*
	 * The actor performs a very long computation, since its waits for a connection
	 */
	protected void waitForAConnection(){
 		try {
 			println("	=== CtxServerAgent WAITS (from other contexts) on port " + port);
 			System.setProperty("inputTimeOut", "10800000"); //3 ore
 			factoryP = new FactoryProtocol(outEnvView,FactoryProtocol.TCP, name);
 		} catch (Exception e) {
	 		println("	=== CtxServerAgent ERROR " + e.getMessage() );
		} 				
	}
	@Override 
	public void onReceive(Object message) throws Throwable {
		//println("	=== CtxServerAgent onReceive " + message );
		if( (message instanceof String) && ((String)message).equals(autoMsgName) ){
			int numOfOtherContexts = ctx.getNumOfContexts()-1;
			int numOfConn = 0;
			while(true){
				try {
					waitForAConnection();
					numOfConn++;					
 					IConnInteraction connIn  = factoryP.createServerProtocolSupport(port);
 		  			String receiverClassName = "it.unibo.qactors.akka.ReceiverAgent";	
 		  			String actorName = QActorUtils.getNewName("receiver_");
// 		  			ActorRef rec = ctx.activateAkkaQActor(ctx, receiverClassName, actorName, outEnvView); //JUNE2017
		 			ActorRef rec = ctx.getAkkaContext().actorOf( 
		 					Props.create(Class.forName(receiverClassName), actorName, ctx, outEnvView , connIn), actorName );
		 			ctx.actorCtxTab.put(actorName, rec);
		 			receiversTable.addElement(connIn);
		 			/*
		 			 * The following statements is correct for STATIC systems only
		 			 */
//		 			if( numOfConn == numOfOtherContexts ) break; //MAY2017
				} catch (Exception e) {
			 		println( "	=== CtxServerAgent BREAKS BUT RESTARTS " + e.getMessage() );
				} 	
			}//while
		}//if
//		println( "	=== CtxServerAgent ENDS ");			 
	}
	protected void println(String msg) {
  		ctx.getOutputEnvView().addOutput(msg);
	}

}
