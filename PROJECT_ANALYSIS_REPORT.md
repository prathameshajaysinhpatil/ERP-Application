# Balaji Earthmovers App — Beginner-Friendly Project Report

**Reviewed:** 23 November 2026  
**Project type:** Native Android application  
**Main technologies:** Kotlin, Jetpack Compose, Room/SQLite, AndroidX, Coroutines

## 1. What this project does

This app helps an earthmoving business keep track of its vehicles, drivers, customer jobs, payments, expenses, and reports. It is designed to run on an Android phone or tablet.

One important terminology point: this repository does **not** contain a separate website frontend and a remote backend server. Its frontend is the Android interface. Its “backend” is the set of Kotlin data and business-logic layers running inside the app, with data stored locally in a SQLite database through Room. There are no REST API endpoints, server deployment files, or cloud synchronization implementation in the Android source tree.

The repository also contains `Balaji_Earthmovers_Angular_NestJS_Specification.html` and a PDF. These describe a possible web version using Angular and NestJS. They are a specification, not the frontend or server implementation in this project.

## 2. How the app is organized

Think of the app as four layers:

```text
Person using the app
        ↓
Compose screens and dialogs (frontend)
        ↓
BalajiViewModel (screen state, validation, actions)
        ↓
BalajiRepository → Room DAOs → SQLite database (local data/backend)
```

When a user enters a job, for example, the screen collects the values, the ViewModel checks them, the repository passes the operation to the database layer, and Room saves the job and related account updates. Room `Flow`s then send updated values back to the ViewModel and the UI redraws.

### Main folders and files

| Location | Beginner explanation |
| --- | --- |
| `app/src/main/java/.../MainActivity.kt` | Android’s entry point. It sets up the window, theme, ViewModel, and main Compose app. |
| `ui/navigation/AppNavigation.kt` | Chooses which screen is visible and provides the app’s navigation structure. |
| `ui/screens/` | The visible pages, forms, dialogs, and reusable screen UI. |
| `ui/viewmodel/BalajiViewModel.kt` | Holds screen data, validates forms, handles user actions, and calls the repository. |
| `data/repository/BalajiRepository.kt` | A middle layer between UI logic and database queries. It groups data operations and some business rules. |
| `data/local/dao/` | DAOs (“Data Access Objects”) contain SQL queries and database write operations. |
| `data/local/entity/` | Entity classes describe the rows/tables saved in the database. |
| `data/local/AppDatabase.kt` | Defines the Room database, tables, and first-install demo data. |
| `data/prefs/PreferencesManager.kt` | Stores app preferences such as theme, language, and PIN values. |
| `data/security/CryptoManager.kt` | Encrypts and decrypts Aadhaar values using Android Keystore AES-GCM. |
| `data/local/backup/` | JSON backup/restore and SQLite database file export helpers. |
| `data/local/export/ReportExporter.kt` | Creates PDF and CSV report files and provides a share action. |
| `ui/util/AppStrings.kt` | UI string translations and language helpers. |
| `app/src/main/AndroidManifest.xml` | Declares the Android activity, permissions, app settings, and file-sharing provider. |

## 3. Frontend: what users see

### Technology and startup

The user interface uses **Jetpack Compose**, Android’s Kotlin-based UI toolkit. Instead of defining every screen in XML, most screens are Kotlin functions that describe what should appear for the current state. Material 3 supplies common visual components and theming.

`MainActivity` creates the app UI and listens to the selected theme. The navigation layer manages splash/unlock state and the visible destination. The main destinations represented in navigation include Dashboard, Vehicle, Work, Customer, Driver, Ledger, and Report. Settings and detailed passbooks are also reachable through the app’s navigation/UI state.

### Main screens and their purpose

- **Dashboard:** Overview of billed work, collected income, expenses, receivables, and operating status. The ViewModel exposes these values as observable state.
- **Fleet/Vehicle:** Vehicle directory and service-hour tracking. A vehicle becomes service-due when accumulated work since its last service reaches 250 hours.
- **Work:** Job/work-order records, including customer, machine, driver, time or amount, and location. Work can be hourly or fixed-price through the form logic.
- **Customer directory and passbook:** Customer details, billed work, payments, and outstanding balance. Contact actions can use Android intents.
- **Driver directory and passbook:** Driver details, salary/advance transactions, and assigned vehicle information.
- **Ledger:** A combined view of financial records.
- **Reports:** Date-range filtering and report export actions.
- **Settings:** Theme, language, PIN settings, and data backup/restore/reset controls.
- **Dialogs:** Forms for creating work, expenses, payments, and scanning documents are kept separate from the larger screens.

