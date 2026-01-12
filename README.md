# Kolla - Aufgabenmanagementsystem (Prototyp)

Kolla ist ein prototypisches, kollaboratives Aufgabenmanagementsystem, das im Rahmen des Moduls "Software-Architekturen und Qualitätssicherung" entwickelt wurde. Das System ermöglicht die Steuerung von Aufgaben über vordefinierte Workflows, eine automatische Priorisierung und eine Echtzeit-Fortschrittsüberwachung für Manager.

## Kerntechnologien

- **Backend:** Spring Boot 3 / Java 21
- **Sicherheit:** Spring Security mit zustandsloser JWT-Authentifizierung
- **Datenhaltung:** Spring Data JPA mit H2 In-Memory-Datenbank
- **Echtzeit-Kommunikation:** Spring WebSockets (mit STOMP)
- **Build-Tool:** Maven

## Architektur-Highlights

Die Architektur des Backends folgt einer klassischen **Schichtenarchitektur** (Controller, Service, Repository), um die Anforderungen an **Änderbarkeit (Modifiability)** und **Testbarkeit (Testability)** zu erfüllen.

- **Dependency Inversion:** Die Kommunikation zwischen den Schichten erfolgt ausschließlich über **Interfaces** (`ITaskService`, `IUserRepository` etc.). Dies ermöglicht eine lose Kopplung und macht die einzelnen Komponenten isoliert testbar.
- **DTO-Pattern:** Die REST-API ist durch **Data Transfer Objects (DTOs)** vollständig von der internen Domänenlogik entkoppelt. Dies stellt sicher, dass die API stabil bleibt, auch wenn sich die internen Entitäten ändern, und verhindert das unbeabsichtigte Veröffentlichen von sensiblen Daten.
- **Service-Orientierung:** Jede fachliche Domäne (Tasks, User, Roles etc.) hat eine dedizierte Service-Klasse, die die gesamte Geschäftslogik kapselt.

## Logik & Features im Detail

- **Workflow-Engine (`TaskServiceImpl`):** Das Herzstück der Anwendung.
    - Instanziiert konkrete `Tasks` aus vordefinierten `WorkflowDefinitions`.
    - Implementiert die `completeStep`-Logik, die einen `TaskStep` abschließt und den Workflow automatisch zum nächsten Schritt bewegt.
    - Enthält eine **intelligente Benutzerzuweisung**: Basierend auf der benötigten Rolle und der aktuellen Auslastung wird automatisch der am besten geeignete Mitarbeiter für einen Arbeitsschritt ausgewählt.
- **Priorisierung (`PriorityServiceImpl`):**
    - Die Priorität (`IMMEDIATE`, `MEDIUM_TERM`, `LONG_TERM`) wird dynamisch basierend auf der verbleibenden Zeit bis zur Deadline und der Restdauer aller offenen Arbeitsschritte berechnet.
    - Die Logik ist in einem eigenen Service gekapselt, um sie später leicht austauschen zu können.
- **Sicherheit & Authentifizierung (`AuthServiceImpl`, `JwtAuthFilter`):**
    - Implementiert eine zustandslose Authentifizierung via **JSON Web Tokens (JWT)**.
    - Ein rollenbasiertes Berechtigungsmodell (`SecurityConfig`) schützt die API-Endpunkte. Es wird klar zwischen den Rechten eines normalen `Users` (Akteur) und eines `WORKFLOW_MANAGER` unterschieden.
- **Echtzeit-Updates (`NotificationServiceImpl`):**
    - Bei wichtigen Ereignissen (z.B. Abschluss eines Arbeitsschritts, manuelle Prioritätsänderung) werden über **WebSockets** Push-Benachrichtigungen an die verbundenen Frontends gesendet.
    - Dies ermöglicht eine automatische Aktualisierung der Manager- und Akteur-Dashboards ohne manuelles Neuladen der Seite.
- **Benutzer- & Rollenverwaltung (`UserServiceImpl`, `RoleServiceImpl`):**
    - Bietet vollständige CRUD-Funktionalitäten für die Verwaltung von Benutzern und Rollen.
