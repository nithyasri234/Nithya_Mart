# NithyaMart — Multi-Seller E-Commerce Marketplace
### Anna University R2025 Regulations — Semester 3 Project Milestone Manual
**Builder:** Solo Student Development  
**Execution Window:** July 27 – October 10, 2026  
**Tech Stack:** Java Servlets, JSP/JSTL, HikariCP, H2 Embedded Database Engine, Maven

---

## 1. Problem Statement & Scope Definition
YourNameMart is an industrial, multi-seller marketplace architecture engineered without bloated high-level frameworks to demonstrate mastery over primitive web container lifecycles. 
* **Sellers** can securely authenticate, maintain inventory catalogs, track costs, and evaluate product margins.
* **Buyers** can search catalog nodes dynamically, build active shopping cart buffers, and place orders through simulated checkout channels.
* **Admins** oversee global platform operations via structured seed account parameters, retaining master control to audit user accounts or moderate illegal product listings.

### Architecture Scope Boundaries
* **No Real-Time Hooks:** WebSockets or third-party mapping APIs are excluded.
* **Isolated Checkout Framework:** Third-party payment gateway endpoints are bypassed; the application features a mock transaction confirmation ledger processing pipeline.

---

## 2. Technical Stack Specifications

| Component Layer | Technology Specification | Abstraction Framework |
| :--- | :--- | :--- |
| **Language Target** | JDK 17 (Long-Term Support) | Java Platform Core |
| **Servlet Container** | Apache Tomcat 9.0.x | `javax.servlet.*` Native Library API |
| **Build Dependency** | Apache Maven Management Suite | Automated `pom.xml` Build Pipeline |
| **Database Engine** | H2 Database (Server/Local Embedded) | Relational SQL Architecture |
| **Connection Pooling** | HikariCP Vector Pool | Shared Context Lifecycle Lifecycle Listener |
| **View Architecture** | JavaServer Pages (JSP) + JSTL | Server-Side Rendering Framework |
| **Cryptography** | jBCrypt Security Engine | Adaptive Salt Password Hashing |

---

## 3. Mandatory Engineering Security Compliance Checklist
1. **Parameterized Query Enforcements:** Every data interaction utilizes `PreparedStatement` layers exclusively. String-concatenated queries are strictly banned to eliminate SQL Injection (SQLi) vectors.
2. **One-Way Cryptographic Hashing:** Raw plain passwords never touch the database ledger. Fields are transformed via `jBCrypt` with an explicit work factor allocation pool of `12`.
3. **Automated Resource Management:** All database statements are enclosed inside Java `try-with-resources` statements to guarantee the cleanup of connections and eliminate memory leaks.
4. **Session Fixation Defenses:** Call routines call `.invalidate()` explicitly upon valid login authentication before provisioning fresh `HttpSession` tokens.
5. **Cross-Site Scripting (XSS) Mitigation:** User outputs rendered within the presentation layout view layer are tightly processed using JSTL `<c:out>` expression handlers to sanitize raw text elements.

---

## 4. Setup, Build, and Execution Protocols

### Prerequisites
* Java Development Kit (JDK 17) configured in system environment variables.
* VS Code with **Extension Pack for Java** and **Community Server Connectors** extensions installed.

### Step 1: Clone and Clean Compile Lifecycle
Navigate to the root directory where your `pom.xml` file is located and compile the web package cleanly using Maven:
```bash
mvn clean package
```
This triggers Maven to download all required dependencies, verify class compilation paths, and build a deployable `studentmart.war` distribution file inside the target folder structure.

### Step 2: Establish the Local Server Target inside VS Code
1. Open the **Servers** tab on your VS Code sidebar panel.
2. Select **Create New Server** -> **Apache Tomcat** and point the target path directly to the root installation folder directory of your local Tomcat installation.
3. Right-click the newly mapped server instance and select **Start Server**.
4. Right-click the active running server instance, choose **Add Deployment**, and select the project's compiled `webapp/` or `target/studentmart/` folder.

### Step 3: Run the Application
Open your web browser and navigate to the local deployment endpoint:
```text
http://localhost:8080/studentmart/
```
* **Default Admin Credentials for System Audits:** 
  * **Email Username:** `admin@mart.com`
  * **Plain Password:** `Admin@123`

---

## 5. System Design Architecture Blueprint
The application follows a clean **Layered Model-View-Controller (MVC)** architectural design layout:
```text
[ Browser View Layer ] ◄── Uses Native HTML/CSS forms & JSP Server Rendering
         │
         ▼ (HTTP POST/GET Requests processed down)
[ Filter Security Gate ] ◄── AuthFilter blocks unauthenticated resource traffic
         │
         ▼ (Thin Orchestration Layer)
[ Controller Servlets ] ◄── Captures request parameters; dispatches tasks to Service
         │
         ▼ (Isolated Business Rules Engine)
[ Service Layer Core ] ◄── Evaluates validation constraints (No SQL allowed here)
         │
         ▼ (Data Access Level Abstraction)
[ Data Access Objects ] ◄── Executes query tasks via Parametric PreparedStatements
         │
         ▼ (Borrowed Pool Token Mapping)
[ HikariCP Data Pool ] ◄── Manages data connection arrays securely over H2 Database
```
