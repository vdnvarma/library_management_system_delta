# Library Management System - Backend

This is the backend component of the Library Management System.

## Environment Setup

1. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```

2. Edit the `.env` file and update the values with your actual configuration:
   ```
   # Database Configuration
   SPRING_DATASOURCE_URL=jdbc:mysql://your-db-host:port/your-db-name
   SPRING_DATASOURCE_USERNAME=your_username
   SPRING_DATASOURCE_PASSWORD=your_password

   # JWT Configuration
   JWT_SECRET=your_secure_jwt_secret
   ```

3. Make sure to keep your `.env` file secure and never commit it to version control.

## Running with Docker

```bash
docker-compose up
```

## Security Note

Never commit the `.env` file to version control as it contains sensitive information. Always use environment variables for secrets in production environments.
