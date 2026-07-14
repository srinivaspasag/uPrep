# UPrep LMS — AWS Deploy Runbook

Step-by-step to take `backend` (8 services) + website + virtual-classroom
live on **AWS (Mumbai `ap-south-1`)** with **MongoDB Atlas**, a domain, and HTTPS.

**Target:** small production (1k–10k users). **Est. time:** ~½–1 day. **Est. cost:** ~$330–400/mo.

> Deploy model: one EC2 VM running Docker Compose (the repo already ships `docker-compose.yml`).
> Move to ECS/EKS later for autoscaling; the images and config are identical.

---

## 0) Prerequisites (do first)

- [ ] AWS account with billing enabled; IAM admin user (not root) for daily use.
- [ ] A domain you control (register in Route 53 or point your registrar's NS to Route 53).
- [ ] An SSH keypair for EC2.
- [ ] **P0 — rotate the committed secrets** before going public: the AWS access key and
      SMTP password currently in `application-prod.properties` must be **deactivated in IAM**
      and replaced (see step 5).

---

## 1) MongoDB Atlas (managed database)

1. Create a MongoDB Atlas account → **Build a Database** → **Dedicated M10**.
2. **Cloud provider: AWS**, **Region: Mumbai (`ap-south-1`)** (same region as the app = low latency).
3. Create a DB user (e.g. `uprep_app`) with a strong password; note it.
4. **Network Access:** add the EC2 instance's public IP (or VPC peering later). For first
   boot you can temporarily allow your own IP too.
5. Copy the **SRV connection string**, e.g.:
   ```
   mongodb+srv://uprep_app:<PASSWORD>@cluster0.xxxx.mongodb.net/prodlp?retryWrites=true&w=majority
   ```
   The app database name is **`prodlp`** (keep it in the URI path).

---

## 2) S3 bucket + IAM (file storage)

1. **S3 → Create bucket**, e.g. `uprep-content-prod`, region `ap-south-1`, block public access ON.
2. **IAM → Create policy** granting `s3:GetObject/PutObject/DeleteObject/ListBucket` on that bucket.
3. **IAM → Create role** for EC2 (`uprep-ec2-role`) and attach the policy.
   *(Preferred over access keys — the instance gets S3 access via the role, no keys in files.)*

---

## 3) EC2 instance (compute)

1. **EC2 → Launch instance**
   - Name: `uprep-app`
   - AMI: **Ubuntu 22.04 (ARM64)**
   - Type: **`m7g.2xlarge`** (8 vCPU / 32 GB, Graviton)
   - Key pair: your SSH key
   - Storage: **200 GB gp3**
   - IAM instance profile: **`uprep-ec2-role`** (from step 2)
2. **Security group (inbound):**
   - `22` (SSH) from your IP only
   - `80`, `443` from anywhere
   - Keep service ports `8081–8088`, `20000` **closed** to the internet (reached via the proxy).
3. Allocate an **Elastic IP** and associate it (stable public IP for DNS).

---

## 4) Install Docker on the VM

```bash
ssh ubuntu@<ELASTIC_IP>
sudo apt-get update && sudo apt-get install -y docker.io docker-compose-plugin git nginx
sudo usermod -aG docker $USER && newgrp docker
docker --version && docker compose version
```

---

## 5) Get the code + configure secrets

```bash
git clone <your-repo-url> uprep && cd uprep
cp .env.example .env
```

Edit `.env` to point the services at Atlas and set the heap:

```bash
# .env
JAVA_OPTS=-Xms256m -Xmx768m
SPRING_DATA_MONGODB_URI=mongodb+srv://uprep_app:<PASSWORD>@cluster0.xxxx.mongodb.net/prodlp?retryWrites=true&w=majority
```

In `docker-compose.yml`, for each `*-services` block:
- **add** `SPRING_DATA_MONGODB_URI: ${SPRING_DATA_MONGODB_URI}` under `environment`,
- **remove** the local `mongo` service and `SPRING_DATA_MONGODB_HOST: mongo` (Atlas replaces it),
- set S3 to use the IAM role: clear `amazon.s3.accessKey/secretKey` (SDK picks up the role),
  keep `amazon.s3.bucket.identifier=uprep-content-prod`.

> **P0 secret hygiene:** rotate the old committed AWS key + SMTP password in IAM/SES now.
> Long-term, load secrets from **AWS Secrets Manager** instead of `.env`/properties, and
> scrub the old values from git history.

---

## 6) Build & start

```bash
docker compose up -d --build          # first build ~5–10 min (full Maven reactor)
docker compose ps                     # every service should be "running"
```

Smoke-test each service locally on the box:

```bash
for p in 8081 8082 8083 8084 8085 8086 8087 8088; do
  echo "== :$p =="; curl -s -o /dev/null -w "%{http_code}\n" http://localhost:$p/v2/api-docs
done
curl -I http://localhost:80           # website
curl -s http://localhost:20000/actuator/health 2>/dev/null || true   # virtual classroom
```

Expect `200` for services that booted cleanly. Investigate any that don't (logs:
`docker compose logs -f <service>`).

