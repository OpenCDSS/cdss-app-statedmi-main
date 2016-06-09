!ifndef JRE_VERSION
  !define JRE_VERSION 142
!endif

!ifndef JRE_SRC_DIR
  !define JRE_SRC_DIR "${INST_BUILD_DIR}"
!endif

!ifndef JRE_EXCLUDES
  !define JRE_EXCLUDES "/x rmid.exe /x rmiregistry.exe /x tnameserv.exe /x keytool.exe /x kinit.exe /x klist.exe /x ktab.exe /x policytool.exe /x orbd.exe /x servertool.exe /x javaws* /x NPJ* /x JavaWebStart.dll /x jpi* /x deploy.jar /x plugin.jar"
!endif

# To prepare a jre for use
# 1) remove the $JRE/bin/client/classes.jsa file (if present)
# 2) execute "pack200 -J-Xmx256m $JRE/lib/rt.pack.gz $JRE/lib/rt.jar"
# 3) remove $JRE/lib/rt.jar
# if you are using a 1.4 jre you need to copy the unpack200.exe and dll from
# a 1.5 jre into the $JRE/bin directory

Function unpack
    Var /GLOBAL JREDIR
    StrCpy $JREDIR "$INSTDIR\jre_${JRE_VERSION}"

    IfFileExists "$JREDIR\lib\rt.jar" +3
        DetailPrint "Unpacking runtime - this may take a while"
        nsExec::Exec '"$JREDIR\bin\unpack200" "$JREDIR\lib\rt.pack.gz" "$JREDIR\lib\rt.jar"'
    Delete "$JREDIR\lib\rt.pack.gz"

    IfFileExists "$JREDIR\bin\client\classes.jsa" +3
        DetailPrint "Creating shared classes"
        nsExec::Exec '"$JREDIR\bin\java" -Xshare:dump'

    DetailPrint "Completed"
FunctionEnd

Section "Java™ Runtime Environment" JRE
    strcpy $choseJRE "1"

    # set the cursor to hour glass so user knows the application is installing
    SetCursor::System WAIT

    # make sure only newer files are installed
    SetOverwrite ifnewer
    # install JRE - if 142 include rt.jar, otherwise exclude it - we used a packed jar
    SetOutPath $INSTDIR
    !ifndef TEST
        !if ${JRE_VERSION} == "142"
            File /r /x *svn* "${JRE_SRC_DIR}\jre_${JRE_VERSION}"
        !else
            File /r /x rt.jar /x classes.jsa ${JRE_EXCLUDES} "${JRE_SRC_DIR}\jre_${JRE_VERSION}"
            # ugliness - if the jre doesn't contain a packed rt.jar, then we need to include
            # the original rt.jar - unpack will silently fail so this is OK.
            # For now, JRE_PACKED is being passed in via the ant build process since
            # so many nsis operations are a pain to do.
            !if ${JRE_PACKED} != "true"
                SetOutPath "$INSTDIR\jre_${JRE_VERSION}\lib"
                File "${JRE_SRC_DIR}\jre_${JRE_VERSION}\lib\rt.jar"
            !endif
        !endif
    !else
        File /r /x *.jar /x *.dll /x *zi* "${JRE_SRC_DIR}\jre_${JRE_VERSION}"
    !endif

    Call unpack
    
    # set the cursor back to normal
    SetCursor::System NORMAL
    
SectionEnd

Function un.JRE

  DetailPrint "Removing JRE"
  RmDir /r /REBOOTOK $INSTDIR\jre_${JRE_VERSION}

FunctionEnd