# 🛒 NithyaMart — Multi-Seller E-Commerce Marketplace

[![Java Version](https://shields.io)](https://oracle.com)
[![Servlet Container](https://shields.io)](https://apache.org)
[![Database](https://shields.io)](https://h2database.com)
[![Build Management](https://shields.io)](https://apache.org)  

---

## 📝 Project Overview

**NithyaMart** is an industrial-grade, multi-seller marketplace architecture engineered completely from scratch without high-level enterprise frameworks (like Spring or Hibernate). The core objective of this project is to demonstrate exhaustive mastery over primitive web container lifecycles, low-level HTTP transaction states, and native relational database mappings.

### 👥 System Role Capabilities
* 💼 **Sellers:** Authenticate securely, manage real-time inventory catalogs, track wholesale costs, and accurately evaluate product margins.
* 🛍️ **Buyers:** Dynamically browse catalog nodes, build active shopping cart buffers, and place orders through simulated checkout channels.
* 🔑 **Admins:** Oversee global system health via structured master seed parameters, retaining root permissions to audit user accounts or moderate platform product listings.

### 🚫 Architectural Boundaries
To ensure strict alignment with course guidelines, the project explicitly enforces the following constraints:
* **No Real-Time Hooks:** WebSockets, external messaging streams, or third-party mapping APIs are excluded.
* **Isolated Checkout Framework:** Third-party payment gateway endpoints are bypassed in favor of a fast, local mock transaction confirmation ledger processing pipeline.

---

## 🛠️ Technical Stack Specifications

| Component Layer | Technology Specification | Abstraction Framework |
| :--- | :--- | :--- |
| **Language Target** | JDK 17 (Long-Term Support) | Java Platform Core |
| **Servlet Container** | Apache Tomcat 9.0.x | `javax.servlet.*` Native Library API |
| **Build Dependency** | Apache Maven Management Suite | Automated `pom.xml` Build Pipeline |
| **Database Engine** | H2 Database (Server/Local Embedded) | Relational SQL Architecture |
| **Connection Pooling** | HikariCP Vector Pool | Shared Context Lifecycle Listener |
| **View Architecture** | JavaServer Pages (JSP) + JSTL | Server-Side Rendering Framework |
| **Cryptography** | jBCrypt Security Engine | Adaptive Salt Password Hashing |

---

## 🛡️ Mandatory Engineering Security Compliance

To pass academic rigorous auditing, the system implements a zero-trust compliance baseline directly at the low-level data and presentation layer:

- **Parameterized Query Enforcements:** Every single database interaction utilizes `PreparedStatement` layers exclusively. String-concatenated query formats are strictly banned to completely eliminate SQL Injection (SQLi) vectors.
- **One-Way Cryptographic Hashing:** Raw plain passwords never touch the database ledger. Values are securely transformed via `jBCrypt` utilizing an explicit work factor allocation pool of `12`.
- **Automated Resource Management:** All database statements, result sets, and pool connections are tightly enclosed inside Java `try-with-resources` statements to guarantee complete resource cleanup and eliminate memory leaks.
- **Session Fixation Defenses:** Authentication routines call `.invalidate()` explicitly upon valid login validation before provisioning completely fresh `HttpSession` tokens to the client.
- **Cross-Site Scripting (XSS) Mitigation:** User outputs rendered within the presentation layout view layer are tightly processed using JSTL `<c:out>` expression handlers to escape and sanitize raw text elements.

---

## 📐 System Design Architecture Blueprint

The application follows a clean, decoupled **Layered Model-View-Controller (MVC)** architectural design flow:

```text
  [ Browser View Layer ]
         │ (Uses Native HTML/CSS forms & JSP Server Side Rendering)
         ▼ 
  [ Filter Security Gate ]
         │ (AuthFilter blocks unauthenticated resource traffic)
         ▼ 
  [ Controller Servlets ]
         │ (Captures request parameters; dispatches tasks to Service)
         ▼ 
  [ Service Layer Core ]
         │ (Evaluates validation business rules; no SQL permitted here)
         ▼ 
  [ Data Access Objects ]
         │ (Executes query tasks via Parametric PreparedStatements)
         ▼ 
  [ HikariCP Data Pool ] ──► [ H2 Embedded Database Engine ]
```

---

## 🚀 Setup, Build, and Execution Protocols

### 📋 Prerequisites
* **Java Development Kit (JDK 17)** configured cleanly in system environment variables.
* **VS Code** with both the **Extension Pack for Java** and **Community Server Connectors** extensions active.

### Step 1: Clone and Clean Compile Lifecycle
Navigate directly to the root directory where your `pom.xml` file is located and compile the web package cleanly using the Maven suite:
```bash
mvn clean package
```
*This triggers Maven to download all required dependencies, verify class compilation paths, and build a deployable `studentmart.war` distribution file inside the target folder structure.*

### Step 2: Establish the Local Server Target inside VS Code
1. Open the **Servers** tab on your VS Code sidebar panel.
2. Select **Create New Server** ──► **Apache Tomcat** and point the target path directly to the root installation folder directory of your local Tomcat installation.
3. Right-click the newly mapped server instance and select **Start Server**.
4. Right-click the active running server instance, choose **Add Deployment**, and select the project's compiled `webapp/` or `target/NithyaMart/` folder.

### Step 3: Run the Application
Open your web browser and navigate to the local deployment endpoint:
```text
http://localhost:8080/NithyaMart/
```

#### 🔐 Default Admin Credentials for System Audits:
* **Email Username:** `admin@mart.com`
* **Plain Password:** `Admin@123`
