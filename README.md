# 🏦 Bank Management System (Java Full-Stack Website & Console CLI)

A clean, beginner-friendly Bank Management System designed specifically for **second-year engineering students** learning **Core Java, Object-Oriented Programming (OOP), and Web Fundamentals (HTML5, CSS3, JavaScript & REST APIs)**.

No heavy frameworks (no Spring Boot, no Maven/Gradle, no external database dependencies) — built 100% with **Standard Core Java (`com.sun.net.httpserver.HttpServer`)** and **pure HTML/CSS/JavaScript**.

---

## 📁 Project Directory Structure

```text
BankManagementSystem/
├── run-server.bat                   # 🚀 One-click Windows launcher for Web Server & UI
│
├── BankJavaApp/                     # Java Backend & CLI
│   ├── BankWebServer.java           # Java HTTP Web Server (port 8080) serving REST APIs & UI
│   ├── Main.java                    # Menu-driven console interface (CLI)
│   ├── Bank.java                    # Business logic engine (validations, transactions, accounts)
│   ├── Account.java                 # Account entity model
│   ├── Transaction.java             # Transaction history entity model
│   ├── FileHandler.java             # Persistence layer (reads & writes accounts.txt)
│   └── accounts.txt                 # Comma-separated text database
│
├── BankWebUI/                       # Dynamic Web Frontend
│   ├── index.html                   # Official Homepage / Landing page
│   ├── login.html                   # Account Login page
│   ├── register.html                # New Account Registration page
│   ├── dashboard.html               # Customer Dashboard (live balance, deposit, withdraw, statements)
│   ├── transfer.html                # Inter-account fund transfer screen
│   ├── style.css                    # Shared responsive modern CSS stylesheet
│   └── app.js                       # Frontend JavaScript connecting UI to Java REST APIs
│
└── README.md                        # Documentation & setup guide
```

---

## 🚀 Quick Start: Running & Viewing from Other PCs / Devices

### 🌐 Access Online from ANY PC or Mobile Phone (Worldwide)
Your website is currently deployed live on a public HTTPS tunnel:
👉 **[https://local-recordings-which-richmond.trycloudflare.com](https://local-recordings-which-richmond.trycloudflare.com)**

### 📶 Access from Devices on the Same Wi-Fi / Local Network
If your other PC or phone is connected to the same Wi-Fi router, open:
👉 **`http://192.168.0.109:8080`**

### ⚡ One-Click Online Deployment Launcher (Windows)
Double-click **`share-online.bat`** in the project root directory. It will automatically:
1. Compile and launch the Java Web Server.
2. Start Cloudflare Tunnel and generate a live public HTTPS link.

### 💻 Localhost Access (This PC Only)
Double-click **`run-server.bat`** or run:
```powershell
cd "C:\Users\HP\.gemini\antigravity\scratch\BankManagementSystem\BankJavaApp"
javac *.java
java BankWebServer
```
Then open **[http://localhost:8080](http://localhost:8080)** in your browser.

---

## 💻 Features Available on the Website

1. **🏠 Landing Page (`index.html`)**: Feature highlights and quick links to open accounts or sign in.
2. **📝 Open New Account (`register.html`)**: Register a new account with Full Name, Account Number, Initial Deposit, and 4-digit PIN.
3. **🔑 Secure Login (`login.html`)**: Authenticate with Account Number and PIN.
4. **📊 Interactive Dashboard (`dashboard.html`)**:
   - Live balance display.
   - **📥 Quick Deposit**: Add money to your account with immediate balance refresh.
   - **🏧 Quick Withdraw**: Secure cash withdrawal verified with your PIN.
   - **📜 Live Mini Statement**: Auto-rendered table of all recent transactions (Deposits, Withdrawals, Transfers).
5. **💸 Fund Transfer (`transfer.html`)**: Transfer funds to another account number with security PIN authorization.
6. **💾 Auto-Persistence**: Any action taken on the web interface is instantly saved into `accounts.txt` by `FileHandler.java`.

---

## 🔌 Java REST API Endpoints

The built-in Java server (`BankWebServer.java`) exposes the following endpoints:

| Method | Endpoint | Description | Sample Payload / Query |
|---|---|---|---|
| `POST` | `/api/register` | Open a new bank account | `{"accountNumber":"1001", "holderName":"Alice", "pin":"1234", "initialDeposit":500}` |
| `POST` | `/api/login` | Authenticate customer | `{"accountNumber":"1001", "pin":"1234"}` |
| `GET` | `/api/account` | Retrieve account details | `?accountNumber=1001&pin=1234` |
| `POST` | `/api/deposit` | Deposit money | `{"accountNumber":"1001", "amount":200}` |
| `POST` | `/api/withdraw` | Withdraw money (PIN required) | `{"accountNumber":"1001", "pin":"1234", "amount":100}` |
| `POST` | `/api/transfer` | Transfer funds to another account | `{"fromAccount":"1001", "pin":"1234", "toAccount":"1002", "amount":50, "remarks":"Rent"}` |
| `GET` | `/api/statement` | Get transaction history list | `?accountNumber=1001` |

---

## ☕ Alternative: Running the Console Application (CLI)

If you wish to run the console menu-driven version:
```powershell
cd "C:\Users\HP\.gemini\antigravity\scratch\BankManagementSystem\BankJavaApp"
javac *.java
java Main
```

---

## 🎓 Key Engineering Learning Points

1. **Full-Stack Architecture**: Demonstrates the separation between the **Frontend (HTML/CSS/JS)** and the **Backend (Java HTTP Server & Business Logic)**.
2. **REST API Design**: Shows how HTTP request methods (`GET`, `POST`), status codes (`200`, `400`, `401`, `404`), and JSON payloads coordinate client-server communication.
3. **Data Persistence**: Illustrates file I/O streams (`BufferedReader`, `BufferedWriter`) in Java.
4. **Security & Validation**: Enforces credential authentication, balance boundary checks, and input sanitization.

---

## 📜 License
This project is open-source and intended for academic study and coursework submission.
