import React, { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { orderService } from '../services/api'
import './OrderList.css'

function OrderList() {
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [filter, setFilter] = useState('all')

  useEffect(() => {
    loadOrders()
  }, [filter])

  const loadOrders = async () => {
    try {
      setLoading(true)
      setError(null)
      let data
      if (filter === 'all') {
        data = await orderService.getAllOrders()
      } else {
        data = await orderService.getOrdersByStatus(filter.toUpperCase())
      }
      setOrders(data)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load orders')
    } finally {
      setLoading(false)
    }
  }

  const getStatusClass = (status) => {
    return `status-badge status-${status.toLowerCase()}`
  }

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A'
    return new Date(dateString).toLocaleString()
  }

  const formatCurrency = (amount) => {
    if (!amount) return '$0.00'
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount)
  }

  if (loading) {
    return <div className="loading">Loading orders...</div>
  }

  return (
    <div className="order-list">
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
          <h2>Orders</h2>
          <Link to="/create" className="btn btn-primary">
            + Create New Order
          </Link>
        </div>

        {error && <div className="error">{error}</div>}

        <div className="filters">
          <button
            className={`filter-btn ${filter === 'all' ? 'active' : ''}`}
            onClick={() => setFilter('all')}
          >
            All
          </button>
          <button
            className={`filter-btn ${filter === 'created' ? 'active' : ''}`}
            onClick={() => setFilter('created')}
          >
            Created
          </button>
          <button
            className={`filter-btn ${filter === 'confirmed' ? 'active' : ''}`}
            onClick={() => setFilter('confirmed')}
          >
            Confirmed
          </button>
          <button
            className={`filter-btn ${filter === 'processed' ? 'active' : ''}`}
            onClick={() => setFilter('processed')}
          >
            Processed
          </button>
          <button
            className={`filter-btn ${filter === 'shipped' ? 'active' : ''}`}
            onClick={() => setFilter('shipped')}
          >
            Shipped
          </button>
        </div>

        {orders.length === 0 ? (
          <div className="empty-state">
            <h3>No orders found</h3>
            <p>Create your first order to get started</p>
          </div>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>Order ID</th>
                <th>Customer</th>
                <th>Status</th>
                <th>Items</th>
                <th>Total</th>
                <th>Created</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((order) => (
                <tr key={order.id}>
                  <td>
                    <Link to={`/orders/${order.id}`} className="order-link">
                      {order.id.substring(0, 8)}...
                    </Link>
                  </td>
                  <td>{order.customerName}</td>
                  <td>
                    <span className={getStatusClass(order.status)}>
                      {order.status}
                    </span>
                  </td>
                  <td>{order.items?.length || 0}</td>
                  <td>{formatCurrency(order.totalAmount)}</td>
                  <td>{formatDate(order.createdAt)}</td>
                  <td>
                    <Link
                      to={`/orders/${order.id}`}
                      className="btn btn-secondary"
                      style={{ fontSize: '0.875rem', padding: '0.5rem 1rem' }}
                    >
                      View
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}

export default OrderList

