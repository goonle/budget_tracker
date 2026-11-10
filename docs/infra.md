# Infrastructure (GCP VM + Neon)

- **App**: Docker container on a GCP Compute Engine VM (e2-micro), run with Docker Compose.
- **Database**: [Neon](https://neon.tech) (managed PostgreSQL). Nothing database-related runs on the VM.
- **Deploy**: on every push to `main`, GitHub Actions builds the image, pushes it to GitHub Container Registry and runs `docker compose pull app && docker compose up -d app` on the VM over SSH. See `.github/workflows/pipeline.yml`.

## 1. Create the VM

```bash
gcloud compute instances create <INSTANCE_NAME> \
    --project=<YOUR_PROJECT_ID> \
    --zone=us-west1-b \
    --machine-type=e2-micro \
    --network-interface=network-tier=PREMIUM,stack-type=IPV4_ONLY,subnet=default \
    --maintenance-policy=MIGRATE \
    --provisioning-model=STANDARD \
    --service-account=<PROJECT_NUMBER>-compute@developer.gserviceaccount.com \
    --scopes=https://www.googleapis.com/auth/devstorage.read_only,https://www.googleapis.com/auth/logging.write,https://www.googleapis.com/auth/monitoring.write,https://www.googleapis.com/auth/service.management.readonly,https://www.googleapis.com/auth/servicecontrol,https://www.googleapis.com/auth/trace.append \
    --tags=budget-tracker \
    --create-disk=auto-delete=yes,boot=yes,device-name=<INSTANCE_NAME>,image=projects/debian-cloud/global/images/debian-13-trixie-v20261102,mode=rw,size=30,type=pd-standard \
    --no-shielded-secure-boot \
    --shielded-vtpm \
    --shielded-integrity-monitoring \
    --labels=goog-ec-src=vm_add-gcloud \
    --reservation-affinity=any
```

## 2. Install Docker on the VM

```bash
gcloud compute ssh <INSTANCE_NAME> --zone=us-west1-b
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER   # log out and back in so docker works without sudo
```

## 3. Database on Neon

1. Create a project and database in the Neon console. Use **PostgreSQL 15 or newer**: migration V3 uses `UNIQUE NULLS NOT DISTINCT`.
2. Copy the connection details. Spring needs the **JDBC** form, with the user and password kept separate:
   ```
   jdbc:postgresql://<endpoint>.neon.tech/<database>?sslmode=require
   ```
3. Flyway creates all tables on the app's first start. Nothing needs to be run by hand.

## 4. App folder and config

```bash
sudo mkdir -p /opt/budgettracker && sudo chown $USER /opt/budgettracker
cd /opt/budgettracker
```

Copy [`deploy/docker-compose.yml`](../deploy/docker-compose.yml) here, and create `.env` next to it from [`deploy/.env.example`](../deploy/.env.example) with the real Neon values. Never commit `.env`.

The deploy job runs `cd /opt/budgettracker`, so the folder name must match.

## 5. Let the VM pull the image

The image on GitHub Container Registry is private, so log in once with a GitHub personal access token (classic) that has only `read:packages`:

```bash
echo <TOKEN> | docker login ghcr.io -u goonle --password-stdin
```

## 6. GitHub secrets for the deploy job

Repository → Settings → Secrets and variables → Actions:

| Secret | Value |
|---|---|
| `VM_HOST` | the VM's external IP |
| `VM_USER` | the Linux user on the VM |
| `VM_SSH_KEY` | private key of the `gh-deploy` key pair; its `.pub` goes in the VM's `~/.ssh/authorized_keys` |

## 7. First start and check

```bash
docker compose up -d
curl -s http://localhost:8080/actuator/health   # → {"status":"UP"}
```

The first start can take a while: Spring Boot is slow on e2-micro, and Neon may need a few seconds to wake a suspended database. The deploy job waits up to 150 seconds.

After that, every push to `main` deploys automatically.
