@echo off
if [%1] == [] goto :USAGE

for /l %%I in (1,1,%x%) do (
	if [%3] == [] java -jar DICOMTester-EN.jar -run query
	if not [%3] == [] java -jar DICOMTester-EN.jar -run query -log %3
)
java -jar readLogToCsv-EN.jar %2 %3
exit /b

:USAGE
	echo USAGE :   runQuery.bat nbTest outFilePath inFilePath
	echo NOTES :   outFilePath and inFilePath are optionnal
	echo           You can specify only outFilePath or both
	echo               If you specify inFilePath, this file will not be deleted
	echo               Else it will
	echo DEFAULT : outFilePath : out.csv
	echo           inFilePath : queryLog.txt
	echo EXAMPLE : runQuery.bat 10 MongoDB.csv resultLog.txt