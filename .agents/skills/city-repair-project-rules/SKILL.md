---
name: city-repair-project-rules
description: Project rules for the City Repair and Work Order Processing System. Use whenever Codex or another coding agent works on backend, frontend, database, API, tests, README, AI logs, GitHub collaboration, or delivery materials for this project.
---

# City Repair Project Rules

## Project Boundary

Build only one runnable, 5-minute-demonstrable business loop:

```text
resident submits repair
-> admin reviews
-> admin assigns worker
-> worker accepts and processes
-> worker completes
-> resident evaluates
-> admin views statistics
```

Do not expand the project into a general emergency platform, CRM, smart city suite, or unrelated management system.

## Fixed Stack

Use the existing repository stack first. If the repository has not been initialized, use:

- Backend: Spring Boot 3 + MyBatis-Plus
- Frontend: Vue 3 + Vite + Element Plus
- Database: MySQL 8
- Charts: ECharts
- API testing: Apifox or Postman
- Version control: Git + GitHub

Do not create a second login system, second request wrapper, second status enum, or second UI shell.

## Roles

- `RESIDENT`: create and view own repair orders, cancel own `PENDING_REVIEW` order, evaluate own `COMPLETED` order once.
- `ADMIN`: view all orders, approve, reject, set priority, assign workers, manage categories, view logs, evaluations, and statistics.
- `WORKER`: view only assigned orders, accept, process, upload result images, and complete assigned orders.

All permission checks must be enforced by backend code. Hiding frontend buttons is not enough.

## Status And Priority

Use only these order statuses:

```text
PENDING_REVIEW -> PENDING_ASSIGN | REJECTED | CANCELLED
PENDING_ASSIGN -> PENDING_ACCEPT
PENDING_ACCEPT -> PROCESSING
PROCESSING -> COMPLETED
COMPLETED -> EVALUATED
REJECTED, CANCELLED, EVALUATED are terminal
```

Use only these priorities:

```text
LOW, NORMAL, HIGH, URGENT
```

Do not add synonyms such as `ACCEPTED`, `REPAIRING`, `CLOSED`, `TODO`, or `SOLVED`.

## Fixed Tables

Use the SQL contract in `sql/init_city_repair.sql` as the baseline. Core tables:

- `sys_user`, `sys_role`, `sys_user_role`
- `repair_category`
- `repair_order`
- `order_assignment`
- `order_status_log`
- `order_attachment`
- `order_evaluation`
- `user_notification`

If a starter framework already has `sys_user` or role tables, reuse and adapt them instead of adding another user table.

## Transaction Rules

Keep each state-changing business operation in one transaction:

- update `repair_order`
- write `order_status_log`
- write related records such as assignment, attachment, evaluation, or notification

Reject invalid transitions, repeated submissions, cross-user access, and updates based on stale state.

## API Rules

- Use RESTful paths under `/api`.
- Use lowercase paths and hyphen-separated resource names.
- Use the repository's existing response wrapper. If none exists, use `code`, `message`, `data`, `timestamp`.
- Never return raw database stack traces to the frontend.
- Get the current user from login context, not request body.
- Pagination uses `page` starting from 1 and `pageSize` capped at 100.

The fixed API boundary is documented in `docs/数据库与接口定稿.md`.

## Git And Delivery Rules

- `main`: final stable branch, maintained by the team leader.
- `develop`: integration branch, merged only by the team leader or an authorized reviewer.
- Members work only on their assigned `feat/*` branch.
- Each member must keep at least 3 meaningful commits.
- Before merge, the member must provide run commands, test results, screenshots, and an AI collaboration log.

Do not push `node_modules`, `target`, `dist`, logs, IDE temp files, real passwords, API keys, or large unrelated files.

## Before Coding

Before editing code, output:

- task boundary
- files likely to change
- APIs likely to add or call
- database tables affected
- risks and questions

After coding, output:

- changed files
- API changes
- database changes
- commands actually run
- test results
- screenshot or log paths
- remaining risks

Do not claim tests passed without actually running commands or producing evidence.
