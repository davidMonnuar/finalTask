package it.unibo.qactors;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.Executors;
import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import alice.tuprolog.Prolog;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.Struct;
import alice.tuprolog.Term;
import alice.tuprolog.Theory;
import it.unibo.connector.IConnector;
import it.unibo.contactEvent.interfaces.IContactEventPlatform;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactors.akka.QActor;
import it.unibo.qactors.akka.SenderObject;
import it.unibo.qactors.akka.SystemCreationActor;
import it.unibo.qactors.platform.EventLoopActor;
import it.unibo.qactors.platform.EventPlatformKb;
import it.unibo.system.SituatedPlainObject;
import it.unibo.system.SituatedSysKb;


/*
 * .) A context is the environment for QActors
 */
public class QActorContext extends SituatedPlainObject{
	protected  static QActorContext myself;
	public static boolean testing = false;
	
	public static final String dispatch   = "dispatch";
	public static final String request    = "request";
	public static final String answer     = "answer";
	public static final String endSystem  = "terminateSystem";
	
	public final InputStream sysKbStream;
	public final InputStream sysRulesStream;
		
	protected Prolog prologEngine;
//	protected boolean usingAkka=false;
	protected int ctxPort = 0;
	protected IContactEventPlatform platform;
	protected String localTheoryRep= "";
	
	protected int msgNum = 0;
	public  Hashtable<String,SenderObject> ctxTable;	//TODO public May2017
	
	protected ActorSystem system;
	protected QActor systemCreatorQa;
	protected ActorRef systemCreator;
	protected ActorContext creatorAkkaContext;
	
//	protected ActorSelection evlpActorSel;
	protected ActorRef evlpActorRef;
	protected EventLoopActor evlpActor;
	protected int numOfContexts = 0;
	
	protected String robotBase="mock";
/*
 * @param name
 * @param outEnvView
 * @param sysKbStream 
 * @param sysRulesStream  
 *  */ 
	protected  QActorContext(String name, IOutputEnvView outEnvView,  
			InputStream sysKbStream, InputStream sysRulesStream, String webDir, boolean test  ) throws Exception{
		super(name, outEnvView);
		this.sysKbStream      = sysKbStream;
 		this.sysRulesStream   = sysRulesStream;
 		testing = test;
 		init(webDir);
		println("	ACTIVATING " + this + " " +
 		  getName() + " ncores=" +  SituatedSysKb.numberOfCores + " port=" + ctxPort + " webDir=" + webDir + " testing=" + testing);
	}
	public static QActorContext initQActorSystem(
			String name, String systemTheoryName, String systemRulesFile, 
			IOutputEnvView outEnvView   ) throws Exception{
		return initQActorSystem(name, systemTheoryName, systemRulesFile, outEnvView , null, true);
	}
	public static QActorContext initQActorSystem(
			String name, String systemTheoryName, String systemRulesFile, 
			IOutputEnvView outEnvView, String webDir ) throws Exception{
   		return initQActorSystem(name,   systemTheoryName,   systemRulesFile, 
   				  outEnvView,   webDir, true);
	}
	public static QActorContext initQActorSystem(
			String name, String systemTheoryName, String systemRulesFile, 
			IOutputEnvView outEnvView, String webDir, boolean testing  ) throws Exception{
//		if( myself != null ) return myself;
 		EventPlatformKb.manyThreadexecutor=Executors.newScheduledThreadPool(9);  //used by ActionObservableGeneric
//		EventPlatformKb.manyThreadexecutor=Executors.newFixedThreadPool(9);  //JUNE2017
		if( outEnvView == null ) outEnvView = SituatedSysKb.standardOutEnvView;
  		InputStream sysKbStream             = new FileInputStream(systemTheoryName);
 		InputStream sysRulesStream          = new FileInputStream(systemRulesFile);
 		QActorContext ctx                   = new QActorContext(
 						name, outEnvView, sysKbStream, sysRulesStream, webDir, testing );
//		myself = ctx;
		outEnvView.addOutput("	%%% activateAkkaSystem in progress ..." + ctx.getName());
 		QActorUtils.activateAkkaSystem(name,ctx);
 		System.out.println("	QActorContext initQActorSystem DONE " + ctx + " akkCtx=" + ctx.getAkkaContext() );
  		return ctx;
	}

	public static void terminateQActorSystem( QActor actor ){
//		myself = null;
		QActorUtils.terminateTheQActor(actor,testing);
	} 
	public void terminateQActorSystem(){
//		System.out.println("REMOVED MARCH 2017: the system must end by itself");
  		myself = null;
// 		QActorUtils.resetActorTable();
 		QActorUtils.terminateTheQActorSystem( systemCreator, testing  );  
	}
   
//	public static QActorContext getSelf(){ 
//		return myself;
//	}
 	public boolean testing(){
 		return testing;
	}
	public void setActorSystem(ActorSystem system){	this.system = system;	}
	public ActorSystem getActorSystem(){ return system;	}

