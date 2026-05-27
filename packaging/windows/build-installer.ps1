[CmdletBinding()]
param(
    [string]$Version = "1.0.0",
    [string]$FrontendDirectory = (Join-Path $PSScriptRoot "..\..\..\doc-central-forms")
)

$ErrorActionPreference = "Stop"
$BackendDirectory = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$FrontendDirectory = (Resolve-Path $FrontendDirectory).Path
$FrontendDist = Join-Path $FrontendDirectory "dist"
$TargetDirectory = Join-Path $BackendDirectory "target"
$InputDirectory = Join-Path $TargetDirectory "jpackage-input"
$InstallerDirectory = Join-Path $TargetDirectory "installer"
$IconPath = Join-Path $TargetDirectory "central-docs.ico"

function New-CentralDocsIcon {
    param([string]$Destination)

    Add-Type -AssemblyName PresentationCore, PresentationFramework, WindowsBase

    $visual = [System.Windows.Media.DrawingVisual]::new()
    $drawing = $visual.RenderOpen()
    $background = [System.Windows.Media.SolidColorBrush]::new(
        [System.Windows.Media.ColorConverter]::ConvertFromString("#4f46e5"))
    $foreground = [System.Windows.Media.SolidColorBrush]::new(
        [System.Windows.Media.Colors]::White)
    $drawing.DrawRoundedRectangle(
        $background,
        $null,
        [System.Windows.Rect]::new(0, 0, 256, 256),
        54,
        54)
    $text = [System.Windows.Media.FormattedText]::new(
        "C",
        [System.Globalization.CultureInfo]::InvariantCulture,
        [System.Windows.FlowDirection]::LeftToRight,
        [System.Windows.Media.Typeface]::new("Segoe UI Semibold"),
        170,
        $foreground,
        1.0)
    $drawing.DrawText($text, [System.Windows.Point]::new(72, 35))
    $drawing.Close()

    $bitmap = [System.Windows.Media.Imaging.RenderTargetBitmap]::new(
        256, 256, 96, 96, [System.Windows.Media.PixelFormats]::Pbgra32)
    $bitmap.Render($visual)
    $encoder = [System.Windows.Media.Imaging.PngBitmapEncoder]::new()
    $encoder.Frames.Add([System.Windows.Media.Imaging.BitmapFrame]::Create($bitmap))
    $pngStream = [System.IO.MemoryStream]::new()
    $encoder.Save($pngStream)
    $pngBytes = $pngStream.ToArray()

    $file = [System.IO.File]::Create($Destination)
    $writer = [System.IO.BinaryWriter]::new($file)
    $writer.Write([UInt16]0)
    $writer.Write([UInt16]1)
    $writer.Write([UInt16]1)
    $writer.Write([Byte]0)
    $writer.Write([Byte]0)
    $writer.Write([Byte]0)
    $writer.Write([Byte]0)
    $writer.Write([UInt16]1)
    $writer.Write([UInt16]32)
    $writer.Write([UInt32]$pngBytes.Length)
    $writer.Write([UInt32]22)
    $writer.Write($pngBytes)
    $writer.Dispose()
    $pngStream.Dispose()
}

Push-Location $FrontendDirectory
try {
    & npm.cmd ci
    & npm.cmd run build
} finally {
    Pop-Location
}

Push-Location $BackendDirectory
try {
    & .\mvnw.cmd clean verify -Pdesktop "-Dfrontend.dist=$FrontendDist"
} finally {
    Pop-Location
}

if (Test-Path $InputDirectory) {
    Remove-Item -Recurse -Force $InputDirectory
}
New-Item -ItemType Directory -Force -Path $InputDirectory, $InstallerDirectory | Out-Null
$JarPath = Get-ChildItem -Path $TargetDirectory -Filter "doc-central-*.jar" |
    Select-Object -First 1
if ($null -eq $JarPath) {
    throw "The Spring Boot application jar was not created."
}
$ApplicationJar = $JarPath.Name
Copy-Item -Force $JarPath.FullName $InputDirectory
New-CentralDocsIcon -Destination $IconPath

& jpackage `
    --type msi `
    --name "Central Docs" `
    --description "Offline legal document generator" `
    --vendor "TheAlexBig" `
    --app-version $Version `
    --dest $InstallerDirectory `
    --input $InputDirectory `
    --main-jar $ApplicationJar `
    --main-class "org.springframework.boot.loader.launch.JarLauncher" `
    --arguments "--spring.profiles.active=desktop" `
    --java-options "-Dfile.encoding=UTF-8" `
    --icon $IconPath `
    --win-menu `
    --win-menu-group "Central Docs" `
    --win-shortcut `
    --win-dir-chooser `
    --win-upgrade-uuid "89a023d9-748e-4479-97ea-571e9b027a84"

Write-Host "Installer created in $InstallerDirectory"
