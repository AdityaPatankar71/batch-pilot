# Releasing batch-pilot to Maven Central

batch-pilot publishes through the **Central Portal** (`central.sonatype.com`),
not the legacy OSSRH/Nexus path. The Maven wiring lives in the `release` profile
of the root `pom.xml`; this document covers the one-time setup and the per-release
steps that require your credentials.

## What gets published

| Module                  | Published? | Notes                                            |
|-------------------------|------------|--------------------------------------------------|
| `batch-pilot-parent`    | ✅ (pom)   | BOM / parent metadata.                           |
| `batch-pilot-ui`        | ✅         | Angular console packaged as classpath resources. |
| `batch-pilot-starter`   | ✅         | The dependency consumers actually add.           |
| `batch-pilot-sample-app`| ❌         | Demo only — `maven.deploy.skip=true`.            |

The starter depends on `batch-pilot-ui`, so the UI artifact **must** ship too,
otherwise consumers can't resolve the starter.

## One-time setup

### 1. Verify the namespace

The groupId is `io.github.batchpilot`. On `central.sonatype.com`, register and
verify the **`io.github.batchpilot`** namespace via GitHub. This means the
GitHub account/organization named **`batchpilot`** must exist and be yours — the
Portal has you create a public repo under it named after a generated code. So
before releasing:

1. Create the GitHub organization (or account) **`batchpilot`**.
2. Use it to verify the `io.github.batchpilot` namespace on the Portal.

The code repo itself can stay anywhere (the `scm`/`url` in the pom currently
point at `AdityaPatankar71/batch-pilot`); only the *namespace* account must be
`batchpilot`. If you move the repo under the org, update those pom URLs too.
Until the namespace is verified, uploads are rejected.

### 2. Generate a Portal token

Central Portal → *Account* → *Generate User Token*. Add it to
`~/.m2/settings.xml` under the server id the profile expects (`central`):

```xml
<settings>
  <servers>
    <server>
      <id>central</id>
      <username>TOKEN_USERNAME</username>
      <password>TOKEN_PASSWORD</password>
    </server>
  </servers>
</settings>
```

### 3. GPG signing key

Central requires every artifact be GPG-signed.

```bash
gpg --gen-key                       # if you don't have one
gpg --list-keys                     # note the key id
gpg --keyserver keys.openpgp.org --send-keys <KEY_ID>   # publish the public key
```

If GPG prompts for a passphrase non-interactively, pass it on the release
command: `-Dgpg.passphrase=...` (or use a gpg-agent).

## Cutting a release

1. **Version is already set.** `main` carries `0.1.0` and is tagged `v0.1.0`
   (commit `release: 0.1.0`) — go straight to the deploy below. To cut a *later*
   release, bump first:

   ```bash
   mvn versions:set -DnewVersion=X.Y.Z -DgenerateBackupPoms=false
   ```

2. **Build + sign + upload to the Portal staging area:**

   ```bash
   mvn -Prelease clean deploy
   ```

   `autoPublish` is `false`, so this uploads a *deployment* you can inspect at
   `central.sonatype.com` → *Deployments* before it goes live.

3. **Verify the staged deployment** (correct artifacts, javadoc/sources/signatures
   present), then click **Publish** in the Portal — or set `autoPublish` to `true`
   in the profile to skip the manual gate on future releases.

4. **After a successful publish, open the next dev cycle:**

   ```bash
   mvn versions:set -DnewVersion=0.2.0-SNAPSHOT -DgenerateBackupPoms=false
   git commit -am "chore: start 0.2.0-SNAPSHOT"
   git push
   ```

Central sync to `repo1.maven.org` typically lands within ~15–30 minutes of publish.

## Sanity check before you publish

- `mvn -Prelease clean install` succeeds locally (exercises source + javadoc jars
  and GPG signing without uploading).
- `batch-pilot-ui` produces a (possibly empty) `-javadoc.jar` — it has no Java
  sources, so `failOnError=false` in the profile keeps the reactor green.
