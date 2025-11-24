#!/usr/bin/env python3
"""Generate the scheduled jobs table in the README from BPMN definitions."""

from __future__ import annotations

import json
import sys
import xml.etree.ElementTree as ET
from collections import OrderedDict
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent
CAMUNDA_DIR = PROJECT_ROOT / "src" / "main" / "resources" / "camunda"
METADATA_PATH = PROJECT_ROOT / "config" / "scheduled-jobs.json"
README_PATH = PROJECT_ROOT / "README.md"
TABLE_START = "<!-- SCHEDULED_JOBS_TABLE_START -->"
TABLE_END = "<!-- SCHEDULED_JOBS_TABLE_END -->"

NS = {
    "bpmn": "http://www.omg.org/spec/BPMN/20100524/MODEL",
    "camunda": "http://camunda.org/schema/1.0/bpmn",
}


def load_metadata() -> dict[str, dict[str, str]]:
    if not METADATA_PATH.exists():
        return {}
    return json.loads(METADATA_PATH.read_text())


def format_topics(topics: list[str]) -> str:
    if not topics:
        return ""
    return "<br>".join(f"`{topic}`" for topic in topics)


def describe_cron(expr: str) -> str:
    parts = expr.split()
    if len(parts) not in (6, 7):
        return f"See cron `{expr}`"

    second, minute, hour, dom, month, dow, *_ = parts
    year = parts[6] if len(parts) == 7 else None

    def fmt_time(hour_str: str, minute_str: str) -> str:
        return f"{int(hour_str):02d}:{int(minute_str):02d}"

    def join_times(times: list[str]) -> str:
        if len(times) == 1:
            return times[0]
        if len(times) == 2:
            return f"{times[0]} and {times[1]}"
        return ", ".join(times[:-1]) + f", and {times[-1]}"

    def describe_daily(time_text: str) -> str:
        return f"Daily at {time_text}"

    if "-" in hour:
        start, end = hour.split("-")
        start_time = fmt_time(start, minute)
        end_time = fmt_time(end, minute)
        minute_text = "the top of the hour" if minute == "0" else f"minute {int(minute):02d} past the hour"
        return f"Hourly at {minute_text} from {start_time}–{end_time}"

    if "," in hour:
        times = [fmt_time(token, minute) for token in hour.split(",")]
        frequency = {2: "Twice daily", 3: "Three times daily"}.get(len(times), "Daily")
        return f"{frequency} at {join_times(times)}"

    base = None
    def append_year(text: str) -> str:
        if year not in (None, "*", "?"):
            return f"{text} until {year}"
        return text

    if dom == "1" and month == "*" and dow in {"?", "*"}:
        base = append_year("First day of each month")
    elif dom in {"*", "?"} and dow in {"*", "?"} and month == "*":
        base = append_year("Daily")

    time_text = fmt_time(hour, minute)
    if base:
        return f"{base} at {time_text}"
    return f"See cron `{expr}`"


def gather_jobs() -> list[dict[str, str]]:
    jobs: list[dict[str, str]] = []
    for path in sorted(CAMUNDA_DIR.rglob("*.bpmn")):
        text = path.read_text()
        if "timeCycle" not in text:
            continue
        tree = ET.fromstring(text)
        process = tree.find("bpmn:process", NS)
        if process is None:
            continue
        timers = [
            node.find("bpmn:timeCycle", NS).text.strip()
            for node in process.findall(".//bpmn:timerEventDefinition", NS)
            if node.find("bpmn:timeCycle", NS) is not None and node.find("bpmn:timeCycle", NS).text
        ]
        if not timers:
            continue
        topics_ordered: OrderedDict[str, None] = OrderedDict()
        for task in process.findall(".//bpmn:serviceTask", NS):
            topic = task.get("{http://camunda.org/schema/1.0/bpmn}topic")
            if topic:
                topics_ordered.setdefault(topic, None)
        jobs.append(
            {
                "name": process.get("name"),
                "cron": "\n".join(timers),
                "topics": list(topics_ordered.keys()),
            }
        )
    jobs.sort(key=lambda item: item["name"].lower())
    return jobs


def build_table(jobs: list[dict[str, str]], metadata: dict[str, dict[str, str]]) -> str:
    lines = [
        "| Job | Purpose | Camunda topic(s) | Schedule (cron, UTC) | When it runs |",
        "| --- | --- | --- | --- | --- |",
    ]
    for job in jobs:
        name = job["name"]
        purpose = metadata.get(name, {}).get("purpose", "TODO: Describe this job.")
        topics = format_topics(job["topics"])
        cron_expr = job["cron"]
        when = describe_cron(cron_expr)
        lines.append(
            f"| {name} | {purpose} | {topics} | `{cron_expr}` | {when} |"
        )
    return "\n".join(lines)


def update_readme(table_markdown: str) -> None:
    content = README_PATH.read_text()
    if TABLE_START not in content or TABLE_END not in content:
        sys.exit("Unable to locate scheduled jobs markers in README.md")
    start_index = content.index(TABLE_START)
    end_index = content.index(TABLE_END, start_index) + len(TABLE_END)
    replacement = f"{TABLE_START}\n{table_markdown}\n{TABLE_END}"
    updated = content[:start_index] + replacement + content[end_index:]
    README_PATH.write_text(updated)


def main() -> None:
    metadata = load_metadata()
    jobs = gather_jobs()
    table = build_table(jobs, metadata)
    update_readme(table)
    print(f"Updated scheduled jobs table with {len(jobs)} entries.")


if __name__ == "__main__":
    main()
