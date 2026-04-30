#!/usr/bin/env python3
"""
Start the GSMS backend services from the project root above src/.

The script compiles the Maven project, starts the datastore services first,
then starts the alertmanager connected to LogStore. Stop it with Ctrl+C.
"""

from __future__ import annotations

import argparse
import os
import platform
import shlex
import shutil
import subprocess
import sys
import time
from pathlib import Path


DEFAULT_PROJECT_DIR = Path(__file__).resolve().parent
IS_WINDOWS = os.name == "nt"
UNSAFE_WARNING_FRAGMENTS = (
    "A terminally deprecated method in sun.misc.Unsafe has been called",
    "sun.misc.Unsafe::objectFieldOffset",
    "com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper",
    "Please consider reporting this to the maintainers of class",
    "sun.misc.Unsafe::objectFieldOffset will be removed in a future release",
)


def format_command(command: list[str]) -> str:
    if IS_WINDOWS:
        return subprocess.list2cmdline(command)
    return shlex.join(command)


def command_from_env(env_var: str) -> list[str] | None:
    configured_command = os.environ.get(env_var)
    if not configured_command:
        return None
    return shlex.split(configured_command, posix=not IS_WINDOWS)


def resolve_maven_command(project_dir: Path) -> list[str]:
    env_command = command_from_env("MAVEN_CMD")
    if env_command is not None:
        return env_command

    windows_wrapper = project_dir / "mvnw.cmd"
    unix_wrapper = project_dir / "mvnw"
    if IS_WINDOWS and windows_wrapper.exists():
        return [str(windows_wrapper)]
    if unix_wrapper.exists():
        if os.access(unix_wrapper, os.X_OK):
            return [str(unix_wrapper)]
        return ["sh", str(unix_wrapper)]

    executable = shutil.which("mvn.cmd" if IS_WINDOWS else "mvn") or shutil.which("mvn")
    if executable is not None:
        return [executable]

    raise RuntimeError(missing_maven_message(project_dir))


def resolve_java_command() -> list[str]:
    env_command = command_from_env("JAVA_CMD")
    if env_command is not None:
        return env_command

    executable = shutil.which("java.exe" if IS_WINDOWS else "java") or shutil.which("java")
    if executable is not None:
        return [executable]

    raise RuntimeError(
        "Java was not found. Install a JDK/JRE, add java to PATH, or set JAVA_CMD "
        "to the Java command."
    )


def missing_maven_message(project_dir: Path) -> str:
    lines = [
        "Maven was not found.",
        "",
        "Install Maven, add it to PATH, add a Maven Wrapper to the project, or set MAVEN_CMD.",
        f"Project checked: {project_dir}",
        "",
        "Suggested install commands:",
    ]

    system_name = platform.system()
    if system_name == "Linux":
        lines.extend([
            "  Debian/Ubuntu: sudo apt update && sudo apt install maven",
            "  Fedora/RHEL:   sudo dnf install maven",
            "  Arch:         sudo pacman -S maven",
        ])
    elif system_name == "Darwin":
        lines.append("  macOS/Homebrew: brew install maven")
    elif system_name == "Windows":
        lines.extend([
            "  Windows winget: winget install Apache.Maven",
            "  Windows choco:  choco install maven",
        ])
    else:
        lines.append("  See https://maven.apache.org/install.html")

    lines.extend([
        "",
        "After installing, open a new terminal and run this script again.",
    ])
    return "\n".join(lines)


def filter_maven_warning_output(output: str) -> str:
    return "\n".join(
        line
        for line in output.splitlines()
        if not any(fragment in line for fragment in UNSAFE_WARNING_FRAGMENTS)
    )


def run_compile(command: list[str], cwd: Path) -> None:
    process = subprocess.run(
        command,
        cwd=cwd,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        check=False,
    )

    stdout = filter_maven_warning_output(process.stdout)
    stderr = filter_maven_warning_output(process.stderr)
    if stdout:
        print(stdout, flush=True)
    if stderr:
        print(stderr, file=sys.stderr, flush=True)

    if process.returncode != 0:
        raise subprocess.CalledProcessError(process.returncode, command)


def start_process(command: list[str], cwd: Path) -> subprocess.Popen:
    print(f"\nStarting: {format_command(command)}\n", flush=True)
    return subprocess.Popen(command, cwd=cwd)


def terminate(process: subprocess.Popen | None, name: str) -> None:
    if process is None or process.poll() is not None:
        return

    print(f"Stopping {name}...", flush=True)
    process.terminate()
    try:
        process.wait(timeout=8)
    except subprocess.TimeoutExpired:
        process.kill()


def main() -> int:
    parser = argparse.ArgumentParser(description="Start GSMS datastore and alertmanager services.")
    parser.add_argument("--project-dir", type=Path, default=DEFAULT_PROJECT_DIR)
    parser.add_argument("--skip-compile", action="store_true")
    args = parser.parse_args()

    project_dir = args.project_dir.resolve()
    if not (project_dir / "pom.xml").exists() or not (project_dir / "src").is_dir():
        print(f"Expected project root above src/ and pom.xml, got {project_dir}", file=sys.stderr)
        return 1

    try:
        java_command = resolve_java_command()
    except RuntimeError as error:
        print(error, file=sys.stderr)
        return 1

    target_classes = project_dir / "target" / "classes"

    if not args.skip_compile:
        try:
            maven_command = resolve_maven_command(project_dir)
        except RuntimeError as error:
            print(error, file=sys.stderr)
            return 1

        compile_command = [*maven_command, "-q", "-DskipTests", "compile"]
        print(f"Compiling project: {format_command(compile_command)}", flush=True)
        try:
            run_compile(compile_command, project_dir)
        except subprocess.CalledProcessError as error:
            print(f"Compilation failed with exit code {error.returncode}.", file=sys.stderr)
            return error.returncode or 1
    elif not target_classes.is_dir():
        print(
            f"Compiled classes were not found at {target_classes}. "
            "Run without --skip-compile first, or compile the project manually.",
            file=sys.stderr,
        )
        return 1

    classpath = os.pathsep.join([str(target_classes)])
    data_store = None
    alert_manager = None

    try:
        data_store = start_process(
            [
                *java_command,
                "-cp",
                classpath,
                "datastore.StartDataStore",
            ],
            project_dir,
        )
        time.sleep(2)

        if data_store.poll() is not None:
            print("datastore exited before alertmanager could start.", file=sys.stderr)
            return data_store.returncode or 1

        alert_manager = start_process(
            [
                *java_command,
                "-cp",
                classpath,
                "alertmanager.StartAlertManager",
            ],
            project_dir,
        )

        while True:
            if data_store.poll() is not None:
                print("datastore exited.", file=sys.stderr)
                return data_store.returncode or 1
            if alert_manager.poll() is not None:
                print("alertmanager exited.", file=sys.stderr)
                return alert_manager.returncode or 1
            time.sleep(1)
    except KeyboardInterrupt:
        print("Shutdown requested.", flush=True)
        return 0
    finally:
        terminate(alert_manager, "alertmanager")
        terminate(data_store, "datastore")


if __name__ == "__main__":
    raise SystemExit(main())
