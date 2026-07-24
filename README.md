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
| `/` | Daily, last-used length |
| `/daily/{n}` | Daily, length n (5–10) |
| `/free/{n}` | Free play, length n |

## Tests

```bash
./gradlew desktopTest wasmJsTest
./gradlew verifyRoborazziDesktop
npm run web:screenshots
```

## Production

- Web: https://lexikon.joetr.com

See [docs/github-actions.md](docs/github-actions.md) for CI/CD setup.
