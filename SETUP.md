# Setup-Anleitung - Docker Ready

## Voraussetzungen
- Docker Desktop installiert und gestartet
- Git installiert

## Schnellstart

### 1. Branch auschecken
```bash
git checkout neue-branch
```

### 2. Docker Container starten
```bash
docker-compose -f docker-compose.yml -f docker-compose.app.yml up -d
```

### 3. Warten bis App läuft (ca. 1-2 Minuten)
```bash
docker-compose -f docker-compose.yml -f docker-compose.app.yml logs -f app
```
Warte auf: `Started HotelBookingApplication`

### 4. App öffnen
```
http://localhost:8080
```

## Wichtige Befehle

**Container stoppen:**
```bash
docker-compose -f docker-compose.yml -f docker-compose.app.yml down
```

**Container starten:**
```bash
docker-compose -f docker-compose.yml -f docker-compose.app.yml up -d
```

**Status prüfen:**
```bash
docker-compose -f docker-compose.yml -f docker-compose.app.yml ps
```

**Logs anzeigen:**
```bash
docker-compose -f docker-compose.yml -f docker-compose.app.yml logs -f app
```

## Hinweise

- **Datenbank-Daten bleiben erhalten** beim Stoppen (außer mit `-v` Flag)
- **Erste Installation:** Datenbank wird automatisch initialisiert
- **Standard-Login:** `admin` / `Test123!`

## Troubleshooting

**Port 8080 belegt:**
- Lokale Java-Prozesse stoppen oder Port ändern

**Container startet nicht:**
- Docker Desktop prüfen (muss laufen)
- Logs prüfen: `docker-compose logs app`

**Datenbank zurücksetzen:**
```bash
docker-compose -f docker-compose.yml -f docker-compose.app.yml down -v
docker-compose -f docker-compose.yml -f docker-compose.app.yml up -d
```
