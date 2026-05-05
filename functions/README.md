# FileVault Azure Functions (Java)

Serverless HTTP API for FileVault. This is what **GitHub Actions** builds and deploys (see `.github/workflows/deploy-functions.yml`).

## Deploy (GitHub — no Azure CLI required on your PC)

1. Repository **Settings** → **Secrets and variables** → **Actions**:
   - `AZURE_FUNCTIONAPP_NAME`
   - `AZURE_FUNCTIONAPP_PUBLISH_PROFILE` (full `.PublishSettings` XML)
2. Push to `main` / `master` (paths under `functions/`) or run the workflow manually.

After a successful run:

`https://<functionAppName>.azurewebsites.net/api/health` → `{"status":"ok","service":"filevault-functions"}`

## Local build

```bash
cd functions
mvn clean package
```

## Optional: deploy from your machine with Maven + Azure CLI

Only if you have `az login` working. Edit `pom.xml` properties or pass `-DfunctionAppName=...` etc.:

```bash
mvn clean package azure-functions:deploy -DfunctionAppName=my-func -DfunctionResourceGroup=my-rg -DfunctionRegion=westeurope
```

## Local run (optional)

- Copy `local.settings.json.example` → `local.settings.json` (not committed).
- [Azure Functions Core Tools](https://learn.microsoft.com/azure/azure-functions/functions-run-local) and optionally Azure CLI for auth to real storage.

## Azure portal (later)

**Function App → Configuration**: storage connection string, Document Intelligence keys, etc.  
**Function App → API → CORS**: add your Angular origin (e.g. `http://localhost:4200`).

Do not commit secrets in git.
