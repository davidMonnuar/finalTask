%====================================================================================
% Context cxt_demo1Robot  SYSTEM-configuration: file it.unibo.cxt_demo1Robot.demo1Sys.pl 
%====================================================================================
context(cxt_demo1robot, "172.20.10.3",  "TCP", "8079" ).  		 
context(cxt_demo1remoteconsole, "172.20.10.2",  "TCP", "8089" ).  		 
context(cxt_demo1sonara, "172.20.10.5",  "TCP", "8099" ).  		 
context(cxt_demo1sonarb, "172.20.10.4",  "TCP", "8069" ).  		 
%%% -------------------------------------------
qactor( qademo1console , cxt_demo1remoteconsole, "it.unibo.qademo1console.MsgHandle_Qademo1console"   ). %%store msgs 
qactor( qademo1console_ctrl , cxt_demo1remoteconsole, "it.unibo.qademo1console.Qademo1console"   ). %%control-driven 
qactor( qademo1sonara , cxt_demo1sonara, "it.unibo.qademo1sonara.MsgHandle_Qademo1sonara"   ). %%store msgs 
qactor( qademo1sonara_ctrl , cxt_demo1sonara, "it.unibo.qademo1sonara.Qademo1sonara"   ). %%control-driven 
qactor( qademo1sonarb , cxt_demo1sonarb, "it.unibo.qademo1sonarb.MsgHandle_Qademo1sonarb"   ). %%store msgs 
qactor( qademo1sonarb_ctrl , cxt_demo1sonarb, "it.unibo.qademo1sonarb.Qademo1sonarb"   ). %%control-driven 
%%% -------------------------------------------
%%% -------------------------------------------
qactor( qrdemo1robot , cxt_demo1robot, "it.unibo.qrdemo1robot.MsgHandle_Qrdemo1robot" ). 
qactor( qrdemo1robot_ctrl , cxt_demo1robot, "it.unibo.qrdemo1robot.Qrdemo1robot" ). 

