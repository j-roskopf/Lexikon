# GitHub Actions — Lexikon

## Overview

| Workflow | Trigger | Purpose |
|----------|---------|---------|
| `pr-checks.yml` | PR to `main` | Desktop tests, Roborazzi, wasm unit, Playwright |
| `release.yml` | Push to `main` | Version bump, unsigned desktop packages, production Pages |
| `record-screenshot-baselines.yml` | Manual | Re-record Roborazzi baselines |

## Hostnames

- Production: `lexikon.joetr.com` (GitHub Pages on main repo)

## Desktop packages

Release builds publish unsigned DEB, MSI, and DMG artifacts to GitHub Releases. No code signing or notarization is configured.

## DNS (out of scope for CI)

Point CNAME when ready:

- `lexikon.joetr.com` → GitHub Pages for main repo

## Local commands

```bash
./gradlew desktopTest verifyRoborazziDesktop wasmJsTest
./gradlew wasmJsBrowserDistribution
npm run web:screenshots
```
