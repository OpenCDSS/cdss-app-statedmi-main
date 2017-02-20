!define TEMP1 $R0  
Var server_name
Var default_server_name
Var default_db_name
Var cdss_cfg
Var HWND
Var DLGITEM

Function SetCustomHydrobaseServer

  strcpy $cdss_cfg "$INSTDIR\system\CDSS.cfg"
  
  MessageBox MB_YESNO "Do you want to use a HydroBase database that is installed on another$\r$\n\
  computer or change the default HydroBase configuration on this$\r$\n\
  computer?$\r$\n$\r$\n\
  Select Yes if you know the name of an available HydroBase$\r$\n\
  server or need to run CDSS software in batch mode with a specific$\r$\n\
  HydroBase version." IDYES true IDNO false
   false:
   Goto skip
   true:
 
  # read old CDSS.cfg file if present for default info
  IfFileExists "$cdss_cfg" CDSSFileFound
     Goto noCDSSFile
   
  CDSSFileFound:
  # search for text in CDSS file needed to fill default values
  ReadINIStr $server_name "$cdss_cfg" HydroBase ServerNames
  ReadINIStr $default_server_name "$cdss_cfg" HydroBase DefaultServerName
  ReadINIStr $default_db_name "$cdss_cfg" HydroBase DefaultDatabaseName
  WriteINIStr "$PLUGINSDIR\server_name.ini" "Field 2" "State" "$server_name"
  WriteINIStr "$PLUGINSDIR\server_name.ini" "Field 4" "State" "$default_server_name"
  WriteINIStr "$PLUGINSDIR\server_name.ini" "Field 6" "State" "$default_db_name"
  
  Goto promptUser
  
  # no CDSS file found so set defaults
  noCDSSFile:
  strcpy $server_name ""
  strcpy $default_server_name ""
  strcpy $default_db_name ""
  
  # ask user for input
  promptUser:
  !insertmacro MUI_HEADER_TEXT "Configure HydroBase Settings" "Specify the HydroBase servers for software"
  !insertmacro MUI_INSTALLOPTIONS_DISPLAY "server_name.ini"
  
  # get the user input string for the external DB
  ReadINIStr ${TEMP1} "$PLUGINSDIR\server_name.ini" "Field 2" "State"
  strcpy $server_name ${TEMP1}
  ReadINIStr ${TEMP1} "$PLUGINSDIR\server_name.ini" "Field 4" "State"
  strcpy $default_server_name ${TEMP1}
  ReadINIStr ${TEMP1} "$PLUGINSDIR\server_name.ini" "Field 6" "State"
  strcpy $default_db_name ${TEMP1}
  
  # default the two servers to local and db to HydroBase if user supplied no input
  strcmp $server_name "" 0 +2
    strcpy $server_name "local"
  strcmp $default_server_name "" 0 +2
    strcpy $default_server_name "local"
  strcmp $default_db_name "" 0 +2
    strcpy $default_db_name "HydroBase"
  
  # replace/rewrite CDSS.cfg file 
  ClearErrors
  FileOpen $0 $cdss_dir\system\CDSS.cfg w
  IfErrors finish
  FileWrite $0 "# Configuration file for CDSS (Colorado's Decision Support Systems)$\r$\n"
  FileWrite $0 "#$\r$\n"
  FileWrite $0 "# This file stores shared information for the system, including:$\r$\n"
  FileWrite $0 "#$\r$\n"
  FileWrite $0 "# * HydroBase database properties, defining connection defaults.$\r$\n"
  FileWrite $0 "# * Satellite Monitoring System (ColoradoSMS) properties, defining connection$\r$\n"
  FileWrite $0 "#   defaults.$\r$\n"
  FileWrite $0 "$\r$\n"
  FileWrite $0 "[HydroBase]$\r$\n"
  FileWrite $0 "$\r$\n"
  FileWrite $0 "ServerNames=$\"$server_name$\"$\r$\n"
  FileWrite $0 "DefaultServerName=$\"$default_server_name$\"$\r$\n"
  FileWrite $0 "DefaultDatabaseName=$\"$default_db_name$\"$\r$\n"
  FileClose $0
  finish:
  
 
  # user chose not to configure an external DB
  skip:  
    

FunctionEnd

Function ValidateCustom

  ReadINIStr ${TEMP1} "$PLUGINSDIR\server_name.ini" "Field 2" "State"
  StrCmp ${TEMP1} 1 done
  
  done:
  
FunctionEnd