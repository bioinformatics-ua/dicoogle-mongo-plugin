@echo off
if [%1] == [] (
	echo USAGE :   store.bat dcimDirPath nbTest outFilePath
	echo NOTES :   outFilePath is optionnal, it is set to out.csv by default
	echo EXAMPLE : store.bat c:\DICOM_DATA\. 10 C:\Results\MongoDB.csv
	exit /b
)
if [%2] == [] (
	echo USAGE :   store.bat dcimDirPath nbTest outFilePath
	echo NOTES :   outFilePath is optionnal, it is set to out.csv by default
	echo EXAMPLE : store.bat c:\DICOM_DATA\. 10 C:\Results\MongoDB.csv
	exit /b
)
java -jar DICOMTester-FR.jar -dir add %1
for /l %%I in (1,1,%2) do (
	java -jar DICOMTester-FR.jar -run store -log temp.txt
)
java -jar DICOMTester-FR.jar -dir del %1
echo Press a key only when indexing is finished
pause
java -jar readtimetocsv-FR.jar temp.txt %3