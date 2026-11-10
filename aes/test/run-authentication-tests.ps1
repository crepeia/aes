param([string]$GlassfishModules = $env:GLASSFISH_MODULES,
      [string]$AnnotationApi = $env:JAVA_ANNOTATION_API)
$ErrorActionPreference = 'Stop'
if (-not $GlassfishModules) {
    $GlassfishModules = 'C:\Program Files\glassfish-5.0.1\glassfish5\glassfish\modules'
}
if (-not (Test-Path -LiteralPath (Join-Path $GlassfishModules 'javax.ws.rs-api.jar'))) {
    throw 'Informe -GlassfishModules com o diretório de módulos do GlassFish 5.'
}
$authProject = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$authOutput = Join-Path ([System.IO.Path]::GetTempPath()) ('aes-auth-tests-' + [guid]::NewGuid())
New-Item -ItemType Directory -Path $authOutput | Out-Null
$authClasses = Join-Path $authProject 'build/web/WEB-INF/classes'
if (-not (Test-Path -LiteralPath (Join-Path $authClasses 'aes/model/User.class'))) {
    throw 'Compile o backend primeiro: os testes reutilizam as entidades compiladas em build/web/WEB-INF/classes.'
}
if (-not $AnnotationApi) {
    $AnnotationApi = 'C:\Program Files\NetBeans-12.0\netbeans\enterprise\modules\ext\javaee7-endorsed\javax.annotation-api.jar'
}
if (-not (Test-Path -LiteralPath $AnnotationApi)) {
    throw 'Informe -AnnotationApi com o JAR javax.annotation-api 1.2 ou mais recente (javax, não jakarta).'
}
# Avoid unrelated legacy/Hibernate JARs: only dependencies used by these classes.
$authJars = @('javax.ws.rs-api.jar', 'javax.persistence.jar', 'javax.ejb-api.jar', 'javax.transaction-api.jar',
    'javax.inject.jar', 'cdi-api.jar', 'jersey-common.jar', 'hk2-api.jar', 'hk2-utils.jar',
    'osgi-resource-locator.jar', 'jackson-annotations.jar') | ForEach-Object { Join-Path $GlassfishModules $_ }
# Compile a temporary snapshot without modifying the existing build tree.
$authStagedClasses = Join-Path $authOutput 'classes'
Copy-Item -LiteralPath $authClasses -Destination $authStagedClasses -Recurse
$authStagedJars = @($AnnotationApi, (Join-Path $authProject 'lib/Primefaces_6.1/primefaces-6.1.jar'),
    (Join-Path $authProject 'lib/gson-2.8.6.jar'), (Join-Path $authProject 'lib/caffeine-2.9.3.jar'),
    (Join-Path $authProject 'lib/bucket4j-core-7.6.0.jar')) + $authJars
$authLocalJars = foreach ($authJar in $authStagedJars) {
    $authDestination = Join-Path $authOutput ([IO.Path]::GetFileName($authJar))
    Copy-Item -LiteralPath $authJar -Destination $authDestination
    $authDestination
}
$authClasspath = (@($authStagedClasses) + $authLocalJars) -join ';'
$authSources = @('src/java/aes/service/AuthenticationFilter.java', 'src/java/aes/utility/SecurityContextHelper.java',
    'src/java/aes/service/ChatbotInteractionFacadeREST.java', 'src/java/aes/utility/ChatbotClient.java',
    'src/java/aes/utility/ChatbotPolicy.java', 'test/aes/service/AuthenticationFilterTest.java',
    'test/aes/service/ChatbotGatewayTest.java') | ForEach-Object { Join-Path $authProject $_ }
& javac -proc:none -encoding UTF-8 --release 8 -cp $authClasspath -d $authOutput $authSources
if ($LASTEXITCODE -ne 0) { throw 'Authentication compilation failed' }
& java -cp ($authOutput + ';' + $authClasspath) aes.service.AuthenticationFilterTest
if ($LASTEXITCODE -ne 0) { throw 'Authentication filter tests failed' }
& java -cp ($authOutput + ';' + $authClasspath) aes.service.ChatbotGatewayTest
if ($LASTEXITCODE -ne 0) { throw 'Chatbot gateway tests failed' }
