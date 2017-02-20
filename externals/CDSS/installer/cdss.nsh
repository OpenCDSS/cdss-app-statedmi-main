#############################################################
# AUTHOR: Ian Schneider
# DATE: March 17, 2008
#
# Extract common installation bits from CDSS installers to
# simplify the installers.
# 
# @todo - extract relevant bits to even more generic nsh
#
##############################################################

# Users of this script should define the following

# DISPLAYNAME - a nice display name, only used for text, etc.
# CODENAME[optional, defaults to DISPLAYNAME] - a "legal" name - should not have
#   special characters, etc. - used for paths, exe, etc.
# VERSION - some versioning string
#
# optional functionality:
#
# * file associations:
#   -FILE_EXT - an extension that the application should open
#   -FILE_EXT_DESC - the description of that file
# * configure hydrobase:
#   -CONFIGURE_HYDROBASE - define this to allow user configuration of hydrobase
#
# advanced functionality:
# 
# * testing:
#   - define TEST to avoid installation of certain components to allow nsis
#     compilation to run faster

!ifndef CODENAME
  !define CODENAME ${DISPLAYNAME}
!endif
!ifndef VERSION
    !error "VERSION must be defined"
!endif

Name "${DISPLAYNAME}"
# NAMEVERSION is used for:
#    -the menu (e.g., TSTool-10.01.01)
#    -the installation folder
#    -the registry key
# The pattern Name-Version works for everything except StateModGUI because
# StateMod has a bug that it picks up on the dash and treats like a command line option
# Reset NAMEVERSION in the section (can't use StrCmp here)
!if $CODENAME == "StateModGUI"
    !define NAMEVERSION ${CODENAME}_${VERSION}
!else
    # This is preferable as it matches the historical use for installers
    !define NAMEVERSION ${CODENAME}-${VERSION}
!endif
!define REGKEY "Software\State of Colorado\CDSS\${NAMEVERSION}"
!define COMPANY OWF
!define URL http://www.openwaterfoundation.org
!define EXTERNALS_DIR "externals"
!define INSTALL_IS_CDSS "true"
!ifndef INST_BUILD_DIR
    !define INST_BUILD_DIR "dist\install"
!endif

SetCompressor lzma
BrandingText "Open Water Foundation"

# Included files
!include "UMUI.nsh"
!include "Registry.nsh"
!include "TextReplace.nsh"

# Global Variables
Var StartMenuGroup
Var choseApp
Var choseDocs
Var choseJRE

# Installer attributes
OutFile "dist\${CODENAME}_CDSS_${VERSION}_Setup.exe"
InstallDir "C:\CDSS\${NAMEVERSION}"
InstallDirRegKey HKLM "${REGKEY}" Path

# MUI defines
!define MUI_ICON "externals\CDSS\graphics\watermark.ico"
!define MUI_FINISHPAGE_NOAUTOCLOSE
!define MUI_STARTMENUPAGE_REGISTRY_ROOT HKLM
!define MUI_STARTMENUPAGE_NODISABLE
!define MUI_STARTMENUPAGE_REGISTRY_KEY "${REGKEY}"
!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME StartMenuGroup
!define MUI_STARTMENUPAGE_DEFAULT_FOLDER CDSS
!define MUI_UNICON "externals\CDSS\graphics\watermark.ico"
!define MUI_UNFINISHPAGE_NOAUTOCLOSE
!define MUI_ABORTWARNING

# MUI Overrides for Text
!define MUI_PAGE_HEADER_SUBTEXT "This wizard will guide you through the installation of ${DISPLAYNAME}"
!define MUI_WELCOMEPAGE_TEXT "The installation will be independent of other versions of ${DISPLAYNAME} software that have been previously installed."
!define MUI_COMPONENTSPAGE_TEXT_TOP "Select the components to install by checking the corresponding boxes.  Click Next to continue."
!define MUI_COMPONENTSPAGE_TEXT_DESCRIPTION_INFO "Position the mouse over a component to view its description."
!define MUI_DIRECTORYPAGE_TEXT_TOP "Setup will install ${DISPLAYNAME} in the following folder.  It is recommended that a versioned software folder under the main CDSS folder be specified.  To install in a different folder, click Browse and select another folder.  Click Next to continue."
!define MUI_STARTMENUPAGE_DEFAULTFOLDER "CDSS"

### Use custom button text
MiscButtonText "Back" "Next" "Cancel" "Done"

!addincludedir ..\cdss-util-buildtools\externals\NSIS_Common
#!include PathManipulation.nsh
!include RegisterExtension.nsh
!include RTiUtil.nsh
!include JRE.nsh
!addincludedir externals\CDSS\installer
!include BaseComponents.nsh