### UI state and form handling

The ViewModel keeps form values and validation messages in Kotlin state objects. Compose observes those values, so when a value changes or a database query emits new data, the relevant UI updates. Examples of validation include positive payment/expense amounts, a ten-digit mobile number format, and GSTIN format.

User-visible success or failure messages are emitted as UI events and shown by the screen layer. Long-running database and backup actions use Kotlin coroutines so the UI does not need to wait synchronously on the main thread.

## 4. Backend/data layer: how records are saved

### Local database, not a remote server

Room is a library that maps Kotlin data classes and DAO methods to SQLite tables and SQL queries. The database file is named `balaji_earthmovers.db` and is stored in the app’s private internal storage. The app can therefore operate without a network connection, but its records are local to that installation unless the user exports and transfers a backup.

### Main data tables

| Table/entity | What one row represents | Important links or values |
| --- | --- | --- |
| `vehicles` | A machine or vehicle owned/used by the business | Registration, name/model, worked hours, last service hours, assigned driver, fuel total |
| `drivers` | A driver/employee | Contact details, salary, assigned vehicle, leave count, paid salary |
| `customers` | A customer | Contact/GST details, total billed, total paid; pending balance is billed minus paid, clamped at zero |
| `works` | A job or work order | Customer, vehicle, driver, date, hours, rate, total amount, location |
| `incomes` | A payment/receipt associated with a customer and optionally a job | Paid amount, pending amount, payment mode, reference number |
| `expenses` | A business expense | Type, amount, date, optional linked driver or vehicle |
| `driver_transactions` | A salary/advance/bonus transaction for a driver | Driver, amount, type, date, optional source expense |

The IDs connect related records. For example, a work row stores IDs for its customer, driver, and vehicle. Room relations can then load a work item together with readable related details.

### DAOs and repositories in plain language

A **DAO** is the database-facing object: it knows how to select, insert, update, or mark rows deleted. For example, `WorkDao` reads work entries and calculates total billed work. The **repository** gives the ViewModel a cleaner set of operations such as `createWorkAtomic` or `recordPaymentReceipt` without making the UI know SQL.

Many DAO read methods return Kotlin `Flow`. A Flow is a stream of values: if a watched table changes, Room can emit a fresh result and the screen updates without manually reloading every list.

### Important business flows

1. **Create work:** The ViewModel validates the form, creates a `WorkEntity`, and calls `createWorkAtomic`. A single Room transaction inserts the work, adds hours to the vehicle, adds the billed amount to the customer, and, if an initial payment was entered, creates an income record and adds it to the customer’s paid total.
2. **Record a customer receipt:** An income row is inserted and the customer’s paid total is increased in a database transaction.
3. **Record an expense:** The expense row is inserted. For a salary expense, a driver transaction is also created and the driver’s given-salary total is increased. For fuel, the vehicle’s fuel-expense total is increased.
4. **Delete work or expense:** The app generally uses a soft delete (sets `deletedAt`) so normal lists and totals ignore the record. The work deletion transaction also adjusts hours and customer totals and soft-deletes linked income rows. Expense deletion reverses linked salary or fuel totals.
5. **Service reminder:** The vehicle entity calculates `workedHours - lastServiceHours`; 250 hours or more means service is due. Marking service records the current hours as the new service baseline.
6. **Reporting:** The report UI prepares rows and summary values; `ReportExporter` writes CSV/PDF files to the app’s Balaji Earthmovers storage directory and can share them through Android’s file provider.

`@Transaction` means a group of related database changes succeeds or fails together. This is valuable for money and ledger updates: the app should not save the work but fail halfway through updating the customer balance.

## 5. Preferences, security, and file handling

- **Theme and language:** The app supports system/light/dark theme selection and English/Marathi/Hindi language selection. Preferences are stored with Android `SharedPreferences`.
- **PINs:** App and admin PINs are also stored as plain strings in `SharedPreferences`, and the verification methods compare the entered value with the stored value. This is an app-level gate, not server authentication or a robust device security boundary.
- **Aadhaar:** `CryptoManager` uses AES/GCM with a key held in Android Keystore for Aadhaar values before they are passed to Room. However, its catch path returns the original plaintext if encryption fails; this should be reviewed before storing real identity numbers. Backup/export behavior should also be reviewed for sensitive data.
- **File access:** The manifest declares camera and older Android external-storage permissions. A `FileProvider` is configured for securely sharing app-generated files with other apps.
- **Backup:** JSON backup and restore are implemented locally through Android storage/file-picker flows. A backup is only useful if it is copied somewhere safe and restore is tested on the intended device/app version.

