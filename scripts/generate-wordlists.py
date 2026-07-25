#!/usr/bin/env python3
"""Generate answer/guess word lists for Lexikon from open-source word sources.

Guesses are permissive: anything in a large open spelling dictionary is accepted
so players are rarely told a real word "isn't a word".

Answers are deliberately narrow. A word only becomes an answer if it is:
  1. spelled validly (present in the dwyl english-words list),
  2. genuinely common (ranked in the Google Web Trillion Word Corpus above a
     minimum occurrence count), and
  3. not a proper noun (person/place/brand names are filtered out using a
     case-preserving Hunspell dictionary plus name lists), and
  4. not profanity or an adult term (the web corpus is full of both).

That combination is what keeps esoterica like "aalii" and "abacaxi" out of the
answer pool.

The surviving answers are then split into easy/medium/hard tiers. Tiering is
mostly frequency rank, nudged by how awkward the word is to guess: rare letters,
repeated letters and a low vowel count all push a word toward "hard" even if it
is a household word. Every tier is drawn from the same vetted pool, so "hard"
means tricky, never obscure.
"""

from __future__ import annotations

import argparse
import re
import sys
import urllib.request
from pathlib import Path

# Spelling validity: large permissive word list (public domain / open license).
WORDS_URL = "https://raw.githubusercontent.com/dwyl/english-words/master/words_alpha.txt"
# Word frequency: Google Web Trillion Word Corpus unigram counts (Peter Norvig).
FREQUENCY_URL = "https://norvig.com/ngrams/count_1w.txt"
# Case-preserving Hunspell dictionary: capitalized entries mark proper nouns.
HUNSPELL_URL = "https://raw.githubusercontent.com/wooorm/dictionaries/main/dictionaries/en/index.dic"
# Profanity/adult terms. The web corpus is full of them; they make poor answers.
BAD_WORDS_URL = (
    "https://raw.githubusercontent.com/LDNOOBW/"
    "List-of-Dirty-Naughty-Obscene-and-Otherwise-Bad-Words/master/en"
)
# Given names and surnames, to catch proper nouns Hunspell doesn't carry.
NAME_URLS = (
    "https://raw.githubusercontent.com/dominictarr/random-name/master/first-names.txt",
    "https://raw.githubusercontent.com/dominictarr/random-name/master/names.txt",
)

OUTPUT_DIR = Path(__file__).resolve().parent.parent / "src/commonMain/composeResources/files/words"
LENGTHS = range(5, 11)

ALPHA = re.compile(r"^[a-z]+$")
VOWELS = set("aeiouy")

# A word must appear at least this many times in the ~1 trillion word corpus to
# be answer-worthy. Tuned by inspection: below roughly this line the lists start
# filling with jargon, abbreviations and obscure technical terms.
MIN_OCCURRENCES = 2_000_000
# Hard caps so no single length dominates, and so short lists stay playable.
MAX_ANSWERS = 2_500
MIN_ANSWERS = 750

DIFFICULTIES = ("easy", "medium", "hard")
# Letters that make a word awkward to guess, and how much they hurt.
RARE_LETTERS = {
    "j": 1.0, "q": 1.0, "x": 1.0, "z": 1.0,
    "v": 0.6, "w": 0.5, "k": 0.5, "f": 0.3, "y": 0.3, "b": 0.2,
}
# Weights for the trickiness nudges, relative to frequency rank (which is 0..1).
RARE_LETTER_WEIGHT = 0.25
REPEAT_WEIGHT = 0.10
LOW_VOWEL_WEIGHT = 0.10
LOW_VOWEL_RATIO = 0.3


def fetch(url: str) -> str:
    print(f"Downloading {url}")
    with urllib.request.urlopen(url, timeout=120) as resp:
        return resp.read().decode("utf-8", errors="replace")


def load_valid_words() -> set[str]:
    return {w for w in (l.strip().lower() for l in fetch(WORDS_URL).splitlines()) if ALPHA.match(w)}


def load_frequencies() -> list[tuple[str, int]]:
    """Return (word, count) pairs ordered most frequent first, deduplicated."""
    ranked: list[tuple[str, int]] = []
    seen: set[str] = set()
    for line in fetch(FREQUENCY_URL).splitlines():
        parts = line.split("\t")
        if len(parts) != 2:
            continue
        word = parts[0].strip().lower()
        if not ALPHA.match(word) or word in seen:
            continue
        seen.add(word)
        ranked.append((word, int(parts[1])))
    ranked.sort(key=lambda item: (-item[1], item[0]))
    return ranked


def load_proper_nouns() -> set[str]:
    """Words that are (almost) always capitalized, so unfair as answers.

    Hunspell entries are `word/FLAGS`; a capitalized root means a proper noun.
    Name lists are only trusted when the word never appears lowercase in
    Hunspell, so ordinary nouns that double as surnames ("cook", "stone",
    "green") survive.
    """
    capitalized: set[str] = set()
    lowercase: set[str] = set()
    for line in fetch(HUNSPELL_URL).splitlines():
        root = line.split("/")[0].strip()
        if not root.isalpha():
            continue
        (capitalized if root[0].isupper() else lowercase).add(root.lower())

    names: set[str] = set()
    for url in NAME_URLS:
        names |= {w.strip().lower() for w in fetch(url).splitlines() if w.strip().isalpha()}

    return (capitalized | names) - lowercase


