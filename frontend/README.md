# Frontend Architecture

This frontend now uses a layered Vue 3 + Vite structure designed for a graduation-project style codebase while keeping the current campus lost-and-found pages runnable.

## Directory Guide

```text
src/
├── api/
│   ├── index.js
│   ├── interceptors.js
│   └── modules/
├── assets/
├── components/
│   ├── common/
│   └── business/
├── composables/
├── layouts/
├── router/
│   ├── index.js
│   ├── guards.js
│   └── modules/
├── stores/
│   ├── index.js
│   └── modules/
├── utils/
├── views/
│   ├── admin/
│   ├── auth/
│   └── user/
├── App.vue
└── main.js
```

## Key Decisions

- `axios` now lives in `src/api/` with centralized interceptors.
- business API calls are grouped under `src/api/modules/`.
- `Pinia` is initialized in `src/stores/`, with user auth state in `src/stores/modules/user.js`.
- route definitions are split by domain under `src/router/modules/`.
- auth and permission control now live in `src/router/guards.js`.
- reusable layout shells live in `src/layouts/`.
- common UI and business UI are separated under `src/components/common` and `src/components/business`.

## Current Notes

- The project still keeps `src/data/catalog.js` for local fallback/mock content during the current milestone.
- User-side and admin-side styles intentionally differ:
  - user side: portal / marketplace style
  - admin side: traditional management console

## Commands

```bash
npm install
npm run dev
npm run build
```
