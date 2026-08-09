#!/usr/bin/env python3
"""
Deterministic, pattern-based failure triage for CI.

Reads the JUnit XML produced by `./gradlew test` (build/test-results/test/TEST-*.xml -
Gradle's XML writer, driven by TestNG), pulls out every <failure>/<error>, classifies it
against a small set of known failure signatures this project has actually hit, and prints
a short GitHub-Flavored Markdown report meant to be appended to $GITHUB_STEP_SUMMARY.

Deliberately NOT an LLM/API call - pure regex/string matching over the XML already on
disk, so it needs no network access and no secrets. Run only `if: failure()`, right after
the test step, before the Allure report step.

Usage:
    python3 scripts/ci/triage-failures.py [results_dir] >> "$GITHUB_STEP_SUMMARY"

`results_dir` defaults to build/test-results/test (Gradle's default location).
"""

from __future__ import annotations

import glob
import os
import re
import sys
import xml.etree.ElementTree as ET
from dataclasses import dataclass, field

DEFAULT_RESULTS_DIR = "build/test-results/test"
EXCERPT_MAX_LINES = 5
EXCERPT_MAX_CHARS = 500

# Lines that are pure WebDriver capability/environment noise - useful in a full log, not
# in a 5-line excerpt. Anything from here down in a message gets truncated away.
NOISE_LINE_PREFIXES = (
    "Build info:",
    "Driver info:",
    "Command:",
    "Capabilities {",
    "System info:",
    "at ",  # stack trace frames when they leak into the `message` attribute
)


@dataclass
class Failure:
    classname: str
    method: str
    kind: str  # "failure" or "error"
    exc_type: str
    message: str
    category: str
    explanation: str
    excerpt: str


@dataclass
class Totals:
    tests: int = 0
    failures: int = 0
    errors: int = 0
    skipped: int = 0
    suites_parsed: int = 0


def find_result_files(results_dir: str) -> list[str]:
    return sorted(glob.glob(os.path.join(results_dir, "TEST-*.xml")))


def first_meaningful_line(text: str) -> str:
    """First non-blank line of a message - the human-readable summary in practice."""
    if not text:
        return ""
    for line in text.splitlines():
        stripped = line.strip()
        if stripped:
            return stripped
    return ""


def build_excerpt(message: str, body: str) -> str:
    """
    A short, readable excerpt: prefer the `message` attribute (already the concise
    summary in TestNG/Gradle's XML), falling back to the element body (stack trace text)
    if `message` is empty. Truncates at the first WebDriver capabilities/stack-frame noise
    line and caps at EXCERPT_MAX_LINES / EXCERPT_MAX_CHARS either way.
    """
    source = message.strip() if message and message.strip() else body.strip()
    if not source:
        return "(no message captured)"

    lines = []
    for raw_line in source.splitlines():
        line = raw_line.rstrip()
        if not line.strip():
            continue
        if any(line.strip().startswith(p) for p in NOISE_LINE_PREFIXES):
            break
        lines.append(line.strip())
        if len(lines) >= EXCERPT_MAX_LINES:
            break

    excerpt = "\n".join(lines) if lines else first_meaningful_line(source)
    if len(excerpt) > EXCERPT_MAX_CHARS:
        excerpt = excerpt[:EXCERPT_MAX_CHARS].rstrip() + " …(truncated)"
    return excerpt


def classify(exc_type: str, message: str) -> tuple[str, str]:
    """
    Returns (category, explanation) for a failed/errored testcase, based on known
    signatures this project has actually hit. Order matters - more specific patterns are
    checked before generic ones (e.g. an HTTP-status AssertionError before a plain
    AssertionError).
    """
    t = exc_type or ""
    m = message or ""

    # 1. Browser failed to launch (SessionNotCreatedException, Chrome process died).
    if "SessionNotCreatedException" in t or re.search(
        r"session not created|chrome (instance )?exited|process.*unexpectedly closed|chrome not reachable",
        m,
        re.I,
    ):
        return (
            "Browser failed to launch",
            "Browser/driver could not start (sandbox, headless flag, or driver-binary "
            "version mismatch). Not a real test failure - check the CI environment/browser "
            "setup, not the test logic.",
        )

    # 2. Selenide element/wait-condition failures.
    if re.search(r"com\.codeborne\.selenide\.ex\.(ElementNotFound|ElementShould|ElementShouldNot)", t):
        return (
            "UI element/timing issue",
            "Locator didn't match in time, or a wait condition wasn't met. Check for a "
            "real selector change vs. a flaky timing race.",
        )

    # 3. API status-code mismatches - a specific, more informative case of AssertionError,
    #    so this must be checked before the generic assertion bucket below.
    if re.search(r"expected\s*\[?\s*\d{3}\s*\]?\s*but\s*(found|was)\s*\[?\s*\d{3}\s*\]?", m, re.I) or re.search(
        r"expected status code\s*<?\d{3}>?\s*doesn'?t match actual status code\s*<?\d{3}>?", m, re.I
    ):
        return (
            "Unexpected API status code",
            "API returned an unexpected HTTP status code - check the backend/endpoint behavior.",
        )

    # 4. Network/connectivity issues reaching a target site or API.
    if re.search(r"\b(ConnectException|UnknownHostException|SocketTimeoutException)\b", t) or re.search(
        r"connection refused|name or service not known|nodename nor servname|"
        r"temporary failure in name resolution|err_name_not_resolved|err_connection|"
        r"failed to respond|connect timed out",
        m,
        re.I,
    ):
        return (
            "Network/connectivity issue",
            "Could not reach the target site or API - could be transient, or the target "
            "being down.",
        )

    # 5. Generic assertion failures - real test-logic/behavior mismatches.
    if re.search(r"\b(AssertionError|ComparisonFailure)\b", t) or re.search(
        r"^expected(\s*\[.*\])?\s*[:=]", m, re.I
    ):
        return (
            "Assertion failure",
            "A real assertion failed - actual test logic/behavior mismatch, read the "
            "expected-vs-actual values below.",
        )

    # 6. Anything else - surfaced, not silently dropped.
    return (
        "Unclassified failure",
        "Doesn't match a known signature - exception type and message shown below, review manually.",
    )


