# DE LA SALLE UNIVERSITY – DASMARIÑAS
## College of Information and Computer Studies (CICS)
### S-ITCS112 Fundamentals of Programming — Finals Summative Assessment
---
# PROJECT SCOPE AND FEATURES DOCUMENT

**Project Title:** Staff Timekeeping and Payroll Desktop Application  
**Target Environment:** Java SE (JDK 8+) / Apache NetBeans IDE / Maven / Ant  
**Data Storage Model:** File Handling (Flat-File CSV / TXT Persistence)

---

## A. Project Scope

### 1. Purpose of the Application
In many small-to-medium enterprises and institutions, attendance tracking and payroll processing are conducted manually using paper logs or basic spreadsheets. This traditional approach is highly prone to human computation errors, missing punch records, inaccurate overtime tracking, buddy-punching, and time-consuming tax calculations.

The **Staff Timekeeping and Payroll System** solves these challenges by providing an automated, standalone Java desktop application that streamlines:
- Real-time employee attendance tracking (Clock-In and Clock-Out).
- Automatic mathematical computation of regular hours ($\le 8.0\text{ hours}$), overtime hours ($> 8.0\text{ hours}$ at $1.5\times$ base rate), withholding taxes ($10\%$), statutory benefits ($4\%$), and net take-home pay.
- Role-based management for Staff, Supervisors, and Administrators.
- Persistent file-based data storage without requiring third-party database servers.
- Itemized official pay stub generation in both plain text and print-ready PDF formats.

### 2. Target Users
The system is built with role-based access control (RBAC) to cater to three distinct user groups:
1. **Staff / Regular Employees:**
   - Punch in at shift start and punch out upon shift completion.
   - View their personal shift history and accrued earnings.
   - Generate and review their personal official pay slips.
   - Manage personal login credentials and passwords.
2. **Supervisors:**
   - Monitor team-wide attendance records across all personnel.
   - Resolve "missed punch" exceptions (e.g., if an employee forgets to clock out).
   - Manually insert retroactive shifts for off-site or approved work.
   - Archive and restore shift logs to maintain an organized active ledger.
3. **Administrators / HR / Payroll Managers:**
   - Execute full employee account management (Add, Edit, Archive, Restore, Delete).
   - Set and adjust employee hourly pay rates.
   - Reset employee credentials and manage system permissions.
   - Generate official pay stubs and PDF receipts for any employee.

### 3. Main Functions
- **Authentication & Security:** Secure login system checking credentials against persistent storage with password show/hide toggle.
- **Automated Time Punching:** 1-click Clock-In and Clock-Out mechanism with live status indicators and duplicate punch prevention.
- **Automated Payroll Engine:** Instant parsing of shift duration, automatic separation of regular and overtime hours, and deduction computation.
- **Supervisor Oversight:** Resolution modal for open/pending shifts and manual shift entry with timestamp validation.
- **Comprehensive Employee Administration:** Automated employee ID sequencing (`EMP001`, `EMP002`, ...), wage adjustments, and credential updates.
- **Two-Tiered Data Safety (Archiving vs Deletion):** Deactivating records to archive files to preserve historical payroll audits while keeping active tables clean.
- **Digital Pay Slip Export:** Automatic compilation of itemized receipts, text backup, and PDF export opening directly in the default web browser.

### 4. Data Management
- **Information Collected:**
  - Employee records: Employee ID, Full Name, Username, Password, Role, Hourly Rate.
  - Shift records: Employee ID, Employee Name, Shift Date, Clock-In Timestamp, Clock-Out Timestamp, Regular Hours, Overtime Hours, Total Shift Pay.
- **Information Processed:**
  - Elapsed shift durations parsed from ISO timestamps (`yyyy-MM-dd HH:mm:ss`).
  - Regular vs. Overtime hours split.
  - Gross earnings, 10% withholding tax, 4% social security/benefits deduction, net take-home pay.
