Param(
    [string]$Token
)

$owner = "waLaz05"
$repo = "BrainFood-"
$tag = "v1.0"
$name = "BrainFood v1.0"
$body = "Release v1.0 - First Production Release`n`nFeatures:`n- Authentication (Google + Guest)`n- Cloud Sync (Deferred)`n- Recipe Management`n- Backpack Inventory"
$apkPath = "app/build/outputs/apk/release/app-release.apk"

# Headers
$headers = @{
    "Authorization" = "token $token"
    "Accept"        = "application/vnd.github.v3+json"
}

# 1. Create Release
$releaseUrl = "https://api.github.com/repos/$owner/$repo/releases"
$releaseBody = @{
    tag_name         = $tag
    target_commitish = "main"
    name             = $name
    body             = $body
    draft            = $false
    prerelease       = $false
} | ConvertTo-Json

try {
    Write-Host "Creating release..."
    $releaseResponse = Invoke-RestMethod -Uri $releaseUrl -Method Post -Headers $headers -Body $releaseBody -ContentType "application/json"
    $uploadUrlTemplate = $releaseResponse.upload_url
    Write-Host "Release created: $($releaseResponse.html_url)"
}
catch {
    Write-Error "Failed to create release: $_"
    exit 1
}

# 2. Upload Asset
if (Test-Path $apkPath) {
    $uploadUrl = $uploadUrlTemplate -replace "\{.*\}", "?name=BrainFood-v1.0.apk"
    Write-Host "Uploading APK to: $uploadUrl"
    
    try {
        # PowerShell upload binary file needs specific handling or use curl
        # Invoke-RestMethod -InFile handles binary reasonably well
        $uploadResponse = Invoke-RestMethod -Uri $uploadUrl -Method Post -Headers @{
            "Authorization" = "token $token"
            "Content-Type"  = "application/vnd.android.package-archive"
        } -InFile $apkPath -ContentType "application/vnd.android.package-archive"
        
        Write-Host "Upload successful!"
        Write-Host "Direct Download Link: $($uploadResponse.browser_download_url)"
    }
    catch {
        Write-Error "Failed to upload asset: $_"
        exit 1
    }
}
else {
    Write-Error "APK file not found at $apkPath"
    exit 1
}
