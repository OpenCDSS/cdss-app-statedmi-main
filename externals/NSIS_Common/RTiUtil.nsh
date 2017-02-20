 # Util.nsh                                   
 #######################################################
 # AUTHOR: Kurt Tometich
 # DATE: 8/23/2006
 #
 # BRIEF:
 #  This is a header file that provides 
 #  utility functions for other .nsi scripts
 #  
 # FUNCTIONS: 
 #  FileModifiedDate - finds last modified date of file
 #  VersionCheckV5 - checks two versions (or numbers)
 #                   to see which is larger/newer
 #  CompareFileModificationDates - compares two
 #                   file modification dates and
 #                   returns which is newer
 #  ReadFromFile - sets up filehandle and reads line
 #                 from file
 #  WriteToFile  - sets up filehand and writes line
 #                 to a file
 #  UninstallCDSS - removes base components of CDSS
 #  
 #  
 ########################################################
 
 !macro VersionCheckV5 Ver1 Ver2 OutVar
 Push "${Ver1}"
 Push "${Ver2}"
 Call VersionCheckV5
 Pop "${OutVar}"
!macroend
!define VersionCheckV5 "!insertmacro VersionCheckV5"
 
Var year
Var cur_year
Var month
Var cur_month
Var day
Var cur_day
Var cur_file
Var dow
Var hour
Var minute
Var seconds
Var install_file
Var AccessReturnVal
Var IO_result
Var numSubKeys


    
#######################################################
# PRE: need to push "path/filename" onto stack
#   ex: push "C:\CDSS\file.txt"
#
# SYNTAX: Call FileModifiedDate
#
#
# POST: returns a number of dates and times as seen below
#
#        Pop "$cur_day" ;Variable (for day)
#        Pop "$cur_month" ;Variable (for year)
#        Pop "$cur_year" ;Variable (for day)
#        Pop "$4" ;Variable (for day of week name)
#        Pop "$5" ;Variable (for hour)
#        Pop "$6" ;Variable (for minute)
#        Pop "$7" ;Variable (for seconds) 
#
#######################################################    
Function FileModifiedDate
  DetailPrint "staring FileModifiedDate"
  # Prepare variables
  Exch $0
  Push $1
  Push $2
  Push $3
  Push $4
  Push $5
  Push $6
  Push $7
  Push $R0
 
  # Original System Example (with no dependencies)
 
  ; create WIN32_FIND_DATA struct
  System::Call '*(i, l, l, l, i, i, i, i, &t260, &t14) i .r2'
 
  ; Find file info
  System::Call 'kernel32::FindFirstFileA(t, i) i(r0, r2) .r3'
 
  ; ok?
  IntCmp $3 -1 sgfst_exit
 
    ; close file search
    System::Call 'kernel32::FindClose(i) i(r3)'
 
    ; Create systemtime struct for local time
    System::Call '*(&i2, &i2, &i2, &i2, &i2, &i2, &i2, &i2) i .R0'
  
    ; Get File time
    System::Call '*$2(i, l, l, l, i, i, i, i, &t260, &t14) i (,,, .r3)'
 
    ; Convert file time (UTC) to local file time
    System::Call 'kernel32::FileTimeToLocalFileTime(*l, *l) i(r3, .r1)'
 
    ; Convert file time to system time
    System::Call 'kernel32::FileTimeToSystemTime(*l, i) i(r1, R0)'
 
    sgfst_exit:
 
  System::Call '*$R0(&i2, &i2, &i2, &i2, &i2, &i2, &i2, &i2)i \
  (.r4, .r5, .r3, .r6, .r2, .r1, .r0,)'
 

 
  # Day of week: convert to name
  StrCmp $3 0 0 +3
    StrCpy $3 Sunday
      Goto WeekNameEnd
  StrCmp $3 1 0 +3
    StrCpy $3 Monday
      Goto WeekNameEnd
  StrCmp $3 2 0 +3
    StrCpy $3 Tuesday
      Goto WeekNameEnd
  StrCmp $3 3 0 +3
    StrCpy $3 Wednesday
      Goto WeekNameEnd
  StrCmp $3 4 0 +3
    StrCpy $3 Thursday
      Goto WeekNameEnd
  StrCmp $3 5 0 +3
    StrCpy $3 Friday
      Goto WeekNameEnd
  StrCmp $3 6 0 +2
    StrCpy $3 Saturday
  WeekNameEnd:
 
  # Minute: convert to 2 digits format
    IntCmp $1 9 0 0 +2
      StrCpy $1 '0$1'
    
  # Second: convert to 2 digits format
    IntCmp $0 9 0 0 +2
      StrCpy $0 '0$0'
 
  # Return to user
  Pop $R0
  Exch $6
  Exch
  Exch $5
  Exch
  Exch 2
  Exch $4
  Exch 2
  Exch 3
  Exch $3
  Exch 3
  Exch 4
  Exch $2
  Exch 4
  Exch 5
  Exch $1
  Exch 5
  Exch 6
  Exch $0
  Exch 6
 
 DetailPrint "ending FileModifiedDate"
 
