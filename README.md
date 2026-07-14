# UPrep

Learning-management platform (LMS + CMDS) — a modern Next.js UI on top of the
UPrep backend services, with native mobile wrappers.

The **actively maintained stack lives in [`platform/`](platform/)**
(backend, web, website, mobile, observability, deploy, docs). Start there.

For the full architecture and the legacy-to-cloud migration story, see
[`platform/docs/UPREP_ARCHITECTURE_AND_MIGRATION.md`](platform/docs/UPREP_ARCHITECTURE_AND_MIGRATION.md).

## Repository layout

| Path | What it is |
|------|------------|
| `platform/` | **The modern platform** — everything currently maintained. See [`platform/README.md`](platform/README.md). |
| `legacy/` | **Everything old**, kept for reference only: the Play Framework system (`lms-master`), local seed scripts (`lms-local`), and supporting legacy microservices (`proxy-server-master`, `secure-api-master`, `sdcard-server-master`, `organisation-logos-master`, `scripts-master`). Not required to build or run the platform. |

The repo is a clean two-way split: **`platform/` = new/maintained**, **`legacy/`
= old/reference**. Everything under `platform/` is self-contained.

## Quick start

```bash
cd platform
cp .env.example .env        # fill in values (never commit .env)
docker compose up -d --build
```

Full instructions are in [`platform/README.md`](platform/README.md).

## Notes

- Secrets (`.env`, keystores, `*.pem`) are git-ignored — never commit them.
- Large legacy/peripheral projects are kept for reference; they are not part of
  the maintained platform.