## 6. Build and test setup

The project uses Gradle Kotlin DSL. The app module declares Android, Compose, Room/KSP, AndroidX Security Crypto, coroutines, and Gson dependencies. The manifest identifies `MainActivity` as the launcher activity.

The source tree contains a small unit-test template and an Android instrumentation-test template. Their presence does not by itself prove that the accounting workflows have comprehensive automated coverage. This report is a code review; no build or tests were run for it.

## 7. Important implementation notes to verify

These points are based on the checked-in source and are useful before relying on the app for live business records:

1. **Demo records are seeded on first database creation.** `AppDatabase` inserts sample vehicles, drivers, customers, a work item, an income, and an expense. Confirm whether this behavior is intended for real installs and how demo data should be distinguished from business data.
2. **Database version is 1 and migration fallback is destructive.** `fallbackToDestructiveMigration()` allows Room to discard and recreate the database if a future schema version has no migration. That can erase local records during an upgrade. A production data plan should use explicit migrations and tested backups.
3. **PIN documentation and implementation disagree.** README and the Angular/NestJS specification state the app unlock PIN default is `1234`; `PreferencesManager.kt` sets it to `2838`. The admin PIN default is `2476` in the Kotlin source. Make docs and actual defaults consistent, and avoid relying on a shared default PIN for protection.
4. **The PINs are not hashed/encrypted by `PreferencesManager`.** Anyone able to inspect app preferences on a compromised/rooted device may be able to recover them. These PIN checks should be viewed as a convenience barrier, not strong authentication.
5. **Aadhaar encryption has a plaintext fallback.** If crypto setup or encryption fails, `encrypt()` returns the input value. The code comment frames this as helpful for preview/test environments, but production behavior should fail closed or otherwise avoid persisting plaintext.
6. **Financial totals are stored as mutable summary fields as well as transaction records.** Customer totals and driver/vehicle totals are incremented and decremented by DAO operations. Any workflow that edits an existing transaction must keep these summaries consistent. Review update paths and test create/edit/delete/restore combinations carefully.
7. **Local data has no automatic multi-device sync.** Losing/uninstalling the app or changing devices can make records unavailable if no backup exists. The project has export/restore tools but no server-side copy or synchronization protocol.
8. **The product specification is not the implementation.** The HTML/PDF may describe features, API endpoints, PostgreSQL, or web architecture that are not present in this Android source. Treat source code as the authority for what this app currently does.

These are review observations, not a claim that a particular failure has occurred. A business should reconcile exported data and thoroughly validate accounting workflows before using the app as its only financial record system.

## 8. Suggested beginner reading order

To understand the code without jumping around, read it in this order:

1. `MainActivity.kt` — app startup.
2. `ui/navigation/AppNavigation.kt` — how screens are selected.
3. A screen such as `DashboardScreen.kt` or `WorkEntryDialog.kt` — how UI is composed.
4. `ui/viewmodel/BalajiViewModel.kt` — state, validation, and user actions.
5. `data/repository/BalajiRepository.kt` — app-level data operations.
6. An entity such as `WorkEntity.kt` — what is stored.
7. `WorkDao.kt` — SQL reads and multi-step transactions.
8. `AppDatabase.kt` — database setup, schema version, and initial sample data.

## 9. Glossary

- **Frontend:** The part a person sees and interacts with. Here, that is the Android Compose UI.
- **Backend:** Usually a server that receives requests and stores shared data. This project has no such server; its local Kotlin data layer fills some similar responsibilities on-device.
- **Entity:** A Kotlin data class that represents a database table row.
- **DAO:** A set of database commands and queries.
- **Repository:** A layer that offers the rest of the app a convenient interface to data operations.
- **ViewModel:** A screen-oriented class that holds UI state and coordinates actions while surviving normal screen recreation.
- **Flow:** A stream of values that can update as the underlying data changes.
- **Transaction:** A database unit of work in which related changes are applied together.
- **Soft delete:** Hiding a row by recording a deletion timestamp rather than immediately removing the row.
- **SQLite/Room:** SQLite is the local database engine; Room is Android’s Kotlin-friendly layer over it.

