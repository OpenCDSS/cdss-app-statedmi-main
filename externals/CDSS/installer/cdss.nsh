#############################################################
# CDSS NSIS code for StateDMI:
# - this code could be generic but has been customized for StateDMI
##############################################################

# Users of this script should define the following

# Code that includes this script should define the following:
# - for example, the 'installer/CDSS/StateDMI.nsi' file defines preprocessor
#   values described below and then includes this file
#
# DISPLAYNAME - a nice display name, only used for text, etc. (e.g., "StateDMI")
# CODENAME[optional, defaults to DISPLAYNAME] - a "legal" name - should not have
#   special characters, etc. - used for paths, exe, etc.
# VERSION - some versioning string (e.g., "5.2.0")
#
# Optional functionality:
#
# - file associations:
#   - FILE_EXT - an extension that the application should open
#   - FILE_EXT_DESC - the description of that file
# - configure hydrobase:
#   - CONFIGURE_HYDROBASE - define this as "true" to allow user configuration of hydrobase
# - location of files for installer:
#   - INST_BUILD_DIR (e.g., "dist\install-CDSS", which is the local StateDMI build)
#
# Advanced functionality:
#
# - testing:
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
#    - the menu (e.g., StateDMI-10.01.01)
#    - the installation folder
#    - the registry key
# The pattern Name-Version works for everything except StateModGUI because
# StateMod has a bug that it picks up on the dash and treats like a command line option.
# Reset NAMEVERSION in the section (can't use StrCmp here).
!if $CODENAME == "StateModGUI"
    !define NAMEVERSION ${CODENAME}_${VERSION}
!else
    # This is preferable as it matches the historical use for installers:
    # - for example "StateDMI-5.2.0"
    !define NAMEVERSION ${CODENAME}-${VERSION}
!endif
!define REGKEY "Software\State of Colorado\CDSS\${NAMEVERSION}"
# Prior to StateDMI 5.2.0 used this.
#!define COMPANY OWF
#!define URL http://www.openwaterfoundation.org
# Now use OpenCDSS since it has its own website.
!define COMPANY "Colorado Department of Natural Resources"
!define URL "https://opencdss.state.co.us"
# The following was previously used with subversion version control and is now just a folder:
# - is now a folder in the cdss-app-tstool-main folder
# - TODO smalers 2023-07-28 maybe not needed?
!define EXTERNALS_DIR "externals"
# The following was previously used when multiple StateDMI installers were created:
# - TODO smalers 2023-07-28 maybe not needed?
!define INSTALL_IS_CDSS "true"
# Where the installer is built in the TSTool main repository.
!ifndef INST_BUILD_DIR
    !define INST_BUILD_DIR "dist\install"
!endif

SetCompressor lzma
BrandingText "OpenCDSS"

# Include files:
# - these are in the "C:\Program Files (x86)\NSIS" software folder

# User interface.
!include "UMUI.nsh"
# To manipulate the Windows registry.
!include "Registry.nsh"
# To manipulate text.
!include "TextReplace.nsh"
# To be able to use $(IF) for conditional checks.
!include "LogicLib.nsh"

# Global Variables.
Var StartMenuGroup
Var choseApp
Var choseDocs
Var choseJRE

# Indicate what type of user account is being used for installation:
# - 'user' or 'administrator'
# - initialized in the "-setInstallVariables" section
Var accountType
Var registryTree

# Installer attributes.

# Installer output file (the self-extracting installer executable).
OutFile "dist\${CODENAME}_CDSS_${VERSION}_Setup.exe"
# The default location for installing the software:
# - for example C:\CDSS\StateDMI-5.2.0
InstallDir "C:\CDSS\${NAMEVERSION}"
# See below for call to 'InstallDirRegKey based on the 'accountType'.
# Installation directory registry key.
# - TODO smalers 2023-04-30 what does 'Path' mean in the following?
# - TODO smalers InstallDirRegKey is not valid in a "Section" so how can the registry tree be made dynamic depending on user install type?
#                                  For now hard code HKCU for non-administrative and see how it goes.
#InstallDirRegKey HKLM "${REGKEY}" Path
InstallDirRegKey HKCU "${REGKEY}" Path

# Define the following to NOT require administrator privileges:
# - add this after includes because some of the NSIS software files must set to
#     RequestExecutionLevel admin (or highest)
# - creating files in C:\CDSS seem to not usually require for normal user
RequestExecutionLevel user

