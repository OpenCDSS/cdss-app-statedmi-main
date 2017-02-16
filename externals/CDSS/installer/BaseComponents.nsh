#####################################################
# Header file for use in all other CDSS installs
# for the base components.  
# BaseComponents includes the following:
#
#   shellcon.exe
#   CDSS.cfg
#   DATAUNIT
#
# OPERATION:  function first checks to make sure
#       that the current install is not newer than
#       the files being installed.  If the current
#       version is already up-to-date user should be
#       prompted and install of that file will abort 
#####################################################

Var cdss_dir
Var product
Var prod_found

Section "CDSS Base Components" BaseComponents

    strcpy $prod_found "0"

    # set the cursor to hour glass so user knows the application is installing
    SetCursor::System WAIT

    # set the Registry key value for CDSS
    #WriteRegStr HKLM "SOFTWARE\CDSS" Path $INSTDIR

    # make sure only newer files are installed
    SetOverwrite ifnewer

    # set main program directories
    strcpy $cdss_dir $INSTDIR
    
    # copy important bat/jar files for base CDSS functionality
    SetOutPath $cdss_dir\bin
    
    ## copy system files
    SetOutPath $cdss_dir\system
    File externals\CDSS\system\DATAUNIT
    
    # install the default CDSS.cfg file
    SetOverwrite off
    File externals\CDSS\system\CDSS.cfg
    
    # create the logs directory
    SetOutPath $cdss_dir
    CreateDirectory $cdss_dir\logs
    
    # create graphics directory
    #CreateDirectory $cdss_dir\graphics
    
    SetOverwrite ifnewer
    SetOutPath $cdss_dir\system
    File externals\CDSS\graphics\waterMark.bmp
    File externals\CDSS\graphics\waterMark.ico
    
    # Add $INSTDIR\bin to the PATH ENV Variable
    #Push "$INSTDIR\bin"
    #Call AddToPath
    
    # set the cursor back to normal
    SetCursor::System NORMAL
    
SectionEnd

Function un.BaseComponents


FunctionEnd

Function un.isProductInstalled

    !insertmacro IfKeyExists "HKLM" "Software\State of Colorado\CDSS" "$product"
    Pop $R0
    strcmp $R0 "1" 0 +2
    strcpy $prod_found "1"

FunctionEnd