### Pages ###
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "installer\CDSS\License.txt"
!insertmacro MUI_PAGE_COMPONENTS
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_STARTMENU Application $StartMenuGroup
!insertmacro MUI_PAGE_INSTFILES  
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!ifdef CONFIGURE_HYDROBASE
    !include server_name.nsh
    Page custom SetCustomHydrobaseServer
!endif

# Installer language
!insertmacro MUI_LANGUAGE English

ReserveFile "externals\CDSS\installer\server_name.ini"
!insertmacro MUI_RESERVEFILE_INSTALLOPTIONS



##################################################################
# SECTION: -setInstallVariables
# 
# initializes some global variables
#   choseApp, choseDocs 
#             - Used for dependencies in later sections
#             0 = user chose not to install app,
#             1 = user chose to install docs
#   StartMenuGroup - sets the StartMenu Folder Name
#  
# BRIEF:
#  The minus sign at the beginning of the section name
#  is used to make this a hidden section to the user
#  this means they cannot choose to not run it
###################################################################
Section -setInstallVariables

    strcpy $StartMenuGroup "CDSS"
    SetShellVarContext all

    IfSilent 0 +2
    Goto InstallDefaults
    Goto None

    InstallDefaults:
        strcpy $choseApp "0"
        strcpy $choseDocs "0"
        strcpy $choseJRE "0"
        Goto End
    None:
        strcpy $choseApp "1"
        strcpy $choseDocs "1"
        strcpy $choseJRE "1"
    End:
    
SectionEnd

########################################################
# SECTION: -Main
# 
# BRIEF:
#  used to write the Reg Key for the components
########################################################
Section -Main
    WriteRegStr HKLM "${REGKEY}\Components" Main 1
SectionEnd


##################################################
# SECTION: TSTool
#
# BRIEF: 
#  Installs the TSTool specific files.
#  These may change each release so
#  the files included may need to be 
#  updated. 
#
##################################################
Section "${CODENAME}" ${CODENAME}

    # set choseApp variable to true since it was chosen
    strcpy $choseApp "1"
    
    # copy important bat/jar files specific to this product
    SetOverwrite ifnewer
    SetOutPath $INSTDIR

    CreateDirectory $INSTDIR\logs

    !ifndef TEST
        File /r "${INST_BUILD_DIR}\bin"
    !else
        File /r /x *.jar "${INST_BUILD_DIR}\bin"
    !endif

    File /nonfatal /r "${INST_BUILD_DIR}\examples"
    File /r "${INST_BUILD_DIR}\system"
    File /nonfatal /r "${INST_BUILD_DIR}\python"
    
    # Insert the -home Directory into the .bat file
    # according to the user's install location
    ${textreplace::ReplaceInFile} "$INSTDIR\bin\${CODENAME}.bat" "$INSTDIR\bin\${CODENAME}.bat" "SET HOMED=\CDSS" "SET HOMED=$INSTDIR" "" $0
    
    # Write some registry keys for TSTool
    WriteRegStr HKLM "${REGKEY}" Path $INSTDIR
    WriteRegStr HKLM "${REGKEY}" StartMenuGroup $StartMenuGroup
    SetOverwrite off
    WriteUninstaller $INSTDIR\Uninstall_${NAMEVERSION}.exe
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${NAMEVERSION}" DisplayName "${NAMEVERSION}"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${NAMEVERSION}" DisplayVersion "${VERSION}"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${NAMEVERSION}" Publisher "${COMPANY}"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${NAMEVERSION}" URLInfoAbout "${URL}"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${NAMEVERSION}" DisplayIcon $INSTDIR\Uninstall_${NAMEVERSION}.exe
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${NAMEVERSION}" UninstallString $INSTDIR\Uninstall_${NAMEVERSION}.exe
    WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${NAMEVERSION}" NoModify 1
    WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${NAMEVERSION}" NoRepair 1

    !ifdef FILE_EXT
        ${registerExtension} "$INSTDIR\bin\${CODENAME}.exe" ".${FILE_EXT}" "${FILE_DESC}"
    !endif
   
SectionEnd


#########################################
# SECTION: Documentation
#
# BRIEF:
#  Installs current documentation   
#  for TSTool to C:\CDSS\doc\TSTool
#
# The /o stands for optional.  This
# allows the component page to uncheck
# this box by default.   
#########################################
Section "Documentation" Docs
   
    # set boolean choseDocs since documentation was selected
    strcpy $choseDocs "1"
    
    # copy documentation
    SetOutPath $INSTDIR\doc
    SetOverwrite on
    
    !ifndef TEST
        File /r "${INST_BUILD_DIR}\doc\"
    !else
        FileOpen $0 $INSTDIR\doc\blank.txt" w
        FileClose $0
    !endif

SectionEnd

