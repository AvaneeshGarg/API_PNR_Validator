# Frontend-Backend Debug Script for PowerShell
Write-Host "================================" -ForegroundColor Cyan
Write-Host "Frontend-Backend Debug Script" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan

Write-Host "`n[1] Checking if Java is available..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1
    Write-Host "Java found: $($javaVersion[0])" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Java not found. Please install Java 17 or higher." -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "`n[2] Checking if Node.js is available..." -ForegroundColor Yellow
try {
    $nodeVersion = node --version
    Write-Host "Node.js found: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Node.js not found. Please install Node.js." -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "`n[3] Checking if npm is available..." -ForegroundColor Yellow
try {
    $npmVersion = npm --version
    Write-Host "npm found: $npmVersion" -ForegroundColor Green
} catch {
    Write-Host "ERROR: npm not found. Node.js installation may be incomplete." -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "`n[4] Checking backend directory..." -ForegroundColor Yellow
if (-not (Test-Path "src\main\java")) {
    Write-Host "ERROR: Please run this script from the deeplearning-extractor root directory." -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host "Backend directory found." -ForegroundColor Green

Write-Host "`n[5] Checking frontend directory..." -ForegroundColor Yellow
if (-not (Test-Path "vite-frontend\package.json")) {
    Write-Host "ERROR: Frontend directory not found." -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host "Frontend directory found." -ForegroundColor Green

Write-Host "`n[6] Installing frontend dependencies..." -ForegroundColor Yellow
Set-Location "vite-frontend"
try {
    npm install
    Write-Host "Frontend dependencies installed successfully." -ForegroundColor Green
} catch {
    Write-Host "ERROR: Failed to install frontend dependencies." -ForegroundColor Red
    Set-Location ".."
    Read-Host "Press Enter to exit"
    exit 1
}
Set-Location ".."

Write-Host "`n[7] Compiling backend..." -ForegroundColor Yellow
try {
    mvn clean compile -q
    Write-Host "Backend compiled successfully." -ForegroundColor Green
} catch {
    Write-Host "ERROR: Backend compilation failed." -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "`n[8] Checking for port conflicts..." -ForegroundColor Yellow
$port8080 = netstat -an | Select-String ":8080"
if ($port8080) {
    Write-Host "WARNING: Port 8080 is already in use." -ForegroundColor Magenta
} else {
    Write-Host "Port 8080 is available." -ForegroundColor Green
}

$port5173 = netstat -an | Select-String ":5173"
if ($port5173) {
    Write-Host "WARNING: Port 5173 is already in use." -ForegroundColor Magenta
} else {
    Write-Host "Port 5173 is available." -ForegroundColor Green
}

Write-Host "`n================================" -ForegroundColor Cyan
Write-Host "Setup Complete!" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Cyan

Write-Host "`nTo start the services:" -ForegroundColor White
Write-Host "1. Backend: mvn spring-boot:run" -ForegroundColor Yellow
Write-Host "2. Frontend: cd vite-frontend && npm run dev" -ForegroundColor Yellow
Write-Host "`nThen open http://localhost:5173 in your browser" -ForegroundColor White

Write-Host "`nDo you want to start both services now? (y/N): " -ForegroundColor Cyan -NoNewline
$response = Read-Host

if ($response -eq "y" -or $response -eq "Y") {
    Write-Host "`nStarting backend..." -ForegroundColor Yellow
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "mvn spring-boot:run"
    
    Write-Host "Waiting 10 seconds for backend to start..." -ForegroundColor Yellow
    Start-Sleep -Seconds 10
    
    Write-Host "Starting frontend..." -ForegroundColor Yellow
    Set-Location "vite-frontend"
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "npm run dev"
    Set-Location ".."
    
    Write-Host "`nBoth services are starting in separate windows." -ForegroundColor Green
    Write-Host "Check the console windows for any errors." -ForegroundColor Yellow
} else {
    Write-Host "`nSetup complete. Start services manually when ready." -ForegroundColor White
}

Read-Host "`nPress Enter to exit"