	public  ActorRef getSystemCreator(){ return systemCreator ;	}
	
	public void setSystemCreator(    ActorRef ref  ){
 		systemCreator      = ref;
 	}
	public void setSystemCreatorQa( QActor systemCreatorQa,  ActorContext creatorAkkaCtx ){
		this.systemCreatorQa = systemCreatorQa;
 		creatorAkkaContext   = creatorAkkaCtx;
 		
	}
	public ActorContext getAkkaContext(){ return creatorAkkaContext ;	}
	
	public void incNumOfContexts(){
		numOfContexts++;
	}
	public int getNumOfContexts(){
		return numOfContexts;
	}
//	public  ActorSelection getEvlpActorSel(){	return evlpActorSel;	}
	public  ActorRef getEvlpActorRef(){	return evlpActorRef;	}
	public  EventLoopActor getEventLoopActor(){		return evlpActor;	}
//	public  void  setEvlpActorSel( ActorSelection evlpActorSel){ this.evlpActorSel=evlpActorSel;	}
	public  void  setEvlpActorRef(ActorRef aref){	this.evlpActorRef=aref;	}
	public  void  setEventLoopActor(EventLoopActor a){	this.evlpActor= a;	}
	

	public void setRobotBase( String name){	robotBase = name;	}
	public String getRobotBase(  ){		return robotBase ;      }

	/*
	 * Initialize the platform
 	 */
	protected void init(String webDir ) throws Exception{
//		println("ActorContext init" );
		SituatedSysKb.init();	//Allows us to restart an application like happens in Android	
		ctxTable = new Hashtable<String,SenderObject>();
		loadSystemTheory();		//TO RUN FIRST	 		
		if(webDir!=null){
			println("	%%% Starting the http server ... ");
			new  it.unibo.qactors.web.QActorHttpServer(this,outEnvView,webDir,8080).start();
		}
 	}
	protected void loadSystemTheory() throws Exception{
		prologEngine 	 = new Prolog();
		Theory configTh  = new Theory( sysKbStream );
  		Theory rulesTh   = new Theory( sysRulesStream );
		prologEngine.addTheory(configTh);
		prologEngine.addTheory(rulesTh);
//		usingAkka        = QActorUtils.checkIfAkkaImplementation( prologEngine );
		ctxPort          = QActorUtils.getCtxPort( prologEngine, getName() );
		localTheoryRep   = QActorUtils.createLocalTheoryRep(this,prologEngine,this.outEnvView );
 	}

	//MAY2017
	public void addInstance( QActorContext ctx, String actorid, String className, IOutputEnvView view) throws Exception{
//  		System.out.println("			QActorContext addInstance " + actorid + " className=" + className);
		int lastDot = className.lastIndexOf(".");
		String rule = "assert(qactor( AID , CTX, CLASS  )).".
				replace("AID", actorid+"_ctrl").
				replace("CTX", this.getName()).
				replace("CLASS", "\""+className+"\"");
		
		this.prologEngine.solve(rule);
//		System.out.println("			QActorContext addInstance " + rule);
		String msgHandleClass = 
				className.substring( 0,lastDot )+
				".MsgHandle_"+className.substring( lastDot+1 );
//		System.out.println("			msgHandleClass " + msgHandleClass);
		rule = "assert(qactor( AID , CTX, CLASS  )).".
				replace("AID", actorid).
				replace("CTX", this.getName()).
				replace("CLASS", "\""+msgHandleClass+"\"" );
		
		this.prologEngine.solve(rule);
  		
		activateAkkaQActor(ctx,msgHandleClass,actorid,view);
		SystemCreationActor.numOfActors = SystemCreationActor.numOfActors + 1;
		activateAkkaQActor(ctx,className,actorid+"_ctrl",view);
		SystemCreationActor.numOfActors = SystemCreationActor.numOfActors + 1;
//  		System.out.println("			QActorContext addInstance " + rule);
		
//  if( actorid.equals("filter8")) System.out.println("			addInstance " + rule); //JUNE2017
 			
//		updateOtherContextsWithNewInstance();
		
		
	}
	protected void updateOtherContextsWithNewInstance() throws Exception{
		Enumeration<SenderObject> enob = ctxTable.elements();
//		System.out.println("	updateOtherContextsWithNewInstance " + enob.hasMoreElements() );
		while( enob.hasMoreElements()){
			SenderObject sa = enob.nextElement();
//			if( ! sa.ctx.getName().equals(this.getName() )) 
				sendUpdateSysKbpMsg(sa);
		}
	}
	//END MAY2017
	
