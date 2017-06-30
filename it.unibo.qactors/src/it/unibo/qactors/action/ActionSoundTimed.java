package it.unibo.qactors.action;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.Callable;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactors.QActorContext;
import it.unibo.qactors.akka.QActor;
 
 
/*
 * ActionSound new version
 */
public class ActionSoundTimed extends ActorTimedAction{
protected String fName;
protected Clip clip = null;
 
 	public ActionSoundTimed(String name, QActor actor,  QActorContext ctx, String terminationEvId,  String[] alarms, IOutputEnvView outView,
			int maxduration, String fName) throws Exception {
		super(name, actor, ctx, false, terminationEvId,  alarms, outView, maxduration);
		this.fName = fName.trim();
		if( fName.startsWith("'")) this.fName = fName.substring(1,fName.length()-1);
//   		println("	%%% ActionSoundTimed CREATED " +  fName + " time=" + maxduration + " answerEvId=" + (answerEvId.length()==0?"":answerEvId) );
 	}
	/*
	 * This operation is called by ActionObservableGeneric.call  
 	 */

	
	@Override
	protected Callable<String> getActionBodyAsCallable() {
 		return new Callable<String>(){
			@Override
			public String call() throws Exception {
				playTheSound();
				return "play done";
			}		
		};
	}

	protected void playTheSound(){
		 try{	   		
// 	 	   	 println("	%%% ActionSoundTimed STARTS " +  fName   );
			 InputStream inputStream = new FileInputStream(fName);	
			 // get the sound file as a resource out of my jar file;
			 // the sound file must be in the same directory as this class file.
			 InputStream bufferedIn       = new BufferedInputStream(inputStream);
			 AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
			 clip = AudioSystem.getClip(); 
			 // Open audio clip and load samples from the audio input stream.
			 clip.open(audioStream);
			 clip.start();
//	 		 println("	%%% ActionSoundTimed sleep " + maxduration  );
 			 Thread.sleep(maxduration); 
			 clip.stop();
// 	 		 println("	%%% ActionSoundTimed END OF ACTION "   );
		 }catch(Exception e){
			 clip.stop();
//			 println("	%%% ActionSoundTimed INTERRUPTED " +  e.getMessage() );
		 }		
	}
	
	@Override
	public String getApplicationResult() throws Exception {
		if( this.suspendevent == null )
			return "playsound('" + fName+ "',timeremained(" + timeRemained +"))";
		else 
			return "playsound("+suspendevent+",timeRemained("+timeRemained+"))";
	}
  
	@Override
	public String toString(){
		return "ActionSoundTimed " + name + "(" + this.maxduration +")";
	}

}