- **Information Stored:**
  - Active employees stored in `data/users.txt`.
  - Active time records stored in `data/timelogs.txt`.
  - Deactivated employees stored in `data/archived_users.txt`.
  - Deactivated shifts stored in `data/archived_timelogs.txt`.
  - Itemized text receipts and PDFs stored in `paystubs/`.
- **Information Retrieved:**
  - Text records parsed via `BufferedReader` into domain objects (`User`, `TimeLog`) and dynamically rendered into Swing `JTable` components.

### 5. Limitations
To keep the application robust, modular, and strictly aligned with the course objectives:
1. **Local Desktop Architecture:** Operates as a standalone desktop app; data is saved to local storage rather than a centralized cloud or networked database.
2. **Fixed Deduction Model:** Statutory deductions are implemented as fixed standard percentages ($10\%$ withholding tax, $4\%$ benefits) rather than complex graduated tax matrices (e.g., TRAIN law brackets).
3. **No External Biometric Hardware:** Punching relies on authenticated user software actions rather than physical fingerprint or RFID scanners.
4. **Continuous Shift Tracking:** Shift duration calculations are continuous between clock-in and clock-out (automated meal break deductions are not subtracted unless separate punches are made).

---

## B. Project Features and Functionalities

| No. | Feature / Functionality | Description |
| :---: | :--- | :--- |
| **1** | **Data Entry** | Allows staff to punch in/out, administrators to register new employees with auto-incrementing IDs, and supervisors to input manual shifts. |
| **2** | **View Records** | Displays personalized shift history for staff, complete team-wide timecards for supervisors, and employee master directories for administrators in styled JTables. |
| **3** | **Search Records** | Automatically looks up records by Employee ID (`findUserById`), username validation during login (`authenticate`), and locates active pending punches (`isPending`). |
| **4** | **Update Records** | Enables profile updates, password changes, hourly rate adjustments, and resolving missed clock-outs by updating pending records in-place. |
| **5** | **Delete / Archive Records** | Features a two-tiered system: "Archive" (soft-delete to separate archive files preserving audit trails) and "Permanent Delete" for records no longer needed. |
| **6** | **File Handling** | Reads and writes comma-separated text files (`users.txt`, `timelogs.txt`, `archived_*.txt`) using `BufferedReader` and `BufferedWriter`, and exports print-ready PDFs. |
| **7** | **Input Validation** | Validates non-empty fields, prevents CSV delimiter injection (`isValidCsv`), checks positive numeric rates, prevents duplicate IDs/usernames, and validates timestamp syntax. |
| **8** | **Computation / Reports** | Calculates shift hours, overtime multipliers ($1.5\times$), gross pay, tax deductions, and compiles complete itemized official pay slips. |

---

## C. User-Defined Methods Implementation

The project is structured into modular layers (`model`, `dao`, `service`, `ui`, `util`). Below are the primary user-defined methods powering the system:

