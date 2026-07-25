# Lexikon

A Compose Multiplatform Wordle-like word puzzle for **web (Wasm)** and **desktop JVM**.

## Play

- **Daily mode** — one puzzle per word length (5–10 letters), deterministic across platforms
- **Free play** — random word, ephemeral on refresh
- **Hard mode** — revealed letters must be reused
- **Colorblind palette** — accessible tile colors

## Development

### Desktop

```bash
./gradlew run
```

### Web (dev server)

```bash
./gradlew wasmJsBrowserDevelopmentRun
```

Opens at http://localhost:10001

### Web launcher (static dist)

```bash
./gradlew :web-launcher:run
```

Serves the production Wasm bundle at http://localhost:10001

## Routes (web)

| Path | Mode |
|------|------|
| `/` | Daily, last-used length and difficulty |
| `/daily/{n}` | Daily, length n (5–10), medium difficulty |
| `/daily/{n}/{difficulty}` | Daily, length n, `easy`/`medium`/`hard` |
| `/free/{n}` | Free play, length n, medium difficulty |
| `/free/{n}/{difficulty}` | Free play, length n, given difficulty |

Each day has an independent easy, medium and hard word per length, and progress
and stats are tracked separately for each combination. Difficulty only changes
which word you get — the guess budget stays at `length + 1` throughout.

## Word lists

`src/commonMain/composeResources/files/words/` is generated, not hand-edited:

```bash
python3 scripts/generate-wordlists.py           # regenerate
python3 scripts/generate-wordlists.py --verify  # exit 1 if outputs would change
```

Guesses accept anything in a large open spelling dictionary. Answers are much
narrower — a word must also be common in the Google Web Trillion Word Corpus,
not a proper noun, and not profanity — then split into easy/medium/hard by
frequency rank plus how awkward it is to spell. See the script's docstring.

## Tests

```bash
./gradlew desktopTest wasmJsTest
./gradlew verifyRoborazziDesktop
npm run web:screenshots
```

## Production

- Web: https://lexikon.joetr.com

See [docs/github-actions.md](docs/github-actions.md) for CI/CD setup.
