%====================================================================================
% Context cxt_paSonar  SYSTEM-configuration: file it.unibo.cxt_paSonar.paSys.pl 
%====================================================================================
context(cxt_parobot, "192.168.137.2",  "TCP", "8079" ).  		 
context(cxt_paremoteconsole, "192.168.137.1",  "TCP", "8089" ).  		 
context(cxt_pasonar, "192.168.137.1",  "TCP", "8099" ).  		 
%%% -------------------------------------------
qactor( qapaconsole , cxt_paremoteconsole, "it.unibo.qapaconsole.MsgHandle_Qapaconsole"   ). %%store msgs 
qactor( qapaconsole_ctrl , cxt_paremoteconsole, "it.unibo.qapaconsole.Qapaconsole"   ). %%control-driven 
qactor( qapasonar , cxt_pasonar, "it.unibo.qapasonar.MsgHandle_Qapasonar"   ). %%store msgs 
qactor( qapasonar_ctrl , cxt_pasonar, "it.unibo.qapasonar.Qapasonar"   ). %%control-driven 
%%% -------------------------------------------
%%% -------------------------------------------
qactor( qrparobot , cxt_parobot, "it.unibo.qrparobot.MsgHandle_Qrparobot" ). 
qactor( qrparobot_ctrl , cxt_parobot, "it.unibo.qrparobot.Qrparobot" ). 

