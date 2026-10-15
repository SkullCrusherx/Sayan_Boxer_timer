# Fixes for GitHub compilation error (15 October 2026)

The previous GitHub Actions build stopped at `:app:compileDebugJavaWithJavac` because of three Java compiler errors:

1. `BoxingData.java`: the method argument `Context c` and local `String c` had the same name. The local was renamed to `normalizedCombo`.
2. `MainActivity.java`: the older source referred to an undefined `pace` variable. This release uses the bundled 1.2x recorded voice files, so the obsolete extra is not needed.
3. `MainActivity.java`: the older source referred to an undefined `frequency` variable. This release passes `combo_gap_ms`, `voice_enabled`, `bell_enabled`, `warning_enabled`, and `in_order` as expected by `WorkoutService`.

The `build-apk.yml` file already exists in `.github/workflows/`.

## Upload

Extract the ZIP, open the extracted directory, and upload its **contents** to the root of the GitHub repository. **Replace** the old Java files (and any other old project files), do not upload a ZIP alone or add a second project folder. In particular, verify that these exact files were replaced:

- `app/src/main/java/com/sayanboxer/boxing/BoxingData.java`
- `app/src/main/java/com/sayanboxer/boxing/MainActivity.java`

On GitHub, navigate to **Actions → Build Sayan Boxer APK → Run workflow**. If the Java build still fails, share the first compiler error lines.

Java changes were checked against the 3 errors in the supplied build log. This project was not compiled in this container (Android SDK/Gradle unavailable), so a successful APK build has not yet been confirmed.
