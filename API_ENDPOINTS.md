# Notes Marketplace - API Endpoints Documentation

## Table of Contents
- [Web Pages (WebController)](#web-pages-webcontroller)
- [Authentication API (AuthController)](#authentication-api-authcontroller)
- [Buyer API (BuyerController)](#buyer-api-buyercontroller)
- [Seller API (SellerNoteController)](#seller-api-sellernotecontroller)
- [Payment API (PaymentController)](#payment-api-paymentcontroller)

---

## Web Pages (WebController)

### Authentication Pages

| Method | Route | Description | Template | Access |
|--------|-------|-------------|----------|---------|
| GET | `/` | Home - Redirects to login | - | Public |
| GET | `/login` | Login page | `auth/login.html` | Public |
| GET | `/register` | Registration page | `auth/register.html` | Public |
| POST | `/register` | Handle registration form | Redirects to `/login?registered=true` | Public |

**Registration Request Parameters:**
- `name` (String, required)
- `email` (String, required)
- `password` (String, required)
- `role` (String, required) - "BUYER" or "SELLER" only

---

### Admin Pages

| Method | Route | Description | Template | Access |
|--------|-------|-------------|----------|---------|
| GET | `/admin/dashboard` | Admin dashboard | `admin/admin-dashboard.html` | Admin Only |

---

### Seller Pages

| Method | Route | Description | Template | Access |
|--------|-------|-------------|----------|---------|
| GET | `/seller/dashboard` | Seller dashboard | `seller/seller-dashboard.html` | Seller Only |
| GET | `/seller/notes` | Seller notes list | `seller/seller-notes.html` | Seller Only |
| GET | `/seller/upload-note` | Upload note form | `seller/seller-upload-note.html` | Seller Only |

---

### Buyer Pages

| Method | Route | Description | Template | Access |
|--------|-------|-------------|----------|---------|
| GET | `/buyer/dashboard` | Buyer dashboard | `buyer/buyer-dashboard.html` | Buyer Only |
| GET | `/buyer/browse` | Browse notes | `buyer/buyer-browse.html` | Buyer Only |
| GET | `/buyer/my-downloads` | My purchased notes | `buyer/buyer-downloads.html` | Buyer Only |
| GET | `/buyer/orders` | Order history | `buyer/buyer-orders.html` | Buyer Only |

---

### Payment Pages

| Method | Route | Description | Template | Access |
|--------|-------|-------------|----------|---------|
| GET | `/payment/success` | Payment success page | `payment/payment-success.html` | Public |
| GET | `/payment/failed` | Payment failed page | `payment/payment-failed.html` | Public |
| GET | `/payment/cancelled` | Payment cancelled page | `payment/payment-cancelled.html` | Public |

---

## Authentication API (AuthController)

**Base URL:** `/api/auth`

### 1. Register User
```
POST /api/auth/register
```
**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": "BUYER"
}
```
**Response:** `201 Created`
```json
{
  "message": "User registered successfully"
}
```

---

### 2. Login
```
POST /api/auth/login
```
**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```
**Response:** `200 OK`
```json
{
  "message": "Login successful"
}
```

---

### 3. Logout
```
POST /api/auth/logout
```
**Response:** `200 OK`
```json
{
  "message": "Logout successful"
}
```

---

## Buyer API (BuyerController)

**Base URL:** `/api/buyer`  
**Access:** Requires BUYER role

### 1. Browse All Notes
```
GET /api/buyer/notes
```
**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "title": "Calculus Notes",
    "description": "Complete calculus notes",
    "category": "Mathematics",
    "price": 15.99,
    "sellerName": "John Doe",
    "previewImageUrl": "https://...",
    "createdAt": "2026-03-13T10:00:00"
  }
]
```

---

### 2. Search Notes
```
GET /api/buyer/notes/search?keyword={keyword}
```
**Query Parameters:**
- `keyword` (String, required) - Search in title or description

**Response:** `200 OK` - Returns array of matching NoteDto objects

---

### 3. Filter Notes by Category
```
GET /api/buyer/notes/filter?category={category}
```
**Query Parameters:**
- `category` (String, required) - Category name to filter

**Response:** `200 OK` - Returns array of filtered NoteDto objects

---

### 4. Get Note Details
```
GET /api/buyer/notes/{id}
```
**Response:** `200 OK` - Returns single NoteDto object

---

### 5. Get Preview Image
```
GET /api/buyer/notes/{id}/preview
```
**Response:** `200 OK`
```json
{
  "previewImageUrl": "https://...",
  "title": "Calculus Notes",
  "category": "Mathematics",
  "description": "Complete calculus notes",
  "price": 15.99
}
```

---

### 6. View Full PDF (Purchased Only)
```
GET /api/buyer/notes/{id}/full-preview
```
**Access:** Must have purchased the note  
**Response:** `200 OK` - Returns PDF file (Content-Type: application/pdf, inline)  
**Error:** `403 Forbidden` - If note not purchased

---

### 7. Download PDF (Purchased Only)
```
GET /api/buyer/notes/{id}/download
```
**Access:** Must have purchased the note  
**Response:** `200 OK` - Returns PDF file (Content-Type: application/pdf, attachment)  
**Error:** `403 Forbidden` - If note not purchased

---

### 8. Get My Orders
```
GET /api/buyer/my-orders
```
**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "buyerId": 3,
    "totalPrice": 15.99,
    "orderDate": "2026-03-13T10:00:00",
    "transactionId": "SSLCZtxn123456",
    "orderItems": [
      {
        "id": 1,
        "noteId": 12,
        "noteTitle": "Calculus Notes",
        "price": 15.99
      }
    ]
  }
]
```

---

### 9. Get My Purchased Notes
```
GET /api/buyer/my-downloads
```
**Response:** `200 OK` - Returns array of Note objects

---

### 10. Check if Note is Purchased
```
GET /api/buyer/purchased/{noteId}
```
**Response:** `200 OK`
```json
{
  "purchased": true
}
```

---

### 11. Get Buyer Statistics
```
GET /api/buyer/stats
```
**Response:** `200 OK`
```json
{
  "totalPurchases": 5,
  "totalSpent": 75.50,
  "notesAvailable": 150
}
```

---

## Seller API (SellerNoteController)

**Base URL:** `/seller/notes`  
**Access:** Requires SELLER role

### 1. Upload Note
```
POST /seller/notes/upload
```
**Content-Type:** `multipart/form-data`

**Request Parameters:**
- `title` (String, required)
- `description` (String, required)
- `category` (String, required)
- `price` (Double, required)
- `pdfFile` (File, required) - PDF file
- `previewFile` (File, required) - Image file for preview

**Response:** `200 OK` - Returns created Note object

---

### 2. Get My Notes
```
GET /seller/notes/my-notes
```
**Response:** `200 OK` - Returns array of seller's Note objects

---

### 3. Update Note
```
PUT /seller/notes/{id}
```
**Request Body:**
```json
{
  "title": "Updated Title",
  "description": "Updated description",
  "category": "Updated Category",
  "price": 19.99
}
```
**Response:** `200 OK` - Returns updated Note object

---

### 4. Delete Note
```
DELETE /seller/notes/{id}
```
**Response:** `200 OK`
```json
"Note deleted successfully"
```

---

### 5. View Own Note PDF
```
GET /seller/notes/view/{id}
```
**Access:** Only seller who owns the note  
**Response:** `200 OK` - Returns PDF file (Content-Type: application/pdf)

---

## Payment API (PaymentController)

**Base URL:** `/api/payment`

### 1. Create Payment Session
```
POST /api/payment/pay
```
**Request Body:**
```json
{
  "noteId": 12
}
```
**Response:** `200 OK`
```json
"https://sandbox.sslcommerz.com/gwprocess/v4/gw.php?Q=..."
```

---

### 2. Payment Success Callback (SSLCommerz)
```
POST /api/payment/success?tran_id={tran_id}&status={status}
```
**Access:** Called by SSLCommerz  
**Response:** Redirects to `/payment/success?tran_id={tran_id}`

---

### 3. Payment Failure Callback (SSLCommerz)
```
POST /api/payment/fail?tran_id={tran_id}
```
**Access:** Called by SSLCommerz  
**Response:** Redirects to `/payment/failed?tran_id={tran_id}`

---

### 4. Payment Cancel Callback (SSLCommerz)
```
POST /api/payment/cancel?tran_id={tran_id}
```
**Access:** Called by SSLCommerz  
**Response:** Redirects to `/payment/cancelled?tran_id={tran_id}`

---

## Security Configuration

### Public Routes (No Authentication Required)
- `/` - Home page
- `/login` - Login page
- `/register` - Registration page
- `/api/auth/**` - Authentication endpoints
- `/payment/**` - Payment callback pages
- `/api/payment/**` - Payment endpoints

### Protected Routes
- `/admin/**` - Requires `ROLE_ADMIN`
- `/seller/**` - Requires `ROLE_SELLER`
- `/buyer/**` - Requires `ROLE_BUYER`
- `/api/buyer/**` - Requires `ROLE_BUYER`

### Static Resources
- `/css/**` - CSS files (public)
- `/js/**` - JavaScript files (public)
- `/images/**` - Image files (public)

---

## Common Response Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 500 | Internal Server Error |

---

## Notes

1. **Authentication:** Most endpoints require authentication via Spring Security with session-based authentication
2. **File Upload:** PDF files and preview images are stored on Cloudinary
3. **Payment Gateway:** SSLCommerz sandbox is used for payment processing
4. **CORS:** Configure CORS if accessing from different domains
5. **Content Type:** 
   - API endpoints return JSON (`application/json`)
   - PDF endpoints return PDF (`application/pdf`)
   - File upload uses `multipart/form-data`

---

## Development Information

**Technology Stack:**
- Spring Boot 4.0.3
- PostgreSQL Database
- Spring Security
- Cloudinary (File Storage)
- SSLCommerz (Payment Gateway)
- Thymeleaf (Template Engine)

**Base URL (Local):** `http://localhost:8080`  
**Database:** PostgreSQL on port 5432  
**Docker:** Application containerized with Docker Compose
