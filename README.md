# BrewHub - Your Brewing Central for Hobbyists

BrewHub is a web application designed to enhance the connection between hobbyist brewers and retailers, focusing on improving both customer relations and supply chain efficiency. The platform addresses a significant gap in the home brewing community where hobbyist brewers often struggle with selecting appropriate ingredients and determining proper quantities for their brewing projects. This challenge affects both the sustainability of their hobby and retailers' ability to maintain effective inventory management.

- **Frontend Repository:** [BrewHub Frontend](https://github.com/PeterKock/brewhub-frontend)
- **Backend Repository:** [BrewHub Backend](https://github.com/PeterKock/brewhub-backend)

---

## Features
### User-Facing Features
✅ **Authentication** – Secure login and role-based access control using JWT.  
✅ **Order Management** – Users can browse brewing ingredients, place orders, and track their order history and status.  
✅ **Recipes & Guides** – Users can explore brewing recipes and guides.  
✅ **Community Forum** – A dedicated space for users to ask brewing-related questions and share knowledge.  
✅ **Retailer Ratings & Reviews** – Users can view and select different retailers based on their average ratings.

### Retailer-Specific Features
✅ **Authentication** – Secure login and role-based access control using JWT.  
✅ **Inventory Management** – Retailers can track stock, import/export inventory, and receive low-stock alerts.  
✅ **CSV Import/Export** – Efficient bulk inventory updates using CSV files.  
✅ **Community Forum Participation** – Retailers can share knowledge and answer customer questions.  
✅ **Order Overview** – Retailers can view and manage incoming orders, update statuses, and track past transactions.

### Moderator-Specific Features
✅ **Authentication** – Secure login and role-based access control using JWT.  
✅ **Community Forum Participation** – Moderators can ask and answer brewing-related questions.  
✅ **Reporting System** – Moderators handle reports submitted by users and retailers for inappropriate content.  
✅ **Recipe & Guide Management** – Moderators can create, edit, and delete brewing recipes and guides.

---

## Requirements
To install and run the application, ensure the following software and configurations are present.

### **System Requirements**
| Component            | Minimum Requirements                                 | Recommended Requirements                                   |
|----------------------|------------------------------------------------------|------------------------------------------------------------|
| **Operating System** | Windows 10/11 (64-bit), macOS 12+, Ubuntu 20.04+     | Windows 11, macOS Ventura+, Ubuntu 22.04+                  |
| **RAM**              | 16GB                                                 | 32GB                                                       |
| **Processor**        | Intel Core i5/i7 (10th gen or newer) / AMD Ryzen 5/7 | Intel Core i9 / AMD Ryzen 9 for multitasking               |
| **Storage**          | 256GB SSD                                            | 512GB SSD                                                  |
| **Display**          | 1920x1080 resolution                                 | Multi-monitor setup recommended                            |
| **Graphics**         | Integrated GPU is sufficient                         | Dedicated GPU not required unless for graphics-heavy tasks |
| **Internet**         | Stable broadband (50+ Mbps)                          | High-speed fiber recommended                               |
| **Free Space**       | At least 20GB available for project files            | More for database & future scaling                         |

---

### **Software Requirements**
The following tools are required to develop and run **BrewHub**:

| Software                       | Purpose                                                                                 |
|--------------------------------|-----------------------------------------------------------------------------------------|
| **IntelliJ IDEA**              | Recommended IDE for backend development (Spring Boot).                                  |
| **WebStorm**                   | Recommended IDE for frontend development (React).                                       |
| **Maven**                      | Manages dependencies and builds for the backend.                                        |
| **Java 17**                    | Stable long-term support (LTS) version required for backend.                            |
| **OpenJDK 22**                 | Latest Java version for testing advanced features.                                      |
| **Node.js (22.1.0 or higher)** | Required for frontend dependency management and development.                            |
| **PostgreSQL & pgAdmin**       | PostgreSQL is the primary database for BrewHub. pgAdmin is optional for GUI management. |

---