# Turn on verbose mode for troubleshooting.
#!verbose 4

# Modern User Interface (MUI) defines:
# - used to create dialogs, etc.
!define MUI_ICON "externals\CDSS\graphics\watermark.ico"
!define MUI_FINISHPAGE_NOAUTOCLOSE
# TODO smalers 2023-04-30 used to be KKLM for admin install:
# - need to figure out how to use $registryTree here for flexibility but no time right now to figure out,
#   try putting "registryTree" name without dollar sign
#!define MUI_STARTMENUPAGE_REGISTRY_ROOT registryTree
!define MUI_STARTMENUPAGE_REGISTRY_ROOT HKCU
!define MUI_STARTMENUPAGE_NODISABLE
!define MUI_STARTMENUPAGE_REGISTRY_KEY "${REGKEY}"
!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME StartMenuGroup
!define MUI_STARTMENUPAGE_DEFAULT_FOLDER CDSS
!define MUI_UNICON "externals\CDSS\graphics\watermark.ico"
!define MUI_UNFINISHPAGE_NOAUTOCLOSE
!define MUI_ABORTWARNING

# MUI Overrides for Text.
!define MUI_PAGE_HEADER_SUBTEXT "This wizard will guide you through the installation of ${DISPLAYNAME}"
!define MUI_WELCOMEPAGE_TEXT "The installation will be independent of other versions of ${DISPLAYNAME} software that have been previously installed, which allows multiple versions to be installed and run."
!define MUI_COMPONENTSPAGE_TEXT_TOP "Select the components to install by checking the corresponding boxes.  Click Next to continue."
!define MUI_COMPONENTSPAGE_TEXT_DESCRIPTION_INFO "Position the mouse over a component to view its description."
!define MUI_DIRECTORYPAGE_TEXT_TOP "Setup will install ${DISPLAYNAME} in the following folder.  C:\CDSS is the normal location, consistent with other files used with Colorado's Decision Support Systems (CDSS) software, and should not require administrative privileges.  It is recommended that a versioned software folder under the main CDSS folder be specified.  To install in a different folder, click Browse and select another folder.  Click Next to continue."
!define MUI_STARTMENUPAGE_DEFAULTFOLDER "CDSS"

### Use custom button text.
MiscButtonText "Back" "Next" "Cancel" "Done"

!addincludedir ..\cdss-util-buildtools\externals\NSIS_Common
#!include PathManipulation.nsh
!include RegisterExtension.nsh
!include RTiUtil.nsh
!include JRE.nsh
!addincludedir externals\CDSS\installer
!include BaseComponents.nsh

### UI pages (installation wizard dialogs) ###
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

# Installer language.
!insertmacro MUI_LANGUAGE English

ReserveFile "externals\CDSS\installer\server_name.ini"
!insertmacro MUI_RESERVEFILE_INSTALLOPTIONS


# Sections are run in the order that they appear below:
# - therefore, define variables early on as needed

##################################################################
# SECTION: -setInstallVariables
#
# Initializes some global variables:
#
#   choseApp, choseDocs 
#             - Used for dependencies in later sections:
#             0 = user chose not to install the application
#             1 = user chose to install docs
#   StartMenuGroup - sets the StartMenu Folder Name
#
# BRIEF:
#  The minus sign at the beginning of the section name
#  is used to make this a hidden section to the user
#  this means they cannot choose to not run it.
###################################################################
Section -setInstallVariables

    strcpy $StartMenuGroup "CDSS"
    # As of StateDMI 5.2.0 don't require admin rights so the default is to write to the current user registry tree:
    # - default value is supposed to be for the current user but set to make sure
    # The following was used when requiring administrator privileges
    #SetShellVarContext all
    SetShellVarContext current

    # Define the Windows registry tree used for install/uninstall properties:
    # - 'accountType' can be "user" or "administrator"
    # - currently default the account type to "user" but may need to detect from user input
    StrCpy $accountType "user"
    ${IF} $accountType == "user"
      # Normal user:
      # - the registry tree is writeable by the current user
      # - "CU" means "current user"
      StrCpy $registryTree "HKCU"
    ${ELSE}
      # Administrative user:
      # - the registry tree is writeable by an administrative user
      # - "LM" means "local machine"
      StrCpy $registryTree "HKLM"
    ${ENDIF}
    # For troubleshooting:
    !verbose push
    !verbose 4
    !echo "accountType=$accountType registryTree=$registryTree"
    !verbose pop

    IfSilent 0 +2
    Goto InstallDefaults
    Goto None

    InstallDefaults:
        StrCpy $choseApp "0"
        StrCpy $choseDocs "0"
        StrCpy $choseJRE "0"
        Goto End
    None:
        StrCpy $choseApp "1"
        StrCpy $choseDocs "1"
        StrCpy $choseJRE "1"
    End:

