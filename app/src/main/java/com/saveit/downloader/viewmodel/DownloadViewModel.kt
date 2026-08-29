I'll analyze the failing job for you. Let me start by retrieving the job logs.Now let me examine the failing file:## Solution for Failing Job

The build is failing due to **compilation errors in `DownloadViewModel.kt`** at lines 117 and 123:

### Errors:
1. **Line 117**: `Unresolved reference: option` — The `option()` method doesn't exist on `YoutubeDLRequest`
2. **Line 123**: `Type mismatch` — The lambda expects `(Float, Long, String) -> Unit` but receives `(Int, Long, String) -> Unit`; progress should be `Float`, not `Int`

### Root Cause:
The code is using an incorrect API for the `YoutubeDL` library. The `YoutubeDLRequest` doesn't have an `option()` method, and the progress callback parameter type is wrong.

### Fix:

Replace lines 114-117 with the correct API usage:

```kotlin
val outputTemplate = File(downloadDir, "%(title)s_%(height)sp.%(ext)s").absolutePath
val request = YoutubeDLRequest(url)
request.addOption("--no-playlist")
request.addOption("-f", formatOption)
request.addOption("-o", outputTemplate)
```

And update line 123 to use `Float` for progress instead of `Int`:

```kotlin
YoutubeDL.getInstance().execute(request) { progress: Float, eta: Long, line: String ->
    val progressPercent = progress / 100f
    // ... rest of the code
}
```

**Key changes:**
- Use `addOption()` instead of `option()`
- Change progress parameter from `Int` to `Float`

After making these corrections, the Kotlin compiler errors should resolve and the build will pass.
