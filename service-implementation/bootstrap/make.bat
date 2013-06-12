@echo off


            NOTE NEEDS UPDATE FOR PATHS - SEE .sh for the model


SET BOOTSTRAP=.\docs\assets\css\bootstrap.css
SET BOOTSTRAP_LESS=.\less\bootstrap.less
SET BOOTSTRAP_RESPONSIVE=.\docs\assets\css\bootstrap-responsive.css
SET BOOTSTRAP_RESPONSIVE_LESS=.\less\responsive.less
SET "CHECK= "
SET HR=##################################################

SETLOCAL ENABLEDELAYEDEXPANSION

if [%1]==[] goto build
if /i "%~1"=="test" goto test
if /i "%~1"=="build" goto build
if /i "%~1"=="bootstrap" goto bootstrap
if /i "%~1"=="gh-pages" goto gh-pages
if /i "%~1"=="watch" goto watch
if /i "%~1"=="haunt" goto haunt
if /i "%~1"==".PHONY" goto .PHONY
echo Unknown make action "%~1"
goto end

::
:: BUILD DOCS
::

:build
echo:
echo %HR%
echo Building Bootstrap...
echo %HR%
echo:

<nul set /p =Running JSHint on javascript...
SET "FILES= "
for %%x in (.\js\*.js) do SET FILES=!FILES! .\js\%%~nx.js
for %%x in (.\js\tests\unit\*.js) do SET FILES=!FILES! .\js\tests\unit\%%~nx.js
call jshint %FILES% --config .\js\.jshintrc
echo %CHECK% Done.

<nul set /p =Compiling LESS with Recess...
call recess --compile %BOOTSTRAP_LESS% > %BOOTSTRAP%
call recess --compile %BOOTSTRAP_RESPONSIVE_LESS% > %BOOTSTRAP_RESPONSIVE%
echo %CHECK% Done.

<nul set /p =Compiling documentation...
call node .\docs\build
echo %CHECK% Done.
<nul set /p =Compiling and minifying javascript...
copy /Y .\img\* .\docs\assets\img\>nul
copy /Y .\js\*.js .\docs\assets\js\>nul
copy /Y .\js\tests\vendor\jquery.js .\docs\assets\js\>nul
copy /B .\js\bootstrap-transition.js+.\js\bootstrap-alert.js+.\js\bootstrap-button.js+.\js\bootstrap-carousel.js+.\js\bootstrap-collapse.js+.\js\bootstrap-dropdown.js+.\js\bootstrap-modal.js+.\js\bootstrap-tooltip.js+.\js\bootstrap-popover.js+.\js\bootstrap-scrollspy.js+.\js\bootstrap-tab.js+.\js\bootstrap-typeahead.js+.\js\bootstrap-affix.js .\docs\assets\js\bootstrap.js>nul
call uglifyjs -nc .\docs\assets\js\bootstrap.js > .\docs\assets\js\bootstrap.min.tmp.js
(
echo /** && ^
echo.* Bootstrap.js by @fat ^& @mdo && ^
echo.* Copyright 2012 Twitter, Inc. && ^
echo.* http://www.apache.org/licenses/LICENSE-2.0.txt && ^
echo.*/
) > .\docs\assets\js\copyright.js
copy /B .\docs\assets\js\copyright.js+.\docs\assets\js\bootstrap.min.tmp.js .\docs\assets\js\bootstrap.min.js>nul
del /F /Q .\docs\assets\js\copyright.js .\docs\assets\js\bootstrap.min.tmp.js
echo %CHECK% Done.

echo:
echo %HR%
echo Bootstrap successfully built at %TIME%.
echo %HR%
echo:
echo Thanks for using Bootstrap,
echo ^<3 @mdo and @fat
echo:

goto end

::
:: RUN JSHINT & QUNIT TESTS IN PHANTOMJS
::

