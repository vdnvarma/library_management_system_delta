# 🔒 SECURITY NOTICE - REQUIRED SETUP

## ⚠️ IMPORTANT: This repository has been secured!

The sensitive configuration files have been removed for security. You MUST set up the following before deployment:

## 🗂️ Required Configuration Files

### 1. Backend Configuration
Create these files in `backend/src/main/resources/` (they are git-ignored):

#### `application.properties`
```properties
# Copy from application.properties.example and set your values
spring.datasource.url=YOUR_DATABASE_URL
spring.datasource.username=YOUR_DB_USERNAME  
spring.datasource.password=YOUR_DB_PASSWORD
jwt.secret=YOUR_SECURE_JWT_SECRET
admin.password=YOUR_ADMIN_PASSWORD
cors.allowed-origins=https://your-frontend-url.com
```

#### `application-dev.properties` (for local development)
```properties
# Copy from application-dev.properties.example and set your values
spring.datasource.url=jdbc:mysql://localhost:3306/lms_dev
spring.datasource.username=your-dev-username
spring.datasource.password=your-dev-password
```

## 🚀 Render Deployment Setup

In your Render dashboard, set these environment variables:

### Backend Service Environment Variables:
```
SPRING_DATASOURCE_URL=your-database-url
SPRING_DATASOURCE_USERNAME=your-db-username
SPRING_DATASOURCE_PASSWORD=your-db-password
JWT_SECRET=your-secure-jwt-secret-key
ADMIN_PASSWORD=your-admin-password
CORS_ALLOWED_ORIGINS=https://your-frontend-url.com
```

### Frontend Service Environment Variables:
```
REACT_APP_API_URL=https://your-backend-url.onrender.com
```

## 🧪 Testing

The Selenium test suite is included and will test your deployed application automatically.

Run tests with: `mvn test`

## 📁 Project Structure

```
├── backend/          # Spring Boot API
│   ├── src/test/     # Selenium Tests ✅
│   └── pom.xml       # With Selenium dependencies ✅
├── frontend/         # React Application
└── README_SECURITY.md # This file
```

## 🔐 Security Best Practices Applied

- ✅ Removed all sensitive data from git history
- ✅ Added proper .gitignore rules  
- ✅ Created example templates
- ✅ Environment variable configuration
- ✅ Secure deployment instructions

**Never commit files containing passwords, API keys, or database credentials!**
