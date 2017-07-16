%====================================================================================
% Context cxt_paRemoteConsole standalone= SYSTEM-configuration: file it.unibo.cxt_paRemoteConsole.paSys.pl 
%====================================================================================
context(cxt_paremoteconsole, "192.168.137.1",  "TCP", "8089" ).  		 
%%% -------------------------------------------
qactor( qapaconsole , cxt_paremoteconsole, "it.unibo.qapaconsole.MsgHandle_Qapaconsole"   ). %%store msgs 
qactor( qapaconsole_ctrl , cxt_paremoteconsole, "it.unibo.qapaconsole.Qapaconsole"   ). %%control-driven 