FunctionEnd

##########################################################
# PRE: need to send it correct params   
# SYNTAX: ${VersionCheckV5} "4.2" "$2.5" "$R0"
# POST: result is stored in $R0
#  
# 0: Versions are equal
# 1: Version 1 is newer
# 2: Version 2 is newer
#########################################
Function VersionCheckV5
 Exch $R0 ; second version number
 Exch
 Exch $R1 ; first version number
 Push $R2
 Push $R3
 Push $R4
 Push $R5 ; second version part
 Push $R6 ; first version part
 
  StrCpy $R1 $R1.
  StrCpy $R0 $R0.
 
 Next: StrCmp $R0$R1 "" 0 +3
  StrCpy $R0 0
  Goto Done
 
  StrCmp $R0 "" 0 +2
   StrCpy $R0 0.
  StrCmp $R1 "" 0 +2
   StrCpy $R1 0.
 
 StrCpy $R2 0
  IntOp $R2 $R2 + 1
  StrCpy $R4 $R1 1 $R2
  StrCmp $R4 . 0 -2
    StrCpy $R6 $R1 $R2
    IntOp $R2 $R2 + 1
    StrCpy $R1 $R1 "" $R2
 
 StrCpy $R2 0
  IntOp $R2 $R2 + 1
  StrCpy $R4 $R0 1 $R2
  StrCmp $R4 . 0 -2
    StrCpy $R5 $R0 $R2
    IntOp $R2 $R2 + 1
    StrCpy $R0 $R0 "" $R2
 
 IntCmp $R5 0 Compare
 IntCmp $R6 0 Compare
 
 StrCpy $R3 0
  StrCpy $R4 $R6 1 $R3
  IntOp $R3 $R3 + 1
  StrCmp $R4 0 -2
 
 StrCpy $R2 0
  StrCpy $R4 $R5 1 $R2
  IntOp $R2 $R2 + 1
  StrCmp $R4 0 -2
 
 IntCmp $R3 $R2 0 +2 +4
 Compare: IntCmp 1$R5 1$R6 Next 0 +3
 
  StrCpy $R0 1
  Goto Done
  StrCpy $R0 2
 
 Done:
 Pop $R6
 Pop $R5
 Pop $R4
 Pop $R3
 Pop $R2
 Pop $R1
 Exch $R0 ; output
FunctionEnd


########################################################################
### PRE: Assumes the following
### $cur_file = path to current filename
### $install_file = path to install file
########################################################################
### POST: ($AccessReturnVal contains 0 or 1 as return value)
### $R7 = 0 : current version is older, new can be installed
### $R7 = 1 : current version is newer, cannot install
########################################################################
Function CompareFileModificationDates

strcpy $AccessReturnVal "1"  ;set to fail, unless otherwises passes all tests