---

## 7) Reverse proxy + HTTPS

1. Point DNS: **Route 53 → A record** `app.yourdomain.com → <ELASTIC_IP>`.
2. Configure Nginx to route the public hostname to internal services (example routes services
   under paths; adjust to your gateway design):
   ```nginx
   server {
     listen 80;
     server_name app.yourdomain.com;
     location /user/    { proxy_pass http://127.0.0.1:8081/; }
     location /org/     { proxy_pass http://127.0.0.1:8082/; }
     location /content/ { proxy_pass http://127.0.0.1:8083/; }
     location /cmds/    { proxy_pass http://127.0.0.1:8084/; }
     location /         { proxy_pass http://127.0.0.1:80/;  }  # website
   }
   ```
3. Enable HTTPS with Let's Encrypt:
   ```bash
   sudo snap install --classic certbot && sudo ln -sf /snap/bin/certbot /usr/bin/certbot
   sudo certbot --nginx -d app.yourdomain.com
   ```
   *(Alternative: put an AWS **ALB + ACM certificate** in front instead of Nginx TLS.)*

---

## 8) Seed data & verify end-to-end

```bash
# from the box (or your laptop with Atlas access)
# import any demo seed you use, e.g. mongosh against the Atlas URI:
mongosh "mongodb+srv://uprep_app:<PASSWORD>@cluster0.xxxx.mongodb.net/prodlp" seed-demo-test.js
```

Then hit a real endpoint through the proxy, e.g. authenticate a user, fetch content — confirm
a 200 + expected JSON.

---

## 9) Operate

```bash
docker compose logs -f content-services   # tail one service
docker compose restart cmds-services       # restart one
docker compose pull && docker compose up -d --build   # redeploy after code change
docker compose down                        # stop all
```

- **Backups:** Atlas provides automated backups — confirm the schedule/retention.
- **Monitoring:** enable the CloudWatch agent for host metrics; add Spring Actuator + alarms.
- **Scaling:** resize EC2 (`m7g.4xlarge`) and Atlas (M20/M30) as load grows; then migrate
  to ECS/EKS behind an ALB for autoscaling.

---

## Checklist (launch gate)

- [ ] All 8 services return `200` on `/v2/api-docs`
- [ ] Old committed AWS/SMTP creds rotated; app uses IAM role + Secrets Manager
- [ ] HTTPS working; service ports `8081–8088`/`20000` not public
- [ ] Atlas backups on; network access locked to the EC2 IP/VPC
- [ ] UI decision made and wired (or explicitly deferred for this phase)
- [ ] End-to-end journey verified (login → content → test)

---
_See `UPREP_FINAL_BRIEF.md` for the planning summary and the `uprep-cloud-deployment-review` canvas for the architecture overview._