- **Manuelle Steuerung (`TaskStepServiceImpl`):**
    - Ermöglicht dem `WORKFLOW_MANAGER`, die automatisch berechnete Priorität eines einzelnen Arbeitsschritts manuell zu überschreiben.

## API-Endpunkte (REST API)

Alle Endpunkte, die nicht als `Öffentlich` markiert sind, erfordern ein gültiges JWT im `Authorization: Bearer <token>` Header.

### Authentifizierung

| Methode | Endpoint          | Zweck                                               | Berechtigung |
| :------ | :---------------- | :-------------------------------------------------- | :----------- |
| `POST`  | `/api/auth/login` | Authentifiziert einen Benutzer und gibt einen JWT zurück. | Öffentlich   |

### Manager-Funktionen (Dashboard & Verwaltung)

| Methode | Endpoint                     | Zweck                                         | Berechtigung       |
| :------ | :--------------------------- | :-------------------------------------------- | :----------------- |
| `GET`   | `/api/manager/dashboard`     | Ruft die Kennzahlen für das Manager-Dashboard ab. | `WORKFLOW_MANAGER` |
| `GET`   | `/api/users`                 | Ruft eine Liste aller Benutzer ab.                | `WORKFLOW_MANAGER` |
| `POST`  | `/api/users`                 | Erstellt einen neuen Benutzer.                    | `WORKFLOW_MANAGER` |
| `GET`   | `/api/roles`                 | Ruft alle verfügbaren Rollen ab.              | `WORKFLOW_MANAGER` |
| `POST`  | `/api/roles`                 | Erstellt eine neue Rolle.                       | `WORKFLOW_MANAGER` |
| `GET`   | `/api/workflow-definitions`  | Ruft alle Workflow-Vorlagen ab.             | `WORKFLOW_MANAGER` |

### Aufgaben- & Workflow-Steuerung

| Methode | Endpoint                         | Zweck                                               | Berechtigung       |
| :------ | :------------------------------- | :-------------------------------------------------- | :----------------- |
| `POST`  | `/api/tasks`                     | Erstellt eine neue Aufgabe aus einer Workflow-Vorlage. | `WORKFLOW_MANAGER` |
| `GET`   | `/api/tasks/{id}/details`        | Ruft die Detailansicht einer Aufgabe ab.          | `WORKFLOW_MANAGER` |
| `POST`  | `/api/task-steps/{id}/set-priority`| Setzt die Priorität eines Schritts manuell.         | `WORKFLOW_MANAGER` |

### Akteur-Funktionen (User)

| Methode | Endpoint                            | Zweck                                               | Berechtigung   |
| :------ | :---------------------------------- | :-------------------------------------------------- | :------------- |
| `GET`   | `/api/users/dashboard-tasks/{userId}` | Ruft die Aufgabenliste für das Akteur-Dashboard ab. | Authentifiziert|
| `POST`  | `/api/task-steps/{stepId}/complete`   | Markiert einen Arbeitsschritt als abgeschlossen.    | Authentifiziert|

## Setup & Starten

1.  Stellen Sie sicher, dass Java 21 und Maven installiert sind.
2.  Klonen Sie das Repository.
3.  Führen Sie im Hauptverzeichnis des Projekts den folgenden Befehl aus:
    ```bash
    mvn spring-boot:run
    ```
4.  Die Anwendung startet auf `http://localhost:8080`.
5.  Die H2-In-Memory-Datenbank ist unter `http://localhost:8080/h2-console` erreichbar (JDBC URL: `jdbc:h2:mem:kolla`).

### Initial-Daten (Data Seeder)

Beim ersten Start befüllt der `DataSeeder` die Datenbank automatisch mit Testdaten. Dies inkludiert:
-   Einen `WORKFLOW_MANAGER`: `admin` / `adminpassword`
-   Einen `DEVELOPER`: `developer` / `devpassword`
-   Einen `TESTER`: `tester` / `testpassword`
-   Eine Beispiel-Workflow-Vorlage und eine daraus erstellte Aufgabe.