package it.unibo.qactors.action;
import java.util.concurrent.Callable;

import alice.tuprolog.MalformedGoalException;
import alice.tuprolog.Prolog;
import alice.tuprolog.SolveInfo;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactors.QActorContext;
import it.unibo.qactors.akka.QActor;
 
/*
 * ActionSolveTimed
 */
public class ActionSolveTimed extends ActorTimedAction{
private String goal ;
private String myresult;
private Prolog pengine;

 	public ActionSolveTimed(String name,   QActor actor,  QActorContext ctx, Prolog pengine, String goal,
			String terminationEvId, String[] alarms, IOutputEnvView outView,
			int maxduration) throws Exception {
		super(name, actor, ctx, false, terminationEvId,  alarms, outView, maxduration);
		this.goal    = goal;
		this.pengine = pengine;
		myresult     ="going";
//		this.myself	 = this.cu;
 	}
 
 	@Override
	protected Callable<String> getActionBodyAsCallable(){
 		return new Callable<String>(){
			@Override
			public String call()   {
				try{
//   		  				System.out.println("ActionSolveTimed " + getName() + " solving ... " + goal + 
//   		  						" maxduration=" +maxduration + " pengine isHalted=" + pengine.isHalted() + " myself=" + myself);
						SolveInfo sol = pengine.solve(goal+".");
// 						System.out.println("ActionSolveTimed goal= " + goal + " sol= " + sol.isSuccess() ); 
						if( sol.isSuccess() ){
							myresult = ""+sol.getSolution();
						}else{
							myresult="failure";
 							if(suspendevent==null) pengine.solve("setPrologResult(failure).");  
						}
				}catch( Exception e){
					System.out.println("ActionSolveTimed " + goal + " ERROR= " + e.getMessage() );
					myresult="failure";
				}
 				return myresult;
			}
		};
	}
	
 	@Override
	public String getApplicationResult() throws Exception {
		//we return the solution as it is !!
// 		System.out.println("ActionSolveTimed  getApplicationResult myresult=" + myresult + " suspendevent=" + suspendevent);
// 		System.out.println("ActionSolveTimed  getApplicationResult execTime=" + this.getExecTime() );
		
		if( this.suspendevent == null ){
//			pengine.solve("setPrologResult(result("+myresult+",time(" + this.getExecTime() + "))).");  //SIDE EFFECT
			pengine.solve("setPrologResult("+myresult + ").");  //JAN 2017
			//MARCH 2017
			String result = "goalResult("+ this.myactor.getName() +","+ goal +","+ myresult+")";
			Thread.sleep(5);	//the sense in QActor solveGoalReactive must react first of all for alarms
//			this.myactor.emit("local_goalResult", "goalResult("+ this.myactor.getName() +","+ goal +","+ myresult+")");
			return result; // myresult;	 
		}else{			  
			//MARCH 2017
			String result = "goalResult("+ this.myactor.getName() +","+ goal +","+ suspendevent+")";
			Thread.sleep(5); //the sense in QActor solveGoalReactive must react first of all for alarms
//			this.myactor.emit("local_goalResult", "goalResult("+ this.myactor.getName() +","+ goal +","+ suspendevent+")" ); 
			pengine.solve("setPrologResult(EVENT).".replace("EVENT", suspendevent));
 			return result; //suspendevent;
		}
		
		//return "testgetApplicationResulttodo";
	}
 }
