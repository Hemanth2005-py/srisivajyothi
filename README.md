# MYMART — Full-Stack Supermarket E-Commerce Platform

A production-grade, full-stack supermarket e-commerce web platform inspired by **Blinkit**, built with a **Java 21 & Spring Boot 3.3** backend, **MongoDB** database, and a high-performance, responsive **HTML5 / CSS3 / Vanilla JavaScript / Bootstrap 5.3** frontend.

MYMART delivers an ultra-fast grocery shopping experience with real-time stock verification, backend-authoritative pricing, downloadable PDF tax invoices, multi-format business analytics (CSV, Excel XLSX, OpenPDF), and a dual-storage inventory architecture with independent tracking for **Main** and **store** depots.

---

## 🌟 Key Features

### 🛒 Customer Storefront
- **Blinkit-Inspired Visual Aesthetics:** High-contrast fresh green palette, dark/light theme switcher, animated banners, and micro-interactions.
- **Instant Product Catalog:** Real-time search, brand & category filters, price slider, in-stock and on-sale toggles, and sorting (price, discount, rating).
- **Product Details & Gallery:** High-resolution product images, thumbnail switcher, stock status badges, unit metrics (e.g. 500g, 1L), and nutritional/storage information.
- **Smart Cart & Pricing Engine:**
  - Live quantity modification (`+` and `-` controls).
  - Free delivery milestone progress bar (Free shipping on orders above ₹299).
  - Server-calculated taxes (5% GST) and delivery fees.
- **Frictionless Checkout:** Customer address form, 10-digit phone and 6-digit PIN validation, Cash on Delivery (COD) mode, and instant order placement modal.
- **Order Tracking & Printable Invoices:** Order status progress badges (`PENDING`, `CONFIRMED`, `PACKED`, `OUT_FOR_DELIVERY`, `DELIVERED`, `CANCELLED`) with one-click OpenPDF printable tax invoice bill generation.

### 🏢 Dual Independent Inventory Control (Strict "Main" vs "store")
- **Independent Location Balances:** Products are separately accounted for in two isolated storage depots:
  - **`Main`**: Central bulk storage / warehouse depot.
  - **`store`**: Supermarket retail shop / active picking shelves.
- **Stock Operations:**
  - **Stock In:** Receive incoming vendor stock directly into either `Main` or `store`.
  - **Stock Transfer:** Transfer stock between `Main` and `store` with atomic debit/credit balance verification.
  - **Audit Adjustment:** Log discrepancy corrections with admin audit notes.
  - **Audit Trail (Stock Movements):** Chronological movement ledger recording timestamp, operator, location, delta quantity, previous balance, and reason.

### 📊 Admin Operations & Business Analytics
- **Executive Dashboard:** Live KPI cards for total revenue, active orders, product counts, and stock alarms.
- **Interactive Visualizations (Chart.js):** 6-month monthly revenue trend line chart and category sales volume doughnut chart.
- **Catalog Management:** Full CRUD for Products, Categories, and Brands with image URL support.
- **Order Fulfillment Center:** Searchable customer orders table, status transitions, and admin invoice generation.
- **One-Click Business Reports:**
  - **CSV Export:** RFC 4180 compliant CSV export for spreadsheets and data warehouses.
  - **Excel XLSX Export:** Stylized Apache POI workbook with auto-fitted columns, header palettes, and numeric formatting.
  - **PDF Executive Summary:** OpenPDF landscape report with business header, revenue summary table, and generation timestamp.

---

## 🏗️ Technology Architecture

| Layer | Technologies |
| :--- | :--- |
| **Backend Framework** | Java 21, Spring Boot 3.3.5, Spring Web MVC, Spring Security 6, Spring Data MongoDB, Bean Validation (Hibernate Validator) |
| **Database** | MongoDB (v7.0+ / v8.0+ / Atlas) |
| **Documentation** | OpenAPI 3.0 / Swagger UI via `springdoc-openapi-starter-webmvc-ui:2.6.0` |
| **PDF Generation** | OpenPDF 2.0.3 |
| **Spreadsheet Generation** | Apache POI 5.3.0 (`poi-ooxml`), OpenCSV 5.9 |
| **Authentication** | JWT (JSON Web Tokens via `jjwt:0.12.6`), Firebase Admin SDK 9.3.0 |
| **Frontend** | HTML5, CSS3, Modern ES6+ JavaScript, Bootstrap 5.3.3, Bootstrap Icons, Chart.js 4.4 |
| **Build & Tooling** | Apache Maven 3.9+, Docker (multi-stage) |

---

## ⚡ Quick Start & Local Preview

The backend automatically packages and serves all frontend pages from `classpath:/static/` on port **8080**.

### 1. Prerequisites
- **JDK 21** installed (`java -version`)
- **MongoDB** running locally on port 27017 (`brew services start mongodb-community`)
- **Maven 3.9+** (`mvn -v`)

### 2. Run the Application

```bash
# Build & package production JAR
cd backend
mvn clean package -DskipTests

# Start the application
java -jar target/mymart-backend-1.0.0.jar
```

