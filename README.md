# 🚜 Balaji Earthmovers Kodoli - Fleet & Accounts ERP

[![Platform](https://img.shields.io/badge/Platform-Android%20%28Jetpack%20Compose%29-3DDC84?logo=android&logoColor=white)](#)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%202.0-7F52FF?logo=kotlin&logoColor=white)](#)
[![Database](https://img.shields.io/badge/Database-Room%20ORM%20%2B%20SQLite-4285F4?logo=sqlite&logoColor=white)](#)
[![Security](https://img.shields.io/badge/Security-AES--256%20Encryption%20%2B%20Dual%20PIN-059669?logo=shield&logoColor=white)](#)
[![Design](https://img.shields.io/badge/UI%2FUX-Material%203%20%2B%20WhatsApp%20Style-D97706?logo=google&logoColor=white)](#)

> **Enterprise Fleet, Billing, and Financial Accounts Management System**  
> **Business:** Balaji Earthmovers Kodoli  
> **Owner:** Ajaysinh Jaysing Patil  

---

## 📖 Overview

**Balaji Earthmovers ERP** is a dedicated enterprise mobile application engineered for earthmoving machinery contracting (JCBs, Poclain excavators, Tractors, Tippers, Rollers, and custom machinery). It digitizes field operations, hourly and fixed-price contracts, fuel costs, 250-hour routine maintenance triggers, driver salary payouts, customer receivables ledgers, and automated PDF/CSV report generation.

---

## ✨ Key Features & Capabilities

### 1. 📊 Executive Dashboard & Financial Pillars
- **Net Earnings Hero Card:** Calculated dynamically as `Total Incomes Collected - Total Expenses`.
- **4 Operational Pillars:** Gross Billed Turnover, Total Cash Received, Total Fleet Spend, and Pending Receivables from Clients.
- **250-Hour Maintenance Alert:** Automatically monitors machine work hours; alerts when `(workedHours - lastServiceHours) >= 250.0 hrs` with an accessible "Reset 250h Service" button.
- **Quick Action FAB:** Instant creation of new work orders and fuel/operating expenses.

### 2. 🚜 Fleet & Work Order Management
- **Machinery Catalog:** JCB 3DX, Tractor, Trolley, Dumper, Poclain Excavator, Mini Excavator, Roller, Crane, and custom `"Other"` vehicle name input.
- **Dual Pricing Engine:** Supports both **Hourly Rate Work** (`hours * rate`) and **Fixed Amount Work** (lump-sum contract).
- **Auto-Driver Linkage:** Automatically pre-selects the driver assigned to the selected vehicle.
- **Atomic Income Booking:** When an initial payment is recorded upon work creation, it is atomically booked into the `Incomes` ledger and credited to the customer.
- **Atomic Deletion Rollback:** Deleting work orders rolls back customer billings, customer paid totals, vehicle worked hours, and soft-deletes linked incomes.

### 3. 👥 Customer & Driver Directory + Passbooks
- **Driver Passbook & Ledger:** Tracks monthly base salary, daily advances, and bonuses. Displays total earned to date, given salary, and pending salary balance with timeline transaction bubbles.
- **Customer Passbook & Receivables:** Replaces complex aging buckets with a clean chronological ledger:
  - Billed work orders (+ Debit in Gold).
  - Payment receipts (- Credit in Emerald Green).
  - Direct "Receive Payment" modal with blank amount initialization.
  - Direct call integration.

### 4. 📈 Reports & Data Export Module
- **Timeframe Filters:** Quick filter chips for *All Time*, *This Month*, *This Week*, and *Today*.
- **Summary Cards:** Total Work Billed, Total Collected Income, Total Fleet Expenses, and Net Operating Balance.
- **One-Tap Export:** Generates formatted **PDF Reports** and **CSV Spreadsheets** saved directly to `Documents/BalajiEarthmovers/reports/`.

### 5. ⚙️ Structured Settings & Directory Architecture
- **Themes & Languages:** Theme mode (System, Light, Dark) and trilingual localization (**English**, **Marathi (मराठी)**, and **Hindi (हिंदी)**).
- **Data & Storage:** Dedicated local backup directory (`Documents/BalajiEarthmovers/`), one-tap JSON backup, JSON restoration from file picker, and Admin PIN protected Factory Reset.
- **Dual PIN Security:** 
  - 4-digit **App Unlock PIN** (`1234`) with smooth open-lock success animation.
  - 4-digit **Admin PIN** (`2476`) required for editing or deleting records.

### 6. 🎨 Branding & Executive Polish
- **Microsoft 365-Style Motion Splash Screen:** Paced 3.8s multi-phase transition from the Tilak emblem with ambient golden radial glow into the official high-resolution logo.
- **Unified Global Top Bar:** Compact `BalajiTopBar` glued to the screen top across Dashboard, Vehicle, Work, Customer, Driver, and Report screens.
- **Seamless Bottom Navigation:** Zero white gap above bottom navigation bar with refined outline borders.

---

## 🛠️ Architecture & Tech Stack

```
com.example.balaji_earthmovers/
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt            # Room DB v6 with auto-migrations
│   │   ├── dao/                      # Vehicle, Driver, Work, Income, Expense, Customer DAOs
│   │   ├── entity/                   # Type-safe Room entities & relational joins
│   │   ├── backup/                   # Local JSON backup & restore engine
│   │   └── export/                   # Native PDF (PdfDocument) & CSV export engine
│   ├── repository/                   # BalajiRepository coordinating transactional DB queries
│   ├── security/                     # AES-256 CryptoManager for Aadhaar encryption
│   └── prefs/                        # DataStore Preferences for Theme, Language & PINs
├── ui/
│   ├── navigation/                   # AppNavigation, NavItem, unified BalajiTopBar
│   ├── screens/                      # Dashboard, FleetWork, Directory, Passbooks, Reports, Settings
│   ├── theme/                        # Material 3 Color palette, Theme, Typography
│   ├── util/                         # Trilingual AppStrings localization (EN, MR, HI)
│   └── viewmodel/                    # BalajiViewModel exposing state flows & transactional actions
```

---

## 🚀 Building the Project

### Prerequisites
- Android Studio Ladybug / Meerkat (or newer)
- JDK 17+ (e.g. Android Studio bundled JBR)
- Android SDK 34+

### Build Debug APK
```bash
# Clone the repository
git clone https://github.com/<your-username>/Balaji_Earthmoversapp.git
cd Balaji_Earthmoversapp

# Build debug APK
./gradlew assembleDebug --no-daemon
```
The compiled APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📄 Full-Stack Angular + NestJS Reproduction Spec
For teams looking to replicate this system on the web:
- [Balaji_Earthmovers_Angular_NestJS_Specification.pdf](./Balaji_Earthmovers_Angular_NestJS_Specification.pdf) contains the complete PostgreSQL database schema, NestJS REST API OpenAPI specification, and Angular 18+ component architecture.

---

## 📜 License & Ownership
Copyright © 2026 **Balaji Earthmovers Kodoli**.  
All rights reserved. Owner: **Ajaysinh Jaysing Patil**.
