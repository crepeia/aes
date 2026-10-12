$ErrorActionPreference = 'Stop'
$doseProject = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$doseOutput = Join-Path ([System.IO.Path]::GetTempPath()) ('aes-dose-tests-' + [guid]::NewGuid())
New-Item -ItemType Directory -Path $doseOutput | Out-Null
$doseClasses = Join-Path $doseProject 'build/web/WEB-INF/classes'
$doseLib = Join-Path $doseProject 'build/web/WEB-INF/lib'
$dosePrimefaces = Join-Path $doseProject 'lib/Primefaces_6.1/primefaces-6.1.jar'
$doseCompilePath = @($doseClasses, (Join-Path $doseProject 'lib/javaee-api-6.0.jar'), $dosePrimefaces, (Join-Path $doseLib 'jackson-annotations-2.8.11.jar')) -join ';'
$doseSources = @(
    'src/java/aes/model/DailyLogOperation.java',
    'src/java/aes/persistence/DailyLogDAO.java',
    'src/java/aes/service/DailyLogFacadeREST.java',
    'test/aes/persistence/DailyLogOfflineTest.java'
) | ForEach-Object { Join-Path $doseProject $_ }
# Uses the existing compiled legacy classes/JARs, not a full WAR build.
& javac -proc:none -encoding UTF-8 --release 8 -cp $doseCompilePath -d $doseOutput $doseSources
if ($LASTEXITCODE -ne 0) { throw 'Dose compilation failed' }
$doseRuntimePath = @($doseOutput, $doseClasses, $dosePrimefaces, ($doseLib + '/*')) -join ';'
& java -cp $doseRuntimePath aes.persistence.DailyLogOfflineTest
if ($LASTEXITCODE -ne 0) { throw 'Dose DAO tests failed' }
