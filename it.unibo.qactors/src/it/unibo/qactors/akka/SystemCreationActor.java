package it.unibo.qactors.akka;

import java.util.Iterator;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.actor.Terminated;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.Struct;
import alice.tuprolog.Term;
import it.unibo.contactEvent.interfaces.IEventItem;
import it.unibo.is.interfaces.protocols.IConnInteraction;
import it.unibo.qactors.QActorContext;
import it.unibo.qactors.ActorTerminationMessage;
import it.unibo.qactors.QActorUtils;
import it.unibo.qactors.platform.EventPlatformKb;
import scala.concurrent.Future;
 
 
public class SystemCreationActor extends QActor{//UntypedAbstractActor{
   	public static int numOfActors = 0;
   	private int numOfActorsTerminated = 0;
   	
	public SystemCreationActor( String name, QActorContext ctx ){
 		super(name,ctx,null,ctx.getOutputEnvView(),null);
 	}
	//String actorId, ActorContext myCtx, String planFilePath, String worldTheoryPath, IOutputEnvView outEnvView, String defaultPlan
	
	@Override
	public void preStart() { 
		try {
			myCtx.setSystemCreatorQa( this, getContext()  );
// 			println("	SYSTEM CREATOR=" + myCtx.getSystemCreator() );		
// 			println("	================ context: "  + getName()   );		
			QActorUtils.startEventLoopActor(myCtx);
			activateTheServerAgent();		//required if standalone
			if( myCtx.getNumOfContexts() > 1 ){
//				activateTheServerAgent();
				activateSenderAgents();
			}
			numOfActors = myCtx.activateAkkaActorsInContext( getContext() ) ;
  			println("	%%%%% contexts= "  + myCtx.getNumOfContexts() + " actors=" + numOfActors + " " + myCtx.getSystemCreator());		
			println("	==== context: "  + getName()  + " activated " + numOfActors + " actors");		
			QActorUtils.memoQActor(  this );
			 
		} catch (Exception e) {
			println( "	%%%  SystemCreationActor " + getName() + " preStart FAIL:" + e.getMessage() );
			e.printStackTrace();
		}
	}

	@Override
	public void onReceive(Object message) throws Exception {
		println( "	%%%  SystemCreationActor receive  " + message   );
		if( message instanceof ActorTerminationMessage){
 			ActorTerminationMessage msg = (ActorTerminationMessage)message;
			String sender = msg.getName();
// 			System.out.println( "	%%%  SystemCreationActor onReceive " + msg );
			if(  msg.getName().equals("all")){
 				terminateActorInSystem( sender, msg.testing() );
 			}else{
				//unregister all for 
  	 			println( "	%%%  SystemCreationActor terminates the actor " + msg.getName()   );
	 			ActorRef evlp    = myCtx.getEvlpActorRef(); 
	 			//println( "	%%%  SystemCreationActor evlp " + evlp    );
	 			IEventItem unreg = QActorUtils.buildEventItem(  "system", EventPlatformKb.unregisterAll,  msg.getName()  );
	 			if( evlp != null ) evlp.tell(unreg, getSelf());
	 			//stop the actor
	 			ActorRef aref = getSender();  		
	 			boolean testing = msg.testing();
// 	 			println( "    %%% SystemCreationActor stops " + msg.getName() + " testing=" +testing );
	 			if( aref != null ) getContext().stop(aref);				
 				terminateActorInSystem( sender,testing ); //to increment numOfActorsTerminated
			
				if( msg.getName().endsWith("_ctrl")){
					aref = null;
					String name = msg.getName().replace("_ctrl", "");
		 			unreg = QActorUtils.buildEventItem(  "system", EventPlatformKb.unregisterAll,  msg.getName()  );
		 			if( evlp != null ) evlp.tell(unreg, getSelf());
		 			//stop the actor
		 			/*
		 			ActorSelection sel = QActorUtils.getSelectionIfLocal(this.myCtx, name);
		 			if(sel != null) aref = QActorUtils.getActorRefFromActorSelection(sel);  			 
// 		 			println( "    %%% SystemCreationActor stops " + name + " " + testing  );//+ ":" +aref
					if( aref != null ) getContext().stop(aref);
					*/
		 			//JUNE2017 after update to 2.5.2
		 			aref = this.myCtx.getActorRefInQActorContext(name);
 		 			if( aref != null ) getContext().stop(aref);
 		 			
  					terminateActorInSystem( name,testing ); //to increment numOfActorsTerminated
				}
			}
		}
  	}
	
