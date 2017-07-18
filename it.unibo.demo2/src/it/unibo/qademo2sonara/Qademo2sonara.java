/* Generated by AN DISI Unibo */ 
/*
This code is generated only ONCE
*/
package it.unibo.qademo2sonara;
import java.io.BufferedReader;

import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactors.QActorContext;

public class Qademo2sonara extends AbstractQademo2sonara { 
	protected BufferedReader readerC;
	
	public Qademo2sonara(String actorId, QActorContext myCtx, IOutputEnvView outEnvView )  throws Exception{
		super(actorId, myCtx, outEnvView);
	}
	public void startSonarC(){
  		try {
  			println("startSonarC"   );
			Process p = Runtime.getRuntime().exec("sudo ./SonarAlone");
			readerC   = new BufferedReader(new java.io.InputStreamReader(p.getInputStream()));
			println("Process in C STARTED "  +  readerC);
		} catch (Exception e) {
 			e.printStackTrace();
		}		
	}	
	public void getDistanceFromSonar(){
		try {
			
//			println("getDistanceFromSonar"   );
			String dist = readerC.readLine();			
			println("getDistanceFromSonar " + dist  );		
			this.addRule("d("+dist+")");
			
		} catch (Exception e) {
 			e.printStackTrace();
 			
		}
	}	
}