SectionEnd

########################################################
# SECTION: -Main
#
# BRIEF:
#  - used to write the Reg Key for the components
########################################################
Section -Main
    # As of StateDMI 5.2.0 don't require admin rights so the default is to write to the user registry.
    #WriteRegStr HKLM "${REGKEY}\Components" Main 1
    WriteRegStr HKCU "${REGKEY}\Components" Main 1
SectionEnd


##################################################
# SECTION: StateDMI
#
# BRIEF: 
#  - installs the StateDMI specific files
#  - these may change each release so the files included may need to be updated
#  - see also the conf/build-cdss.xml file for files/folders to include
#
##################################################
Section "${CODENAME}" ${CODENAME}

    # Set choseApp variable to true since it was chosen.
    strcpy $choseApp "1"

    # Copy important bat/jar files specific to this product.
    SetOverwrite ifnewer
    SetOutPath $INSTDIR

    CreateDirectory $INSTDIR\logs

    !ifndef TEST
        File /r "${INST_BUILD_DIR}\bin"
    !else
        File /r /x *.jar "${INST_BUILD_DIR}\bin"
    !endif

    #File "${INST_BUILD_DIR}\README.md"
    File /r "${INST_BUILD_DIR}\datastores"
    #File /nonfatal /r "${INST_BUILD_DIR}\examples"
    File /r "${INST_BUILD_DIR}\system"
    #File /nonfatal /r "${INST_BUILD_DIR}\python"
    
    # Insert the -home Directory into the .bat file according to the user's install location.
    ${textreplace::ReplaceInFile} "$INSTDIR\bin\${CODENAME}.bat" "$INSTDIR\bin\${CODENAME}.bat" "SET HOMED=\CDSS" "SET HOMED=$INSTDIR" "" $0
    
    # As of StateDMI 5.2.0 don't require admin rights so the default is to write to the current user registry tree.
    # Write some registry keys for StateDMI.
    #WriteRegStr HKLM "${REGKEY}" Path $INSTDIR
    #WriteRegStr HKLM "${REGKEY}" StartMenuGroup $StartMenuGroup
    WriteRegStr HKCU "${REGKEY}" Path $INSTDIR
    WriteRegStr HKCU "${REGKEY}" StartMenuGroup $StartMenuGroup
    SetOverwrite off
    WriteUninstaller $INSTDIR\Uninstall_${NAMEVERSION}.exe
    # As of StateDMI 5.2.0 don't require admin rights so the default is to write to the current user registry tree.
    #WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${NAMEVERSION}" DisplayName "${NAMEVERSION}"
    #WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${NAMEVERSION}" DisplayVersion "${VERSION}"
    #WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${NAMEVERSION}" Publisher "${COMPANY}"
    #WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${NAMEVERSION}" URLInfoAbout "${URL}"
    #WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${NAMEVERSION}" DisplayIcon $INSTDIR\Uninstall_${NAMEVERSION}.exe
    #WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${NAMEVERSION}" UninstallString $INSTDIR\Uninstall_${NAMEVERSION}.exe
    #WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${NAMEVERSION}" NoModify 1
    #WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${NAMEVERSION}" NoRepair 1

    WriteRegStr HKCU "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${NAMEVERSION}" DisplayName "${NAMEVERSION}"
    WriteRegStr HKCU "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${NAMEVERSION}" DisplayVersion "${VERSION}"
    WriteRegStr HKCU "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${NAMEVERSION}" Publisher "${COMPANY}"
    WriteRegStr HKCU "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${NAMEVERSION}" URLInfoAbout "${URL}"
    WriteRegStr HKCU "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${NAMEVERSION}" DisplayIcon $INSTDIR\Uninstall_${NAMEVERSION}.exe
    WriteRegStr HKCU "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${NAMEVERSION}" UninstallString $INSTDIR\Uninstall_${NAMEVERSION}.exe
    WriteRegDWORD HKCU "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${NAMEVERSION}" NoModify 1
    WriteRegDWORD HKCU "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${NAMEVERSION}" NoRepair 1

    !ifdef FILE_EXT
        ${registerExtension} "$INSTDIR\bin\${CODENAME}.exe" ".${FILE_EXT}" "${FILE_DESC}"
    !endif
   
