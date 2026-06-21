```mermaid
sequenceDiagram
autonumber
participant C as Client (Browser/App)
participant O as Orders Service
participant I as Inventory Service
participant P as Payments Service
participant PP as External Gateway (PayPal)
participant N as Notifications Service

    C->>O: POST /orders (dati_ordine, pagamento)
    Note over O: Salva ordine nel DB<br/>in stato CREATED
    O-->>C: 202 Accepted (Ritorna velocemente)
    Note right of C: L'utente non aspetta.<br/>Da qui parte la pipeline<br/>asincrona (Coroutines/Flows).

    rect rgb(240, 248, 255)
        Note over O, I: FASE 1: Prenotazione Inventario
        O->>I: Sincrono HTTP/gRPC: /reserve (idempotency_key_1)
        Note over I: DB Tx: Decrementa Stock + Outbox Pattern
        I-->>O: 200 OK (Riservato)
        I-->>O: Evento Kafka: 'inventory-reserved'
    end

    rect rgb(255, 240, 245)
        Note over O, PP: FASE 2: Autorizzazione Pagamento
        O->>P: Sincrono HTTP/gRPC: /authorize (idempotency_key_2)
        P->>PP: HTTP POST /auth (Simulazione PayPal)
        PP-->>P: 200 OK (Transaction ID)
        Note over P: DB Tx: Salva Pagamento + Outbox Pattern
        P-->>O: 200 OK (Pagato)
        P-->>O: Evento Kafka: 'payment-authorized'
    end

    Note over O: Orders riceve l'OK dai servizi<br/>e segna l'ordine come COMPLETED

    rect rgb(240, 255, 240)
        Note over O, N: FASE 3: Completamento e Notifiche
        O-->>N: Evento Kafka: 'notification-requested'
        Note over N: Fully Asynchronous:<br/>N consuma il topic quando è pronto.
        N->>C: Invia Email di Conferma
    end
```

Stessa idempotency key generata dal client e propagata per i servizi downstream e salvata nei DB. 
Implementare meccanismi di retry, backoff esponenziale con jitter.
Outbox pattern per garantire la consistenza tra DB e messaggi Kafka.
Aggiungere circuit breaker e bulkheads. 
In caso di fallimenti implementare strategia di compensazione per correggere la quantità del prodotto. 



