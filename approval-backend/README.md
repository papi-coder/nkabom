# Nkabom approval backend

This Spring Boot service serves the dashboard and exposes a public approval-request API. It validates Cloudflare Turnstile challenges, limits each client address to five requests per ten minutes, and sends plain-text approval notifications through Amazon SES to `solopappy76@gmail.com`. It does not send SMS, WhatsApp, or email campaigns to contacts.

## Required configuration

| Variable | Description |
| --- | --- |
| `TURNSTILE_SITE_KEY` | Public site key for the Turnstile widget |
| `TURNSTILE_SECRET_KEY` | Private Turnstile secret; keep it only in the backend environment |
| `TURNSTILE_EXPECTED_HOSTNAME` | Exact hostname expected in successful Turnstile responses (for example, `127.0.0.1` locally or the production domain) |
| `APPROVAL_FROM_EMAIL` | Email address verified in SES in the configured AWS region |
| `AWS_REGION` | SES region; defaults to `us-east-1` |
| `CORS_ALLOWED_ORIGINS` | Optional comma-separated browser origins when the dashboard is hosted separately; leave empty for same-origin hosting |
| `PORT` | HTTP port; defaults to `8080` |

## Configure Turnstile and SES

1. In the Cloudflare dashboard, create a Turnstile widget for the dashboard hostname. Add `127.0.0.1` for the current local preview and the public hostname before deployment.
2. Set `TURNSTILE_SITE_KEY` and `TURNSTILE_SECRET_KEY` from the widget settings. The site key is public; never place the secret key in HTML or commit it.
3. In the AWS Console, create a CloudFormation stack from `aws/ses-sender.yaml`. Verify the sender address using the email SES sends. Request SES production access if the account is in the SES sandbox and needs to send to unverified recipients.
4. Give the backend's AWS runtime role only the `ses:SendEmail` permission needed for the verified sender. The backend uses the AWS SDK default credential chain; do not put AWS credentials in this repository.

The application rate limiter is per running instance. For a multi-instance public deployment, also put AWS WAF in front of the public entry point and configure a rate-based rule.

## Local run

Use JDK 25 and Maven. Open PowerShell in this directory, set the Turnstile and SES values, then start the backend:

```powershell
$env:TURNSTILE_SITE_KEY = "<site-key>"
$env:TURNSTILE_SECRET_KEY = "<secret-key>"
$env:TURNSTILE_EXPECTED_HOSTNAME = "127.0.0.1"
$env:APPROVAL_FROM_EMAIL = "solopappy76@gmail.com"
$env:AWS_REGION = "us-east-1"
$env:CORS_ALLOWED_ORIGINS = "http://127.0.0.1:5500"
mvn spring-boot:run
```

The page opened in VS Code Live Server on port 5500 calls the backend at `http://127.0.0.1:8080`. If the API runs elsewhere, set `window.NKABOM_API_BASE_URL` before the page script to that API's base URL. For same-origin use, serve the dashboard through Spring Boot and leave `CORS_ALLOWED_ORIGINS` empty. To send email locally, configure AWS credentials through an AWS profile.

## Container

From the repository root, build with:

```powershell
docker build -f approval-backend/Dockerfile -t nkabom-approval-backend .
```

Push the image to Amazon ECR and deploy it to AWS App Runner or another HTTPS container service. Configure the environment variables above and attach an instance role with SES send permission. Configure the Turnstile widget for the public hostname and set `TURNSTILE_EXPECTED_HOSTNAME` to that hostname.
