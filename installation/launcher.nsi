; Java Launcher
;--------------
 
;You want to change the next four lines
Name "RapidMiner"
Caption "RapidMiner Launcher"
Icon "rapidminer_icon.ico"
OutFile "../release/windows_installer/rapidminer/RapidMiner.exe"
 
SilentInstall silent
AutoCloseWindow true
ShowInstDetails nevershow
 
Section ""

System::Alloc 32
Pop $1
System::Call "Kernel32::GlobalMemoryStatus(i) v (r1)"
System::Call "*$1(&i4 .r2, &i4 .r3, &i4 .r4, &i4 .r5, \
                  &i4 .r6, &i4.r7, &i4 .r8, &i4 .r9)"
System::Free $1
;DetailPrint "Structure size (useless): $2 Bytes"
;DetailPrint "Memory load: $3%"
;DetailPrint "Total physical memory: $4 Bytes"
;DetailPrint "Free physical memory: $5 Bytes"
;DetailPrint "Total page file: $6 Bytes"
;DetailPrint "Free page file: $7 Bytes"
;DetailPrint "Total virtual: $8 Bytes"
;DetailPrint "Free virtual: $9 Bytes"

; for Xmx and Xms
IntOp $R9 $5 / 1024
IntOp $R9 $R9 / 1024
IntOp $R9 $R9 * 90
IntOp $R9 $R9 / 100
IntCmp $R9 64 less64 less64 more64
less64: 
StrCpy $R9 64
Goto mem_more
more64:
Goto mem_more

mem_more:
IntCmp $R9 1200 less1200 less1200 more1200
less1200:
Goto after_mem_more
more1200: 
StrCpy $R9 1200
Goto after_mem_more

after_mem_more:
; for Xmn
;IntOp $R8 $R9 / 3

;DetailPrint "90% of free: $R9 M"

  Call GetJRE
  Pop $R0
 
  ; change for your purpose (-jar etc.)
  ;StrCpy $0 '"$R0" -classpath "${CLASSPATH}" ${CLASS}'
  
  ; invoking RapidMiner via rapidminer.jar
  StrCpy $0 '"$R0" -Xmx$R9m -Xms$R9m -classpath "${CLASSPATH}" -Drapidminer.home=. -Drapidminer.operators.additional="${RAPIDMINER_OPERATORS_ADDITIONAL}" -jar lib/rapidminer.jar'
 
  ; invoking RapidMiner via start script
  ;System::Call 'Kernel32::SetEnvironmentVariableA(t, t) i("MAX_JAVA_MEMORY", "$R9").r0'
  ;StrCpy $0 'scripts\RapidMinerGUI.bat'
  
  SetOutPath $EXEDIR
  Exec $0
SectionEnd
 
Function GetJRE
;
;  Find JRE (javaw.exe)
;  1 - in .\jre directory (JRE Installed with application)
;  2 - in JAVA_HOME environment variable
;  3 - in the registry
;  4 - assume javaw.exe in current dir or PATH
 
  Push $R0
  Push $R1
 
  ClearErrors
  StrCpy $R0 "$EXEDIR\jre\bin\javaw.exe"
  IfFileExists $R0 JreFound
  StrCpy $R0 ""
 
  ClearErrors
  ReadEnvStr $R0 "JAVA_HOME"
  StrCpy $R0 "$R0\bin\javaw.exe"
  IfErrors 0 JreFound
 
  ClearErrors
  ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
  StrCpy $R0 "$R0\bin\javaw.exe"
 
  IfErrors 0 JreFound
  StrCpy $R0 "javaw.exe"
        
 JreFound:
  Pop $R1
  Exch $R0
FunctionEnd