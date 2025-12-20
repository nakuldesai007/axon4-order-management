import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { orderService } from '../services/api'

function OrderForm() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [formData, setFormData] = useState({
    customerId: '',
    customerName: '',
    customerEmail: '',
    shippingAddress: '',
  })

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError(null)

    try {
      const orderId = await orderService.createOrder(formData)
      navigate(`/orders/${orderId}`)
    } catch (err) {
      setError(
        err.response?.data?.message || 'Failed to create order. Please try again.'
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="order-form">
      <div className="card">
        <h2>Create New Order</h2>
        {error && <div className="error">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="customerId">Customer ID *</label>
            <input
              type="text"
              id="customerId"
              name="customerId"
              value={formData.customerId}
              onChange={handleChange}
              required
              placeholder="e.g., CUST-001"
            />
          </div>

          <div className="form-group">
            <label htmlFor="customerName">Customer Name *</label>
            <input
              type="text"
              id="customerName"
              name="customerName"
              value={formData.customerName}
              onChange={handleChange}
              required
              placeholder="e.g., John Doe"
            />
          </div>

          <div className="form-group">
            <label htmlFor="customerEmail">Customer Email</label>
            <input
              type="email"
              id="customerEmail"
              name="customerEmail"
              value={formData.customerEmail}
              onChange={handleChange}
              placeholder="e.g., john.doe@example.com"
            />
          </div>

          <div className="form-group">
            <label htmlFor="shippingAddress">Shipping Address</label>
            <textarea
              id="shippingAddress"
              name="shippingAddress"
              value={formData.shippingAddress}
              onChange={handleChange}
              rows="3"
              placeholder="e.g., 123 Main St, City, State 12345"
            />
          </div>

          <div style={{ display: 'flex', gap: '1rem', marginTop: '1.5rem' }}>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading}
            >
              {loading ? 'Creating...' : 'Create Order'}
            </button>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => navigate('/')}
            >
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default OrderForm

