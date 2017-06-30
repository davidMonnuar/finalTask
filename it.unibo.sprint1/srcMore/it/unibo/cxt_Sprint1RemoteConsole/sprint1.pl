%====================================================================================
% Context cxt_Sprint1RemoteConsole  SYSTEM-configuration: file it.unibo.cxt_Sprint1RemoteConsole.sprint1.pl 
%====================================================================================
context(cxt_sprint1robot, "localhost",  "TCP", "8079" ).  		 
context(cxt_sprint1remoteconsole, "localhost",  "TCP", "8089" ).  		 
context(cxt_sprint1sonar, "localhost",  "TCP", "8099" ).  		 
%%% -------------------------------------------
qactor( qasprint1robot , cxt_sprint1robot, "it.unibo.qasprint1robot.MsgHandle_Qasprint1robot"   ). %%store msgs 
qactor( qasprint1robot_ctrl , cxt_sprint1robot, "it.unibo.qasprint1robot.Qasprint1robot"   ). %%control-driven 
qactor( qasprint1console , cxt_sprint1remoteconsole, "it.unibo.qasprint1console.MsgHandle_Qasprint1console"   ). %%store msgs 
qactor( qasprint1console_ctrl , cxt_sprint1remoteconsole, "it.unibo.qasprint1console.Qasprint1console"   ). %%control-driven 
qactor( qasprint1sonar , cxt_sprint1sonar, "it.unibo.qasprint1sonar.MsgHandle_Qasprint1sonar"   ). %%store msgs 
qactor( qasprint1sonar_ctrl , cxt_sprint1sonar, "it.unibo.qasprint1sonar.Qasprint1sonar"   ). %%control-driven 
%%% -------------------------------------------
%%% -------------------------------------------

