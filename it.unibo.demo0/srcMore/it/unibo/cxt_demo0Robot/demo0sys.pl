%====================================================================================
% Context cxt_demo0Robot  SYSTEM-configuration: file it.unibo.cxt_demo0Robot.demo0Sys.pl 
%====================================================================================
context(cxt_demo0robot, "172.20.10.3",  "TCP", "8079" ).  		 
context(cxt_demo0remoteconsole, "172.20.10.2",  "TCP", "8089" ).  		 
context(cxt_demo0sonara, "172.20.10.2",  "TCP", "8099" ).  		 
context(cxt_demo0sonarb, "172.20.10.2",  "TCP", "8069" ).  		 
%%% -------------------------------------------
qactor( qademo0console , cxt_demo0remoteconsole, "it.unibo.qademo0console.MsgHandle_Qademo0console"   ). %%store msgs 
qactor( qademo0console_ctrl , cxt_demo0remoteconsole, "it.unibo.qademo0console.Qademo0console"   ). %%control-driven 
qactor( qademo0sonara , cxt_demo0sonara, "it.unibo.qademo0sonara.MsgHandle_Qademo0sonara"   ). %%store msgs 
qactor( qademo0sonara_ctrl , cxt_demo0sonara, "it.unibo.qademo0sonara.Qademo0sonara"   ). %%control-driven 
qactor( qademo0sonarb , cxt_demo0sonarb, "it.unibo.qademo0sonarb.MsgHandle_Qademo0sonarb"   ). %%store msgs 
qactor( qademo0sonarb_ctrl , cxt_demo0sonarb, "it.unibo.qademo0sonarb.Qademo0sonarb"   ). %%control-driven 
%%% -------------------------------------------
%%% -------------------------------------------
qactor( qrdemo0robot , cxt_demo0robot, "it.unibo.qrdemo0robot.MsgHandle_Qrdemo0robot" ). 
qactor( qrdemo0robot_ctrl , cxt_demo0robot, "it.unibo.qrdemo0robot.Qrdemo0robot" ). 

