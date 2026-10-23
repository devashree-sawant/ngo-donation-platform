# HopeBridge — Frontend

React + Vite frontend for HopeBridge, a Spring Boot NGO donation platform.

## Features

- Login and registration for NGO and donor accounts
- NGO dashboard for publishing, editing, and tracking money/item campaigns
- Donor dashboard with campaign search, type filters, contribution history, and certificates
- Money donations and item pledges
- Profile page to view and update name, phone, address, and NGO description
- Responsive navigation and layouts for desktop and mobile
- Printable contribution acknowledgement (browser Print / Save as PDF)

## Run locally

1. Start the Spring Boot backend on `http://localhost:8080`.
2. In this directory, install dependencies and start Vite:

   ```bash
   npm install
   npm run dev
   ```

3. Open the local URL displayed by Vite (normally `http://localhost:5173`).

## API base URL

The default API URL is `http://localhost:8080/api`. To use a different backend URL, create a `.env` file in this directory using `.env.example`:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

Restart Vite after changing environment variables.

## Profile editing

The profile page uses the existing backend routes:

- `GET /api/ngos/{id}` and `PUT /api/ngos/{id}` for NGO accounts
- `GET /api/donors/{id}` and `PUT /api/donors/{id}` for donor accounts

The email is shown as read-only because it is the identity associated with the active JWT. Profile updates do not change email or password.