:test
SET "FILES= "
for %%x in (.\js\*.js) do SET FILES=!FILES! .\js\%%~nx.js
for %%x in (.\js\tests\unit\*.js) do SET FILES=!FILES! .\js\tests\unit\%%~nx.js
call jshint %FILES% --config .\js\.jshintrc
start node .\js\tests\server.js && ^
call phantomjs .\js\tests\phantom.js "http://localhost:3000/js/tests"
SET PID=unknown
for /F "tokens=*" %%r IN ('type .\js\tests\pid.txt') do SET PID=%%r
if /i not "%PID%"=="unknown" call taskkill /PID %PID%>nul
if exist ".\js\tests\pid.txt" del /F /Q .\js\tests\pid.txt
goto end

::
:: BUILD SIMPLE BOOTSTRAP DIRECTORY
:: recess & uglifyjs are required
::

:bootstrap
if not exist ".\bootstrap\img" md .\bootstrap\img
if not exist ".\bootstrap\css" md .\bootstrap\css
if not exist ".\bootstrap\js" md .\bootstrap\js
copy /Y .\img\* .\bootstrap\img\>nul
call recess --compile %BOOTSTRAP_LESS% > .\bootstrap\css\bootstrap.css
call recess --compress %BOOTSTRAP_LESS% > .\bootstrap\css\bootstrap.min.css
call recess --compile %BOOTSTRAP_RESPONSIVE_LESS% > .\bootstrap\css\bootstrap-responsive.css
call recess --compress %BOOTSTRAP_RESPONSIVE_LESS% > .\bootstrap\css\bootstrap-responsive.min.css
copy /B .\js\bootstrap-transition.js+.\js\bootstrap-alert.js+.\js\bootstrap-button.js+.\js\bootstrap-carousel.js+.\js\bootstrap-collapse.js+.\js\bootstrap-dropdown.js+.\js\bootstrap-modal.js+.\js\bootstrap-tooltip.js+.\js\bootstrap-popover.js+.\js\bootstrap-scrollspy.js+.\js\bootstrap-tab.js+.\js\bootstrap-typeahead.js+.\js\bootstrap-affix.js .\bootstrap\js\bootstrap.js>nul
call uglifyjs -nc .\bootstrap\js\bootstrap.js > .\bootstrap\js\bootstrap.min.tmp.js
(
echo /** && ^
echo.* Bootstrap.js by @fat ^& @mdo && ^
echo.* Copyright 2012 Twitter, Inc. && ^
echo.* http://www.apache.org/licenses/LICENSE-2.0.txt && ^
echo.*/
) > .\bootstrap\js\copyright.js
copy /B .\bootstrap\js\copyright.js+.\bootstrap\js\bootstrap.min.tmp.js .\bootstrap\js\bootstrap.min.js>nul
del /F /Q .\bootstrap\js\copyright.js .\bootstrap\js\bootstrap.min.tmp.js
goto end

::
:: MAKE FOR GH-PAGES 4 FAT & MDO ONLY (O_O )
::

:gh-pages
call :build
call :bootstrap
if exist ".\docs\assets\bootstrap.zip" del /F /Q .\docs\assets\bootstrap.zip
call zip -r .\docs\assets\bootstrap.zip bootstrap
del /F /Q bootstrap
if exist "..\bootstrap-gh-pages\assets\bootstrap.zip" del /F /Q ..\bootstrap-gh-pages\assets\bootstrap.zip
call node .\docs\build production
copy .\docs\* ..\bootstrap-gh-pages\*
goto end

::
:: WATCH LESS FILES
::

:watch
echo Watching less files... && ^
call watchr -e "watch('less/.*\.less') { system 'make.bat' }"
goto end

::
:: HAUNT GITHUB ISSUES 4 FAT & MDO ONLY (O_O )
::

:haunt
echo To-Do: haunt
goto end

:.PHONY
echo To-Do: PHONY
goto end

:end