	protected void terminateActorInSystem( String actorName, boolean testing ) throws Exception{
		numOfActorsTerminated++;
		System.out.println( "	%%%  SystemCreationActor terminates1:" + actorName 
				+ "  " + numOfActorsTerminated + "/" + numOfActors + " testing=" + testing );
		if( numOfActorsTerminated == numOfActors ){
 			if( ! testing){
 				System.exit(0);
 				return;
 			}
			System.out.println( "	%%%  SystemCreationActor terminates "  );
			terminateTheReceivers();
			terminateTheExecutor();
			QActor actor = QActorUtils.getQActor(actorName);
 			Thread.sleep(1000); //wait for a while to avoid dead letters ....
 			println( "    %%% SystemCreationActor ENDS THE SYSTEM " + actor.getQActorContext().getActorSystem() + " testing=" + testing );
			Future<Terminated> ft = actor.getQActorContext().getActorSystem().terminate();
				/*
If one of the actors does not respond (i.e. processing a message for extended periods of time and
therefore not receiving the stop command), this whole process will be stuck.
				 */
 			println( "    %%% SystemCreationActor ENDS completed=" + ft.isCompleted() );
		} 
	}

	protected void terminateTheExecutor() throws Exception{
		if(EventPlatformKb.manyThreadexecutor != null){
				System.out.println( "%%%SYSTEM TERMINATES EXECUTOR "     );
				EventPlatformKb.manyThreadexecutor.shutdown() ;
				EventPlatformKb.manyThreadexecutor.shutdownNow() ; 
		}	
//		System.exit(0);
	}
	protected void terminateTheReceivers() throws Exception{
		Iterator<IConnInteraction> iter = CtxServerAgent.receiversTable.listIterator();
		while( iter.hasNext() ){
			IConnInteraction conn = iter.next();
 			println( "    %%% SystemCreationActor GOING TO CLOSE " + conn );
// 			conn.sendALine( CtxServerAgent.endOfSystem   );  //DOES NOT RECEIVE
// 			Thread.sleep(3000);
			conn.closeConnection();
		} 
	}
	protected void activateTheServerAgent() throws Exception{
		String ctxServerClassName = "it.unibo.qactors.akka.CtxServerAgent";
		String serverName = myCtx.getName()+"_Server";	
 		getContext().actorOf( 
				Props.create(Class.forName(ctxServerClassName), serverName, myCtx, outEnvView , myCtx.getCtxPort()), 
				serverName );
	}
 	/*
 	 * For each known context activate a sender
 	 */
	public void activateSenderAgents() throws Exception{	
		SolveInfo sol   = myCtx.getEngine().solve("getCtxNames( CTXNAMES ).");	
 		if( !sol.isSuccess()){
 			throw new Exception("No contexts");
		}else{
			Struct ctxList  = (Struct) sol.getVarValue("CTXNAMES");
//			println("activateSenderAgents " +  ctxList );
			Iterator<? extends Term> it = ctxList.listIterator();
			while( it.hasNext() ){
				String curOtherCtx =  ""+it.next();
				//println("activateSenderAgents " +  curOtherCtx );
				if( ! curOtherCtx.equals(myCtx.getName()))
					myCtx.activateSenderToCtx(curOtherCtx,true);
			}
		}
	}
 
// 	protected void println(String msg) {
//  		ctx.getOutputEnvView().addOutput(msg);
//	}
	@Override
	protected void doJob() throws Exception {
		// TODO Auto-generated method stub
		
	}


}