SectionEnd


#########################################
# SECTION: Documentation
#
# BRIEF:
#  - installs current documentation for StateDMI
#  - documentation is now online, so a minimal README is installed locally
#
# The /o stands for optional.
# This allows the component page to uncheck this box by default.
#########################################
Section "Documentation" Docs

    # Set boolean choseDocs since documentation was selected.
    strcpy $choseDocs "1"

    # Copy documentation.
    SetOutPath $INSTDIR\doc
    SetOverwrite on

    !ifndef TEST
        File /r "${INST_BUILD_DIR}\doc\"
    !else
        FileOpen $0 "$INSTDIR\doc\blank.txt" w
        FileClose $0
    !endif

SectionEnd

##############################################
# SECTION: Start Menu Shortcuts
#
# BRIEF:
#  This section creates the start -> apps shortcuts as:
#
#  CDSS / StateDMI-Version -> run StateDMI
#  
##############################################
Section "Start Menu" StartMenu

    # Make sure user chose to install StateDMI.
    strcmp $choseApp "0" 0 +2
      Goto skipMenu
    
    !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
    
    # Shortcut added for launch of java program:
    # - 'SMPROGRAMS' is a built-in NSIS constant
    # - the shortcut is created in C:\ProgramData\Microsoft\Windows\Start Menu\Programs\CDSS\StateDMI-5.2.0.lnk
    # - see: https://nsis.sourceforge.io/Docs/Chapter4.html
    SetOutPath $SMPROGRAMS\$StartMenuGroup
    SetOutPath $INSTDIR\bin
    CreateShortCut "$SMPROGRAMS\$StartMenuGroup\${NAMEVERSION}.lnk" "$INSTDIR\bin\${CODENAME}.exe"
    
    # Shortcut for uninstall of program:
    # - as of StateDMI 5.00.00 2019-07-10 don't include the uninstall menu because it takes up space
    # - can remove using normal Windows software uninstall tool
    #SetOutPath $SMPROGRAMS\$StartMenuGroup\Uninstall
    #CreateShortcut "$SMPROGRAMS\$StartMenuGroup\Uninstall\${NAMEVERSION}.lnk" $INSTDIR\Uninstall_${NAMEVERSION}.exe
    
    skipMenu:
    
    # Make sure user chose to install docs.
    strcmp $choseDocs "0" 0 +2
      Goto Done
      
    # Shortcut for StateDMI documentation:
    # - as of StateDMI 5.00.00 2019-07-10 don't need this since it is on the web and accessible from StateDMI Help menu
    #SetOutPath $SMPROGRAMS\$StartMenuGroup\Documentation
    #CreateShortcut "$SMPROGRAMS\$StartMenuGroup\Documentation\${NAMEVERSION} User Manual.lnk" $INSTDIR\doc\UserManual\${CODENAME}.pdf
      
    !insertmacro MUI_STARTMENU_WRITE_END  
      
    Done:
    
SectionEnd


############################################
# SECTION: DesktopShortcut
#
# BRIEF:
#   Creates a desktop shortcut to start the application.
#
############################################
Section /o "Desktop Shortcut" DesktopShortcut

    # Make sure user chose to install application.
    strcmp $choseApp "0" 0 +2
      Goto skipShortcut
   
    # Installs shortcut on desktop.
    SetOutPath $INSTDIR\bin
    CreateShortCut "$DESKTOP\${NAMEVERSION}.lnk" "$INSTDIR\bin\${CODENAME}.exe"

    skipShortcut:

SectionEnd


