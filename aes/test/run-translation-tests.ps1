$ErrorActionPreference = 'Stop'
$translationProject = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$translationOutput = Join-Path ([IO.Path]::GetTempPath()) ('aes-translation-tests-' + [guid]::NewGuid())
New-Item -ItemType Directory -Path $translationOutput | Out-Null
$translationJavac = Get-Command javac -ErrorAction SilentlyContinue
if ($translationJavac) {
    $translationJavac = $translationJavac.Source
} elseif ($env:JAVA_HOME -and (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin/javac.exe'))) {
    $translationJavac = Join-Path $env:JAVA_HOME 'bin/javac.exe'
} elseif (Test-Path -LiteralPath 'C:/Program Files/Java/jdk1.8.0_241/bin/javac.exe') {
    $translationJavac = 'C:/Program Files/Java/jdk1.8.0_241/bin/javac.exe'
} else {
    throw 'A JDK with javac is required. Set JAVA_HOME or add javac to PATH.'
}
$translationJava = Join-Path (Split-Path $translationJavac) 'java.exe'
$translationSources = @(
    'src/java/aes/utility/MessageResources.java',
    'test/aes/utility/MessageResourcesTest.java'
) | ForEach-Object { Join-Path $translationProject $_ }
& $translationJavac -proc:none -encoding UTF-8 -source 8 -target 8 -d $translationOutput $translationSources
if ($LASTEXITCODE -ne 0) { throw 'Translation test compilation failed' }
$translationClasspath = @($translationOutput, (Join-Path $translationProject 'src/java')) -join ';'
& $translationJava -cp $translationClasspath aes.utility.MessageResourcesTest $translationProject
if ($LASTEXITCODE -ne 0) { throw 'Translation tests failed' }
