# Guía de arquitectura de contract-api

Explicación visual de cómo está construido `contract-api`: qué piezas tiene, cómo se conectan y por dónde pasa una petición. Está pensada para quien llega al proyecto y necesita orientarse antes de tocar código.

- Las **reglas** de arquitectura (lo que es obligatorio) están en [architecture.md](../architecture.md). Esta guía las explica con ejemplos del código; si hay contradicción, manda `architecture.md`.
- El **estado implementado** (endpoints, configuración, base de datos) está en [technical.md](../technical.md).
- Las **reglas de negocio** están en [business-rules.md](../business-rules.md).

## Índice

1. [Vista de contexto](#1-vista-de-contexto)
2. [Vista de contenedores y despliegue](#2-vista-de-contenedores-y-despliegue)
3. [Módulos y regla de dependencia](#3-módulos-y-regla-de-dependencia)
4. [Puertos y adaptadores](#4-puertos-y-adaptadores)
5. [Cómo se ensambla: casos de uso como beans](#5-cómo-se-ensambla-casos-de-uso-como-beans)
6. [Modelo de dominio](#6-modelo-de-dominio)
7. [Recorrido de una petición](#7-recorrido-de-una-petición)
8. [Autenticación y tokens](#8-autenticación-y-tokens)
9. [Manejo de errores](#9-manejo-de-errores)
10. [Ciclos de vida](#10-ciclos-de-vida)
11. [Pruebas por capa](#11-pruebas-por-capa)
12. [Cómo añadir una funcionalidad](#12-cómo-añadir-una-funcionalidad)
13. [Observaciones](#13-observaciones)

## 1. Vista de contexto

Quién usa el sistema y con qué se comunica. Hoy la API solo depende de PostgreSQL; no hay integraciones con sistemas externos.

```mermaid
flowchart TB
    HR([RR.HH. / Administrador])
    EMP([Empleado])
    SYS(["Sistema<br/>tarea de vencimiento<br/>(pendiente)"])
    API[contract-api<br/>API REST de contratos laborales]
    DB[(PostgreSQL)]

    HR -->|gestiona empleados, usuarios<br/>y contratos · HTTPS + JWT| API
    EMP -->|"consulta sus contratos<br/>(pendiente)"| API
    SYS -.->|vence contratos a plazo fijo| API
    API -->|JDBC| DB
```

| Actor | Qué hace hoy | Pendiente |
|---|---|---|
| RR.HH. / Administrador | Inicia sesión, registra y consulta empleados; el administrador crea usuarios | Operaciones de contratos |
| Empleado | Puede iniciar sesión | Consultar sus contratos (requiere el vínculo `User` ↔ `Employee`, business-rules §9) |
| Sistema | Crea el administrador inicial al arrancar (BR-USR-007) | Vencimiento automático de contratos |

## 2. Vista de contenedores y despliegue

La API es un único proceso Spring Boot empaquetado en una imagen Docker. En local, la base de datos y pgAdmin se levantan con `docker compose`.

```mermaid
flowchart LR
    subgraph local[Entorno local · docker-compose.yml]
        PG[(PostgreSQL 16<br/>:5432)]
        PGA[pgAdmin<br/>:5050]
    end
    subgraph app[Imagen contract-api · perfil prod]
        JVM[Spring Boot 3.5 · Java 21<br/>:8081<br/>virtual threads]
    end
    CLI([Cliente HTTP<br/>Swagger UI, Postman, SPA]) -->|REST + Bearer JWT| JVM
    JVM -->|Flyway al arrancar<br/>JPA en ejecución| PG
    PGA --> PG
    JVM -->|logs JSON ECS| OUT[[stdout]]
```

```mermaid
flowchart LR
    DEV[Rama feature/*] --> PR[Pull Request]
    PR --> CI{GitHub Actions<br/>clean verify + imagen}
    CI -->|merge| MAIN[main]
    MAIN --> GHCR[(GHCR)]
    MAIN -.->|si está configurado| DH[(Docker Hub)]
    GHCR --> RUN[docker run<br/>con variables DB_* y SECURITY_JWT_*]
```

El detalle del pipeline está en la [guía de CI/CD](ci-cd.md); las variables de entorno, en [technical.md §4](../technical.md#4-configuración).

## 3. Módulos y regla de dependencia

El proyecto Maven tiene tres módulos. Las dependencias apuntan **solo hacia el centro**: el dominio no conoce a nadie y la infraestructura conoce a todos.

```mermaid
flowchart LR
    INF[infrastructure<br/>Spring Web, Security, JPA,<br/>Flyway, MapStruct, springdoc]
    APP[application<br/>casos de uso y puertos<br/>solo spring-tx]
    DOM[domain<br/>Java puro]
    INF --> APP --> DOM
    INF -.->|también usa tipos de| DOM
```

Una forma práctica de recordarlo son círculos concéntricos:

```mermaid
flowchart TB
    subgraph INF[infrastructure: detalles técnicos]
        subgraph APP[application: qué hace el sistema]
            subgraph DOM[domain: reglas del negocio]
                M[Contract · Employee · User<br/>value objects · eventos]
            end
            UC[Casos de uso · Puertos]
        end
        AD[Controllers · JPA · Seguridad · Configuración]
    end
```

| Módulo | Responsabilidad | Puede usar | Nunca usa |
|---|---|---|---|
| `domain` | Invariantes, estados y eventos del negocio | JDK | Spring, JPA, Jackson, Lombok |
| `application` | Orquestar una operación: cargar, delegar al dominio, persistir | `domain`, `@Transactional` | Controllers, entidades JPA, `@Service` |
| `infrastructure` | Traducir entre el mundo exterior y el núcleo | Todo | — |

**Por qué importa:** las reglas de negocio se prueban sin levantar Spring ni una base de datos, y cambiar una tecnología (por ejemplo, el emisor de tokens) no toca la lógica.

Dentro de `domain` los paquetes se organizan por agregado (ADR-0004):

```mermaid
flowchart TB
    subgraph domain
        COMMON[common<br/>AggregateRoot, Entity,<br/>DomainEvent, Guard]
        SHARED[shared<br/>Money]
        CONTRACT[contract]
        EMPLOYEE[employee]
        IDENTITY[identity]
    end
    CONTRACT --> COMMON
    CONTRACT --> SHARED
    CONTRACT -->|solo EmployeeId| EMPLOYEE
    EMPLOYEE --> COMMON
    IDENTITY --> COMMON
```

## 4. Puertos y adaptadores

`application` define **puertos**: interfaces que describen lo que el núcleo ofrece (entrada) y lo que necesita (salida). `infrastructure` los conecta con tecnología concreta mediante **adaptadores**.

```mermaid
flowchart LR
    subgraph IN[Adaptadores de entrada · infrastructure]
        AC[AuthController]
        EC[EmployeeController]
        UCN[UserController]
        BR[BootstrapAdministratorRunner]
    end

    subgraph CORE[application]
        direction TB
        subgraph PIN[port.in]
            P1[AuthenticateUser · RefreshToken<br/>Logout · GetCurrentUser]
            P2[RegisterEmployee · UpdateEmployee<br/>GetEmployee · ListEmployees]
            P3[CreateUser · BootstrapAdministrator]
        end
        S[usecase<br/>XxxService]
        subgraph POUT[port.out]
            O1[EmployeeRepositoryPort]
            O2[UserRepositoryPort<br/>RoleRepositoryPort<br/>RefreshTokenRepositoryPort]
            O3[CredentialsAuthenticatorPort<br/>PasswordHasherPort<br/>TokenIssuerPort]
        end
    end

    subgraph OUT[Adaptadores de salida · infrastructure]
        PA1[EmployeePersistenceAdapter]
        PA2[User/Role/RefreshToken<br/>PersistenceAdapter]
        SA[CredentialsAuthenticatorAdapter<br/>PasswordHasherAdapter<br/>TokenIssuerAdapter]
    end

    AC --> P1
    EC --> P2
    UCN --> P3
    BR --> P3
    PIN --> S --> POUT
    PA1 -. implementa .-> O1
    PA2 -. implementa .-> O2
    SA -. implementa .-> O3
    PA1 & PA2 --> DB[(PostgreSQL)]
```

Dos ideas clave:

- **El núcleo define el contrato, la infraestructura se adapta.** `TokenIssuerPort` no menciona JWT ni Nimbus; `TokenIssuerAdapter` sí. Si mañana se cambiara la librería de firmas, solo cambia el adaptador.
- **No todos los puertos de salida son repositorios.** Autenticar credenciales, hashear contraseñas o emitir tokens también son capacidades técnicas que el caso de uso pide a través de un puerto.

Dentro de `infrastructure` también hay aislamiento (regla 9): `api` solo llama a puertos de entrada y nunca a `persistence`; `persistence` no conoce `api`.

```mermaid
flowchart LR
    API[api] -->|solo port.in| APPL[application]
    PERS[persistence] -.->|implementa port.out| APPL
    SEC[security] -.->|implementa port.out| APPL
    API -- prohibido --x PERS
```

## 5. Cómo se ensambla: casos de uso como beans

Los servicios de `application` no llevan `@Service`: son clases Java normales. `UseCaseConfig` los crea y Spring inyecta los adaptadores que implementan cada puerto.

```mermaid
flowchart LR
    CFG[UseCaseConfig<br/>@Configuration] -->|new RegisterEmployeeService| UC[RegisterEmployeeUseCase<br/>bean]
    ADP[EmployeePersistenceAdapter<br/>@Component] -->|se inyecta como<br/>EmployeeRepositoryPort| CFG
    UC -->|se inyecta en| CTRL[EmployeeController]
```

```java
@Bean
RegisterEmployeeUseCase registerEmployeeUseCase(EmployeeRepositoryPort employeeRepository) {
    return new RegisterEmployeeService(employeeRepository);
}
```

El controller recibe la **interfaz** `RegisterEmployeeUseCase`, no la clase del servicio. Así `api` depende de un contrato y las pruebas `@WebMvcTest` pueden simular el caso de uso.

## 6. Modelo de dominio

### 6.1 Bloques comunes

```mermaid
classDiagram
    class Identifier {
        <<interface>>
    }
    class Entity~ID~ {
        <<abstract>>
        +id() ID
        +equals() por id
    }
    class AggregateRoot~ID~ {
        <<abstract>>
        #registerEvent(DomainEvent)
        +pullDomainEvents() List
    }
    class DomainEvent {
        <<interface>>
        eventId
        occurredOn
        aggregateId
    }
    Entity <|-- AggregateRoot
    Entity ..> Identifier
    AggregateRoot o-- DomainEvent : acumula
```

- Una **entidad** se compara por su identificador; un **value object** (`record`) por su valor.
- Un **agregado** es la unidad de consistencia: se carga, se modifica y se guarda completo, y solo su raíz es accesible desde fuera.
- Los métodos que emiten eventos reciben un `Clock`: el dominio nunca llama a `now()`, lo que hace las pruebas deterministas.

### 6.2 Agregados

```mermaid
classDiagram
    direction LR
    class Contract {
        <<AggregateRoot>>
        ContractNumber number
        EmployeeId employeeId
        ContractType type
        JobTitle jobTitle
        Salary salary
        ContractPeriod period
        ContractStatus status
        List~ContractRenewal~ renewals
        Termination termination
        +create(..., MinimumWage, Clock)$
        +update(...)
        +sign(Clock)
        +renew(LocalDate, Clock)
        +terminate(LocalDate, String, Clock)
        +cancel()
        +expire(LocalDate, Clock)
    }
    class Employee {
        <<AggregateRoot>>
        DocumentNumber documentNumber
        FullName fullName
        Email email
        +register(...)$
        +update(FullName, Email)
    }
    class User {
        <<AggregateRoot>>
        Username username
        PasswordHash passwordHash
        Set~RoleName~ roles
        boolean enabled
        +create(...)$
        +upgradePasswordHash(PasswordHash)
        +hasRole(RoleName)
    }
    class RefreshToken {
        <<AggregateRoot>>
        UserId userId
        UUID familyId
        String tokenHash
        Instant expiresAt
        Instant usedAt
        +issue(...)$
        +rotate(...) RefreshToken
        +revoke(RevocationReason, Clock)
        +isUsable(Instant)
    }
    class Role {
        <<record>>
        RoleName name
        Set~Permission~ permissions
    }
    class Money {
        <<shared kernel>>
    }
    Contract ..> Employee : solo EmployeeId
    RefreshToken ..> User : solo UserId
    User ..> Role : solo RoleName
    Contract ..> Money : Salary
```

Los agregados se referencian entre sí **solo por identificador** (`Contract` guarda un `EmployeeId`, nunca un `Employee`). Comprobar que el empleado existe es tarea del caso de uso (BR-CTR-001), no del agregado.

### 6.3 Dónde vive cada regla

| Tipo de regla | Dónde | Ejemplo |
|---|---|---|
| Formato de un valor | Constructor del value object | DNI de 8 dígitos (`DocumentNumber`, BR-EMP-002) |
| Invariante de un agregado | Método del agregado | Solo se modifica un contrato en `DRAFT` (`Contract.update`, BR-CTR-006) |
| Regla que necesita otros datos | Caso de uso | Documento único (`RegisterEmployeeService`, BR-EMP-001) |
| Formato de la petición HTTP | Bean Validation en el `Request` | `email` obligatorio y ≤ 254 caracteres |
| Permisos | Seguridad (`infrastructure`) | `/api/v1/users` exige `user:manage` |

## 7. Recorrido de una petición

`POST /api/v1/employees` atraviesa todas las capas:

```mermaid
sequenceDiagram
    autonumber
    actor C as Cliente
    participant F as Filtros de seguridad
    participant CT as EmployeeController
    participant M as EmployeeApiMapper
    participant UC as RegisterEmployeeService
    participant D as Employee (dominio)
    participant P as EmployeePersistenceAdapter
    participant DB as PostgreSQL

    C->>F: POST /api/v1/employees + Bearer JWT
    F->>F: valida firma, exp, iss y aud
    F->>CT: EmployeeRequest
    CT->>CT: @Valid (formato, 400 si falla)
    CT->>M: toCommand(request)
    M-->>CT: RegisterEmployeeCommand
    CT->>UC: register(command)
    Note over UC: @Transactional
    UC->>UC: new DocumentNumber(...) valida BR-EMP-002
    UC->>P: existsByDocumentNumber
    P->>DB: SELECT
    alt ya existe
        UC-->>CT: DuplicateEmployeeDocumentException (409)
    end
    UC->>D: Employee.register(documento, nombre, correo)
    D-->>UC: Employee
    UC->>P: save(employee)
    P->>P: mapper dominio a JpaEntity
    P->>DB: INSERT
    P-->>UC: Employee
    UC-->>CT: EmployeeResult
    CT->>M: toResponse(result)
    CT-->>C: 201 Created + Location + EmployeeResponse
```

Fíjate en las **cuatro representaciones** del mismo empleado, cada una en su capa:

```mermaid
flowchart LR
    R[EmployeeRequest<br/>api/dto] -->|EmployeeApiMapper| CMD[RegisterEmployeeCommand<br/>application/dto]
    CMD -->|caso de uso| DOM[Employee<br/>domain]
    DOM -->|EmployeePersistenceMapper| JPA[EmployeeJpaEntity<br/>persistence/entity]
    DOM -->|EmployeeResult.from| RES[EmployeeResult]
    RES -->|EmployeeApiMapper| RESP[EmployeeResponse]
```

Separarlas evita que un cambio en la tabla o en el JSON obligue a tocar el dominio.

**Concurrencia:** si dos peticiones registran el mismo documento a la vez, ambas pasan el `exists`, pero la restricción única `uk_employees_document` rechaza la segunda. El adaptador traduce esa `DataIntegrityViolationException` en `DuplicateEmployeeDocumentException`, así el cliente recibe igualmente `409`.

## 8. Autenticación y tokens

### 8.1 Cadenas de filtros

```mermaid
flowchart TB
    REQ[Petición HTTP] --> Q{¿Ruta pública?<br/>login, refresh, swagger,<br/>actuator/health}
    Q -->|sí| PUB[Cadena 1 · permitAll] --> CTRL[Controller]
    Q -->|no| PRIV[Cadena 2 · Resource Server JWT]
    PRIV --> V{¿Token válido?}
    V -->|no / ausente| E401[401 AUTH-003]
    V -->|expirado| E401B[401 AUTH-004]
    V -->|sí| AUTHZ{Regla de URL}
    AUTHZ -->|/api/v1/users/** sin user:manage<br/>/actuator/** sin system:monitor| E403[403 AUTH-001]
    AUTHZ -->|ok| CTRL
    AUTHZ -->|ruta no declarada| E403
```

Ambas cadenas son *stateless*: no hay sesión ni CSRF, todo viaja en la cabecera `Authorization`. El claim `perms` del JWT se convierte en las authorities que evalúa Spring Security.

### 8.2 Login

```mermaid
sequenceDiagram
    autonumber
    actor C as Cliente
    participant AC as AuthController
    participant S as AuthenticateUserService
    participant CA as CredentialsAuthenticatorAdapter
    participant R as RoleRepositoryPort
    participant T as TokenIssuerAdapter
    participant RT as RefreshTokenRepositoryPort

    C->>AC: POST /auth/login {username, password}
    AC->>S: authenticate(LoginCommand)
    S->>CA: authenticate(username, password)
    Note over CA: > 72 bytes, usuario inexistente,<br/>clave incorrecta o deshabilitado<br/>= InvalidCredentialsException (AUTH-002)
    CA-->>S: User
    S->>R: findPermissionsByRoles(roles)
    S->>T: issueAccessToken(user, permisos)
    T-->>S: JWT RS256 (15 min)
    S->>T: generateRefreshToken()
    T-->>S: valor opaco + hash SHA-256
    S->>RT: save(RefreshToken.issue) en familia nueva
    S-->>AC: TokenPairResult
    AC-->>C: 200 TokenResponse (Cache-Control no-store)
```

Solo el **hash** del refresh token se guarda en base de datos; el valor en claro solo lo tiene el cliente.

### 8.3 Rotación del refresh token

Cada refresh token es de un solo uso (BR-USR-006). Al usarlo nace otro de la **misma familia**; si alguien presenta uno ya usado, se asume robo y se revoca la familia completa.

```mermaid
sequenceDiagram
    autonumber
    actor C as Cliente
    participant S as RefreshTokenService
    participant RT as RefreshTokenRepositoryPort
    participant U as UserRepositoryPort

    C->>S: refresh(token)
    S->>RT: findByTokenHashForUpdate(hash)
    Note over RT: SELECT ... FOR UPDATE<br/>serializa rotaciones simultáneas
    alt no existe
        S-->>C: 401 AUTH-005
    else ya usado
        S->>RT: revokeFamily(REUSE_DETECTED)
        S-->>C: 401 AUTH-005
    else expirado o revocado
        S-->>C: 401 AUTH-005
    else usuario inexistente o deshabilitado
        S->>RT: revokeFamily(USER_DISABLED)
        S-->>C: 401 AUTH-005
    else válido
        S->>S: current.rotate() marca usado y crea sucesor
        S->>RT: save(sucesor), save(actual)
        S-->>C: 200 nuevo access + nuevo refresh
    end
```

La transacción usa `noRollbackFor = InvalidRefreshTokenException`: la revocación de la familia se confirma aunque la respuesta sea un error.

```mermaid
flowchart LR
    T1[RT-1<br/>usado] -->|replaced_by| T2[RT-2<br/>usado] -->|replaced_by| T3[RT-3<br/>vigente]
    ATK([Atacante presenta RT-1]) -.-> T1
    T1 -.->|reutilización detectada| REV[Se revocan RT-1, RT-2 y RT-3]
```

## 9. Manejo de errores

Cada capa lanza su propio tipo de excepción y **solo** `infrastructure` las traduce a HTTP, siempre en formato RFC 9457 (Problem Details).

```mermaid
flowchart TB
    subgraph domain
        DE[DomainException<br/>InvalidValueException VAL-001<br/>InvalidDocumentNumberException EMP-002<br/>CTR-001 ... CTR-008]
    end
    subgraph application
        NF[NotFoundException<br/>EMP-404, AUTH-404, AUTH-009]
        CF[ConflictException<br/>EMP-001, AUTH-007]
        UA[UnauthenticatedException<br/>AUTH-002]
    end
    subgraph infrastructure
        GEH[GlobalExceptionHandler<br/>@RestControllerAdvice]
        SEC[ProblemDetailsAuthenticationEntryPoint<br/>ProblemDetailsAccessDeniedHandler]
    end
    DE --> GEH
    NF --> GEH
    CF --> GEH
    UA --> GEH
    GEH --> PD[application/problem+json<br/>type, title, status, detail, code]
    SEC --> PD
```

| Origen | HTTP |
|---|---|
| `@Valid` en el controller | `400` con `errors[]` |
| `DomainException` | `422` (salvo `CTR-008` → `409` y `AUTH-005` → `401`) |
| `NotFoundException` | `404` |
| `ConflictException` | `409` |
| `UnauthenticatedException` | `401` |
| Filtros de seguridad | `401` (AUTH-003/004) o `403` (AUTH-001) |
| Cualquier otra | `500` sin detalles internos |

La tabla completa de códigos está en [business-rules.md §8](../business-rules.md#8-catálogo-de-errores-de-negocio).

## 10. Ciclos de vida

### 10.1 Contrato

Implementado y probado en `domain`; aún sin casos de uso ni endpoints.

```mermaid
stateDiagram-v2
    [*] --> DRAFT: create()
    DRAFT --> DRAFT: update()
    DRAFT --> ACTIVE: sign()
    DRAFT --> CANCELLED: cancel()
    ACTIVE --> ACTIVE: renew() · solo FIXED_TERM
    ACTIVE --> TERMINATED: terminate()
    ACTIVE --> EXPIRED: expire() · solo FIXED_TERM
    TERMINATED --> [*]
    EXPIRED --> [*]
    CANCELLED --> [*]
```

Cada transición registra un evento (`ContractCreated`, `ContractSigned`, `ContractRenewed`, `ContractTerminated`, `ContractExpired`), que forman la interfaz *sealed* `ContractEvent`. Hoy nadie los publica fuera del agregado (business-rules §9).

### 10.2 Refresh token

```mermaid
stateDiagram-v2
    [*] --> Vigente: issue() / rotate()
    Vigente --> Usado: rotate()
    Vigente --> Revocado: revoke() · logout, reutilización o usuario deshabilitado
    Vigente --> Expirado: pasa expires_at
    Usado --> Revocado: revokeFamily()
    Usado --> [*]
    Revocado --> [*]
    Expirado --> [*]
```

## 11. Pruebas por capa

```mermaid
flowchart TB
    E2E[Extremo a extremo<br/>AuthenticationFlowIntegrationTest<br/>BootstrapAdministratorIntegrationTest]
    INT[Integración · Testcontainers PostgreSQL<br/>adaptadores de persistencia, seguridad]
    SLICE[Slice web · @WebMvcTest<br/>Auth, Employee y UserController]
    APPT[Unitarias de application · Mockito sobre port.out]
    DOMT[Unitarias de domain · JUnit + AssertJ, sin mocks]
    E2E --- INT --- SLICE --- APPT --- DOMT
```

Cuanto más abajo, más pruebas, más rápidas y más reglas de negocio cubren. `./mvnw clean verify` exige ≥ 80 % de cobertura de líneas en `domain` y `application`.

## 12. Cómo añadir una funcionalidad

El orden de [CLAUDE.md](../../CLAUDE.md) va del centro hacia fuera. Ejemplo con `SignContractUseCase`, todavía pendiente:

```mermaid
flowchart LR
    A[1 · Dominio<br/>Contract.sign ya existe] --> B[2 · port.in<br/>SignContractUseCase]
    B --> C[3 · usecase<br/>SignContractService<br/>+ BR-CTR-005]
    C --> D[4 · port.out<br/>ContractRepositoryPort]
    D --> E[5 · Adaptador<br/>ContractPersistenceAdapter<br/>+ migración V5]
    E --> F[6 · Controller<br/>POST /contracts/id/sign]
    F --> G[7 · Pruebas<br/>de cada capa]
    C -.->|registrar bean| CFG[UseCaseConfig]
```

Lista de comprobación:

- [ ] ¿La regla está en [business-rules.md](../business-rules.md)? Si no, se pregunta antes de implementar.
- [ ] ¿Cada clase está en el módulo y paquete correctos (§3)?
- [ ] ¿El caso de uso está registrado en `UseCaseConfig` y no tiene `@Service`?
- [ ] ¿El controller solo usa el puerto de entrada y DTOs de `api/dto`?
- [ ] ¿Las nuevas excepciones tienen código en el catálogo y su traducción HTTP?
- [ ] ¿`./mvnw clean verify` pasa?

## 13. Observaciones

Diferencias entre el código actual y lo documentado en [architecture.md](../architecture.md) y [business-rules.md](../business-rules.md):

- **`Employee` sin estado:** el agregado aún no tiene `EmployeeStatus` ni los métodos de desactivar/reactivar (BR-EMP-005–008).
- **`Contract` sin persistencia:** el agregado está completo en `domain`, pero no hay `ContractRepositoryPort`, tabla ni endpoints.
- **Regla 9 (aislamiento en `infrastructure`):** verificada por búsqueda; `api` no importa de `persistence` ni `client`, y `persistence` y `security` no importan de `api`.
