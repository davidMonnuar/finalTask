package it.unibo.qactors.action;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.Callable;
import alice.tuprolog.Prolog;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.Struct;
import alice.tuprolog.Term;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactors.QActorContext;
import it.unibo.qactors.QActorUtils;
import it.unibo.qactors.akka.QActor;

public class ActionOpTimed extends ActorTimedAction  {
protected String opcall;
protected String opcallResult;

public ActionOpTimed( 
		String name,   QActor actor,  QActorContext ctx, String opcall,
		String teminationEvId, String[] alarms, IOutputEnvView outView, int maxduration ) throws Exception {
	super(name, actor, ctx, false, teminationEvId, alarms, outView, maxduration); 
	this.opcall = opcall;
}

	@Override
	protected Callable<String> getActionBodyAsCallable() {
 		return new Callable<String>(){
			@Override
			public String call() throws Exception {
//				println("%%%  ActionOpTimed STARTS " + opcall);
  				Object b = actorOpExecute(opcall);  				
// 	 			System.out.println("actorOpExecute " + opcall + " RESULT=" + b); 	 			
  	 			opcallResult = (b == null) ? "null" : b.toString();
 				return opcallResult;
			}		
		};
	}
	
	@Override
	protected String getApplicationResult() throws Exception {
		if( this.suspendevent == null )
			return name+"(" + opcallResult + ", timeremained(" + timeRemained +"))";
		else  
			return name+"("+ opcallResult + "," + suspendevent + ",timeRemained("+timeRemained+"))";
	}
	
	@Override
	public String toString(){
		return "actorOpExecute " + opcall + "(" + opcallResult +")";
	}

/*
 * =====================================================	
 */
	public Object actorOpExecute(String parg) throws Exception{
		try{
//			System.out.println("%%% ActionOpTimed actorOpExecute " + parg);
			/*
			 * Use a new Prolog engine in order to avoid any interference with the actor world
			 * We use Prolog here just to extract the methid and the arguments of the actorOp call
			 */
			Prolog pengine = new Prolog();
  			String gg = "Op =.. L".replace("Op", parg);
// 			System.out.println(" %%%   actorOpExecute  "  + gg  );	//+ " dir= "  + pengine.getCurrentDirectory()			
 			SolveInfo sol   = pengine.solve(gg+".");
// 			System.out.println(" %%%   actorOpExecute L= "  + sol.getVarValue("L")  );
  
 			Struct tts = (Struct) sol.getVarValue("L");
			int arity = Integer.parseInt(""+tts.listSize())-1;
//			System.out.println("ARGS  NUM  = " + arity ) ;		
			Object[] params     = new Object[ arity ];
 			Class[]  paramType  = new Class[  arity ];

 			
 			int paramCount = 0;
 			Iterator<? extends Term> iter = tts.listIterator();
 			String   methodName = iter.next().toString(); 	 
 			while( iter.hasNext() ){
 				Term curt = iter.next();
 				//Struct atomic => list
//				System.out.println("	argtype=" + curt.getClass().getName()  + " isAtomic=" + curt.isAtomic());
 				if( curt instanceof alice.tuprolog.Int ){
// 					System.out.println("	argtype=" + curt.getClass().getName()  + " isAtomic=" + curt.isAtomic());
 					paramType[ paramCount  ]   = int.class;
 					int vv = ((alice.tuprolog.Int) curt).intValue();
 					params[ paramCount  ]    = vv;
 				}else{
 					paramType[ paramCount  ] = String.class;
 					params[ paramCount  ]    = (""+curt).replace("'","");	//eliminate starting and leading '
 				}
 				
 	 			paramCount++;
 			}
// 			for( int i=0; i<arity; i++){
// 				System.out.println("arg [" + i + "]=" +  params[i]  );
// 				System.out.println("type[" + i + "]=" +  paramType[i]  );
// 			}
 			Object b = 
				myactor.execByReflection( myactor.getClass(),methodName, params, paramType);
 			return b;
 			
// 			System.out.println("actorOpExecute RESULT=" + b);
// 			String res = (b == null) ? "null" : b.toString();
// 			myactor.solveGoal("setActorOpResult( Op, R )".replace("OP", opcall).replaceAll("R",res));
	 	} catch (Exception e) {
			System.out.println(" %%%  actorOpExecute WARNING: "  + e.getMessage() );
			return null;
	 	}	
		 
	}	
	
}
