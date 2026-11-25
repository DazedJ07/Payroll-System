# Staff Timekeeping and Payroll Desktop Application

A modern, clean, modular **Java Swing** desktop application for Staff Timekeeping and Payroll calculation. Runs out-of-the-box in **Apache NetBeans IDE** (standard Java SE Ant or Maven project) on Windows, macOS, and Linux with zero external database dependencies.

---

## 📁 Package & Class Architecture

```
Payroll-System/
├── pom.xml                                   # Maven configuration (Java 8+, FlatLaf 3.5.4)
├── build.xml                                 # NetBeans Ant build configuration
├── lib/                                      # Included libraries (flatlaf-3.5.4.jar)
├── data/                                     # Persistent CSV data directory (auto-created)
│   ├── users.txt                             # Seeded employee accounts & hourly rates
│   ├── timelogs.txt                          # Shift clock records & calculated pay
│   ├── archived_users.txt                    # Archived employee accounts
│   └── archived_timelogs.txt                 # Archived shift records
├── paystubs/                                 # Pay stub backup text receipts (auto-created)
└── src/
    └── com/
        └── payroll/
            ├── app/
            │   └── Main.java                 # Entry point, FlatLaf theme boot, EDT launcher
            ├── model/
            │   ├── User.java                 # Model for employee credentials & role permissions
            │   └── TimeLog.java              # Model for shift time punch & earnings
            ├── dao/
            │   └── FileDao.java              # FileReader/FileWriter persistence and PDF export
            ├── service/
            │   └── PayrollService.java       # Math engine (OT 1.5x, Tax, Net Pay) & business rules
            ├── util/
            │   └── UIHelper.java             # Palette (#1E293B, #2563EB), fonts & Swing boilerplate reducers
            └── ui/
                ├── LoginFrame.java           # Modern centered login modal with visibility toggle
                ├── DashboardFrame.java       # Main frame with navy header, live clock, status badge & tabs
                ├── TimekeepingPanel.java     # Staff time tracking: Punch In/Out, history & pay stub
                ├── SupervisorPanel.java      # Team oversight: Resolve missed punches & manual shifts
                ├── AdminPanel.java           # Complete Employee CRUD, rate adjust & credential manager
                ├── AccountPanel.java         # Profile overview & self-service password changer
                └── PayStubDialog.java        # Pay slip preview and PDF generation/browser launch
```

---

## 🔑 Default Accounts (Role-Based Access)

The application automatically seeds `data/users.txt` on first launch with three test accounts:

| Role | Employee ID | Full Name | Username | Password | Base Hourly Rate |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Admin** | `EMP001` | Admin User | `admin` | `admin123` | **$500.00 / hr** |
| **Supervisor** | `EMP002` | Jane Supervisor | `super` | `super123` | **$350.00 / hr** |
| **Staff** | `EMP003` | John Staff | `staff` | `staff123` | **$200.00 / hr** |

---

## 🚀 Running in Apache NetBeans IDE

### Option A: Open as Maven Project (Recommended)
1. Open **Apache NetBeans IDE**.
2. Click **File** > **Open Project...**
3. Browse to and select `Payroll-System` (NetBeans detects `pom.xml`).
4. Press <kbd>F6</kbd> or click **Run Project**.

### Option B: Open as Ant Project
1. Open **Apache NetBeans IDE**.
2. Click **File** > **Open Project...** and select `Payroll-System` (NetBeans detects `build.xml`).
3. Resolve the Maven dependencies once by opening/building the project as Maven; Ant reads OpenPDF from the local Maven repository.
4. If needed, verify `lib/flatlaf-3.5.4.jar` is in **Project Properties > Libraries**.
5. Press <kbd>F6</kbd> to run.

---

## ⚙️ Core Technical Features

1. **Clean Custom Architecture & Compact Code:**
   - Modularized into `com.payroll.app`, `model`, `dao`, `service`, `ui`, and `util`.
   - Every file is under 150–250 lines with zero dead code or NetBeans GUI-Builder coordinate bloat.
   - Dedicated `UIHelper` factory reduces repetitive Swing setup to 1-line calls.

2. **Persistence Engine (`java.io.FileReader` & `java.io.FileWriter`):**
   - Uses `BufferedReader` wrapping `FileReader` and `BufferedWriter` wrapping `FileWriter`.
   - Plain CSV persistence in `data/users.txt` and `data/timelogs.txt`.

3. **Payroll Math & Statutory Deductions:**
   - **Regular Hours:** Up to `8.0` hours per shift.
   - **Overtime:** Elapsed hours beyond `8.0` in a single shift, paid at **`1.5x`** base rate.
   - **Withholding Tax:** 10.0% deduction.
   - **Benefits / Social Security:** 4.0% statutory deduction.
   - **Net Take-Home Pay:** `Gross Pay - Total Deductions`.

4. **Pay Stub PDF Export:**
   - Itemized official receipt summarizing shift breakdown, gross earnings, statutory deductions, and net payout.
   - Automatic `.txt` backup in `paystubs/`.
   - **Create PDF & Open in Browser** generates a `.pdf` in `paystubs/` and opens it in the computer's default web browser.
   - PDF generation uses OpenPDF; Maven resolves this dependency automatically.

5. **Modern Look and Feel:**
   - Powered by FlatLaf (FlatLightLaf) with soft light-gray cards, slate-navy headers, and royal blue accents.
   - Password fields feature an interactive reveal/hide toggle (`◉`).

6. **Archive and Delete:**
   - Admins can archive employee accounts or permanently delete them; an employee's shift history is retained.
   - Admins and supervisors can archive, restore, or permanently delete shift records.
   - Archived employee accounts are disabled and hidden from the active employee list. Archived shift records are hidden from active logs and payroll until restored.
   - Successful changes refresh the dashboard's staff, admin, and supervisor views.
