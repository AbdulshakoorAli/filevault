# FileVault — resume / document flow (Azure + GitHub)

Angular frontend, **Azure Functions (Java)** for the serverless API in the cloud, and an optional **Spring Boot** `backend/` folder for local experiments only. Production-style deploy targets **Functions**, not the Spring app.

## Repo layout

| Path | Role |
|------|------|
| `functions/` | **Serverless API** — build and deploy this to Azure Functions. |
| `frontend/` | Angular UI — point it at your Function App URL when testing cloud. |
| `backend/` | Optional legacy Spring API (local `http://localhost:8080`); not used by GitHub deploy. |
| `.github/workflows/deploy-functions.yml` | **CI/CD** — build + deploy on push to `main` / `master`. |

## Deploy with GitHub Actions (recommended)

1. Create a GitHub repository and push this repo.
2. **GitHub** → **Settings** → **Secrets and variables** → **Actions** → add:
   - `AZURE_FUNCTIONAPP_NAME` — Function App name (hostname prefix before `.azurewebsites.net`).
   - `AZURE_FUNCTIONAPP_PUBLISH_PROFILE` — full XML from Azure Portal → Function App → **Get publish profile**.
3. Push a change under `functions/` to **`main`** or **`master`**, or run the workflow manually (**Actions** → **Deploy Azure Functions** → **Run workflow**).
4. Smoke test: `https://<AZURE_FUNCTIONAPP_NAME>.azurewebsites.net/api/health`

Details are in the comments at the top of `.github/workflows/deploy-functions.yml`.

## Local development

### Functions (Java)

```bash
cd functions
mvn clean package
```

See `functions/README.md` for optional local run (Core Tools / `local.settings.json`).

### Frontend

```bash
cd frontend
npm install
ng serve
```

Set the API base URL in `frontend/src/app/services/document.service.ts` (e.g. your Function App `https://<app>.azurewebsites.net/api/...` when calling Azure, or `http://localhost:8080` if you still run Spring locally).

### Optional Spring backend (local only)

```bash
cd backend
# set AZURE_STORAGE_CONNECTION_STRING (see backend/src/main/resources/application.yml)
mvn spring-boot:run
```

## Security

- Do not commit Azure keys, connection strings, or publish profiles. Use GitHub **Secrets** and Azure **Configuration** / Key Vault.
- Rotate any credentials that were ever committed to git.

## License

MIT
