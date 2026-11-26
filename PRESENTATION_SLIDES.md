# Staff Timekeeping and Payroll Desktop Application
# PRESENTATION SLIDES OUTLINE & DEFENSE SCRIPT

Designed for **S-ITCS112 Fundamentals of Programming** Final Project Group Presentation (DLSU-D CICS).

You can directly copy the text below into **Microsoft PowerPoint**, **Google Slides**, or **Canva**.

---

### Slide 1: Title Slide
- **Title:** Staff Timekeeping & Payroll Desktop Application
- **Subtitle:** An Automated, File-Based Java Desktop Solution for Attendance Tracking & Payroll Computation
- **Course & Section:** S-ITCS112 Fundamentals of Programming | BIT12 / BIT13
- **College & University:** College of Information and Computer Studies (CICS), De La Salle University – Dasmariñas
- **Group Members:** [List group members' names]
- **Instructor:** [Instructor Name]
- **Date:** December 2026

> **Speaker Notes:**
> "Good morning/afternoon, professors and fellow students. Today, our team is proud to present our final project for Fundamentals of Programming: the Staff Timekeeping and Payroll Desktop Application. This is a modular, standalone Java application built to automate employee attendance and wage calculation with zero external database dependencies."

---

### Slide 2: Problem Statement & Motivation
- **The Problem with Manual Systems:**
  - Traditional punch cards and paper timesheets are vulnerable to lost records and buddy-punching.
  - Manual wage computations frequently result in mathematical discrepancies (overtime, tax, deductions).
  - External databases like MySQL require complex installations and network connectivity that many small offices lack.
- **Our Proposed Solution:**
  - A clean, modern desktop application powered by Java Swing and FlatLaf.
  - Built-in payroll engine calculating regular hours, overtime ($1.5\times$), 10% tax, and 4% benefits automatically.
  - 100% self-contained persistence using Java File Handling (`BufferedReader` and `BufferedWriter`).

> **Speaker Notes:**
> "Many small businesses struggle with manual spreadsheets or paper punch logs, leading to miscalculated overtime and wasted hours during payroll cutoff. Our application solves this by providing a reliable, automated system that runs anywhere out-of-the-box without needing any database setup."

---

### Slide 3: System Architecture & Technologies Used
- **Programming Language:** Java SE (JDK 8+)
- **Architecture Pattern:** Layered Architecture / Separation of Concerns:
  - **Presentation Layer:** Swing UI (`LoginFrame`, `DashboardFrame`, panels) with FlatLaf styling.
  - **Business Logic Layer:** `PayrollService.java` (hours parsing, tax formulas, authentication).
  - **Data Access Layer:** `FileDao.java` (CSV reading/writing and PDF generation).
  - **Domain Model Layer:** `User.java` and `TimeLog.java` POJOs.
  - **Storage:** Plain text CSV files (`users.txt`, `timelogs.txt`).
- **External Libraries:** FlatLaf 3.5.4 (Modern UI Look & Feel), OpenPDF (Print-ready PDF export).

> **Speaker Notes:**
> "Our codebase is strictly structured into modular packages. By separating our graphical user interface from our calculation service and our file access objects, our code is easy to maintain, test, and understand."

---

### Slide 4: Key Features & Role-Based Access Control
- **1. Staff / Regular Employees:**
  - 1-click **Clock In** and **Clock Out** with live status indicator.
  - Personal shift history table and cumulative earnings summary.
  - Self-service password changer and personal official pay stub generator.
- **2. Supervisors:**
  - Complete team-wide timesheet overview.
  - "Resolve Missed Clock-Out" tool for employee punch oversights.
  - Retroactive manual shift entry with timestamp validation.
  - Shift archiving and restoration.
- **3. System Administrators:**
  - Full Employee CRUD (Add, Edit, Archive, Restore, Delete).
  - Hourly rate adjustment and credential resets.
  - Pay slip generation for any employee in the company.

> **Speaker Notes:**
> "Security and organization are maintained through Role-Based Access Control. When an employee logs in, the dashboard dynamically shows only the tabs and tools permitted for their specific role—whether they are Staff, Supervisor, or Admin."

---

### Slide 5: Core Mathematical Engine & Formulas
- **Shift Duration:**
  $$\text{Hours} = \frac{\text{Clock-Out Time (ms)} - \text{Clock-In Time (ms)}}{3,600,000}$$
- **Regular Hours Cap:** $\text{RegHours} = \min(8.0, \text{TotalHours})$
- **Overtime Multiplier:** $\text{OTHours} = \max(0.0, \text{TotalHours} - 8.0)$ paid at **$1.5\times$** base rate.
- **Gross Earnings:**
  $$\text{Gross Pay} = (\text{RegHours} \times \text{Rate}) + (\text{OTHours} \times \text{Rate} \times 1.5)$$
- **Statutory Deductions:**
  - Withholding Tax: $\text{Gross Pay} \times 10.0\%$
  - Social Security / Benefits: $\text{Gross Pay} \times 4.0\%$
- **Net Take-Home Pay:** $\text{Net Pay} = \text{Gross Pay} - \text{Total Deductions}$

> **Speaker Notes:**
> "All mathematical calculations are encapsulated in our `PayrollService` class. We strictly enforce standard 8-hour shift limits, compute 1.5x overtime for extra hours, deduct standard 14% combined taxes and statutory contributions, and output the exact net pay."

---

### Slide 6: File Handling & Data Persistence Mechanics
- **Sequential Line Reading:**
  - Uses `BufferedReader` wrapping `FileReader` in a `try-with-resources` block.
  - Parses each CSV line with `line.split(",")` and reconstructs Java objects.
- **Two Writing Strategies:**
  - **Append Mode (`append = true`):** Adds single new punches or employees in $O(1)$ time without reading the whole file.
  - **Overwrite Mode (`append = false`):** Updates existing shifts or modifies employee records atomically.
- **Two-Tiered Archiving Safety:**
  - Instead of outright deletion, archived users and shifts move to `archived_users.txt` and `archived_timelogs.txt` to preserve auditing trails.

> **Speaker Notes:**
> "File handling is the core requirement of this project. In `FileDao.java`, we utilize `BufferedReader` and `BufferedWriter` with try-with-resources. This ensures that file handles are safely closed by the JVM and eliminates memory leaks or corrupted files."

---

### Slide 7: Input Validation & Defensive Programming
- **CSV Injection Prevention:** `UIHelper.isValidCsv()` blocks commas and newline characters from text inputs so records cannot corrupt the CSV format.
- **Duplicate Prevention:** Checks for existing Employee IDs and usernames before creating accounts.
- **Positive Numeric Boundaries:** Verifies that hourly rates and pay amounts are positive numeric doubles.
- **Timestamp Syntax Checks:** Uses `SimpleDateFormat` to enforce valid `yyyy-MM-dd HH:mm:ss` date-time formatting.
- **Exception Shielding:** Catches `NumberFormatException` and `IOException` gracefully, presenting clear Swing error dialogs rather than crashing the program.

> **Speaker Notes:**
> "We placed heavy emphasis on defensive programming. If a user attempts to enter commas in their name, type negative rates, or submit invalid dates, the application blocks the bad input and displays friendly warning modals."

---

### Slide 8: Live System Demonstration Walkthrough
- **Demonstration Flow:**
  1. **Login:** Log in with `admin` / `admin123`.
  2. **Employee Management:** Add new employee (`EMP004`) and adjust an hourly rate.
  3. **Shift Tracking:** Switch account to `staff` / `staff123`. Click **Clock In** -> notice green live status badge -> click **Clock Out** -> show calculated hours.
  4. **Supervisor Resolution:** Log in as `super` / `super123`. Show resolving a pending missed shift.
  5. **Pay Slip & PDF Export:** Generate official pay stub -> click **Create PDF & Open in Browser** -> show instant browser display of the PDF file.
  6. **Under the Hood:** Open `data/users.txt` and `data/timelogs.txt` in Notepad to prove data is saved instantly.

> **Speaker Notes:**
> "[Conduct live demonstration on laptop following the 6 steps listed above.]"

---

### Slide 9: Challenges Encountered & Lessons Learned
- **Challenges:**
  - Managing ongoing shifts (`PENDING` state) in flat files without breaking numeric parsing.
  - Preventing CSV column misalignment caused by user input delimiters.
  - Preserving historical shift logs when employee accounts are deactivated.
- **Lessons Learned:**
  - The power of modular design: UI changes never broke payroll calculations because layers are decoupled.
  - The necessity of automated error handling (`try-with-resources`).
  - Designing software from a real-world user perspective (clear colors, readable fonts, confirmation prompts).

> **Speaker Notes:**
> "Throughout this project, we learned that good software engineering isn't just about making code work—it's about handling edge cases, keeping files safe from corruption, and ensuring the interface is intuitive for non-technical users."

---

### Slide 10: Conclusion & Q&A
- **Summary:**
  - A fully functional, database-independent Java desktop application.
  - Meets 100% of the S-ITCS112 guidelines (modular code, file persistence, validation, clean UI, PDF export).
  - Complete documentation and test cases provided.
- **Thank you!**
- **Questions & Answers (Q&A)**

> **Speaker Notes:**
> "Thank you for your time and attention. We are now open to any questions or requests to inspect specific parts of the source code."
