package it.unibo.qactors.action;
import java.util.concurrent.Callable;
//import java.util.concurrent.Future;

import alice.tuprolog.Struct;
import alice.tuprolog.Term;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactors.QActorContext;
import it.unibo.qactors.akka.QActor;

public class ActionFibonacciTimed extends ActorTimedAction{
private int n = 0;
private String myresult="going";

	public ActionFibonacciTimed( QActor actor, QActorContext ctx, IOutputEnvView outEnvView,int n  ) throws Exception {
		this("fib",actor,ctx,n,false,"endFib", new String[]{}, outEnvView,6000000 );
	} 
	
 	public ActionFibonacciTimed(String name, QActor actor, QActorContext ctx, int n, boolean cancompensate,
			String terminationEvId, String[] alarms, IOutputEnvView outView,
			int maxduration) throws Exception {
		super(name, actor, ctx, cancompensate, terminationEvId, alarms, outView, maxduration);
		this.n = n;
//  		println("%%% ActionFibonacci CREATED " + name + " n= " +  n  );
  	}
 	
	
	@Override
	protected Callable<String> getActionBodyAsCallable() {
 		return new Callable<String>(){
			@Override
			public String call() throws Exception {
				myresult = ""+fibonacci(n);
				return myresult;
			}		
		};
	}	
	protected String fibonacci( String goalTodo ) throws Exception{
		Struct st = (Struct) Term.createTerm(goalTodo);
		n = Integer.parseInt( ""+st.getArg(0) );
 		return ""+fibonacci(n);
	}	
	protected long fibonacci( int n ) throws Exception{
 		if( n<0 || n==0 || n == 1 ) return 1;
		else return fibonacci(n-1) + fibonacci(n-2);
	}	 
	@Override
	public String getApplicationResult() throws Exception {
  		if( this.suspendevent == null ){ 
			return "fibo("+n+",val("+this.myresult+"),exectime("+this.getExecTime()+"))";		
		}else{
			return "fibo("+suspendevent+",exectime("+this.getExecTime()+"))";
		}
	}
 }
