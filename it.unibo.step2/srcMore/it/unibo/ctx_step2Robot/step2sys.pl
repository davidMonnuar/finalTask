%====================================================================================
% Context ctx_step2Robot  SYSTEM-configuration: file it.unibo.ctx_step2Robot.step2Sys.pl 
%====================================================================================
context(ctx_step2robot, "172.20.10.3",  "TCP", "8079" ).  		 
context(ctx_step2remoteconsole, "172.20.10.2",  "TCP", "8089" ).  		 
context(ctx_step2sonara, "172.20.10.5",  "TCP", "8099" ).  		 
context(ctx_step2sonarb, "172.20.10.4",  "TCP", "8069" ).  		 
%%% -------------------------------------------
qactor( qastep2console , ctx_step2remoteconsole, "it.unibo.qastep2console.MsgHandle_Qastep2console"   ). %%store msgs 
qactor( qastep2console_ctrl , ctx_step2remoteconsole, "it.unibo.qastep2console.Qastep2console"   ). %%control-driven 
qactor( qastep2sonara , ctx_step2sonara, "it.unibo.qastep2sonara.MsgHandle_Qastep2sonara"   ). %%store msgs 
qactor( qastep2sonara_ctrl , ctx_step2sonara, "it.unibo.qastep2sonara.Qastep2sonara"   ). %%control-driven 
qactor( qastep2sonarb , ctx_step2sonarb, "it.unibo.qastep2sonarb.MsgHandle_Qastep2sonarb"   ). %%store msgs 
qactor( qastep2sonarb_ctrl , ctx_step2sonarb, "it.unibo.qastep2sonarb.Qastep2sonarb"   ). %%control-driven 
%%% -------------------------------------------
%%% -------------------------------------------
qactor( qrstep2robot , ctx_step2robot, "it.unibo.qrstep2robot.MsgHandle_Qrstep2robot" ). 
qactor( qrstep2robot_ctrl , ctx_step2robot, "it.unibo.qrstep2robot.Qrstep2robot" ). 

