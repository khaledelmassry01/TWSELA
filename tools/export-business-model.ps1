# Requires Windows with Microsoft Word installed
param(
    [string]$Source = "$(Join-Path $PSScriptRoot '..\docs\business-model.md')",
    [string]$Output = "$(Join-Path $PSScriptRoot '..\docs\Twsela-Business-Model.docx')"
)

if (-not (Test-Path $Source)) {
    Write-Error "Source file not found: $Source"
    exit 1
}

# Ensure output directory exists
$outDir = Split-Path -Parent $Output
if (-not (Test-Path $outDir)) { New-Item -ItemType Directory -Force -Path $outDir | Out-Null }

# Try to convert Markdown to DOCX using Word COM (quick import via HTML fallback)
# Approach: Convert Markdown to HTML in-memory via a simple parser isn't trivial; use pandoc if available.
# Fallback: Open the .md directly in Word (Word can open text/markdown for recent versions) and save as DOCX.

function Get-Pandoc {
    $pandoc = (Get-Command pandoc -ErrorAction SilentlyContinue).Source
    if ($pandoc) { return $pandoc }
    return $null
}

$pandoc = Get-Pandoc
if ($pandoc) {
    & $pandoc -f markdown -t docx -o $Output $Source
    if ($LASTEXITCODE -ne 0) { Write-Error "Pandoc failed with exit code $LASTEXITCODE"; exit $LASTEXITCODE }
    Write-Host "DOCX generated at $Output via Pandoc"
    exit 0
}

# Fallback using Word COM Automation
try {
    $word = New-Object -ComObject Word.Application
} catch {
    Write-Error "Microsoft Word COM automation is not available. Install Word or install Pandoc and rerun."
    exit 1
}

$word.Visible = $false
try {
    # Open as text and let Word interpret headings marked with '#'
    $doc = $word.Documents.Open($Source)
    $wdFormatXMLDocument = 12
    $doc.SaveAs([ref]$Output, [ref]$wdFormatXMLDocument)
    $doc.Close()
    $word.Quit()
    Write-Host "DOCX generated at $Output via Word automation"
} catch {
    if ($doc) { $doc.Close() }
    if ($word) { $word.Quit() }
    throw
}
