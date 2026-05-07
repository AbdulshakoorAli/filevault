# FileVault — resume / document flow (Azure + GitHub)

[![Deploy Azure Functions](https://github.com/AbdulshakoorAli/filevault/actions/workflows/deploy-functions.yml/badge.svg?branch=main)](https://github.com/AbdulshakoorAli/filevault/actions/workflows/deploy-functions.yml)

Angular frontend and **Azure Functions (Java)** as the serverless API in the cloud. Run the UI against your deployed Function App (see environment files under `frontend/src/environments/`).

## Repo layout

| Path | Role |
|------|------|
| `functions/` | **Serverless API** — documents REST API (health, list, upload, download link, delete, share email, Document Intelligence). Deploy via GitHub Actions. |
| `frontend/` | Angular UI — configure `documentsApiUrl` in `src/environments/` for your Function App. |
| `.github/workflows/deploy-functions.yml` | **CI/CD** — Java unit tests (`mvn`), package Functions, deploy to Azure. |

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

Set **`documentsApiUrl`** in `frontend/src/environments/environment.development.ts` (for `ng serve`) and `environment.ts` (for production builds) to your Function App, e.g. `https://<your-app>.azurewebsites.net/api/documents`.

## Automated tests (CI + local)

- **Functions (JUnit 5):** `cd functions && mvn test` — three unit tests (multipart upload parsing + JSON shape for the API).
- The **Deploy Azure Functions** workflow runs `mvn clean package` (tests then package) before deploy; a failing test blocks the release.

## Security

- Do not commit Azure keys, connection strings, or publish profiles. Use GitHub **Secrets** and Azure **Configuration** / Key Vault.
- Rotate any credentials that were ever committed to git.

## License

MIT
