# Staff Timekeeping and Payroll Desktop Application
# SYSTEM FLOWCHARTS & PROCESS LOGIC

This document provides visual flowcharts and logic diagrams for the **Staff Timekeeping & Payroll System** as required by the **DLSU-D S-ITCS112 Fundamentals of Programming** rubric.

> **Tip:** You can paste any of the Mermaid diagrams below into [Mermaid Live Editor](https://mermaid.live) to download high-resolution PNG or SVG images for your final document or slides.

---

## 1. Overall System Architecture & Authentication Flow

```mermaid
flowchart TD
    Start([Application Start]) --> InitStorage[FileDao.initStorage\nCheck folders & default files]
    InitStorage --> SetTheme[Apply FlatLaf UI Theme]
    SetTheme --> OpenLogin[Display LoginFrame]
    
    OpenLogin --> InputCreds[/User enters Username & Password/]
    InputCreds --> SubmitLogin{Click Login / Press Enter}
    
    SubmitLogin --> CheckEmpty{Are fields empty?}
    CheckEmpty -- Yes --> ShowWarning[Show Warning Alert: Please enter credentials]
    ShowWarning --> InputCreds
    
    CheckEmpty -- No --> AuthCheck{PayrollService.authenticate\nMatch in users.txt?}
    AuthCheck -- Invalid --> ShowError[Show Error Alert: Invalid credentials]
    ShowError --> ClearPass[Clear password field & refocus]
    ClearPass --> InputCreds
    
    AuthCheck -- Valid --> OpenDashboard[Display DashboardFrame\nPass authenticated User]
    OpenDashboard --> StartClock[Start 1-second live clock timer]
    OpenDashboard --> CheckRole{Check User Role}
    
    CheckRole -- Staff --> StaffTabs[Load: My Timekeeping + Account Details]
    CheckRole -- Supervisor --> SuperTabs[Load: My Timekeeping + Account Details\n+ Team Overview & Logs]
    CheckRole -- Admin --> AdminTabs[Load: My Timekeeping + Account Details\n+ Team Overview & Logs\n+ Employee & Payroll Admin]
```

---

## 2. Shift Lifecycle & Timekeeping Logic (Clock In / Clock Out)

```mermaid
flowchart TD
    StaffDashboard[Staff Views My Timekeeping Panel] --> ActionChoice{Select Action}
    
    %% CLOCK IN
    ActionChoice -- Click 'Clock In' --> CheckPendingIn{Has ongoing PENDING punch?}
    CheckPendingIn -- Yes --> WarnAlreadyIn[Alert: Already clocked in]
    CheckPendingIn -- No --> CaptureNowIn[Capture current timestamp: yyyy-MM-dd HH:mm:ss]
    CaptureNowIn --> AppendLog[FileDao.appendLog\nStatus: PENDING, Hours: 0, Pay: 0]
    AppendLog --> UpdateUIIn[Set Badge to Green 'Clocked In'\nDisable Clock-In / Enable Clock-Out]
    
    %% CLOCK OUT
    ActionChoice -- Click 'Clock Out' --> CheckPendingOut{Find PENDING punch in logs?}
    CheckPendingOut -- None --> WarnNoIn[Alert: No active clock-in found]
    CheckPendingOut -- Found --> CaptureNowOut[Capture Clock-Out timestamp]
    CaptureNowOut --> CalcDiff[Compute elapsed milliseconds:\nTotalHours = diff / 3,600,000]
    
    CalcDiff --> SplitHours[Calculate Hours:\nRegularHours = min 8.0, TotalHours\nOvertimeHours = max 0.0, TotalHours - 8.0]
    SplitHours --> CalcPay[Calculate Gross Pay:\nRegPay = RegularHours * HourlyRate\nOTPay = OvertimeHours * HourlyRate * 1.5\nShiftTotal = RegPay + OTPay]
    
    CalcPay --> SaveLogFile[FileDao.saveLogs\nUpdate record in timelogs.txt]
    SaveLogFile --> UpdateUIOut[Set Badge to Gray 'Clocked Out'\nEnable Clock-In / Disable Clock-Out\nRefresh Shift Table & Personal Summary]
```

---

## 3. Payroll Calculation & Official Pay Slip Generation

```mermaid
flowchart TD
    TriggerSlip[User clicks 'Generate Pay Stub'] --> LoadLogs[Load all timelogs for Employee]
    LoadLogs --> FilterCompleted{Any completed non-pending shifts?}
    
    FilterCompleted -- No --> ShowNoData[Alert: No completed shifts found]
    FilterCompleted -- Yes --> Accumulate[Sum totals across completed shifts:\nTotalRegHours = sum RegularHours\nTotalOTHours = sum OvertimeHours\nGrossPay = sum ShiftTotalPay]
    
    Accumulate --> CalcDeductions[Calculate Statutory Deductions:\nTax = GrossPay * 0.10 (10% Withholding)\nBenefits = GrossPay * 0.04 (4% SSS/Health)\nTotalDeductions = Tax + Benefits]
    
    CalcDeductions --> CalcNet[Calculate Take-Home Pay:\nNetPay = GrossPay - TotalDeductions]
    
    CalcNet --> BuildText[Format Official Text Pay Slip\nInclude Header, Itemized Shift Rows,\nEarnings Breakdown, Deductions, and Net Pay]
    
    BuildText --> SaveTxt[Save text backup to paystubs/PayStub_EMP_TIMESTAMP.txt]
    SaveTxt --> ShowDialog[Display PayStubDialog Modal Preview]
    
    ShowDialog --> PDFAction{Click 'Create PDF & Open in Browser'?}
    PDFAction -- Yes --> OpenPDFLib[Use OpenPDF PdfWriter\nRender monospace text to PDF document]
    OpenPDFLib --> SavePDFFile[Save to paystubs/PayStub_EMP_TIMESTAMP.pdf]
    SavePDFFile --> LaunchBrowser[Desktop.getDesktop.browse\nOpen generated PDF in system web browser]
    PDFAction -- No --> CloseDialog[Close Preview]
```

---

## 4. Supervisor Shift Resolution Logic

```mermaid
flowchart TD
    SuperStart[Supervisor opens Team Overview & Logs] --> SelectShift[Select a shift with PENDING status]
    SelectShift --> ClickResolve[Click 'Resolve Missed Clock-Out']
    
    ClickResolve --> PromptOutTime[/Prompt: Enter Clock-Out timestamp\nyyyy-MM-dd HH:mm:ss/]
    PromptOutTime --> CheckCancel{Did user cancel or leave empty?}
    CheckCancel -- Yes --> AbortResolve[Cancel operation]
    
    CheckCancel -- No --> ValidateTime{Is OutTime after InTime\n& valid format?}
    ValidateTime -- No --> ShowTimeError[Show Error: Invalid timestamp]
    
    ValidateTime -- Yes --> ComputeResolved[Compute total hours, regular hours,\novertime hours, and shift gross pay]
    ComputeResolved --> UpdateRecord[Update TimeLog clockOutTime, hours, and pay]
    UpdateRecord --> SaveToDisk[FileDao.saveLogs\nWrite updated records to timelogs.txt]
    SaveToDisk --> RefreshAllViews[Refresh supervisor table, personal views & dashboard]
    RefreshAllViews --> ShowSuccess[Show Success Alert: Missed clock-out resolved!]
```

---

## 5. File Persistence Mechanics (Read / Write Flow)

```mermaid
flowchart LR
    subgraph ReadOperation ["Reading Data (loadUsers / loadLogs)"]
        F1[(data/*.txt)] --> FR[FileReader]
        FR --> BR[BufferedReader]
        BR --> LineLoop[Loop: readLine]
        LineLoop --> ParseCSV[Entity.fromCsv split by comma]
        ParseCSV --> AddList[Add to Java List]
    end

    subgraph WriteOperation ["Writing Data (saveUsers / saveLogs / append)"]
        JavaList[Java List of Objects] --> LoopObj[Loop each Entity]
        LoopObj --> ToCSV[Entity.toCsv]
        ToCSV --> BW[BufferedWriter]
        BW --> FW[FileWriter]
        FW --> F2[(data/*.txt)]
    end
```