	public int getCtxPort(){
		return ctxPort;
	}

/*
* ---------------------------------------------------------------
* DISTRIBUTION
* ---------------------------------------------------------------
*/
  
	
	public void activateSenderToCtx(String ctxName, boolean propagate ) throws Exception{
		//contextFact = context( CTX, HOST,  PROTOCOL, PORT )
		//activate the sender object for the new remote context
 		int curPort   	   =  QActorUtils.getCtxPort( ctxName, prologEngine );
		String curHostName =  QActorUtils.getCtxHost( ctxName, prologEngine );
		curHostName 	   =  curHostName.replaceAll("'", "");
//   		println("	%%% QActorContext activateSenderToCtx " + ctxName +":" + curPort  + " curHostName=" + curHostName );
		String protocol    =  QActorUtils.getCtxProtocol( ctxName, prologEngine );
		protocol           =  protocol.replaceAll("'", "");
		
		SenderObject sobj  =  ctxTable.get(ctxName) ;
		if( sobj != null ){ //sender already created
 			println("	%%% QActorContext has FOUND a sender for " + ctxName  );
			if( propagate ) sendUpdateSysKbpMsg( sobj );
			return;	
		}
		if( ctxPort != curPort ){ // CHECK WHY (for actors in the same ctx?)
			SenderObject sa = new SenderObject("sa_"+curPort, this, outView, protocol, curHostName, curPort ); //curHostName ???
//  			println("	%%% QActorContext activateSenderAgents TOWARDS context -> " + ctxName + " sa=" + sa.getName() );
			ctxTable.put(ctxName, sa);		
			if( propagate ) sendUpdateSysKbpMsg( sa ); 
		}
	}
	protected void sendUpdateSysKbpMsg(SenderObject sa) throws Exception{ 		
		String mout ="msg( MSGID, MSGTYPE, SENDER, RECEIVER, CONTENT, SEQNUM )".
				replace("MSGID","updatesyskb").replace("MSGTYPE","dispatch").replace("SENDER","remotectx_"+name).
				replace("RECEIVER","qasystem").replace("CONTENT",localTheoryRep).replace("SEQNUM","0");
 		System.out.println("	%%% QActorContext send updatesyskb SENDER=" + "remotectx_"+name + " sa=" + sa.getName() );
 		sa.sendMsg(mout);
 	}

	public void updateLocalTheoryRep( ) throws Exception{		
		localTheoryRep = QActorUtils.createLocalTheoryRep(this,prologEngine,this.outEnvView );
//		println(" %%%%%%%%%%%%%%%% localTheoryRep=" + localTheoryRep);
	}

	public SenderObject getSenderAgent(String actorId) throws Exception{
		String ctxName = QActorUtils.getActorCtx(actorId,this.prologEngine);
// 		println("getSenderAgent: " + actorId + " ctxName " + ctxName );
//  		println("QActorContext: " + actorId + " FINDS " + ctxTable.get(ctxName) + " size=" +  ctxTable.size()  + " " +ctxTable);
		return ctxTable.get(ctxName);
	}
	public SenderObject getSenderObject(String actorId) throws Exception{
//   		println("QActorContext: " + actorId + " FINDS " + ctxTable.get(actorId) + " size=" +  ctxTable.size()  + " " +ctxTable);
		return ctxTable.get(actorId);
	}
	public void clearSenderAgent(String actorId) throws Exception{
		String ctxName = QActorUtils.getActorCtx(actorId,this.prologEngine);
 		println("ctxTable: " + ctxName + " REMOVED"  );
		ctxTable.remove(ctxName);
  	}
	//Called by SystemCreationActor
	public int activateAkkaActorsInContext( ActorContext actorCtx) throws Exception{
//		println("	QActorContext ACTIVATE activateAkkaActorsInContext " + this.getName());
		int n3 = activateActorsInContext(   "robot" );
		int n2 = activateActorsInContext(   "eventhandler" );
		int n1 = activateActorsInContext(   "qactor"  );
		return n1 + n3 ; //we do not return n2 to allow strong termination when all the actors die
 	}
	
