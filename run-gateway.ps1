# Script khởi chạy Spring Cloud Gateway cho FutaBus
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Khoi dong FUTA API Gateway (Port 8080)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Thiết lập đường dẫn JDK
$candidateJdks = @(
    "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr",
    "C:\Users\ADMIN\.jdks\openjdk-27",
    $env:JAVA_HOME
)

$javaExe = $null
foreach ($jdk in $candidateJdks) {
    if ($jdk -and (Test-Path "$jdk\bin\java.exe")) {
        $env:JAVA_HOME = $jdk
        $javaExe = "$jdk\bin\java.exe"
        break
    }
}

if (-not $javaExe) {
    Write-Host "[ERROR] Khong tim thay Java. Vui long cai dat JDK hoac kiem tra bien JAVA_HOME." -ForegroundColor Red
    exit 1
}

Write-Host "[INFO] Su dung Java tai: $env:JAVA_HOME" -ForegroundColor Green

# Kiem tra file JAR da duoc build hay chua
$jarPath = "gateway\target\gateway-1.0.0.jar"
if (-not (Test-Path $jarPath)) {
    Write-Host "[INFO] Dang build gateway..." -ForegroundColor Yellow
    $mvnCmd = "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\plugins\maven-plugin\lib\maven3\bin\mvn.cmd"
    if (Test-Path $mvnCmd) {
        & $mvnCmd -f gateway/pom.xml package -DskipTests
    } else {
        mvn -f gateway/pom.xml package -DskipTests
    }
}

Write-Host "[INFO] Dang khoi chay Gateway tren cong 8080 (Profile: local)..." -ForegroundColor Green
& $javaExe -jar $jarPath --spring.profiles.active=local
