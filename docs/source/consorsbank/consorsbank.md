Technischer Leitfaden: Consorsbank Trading-API-Integration für automatisierte Handelssysteme

Dieser Leitfaden dient als strategische und technische Referenz für die Integration der Consorsbank Trading-API. Er richtet sich an Entwickler und Systemarchitekten, die hochperformante und regulatorisch konforme automatisierte Handelssysteme (Trading-Bots) implementieren.

1. Strategische Übersicht der API-Architektur und Geschäftsobjekte

In einer professionellen Trading-Architektur ist die strikte Trennung zwischen dem Entwurfsstadium einer Order (OrderEntry) und der aktiven Marktpräsenz (Order) eine architektonische Notwendigkeit. Diese Entkopplung fungiert als regulatorischer und technischer Schutzwall: Sie ermöglicht es dem System, eine Order zunächst als Staging-Objekt zu validieren, die automatisch generierten Kostenaufstellungen (ExAnteCost) zu prüfen und Compliance-Checks (z. B. Verlustschwellen-Monitoring) durchzuführen, bevor eine unwiderrufliche Übermittlung an den Marktplatz erfolgt. Für Bots minimiert dies das Risiko von "Fat-Finger"-Fehlern oder algorithmischen Amokläufen signifikant.

Zentrale Geschäftsobjekte

Die folgende Matrix definiert die Kernobjekte und bewertet deren strategischen Nutzen für die Bot-Logik:

Geschäftsobjekt	Funktion	Nutzen für Trading-Bots
OrderEntry	Staging/Draft-Objekt; noch nicht marktwirksam.	Ermöglicht asynchrone Validierung und Risikoprüfung vor der Platzierung.
Order	Aktive, an den Handelsplatz übermittelte Order.	Status-Tracking und Execution-Monitoring für die Bestandsführung.
OrderChange	Entwurf für die Modifikation einer bestehenden Order.	Erlaubt dynamische Strategieanpassungen (z. B. Limit-Trailing).
OrderTransactionState	Statusressource für (a)synchrone Transaktionen.	Zentraler Anker für das Zustandsmanagement während des Order-Life-Cycles.
QuoteOrderEntry	Anfrage für verbindliche Preis-Quotes (Direkthandel).	Ermöglicht deterministische Handelskosten im OTC-Markt.
ExAnteCost	Automatisch generierte Kostenaufstellung.	Essentiell: Ermöglicht die automatisierte Rentabilitätsprüfung (Pre-Trade).
SecuritiesAccount	Das Depot (Portfolio) inkl. Beständen.	Basis für Rebalancing-Algorithmen und Margin-Berechnungen.

Der "So What?"-Faktor: Ein kritischer Parameter für die Bot-Entwicklung ist das 30-Minuten-Zeitfenster. Jede OrderEntry oder QuoteOrderEntry, die nicht innerhalb dieses Fensters durch einen /place-Aufruf aktiviert wird, verwirft das System automatisch. Bots müssen daher eine State-Machine implementieren, die Entwürfe entweder rechtzeitig finalisiert oder proaktiv bereinigt, um Speicher- und Logik-Leichen in der API zu vermeiden.

2. Authentifizierungs-Framework und Transaktionssicherheit

Die API nutzt eine zweistufige Sicherheitsstruktur, um den Zugriff und die Transaktionsintegrität zu trennen. Während OAuth 2.0 den Zugriffsschutz auf API-Ebene übernimmt, sichert die Transaktions-Ebene (TAN) die tatsächliche Disposition des Kapitals ab.

Technische Struktur

1. OAuth 2.0 (Authorization): Jeder API-Request erfordert einen validen Bearer Token im Header: Authorization: Bearer [TOKEN].
2. Transaktions-Authentifizierung (Authentication): Operationen am /place-Endpunkt erfordern ein Authentifizierungsobjekt im Body. Verfügbare Methoden:
  * tan: Manuelle Eingabe einer extern generierten TAN.
  * session: Nutzung einer zuvor aktivierten Session-TAN.
  * secureMessage: App-Freigabe (Push). Wichtig: Diese Methode ist derzeit explizit nicht für die Aktivierung einer Session-TAN zugelassen.

Der "So What?"-Faktor: Für einen vollautomatisierten, Headless-Betrieb ist die session-TAN die einzig skalierbare Lösung. Sie erlaubt es dem Bot, nach einmaliger initialer Autorisierung durch den Nutzer, Orders ohne weitere manuelle Interaktion zu platzieren. Die Beschränkung von secureMessage bedeutet für Systemarchitekten, dass der Aktivierungsprozess der Session-TAN (via /session-tan) zwingend über ein TAN-Verfahren erfolgen muss, das nicht auf der Secure-Message-Logik basiert.

3. Handels-Workflows und Prozess-Logik

Trading über die API ist von Natur aus asynchron. Ein robustes State Management ist das Herzstück jeder Bot-Implementierung, um Race Conditions zu vermeiden und den Übergang von der Transaktionsphase in die Orderbuchphase sicherzustellen.

New Order Entry Flow & Order Change Flow

Der Prozess folgt einer strikten Sequenz von HTTP-Methoden und Statuscodes:

1. POST /trading/v1/order-entries: Initialisierung des Entwurfs (201 Created).
2. GET /trading/v1/ex-ante-costs/{id}: Abruf der Kosten (architektonisch empfohlen für Risk-Checks).
3. POST /trading/v1/order-entries/{id}/place: Finale Platzierung (202 Accepted).
4. Polling: Kontinuierliches GET auf den im 202-Response gelieferten OrderTransactionState-Link.
5. Redirect (Trigger): Sobald die Transaktion erfolgreich verarbeitet wurde, antwortet die API mit einem HTTP 303 See Other. Der im Location-Header enthaltene Link führt zur finalen Order-Ressource.

