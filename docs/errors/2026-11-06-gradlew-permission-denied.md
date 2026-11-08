# gradlew: Permission denied (GitHub Actions)

- Date: 2026-11-08
- Where: GitHub Actions, `test` job, step `./gradlew build`

## Symptom

```
./gradlew: Permission denied
Error: Process completed with exit code 126.
```

## Cause

I committed these changes from Windows, which has no executable permission for files.
As a result, `gradlew` was stored in Git as `100644` (not executable), and the Linux runner refused to run it.

## Solution

```bash
git update-index --chmod=+x gradlew
git commit -m "build: make gradlew executable"
git push
```

## Verification

```bash
git ls-files -s gradlew   # → 100755
```

## Lessons learned

Git stores file permissions as well as file contents.