### call modified date function with file to get access date
Push "$cur_file" ;File or folder
        # Verify if the file exists
        IfFileExists $cur_file FileExists
          SetErrors
          Goto End
        
        FileExists:
         Call FileModifiedDate
         Pop "$cur_day" ;Variable (for day)
         Pop "$cur_month" ;Variable (for year)
         Pop "$cur_year" ;Variable (for day)
         Pop "$dow" ;Variable (for day of week name)
         Pop "$hour" ;Variable (for hour)
         Pop "$minute" ;Variable (for minute)
         Pop "$seconds" ;Variable (for seconds) 
         Goto +4
        
        # File doesn't exist, then set all to zero
        End:
         strcpy $cur_day "0"
         strcpy $cur_month "0"
         strcpy $cur_year "0"
        
        
        #MessageBox MB_OK "file:$cur_file   $cur_month/$cur_day/$cur_year"


Push "$install_file" ;File or folder
        # Verify if file exists
        IfFileExists $install_file FileExists2
          SetErrors
          Goto End2
        
        FileExists2:
         Call FileModifiedDate
         Pop "$day" ;Variable (for day)
         Pop "$month" ;Variable (for month)
         Pop "$year" ;Variable (for year)
         Pop "$dow" ;Variable (for day of week name)
         Pop "$hour" ;Variable (for hour)
         Pop "$minute" ;Variable (for minute)
         Pop "$seconds" ;Variable (for seconds) 
         Goto +4
        
         # File doesn't exist, then set all to zero
        End2:
         strcpy $day "1"
         strcpy $month "1"
         strcpy $year "1"
        
        
        #MessageBox MB_OK "file:$install_file  $month/$day/$year"



### first check the year
${VersionCheckV5} "$year" "$cur_year" "$R0"
      StrCmp $R0 "2" +2 0
      Goto +2
      Goto skip
      
      ### Don't care about equality at the year level
      ### don't worry about that until we get down to days

### Check the Month
      ${VersionCheckV5} "$month" "$cur_month" "$R0"
      StrCmp $R0 "2" +2 0
      Goto +2
      Goto skip
      
      ### Don't care about equality at the year level
      ### don't worry about that until we get down to days

### Check the Day
      ${VersionCheckV5} "$day" "$cur_day" "$R0"
      StrCmp $R0 "2" +2 0
      Goto +2
      Goto skip
      
      StrCmp $R0 "0" +3 0
      strcpy $AccessReturnVal "0" ; passed, new version can be installed
      Goto +2
      Goto skip
      
        
### skip is used to skip the rest of this function
### if either the year or month of current version is newer
### than the year or month of file being installed
skip:

FunctionEnd

#########################################
# PRE: set $0 to filename
# SYNTAX: Call ReadFromFile
# POST: sets $cur_version to version in 
# $ver_file and if file is not found then
# $cur_version is set to 0
#########################################
Function ReadFromFile
    
    # first check version installed vs. version installing
    ClearErrors
    FileOpen $0 $INSTDIR\$0 r
    IfErrors done1
    FileRead $0 $IO_result
    FileClose $0
    goto +2
    
    # if version file not found then set version to 0
    done1:
      StrCpy $IO_result 0
      
FunctionEnd


#####################################################
# PRE: $0 holds filename
#      $1 holds String to write to file 
# SYNTAX: Call WriteToFile
# POST: 
#####################################################
Function WriteToFile

    ClearErrors
    FileOpen $0 $INSTDIR\$0 w
    IfErrors done
    FileWrite $0 "$1"
    FileClose $0
    
    done:
    strcpy $IO_result "0" 

FunctionEnd

Function UninstallCDSS

    Delete /REBOOTOK $INSTDIR\bin\msutil.jar
    Delete /REBOOTOK $INSTDIR\bin\mssqlserver.jar
    Delete /REBOOTOK $INSTDIR\bin\msbase.jar
    Delete /REBOOTOK $INSTDIR\bin\HydroBaseDMI_142.jar
    Delete /REBOOTOK $INSTDIR\bin\RTI_142.jar
    Delete /REBOOTOK $INSTDIR\bin\shellcon.exe
    Delete /REBOOTOK $INSTDIR\system\DATAUNIT
    Delete /REBOOTOK $INSTDIR\system\cdss.cfg
    RmDir /r /REBOOTOK $INSTDIR\jre_142
    RmDir /r /REBOOTOK $INSTDIR\bin
    RmDir /r /REBOOTOK $INSTDIR\system
    
