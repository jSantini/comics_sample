#!/usr/bin/env python3
"""Exercise real Gradle generation and javac; fixtures contain no credentials.

Requires the repository's JDK 17/Android SDK setup. Run alone, from any directory.
No external API is contacted. Temporary Java classes are automatically removed.
"""
import base64
import os
from pathlib import Path
import shutil
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
GENERATED = ROOT / "data/build/generated/source/buildConfig/debug/com/example/js_comics/data/BuildConfig.java"
VALID = [
    "",  # Public default: offline demo.
    "https://catalog.example.invalid/api/",
    "https://catalog.example.invalid:8443/api/",
    "https://catalog.example.invalid/a$b/O'Reilly/a+b;c&d=/",
    "https://catalog.example.invalid/%22/%5C/%5Cu0022/%0A/",
    "https://catalog.example.invalid/café/東京/🛰/",
    "https://[::1]:8443/api/",
]
INVALID = [
    "http://catalog.example.invalid/",
    "https://user:password@catalog.example.invalid/",  # Synthetic userinfo.
    "https://catalog.example.invalid/?token=fixture",  # Synthetic query.
    "https://catalog.example.invalid/#fragment",
    "https://catalog.example.invalid/api",  # Missing trailing slash.
    "https:///api/",  # Missing host.
    "https://catalog.example.invalid:99999/",
    "https://catalog.example.invalid:0/",
    'https://catalog.example.invalid/quote"/',
    "https://catalog.example.invalid/back\\slash/",
    "https://catalog.example.invalid/line\nbreak/",
    "https://catalog.example.invalid/space here/",
]


def tool(name):
    java_home = os.environ.get("JAVA_HOME")
    executable = f"{name}.exe" if os.name == "nt" else name
    candidate = Path(java_home) / "bin" / executable if java_home else None
    resolved = str(candidate) if candidate and candidate.is_file() else shutil.which(executable)
    if not resolved:
        raise RuntimeError(f"Missing {name}; configure JDK 17 first")
    return resolved


def gradle(url=None):
    # Use the checked-in Windows batch wrapper on Windows, and the shell wrapper elsewhere.
    wrapper = ROOT / ("gradlew.bat" if os.name == "nt" else "gradlew")
    if not wrapper.is_file():
        raise RuntimeError(f"Missing Gradle wrapper: {wrapper}")
    command = [str(wrapper), "--daemon", "--console=plain", "--max-workers=2",
               ":data:generateDebugBuildConfig"]
    # Pass fixture data through the Gradle project-property environment interface.
    # Passing -PCOMICS_BASE_URL=... through gradlew.bat lets cmd.exe interpret
    # &, quotes, percent signs and other URL characters as shell syntax.
    # Set even for the final restoration to ensure the offline default.
    env = os.environ.copy()
    env["ORG_GRADLE_PROJECT_COMICS_BASE_URL"] = "" if url is None else url
    return subprocess.run(
        command, cwd=ROOT, env=env, capture_output=True, text=True, errors="replace"
    )


def require_success(result, context):
    if result.returncode:
        raise RuntimeError(f"{context} failed (exit {result.returncode})\n{result.stdout[-3000:]}\n{result.stderr[-1000:]}")


def main():
    javac, java = tool("javac"), tool("java")
    try:
        for index, url in enumerate(VALID, 1):
            require_success(gradle(url), f"valid fixture {index}: Gradle")
            with tempfile.TemporaryDirectory(prefix="comics-url-") as directory:
                folder = Path(directory)
                source = folder / "VerifyLiteral.java"
                source.write_text('''import com.example.js_comics.data.BuildConfig;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
public final class VerifyLiteral {
    public static void main(String[] args) {
        String expected = new String(Base64.getDecoder().decode(args[0]), StandardCharsets.UTF_8);
        if (!expected.equals(BuildConfig.COMICS_BASE_URL)) {
            throw new AssertionError("BuildConfig literal did not round-trip");
        }
    }
}
''', encoding="utf-8")
                require_success(subprocess.run(
                    [javac, "-encoding", "UTF-8", "-d", directory, str(GENERATED), str(source)],
                    capture_output=True, text=True), f"valid fixture {index}: javac")
                expected = base64.b64encode(url.encode("utf-8")).decode("ascii")
                require_success(subprocess.run(
                    [java, "-cp", directory, "VerifyLiteral", expected], capture_output=True, text=True),
                    f"valid fixture {index}: round-trip")
            print(f"PASS valid fixture {index}: generated Java compiles and preserves the exact URL", flush=True)
        for index, url in enumerate(INVALID, 1):
            result = gradle(url)
            message = result.stdout + result.stderr
            if result.returncode == 0 or "COMICS_BASE_URL must be a public HTTPS" not in message:
                raise AssertionError(f"Invalid fixture {index} was not rejected by the expected guard")
            print(f"PASS invalid fixture {index}: rejected by URL validation", flush=True)
    finally:
        require_success(gradle(), "Restore default demo BuildConfig")
    print(f"PASS: {len(VALID)} literal round-trips, {len(INVALID)} rejected URLs; demo default restored")


if __name__ == "__main__":
    main()
