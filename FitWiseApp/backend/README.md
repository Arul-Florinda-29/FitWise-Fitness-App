# FitWise REST Backend (JDK-only)

This backend is implemented with the JDK's built-in `HttpServer`, so no Maven or third-party libraries are required. Use JDK 17 or newer.

## Start
1. Keep the project extracted.
2. Double-click `backend/run-backend.bat` and leave the terminal open.
3. Verify `http://127.0.0.1:8080/api/health` returns `{"ok":true,...}`.
4. Start the FitWise desktop app with the root `run.bat`.

The desktop registration and login screens now call this backend. Existing local accounts are migrated when the user successfully logs in locally for the first time after the backend is started.

## API
- `GET /api/health` — backend health check.
- `POST /api/auth/register` — URL-encoded form: `name,email,password,age,height,weight,gender,activity,goal,diet,allergies`.
- `POST /api/auth/login` — URL-encoded `email,password`; returns a bearer token.
- `POST /api/experience` — bearer token required; form fields `type,value,date,notes`.
- `GET /api/experience/history` — bearer token required; returns saved experience records.

## Storage and security notes
- Backend records persist in `%USERPROFILE%\.fitwise-backend\backend.ser` on Windows.
- Passwords are stored as PBKDF2-HMAC-SHA256 hashes with random salts, not plaintext.
- The server binds only to `127.0.0.1` by default; it is for local development/demo use, not direct public deployment.
- This is a lightweight educational backend. The current desktop tracker screens still keep their detailed logs and online-learning model in the existing local FitWise store; the REST experience endpoint is available for adding remote sync of those records in a later integration step.