| Method Name | Purpose / Functionality | Expected Parameters | Return Type |
| :--- | :--- | :--- | :---: |
| `main()` | Program entry point; sets FlatLaf theme, initializes file storage, and launches `LoginFrame` on the EDT. | `String[] args` | `void` |
| `PayrollService.calcTotalHours()` | Computes elapsed time in fractional hours between clock-in and clock-out timestamps. | `String clockInStr, String clockOutStr` | `double` |
| `PayrollService.calcRegularHours()` | Returns regular shift hours, capped at a maximum of 8.0 hours. | `double totalHours` | `double` |
| `PayrollService.calcOvertimeHours()` | Computes overtime hours worked beyond the standard 8.0-hour threshold. | `double totalHours` | `double` |
| `PayrollService.calcGrossPay()` | Computes gross pay combining regular earnings and 1.5x overtime earnings. | `double regHours, double otHours, double hourlyRate` | `double` |
| `PayrollService.calcTax()` | Computes standard 10% withholding tax on gross pay. | `double grossPay` | `double` |
| `PayrollService.calcBenefits()` | Computes standard 4% statutory social security / benefits deduction. | `double grossPay` | `double` |
| `PayrollService.calcNetPay()` | Computes net take-home pay after deducting total statutory withholdings. | `double grossPay` | `double` |
| `PayrollService.authenticate()` | Verifies entered username and password against loaded users from storage. | `String username, String password` | `User` |
| `PayrollService.clockIn()` | Creates a new shift record with `PENDING` clock-out if no open shift exists. | `User user` | `String` |
| `PayrollService.clockOut()` | Closes an active pending shift, calculates hours and pay, and saves the updated record. | `User user` | `TimeLog` |
| `PayrollService.resolvePendingShift()` | Allows supervisors to retroactively supply an exit timestamp for missed punches. | `TimeLog log, String outTime, double hourlyRate` | `boolean` |
| `PayrollService.addManualShift()` | Allows supervisors to add retroactive completed shift records directly. | `User user, String inTime, String outTime` | `boolean` |
| `PayrollService.generatePayStub()` | Aggregates all completed shifts for an employee, builds an itemized summary, and writes a backup text slip. | `User emp` | `String[]` |
| `FileDao.initStorage()` | Verifies that storage directories exist and seeds default user accounts if `users.txt` is missing. | *None* | `void` |
| `FileDao.loadUsers()` | Reads active user records from `data/users.txt` using `BufferedReader`. | *None* | `List<User>` |
| `FileDao.saveUsers()` | Overwrites `data/users.txt` with the current list of active users via `BufferedWriter`. | `List<User> users` | `boolean` |
| `FileDao.appendUser()` | Appends a single newly registered user to the end of `data/users.txt`. | `User user` | `boolean` |
| `FileDao.archiveUser()` | Moves an employee from `data/users.txt` to `data/archived_users.txt`. | `String employeeId` | `boolean` |
| `FileDao.restoreUser()` | Restores an archived employee back to active users. | `String employeeId` | `boolean` |
| `FileDao.deleteUser()` | Permanently deletes an employee record from active and archived files. | `String employeeId` | `boolean` |
| `FileDao.loadLogs()` | Reads all active shift timelogs from `data/timelogs.txt`. | *None* | `List<TimeLog>` |
| `FileDao.saveLogs()` | Overwrites `data/timelogs.txt` with updated shift lists. | `List<TimeLog> logs` | `boolean` |
| `FileDao.appendLog()` | Appends a single shift punch record to `data/timelogs.txt`. | `TimeLog log` | `boolean` |
| `FileDao.archiveLog()` | Moves a shift record from `timelogs.txt` to `archived_timelogs.txt`. | `String employeeId, String clockInTime` | `boolean` |
| `FileDao.restoreLog()` | Restores an archived shift log back to active payroll. | `String employeeId, String clockInTime` | `boolean` |
| `FileDao.deleteLog()` | Permanently removes a shift record from storage. | `String employeeId, String clockInTime` | `boolean` |
| `FileDao.savePayStubPdf()` | Compiles formatted payslip text into an official PDF document using OpenPDF. | `String employeeId, String payStubText` | `String` |
| `User.toCsv()` | Converts a User object's fields into a formatted CSV string. | *None* | `String` |
| `User.fromCsv()` | Parses a CSV string into a populated User object. | `String line` | `User` |
| `TimeLog.toCsv()` | Converts a TimeLog object's fields into a formatted CSV string. | *None* | `String` |
| `TimeLog.fromCsv()` | Parses a CSV string into a populated TimeLog object. | `String line` | `TimeLog` |
| `UIHelper.isValidCsv()` | Sanitizes user inputs to ensure they do not contain commas or linebreaks. | `String s` | `boolean` |

---

## D. File Handling Implementation

### 1. File Organization
The application organizes all data in dedicated local subdirectories:

- **Active Employee Accounts File:**
  - *File Name:* `data/users.txt`
  - *File Format:* Text file with Comma-Separated Values (`.txt` / CSV)
  - *Information Stored:* `EmployeeID,FullName,Username,Password,Role,HourlyRate`
