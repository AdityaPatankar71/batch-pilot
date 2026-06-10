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

The groupId is `io.github.adityapatankar71`. On `central.sonatype.com`, register
and verify the **`io.github.adityapatankar71`** namespace (GitHub-based
verification: the portal has you create a public repo named after a generated
code). Until verified, uploads are rejected.

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

1. **Drop the SNAPSHOT.** The repo carries `0.1.0-SNAPSHOT`; set the release
   version across all modules:

   ```bash
   mvn versions:set -DnewVersion=0.1.0 -DgenerateBackupPoms=false
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

4. **Tag and bump to the next snapshot:**

   ```bash
   git commit -am "release: 0.1.0"
   git tag v0.1.0
   mvn versions:set -DnewVersion=0.2.0-SNAPSHOT -DgenerateBackupPoms=false
   git commit -am "chore: start 0.2.0-SNAPSHOT"
   git push --follow-tags
   ```

Central sync to `repo1.maven.org` typically lands within ~15–30 minutes of publish.

## Sanity check before you publish

- `mvn -Prelease clean install` succeeds locally (exercises source + javadoc jars
  and GPG signing without uploading).
- `batch-pilot-ui` produces a (possibly empty) `-javadoc.jar` — it has no Java
  sources, so `failOnError=false` in the profile keeps the reactor green.
