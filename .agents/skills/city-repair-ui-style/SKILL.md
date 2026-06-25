---
name: city-repair-ui-style
description: UI style rules for the City Repair and Work Order Processing System. Use whenever Codex or another coding agent creates or modifies Vue pages, layouts, Element Plus components, charts, forms, tables, status displays, screenshots, or frontend documentation for this project.
---

# City Repair UI Style

## Product Tone

Design a practical city public-service management tool. The interface should feel clear, restrained, trustworthy, and efficient.

Avoid marketing landing pages, decorative hero sections, purple neon gradients, glassmorphism, 3D dashboards, unrelated animations, and "technology big screen" styling.

## Fixed Visual Tokens

Use these tokens unless the existing project already defines equivalent variables:

| Token | Value | Use |
|---|---|---|
| Primary | `#1F5FBF` | primary buttons, selected menu, links |
| Deep blue | `#174A7E` | page titles, key text |
| Background | `#F5F7FA` | main page background |
| Surface | `#FFFFFF` | tables, forms, panels |
| Text | `#1F2937` | body and table text |
| Muted text | `#5F6B7A` | timestamps and helper text |
| Border | `#D9E1EA` | dividers, inputs, panels |
| Success | `#2E7D5B` | completed and evaluated |
| Warning | `#C97A10` | pending states |
| Danger | `#B83A3A` | reject, cancel, delete, errors |
| Card radius | `8px` | cards and panels |
| Control radius | `6px` | buttons and inputs |

Use Microsoft YaHei, PingFang SC, or Noto Sans CJK SC. Do not mix many font families.

## Layout

Use one shared application shell:

```text
left sidebar + top bar + main content
```

Do not let members build separate sidebars, headers, or route shells for their modules.

List pages follow:

```text
title/breadcrumb -> filters -> actions -> table -> pagination
```

Detail pages follow:

```text
back/title -> status and base info -> timeline -> images/results -> legal actions
```

Form pages follow:

```text
title -> grouped form -> upload -> cancel/submit actions
```

Statistics pages follow:

```text
title and date range -> 4 metric cards -> trend chart -> distribution charts -> worker ranking table
```

## Components

- Use the existing component library, preferably Element Plus.
- Each page should have at most one solid primary button.
- Use status tags for order status and priority.
- Use confirmation dialogs for delete, reject, cancel, and irreversible actions.
- Provide loading, empty, request-failed, validation-error, and success states.
- Use pagination for long lists.
- Use ECharts for charts and handle resize.

## Status Display

Map statuses consistently:

- `PENDING_REVIEW`, `PENDING_ASSIGN`, `PENDING_ACCEPT`: warning
- `PROCESSING`: primary
- `COMPLETED`, `EVALUATED`: success
- `REJECTED`, `CANCELLED`: danger or muted danger

Do not invent new status labels that conflict with backend enums.

## Forms And Tables

- Validate required fields on both frontend and backend.
- Disable submit while the request is in progress.
- Do not trust frontend current user IDs.
- Keep table columns scannable: order number, title, category, status, priority, owner/worker, updated time, actions.
- Long text should be truncated with tooltip or detail view, not force the table width to break.

## Responsive Requirements

The prototype is desktop-first, but pages must not be broken on common laptop widths. Keep controls wrapping cleanly in filter bars. Text must not overlap buttons, tags, or charts.

## Evidence

Every completed frontend module must provide screenshots for:

- normal successful state
- validation or permission error
- empty or loading state where applicable
- integrated API result, not static mock data
