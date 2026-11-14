# IT Asset Management System — SE411 Project

Full-stack app for tracking IT hardware (laptops, desktops, servers, mobiles).

- **Part 1:** React frontend (repo root)
- **Part 2:** Java Spring Boot REST server (`backend/`) + integration with the frontend

## Architecture

```
┌──────────────────────┐   HTTP / JSON (fetch)   ┌──────────────────────────────────────────────┐
│  React SPA :3000     │ ──────────────────────▶ │  Spring Boot server :8080                    │
│                      │                         │                                              │
│  App.js (state)      │                         │  AssetController   (REST, @RestController)   │
│  AssetList / Form    │ ◀────────────────────── │        │                                     │
│  Pagination / About  │     JSON responses      │  AssetService      (business logic)          │
│  api/assetApi.js     │                         │        │                                     │
└──────────────────────┘                         │  AssetRepository   (Spring Data JPA)         │
                                                 │        │                                     │
                                                 │  H2 in-memory database (table: assets)       │
                                                 │                                              │
                                                 │  + GlobalExceptionHandler (404/400/409 JSON) │
                                                 │  + CorsConfig (allows localhost:3000)        │
                                                 │  + DataSeeder (12 sample assets at startup)  │
                                                 └──────────────────────────────────────────────┘
```

## REST API

Base URL: `http://localhost:8080/api/assets`

| Method | Route | Description | Request body | Success | Errors |
|---|---|---|---|---|---|
| POST | `/api/assets` | Add an asset | Asset JSON (no id) | 201 Created + asset | 400 invalid, 409 duplicate serial |
| GET | `/api/assets/{id}` | Search an asset by id | – | 200 OK + asset | 404 not found |
| GET | `/api/assets?page=0&size=5&sort=id,asc` | All assets, paginated | – | 200 OK + page object | – |
| PUT | `/api/assets/{id}` | Update an asset | Asset JSON | 200 OK + updated asset | 400 invalid, 404 not found |
| DELETE | `/api/assets/{id}` | Delete an asset | – | 204 No Content | 404 not found |

Asset JSON:
```json
{ "id": 1, "name": "Dell XPS 15", "category": "Laptop", "serialNumber": "SN-33211",
  "assignedTo": "Eyad Al-Shuhail", "status": "In Use", "purchaseDate": "2024-03-10" }
```

Page JSON:
```json
{ "content": [ ... ], "page": 0, "size": 5, "totalElements": 12, "totalPages": 3, "first": true, "last": false }
```

## Running it

Requirements: Java 17+ (17 or 21 recommended), Maven 3.9+ (or IntelliJ's bundled Maven), Node 18+.

```bash
# 1) backend
cd backend
mvn spring-boot:run          # http://localhost:8080/api/assets

# 2) frontend (new terminal, repo root)
npm install
npm start                    # http://localhost:3000
```

H2 console (to show the database in the demo): http://localhost:8080/h2-console, JDBC URL `jdbc:h2:mem:assetdb`, user `sa`, empty password.

Change the API URL for the frontend with `REACT_APP_API_URL=http://host:port/api/assets npm start`.

## Tests

```bash
cd backend && mvn test       # JUnit 5 + Mockito; coverage report: backend/target/site/jacoco/index.html
npm test                     # React (Jest + React Testing Library), from repo root
```

| Test class | What it covers |
|---|---|
| `AssetServiceTest` | Service logic with a **Mockito mock** repository (`@Mock` / `@InjectMocks`): create, get by id (found / 404), paginated list, update (found / 404), delete (found / 404) |
| `AssetControllerTest` | Every REST route through **MockMvc** with the service mocked (`@WebMvcTest` + `@MockitoBean`): status codes, JSON body, pagination params and defaults, validation 400, 404, 409, CORS |
| `DataSeederTest` | Seeding runs only when the DB is empty |
| `AssetManagementApplicationTests` | Whole Spring context starts |

## Code walkthrough (for the Q&A)

- **Layers:** Controller → Service → Repository. Each layer only talks to the one below it, and dependencies are passed in through constructors. That's why the tests can swap in mocks.
- **`Asset`** (`@Entity`): JPA maps it to the `assets` table. `@Id @GeneratedValue(IDENTITY)` means the DB generates ids. `@NotBlank`/`@NotNull` are validation rules that `@Valid` checks in the controller.
- **`AssetRepository extends JpaRepository<Asset, Long>`:** Spring generates the implementation at runtime. `findAll(Pageable)` does the `LIMIT/OFFSET` paging SQL.
- **Pagination:** the controller takes a `Pageable` built from `?page=&size=&sort=` (default page 0, size 5, sorted by id) and wraps the result in the `PageResponse` record, which has a stable JSON shape.
- **Errors:** the service throws `AssetNotFoundException`, and `@RestControllerAdvice GlobalExceptionHandler` turns it into a 404 JSON response. Validation errors become 400 and duplicate serial numbers become 409.
- **CORS:** the browser blocks calls from :3000 to :8080 unless the server allows that origin, so `CorsConfig` allows it.
- **Frontend integration:** `src/api/assetApi.js` wraps `fetch` for each route. `App.js` keeps `page`/`size` in state, calls `getAssets(page, size)` in a `useEffect`, and re-fetches after each add, update or delete.
- **Mockito terms:** `when(...).thenReturn(...)` stubs a call, `verify(...)` checks that a call happened, `never()` checks that it didn't, and `ArgumentCaptor` grabs the argument that was passed so the test can inspect it.

## Team

| Name | Student ID |
|---|---|
| Mohammed Al-Jarbou | 221110338 |
| Eyad Al-Shuhail | 222110093 |
| Hamad Al-Waheebi | 222110607 |
| Ahmad Al-Talhi | 222111457 |
