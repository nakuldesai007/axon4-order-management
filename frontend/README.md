# Order Management UI

Modern React frontend for the Axon 4 Order Management System.

## Features

- 📋 View all orders with filtering by status
- ➕ Create new orders
- 📦 Manage order items (add/remove)
- 🔄 Complete order lifecycle management (confirm, process, ship, cancel)
- 🎨 Modern, responsive UI
- ⚡ Real-time order updates

## Development

### Prerequisites

- Node.js 18+ and npm

### Install Dependencies

```bash
cd frontend
npm install
```

### Run Development Server

```bash
npm run dev
```

The frontend will run on `http://localhost:3000` and proxy API requests to `http://localhost:8080`.

### Build for Production

```bash
npm run build
```

This will build the frontend and output the files to `src/main/resources/static/` so Spring Boot can serve them.

## Usage

1. Start the Spring Boot backend: `mvn spring-boot:run`
2. Start the frontend dev server: `cd frontend && npm run dev`
3. Open `http://localhost:3000` in your browser

For production, build the frontend and the static files will be served by Spring Boot at `http://localhost:8080`.

