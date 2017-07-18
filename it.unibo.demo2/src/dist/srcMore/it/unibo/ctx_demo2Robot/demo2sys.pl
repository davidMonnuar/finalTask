%====================================================================================
% Context ctx_demo2Robot  SYSTEM-configuration: file it.unibo.ctx_demo2Robot.demo2Sys.pl 
%====================================================================================
context(ctx_demo2robot, "172.20.10.3",  "TCP", "8079" ).  		 
context(ctx_demo2remoteconsole, "172.20.10.2",  "TCP", "8089" ).  		 
context(ctx_demo2sonara, "172.20.10.2",  "TCP", "8099" ).  		 
context(ctx_demo2sonarb, "172.20.10.2",  "TCP", "8069" ).  		 
%%% -------------------------------------------
qactor( qademo2console , ctx_demo2remoteconsole, "it.unibo.qademo2console.MsgHandle_Qademo2console"   ). %%store msgs 
qactor( qademo2console_ctrl , ctx_demo2remoteconsole, "it.unibo.qademo2console.Qademo2console"   ). %%control-driven 
qactor( qademo2sonara , ctx_demo2sonara, "it.unibo.qademo2sonara.MsgHandle_Qademo2sonara"   ). %%store msgs 
qactor( qademo2sonara_ctrl , ctx_demo2sonara, "it.unibo.qademo2sonara.Qademo2sonara"   ). %%control-driven 
qactor( qademo2sonarb , ctx_demo2sonarb, "it.unibo.qademo2sonarb.MsgHandle_Qademo2sonarb"   ). %%store msgs 
qactor( qademo2sonarb_ctrl , ctx_demo2sonarb, "it.unibo.qademo2sonarb.Qademo2sonarb"   ). %%control-driven 
%%% -------------------------------------------
%%% -------------------------------------------
qactor( qrdemo2robot , ctx_demo2robot, "it.unibo.qrdemo2robot.MsgHandle_Qrdemo2robot" ). 
qactor( qrdemo2robot_ctrl , ctx_demo2robot, "it.unibo.qrdemo2robot.Qrdemo2robot" ). 

