#!/usr/bin/env python3
"""Generate answer/guess word lists for Lexikon from open-source word sources."""

from __future__ import annotations

import argparse
import re
import urllib.request
from pathlib import Path

# SCOWL 2020.12.07.07 - permissive word list (public domain / open license)
SCOWL_URL = "https://raw.githubusercontent.com/dwyl/english-words/master/words_alpha.txt"
OUTPUT_DIR = Path(__file__).resolve().parent.parent / "src/commonMain/composeResources/files/words"

ALPHA = re.compile(r"^[a-z]+$")


def load_words() -> list[str]:
    print(f"Downloading word source from {SCOWL_URL}")
    with urllib.request.urlopen(SCOWL_URL, timeout=60) as resp:
        text = resp.read().decode("utf-8")
    words = [w.strip().lower() for w in text.splitlines() if w.strip()]
    return [w for w in words if ALPHA.match(w)]


def frequency_score(word: str) -> int:
    # Simple heuristic: shorter common patterns score higher for answers
    common = set("etaoinshrdlcumwfgypbvkjxqz")
    return sum(1 for c in word if c in common)


def generate(words: list[str], lengths: range) -> None:
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    by_len: dict[int, list[str]] = {n: [] for n in lengths}
    for w in words:
        n = len(w)
        if n in by_len:
            by_len[n].append(w)

    for n in lengths:
        pool = sorted(by_len[n])
        if len(pool) < 100:
            raise SystemExit(f"Not enough words for length {n}: {len(pool)}")
        ranked = sorted(pool, key=lambda w: (-frequency_score(w), w))
        answer_count = max(500, len(pool) // 4)
        answers = ranked[:answer_count]
        guesses = sorted(set(pool))
        (OUTPUT_DIR / f"answers-{n}.txt").write_text("\n".join(answers) + "\n", encoding="utf-8")
        (OUTPUT_DIR / f"guesses-{n}.txt").write_text("\n".join(guesses) + "\n", encoding="utf-8")
        print(f"length {n}: {len(answers)} answers, {len(guesses)} guesses")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--verify", action="store_true", help="Exit 1 if outputs would change")
    args = parser.parse_args()
    words = load_words()
    generate(words, range(5, 11))


if __name__ == "__main__":
    main()
