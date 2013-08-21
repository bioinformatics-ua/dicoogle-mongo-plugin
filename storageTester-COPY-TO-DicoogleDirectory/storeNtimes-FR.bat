@echo off
if [%1] == [] (
	echo USAGE :   store.bat nbSameDir dcimDirPath nbTest outFilePath
	echo NOTES :   outFilePath is optionnal, it is set to out.csv by default
	echo EXAMPLE : store.bat 3 c:\DICOM_DATA\. 10 C:\Results\MongoDB.csv
	exit /b
)
if [%2] == [] (
	echo USAGE :   store.bat nbSameDir dcimDirPath nbTest outFilePath
	echo NOTES :   outFilePath is optionnal, it is set to out.csv by default
	echo EXAMPLE : store.bat 3 c:\DICOM_DATA\. 10 C:\Results\MongoDB.csv
	exit /b
)
for /l %%I in (1,1,%1) do (
	java -jar DICOMTester-FR.jar -dir add %2
)
for /l %%I in (1,1,%3) do (
	java -jar DICOMTester-FR.jar -run store -log temp.txt
)
for /l %%I in (1,1,%1) do (
	java -jar DICOMTester-FR.jar -dir del %2
)
echo Press a key only when indexing is finished
pause
java -jar readtimetocsv-FR.jar temp.txt %4