FunctionEnd    

#####################################################
# PRE: $R0 holds the Key Value to search
#      
# SYNTAX: Call getNumSubKeys
# POST: $R1 returns the number of subkeys
#####################################################
#Function un.getNumSubKeys

    # get the parameter
    # $0 -> KeyPath
 #   Exch $R0
    
 #   IntOp $numSubKeys 0 + 0
    #${registry::Open} "HKEY_LOCAL_MACHINE\Software\State Of Colorado\CDSS" "" $0
 #   ${registry::Open} "$R0" "" $0
 #   StrCmp $0 0 0 loop
 #   MessageBox MB_OK "Regkey:$R0 not found" IDOK close

 #   loop:
 #   ${registry::Find} "$0" $1 $2 $3 $4
 #   StrCmp $4 '' close
 #   StrCmp $4 'REG_KEY' 0 +3
 #   IntOp $numSubKeys $numSubKeys + 1
 #   DetailPrint "$R1 $4:$1\$2"
 #   goto +2
 #   DetailPrint "$R1 $4:$1 $2=$3"
 #   goto loop

 #   close:
 #  ${registry::Close} "$0"
 #  ${registry::Unload}

 #  StrCpy $R1 "$numSubKeys"
    
    # Return the number of subKeys found
  #  Exch $R1

#FunctionEnd


####################################################
# This function will search in a file for the      #
# specified string, and return some values.        #
####################################################
Function FileSearch

Exch $0 ;search for
Exch
Exch $1 ;input file
Push $2
Push $3
Push $4
Push $5
Push $6
Push $7
Push $8
Push $9
Push $R0
  FileOpen $2 $1 r
  StrLen $4 $0
  StrCpy $5 0
  StrCpy $7 no
  StrCpy $8 0
  StrCpy $9 0
  ClearErrors
loop_main:
  FileRead $2 $3
  IfErrors done
 IntOp $R0 $R0 + $9
  StrCpy $9 0
  StrCpy $5 0
filter_top:
 IntOp $5 $5 - 1
  StrCpy $6 $3 $4 $5
  StrCmp $6 "" loop_main
  StrCmp $6 $0 0 filter_top
  StrCpy $3 $3 $5
  StrCpy $5 0
 StrCpy $7 yes
 StrCpy $9 1
 IntOp $8 $8 + 1
Goto filter_top
done:
  FileClose $2
  StrCpy $0 $8
  StrCpy $1 $7
  StrCpy $2 $R0
Pop $R0
Pop $9
Pop $8
Pop $7
Pop $6
Pop $5
Pop $4
Pop $3
Exch $2 ;output number of lines
Exch
Exch $1 ;output yes/no
Exch 2
Exch $0 ;output count found

FunctionEnd

##############################################
# Checks for the existence of a registry key
# Usage:
#  !insertmacro IfKeyExists "ROOT" "KeyToCheckIn" "KeyToCheck"
#  Pop $R0
#  $R0 contains 0 (not present) or 1 (present)
##############################################
!macro IfKeyExists ROOT MAIN_KEY KEY
push $R0
push $R1

!define Index 'Line${__LINE__}'

StrCpy $R1 "0"

"${Index}-Loop:"
; Check for Key
EnumRegKey $R0 ${ROOT} "${MAIN_KEY}" "$R1"
StrCmp $R0 "" "${Index}-False"
  IntOp $R1 $R1 + 1
  StrCmp $R0 "${KEY}" "${Index}-True" "${Index}-Loop"

"${Index}-True:"
;Return 1 if found
push "1"
goto "${Index}-End"

"${Index}-False:"
;Return 0 if not found
push "0"
goto "${Index}-End"

"${Index}-End:"
!undef Index
exch 2
pop $R0
pop $R1
!macroend