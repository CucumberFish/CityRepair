# City Repair Frontend

Vue 3 + Vite + Element Plus frontend for the city repair and work order processing system.

## Run

```bash
npm install
npm run dev
```

Frontend URL:

```text
http://localhost:5173/
```

The Vite dev server proxies `/api` to:

```text
http://localhost:8080
```

Keep backend `server.servlet.context-path` as `/api`, so frontend requests such as `/api/health` work without extra path rewriting.
