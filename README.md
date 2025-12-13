# kolla

## Service Layer & API

- Maven auf Spring Boot 3.2.0 und gültige Starter (Data JPA, Web, Actuator, Starter-Test) umgestellt, damit die neuen Unit-Tests gebaut werden können.
- Neues `TimeConfig` liefert eine gemeinsame `Clock`, um Priorisierungen deterministisch berechnen zu können.
- `TaskService` erhielt einen `TaskCreationRequest`, eine Fortschrittsabfrage (`TaskProgress`) sowie eine überarbeitete Signatur für `completeStep` (User-IDs als `Long`). So können Deadlines, Ersteller und optionale Step-Zuweisungen sauber verarbeitet werden.
- `TaskStepStatus` besitzt jetzt den Zustand `WAITING`, damit Schritte erst nach Abschluss des Vorgängers in die Benutzerliste wandern. `TaskStep.assignedAt` darf für wartende Schritte leer bleiben.
- `PriorityServiceImpl` implementiert die im Pflichtenheft geforderte Logik (Deadline minus Restaufwand) und wird bei jeder Statusänderung aufgerufen.
- `TaskServiceImpl` erzeugt Tasks komplett aus einer WorkflowDefinition, weist Benutzer automatisch anhand ihrer Rolle zu, aktualisiert `TaskStep`-Prioritäten und liefert Deadline-Tracking (`getTaskProgress`). `completeStep` schaltet automatisch auf den nächsten Schritt.
- `UserService` fokussiert sich auf aktive Arbeitsschritte eines Benutzers und erlaubt Overrides der manuellen Reihenfolge (`overrideManualPriority`), wie vom Workflowmanager gefordert. Zur Unterstützung wurde ein `TaskStepRepository` ergänzt.
- Zu allen Services gibt es fokussierte Unit-Tests unter `src/test/java/team5/prototype/service`, die die Kernlogik (Priorisierung, Task-Lebenszyklus, Sortierung/Overrides) absichern.

### REST-Schnittstellen (Prototyp)

| Endpoint | Zweck | Request | Response |
| --- | --- | --- | --- |
| `POST /api/tasks` | Instanziiert eine WorkflowDefinition als konkrete Task | `CreateTaskRequestDto` mit Template-ID, Titel, Deadline, Creator, optional Step→User-Map | `TaskResponseDto` inkl. generierter TaskSteps |
| `POST /api/tasks/{taskId}/steps/{stepId}/complete` | Markiert einen Arbeitsschritt als erledigt | Body `CompleteStepRequestDto` (UserId) | HTTP 204 |
| `GET /api/tasks/{taskId}/progress` | Fortschritts/Deadline-Tracking für Workflowmanager | – | `TaskProgressDto` |
| `GET /api/users/{userId}/steps` | Liefert aktive Arbeitsschritte eines Nutzers inkl. Priorität | – | Liste von `TaskStepDto` |
| `PATCH /api/task-steps/{taskStepId}/priority` | Setzt/entfernt die manuelle Priorität eines Arbeitsschritts | Body `ManualPriorityRequestDto` (`manualPriority` oder `null`) | Aktualisiertes `TaskStepDto` |

Alle DTOs finden sich im Paket `team5.prototype.dto`. Die Controller rufen ausschließlich die Service-Schicht auf und kapseln so die Entities.
