import os
import subprocess
import html

project_dir = r"C:\Users\HP\.gemini\antigravity\scratch\BankManagementSystem"
output_html = os.path.join(project_dir, "code_document.html")
output_pdf = os.path.join(project_dir, "Bank_Management_System_Complete_Code.pdf")

files = [
    # Java Backend
    ("1.1 Account.java", "BankJavaApp/Account.java", "Java - Entity Class representing bank account details"),
    ("1.2 Transaction.java", "BankJavaApp/Transaction.java", "Java - Entity Class representing financial transactions"),
    ("1.3 Bank.java", "BankJavaApp/Bank.java", "Java - Core Business Logic Engine & Validations"),
    ("1.4 FileHandler.java", "BankJavaApp/FileHandler.java", "Java - Persistence Layer reading/writing accounts.txt"),
    ("1.5 Main.java", "BankJavaApp/Main.java", "Java - Interactive Console CLI Application"),
    ("1.6 BankWebServer.java", "BankJavaApp/BankWebServer.java", "Java - Built-in HTTP Web Server & REST API Endpoints"),
    
    # Web Frontend
    ("2.1 index.html", "BankWebUI/index.html", "HTML - Modern Banking Home & Landing Page"),
    ("2.2 login.html", "BankWebUI/login.html", "HTML - Customer Authentication Login Form"),
    ("2.3 register.html", "BankWebUI/register.html", "HTML - New Account Registration Form"),
    ("2.4 dashboard.html", "BankWebUI/dashboard.html", "HTML - Customer Dashboard & Live Mini Statement"),
    ("2.5 transfer.html", "BankWebUI/transfer.html", "HTML - Inter-Account Fund Transfer Screen"),
    ("2.6 style.css", "BankWebUI/style.css", "CSS - Responsive Modern Styling & Theme"),
    ("2.7 app.js", "BankWebUI/app.js", "JavaScript - REST API Integration, Session & UI Logic"),
    
    # Scripts & Data
    ("3.1 run-server.bat", "run-server.bat", "Batch - One-Click Launcher for Web Server & UI"),
    ("3.2 accounts.txt", "BankJavaApp/accounts.txt", "Text - Comma-Separated Database File"),
    ("3.3 README.md", "README.md", "Markdown - Comprehensive Project Guide & Setup Instructions")
]

sections_html = []

for title, rel_path, desc in files:
    full_path = os.path.join(project_dir, rel_path.replace("/", "\\"))
    if os.path.exists(full_path):
        with open(full_path, "r", encoding="utf-8", errors="replace") as f:
            code = f.read()
    else:
        code = f"// File not found: {full_path}"

    escaped_code = html.escape(code)
    section = f"""
    <div class="code-section page-break">
        <div class="file-header">
            <div class="file-title">{title}</div>
            <div class="file-path">Path: <code>{rel_path}</code></div>
            <div class="file-desc">{desc}</div>
        </div>
        <pre class="code-block"><code>{escaped_code}</code></pre>
    </div>
    """
    sections_html.append(section)

