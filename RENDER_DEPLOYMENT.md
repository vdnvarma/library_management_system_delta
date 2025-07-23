# Deploying LMS Web Application to Render.com

This document describes how to deploy both the frontend and backend components of the LMS Web Application to Render.com using Docker for the backend.

## Backend Deployment (Spring Boot API with Docker)

1. **Sign up/Login to Render.com**:
   - Go to [Render.com](https://render.com/) and create an account or log in.

2. **Create a new Web Service**:
   - Click "New" > "Web Service".
   - Connect your GitHub repository or use the public repository URL.

3. **Configure the Web Service**:
   - Name: `lms-api` (or your preferred name)
   - Environment: Select `Docker`
   - Root Directory (if your backend is in a subdirectory): `backend` (adjust as needed)
   - Docker Build Context Directory: `.` (or your backend directory path)
   - Instance Type: Select the appropriate plan (e.g., Free plan for development)

4. **Environment Variables**:
   Set the following environment variables in the Render dashboard:
   - `SPRING_DATASOURCE_URL`: Your database URL (see database options below)
   - `SPRING_DATASOURCE_USERNAME`: Database username
   - `SPRING_DATASOURCE_PASSWORD`: Database password
   - `SPRING_DATASOURCE_DRIVER`: Database driver class (MySQL: `com.mysql.cj.jdbc.Driver`)
   - `JWT_SECRET`: A secure, random string at least 32 characters long for JWT token signing
   - `SPRING_JPA_HIBERNATE_DDL_AUTO`: `update` (or your preferred option)
   - `SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT`: Hibernate dialect (MySQL: `org.hibernate.dialect.MySQL8Dialect`)
   - `SPRING_JPA_DATABASE_PLATFORM`: Database platform (MySQL: `org.hibernate.dialect.MySQL8Dialect`)
   - `PORT`: `8080`

5. **Database Options for Production**:
   
   **Option 1: Use Render's PostgreSQL (Recommended)**
   - Click "New" > "PostgreSQL" to create a new PostgreSQL database.
   - Name: `lms-db` (or your preferred name)
   - Select the appropriate plan (Free tier is available for development)
   - After creation, note the connection details from the "Connect" tab.
   - Set environment variables for PostgreSQL:
     - `SPRING_DATASOURCE_URL`: `jdbc:postgresql://host:5432/database_name`
     - `SPRING_DATASOURCE_DRIVER`: `org.postgresql.Driver`
     - `SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT`: `org.hibernate.dialect.PostgreSQLDialect`
     - `SPRING_JPA_DATABASE_PLATFORM`: `org.hibernate.dialect.PostgreSQLDialect`
   
   **Option 2: External MySQL Database (If you prefer MySQL)**
   - Set up a MySQL database on a service like PlanetScale, AWS RDS, or DigitalOcean
   - Make sure the database is publicly accessible with proper security
   - Set environment variables for MySQL:
     - `SPRING_DATASOURCE_URL`: `jdbc:mysql://your-mysql-host:3306/lmsdb?useSSL=true&serverTimezone=UTC`
     - `SPRING_DATASOURCE_DRIVER`: `com.mysql.cj.jdbc.Driver`
     - `SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT`: `org.hibernate.dialect.MySQL8Dialect`
     - `SPRING_JPA_DATABASE_PLATFORM`: `org.hibernate.dialect.MySQL8Dialect`
   
   Note: Do NOT use your local development database for production deployment

6. **Deploy**:
   - Click "Create Web Service" to start the deployment.

## Frontend Deployment (React)

1. **Create a new Static Site**:
   - Click "New" > "Static Site".
   - Connect your GitHub repository or use the public repository URL.

2. **Configure the Static Site**:
   - Name: `lms-web` (or your preferred name)
   - Build Command: `npm install && npm run build`
   - Publish Directory: `build`
   - Environment Variables:
     - `REACT_APP_API_URL`: The URL of your backend API with the `/api` path (e.g., `https://lms-api.onrender.com/api`)

3. **Deploy**:
   - Click "Create Static Site" to start the deployment.

## Testing the Deployment

1. After both services are deployed, go to your frontend URL (e.g., `https://lms-web.onrender.com`).
2. Register and log in to test the application functionality.

## Local Testing with Docker

Before deploying to Render.com, you can test your Docker setup locally:

1. **Navigate to your backend directory**:
   ```
   cd backend
   ```

2. **Start the application with Docker Compose**:
   ```
   docker-compose up
   ```
   This will start both your Spring Boot application and a PostgreSQL database.

3. **Access the API**:
   The API will be available at `http://localhost:8080/api`

4. **Shut down when done**:
   ```
   docker-compose down
   ```
   Use `-v` flag to remove volumes as well if you want to clear database data:
   ```
   docker-compose down -v
   ```

## Database Migration

When deploying to production, you have two options for database initialization:

1. **Automatic Schema Creation**: Your application can automatically create tables when using `spring.jpa.hibernate.ddl-auto=update`. This works well for the first deployment and is recommended for your initial setup.

2. **Manual Data Migration**: For moving existing data from your local MySQL database to production:

   **If using MySQL in production:**
   - Create a database backup from your local MySQL database:
     ```
     mysqldump -u root -p lmsdb > lmsdb_backup.sql
     ```
   - Import to your production MySQL database:
     ```
     mysql -u username -p database_name < lmsdb_backup.sql
     ```

   **If using PostgreSQL in production:**
   - Create a database backup from your local MySQL database:
     ```
     mysqldump -u root -p lmsdb > lmsdb_backup.sql
     ```
   - Convert MySQL-specific SQL to PostgreSQL format using a tool like pgloader:
     ```
     pgloader mysql://root:password@localhost/lmsdb postgresql://postgres:password@localhost/lmsdb
     ```
   - Or use an online converter tool for smaller databases

## Troubleshooting

- Check the Render logs for any error messages.
- Verify that all environment variables are correctly set.
- Ensure that your frontend is correctly pointing to the backend API URL.
- For database connectivity issues, check that the database is accessible from the backend service.
- For Docker-related issues, check that your Dockerfile is valid and the Docker build succeeds locally.
- If your Spring Boot application fails to start, check the logs for specific error messages.
- If you're having database issues after migration, check for MySQL-specific SQL that might not be compatible with PostgreSQL.
