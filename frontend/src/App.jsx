import React from 'react'
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom'
import OrderList from './components/OrderList'
import OrderForm from './components/OrderForm'
import OrderDetails from './components/OrderDetails'
import './App.css'

function App() {
  return (
    <Router>
      <div className="app">
        <header className="app-header">
          <div className="container">
            <h1>📦 Order Management System</h1>
            <nav>
              <Link to="/">Orders</Link>
              <Link to="/create">Create Order</Link>
            </nav>
          </div>
        </header>
        <main className="container">
          <Routes>
            <Route path="/" element={<OrderList />} />
            <Route path="/create" element={<OrderForm />} />
            <Route path="/orders/:orderId" element={<OrderDetails />} />
          </Routes>
        </main>
      </div>
    </Router>
  )
}

export default App