###########################################
# SECTION: Uninstall
#
# BRIEF:
#   Deletes files and RegKeys.
###########################################
Section "Uninstall"

    # As of StateDMI 5.2.0 don't require admin rights so the default is to write to the current user registry tree:
    # - default value is supposed to be for the current user but set to make sure
    # The following was used when requiring administrator privileges
    #SetShellVarContext all
    SetShellVarContext current

    # Get the number of other CDSS installed programs.
    #Push "HKEY_LOCAL_MACHINE\Software\State Of Colorado\CDSS"
    #Call un.getNumSubKeys
    #Pop $R1
    #DetailPrint "Number Installed Comp:$R1"

    # Get the StartMenuFolder.
    !insertmacro MUI_STARTMENU_GETFOLDER Application $StartMenuGroup

    DetailPrint "Removing Menu Items and Links"
    # As of StateDMI 5.2.0 don't require admin rights so the default is to write to the current user registry tree.
    # Delete registry key for the uninstall so that the version won't be listed in Add/Remove programs.
    #DeleteRegKey HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${NAMEVERSION}.lnk"
    DeleteRegKey HKCU "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${NAMEVERSION}.lnk"
    # Comment the following since uninstall menu link should no longer be added as of 2019-07-10
    #Delete /REBOOTOK "$SMPROGRAMS\$StartMenuGroup\Uninstall\${NAMEVERSION}.lnk"
    # Comment the following since documentation menu link should no longer be added as of 2019-07-10
    #Delete /REBOOTOK "$SMPROGRAMS\$StartMenuGroup\Documentation\${NAMEVERSION} User Manual.lnk"
    RmDir /REBOOTOK "$SMPROGRAMS\$StartMenuGroup\Documentation"
    RmDir /REBOOTOK "$SMPROGRAMS\$StartMenuGroup\Uninstall"
    Delete /REBOOTOK "$SMPROGRAMS\$StartMenuGroup\${NAMEVERSION}.lnk"
    Delete /REBOOTOK "$INSTDIR\${NAMEVERSION}.lnk"
    Delete /REBOOTOK "$DESKTOP\${NAMEVERSION}.lnk"
    Delete /REBOOTOK $INSTDIR\Uninstall_${NAMEVERSION}.exe
    # TODO smalers 2023-07-28 the following does not seem to remove enough (leaves Components?)
    # and therefore is still shown in Windows Add/Remove programs.
    # As of StateDMI 5.2.0 don't require admin rights so the default is to write to the current user registry tree.
    # REGKEY includes StateDMI-5.2.0, for example.
    #DeleteRegValue HKLM "${REGKEY}" StartMenuGroup
    #DeleteRegValue HKLM "${REGKEY}" Path
    #DeleteRegKey /IfEmpty HKLM "${REGKEY}\Components"
    #DeleteRegKey /IfEmpty HKLM "${REGKEY}"
    DeleteRegValue HKCU "${REGKEY}" StartMenuGroup
    DeleteRegValue HKCU "${REGKEY}" Path
    DeleteRegKey /IfEmpty HKCU "${REGKEY}\Components"
    DeleteRegKey /IfEmpty HKCU "${REGKEY}"
    RmDir /REBOOTOK $SMPROGRAMS\$StartMenuGroup
    RmDir /REBOOTOK $SMPROGRAMS\CDSS
    # As of StateDMI 5.2.0 don't require admin rights so the default is to write to the current user registry tree.
    #DeleteRegValue HKLM "${REGKEY}\Components" Main
    DeleteRegValue HKCU "${REGKEY}\Components" Main
    
    DetailPrint "Removing ${CODENAME}"
    # Remove files from install directory.
    Delete /REBOOTOK $INSTDIR\README.md
    RmDir /r /REBOOTOK $INSTDIR\bin
    RmDir /r /REBOOTOK $INSTDIR\datastores
    RmDir /r /REBOOTOK $INSTDIR\doc
    #RmDir /r /REBOOTOK $INSTDIR\examples
    RmDir /r /REBOOTOK $INSTDIR\logs
    #RmDir /r /REBOOTOK $INSTDIR\python
    RmDir /r /REBOOTOK $INSTDIR\system
    
    !ifdef FILE_EXT
        DetailPrint "Unregister file extensions"
        ${unregisterExtension} "$INSTDIR\bin\${CODENAME}.exe" "${FILE_EXT}"
    !endif
    
    # Uninstall base components.
    Call un.BaseComponents
    Call un.JRE

    # DON'T DO /r HERE:
    # - why?  does the uninstall program need to be removed last?
    RmDir /REBOOTOK $INSTDIR
    RmDir /REBOOTOK $INSTDIR\..

SectionEnd


