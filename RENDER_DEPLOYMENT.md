# Deploying LMS Web Application to Render.com

This document describes how to deploy both the frontend and backend components of the LMS Web Application to Render.com.

## Backend Deployment (Spring Boot API)

1. **Sign up/Login to Render.com**:
   - Go to [Render.com](https://render.com/) and create an account or log in.

2. **Create a new Web Service**:
   - Click "New" > "Web Service".
   - Connect your GitHub repository or use the public repository URL.

3. **Configure the Web Service**:
   - Name: `lms-api` (or your preferred name)
   - Runtime: `Java`
   - Build Command: `./mvnw clean package -DskipTests`
   - Start Command: `java -jar target/*.jar`
   - Instance Type: Select the appropriate plan (e.g., Free plan for development)

4. **Environment Variables**:
   Set the following environment variables in the Render dashboard:
   - `SPRING_DATASOURCE_URL`: Your PostgreSQL database URL (e.g., `jdbc:postgresql://postgres-host:5432/lmsdb`)
   - `SPRING_DATASOURCE_USERNAME`: Database username
   - `SPRING_DATASOURCE_PASSWORD`: Database password
   - `JWT_SECRET`: A secure, random string at least 32 characters long for JWT token signing
   - `SPRING_JPA_HIBERNATE_DDL_AUTO`: `update` (or your preferred option)
   - `SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT`: `org.hibernate.dialect.PostgreSQLDialect`
   - `SPRING_JPA_DATABASE_PLATFORM`: `org.hibernate.dialect.PostgreSQLDialect`

5. **Create a Database**:
   - Click "New" > "PostgreSQL" to create a PostgreSQL database.
   - Note the connection details - you'll need them for the environment variables above.

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

## Troubleshooting

- Check the Render logs for any error messages.
- Verify that all environment variables are correctly set.
- Ensure that your frontend is correctly pointing to the backend API URL.
- For database connectivity issues, check that the database is accessible from the backend service.
