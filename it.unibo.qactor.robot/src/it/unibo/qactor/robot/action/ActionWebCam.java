package it.unibo.qactor.robot.action;
import java.util.concurrent.Callable;

import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactor.robot.RobotSysKb;
import it.unibo.qactor.robot.devices.RobotWebCam;
import it.unibo.qactors.action.ActorAction;
import it.unibo.qactors.action.ActorTimedAction;
import it.unibo.qactors.akka.QActor;
 

public class ActionWebCam extends ActorTimedAction{
protected String fName;
protected RobotWebCam webCam  = null ;
protected boolean photo;
 
	public ActionWebCam(String name, QActor myactor, boolean cancompensate, 
			String terminationEvId, String answerEvId, IOutputEnvView outEnvView, int maxduration, 
			boolean photo, String fName) throws Exception {
		super(name,  myactor.getQActorContext(), cancompensate, terminationEvId, new String[]{}, outEnvView,  maxduration);
		this.photo = photo;
		this.fName = fName.replaceAll("'","").trim();
 	}
 	@Override
 	protected Callable<String> getActionBodyAsCallable(){
 		return new Callable<String>(){
			@Override
			public String call() throws Exception {
  				if(photo) 
  					execPhoto(fName);
  				else
  					execVideo(fName );		
				return "photo done";
			}		
		};		
 	}

 	protected void execVideo( String fName  ) throws Exception {
//		println("		%%% Action execVideo to " +  fName );
		int nFrames = 300;
 		webCam.setForVideo();
		webCam.captureVideo(nFrames,fName);
		Thread.sleep( maxduration );
    } 
    protected void execPhoto(String fName) throws Exception{
    	println("		%%% Action execPhoto to " +  fName );
	    webCam.setForImage(2592,1944);//width=2592,height=1944
		webCam.captureImg(fName);
		Thread.sleep( maxduration );
    }
//	@Override
//	public void suspendAction(){
// 		if( webCam != null ) webCam.reset();
//  		super.suspendAction();
//	}
//	@Override
//	protected void execTheAction() throws Exception {
//		println("ActionWebCam execTheAction TODO ");
// 	}
    
	@Override
	protected String getApplicationResult() throws Exception {
		println("ActionWebCam getApplicationResult TODO ");
		return photo ? "photoDone" : "videoDone";
	}
 }
