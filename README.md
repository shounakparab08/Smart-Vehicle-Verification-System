# 🛡️ Smart Vehicle Verification & Incident Reporting System (VVS)

[![Java Version](https://img.shields.io/badge/Java-21-orange.svg?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![MongoDB](https://img.shields.io/badge/MongoDB-Atlas-green.svg?style=for-the-badge&logo=mongodb&logoColor=white)](https://www.mongodb.com/)
[![Tomcat](https://img.shields.io/badge/Tomcat-10.1-blue.svg?style=for-the-badge&logo=apachetomcat&logoColor=white)](https://tomcat.apache.org/)
[![UI Design](https://img.shields.io/badge/UI-Glassmorphism%20Dark-purple.svg?style=for-the-badge)](https://developer.mozilla.org/en-US/docs/Web/CSS)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

A state-of-the-art, secure **Java Web Application** designed for digital vehicle registration verification, public traffic violation/incident reporting, and real-time administrative oversight. Engineered with an embedded Tomcat microserver architecture and MongoDB Atlas cloud storage, the system features a premium glassmorphic dark-theme user experience.

---

## 🚀 Key Features

### 👤 Customer & Test User Portal
*   **Secure Authentication**: Multi-role identity management featuring robust session persistence.
*   **Incident Reporting**: Dynamic forms to file incident reports (e.g., speeding, wrong-side driving, illegal parking) with description and vehicle details.
*   **My Registered Vehicles**: View details of your vehicles, insurance validity status, and security standing.
*   **Live Notifications**: Real-time interactive in-app notification dropdown for tracking verification alerts.
*   **Verification History**: Track the status of filed reports (Pending ⏳, Resolved ✅, Rejected ❌).
*   **Account Controls**: Instantly detects and warns if the user account is frozen due to pending penalty fines.

### 🛡️ Administrative Console
*   **Unified Analytics Dashboard**: High-level status monitors displaying total users, active reports, pending reviews, and blacklisted vehicles.
*   **Report Review System**: Advanced incident verification flow allowing admins to accept, reject, or mark reports in-progress.
*   **Vehicle Database Explorer**: Complete list of all vehicles with live status indicators.
*   **Blacklist Management**: Direct toggle switches to secure the streets by blacklisting vehicles and freezing user account privileges.
*   **User Profiles Control**: Review user registration metrics and safely delete bad actors.

---

## 🛠️ Technology Stack

| Component | Technology | Description |
| :--- | :--- | :--- |
| **Backend Core** | `Java 21 (JDK)` | Enterprise-grade compilation & performance optimization. |
| **Server Engine** | `Embedded Tomcat 10.1` | Lightweight, scalable microservice web server. |
| **Database** | `MongoDB Atlas` | High-availability cloud Document Database. |
| **Web API Specification** | `Jakarta Servlet 6.0` | Secure RESTful API routing endpoints. |
| **Serialization** | `Google GSON 2.10` | Highly efficient JSON serialization/deserialization. |
| **Styling Framework** | `Vanilla HSL CSS3` | Modern glassmorphic theme with custom variables. |
| **Authentication** | `Firebase Mock Identity` | Safe and stable token-based authentication engine. |

---

## 📂 Project Directory Structure

```
c:\Users\SHOUNAK\Desktop\java\
├── .vscode/             # Local IDE Workspace Settings
├── pom.xml              # Project dependencies & build instructions
├── run.bat              # Autopilot start & compilation script
├── .gitignore           # Exclusions for clean version control
└── src/
    └── main/
        ├── java/com/vehicleverify/
        │   ├── controller/      # REST API Controllers (Servlets)
        │   ├── database/        # Cloud MongoDB client connection pool
        │   ├── filter/          # Request Interceptors (CORS, Auth)
        │   ├── main/            # Application entry point & Tomcat launcher
        │   ├── model/           # Data entity representations (POJOs)
        │   ├── service/         # Enterprise business logic classes
        │   └── util/            # Helpers (JSON Util, Database Seeder)
        ├── resources/           # Database properties config
        └── webapp/              # Glassmorphic frontend views (HTML, CSS, JS)
```

---

## 📦 Getting Started & Installation

### 📋 Prerequisites
*   **Java JDK 17 or 21** installed and configured in your path.
*   **Apache Maven 3.9+** installed.
*   **MongoDB Atlas Cloud Cluster** or **Local MongoDB Instance**.

### 🔧 Configuration
1. Open the [application.properties](file:///c:/Users/SHOUNAK/Desktop/java/src/main/resources/application.properties) file and configure your credentials:
   ```properties
   mongodb.uri=mongodb+srv://your_username:your_password@cluster.mongodb.net/vehicleDB
   mongodb.database=vehicleDB
   server.port=8080
   ```

### ⚡ Launching the Application
You can compile and launch the entire application with a single double-click using the autopilot startup script:
1. Double-click the `run.bat` file in the root directory.
2. The script will automatically compile, boot the embedded Tomcat server on port `8080`, and open your browser automatically to `http://localhost:8080/`.

*Alternatively, launch via terminal:*
```bash
# Clean, compile, and execute in-place
mvn clean compile exec:java -Dexec.mainClass="com.vehicleverify.main.MainServer"
```

---

## 🌱 Database Seeding (Optional)
To pre-populate the database with clean, realistic mockup customer accounts, registered vehicles, and incident reports:
```bash
mvn exec:java -Dexec.mainClass="com.vehicleverify.util.DatabaseSeeder"
```

---

## 📄 License
Distributed under the MIT License. See `LICENSE` for more information.

---

Developed with ❤️ by **[shounakparab08](https://github.com/shounakparab08)**.
