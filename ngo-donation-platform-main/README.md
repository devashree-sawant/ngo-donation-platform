# NGO Donation Platform

A React + Spring Boot application connecting donors with NGOs. NGOs can publish money or item campaigns; donors can contribute money or pledge items; campaign progress is updated by the backend; donors can review their history and print a contribution acknowledgement certificate.

## Project layout

- `ngo-platform/` — Spring Boot REST API, JPA entities, MySQL persistence, validation, JWT authentication/authorization, contribution processing, and certificate data endpoint.
- `ngo-frontend/` — Vite/React app with registration/login, NGO campaign management, money/item contributions, donor history, and print-to-PDF certificate.

## 1. Database setup

Create the local MySQL database:

```sql
CREATE DATABASE ngo_platform;
```

The backend reads database/JWT configuration from environment variables. Set them before starting Spring Boot. `DB_PASSWORD` must be the password for your local MySQL user.

**Windows PowerShell:**

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/ngo_platform"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "YOUR_MYSQL_PASSWORD"
$env:JWT_SECRET = "replace-this-with-a-long-random-secret-of-at-least-32-bytes"
cd ngo-platform
.\mvnw.cmd spring-boot:run
```

If using IntelliJ, set the same values under **Run > Edit Configurations > Environment variables**, then run `NgoPlatformApplication.java`.

**Linux/macOS:**

```bash
export DB_URL='jdbc:mysql://localhost:3306/ngo_platform'
export DB_USERNAME='root'
export DB_PASSWORD='YOUR_MYSQL_PASSWORD'
export JWT_SECRET='replace-this-with-a-long-random-secret-of-at-least-32-bytes'
cd ngo-platform
chmod +x mvnw
./mvnw spring-boot:run
```

The API runs at `http://localhost:8080`. This student project uses `ddl-auto=update` for local development. Back up data before changing entity mappings. Do not commit database passwords or production JWT secrets.

## 2. Frontend setup

In a separate terminal:

```bash
cd ngo-frontend
npm install
npm run dev
```

Open the URL printed by Vite, normally `http://localhost:5173`. The frontend reads `VITE_API_BASE_URL` if set; otherwise it uses `http://localhost:8080/api`.

## 3. Main user workflow

1. Register an NGO or donor on the login page. Registration signs the new account in.
2. As an NGO, create a `MONEY` campaign with a target amount or an `ITEM` campaign with an item name and required quantity. You can edit campaigns and delete only those without contribution history.
3. As a donor, browse campaigns and contribute money or submit a quantity for the campaign's requested item.
4. The backend validates the contribution and updates the amount/quantity in a transaction. It marks the campaign `COMPLETED` when its goal is reached.
5. The donor dashboard shows contribution history. Open a contribution certificate and select **Print / Save as PDF** in the browser.

Legacy accounts created before password hashing are supported: when a legacy plaintext password is successfully used to log in, the backend upgrades it to a BCrypt hash. New registrations store BCrypt hashes. Passwords are not serialized in JSON responses.

## 4. API reference

All endpoints below are relative to `http://localhost:8080`.

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `POST` | `/api/auth/register/ngo` | Public | Register an NGO and receive a token |
| `POST` | `/api/auth/register/donor` | Public | Register a donor and receive a token |
| `POST` | `/api/auth/login` | Public | Login; response includes token, role, id and name |
| `GET` | `/api/ngos` | Public | List NGOs |
| `GET` | `/api/ngos/{id}` | Public | Get NGO profile |
| `POST` | `/api/ngos` | Public | Create NGO via legacy/Postman API |
| `PUT` / `DELETE` | `/api/ngos/{id}` | Owning NGO | Update/delete own account; deletion is blocked while campaigns exist |
| `GET` | `/api/needs` | Public | Browse all campaigns |
| `GET` | `/api/needs/{id}` | Public | Campaign details |
| `GET` | `/api/needs/{id}/progress` | Public | Progress for money or item campaign |
| `POST` | `/api/needs` | NGO | Create campaign for the authenticated NGO |
| `PUT` / `DELETE` | `/api/needs/{id}` | Owning NGO | Update/delete own campaign; deletion is blocked when contributions exist |
| `POST` | `/api/donors` | Public | Create donor via legacy/Postman API |
| `GET` | `/api/donors/{id}/contributions` | That donor | Donor history, newest first |
| `POST` | `/api/contributions` | Donor | Create money contribution or item pledge |
| `GET` | `/api/contributions` | NGO | Contributions for campaigns owned by that NGO |
| `GET` | `/api/contributions/{id}` | Owning donor/NGO | Contribution details |
| `GET` | `/api/contributions/{id}/certificate` | Owning donor | Certificate details for printable acknowledgement |
| `DELETE` | `/api/contributions/{id}` | Owning donor/NGO | Delete contribution and reverse the campaign progress change |

Protected requests need `Authorization: Bearer <token>`. The frontend Axios client adds the token automatically. A donor's identity is derived from that token; a client-supplied donor ID is checked and is never trusted as the source of identity.

### Example money campaign

```json
{
  "title": "Education Fund",
  "description": "Help fund learning materials.",
  "type": "MONEY",
  "targetAmount": 50000
}
```

### Example item campaign

```json
{
  "title": "Food Kit Distribution",
  "description": "Provide grocery kits to local families.",
  "type": "ITEM",
  "itemRequired": "Food Kit",
  "requiredQuantity": 200
}
```

### Example money contribution

```json
{
  "type": "MONEY",
  "amount": 250,
  "need": { "id": 1 }
}
```

### Example item contribution

```json
{
  "type": "ITEM",
  "itemName": "Food Kit",
  "quantity": 5,
  "need": { "id": 2 }
}
```

## 5. Business rules implemented

- New passwords are BCrypt-hashed; legacy plaintext passwords are upgraded following a successful login.
- A single email cannot be registered for both an NGO and a donor.
- Need type is `MONEY` or `ITEM`; target amount and required quantity must be positive.
- Money contribution amounts must be positive, target a money campaign, and not exceed the remaining target.
- Item pledges must have a positive whole quantity, match the requested item, and not exceed the remaining requirement.
- Only the owning NGO can manage a campaign. Donors can only access their own contribution history and certificates.
- Campaigns with contribution history and donor accounts with contribution history cannot be deleted, preserving references.
- Deleting a contribution reverses its amount/quantity and recalculates campaign status.
- Validation and common API errors are returned in a consistent JSON error format.

## 6. Testing

Backend tests use an in-memory H2 database and don't require local MySQL:

```bash
cd ngo-platform
./mvnw test
```

Frontend validation:

```bash
cd ngo-frontend
npm install
npm run build
npm run lint
```

Before a demonstration, test registrations and logins for both roles, creating a money and an item campaign, both contribution types, goal completion, donor history, certificate printing, rejected oversize/invalid contributions, and authorization failures (for example, a donor trying to create a campaign).