# TODO smalers 2023-04-30 why is this here and not at the start?
### Section Descriptions ###
!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${Docs} "Enabling this component will install ${DISPLAYNAME} documentation to the CDSS\${NAMEVERSION}\doc folder."
  !insertmacro MUI_DESCRIPTION_TEXT ${StateDMI} "Enabling this component will install ${DISPLAYNAME} software in the CDSS\${NAMEVERSION} folder."
  !insertmacro MUI_DESCRIPTION_TEXT ${StartMenu} "Enabling this component will install start menu folders."
  !insertmacro MUI_DESCRIPTION_TEXT ${DesktopShortcut} "Enabling this component will install a desktop shortcut to run the ${DISPLAYNAME} application."
  !insertmacro MUI_DESCRIPTION_TEXT ${BaseComponents} "Enabling this component will install the CDSS base components, including software and configuration files"
  !insertmacro MUI_DESCRIPTION_TEXT ${JRE} "Enabling this component will install the Java${U+2122} Runtime Environment (JRE), which is used to run CDSS software.  CDSS software uses its own copy of the JRE, regardless of what may already by installed elsewhere on the computer."
!insertmacro MUI_FUNCTION_DESCRIPTION_END


################################################
# FUNCTION: .onInstSuccess
#
# BRIEF: NSIS default function:
#  When installation is successful and the user clicks the close button,
#  this function is called.
#  It prompts the user to execute the program and view the readme.
################################################
Function .onInstSuccess

    SetOutPath $INSTDIR\bin
    
    # If 0 then user didn't choose to install the application.
    strcmp $choseApp "0" 0 +2
      Goto skipThis
    IfSilent 0 +2
      Goto skipThis
      
    ### Delete these comments to include a readme.

    #MessageBox MB_YESNO "Would you like to view the README?" IDYES yes IDNO no
    #yes:
    #  Exec 'notepad.exe $INSTDIR\TSTool_README.txt'
    #  Goto next2
    #no:
    #  DetailPrint "Skipping README"
    #next2:
    
    MessageBox MB_OK "Run ${CODENAME} from Start / CDSS / ${NAMEVERSION}"
    # For now don't allow running the application because the install is as root
    # and don't know how to get back to normal user.
    # MessageBox MB_YESNO "Would you like to run the program (if installer is run as administrator you will not have access to specific user configuration TSTool files)?" IDYES true IDNO false
    #true:
    #  Exec '"$INSTDIR\bin\${CODENAME}.exe"'
    #  Goto next
    #false:
    #  DetailPrint "User chose to not start application"
    #next:
                
    skipThis:
    
FunctionEnd


########################################
# FUNCTION: .onInit
#
# BRIEF:
# - NSIS default function
# - executes on Init of Outfile created
#
########################################
Function .onInit
    
    
    InitPluginsDir
    !insertmacro MUI_INSTALLOPTIONS_EXTRACT_AS "externals\CDSS\installer\server_name.ini" "server_name.ini"
    
    # Check user privileges and abort if not admin.
    ClearErrors
    UserInfo::GetName
    IfErrors Win9x
    Pop $0
    UserInfo::GetAccountType
    Pop $1
    ## ---- Start
    ## As of TSTool 14.8.0 don't require administrator rights.
    ## TODO smalers 2023-04-30 may need to reenable something if try to install in 'Program Files' or 'C:\CDSS' is not writeable.
    ##StrCmp $1 "Admin" 0 +3
    ##    #MessageBox MB_OK 'User "$0" is in the Administrators group'
    ##    Goto done
    ##StrCmp $1 "Power" 0 +3
    ##    #MessageBox MB_OK 'User "$0" is in the Power Users group'
    ##    Goto InsufficientRights
    ##StrCmp $1 "User" 0 +3
    ##    #MessageBox MB_OK 'User "$0" is just a regular user'
    ##    Goto InsufficientRights
    ##StrCmp $1 "Guest" 0 +3
    ##    #MessageBox MB_OK 'User "$0" is a guest'
    ##    Goto InsufficientRights
    ##MessageBox MB_OK "Unknown error"
    ## ---- End
    Goto done

    Win9x:
        # This one means you don't need to care about admin or not admin because Windows 9x doesn't either.
        IfSilent done
        MessageBox MB_OK "Error! This DLL can't run under Windows 9x!"
        Abort
        
    InsufficientRights:
        #IfSilent done
        # As of TSTool 14.8.0, don't require administrator privileges any more.
        #MessageBox MB_OK "You must log on using an account with administrator$\nprivileges to install this application."
        #Abort
        
    done:
    
    # Read the CDSS registry key.
    #ReadRegStr $0 ${REGKEY} "Path"
    
    # Check if the RegKey exists.
    #strcmp "$0" "" 0 +2
    #Goto noCDSSFound
    
    # Change the $INSTDIR to the path to the previously installed.
    #strcpy $INSTDIR $0
    
    #noCDSSFound:
    
FunctionEnd

