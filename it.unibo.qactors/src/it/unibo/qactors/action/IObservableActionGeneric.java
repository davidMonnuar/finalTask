package it.unibo.qactors.action;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface IObservableActionGeneric<T> extends Callable<T>  {
	public T execSynch() throws Exception;
	public Future<T> execASynch() throws Exception;
	public String getTerminationEventId(); 
	public long   getExecTime();	 
 	public String getResultRep();
 	public String getName();
}