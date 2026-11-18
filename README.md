# Staff Timekeeping and Payroll Desktop Application

A clean, modular, beginner-friendly **Java Swing** desktop application for Staff Timekeeping and Payroll calculation. Runs out-of-the-box in **Apache NetBeans IDE** (standard Java SE Ant or Maven project) on macOS and Windows with zero external database or internet dependencies.

---

## 📁 Package & Class Architecture

```
Payroll/
├── pom.xml                               # Maven project configuration (Java 8+)
├── build.xml                             # Ant build configuration
├── data/                                 # Persistent CSV data directory (auto-created)
│   ├── users.txt                         # Seeded employee accounts & hourly rates
│   └── timelogs.txt                      # Shift clock records & calculated pay
├── paystubs/                             # Pay stub backup text receipts (auto-created)
└── src/
    └── payroll/                          # Main application package (5 core classes)
        ├── Main.java                     # Application entry point & L&F setup
        ├── LoginFrame.java               # Authentication window & role-based routing
        ├── DashboardFrame.java           # Staff, Supervisor & Admin tabbed views
        ├── FileHelper.java               # FileReader / FileWriter persistence engine
        └── PayrollCalculator.java        # Dedicated mathematical calculation engine
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

### Option A: Open as an Existing Project (Easiest)
1. Open **Apache NetBeans IDE**.
2. Click **File** > **Open Project...**
3. Browse to and select the `Payroll` directory (NetBeans detects `pom.xml` or `build.xml`).
4. Right-click the project in the Projects window and select **Run** (or press <kbd>F6</kbd>).

### Option B: Create a New Standard Java Application (Ant)
1. In NetBeans, click **File** > **New Project...**
2. Choose **Java with Ant** > **Java Application**, click **Next**.
3. Name the project `StaffPayrollSystem`.
4. Ensure the package name is `payroll`.
5. Copy the 5 `.java` files from `src/payroll/` into the `payroll` package inside your NetBeans project:
   - `Main.java`
   - `LoginFrame.java`
   - `DashboardFrame.java`
   - `FileHelper.java`
   - `PayrollCalculator.java`
6. Right-click `Main.java` and select **Run File** (or press <kbd>Shift</kbd> + <kbd>F6</kbd>).

---

## ⚙️ Core Technical Features

1. **Explicit `java.io.FileReader` and `java.io.FileWriter`:**
   - Reads lines using `BufferedReader(new FileReader(...))`
   - Writes/Appends records using `BufferedWriter(new FileWriter(..., append))`
   - No external SQL or ORM dependencies; all data is human-readable `.txt` in `data/`.

2. **Dedicated User-Defined Methods:**
   - `authenticateUser(username, password)` in [`LoginFrame.java`](file:///Users/honestomedina/Desktop/Payroll/src/payroll/LoginFrame.java)
   - `clockIn()` and `clockOut()` in [`DashboardFrame.java`](file:///Users/honestomedina/Desktop/Payroll/src/payroll/DashboardFrame.java)
   - `calculateRegularHours(totalHours)` in [`PayrollCalculator.java`](file:///Users/honestomedina/Desktop/Payroll/src/payroll/PayrollCalculator.java)
   - `calculateOvertimeHours(totalHours)` in [`PayrollCalculator.java`](file:///Users/honestomedina/Desktop/Payroll/src/payroll/PayrollCalculator.java)
   - `calculateGrossPay(regHours, otHours, rate)` in [`PayrollCalculator.java`](file:///Users/honestomedina/Desktop/Payroll/src/payroll/PayrollCalculator.java)
   - `loadLogsFromFile()`, `saveLogsToFile()`, `appendLogToFile()` in [`FileHelper.java`](file:///Users/honestomedina/Desktop/Payroll/src/payroll/FileHelper.java)
   - `exportPayStub(employeeId)` in [`DashboardFrame.java`](file:///Users/honestomedina/Desktop/Payroll/src/payroll/DashboardFrame.java) & [`FileHelper.java`](file:///Users/honestomedina/Desktop/Payroll/src/payroll/FileHelper.java)

3. **Payroll Math Rules:**
   - **Regular Hours:** Up to `8.0` hours per shift.
   - **Overtime Hours:** Elapsed hours beyond `8.0` in a single shift, paid at **`1.5x`** the base hourly rate.
   - **Total Pay:** `(regularHours * hourlyRate) + (overtimeHours * hourlyRate * 1.5)`.

4. **One-Click Pay Stub Generation & PDF Print:**
   - Generates an official, itemized receipt summarizing completed shifts, total hours, and payouts.
   - Backs up a `.txt` receipt in the `paystubs/` directory.
   - Uses Java standard `JTextArea.print()` to allow instant printing or **"Save to PDF"** via the native operating system print dialog without third-party PDF libraries!