html_content = f"""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Bank Management System - Complete Source Code</title>
    <style>
        @page {{
            size: A4;
            margin: 18mm 15mm 18mm 15mm;
            @bottom-right {{
                content: counter(page);
            }}
        }}

        body {{
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            color: #1e293b;
            background: #ffffff;
            line-height: 1.5;
            font-size: 10pt;
            margin: 0;
            padding: 0;
        }}

        .page-break {{
            page-break-after: always;
            break-after: page;
        }}

        /* Cover Page */
        .cover-page {{
            height: 90vh;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            text-align: center;
            padding: 40px 20px;
        }}

        .cover-icon {{
            font-size: 64pt;
            margin-bottom: 20px;
        }}

        .cover-title {{
            font-size: 26pt;
            font-weight: 800;
            color: #1e40af;
            margin-bottom: 10px;
            letter-spacing: -0.5px;
        }}

        .cover-subtitle {{
            font-size: 14pt;
            color: #475569;
            margin-bottom: 30px;
            max-width: 600px;
        }}

        .cover-meta {{
            background: #f1f5f9;
            border-radius: 12px;
            padding: 20px 35px;
            text-align: left;
            font-size: 10.5pt;
            border: 1px solid #cbd5e1;
            margin-bottom: 30px;
        }}

        .cover-meta table {{
            border-collapse: collapse;
        }}

        .cover-meta td {{
            padding: 6px 12px;
        }}

        .cover-meta td.label {{
            font-weight: 600;
            color: #0f172a;
        }}

        .cover-footer {{
            margin-top: auto;
            color: #94a3b8;
            font-size: 9pt;
        }}

        /* Table of Contents */
        .toc-container {{
            padding: 20px 0;
        }}

        .toc-title {{
            font-size: 18pt;
            font-weight: 700;
            color: #1e40af;
            border-bottom: 2px solid #3b82f6;
            padding-bottom: 8px;
            margin-bottom: 20px;
        }}

        .toc-group-title {{
            font-size: 12pt;
            font-weight: 700;
            color: #0f172a;
            margin-top: 18px;
            margin-bottom: 8px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }}

        .toc-item {{
            display: flex;
            justify-content: space-between;
            padding: 5px 0;
            border-bottom: 1px dotted #cbd5e1;
            font-size: 10pt;
        }}

        .toc-item .name {{
            font-weight: 600;
            color: #2563eb;
        }}

        .toc-item .desc {{
            color: #64748b;
            font-size: 9pt;
        }}

        /* Code Sections */
        .code-section {{
            margin-top: 10px;
        }}

        .file-header {{
            background: #f8fafc;
            border-left: 5px solid #1e40af;
            border-top: 1px solid #e2e8f0;
            border-right: 1px solid #e2e8f0;
            border-bottom: 1px solid #e2e8f0;
            padding: 12px 16px;
            border-radius: 6px 6px 0 0;
            margin-bottom: 0;
        }}

        .file-title {{
            font-size: 13pt;
            font-weight: 700;
            color: #1e40af;
        }}

        .file-path {{
            font-size: 8.5pt;
            color: #475569;
            margin-top: 2px;
        }}

        .file-path code {{
            background: #e2e8f0;
            padding: 2px 6px;
            border-radius: 4px;
            font-family: "Consolas", "Courier New", monospace;
        }}

        .file-desc {{
            font-size: 9pt;
            color: #64748b;
            margin-top: 4px;
            font-style: italic;
        }}

        pre.code-block {{
            background: #0f172a;
            color: #f8fafc;
            padding: 14px 16px;
            border-radius: 0 0 6px 6px;
            font-family: "Consolas", "Cascadia Code", "Courier New", monospace;
            font-size: 7.8pt;
            line-height: 1.38;
            white-space: pre-wrap;
            word-wrap: break-word;
            overflow-x: hidden;
            margin-top: 0;
            border: 1px solid #1e293b;
        }}

        code {{
            font-family: "Consolas", "Cascadia Code", "Courier New", monospace;
        }}
    </style>
</head>
<body>

    <!-- Cover Page -->
    <div class="cover-page page-break">
        <div class="cover-icon">🏛️</div>
        <div class="cover-title">Bank Management System</div>
        <div class="cover-subtitle">Complete Source Code & Project Documentation<br>Core Java Backend, Web Server & HTML5/CSS3/JS Web Application</div>

        <div class="cover-meta">
            <table>
                <tr><td class="label">Project Title:</td><td>Bank Management System Website & CLI</td></tr>
                <tr><td class="label">Technologies:</td><td>Core Java (JDK), HttpServer, REST APIs, HTML5, CSS3, JavaScript</td></tr>
                <tr><td class="label">Target Audience:</td><td>2nd-Year Engineering Students (Java & Web Dev)</td></tr>
                <tr><td class="label">Architecture:</td><td>Client-Server Architecture (REST API + Persistence)</td></tr>
                <tr><td class="label">Port:</td><td>http://localhost:8080</td></tr>
            </table>
        </div>

        <div class="cover-footer">
            Generated from Source Code Repository &bull; Apex National Bank Project
        </div>
    </div>

    <!-- Table of Contents -->
    <div class="toc-container page-break">
        <div class="toc-title">📑 Table of Contents</div>

        <div class="toc-group-title">Part 1: Java Backend & Web Server</div>
        <div class="toc-item"><span class="name">1.1 Account.java</span><span class="desc">Account entity model with balance & PIN methods</span></div>
        <div class="toc-item"><span class="name">1.2 Transaction.java</span><span class="desc">Financial transaction record model</span></div>
        <div class="toc-item"><span class="name">1.3 Bank.java</span><span class="desc">Core business logic engine & transaction processing</span></div>
        <div class="toc-item"><span class="name">1.4 FileHandler.java</span><span class="desc">Text persistence layer for accounts.txt</span></div>
        <div class="toc-item"><span class="name">1.5 Main.java</span><span class="desc">Interactive Console CLI Interface</span></div>
        <div class="toc-item"><span class="name">1.6 BankWebServer.java</span><span class="desc">Lightweight HTTP Server with 7 REST API endpoints</span></div>

        <div class="toc-group-title">Part 2: Web Frontend (HTML5 / CSS3 / JavaScript)</div>
        <div class="toc-item"><span class="name">2.1 index.html</span><span class="desc">Home landing page showcasing bank features</span></div>
        <div class="toc-item"><span class="name">2.2 login.html</span><span class="desc">Customer login page with PIN authentication</span></div>
        <div class="toc-item"><span class="name">2.3 register.html</span><span class="desc">New account registration page</span></div>
        <div class="toc-item"><span class="name">2.4 dashboard.html</span><span class="desc">Customer dashboard with balance & live statement</span></div>
        <div class="toc-item"><span class="name">2.5 transfer.html</span><span class="desc">Inter-account fund transfer screen</span></div>
        <div class="toc-item"><span class="name">2.6 style.css</span><span class="desc">Shared responsive CSS stylesheet</span></div>
        <div class="toc-item"><span class="name">2.7 app.js</span><span class="desc">Frontend REST API integration & session state</span></div>

        <div class="toc-group-title">Part 3: Configuration, Persistence & Documentation</div>
        <div class="toc-item"><span class="name">3.1 run-server.bat</span><span class="desc">Windows one-click batch launcher</span></div>
        <div class="toc-item"><span class="name">3.2 accounts.txt</span><span class="desc">Comma-separated account database</span></div>
        <div class="toc-item"><span class="name">3.3 README.md</span><span class="desc">Full documentation & compilation guide</span></div>
    </div>

    <!-- Code Sections -->
    {''.join(sections_html)}

</body>
</html>
"""

with open(output_html, "w", encoding="utf-8") as f:
    f.write(html_content)

print(f"Generated HTML document at: {output_html}")

# Print to PDF using Headless Chrome or Edge
browser_exec = r"C:\Program Files\Google\Chrome\Application\chrome.exe"
if not os.path.exists(browser_exec):
    browser_exec = r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"

cmd = [
    browser_exec,
    "--headless",
    "--disable-gpu",
    "--no-pdf-header-footer",
    f"--print-to-pdf={output_pdf}",
    output_html
]

print(f"Running command to generate PDF: {' '.join(cmd)}")
res = subprocess.run(cmd, capture_output=True, text=True)
print(f"Return code: {res.returncode}")
if os.path.exists(output_pdf):
    print(f"SUCCESS: PDF generated at: {output_pdf} (Size: {os.path.getsize(output_pdf)} bytes)")
else:
    print("FAILED to create PDF:", res.stderr)
