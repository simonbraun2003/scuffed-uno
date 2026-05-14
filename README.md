# 🎮 Scuffed UNO

Ein webbasiertes UNO-Spiel mit Echtzeit-Multiplayer, entwickelt mit **Spring Boot**, **React**, **TypeScript** und **WebSockets**.

Das Projekt besteht aus einem separaten Backend- und Frontend-Teil und ermöglicht das Erstellen sowie Beitreten von Spielräumen inklusive Benutzer-Authentifizierung und Live-Gameplay.

---

# 🏗 Projektstruktur

```text
scuffed-uno/
├── scuffed-uno-backend/      # Spring Boot Backend
└── scuffed-uno-frontend/     # React Frontend
````

---

# 🚀 Technologien

## Backend

* Java 17
* Spring Boot 3
* Spring Security
* JWT Authentication
* Spring WebSocket
* Spring Data JPA
* PostgreSQL / MariaDB
* Maven

## Frontend

* React 19
* TypeScript
* Vite
* Material UI
* React Router
* STOMP + SockJS

---

# ⚙️ Setup

## Voraussetzungen

Installiert sein sollten:

* Java 17+
* Node.js 20+
* npm
* Maven
* PostgreSQL oder MariaDB

---

# 🔧 Backend starten

## 1. In das Backend-Verzeichnis wechseln

```bash
cd scuffed-uno-backend
```

## 2. Konfiguration anpassen

Die Datenbank-Konfiguration befindet sich typischerweise in:

```text
src/main/resources/application.properties
```

Beispiel:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/uno
spring.datasource.username=postgres
spring.datasource.password=password
```

---

## 3. Anwendung starten

### Mit Maven

```bash
./mvnw spring-boot:run
```

### Oder builden

```bash
./mvnw clean install
java -jar target/uno-0.0.1-SNAPSHOT.jar
```

---

## 4. Swagger/OpenAPI

Nach dem Start erreichbar unter:

```text
http://localhost:8080/swagger-ui.html
```

---

# 💻 Frontend starten

## 1. In das Frontend-Verzeichnis wechseln

```bash
cd scuffed-uno-frontend/unoFrontendReact
```

## 2. Dependencies installieren

```bash
npm install
```

---

## 3. Entwicklungsserver starten

```bash
npm run dev
```

Das Frontend läuft standardmäßig unter:

```text
http://localhost:5173
```

---

# 🔌 WebSocket Kommunikation

Das Spiel verwendet WebSockets für Echtzeitkommunikation zwischen Spielern.

Technologien:

* SockJS
* STOMP

---

# 🔐 Authentifizierung

Die Authentifizierung erfolgt über JWT Tokens.

Nach erfolgreichem Login erhält der Client ein Token, das für geschützte Requests verwendet wird.

---

# 📡 API Endpoints

## Auth

| Methode | Endpoint         | Beschreibung          |
| ------- | ---------------- | --------------------- |
| POST    | `/auth/register` | Benutzer registrieren |
| POST    | `/auth/login`    | Benutzer anmelden     |

## Räume

| Methode | Endpoint        | Beschreibung   |
| ------- | --------------- | -------------- |
| POST    | `/rooms/create` | Raum erstellen |
| POST    | `/rooms/join`   | Raum beitreten |

---

# 🐳 Docker

Im Backend ist bereits ein Dockerfile vorhanden.

## Build

```bash
docker build -t scuffed-uno .
```

## Start

```bash
docker run -p 8080:8080 scuffed-uno
```

---

# 📁 Wichtige Komponenten

## Backend

```text
controller/     # REST & WebSocket Controller
service/        # Spiellogik
repository/     # Datenbankzugriffe
config/         # Security, JWT, WebSocket
model/          # Entities
dto/             # Request/Response DTOs
```

## Frontend

```text
components/     # Wiederverwendbare UI-Komponenten
pages/          # Seiten
services/       # API Kommunikation
context/        # Auth & WebSocket Context
types/          # TypeScript Typen
```

---

# 🧪 Entwicklung

## Frontend Linting

```bash
npm run lint
```

## Frontend Build

```bash
npm run build
```
