# SmartInventory Frontend

React + TypeScript + Vite alapu admin felulet a SmartInventory backendhez.

## Funkciok

- admin bejelentkezes Basic Auth-tal
- jarmulista es VIN kereses
- importalt koltsegosszeg megjelenitese
- Excel `.xlsx` jarmuimport feltoltese
- import history sorhibakkal

## Fejlesztes

```bash
npm install
npm run dev
```

A fejlesztoi szerver alapertelmezett cime:

```txt
http://127.0.0.1:5173/
```

A Vite proxy a Spring Boot backendet `http://localhost:8080` cimen eri el.

## Ellenorzes

```bash
npm run build
npm run lint
```