- **Active Shift Timelogs File:**
  - *File Name:* `data/timelogs.txt`
  - *File Format:* Text file with Comma-Separated Values (`.txt` / CSV)
  - *Information Stored:* `EmployeeID,FullName,Date,ClockInTime,ClockOutTime,RegularHours,OvertimeHours,TotalPay`
- **Archived Employee Accounts File:**
  - *File Name:* `data/archived_users.txt`
  - *File Format:* Text file with Comma-Separated Values (`.txt` / CSV)
  - *Information Stored:* Deactivated employee records
- **Archived Shift Timelogs File:**
  - *File Name:* `data/archived_timelogs.txt`
  - *File Format:* Text file with Comma-Separated Values (`.txt` / CSV)
  - *Information Stored:* Deactivated shift records excluded from active payroll
- **Pay Stub Export Files:**
  - *Directory:* `paystubs/`
  - *File Formats:* Plain text receipts (`.txt`) and official formatted documents (`.pdf`)
  - *Information Stored:* Itemized breakdown of shifts, rates, gross earnings, taxes, benefits, and net payout

### 2. File Reading & Processing Mechanics
- **Sequential Line Reading:** Handled via `BufferedReader` wrapped around `FileReader`:
  ```java
  try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = reader.readLine()) != null) {
          TimeLog log = TimeLog.fromCsv(line);
          if (log != null) list.add(log);
      }
  }
  ```
- **Parsing Logic:** Lines are parsed using `line.split(",", -1)`. Empty lines or comment lines starting with `#` are safely skipped.
- **Robust Exception Handling:**
  - Uses **try-with-resources** to guarantee that file handles and OS stream locks are closed immediately, preventing resource leaks.
  - Catches `IOException` and reports descriptive terminal errors without crashing the GUI.
  - Catches `NumberFormatException` during line parsing so a single malformed row does not prevent the rest of the file from loading.
  - Automatically seeds default accounts (`EMP001`, `EMP002`, `EMP003`) on first run if `users.txt` is missing.

### 3. File Writing Mechanics
- **Appending New Records:** Uses `new FileWriter(path, true)` inside `appendUser()` and `appendLog()` to add records to the end of the file in $O(1)$ time without reading the entire dataset into memory.
- **Atomic Rewriting:** Uses `new FileWriter(path, false)` inside `saveUsers()` and `saveLogs()` to overwrite files when updating records, resolving pending shifts, or removing archived entries.

---

## E. Demonstration Requirements & Test Cases

During the final presentation, the group will demonstrate the six core competencies:

### 1. Program Functionality Demonstration Walkthrough
1. **Login:** Log in as Administrator (`admin` / `admin123`).
2. **Dashboard Overview:** Point out the live ticking header clock, employee badge, and dynamic role-based tabs.
3. **Staff Shift Lifecycle:** Switch to the Staff account (`staff` / `staff123`). Click **Clock In** (status badge immediately switches to green "Clocked In"). Click **Clock Out** (shift calculates regular and overtime hours, updates personal table, and badge turns gray).
4. **Supervisor Resolution:** Log in as Supervisor (`super` / `super123`). Demonstrate resolving a pending shift where an employee forgot to clock out.
5. **Admin Operations:** Add a new employee (`EMP004`), adjust an existing pay rate, and generate a pay stub.
6. **Pay Stub Export:** Click "Generate Pay Stub", view the modal receipt, click **Create PDF & Open in Browser**, and showcase the rendered PDF in Chrome/Edge.

### 2. Testing and Error Handling Matrix

