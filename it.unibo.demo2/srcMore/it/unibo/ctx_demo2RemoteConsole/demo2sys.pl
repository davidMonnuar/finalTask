%====================================================================================
% Context ctx_demo2RemoteConsole standalone= SYSTEM-configuration: file it.unibo.ctx_demo2RemoteConsole.demo2Sys.pl 
%====================================================================================
context(ctx_demo2remoteconsole, "172.20.10.2",  "TCP", "8089" ).  		 
%%% -------------------------------------------
qactor( qademo2console , ctx_demo2remoteconsole, "it.unibo.qademo2console.MsgHandle_Qademo2console"   ). %%store msgs 
qactor( qademo2console_ctrl , ctx_demo2remoteconsole, "it.unibo.qademo2console.Qademo2console"   ). %%control-driven 
