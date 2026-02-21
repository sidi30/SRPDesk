# GitLab Webhook Integration

LexSecura can automatically create releases when tags or releases are pushed to GitLab.

## How It Works

1. A GitLab project sends webhook events to LexSecura
2. LexSecura verifies the secret token header (`X-Gitlab-Token`)
3. On `tag_push` or `release` (create) events, a new Release is created for the mapped product
4. Duplicate events are skipped (idempotent processing via `processed_webhook_events` table)

## Prerequisites

- A product must exist in LexSecura
- A **product-repo mapping** must be configured in the database linking the GitLab project ID to the LexSecura product

### Create a Product-Repo Mapping

Insert a row into `product_repo_mappings`:

```sql
INSERT INTO product_repo_mappings (org_id, product_id, forge, project_id, repo_url)
VALUES (
  'your-org-uuid',
  'your-product-uuid',
  'GITLAB',
  12345,                              -- GitLab project ID (numeric)
  'https://gitlab.com/org/repo'       -- optional, for reference
);
```

You can find the GitLab project ID on the project's **Settings > General** page, or via the GitLab API.

## LexSecura Configuration

Set the webhook secret via environment variable:

```bash
GITLAB_WEBHOOK_SECRET=your-secret-token-here
```

Or in `application.yml`:

```yaml
app:
  gitlab:
    webhook-secret: ${GITLAB_WEBHOOK_SECRET:}
```

If the secret is empty, **all webhooks are rejected**.

## GitLab Configuration

### Step 1: Navigate to Webhooks

1. Open your GitLab project
2. Go to **Settings > Webhooks**
3. Click **Add new webhook**

### Step 2: Configure the Webhook

| Field | Value |
|-------|-------|
| **URL** | `https://your-lexsecura-domain/integrations/gitlab/webhook` |
| **Secret token** | The same value as `GITLAB_WEBHOOK_SECRET` |
| **Trigger events** | Check **Tag push events** and/or **Releases events** |
| **Enable SSL verification** | Recommended: Yes |

### Step 3: Save and Test

1. Click **Add webhook**
2. Use the **Test** dropdown next to the webhook to send a test event
3. Verify the response is `200 OK`

## Supported Events

### Tag Push (`tag_push`)

Triggered when a tag is pushed. LexSecura extracts:
- **version**: tag name (e.g., `v1.0.0` from `refs/tags/v1.0.0`)
- **gitRef**: the commit SHA (`checkout_sha`)

### Release (`release`)

Triggered when a GitLab release is created. LexSecura extracts:
- **version**: the release tag name
- **gitRef**: the commit SHA

Only the `create` action is processed. Update/delete actions are ignored.

## Endpoint Details

```
POST /integrations/gitlab/webhook
```

**Headers:**
- `X-Gitlab-Token` (required): must match the configured webhook secret
- `X-Gitlab-Event`: event type (`Tag Push Hook`, `Release Hook`)

**Response codes:**
- `200 OK` — event processed (or skipped if already processed / no mapping)
- `401 Unauthorized` — invalid or missing token

**Authentication:** This endpoint does not require OAuth2/JWT. It uses the `X-Gitlab-Token` header for authentication.

## Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| 401 on all webhooks | Secret mismatch or empty | Verify `GITLAB_WEBHOOK_SECRET` matches GitLab secret token |
| 200 but no release created | No product-repo mapping | Create a row in `product_repo_mappings` with the correct `project_id` |
| 200 but duplicate skipped | Event already processed | Normal idempotent behavior — check `processed_webhook_events` table |
| Release version wrong | Tag name parsing | LexSecura strips `refs/tags/` prefix; the remaining string is the version |

## Audit Trail

Every release created via webhook is recorded in the audit trail with:
- **entity_type**: `RELEASE`
- **action**: `CREATE_VIA_WEBHOOK`
- **payload**: `{"source": "gitlab", "projectId": "...", "version": "...", "eventId": "..."}`
- **actor**: `00000000-0000-0000-0000-000000000000` (system user)
