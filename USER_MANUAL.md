# Staff Timekeeping and Payroll Desktop Application
# USER MANUAL

A comprehensive operational guide for Employees, Supervisors, and System Administrators.

---

## 1. System Requirements & Launching the Application

### Requirements:
- **Operating System:** Windows 10/11, macOS, or Linux
- **Java Runtime Environment:** JDK / JRE 8 or higher
- **IDE (Optional for development):** Apache NetBeans IDE, IntelliJ IDEA, Eclipse, or Terminal CLI

### Launching the Application:
1. **Via NetBeans IDE:**
   - Open NetBeans -> Click **File** -> **Open Project...** -> Select `Payroll-System`.
   - Press <kbd>F6</kbd> or click the green **Run** button.
2. **Via Command Line (Maven):**
   ```bash
   mvn compile exec:java -Dexec.mainClass="com.payroll.app.Main"
   ```

---

## 2. Default Test Accounts (Role-Based Access)

The system automatically initializes these three accounts on first boot:

| Role | Employee ID | Full Name | Username | Password | Base Hourly Rate |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Admin** | `EMP001` | Admin User | `admin` | `admin123` | **$500.00 / hr** |
| **Supervisor** | `EMP002` | Jane Supervisor | `super` | `super123` | **$350.00 / hr** |
| **Staff** | `EMP003` | John Staff | `staff` | `staff123` | **$200.00 / hr** |

---

## 3. User Interface Guide by Feature

### A. Logging In
1. Start the application. The **Login Modal** appears centered on screen.
2. Enter your assigned **Username** and **Password**.
3. *Tip:* Click the eye icon (**◉**) on the right side of the password field to toggle password visibility.
4. Press <kbd>Enter</kbd> or click the blue **Login** button.
5. If authentication succeeds, the role-customized **Dashboard** will load.

---

### B. Staff Guide: Time Tracking & Pay Stubs
*(Available to all logged-in users under the "My Timekeeping" tab)*

#### 1. Clocking In:
- At the start of your shift, click the green **Clock In** button.
- The status badge in the upper right header will immediately turn **Green** showing `Status: Clocked In (HH:mm:ss)`.
- A new row appears in your personal shift table marked as `PENDING`.
- The "Clock In" button is now disabled to prevent accidental double-punching.

#### 2. Clocking Out:
- When your shift ends, click the red **Clock Out** button.
- The system computes:
  - Elapsed hours.
  - Regular hours (up to 8.0 hrs).
  - Overtime hours (any hours beyond 8.0 hrs).
  - Shift total earnings ($1.5\times$ base rate for overtime).
- The status badge updates to gray `Status: Clocked Out`.
- The personal summary at the bottom updates with your cumulative hours and gross pay.

#### 3. Generating Your Official Pay Stub:
1. Click **Generate My Pay Stub** in the toolbar.
2. An itemized preview window (**Official Pay Slip**) opens.
3. Review the breakdown:
   - Header details (Employee ID, Name, Role, Base & Overtime rates).
   - Itemized shift table with dates and hours.
   - Earnings summary (Regular vs. Overtime pay, Gross Pay).
   - Deductions (10.0% Withholding Tax, 4.0% SSS/Benefits).
   - Net Take-Home Pay.
4. Click **Create PDF & Open in Browser**:
   - A PDF file is generated in the `paystubs/` folder.
   - Your computer's default web browser immediately opens the PDF for printing or saving.

---

### C. Supervisor Guide: Team Oversight & Shift Corrections
*(Accessible to Supervisors and Admins under "Team Overview & Logs")*

#### 1. Monitoring Team Logs:
- View all shifts punched by any employee across the entire organization.
- Active shifts currently in progress show `PENDING` in the "Clock Out Time" column.

#### 2. Resolving a Missed Clock-Out:
*Use this when an employee forgets to clock out at the end of their shift:*
1. Select the row containing the `PENDING` shift in the table.
2. Click **Resolve Missed Clock-Out** (Amber button).
3. The dialog displays the recorded Clock-In timestamp. Enter the approved Clock-Out time in `yyyy-MM-dd HH:mm:ss` format.
4. Click **OK**. The shift will be updated, regular/overtime hours computed, and pay calculated immediately.

#### 3. Adding a Manual Shift:
*Use this for retroactive shifts or off-site assignments:*
1. Click **Add Manual Shift Entry** (Purple button).
2. Select the employee from the dropdown list.
3. Enter the approved Clock-In and Clock-Out timestamps.
4. Click **OK**. The record is saved and appended to active logs.

#### 4. Archiving & Restoring Shifts:
- **Archive Shift:** Select any completed shift and click **Archive Selected Shift**. This hides the shift from active payroll without deleting historical audit records.
- **View / Restore:** Click **View Archived** to inspect archived logs, select a shift, and click **Restore Selected Shift** to bring it back to active status.

---

### D. Administrator Guide: Employee & Account Management
*(Exclusively accessible to Administrators under "Employee & Payroll Admin")*

#### 1. Adding a New Employee:
1. Click **+ Add Employee** (Green button).
2. The system auto-generates the next sequential ID (e.g., `EMP004`).
3. Enter the **Full Name**, **Username**, **Password**, select a **Role** (`Staff`, `Supervisor`, or `Admin`), and enter an **Hourly Rate** (e.g. `250.00`).
4. Click **OK**. The employee account is saved to `data/users.txt` and is immediately eligible to log in.

#### 2. Adjusting Pay Rates:
1. Select the employee row from the table.
2. Click **Adjust Rate** (Blue button).
3. Enter the new hourly wage and click **OK**. Future shifts will be calculated using this new rate.

#### 3. Editing Credentials & Resetting Passwords:
- **Edit Credentials:** Select employee -> Click **Edit Credentials** -> Modify Name, Username, or Role.
- **Reset Password:** Select employee -> Click **Reset Password** -> Enter new password.

#### 4. Archiving vs. Deleting Employees:
- **Archive Employee:** Deactivates an employee's login credentials while preserving their historical shift records and past pay stubs.
- **Restore Employee:** Reactivates an archived account from the archived view.
- **Delete Employee:** Permanently deletes the account record.

---

### E. Account Management Guide
*(Available to all users under the "Account Details" tab)*
1. View your profile details (Full Name, ID, Username, Role, Hourly Rate).
2. To change your password:
   - Enter your **Current Password**.
   - Enter your **New Password**.
   - Re-enter your new password in **Confirm Password**.
   - Click **Update Password**.

---

## 4. Troubleshooting & FAQ

| Problem | Cause | Solution |
| :--- | :--- | :--- |
| **"Employee ID or Username already exists!"** | The ID or username is already taken. | Choose a unique username or allow the system to auto-increment the ID. |
| **"Invalid timestamps! Please check format."** | Entered date/time does not follow `yyyy-MM-dd HH:mm:ss`. | Use format: `2026-11-26 17:00:00`. |
| **"No active clock-in found."** | Trying to clock out before clocking in. | Click "Clock In" first before clocking out. |
| **"Cannot open web browser automatically."** | OS lacks default browser registration. | Open the generated PDF directly from the `paystubs/` folder. |
| **Text field contains commas error.** | Commas are reserved delimiters in CSV files. | Remove commas (`,`) from name, username, or password inputs. |
