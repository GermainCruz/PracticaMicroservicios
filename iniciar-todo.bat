@echo off
setlocal

cd /d "%~dp0"

echo ========================================================
echo       INICIANDO ARQUITECTURA DE MICROSERVICIOS
echo   (Eureka, Service1 + MySQL, Service2, Pasarela MVC)
echo ========================================================
echo.

REM 1. Comprobar existencia de los archivos JAR
set "MISSING=0"

if not exist "%~dp0eureka-server\target\eureka-server-0.0.1-SNAPSHOT.jar" (
    echo [ERROR] Falta el archivo: eureka-server\target\eureka-server-0.0.1-SNAPSHOT.jar
    set "MISSING=1"
)
if not exist "%~dp0Service1\target\Service1-0.0.1-SNAPSHOT.jar" (
    echo [ERROR] Falta el archivo: Service1\target\Service1-0.0.1-SNAPSHOT.jar
    set "MISSING=1"
)
if not exist "%~dp0Service2\target\Service2-0.0.1-SNAPSHOT.jar" (
    echo [ERROR] Falta el archivo: Service2\target\Service2-0.0.1-SNAPSHOT.jar
    set "MISSING=1"
)
if not exist "%~dp0Pasarela\target\Pasarela-0.0.1-SNAPSHOT.jar" (
    echo [ERROR] Falta el archivo: Pasarela\target\Pasarela-0.0.1-SNAPSHOT.jar
    set "MISSING=1"
)

if "%MISSING%"=="1" (
    echo.
    echo [SUGERENCIA] Ejecute "mvn clean package -DskipTests" en cada proyecto que falte antes de iniciar.
    pause
    exit /b 1
)

REM 2. Comprobar disponibilidad de MySQL / MariaDB (XAMPP)
:check_mysql
echo Comprobando conexion a MySQL/MariaDB (XAMPP) en el puerto 3306...
powershell -NoProfile -Command "try { $c = New-Object System.Net.Sockets.TcpClient('127.0.0.1', 3306); $c.Close(); exit 0 } catch { exit 1 }"
if errorlevel 1 (
    echo.
    echo ========================================================
    echo [ERROR] MySQL/MariaDB no responde en localhost:3306.
    echo Por favor, inicie el modulo MySQL desde el Panel de Control de XAMPP.
    echo ========================================================
    echo Presione una tecla para reintentar o Ctrl+C para salir...
    pause >nul
    goto check_mysql
)
echo [OK] MySQL/MariaDB detectado y activo en el puerto 3306.
echo.

REM 3. Iniciar Eureka Server
echo [1/4] Iniciando Eureka Server en el puerto 8761...
start "Eureka Server - Puerto 8761" /D "%~dp0eureka-server" cmd /k java -jar "%~dp0eureka-server\target\eureka-server-0.0.1-SNAPSHOT.jar"

echo.
echo Esperando a que Eureka Server este completamente disponible en http://localhost:8761...
set ATTEMPTS=0

:wait_eureka
set /a ATTEMPTS+=1
curl.exe -s -f -o nul http://localhost:8761/
if not errorlevel 1 goto eureka_ok
if %ATTEMPTS% geq 30 goto eureka_fail
echo Intentando conectar con Eureka (intento %ATTEMPTS% de 30)...
ping -n 3 127.0.0.1 >nul
goto wait_eureka

:eureka_fail
echo.
echo ========================================================
echo [ERROR] Eureka Server no respondio dentro del tiempo limite (60s).
echo Verifique la ventana de Eureka Server para revisar los detalles del error.
echo ========================================================
pause
exit /b 1

:eureka_ok
echo.
echo [OK] Eureka Server esta activo y listo.
echo.

REM 4. Iniciar clientes
echo [2/4] Iniciando Service 1 (Acceso a BD) en el puerto 8081...
start "Service 1 - Puerto 8081" /D "%~dp0Service1" cmd /k java -jar "%~dp0Service1\target\Service1-0.0.1-SNAPSHOT.jar"

echo [3/4] Iniciando Service 2 (Consumo de Service 1) en el puerto 8082...
start "Service 2 - Puerto 8082" /D "%~dp0Service2" cmd /k java -jar "%~dp0Service2\target\Service2-0.0.1-SNAPSHOT.jar"

echo [4/4] Iniciando Pasarela (Gateway MVC con Restricciones) en el puerto 8080...
start "Pasarela - Puerto 8080" /D "%~dp0Pasarela" cmd /k java -jar "%~dp0Pasarela\target\Pasarela-0.0.1-SNAPSHOT.jar"

echo.
echo ========================================================
echo       TODOS LOS MICROSERVICIOS HAN SIDO LANZADOS
echo ========================================================
echo - Eureka Server:  http://localhost:8761
echo - Pasarela API:   http://localhost:8080
echo.
echo Endpoints principales en la Pasarela:
echo   * GET/POST/PUT/DELETE  http://localhost:8080/api/equipos
echo   * GET                  http://localhost:8080/service2/equipos
echo   * GET/POST/PATCH/DELETE http://localhost:8080/service1
echo   * GET/POST/PATCH/DELETE http://localhost:8080/service2
echo.
echo Restricciones activas en la Pasarela:
echo   * Restriccion 1: Maximo 2 solicitudes GET por ruta en 60s (3ra devuelve 429).
echo     - Reset contadores: POST http://localhost:8080/api/rate-limit/reset
echo   * Restriccion 2: Operaciones de escritura (POST/PUT/PATCH/DELETE) requieren:
echo     - Cabecera: X-API-KEY: admin123  (sin ella devuelve 401).
echo.
echo NOTA: Los clientes tardan entre 5 y 10 segundos en registrarse en Eureka.
echo Las ventanas de cada microservicio permaneceran abiertas para ver logs.
echo ========================================================
echo.
pause
