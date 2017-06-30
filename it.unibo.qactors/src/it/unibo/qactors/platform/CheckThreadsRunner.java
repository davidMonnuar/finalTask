package it.unibo.qactors.platform;

import java.util.Iterator;
import java.util.Map;

public class CheckThreadsRunner extends Thread {
public static boolean goon = true;
	  public void run(){
		  while(goon) {
			  Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
			  Iterator<Thread> iter = map.keySet().iterator();
			  System.out.println("			%%%% ==================================== ");
			  int k = 0;
			  while(iter.hasNext()){
				  Thread th = iter.next();
				  k++;
 				  System.out.println("			%%%% CheckThreadsRunner "+ k + " | " +
 						  th + " " + th.isInterrupted() + " | " + Thread.activeCount());
			  }
			  System.out.println("			%%%% ==================================== ");
//		    import scala.collection.JavaConverters._
//		     dispThreads =
//		      Thread.getAllStackTraces.keySet.asScala.filter(_.getName startsWith "default-akka.actor.default-dispatcher")
//
//		    dispThreads.toVector.map(_.getName).sorted.foreach(println)
//		    System.out.println();
//		     System.out.println(s"Currently ${dispThreads.size} threads");

		    try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
 				e.printStackTrace();
			}
		  }//while
		  System.out.println("			%%%% CheckThreadsRunner ENDS ");
		}//run
}
