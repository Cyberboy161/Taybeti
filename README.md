# Taybeti — Paranoid-Grade Encrypted Note App

**Zero‑trust, offline‑only, open‑source secure notes for Android.**

Taybeti keeps your secrets truly private.  
No plaintext ever touches storage. No password hash is kept on the device.  
Even the master login is verified by *decrypting* a known test string — there is nothing to crack.

## Features

- **Zero‑trust master auth** — no password hash, no Keystore, no persistent secrets
- **Per‑note encryption** — every note has its own passphrase and independent key
- **Covert sharing** — disguise encrypted messages as a set of normal‑looking YouTube/Instagram links
- **100% offline** — no internet permission, no cloud, no telemetry
- **Open source (MIT)** — auditable by anyone, anytime

## Security Architecture

| Layer | Implementation |
|-------|---------------|
| Key Derivation | Argon2id (6 iterations, 64 MB memory, 4 parallelism) |
| Encryption | AES‑256‑GCM (random 32‑byte salt, random 12‑byte IV, 128‑bit auth tag) |
| Master Auth | Zero‑trust canary: encrypt a fixed string, verify by successful decryption |
| Per‑Note Keys | Each note has its own passphrase, derived separately |
| Password Storage | `char[]` only — zeroed via `Arrays.fill('\0')` after every use |
| Keystore | **NOT used** — no Android Keystore, no hardware‑backed keys |
| Network | **No internet permission** in manifest |
| Keyboard | Custom in‑app keyboard — no system keyboard, no suggestions, no learning |
| Screenshots | `FLAG_SECURE` on every Activity |
| Backup | `android:allowBackup="false"`, `android:fullBackupOnly="false"` |
| Memory | `onTrimMemory` / `onLowMemory` → immediate clear of decrypted content |
| Logging | ProGuard strips all `Log.d/v/i/w/e` in release builds |
| Decoy Vault | Optional decoy password unlocks a separate set of notes (plausible deniability) |

## Threat Model

### Protects against:
- Physical device seizure (all data encrypted at rest)
- Malicious apps on device (no IPC surface, FLAG_SECURE)
- Keyboard logging / keyloggers (custom keyboard)
- Screenshot / screen recording capture (FLAG_SECURE)
- Android backup extraction (backup disabled)
- Cloud sync leaks (no internet permission)
- Google / OS‑level key extraction (no Keystore)
- Brute force (Argon2id memory‑hard KDF, 5‑attempt lockout)

### Does NOT protect against:
- Compromised OS kernel / root‑level malware
- Hardware keyloggers
- RAM forensic capture while a note is decrypted
- Side‑channel attacks on AES (software implementation)

## Build

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34

### Build APK
```bash
cd Taybeti
./gradlew assembleRelease

The release APK will be at:
app/build/outputs/apk/release/app-release.apk
Debug keystore

A debug keystore is included at app/taybeti.keystore:

    Alias: taybeti

    Password: taybeti (change for production)

For production, generate your own:
bash

keytool -genkey -v -keystore app/taybeti.keystore \
  -alias taybeti -keyalg RSA -keysize 4096 \
  -validity 10000 -storepass YOUR_PASSWORD -keypass YOUR_PASSWORD

Project Structure
text

app/src/main/java/com/taybeti/app/
├── MainActivity.kt          # Entry point, FLAG_SECURE
├── TaybetiApp.kt            # Application class, memory callbacks
├── di/
│   └── AppContainer.kt      # Manual DI
├── data/
│   ├── entities/
│   │   ├── LoginInfo.kt     # Room entity: salts, IVs, ciphertexts
│   │   └── NoteEntity.kt    # Room entity: per-note crypto params
│   ├── dao/
│   │   ├── LoginDao.kt      # Login info queries
│   │   └── NoteDao.kt       # Note CRUD queries
│   ├── database/
│   │   └── AppDatabase.kt   # Room database singleton
│   └── repository/
│       └── NoteRepository.kt # Crypto + DB orchestration
├── security/
│   ├── CryptoUtils.kt       # Argon2id + AES-256-GCM
│   └── SecureMemory.kt      # char[]/byte[] zeroing
├── ui/
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Type.kt
│   │   └── Theme.kt
│   ├── components/
│   │   ├── ExportImportScreen.kt
│   │   └── TrashScreen.kt
│   └── navigation/
│       └── NavGraph.kt      # Navigation + drawer
└── util/
    ├── Constants.kt
    └── Extensions.kt

Dependencies
Library	Purpose
Jetpack Compose + Material 3	UI
Navigation Compose	Screen navigation
Room + KSP	Encrypted metadata storage
argon2kt 1.6.0	Argon2id key derivation (Android JNI)
Kotlin Coroutines	Async crypto operations
License

MIT License — see the About screen for full text.
Security Audit

No third‑party security audit has been performed. Use at your own risk.
Review the code — it’s open for anyone to inspect, improve, and trust.
