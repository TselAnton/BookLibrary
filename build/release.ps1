param(
    [Parameter(Mandatory)][string] $Branch,
    [Parameter(Mandatory)][ValidatePattern('^[0-9a-f]{40}$')][string] $Commit,
    [Parameter(Mandatory)][ValidatePattern('^[0-9]+$')][string] $RunNumber,
    [string] $Repository = 'TselAnton/BookLibrary',
    [switch] $BuildOnly,
    [switch] $RunTests
)

$ErrorActionPreference = 'Stop'
if ($Repository -ne 'TselAnton/BookLibrary') {
    throw 'Release publishing is restricted to TselAnton/BookLibrary.'
}
if ((git rev-parse HEAD).Trim() -ne $Commit) {
    throw 'The checked-out commit does not match the requested release commit.'
}
[xml] $releasePom = Get-Content -LiteralPath 'pom.xml' -Raw
$projectVersion = [string] $releasePom.project.version
if ($projectVersion -eq '${revision}') {
    $projectVersion = [string] $releasePom.project.properties.revision
}
if ($projectVersion -notmatch '^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)(-SNAPSHOT)?$') {
    throw 'Unsupported project version.'
}
$stable = $Branch -eq 'main'
if ($stable -and $projectVersion.EndsWith('-SNAPSHOT')) {
    throw 'Stable SNAPSHOT releases are prohibited. Finish development and set revision to the final version first.'
}
$baseVersion = $projectVersion -replace '-SNAPSHOT$', ''
$releaseVersion = if ($stable) { $baseVersion } else { "$baseVersion-preview.$RunNumber.$($Commit.Substring(0, 8))" }
$releaseTag = "v$releaseVersion"
Write-Output "Preparing release $releaseTag from branch $Branch at commit $Commit (POM version: $projectVersion)."
if (-not $BuildOnly) {
    gh release view $releaseTag --repo $Repository *> $null
    if ($LASTEXITCODE -eq 0) {
        throw "Release $releaseTag already exists. Existing releases are never overwritten."
    }
    git ls-remote --exit-code --tags origin "refs/tags/$releaseTag" *> $null
    if ($LASTEXITCODE -eq 0) {
        throw "Tag $releaseTag already exists. Choose a new version or explicitly resolve the incomplete release."
    }
    if ($LASTEXITCODE -ne 2) {
        throw 'Could not check remote release tags.'
    }
}
$skipTests = if ($RunTests) { 'false' } else { 'true' }
$buildArguments = @('--batch-mode', '--no-transfer-progress', "-Drevision=$releaseVersion",
    '-Dbooklibrary.platform=windows-x86_64', "-DskipTests=$skipTests", 'clean', 'verify')
Write-Output "Run tests: $($RunTests.IsPresent)."
& mvn @buildArguments
if ($LASTEXITCODE -ne 0) {
    throw 'Build or tests failed. No release has been created.'
}
$releaseDirectory = Join-Path (Get-Location) 'target/release'
New-Item -ItemType Directory -Path $releaseDirectory -Force | Out-Null
$assetName = 'BookLibraryApp-windows-x86_64.jar'
$releaseAsset = Join-Path $releaseDirectory $assetName
Copy-Item -LiteralPath 'target/BookLibraryApp.jar' -Destination $releaseAsset
Add-Type -AssemblyName System.IO.Compression.FileSystem
$releaseJar = [IO.Compression.ZipFile]::OpenRead($releaseAsset)
try {
    $manifestReader = [IO.StreamReader]::new($releaseJar.GetEntry('META-INF/MANIFEST.MF').Open())
    try { $manifestText = $manifestReader.ReadToEnd() } finally { $manifestReader.Dispose() }
    $updateProtocol = if ($manifestText -match '(?m)^BookLibrary-Update-Protocol: 1\r?$') { '1' } else { '0' }
    if ($updateProtocol -eq '1' -and -not ($releaseJar.Entries | Where-Object { $_.FullName -like 'updater/*-updater.jar' })) {
        throw 'The update-capable release is missing its embedded standalone helper.'
    }
} finally {
    $releaseJar.Dispose()
}
$releaseHash = (Get-FileHash -LiteralPath $releaseAsset -Algorithm SHA256).Hash.ToLowerInvariant()
$releaseMetadata = @(
    "protocol=$updateProtocol"
    "version=$releaseVersion"
    'platform=windows-x86_64'
    'java.minimum=17'
    "asset=$assetName"
    "sha256=$releaseHash"
) -join "`n"
[IO.File]::WriteAllText((Join-Path $releaseDirectory 'update-windows-x86_64.properties'), $releaseMetadata + "`n", [Text.UTF8Encoding]::new($false))
[IO.File]::WriteAllText((Join-Path $releaseDirectory "$assetName.sha256"), "$releaseHash  $assetName`n", [Text.UTF8Encoding]::new($false))
$releaseNotes = "BookLibrary $releaseVersion`n`nBranch: $Branch`nCommit: $Commit`nPlatform: Windows x64; Java 17 or later.`n`nDownload $assetName. Keep your existing repository, backup and settings files.`n"
[IO.File]::WriteAllText((Join-Path $releaseDirectory 'notes.md'), $releaseNotes, [Text.UTF8Encoding]::new($false))
if ($BuildOnly) {
    Write-Output "Prepared $releaseVersion in $releaseDirectory. Nothing was published."
    exit 0
}
git tag $releaseTag $Commit
if ($LASTEXITCODE -ne 0) { throw 'Could not create the release tag.' }
git push origin "refs/tags/$releaseTag"
if ($LASTEXITCODE -ne 0) { throw 'Could not push the release tag.' }
$releaseArguments = @('release', 'create', $releaseTag, '--repo', $Repository, '--verify-tag', '--title', $releaseVersion,
    '--draft', '--notes-file', (Join-Path $releaseDirectory 'notes.md'))
if (-not $stable) { $releaseArguments += '--prerelease' }
& gh @releaseArguments
if ($LASTEXITCODE -ne 0) { throw 'Could not create the draft release.' }
gh release upload $releaseTag $releaseAsset (Join-Path $releaseDirectory "$assetName.sha256") (Join-Path $releaseDirectory 'update-windows-x86_64.properties') --repo $Repository
if ($LASTEXITCODE -ne 0) { throw 'Asset upload failed. The release remains a draft and is not offered by the updater.' }
$publishArguments = @('release', 'edit', $releaseTag, '--repo', $Repository, '--draft=false')
$publishArguments += if ($stable) { '--latest' } else { '--latest=false' }
& gh @publishArguments
if ($LASTEXITCODE -ne 0) { throw 'Could not publish the completed release.' }
gh release view $releaseTag --repo $Repository --json url --jq .url
