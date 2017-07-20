%====================================================================================
% Context cxt_demo1RemoteConsole standalone= SYSTEM-configuration: file it.unibo.cxt_demo1RemoteConsole.demo1Sys.pl 
%====================================================================================
context(cxt_demo1remoteconsole, "172.20.10.2",  "TCP", "8089" ).  		 
%%% -------------------------------------------
qactor( qademo1console , cxt_demo1remoteconsole, "it.unibo.qademo1console.MsgHandle_Qademo1console"   ). %%store msgs 
qactor( qademo1console_ctrl , cxt_demo1remoteconsole, "it.unibo.qademo1console.Qademo1console"   ). %%control-driven 
