@echo off

dir
echo %date%
REM date format: 2017/09/19 週二
rem set dd=%date:~8,2%
rem set dm=%date:~5,2%
rem set dy=%date:~0,4%

REM for double click batch file in Windows
REM date format: 週二 2017/09/19
set dd=%date:~11,2%
set dm=%date:~8,2%
set dy=%date:~3,4%
set dateNow=%dy%%dm%%dd%

echo %time%
rem time format: 17:57:19.08
set th=%time:~0,2%
if "%th:~0,1%" == " " set th=0%th:~1,1%
set tm=%time:~3,2%
set ts=%time:~6,2%
set timeNow=%th%%tm%%ts%

path C:\Program Files\7-Zip
7z a -tzip src_%dateNow%_%timeNow%.zip G:\AStudio-workspace\LiteNote2017-betaStage\app\src\

rem PAUSE
copy G:\AStudio-workspace\LiteNote2017-betaStage\app\src_%dateNow%_%timeNow%.zip G:\GoogleDrive\Git\Android\LiteNote2017\
del G:\AStudio-workspace\LiteNote2017-betaStage\app\src_%dateNow%_%timeNow%.zip