def load_blocked_words() -> set[str]:
    """Profanity and adult terms, plus their common inflections."""
    seeds = set()
    for line in fetch(BAD_WORDS_URL).splitlines():
        # Entries may be phrases; every alphabetic token in one is off limits.
        seeds |= {t for t in line.strip().lower().split() if ALPHA.match(t) and len(t) >= 3}
    blocked = set(seeds)
    for word in seeds:
        blocked |= {word + s for s in ("s", "es", "ed", "ing", "er", "ers", "y")}
        if word.endswith("e"):
            blocked |= {word[:-1] + s for s in ("ed", "ing", "y")}
    return blocked


def choose_answers(
    ranked: list[tuple[str, int]],
    valid: set[str],
    excluded: set[str],
    length: int,
) -> list[str]:
    candidates = [
        word
        for word, _ in ranked
        if len(word) == length
        and word in valid
        and word not in excluded
        and any(c in VOWELS for c in word)
    ]
    common = [word for word, count in ranked if count >= MIN_OCCURRENCES]
    common_set = set(common)
    answers = [w for w in candidates if w in common_set][:MAX_ANSWERS]
    if len(answers) < MIN_ANSWERS:
        # Rare for long words: fall back to the most frequent candidates we have.
        answers = candidates[:MIN_ANSWERS]
    return answers


def difficulty_score(word: str, rank: int, pool_size: int) -> float:
    """Lower is easier. Frequency rank dominates; awkward spelling nudges upward."""
    score = rank / max(pool_size - 1, 1)
    score += RARE_LETTER_WEIGHT * sum(RARE_LETTERS.get(c, 0.0) for c in set(word)) / len(word)
    if len(set(word)) < len(word):
        score += REPEAT_WEIGHT
    if sum(1 for c in word if c in VOWELS) / len(word) < LOW_VOWEL_RATIO:
        score += LOW_VOWEL_WEIGHT
    return score


def split_by_difficulty(answers: list[str]) -> dict[str, list[str]]:
    """Partition frequency-ordered answers into equal easy/medium/hard thirds."""
    rank = {word: i for i, word in enumerate(answers)}
    scored = sorted(answers, key=lambda w: (difficulty_score(w, rank[w], len(answers)), w))
    size = len(scored) // len(DIFFICULTIES)
    bounds = [0, size, 2 * size, len(scored)]
    return {
        tier: sorted(scored[bounds[i]:bounds[i + 1]])
        for i, tier in enumerate(DIFFICULTIES)
    }


def generate(verify: bool) -> int:
    valid = load_valid_words()
    ranked = load_frequencies()
    proper = load_proper_nouns()
    blocked = load_blocked_words()
    excluded = proper | blocked
    print(
        f"{len(valid)} valid words, {len(ranked)} ranked words, "
        f"{len(proper)} proper nouns, {len(blocked)} blocked words"
    )

    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    changed = False
    for length in LENGTHS:
        guesses = sorted(w for w in valid if len(w) == length)
        if len(guesses) < 100:
            raise SystemExit(f"Not enough words for length {length}: {len(guesses)}")

        answers = choose_answers(ranked, valid, excluded, length)
        if len(answers) < 100:
            raise SystemExit(f"Not enough answers for length {length}: {len(answers)}")

        tiers = split_by_difficulty(answers)
        # Every answer must be a legal guess, and the tiers must partition the pool.
        assert set(answers) <= set(guesses)
        assert sum(len(t) for t in tiers.values()) == len(answers)

        files = {f"guesses-{length}.txt": guesses}
        for tier, words in tiers.items():
            if len(words) < 100:
                raise SystemExit(f"Not enough {tier} answers for length {length}: {len(words)}")
            files[f"answers-{length}-{tier}.txt"] = words

        for name, words in files.items():
            path = OUTPUT_DIR / name
            content = "\n".join(words) + "\n"
            if verify:
                existing = path.read_text(encoding="utf-8") if path.exists() else None
                if existing != content:
                    print(f"{name} would change")
                    changed = True
            else:
                path.write_text(content, encoding="utf-8")

        counts = ", ".join(f"{t} {len(tiers[t])}" for t in DIFFICULTIES)
        print(f"length {length}: {len(answers)} answers ({counts}), {len(guesses)} guesses")

    # Untiered answer files predate difficulty support; the app no longer reads them.
    for stale in (OUTPUT_DIR / f"answers-{n}.txt" for n in LENGTHS):
        if stale.exists():
            if verify:
                print(f"{stale.name} would be removed")
                changed = True
            else:
                stale.unlink()

    return 1 if (verify and changed) else 0


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--verify", action="store_true", help="Exit 1 if outputs would change")
    args = parser.parse_args()
    sys.exit(generate(args.verify))


if __name__ == "__main__":
    main()