##############################################
# SECTION: Start Menu Shortcuts
#
# BRIEF: 
#  This section creates the start -> apps
#  shortcuts as CDSS -> TSTool -> uninstall
#                             -> run TSTool
#  
##############################################
Section "Start Menu" StartMenu

    # make sure user chose to install TSTool
    strcmp $choseApp "0" 0 +2
      Goto skipMenu
    
    !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
    
    # Shortcut added for launch of java program
    SetOutPath $SMPROGRAMS\$StartMenuGroup
    SetOutPath $INSTDIR\bin
    CreateShortCut "$SMPROGRAMS\$StartMenuGroup\${NAMEVERSION}.lnk" "$INSTDIR\bin\${CODENAME}.exe"
    
    # Shortcut for uninstall of program
    SetOutPath $SMPROGRAMS\$StartMenuGroup\Uninstall
    CreateShortcut "$SMPROGRAMS\$StartMenuGroup\Uninstall\${NAMEVERSION}.lnk" $INSTDIR\Uninstall_${NAMEVERSION}.exe
    
    skipMenu:
    
    # make sure user chose to install docs
    strcmp $choseDocs "0" 0 +2
      Goto Done
      
    # Shortcut for TSTool documentation
    SetOutPath $SMPROGRAMS\$StartMenuGroup\Documentation
    CreateShortcut "$SMPROGRAMS\$StartMenuGroup\Documentation\${NAMEVERSION} User Manual.lnk" $INSTDIR\doc\UserManual\${CODENAME}.pdf
      
    !insertmacro MUI_STARTMENU_WRITE_END  
      
    Done:
    
SectionEnd


############################################
# SECTION: DesktopShortcut
#
# BRIEF:
#  should be pretty obvious what this does.
#  If its not, you shouldn't be reading this
#  
############################################
Section /o "Desktop Shortcut" DesktopShortcut

    # make sure user chose to install app
    strcmp $choseApp "0" 0 +2
      Goto skipShortcut
   
    # Installs shortcut on desktop
    SetOutPath $INSTDIR\bin
    CreateShortCut "$DESKTOP\${NAMEVERSION}.lnk" "$INSTDIR\bin\${CODENAME}.exe"

    skipShortcut:

SectionEnd


###########################################
# SECTION: Uninstall
#
# BRIEF: Deletes files and RegKeys
###########################################
Section "Uninstall"

    SetShellVarContext all

    # get the number of other CDSS installed programs
    #Push "HKEY_LOCAL_MACHINE\Software\State Of Colorado\CDSS"
    #Call un.getNumSubKeys
    #Pop $R1
    #DetailPrint "Number Installed Comp:$R1"

    # Get the StartMenuFolder
    !insertmacro MUI_STARTMENU_GETFOLDER Application $StartMenuGroup

    DetailPrint "Removing Menu Items and Links"
    # delete registry and StartMenu stuff
    DeleteRegKey HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${NAMEVERSION}.lnk"
    Delete /REBOOTOK "$SMPROGRAMS\$StartMenuGroup\Uninstall\${NAMEVERSION}.lnk"
    Delete /REBOOTOK "$SMPROGRAMS\$StartMenuGroup\Documentation\${NAMEVERSION} User Manual.lnk"
    RmDir /REBOOTOK "$SMPROGRAMS\$StartMenuGroup\Documentation"
    RmDir /REBOOTOK "$SMPROGRAMS\$StartMenuGroup\Uninstall"
    Delete /REBOOTOK "$SMPROGRAMS\$StartMenuGroup\${NAMEVERSION}.lnk"
    Delete /REBOOTOK "$INSTDIR\${NAMEVERSION}.lnk"
    Delete /REBOOTOK "$DESKTOP\${NAMEVERSION}.lnk"
    Delete /REBOOTOK $INSTDIR\Uninstall_${NAMEVERSION}.exe
    DeleteRegValue HKLM "${REGKEY}" StartMenuGroup
    DeleteRegValue HKLM "${REGKEY}" Path
    DeleteRegKey /IfEmpty HKLM "${REGKEY}\Components"
    DeleteRegKey /IfEmpty HKLM "${REGKEY}"
    RmDir /REBOOTOK $SMPROGRAMS\$StartMenuGroup
    RmDir /REBOOTOK $SMPROGRAMS\CDSS
    DeleteRegValue HKLM "${REGKEY}\Components" Main
    
    DetailPrint "Removing ${CODENAME}"
    # Remove files from install directory
    RmDir /r /REBOOTOK $INSTDIR\bin
    RmDir /r /REBOOTOK $INSTDIR\doc
    RmDir /r /REBOOTOK $INSTDIR\examples
    RmDir /r /REBOOTOK $INSTDIR\logs
    RmDir /r /REBOOTOK $INSTDIR\python
    RmDir /r /REBOOTOK $INSTDIR\system
    
    !ifdef FILE_EXT
        DetailPrint "Unregister file extensions"
        ${unregisterExtension} "$INSTDIR\bin\${CODENAME}.exe" "${FILE_EXT}"
    !endif
    
    # uninstall base components
    Call un.BaseComponents
    Call un.JRE

    # DON'T DO /r HERE!!!
    RmDir /REBOOTOK $INSTDIR
    RmDir /REBOOTOK $INSTDIR\..