The database initializes automatically on startup with demo products, brands, categories, dual inventory records, and 35 historical orders across the past 6 months.

---

## 🌐 Live Preview Links & Navigation

Once the server is running on `http://localhost:8080`:

| Page | URL | Description |
| :--- | :--- | :--- |
| **Customer Storefront** | [`http://localhost:8080/index.html`](http://localhost:8080/index.html) | Home page with hero carousel, categories, flash deals, and bestsellers |
| **Product Catalog** | [`http://localhost:8080/products.html`](http://localhost:8080/products.html) | Search, category & brand filtering, price sliders, sorting |
| **Shopping Cart** | [`http://localhost:8080/cart.html`](http://localhost:8080/cart.html) | Live cart items, +/- controls, free delivery progress bar |
| **Checkout** | [`http://localhost:8080/checkout.html`](http://localhost:8080/checkout.html) | Address details, order placement with COD |
| **Order History** | [`http://localhost:8080/order-history.html`](http://localhost:8080/order-history.html) | Order tracking & one-click printable PDF tax invoices |
| **Admin Login** | [`http://localhost:8080/admin-login.html`](http://localhost:8080/admin-login.html) | Admin portal authentication with 1-click demo button |
| **Admin Dashboard** | [`http://localhost:8080/admin-dashboard.html`](http://localhost:8080/admin-dashboard.html) | KPIs, monthly revenue chart, category breakdown chart |
| **Admin Inventory** | [`http://localhost:8080/admin-inventory.html`](http://localhost:8080/admin-inventory.html) | **Main** vs **store** stock control, transfers, adjustments |
| **Admin Products** | [`http://localhost:8080/admin-products.html`](http://localhost:8080/admin-products.html) | Product catalog management (create, edit, delete) |
| **Admin Orders** | [`http://localhost:8080/admin-orders.html`](http://localhost:8080/admin-orders.html) | Order fulfillment & status updates |
| **Admin Reports** | [`http://localhost:8080/admin-reports.html`](http://localhost:8080/admin-reports.html) | Downloadable CSV, Excel (.xlsx), and PDF reports |
| **Swagger UI** | [`http://localhost:8080/swagger-ui.html`](http://localhost:8080/swagger-ui.html) | Interactive OpenAPI 3.0 REST API documentation |

---

## 🔑 Accounts & Credentials

| Role | Email / Username | Password | Access Rights |
| :--- | :--- | :--- | :--- |
| **Customer** | `customer@mymart.com` | `Customer@12345` | Shopping, cart, checkout, profile, my orders, PDF bills |
| **Admin** | `srisivajyothi@ssj` *(or `srisivajyothi`)* | `SSJ@SSJ` | Full administrative dashboard, inventory, products, orders, report exports |

---

## 🛡️ API Endpoints Summary

### Public Endpoints
- `POST /api/auth/register` — Customer registration
- `POST /api/auth/login` — Customer and Admin authentication (JWT)
- `GET /api/products` — Product catalog with filtering, search, and pagination
- `GET /api/products/{id}` — Single product details
- `GET /api/categories` — Category list
- `GET /api/brands` — Brand list

### Customer Endpoints (`ROLE_CUSTOMER` / Authenticated)
- `GET /api/cart` — Fetch customer active cart
- `POST /api/cart/items` — Add item to cart
- `PUT /api/cart/items/{productId}` — Update item quantity
- `DELETE /api/cart/items/{productId}` — Remove item
- `POST /api/orders` — Place order (backend-calculated totals)
- `GET /api/orders/my` — Customer order history
- `GET /api/orders/{orderNumber}/bill` — Download PDF tax invoice bill

### Admin Endpoints (`ROLE_ADMIN`)
- `GET /api/admin/inventory` — Stock overview across **Main** and **store**
- `POST /api/admin/inventory/stock-in` — Add stock to **Main** or **store**
- `POST /api/admin/inventory/transfers` — Move stock between **Main** and **store**
- `POST /api/admin/inventory/adjustments` — Correct inventory count with audit note
- `GET /api/admin/inventory/movements` — Audit trail log of all stock movements
- `GET /api/admin/analytics/overview` — High-level KPI metrics & chart data
- `GET /api/admin/reports/sales?format={csv|xlsx|pdf}` — Download filtered sales reports

---

## ☁️ Production Deployment

### MongoDB Atlas Configuration
In `application.yml` or via environment variable:
```bash
export SPRING_DATA_MONGODB_URI="mongodb+srv://<username>:<password>@cluster0.mongodb.net/mymart?retryWrites=true&w=majority"
```

### Firebase Authentication Setup
Provide either the service account path or raw JSON via environment variables:
```bash
export FIREBASE_ENABLED=true
export FIREBASE_CREDENTIALS_JSON='{"type":"service_account",...}'
# or
export FIREBASE_CREDENTIALS_PATH="/path/to/firebase-service-account.json"
```

### Docker Deployment
The project includes a multi-stage Docker build:
```bash
docker build -t mymart-backend:latest .
docker run -p 8080:8080 -e SPRING_DATA_MONGODB_URI="mongodb://host.docker.internal:27017/mymart" mymart-backend:latest
```
