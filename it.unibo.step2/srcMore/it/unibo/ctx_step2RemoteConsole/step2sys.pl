%====================================================================================
% Context ctx_step2RemoteConsole standalone= SYSTEM-configuration: file it.unibo.ctx_step2RemoteConsole.step2Sys.pl 
%====================================================================================
context(ctx_step2remoteconsole, "172.20.10.2",  "TCP", "8089" ).  		 
%%% -------------------------------------------
qactor( qastep2console , ctx_step2remoteconsole, "it.unibo.qastep2console.MsgHandle_Qastep2console"   ). %%store msgs 
qactor( qastep2console_ctrl , ctx_step2remoteconsole, "it.unibo.qastep2console.Qastep2console"   ). %%control-driven 