	//Called by SystemCreationActor
	public int activateActorsInContext(  String actorType ) throws Exception{
		int numOfActors=0;
		String goal="";
		if( actorType.equals("qactor"))
			goal =  "getTheActors(ACTORS,CTX)."; //"qactor(ACTOR, CTX, CLASS )."  ;
		else if( actorType.equals("eventhandler"))
			goal= "getTheHandlers( ACTORS, CTX  )." ;
		else if( actorType.equals("robot"))
			goal= "getTheRobots( ACTORS, CTX  )." ;
		goal = goal.replace("CTX", getName());
//   		System.out.println("	activateActorsInContext GOAL=" + goal  );
   		
 		SolveInfo actorSol = prologEngine.solve(goal);
// 		System.out.println("	activateActorsInContext actorSol=" + actorSol  );
 		
 		if( actorSol.isSuccess() ){
 			numOfActors = 0;
 			Struct ctxList  = (Struct) actorSol.getVarValue("ACTORS");
			Iterator<? extends Term> it = ctxList.listIterator();
			while( it.hasNext() ){
				Struct actor = (Struct) it.next();
				//System.out.println("	+++ activateActorsInContext " + actor + " " );
  				//qactor( A, CTX, CLASS )
  				String actorName = actor.getArg(0).toString();
  				String className = actor.getArg(2).toString().replaceAll("'", "");
  				String events    =  actorType.equals("eventhandler")?actor.getArg(3).toString():null;
//  				System.out.println(" activateActorsInContext events " + events);
  				activateTheProperActor(actorType,actorName,className,events);
 				numOfActors++;
 			}
		}
		return numOfActors;
 	}
	
	protected  void activateTheProperActor(String actorType, String actorName,String className,String events) throws Exception{
// 		println("	activateTheProperActor actorName=" + actorName + " className=" + className );
		if( actorType.equals("qactor") ){ 
			activateAkkaQActor(this,className,actorName,outEnvView);
		}else if( actorType.equals("eventhandler") ){
			activateAkkaEventHandler(this,className,actorName,outEnvView,events);
		}else if( actorType.equals("robot") ){
			//activateAkkaRobot(ctx,className,actorName,outEnvView);
		}
 	}
	
	//JUNE2017 after update to 2.5.2
	public Hashtable<String, ActorRef> actorCtxTab = new Hashtable<String, ActorRef>();
	
	public ActorRef getActorRefInQActorContext(String key){
		return  actorCtxTab.get(key);
	}
	public  ActorRef activateAkkaQActor( 
			QActorContext ctx,String className, String actorName, IOutputEnvView outEnvView ) throws Exception{
// 		System.out.println( "	%%% QActorContext activateAkkaQActor " + actorName + " " + className );
 		ActorRef aref = ctx.getAkkaContext().actorOf( 
					Props.create(Class.forName(className), actorName, ctx, outEnvView), actorName );
		QActorUtils.waitUntilQActorIsOn(actorName);
		System.out.println( "	%%% ------------------------------------------------------- "   );
		System.out.println( "	%%% QActorContext activateAkkaQActor " + actorName  + " | "  + SystemCreationActor.numOfActors  );
		System.out.println( "	%%% ------------------------------------------------------- "   );
		actorCtxTab.put(actorName, aref);
  		return aref;
	}	
	public  ActorRef activateAkkaEventHandler( 
			QActorContext ctx,String className, String actorName, IOutputEnvView outEnvView,String events) throws Exception{
//	 	System.out.println( "	%%% QActorUtils activateAkkaEventHandler " + actorName + " events=" + events + " className=" + className);
  	 	String[] eventArray =  QActorUtils.createArray( events.replaceAll("'", "") );
	 	ActorRef aref = ctx.getAkkaContext().actorOf( 					
					Props.create(Class.forName(className), actorName, ctx, outEnvView, eventArray), actorName );
	 	QActorUtils.waitUntilQActorIsOn(actorName);			
 		return aref;
	}
	public  ActorRef activateAkkaRobot( 
			QActorContext ctx,String className, String actorName, IOutputEnvView outEnvView ) throws Exception{
// 		System.out.println( "	%%% QActorUtils activateAkkaRobot " + actorName  );
 		ActorRef aref = ctx.getAkkaContext().actorOf( 
					Props.create(Class.forName(className), actorName, ctx, outEnvView, getRobotBase() ), actorName );
 		QActorUtils.waitUntilQActorIsOn(actorName);
  		return aref;
	}
	
	
	/*
	 * 	--------------------------------------------------------
	 *  METHODS
	 * 	--------------------------------------------------------
	 */
	public Prolog getEngine(){
		return prologEngine;
	}
	public IOutputEnvView getOutputEnvView(){
		return this.outEnvView;
	}
	public String getName(){
		return super.getName();
	}	
	protected void println(String msg) {
	 	if (outEnvView != null) outEnvView.addOutput(msg);
		else System.out.println(msg);
	}
	public int newMsgnum(){
		return ++msgNum;
	}

	
	/*
	 * UNITY
	 */
	private IConnector connToUnity;
	public IConnector getUnityConnector() {
 		return connToUnity;
	}	 
	public void setUnityConnector(IConnector connToUnity) {
 		this.connToUnity = connToUnity;
	}	

}
