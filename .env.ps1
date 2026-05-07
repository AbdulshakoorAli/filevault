# FileVault local dev environment
$env:JAVA_HOME = "C:\Users\basit\Desktop\filevault\.tools\jdk\jdk-17.0.11+9"
$env:MAVEN_HOME = "C:\Users\basit\Desktop\filevault\.tools\maven\apache-maven-3.9.9"
$env:PATH = "$env:JAVA_HOME\bin;$env:MAVEN_HOME\bin;$env:PATH"
$env:NODE_HOME = "C:\Users\basit\Desktop\filevault\.tools\node\node-v20.19.0-win-x64"
$env:PATH = "$env:NODE_HOME;$env:PATH"
$env:GEMINI_KEY = "AIzaSyC4yEZxjt_xKujKnzZYnUa1MGdTxVW5N98"
Write-Host "âœ… Java and Maven activated for this session" -ForegroundColor Green
java -version
mvn -version

function Start-Functions {
    Set-Location "C:\Users\basit\Desktop\filevault\functions"
    mvn clean package -DskipTests
    Set-Location "target\azure-functions\filevault-functions"
    func start
}
Write-Host "💡 Run 'Start-Functions' to build and start the functions" -ForegroundColor Cyan