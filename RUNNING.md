# 🚀 Running the Full Stack Application

This guide explains the different ways to run the Axon Order Management System.

## Quick Start

### Production Mode (Recommended for First Run)

```bash
./start.sh
```

This is the simplest way to get started. It will:
1. Build the React frontend
2. Package everything into a Spring Boot JAR
3. Start the application on port 8080

**Access points:**
- Frontend: http://localhost:8080/
- API: http://localhost:8080/api/orders
- Swagger: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console

### Development Mode (Recommended for Active Development)

```bash
./start-dev.sh
```

This runs frontend and backend separately with hot reload:
- **Backend**: http://localhost:8080 (Spring Boot with hot reload via Spring DevTools)
- **Frontend**: http://localhost:3000 (Vite dev server with hot reload)

**Benefits:**
- ✅ Instant frontend hot reload (no rebuild needed)
- ✅ Backend auto-restart on code changes
- ✅ Better error messages and debugging
- ✅ Faster development cycle

**Note:** Access the UI at http://localhost:3000 in dev mode (not 8080)

## Detailed Options

### Option 1: Production Mode (Integrated)

**Best for:** Production deployment, demos, testing the full integrated system

```bash
./start.sh production
```

**How it works:**
1. Builds React frontend → `src/main/resources/static/`
2. Packages Spring Boot with embedded frontend
3. Runs single process on port 8080

**Pros:**
- Single process, easy to manage
- Production-like environment
- No CORS issues
- Simple deployment

**Cons:**
- Must rebuild frontend for changes
- Slower development cycle

### Option 2: Development Mode (Separate Processes)

**Best for:** Active development, UI changes, API development

```bash
./start-dev.sh
```

**How it works:**
1. Starts Spring Boot backend on port 8080
2. Starts Vite dev server on port 3000
3. Vite proxies `/api/*` requests to backend

**Pros:**
- Hot reload on both frontend and backend
- Fast development cycle
- Better debugging experience
- Separate logs for frontend/backend

**Cons:**
- Two processes to manage
- Need to access UI on different port (3000)

### Option 3: Manual Setup

**Backend only:**
```bash
mvn spring-boot:run
```
Backend runs on http://localhost:8080

**Frontend only (development):**
```bash
cd frontend
npm install
npm run dev
```
Frontend runs on http://localhost:3000 and proxies API calls to backend

**Build frontend for production:**
```bash
cd frontend
npm install
npm run build
```
This builds the frontend into `src/main/resources/static/`. Restart Spring Boot to see changes.

## Troubleshooting

### Port Already in Use

If port 8080 is in use:
```bash
# Find what's using the port
lsof -i :8080
# or
netstat -ano | findstr :8080  # Windows

# Kill the process or change port in application.yml
```

If port 3000 is in use (dev mode):
```bash
# Find what's using the port
lsof -i :3000
# Change port in frontend/vite.config.js
```

### Frontend Not Loading

**Production mode:**
- Make sure you ran `npm run build` in the frontend directory
- Check that `src/main/resources/static/index.html` exists
- Restart Spring Boot after building

**Development mode:**
- Make sure backend is running on port 8080
- Check that Vite dev server started on port 3000
- Verify proxy configuration in `frontend/vite.config.js`

### API Calls Failing

**Development mode:**
- Ensure backend is running on port 8080
- Check CORS configuration in `WebConfig.java`
- Verify proxy settings in `frontend/vite.config.js`

**Production mode:**
- Check that requests go to `/api/*` paths
- Verify Spring Boot is serving static files correctly

### Build Errors

**Frontend build fails:**
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
npm run build
```

**Backend build fails:**
```bash
mvn clean install
```

## Environment Variables

The application uses these environment variables (see `application.yml`):

- `EMAIL_CLIENT_IMAP_HOST` - IMAP host (for email features)
- `EMAIL_CLIENT_IMAP_PORT` - IMAP port
- `EMAIL_CLIENT_IMAP_USER` - IMAP username
- `EMAIL_CLIENT_IMAP_PASSWORD` - IMAP password
- `EMAIL_CLIENT_MASTER_KEY` - Encryption master key
- `EMAIL_CLIENT_CRYPTO_SALT` - Encryption salt

For basic order management, these are not required.

## Database

The application uses H2 in-memory database by default. Data is lost on restart.

**H2 Console Access:**
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`

To use persistent database, update `application.yml` with your database configuration.

## Performance Tips

1. **Development:** Use `./start-dev.sh` for fastest iteration
2. **Testing:** Use `./start.sh` for production-like testing
3. **Frontend changes:** In dev mode, changes are instant (no rebuild)
4. **Backend changes:** Spring DevTools auto-restarts (may take a few seconds)

## Next Steps

- Check out the [API Documentation](README.md#-api-documentation)
- Explore the [Swagger UI](http://localhost:8080/swagger-ui.html)
- Review the [Architecture](README.md#-architecture)
- Run the [Tests](README.md#-testing)

