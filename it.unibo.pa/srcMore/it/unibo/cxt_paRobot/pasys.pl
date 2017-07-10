%====================================================================================
% Context cxt_paRobot  SYSTEM-configuration: file it.unibo.cxt_paRobot.paSys.pl 
%====================================================================================
context(cxt_parobot, "localhost",  "TCP", "8079" ).  		 
context(cxt_paremoteconsole, "localhost",  "TCP", "8089" ).  		 
context(cxt_pasonar, "localhost",  "TCP", "8099" ).  		 
%%% -------------------------------------------
qactor( qapaconsole , cxt_paremoteconsole, "it.unibo.qapaconsole.MsgHandle_Qapaconsole"   ). %%store msgs 
qactor( qapaconsole_ctrl , cxt_paremoteconsole, "it.unibo.qapaconsole.Qapaconsole"   ). %%control-driven 
qactor( qapasonar , cxt_pasonar, "it.unibo.qapasonar.MsgHandle_Qapasonar"   ). %%store msgs 
qactor( qapasonar_ctrl , cxt_pasonar, "it.unibo.qapasonar.Qapasonar"   ). %%control-driven 
%%% -------------------------------------------
%%% -------------------------------------------
qactor( qrparobot , cxt_parobot, "it.unibo.qrparobot.MsgHandle_Qrparobot" ). 
qactor( qrparobot_ctrl , cxt_parobot, "it.unibo.qrparobot.Qrparobot" ). 