def parse_suite(path: str, totals: Totals) -> list[Failure]:
    failures: list[Failure] = []
    try:
        tree = ET.parse(path)
    except ET.ParseError as exc:
        failures.append(
            Failure(
                classname=os.path.basename(path),
                method="(unparseable result file)",
                kind="error",
                exc_type="XMLParseError",
                message=str(exc),
                category="Unclassified failure",
                explanation="This result file itself could not be parsed - inspect it directly.",
                excerpt=str(exc),
            )
        )
        return failures

    root = tree.getroot()
    suite = root if root.tag == "testsuite" else root.find("testsuite")
    if suite is None:
        return failures

    totals.suites_parsed += 1
    totals.tests += int(suite.get("tests", 0) or 0)
    totals.skipped += int(suite.get("skipped", 0) or 0)

    for testcase in suite.findall("testcase"):
        classname = testcase.get("classname", "?")
        method = testcase.get("name", "?")

        skip_el = testcase.find("skipped")
        if skip_el is not None:
            continue  # counted via suite totals above, not a failure

        for tag in ("failure", "error"):
            el = testcase.find(tag)
            if el is None:
                continue

            exc_type = el.get("type", "") or ""
            message = el.get("message", "") or ""
            body = el.text or ""

            # org.testng.SkipException surfaces as an <error> in some Gradle/TestNG
            # versions rather than a <skipped> element - treat it as a skip, not a failure.
            if "org.testng.SkipException" in exc_type:
                totals.skipped += 1
                continue

            if tag == "failure":
                totals.failures += 1
            else:
                totals.errors += 1

            category, explanation = classify(exc_type, message)
            excerpt = build_excerpt(message, body)

            failures.append(
                Failure(
                    classname=classname,
                    method=method,
                    kind=tag,
                    exc_type=exc_type or "(unknown exception type)",
                    message=message,
                    category=category,
                    explanation=explanation,
                    excerpt=excerpt,
                )
            )

    return failures


def render_markdown(failures: list[Failure], totals: Totals, results_dir: str, files_found: bool) -> str:
    lines: list[str] = []
    lines.append("## Failure Triage (automated, pattern-based)")
    lines.append("")

    if not files_found:
        lines.append(
            "**No JUnit XML test results were found** under `%s`. This usually means the "
            "build failed before any tests ran (e.g. a compilation error), not that tests "
            "failed - check the earlier build/compile step's log for the real cause."
            % results_dir
        )
        lines.append("")
        return "\n".join(lines)

    if not failures:
        lines.append(
            "No `<failure>`/`<error>` entries found in the JUnit XML, even though this job "
            "failed. The failure likely happened outside the test step (e.g. a later "
            "Gradle task, report generation, or an infrastructure issue) - check the raw "
            "job log."
        )
        lines.append("")
        return "\n".join(lines)

    # Verdict line: N failed: category breakdown.
    by_category: dict[str, int] = {}
    for f in failures:
        by_category[f.category] = by_category.get(f.category, 0) + 1
    breakdown = ", ".join(f"{count} {cat.lower()}" for cat, count in by_category.items())
    verdict = f"**{len(failures)} test(s) failed: {breakdown}.**"
    if totals.skipped:
        verdict += f" ({totals.skipped} skipped, excluded above.)"
    lines.append(verdict)
    lines.append("")

    for f in failures:
        lines.append(f"- **`{f.classname}.{f.method}`** — {f.category}")
        lines.append(f"  - *{f.explanation}*")
        lines.append(f"  - `{f.exc_type}`")
        excerpt_block = "\n".join(f"    {line}" for line in f.excerpt.splitlines()) or "    (no message captured)"
        lines.append("  - Excerpt:")
        lines.append("    ```")
        lines.append(excerpt_block.replace("    ```", "```"))
        lines.append("    ```")
        lines.append("")

    return "\n".join(lines)


def main() -> int:
    results_dir = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_RESULTS_DIR
    files = find_result_files(results_dir)

    totals = Totals()
    all_failures: list[Failure] = []

    for path in files:
        all_failures.extend(parse_suite(path, totals))

    print(render_markdown(all_failures, totals, results_dir, files_found=bool(files)))
    return 0


if __name__ == "__main__":
    sys.exit(main())