| Test ID | Scenario / Feature | Test Input | Expected Result | Actual Result | Status |
| :---: | :--- | :--- | :--- | :--- | :---: |
| **TC01** | User Authentication | Correct: `admin` / `admin123` | Login succeeds; Admin dashboard opens. | Login successful modal; dashboard loads. | **PASSED** |
| **TC02** | User Authentication | Wrong password: `admin` / `wrong123` | Login rejected; error alert shown; password field cleared. | Error alert displayed; credentials blocked. | **PASSED** |
| **TC03** | Empty Login Submission | Empty username or password | Warning alert: "Please enter both username and password." | Warning prompt shown; prevents processing. | **PASSED** |
| **TC04** | Duplicate Clock-In | Click "Clock In" while already clocked in | Warning alert: "You are already clocked in since [Time]." | Button disabled or warning shown; prevents duplicate. | **PASSED** |
| **TC05** | Regular Hours Math | 8 hours worked (08:00 to 16:00) | Reg: 8.00 hrs, OT: 0.00 hrs | Reg: 8.00 hrs, OT: 0.00 hrs | **PASSED** |
| **TC06** | Overtime Hours Math | 10 hours worked (08:00 to 18:00) @ $200/hr | Reg: 8.00 hrs ($1,600), OT: 2.00 hrs @ 1.5x ($600). Total: $2,200. | Calculated exactly as $2,200.00. | **PASSED** |
| **TC07** | Statutory Deductions | Gross Pay = $2,200.00 | Tax (10%) = $220.00; Benefits (4%) = $88.00; Net = $1,892.00. | Tax, benefits, and net match exact formula. | **PASSED** |
| **TC08** | CSV Injection Prevention | Add employee name: `John, Jr.` | Error alert: Fields cannot contain commas. | Input blocked; prevents data corruption. | **PASSED** |
| **TC09** | Invalid Pay Rate | Enter rate: `-150` or `abc` | Error alert: Positive numeric rate required. | Input blocked; prompts user to re-enter. | **PASSED** |
| **TC10** | Missing Storage File | Delete `data/users.txt` and launch app | App recreates `data/` and seeds default accounts automatically. | `initStorage()` detects missing file and recreates defaults. | **PASSED** |
| **TC11** | PDF Generation | Click "Create PDF & Open in Browser" | PDF created in `paystubs/` and opened in default browser. | PDF generated cleanly with monospace alignment. | **PASSED** |

---

## F. Challenges Encountered and Lessons Learned

### Challenges Encountered:
1. **Managing Incomplete Shifts in Flat Files:**
   - *Challenge:* When an employee clocks in, their shift has no clock-out timestamp or total earnings yet. Storing empty fields in standard CSV rows often leads to `ArrayIndexOutOfBoundsException` or parsing errors.
   - *Solution:* Introduced an explicit state token `"PENDING"` in the clock-out column, allowing the parser to cleanly identify ongoing shifts without corrupting numeric columns.
2. **Preventing File Corruption in Delimited Storage:**
   - *Challenge:* Because commas (`,`) serve as delimiters, if a user entered a comma in their name (e.g., `"Cruz, Juan"`) or password, the entire row would shift columns upon reading.
   - *Solution:* Implemented `UIHelper.isValidCsv()` across all input dialogs to sanitize inputs and reject delimiters before writing to disk.
3. **Safe Deletion vs. Historical Integrity:**
   - *Challenge:* If an administrator deletes a former employee, previously logged shifts for that employee could become orphaned or cause null-pointer errors.
   - *Solution:* Engineered a two-tier Archiving architecture (`archived_users.txt` and `archived_timelogs.txt`). This allows administrators to disable accounts while preserving historical audit trails for payroll compliance.

### Lessons Learned:
1. **Separation of Concerns (MVC / Layered Architecture):**
   - Keeping mathematical calculations in `PayrollService`, file streaming in `FileDao`, and graphical widgets in `ui/` made the application significantly easier to debug, test, and explain.
2. **Defensive Programming & Resource Management:**
   - Leveraging Java's **try-with-resources** guarantees that files are never accidentally locked in memory by the operating system, ensuring consistent multi-window stability.
3. **User Experience in Desktop Applications:**
   - Adding visual cues such as dynamic status badges, password visibility toggles, and formatted receipt previews transforms a basic terminal exercise into an intuitive, production-grade desktop application.