Architekten-Empfehlung: Implementieren Sie für das Polling auf den OrderTransactionState eine Exponential-Backoff-Strategie. Dies optimiert den Ressourcenverbrauch und verhindert ein unnötiges "Hämmern" gegen die Rate-Limits, während das System die Order an den Marktplatz routet.

Zusätzlich unterstützt die API das Konzept der "Next Order". Dies ermöglicht komplexe Sequenzen, bei denen eine Basis-Order mit einer oder mehreren Folge-Orders verknüpft wird (z. B. If-Done-Strukturen), was für fortgeschrittene Risikomanagement-Strategien unerlässlich ist.

4. Parameter-Matrix und Routing-Entscheidungen

Präzision bei der Parameterwahl entscheidet über die Validierung und die Ausführungsqualität. Ein Fehlen regulatorischer Pflichtfelder führt zu unmittelbaren 400-Fehlern.

Parameter-Matrix für Order-Entries (Auszug)

Parameter	Market	Limit	Stop Market	Stop Limit	OCO	Trailing Stop
isin / wkn (mutual exclusive)	★	★	★	★	★	★
accountNo	★	★	★	★	★	★
securitiesAccountNo	★	★	★	★	★	★
direction (BUY/SELL)	★	★	★	★	★	★
nominalAmount	★	★	★	★	★	★
overrideRiskClass	★	★	★	★	★	★
limit	-	★	-	-	★	-
stop	-	-	★	★	★	★
stopLimit	-	-	-	★	-	-
trailingDistance	-	-	-	-	-	★

(★ = Pflichtfeld)

Wichtige Einschränkung: Beachten Sie die Order-Modell-Restriktionen. Beispielsweise können Trailing Stop (Market/Limit) Orders konstruktionsbedingt niemals den Status OPEN annehmen, sondern verbleiben in spezifischen Zwischenzuständen, bis die Trigger-Bedingungen erfüllt sind.

Routing-Logik: TradingVenue vs. MarketPlace

Die Entscheidung über den Ausführungsplatz folgt einem binären Pfad:

1. Wird eine tradingVenueId übergeben, die weder TRG (Target) noch OTC ist, erfolgt das Routing direkt an diesen spezifischen Handelsplatz (z. B. XETRA).
2. Ist die tradingVenueId jedoch TRG oder OTC, evaluiert das System die marketPlaceId:
  * Ist die marketPlaceId leer, greift die "Best Execution Rule" – das System wählt automatisiert den optimalen Platz.
  * Ist eine marketPlaceId definiert, erfolgt das Routing an diesen spezifischen Marktplatz.

5. Datenmanagement, Filterung und Fehlerbehandlung

Effizienz im Datenabruf ist entscheidend, um die Latenz der Bot-Entscheidungen zu minimieren und API-Rate-Limits zu respektieren.

Kollektions-Management

* Filterung: Nutzen Sie den orderStatus-Filter (OPEN, EXECUTED, ALL, ACTIVE, INACTIVE) gezielt. Der Abruf von ALL sollte im Hochfrequenzbetrieb vermieden werden.
* Paginierung: Steuerung über perPage und page.
* Performance: Nutzen Sie maxRecords=n bei /orders, um nur die aktuellsten Transaktionen zu synchronisieren (optimiert für das State-Updating).

Fehlerbehandlung & Self-Healing

Die API liefert detaillierte Fehlerobjekte. Ein Bot muss folgende Statuscodes für "Self-Healing"-Routinen interpretieren:

* 401 (Unauthorized): Token-Refresh-Logik triggern.
* 404 (Not Found): Lokale State-Machine mit API synchronisieren (Ressource evtl. bereits abgelaufen).
* 409 (Conflict): Dublettenprüfung; Transaktion bereits in Verarbeitung.
* 400 (Bad Request): Analyse des developerText und der details-Liste. Wenn der Key tradingVenueId ungültig ist, kann der Bot über eine Fallback-Logik (z. B. Best Execution) den Request korrigiert neu senden.

6. Erweiterte Funktionen: Sparplan-Integration

Die Automatisierung von Sparplänen erfordert ein tieferes Verständnis des ServiceOperationMode. Im Gegensatz zum regulären Trading ist dieser Flow stärker auf Challenge-Response-Szenarien ausgelegt.

Sparplan-Workflows & Operation Modes

Für Operationen wie SpkBulkAdd (Erstellen), SpkBulkChange (Update) oder SpkBulkDelete (Löschen) werden folgende Modi genutzt:

* VALIDATE: Prüft die Eingabedaten ohne Transaktionswirkung.
* REQ_QRCODE / REQ_SECURE_MSG: Fordert eine spezifische Autorisierungs-Challenge an.
* REQ_TXRESULT: Finalisiert die Transaktion und liefert das Ergebnis.
* EXECUTE: Direkte Ausführung, sofern die Autorisierung bereits vorliegt.

Der "So What?"-Faktor: Sparplan-Prozesse sind oft durch QR-Code-Challenges geschützt. Für einen Bot bedeutet dies, dass er in der Lage sein muss, den 202-Statuscode abzufangen, den Challenge-Typ aus dem Response zu extrahieren und gegebenenfalls den Nutzer zur Interaktion aufzufordern (Hybrid-Modus). Eine vollautomatisierte Änderung ohne jegliche User-Challenge ist architektonisch bei Sparplänen – anders als beim Order-Flow mit Session-TAN – oft nicht vorgesehen und stellt die größte Integrationshürde dar.