SectionEnd


### Section Descriptions ###
!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${Docs} "Enabling this component will install ${DISPLAYNAME} documentation to the CDSS\${NAMEVERSION}\doc folder."
  !insertmacro MUI_DESCRIPTION_TEXT ${TSTool} "Enabling this component will install ${DISPLAYNAME} software in the CDSS\${NAMEVERSION} folder."
  !insertmacro MUI_DESCRIPTION_TEXT ${StartMenu} "Enabling this component will install start menu folders."
  !insertmacro MUI_DESCRIPTION_TEXT ${DesktopShortcut} "Enabling this component will install a desktop shortcut to run the ${DISPLAYNAME} application."
  !insertmacro MUI_DESCRIPTION_TEXT ${BaseComponents} "Enabling this component will install the CDSS base components, including software and configuration files"
  !insertmacro MUI_DESCRIPTION_TEXT ${JRE} "Enabling this component will install the Java™ Runtime Environment (JRE), which is used to run CDSS software.  CDSS software uses its own copy of the JRE, regardless of what may already by installed elsewhere on the computer."
!insertmacro MUI_FUNCTION_DESCRIPTION_END


################################################
# FUNCTION: .onInstSuccess
#
# BRIEF: NSIS default function
#  When installation is successful and the
#  user clicks the close button, this function
#  is called.  It prompts the user to execute
#  the program and view the readme
################################################
Function .onInstSuccess

    SetOutPath $INSTDIR\bin
    
    # if 0 then user didn't choose to install the app
    strcmp $choseApp "0" 0 +2
      Goto skipThis
    IfSilent 0 +2
      Goto skipThis
      
    ### delete these comments to include a readme
    #MessageBox MB_YESNO "Would you like to view the README?" IDYES yes IDNO no
    #yes:
    #  Exec 'notepad.exe $INSTDIR\TSTool_README.txt'
    #  Goto next2
    #no:
    #  DetailPrint "Skipping README"
    #next2:
    
    MessageBox MB_YESNO "Would you like to run the program?" IDYES true IDNO false
    true:
      Exec '"$INSTDIR\bin\${CODENAME}.exe"'
      Goto next
    false:
      DetailPrint "User chose to not start application"
    next:
                
    skipThis:
    
FunctionEnd


########################################
# FUNCTION: .onInit
#
# BRIEF: NSIS default function
#  executes on Init of Outfile created
#
########################################
Function .onInit
    
    
    InitPluginsDir
    !insertmacro MUI_INSTALLOPTIONS_EXTRACT_AS "externals\CDSS\installer\server_name.ini" "server_name.ini"
    
    # check user privileges and abort if not admin
    ClearErrors
    UserInfo::GetName
    IfErrors Win9x
    Pop $0
    UserInfo::GetAccountType
    Pop $1
    StrCmp $1 "Admin" 0 +3
        #MessageBox MB_OK 'User "$0" is in the Administrators group'
        Goto done
    StrCmp $1 "Power" 0 +3
        #MessageBox MB_OK 'User "$0" is in the Power Users group'
        Goto InsufficientRights
    StrCmp $1 "User" 0 +3
        #MessageBox MB_OK 'User "$0" is just a regular user'
        Goto InsufficientRights
    StrCmp $1 "Guest" 0 +3
        #MessageBox MB_OK 'User "$0" is a guest'
        Goto InsufficientRights
    MessageBox MB_OK "Unknown error"
    Goto done

    Win9x:
        # This one means you don't need to care about admin or
        # not admin because Windows 9x doesn't either
        IfSilent done
        MessageBox MB_OK "Error! This DLL can't run under Windows 9x!"
        Abort
        
    InsufficientRights:
        IfSilent done
        MessageBox MB_OK "You must log on using an account with administrator$\nprivileges to install this application."
        Abort
        
    done:
    
    # read the CDSS registry key
    #ReadRegStr $0 ${REGKEY} "Path"
    
    # check if the RegKey exists
    #strcmp "$0" "" 0 +2
    #Goto noCDSSFound
    
    # change the $INSTDIR to the path to the previously installed  
    #strcpy $INSTDIR $0
    
    #noCDSSFound:
    
FunctionEnd

