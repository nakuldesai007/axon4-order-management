import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { orderService } from '../services/api'
import './OrderDetails.css'

function OrderDetails() {
  const { orderId } = useParams()
  const navigate = useNavigate()
  const [order, setOrder] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [actionLoading, setActionLoading] = useState(false)
  const [showAddItem, setShowAddItem] = useState(false)
  const [showShipForm, setShowShipForm] = useState(false)
  const [showCancelForm, setShowCancelForm] = useState(false)
  const [itemForm, setItemForm] = useState({
    productId: '',
    productName: '',
    quantity: 1,
    price: '',
  })
  const [shipForm, setShipForm] = useState({ trackingNumber: '' })
  const [cancelForm, setCancelForm] = useState({ reason: '' })

  useEffect(() => {
    loadOrder()
    const interval = setInterval(loadOrder, 2000) // Refresh every 2 seconds
    return () => clearInterval(interval)
  }, [orderId])

  const loadOrder = async () => {
    try {
      const data = await orderService.getOrderById(orderId)
      setOrder(data)
      setError(null)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load order')
    } finally {
      setLoading(false)
    }
  }

  const handleAction = async (action, data = {}) => {
    setActionLoading(true)
    setError(null)
    try {
      switch (action) {
        case 'confirm':
          await orderService.confirmOrder(orderId)
          break
        case 'process':
          await orderService.processOrder(orderId)
          break
        case 'ship':
          await orderService.shipOrder(orderId, shipForm.trackingNumber)
          setShowShipForm(false)
          setShipForm({ trackingNumber: '' })
          break
        case 'cancel':
          await orderService.cancelOrder(orderId, cancelForm.reason)
          setShowCancelForm(false)
          setCancelForm({ reason: '' })
          break
        case 'addItem':
          await orderService.addItemToOrder(orderId, itemForm)
          setShowAddItem(false)
          setItemForm({ productId: '', productName: '', quantity: 1, price: '' })
          break
        case 'removeItem':
          await orderService.removeItemFromOrder(orderId, data.productId)
          break
      }
      await loadOrder()
    } catch (err) {
      setError(err.response?.data?.message || `Failed to ${action} order`)
    } finally {
      setActionLoading(false)
    }
  }

  const getStatusClass = (status) => {
    return `status-badge status-${status.toLowerCase()}`
  }

  const formatCurrency = (amount) => {
    if (!amount) return '$0.00'
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount)
  }

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A'
    return new Date(dateString).toLocaleString()
  }

  const canAddItems = order?.status === 'CREATED'
  const canConfirm = order?.status === 'CREATED' && order?.items?.length > 0
  const canProcess = order?.status === 'CONFIRMED'
  const canShip = order?.status === 'PROCESSED'
  const canCancel = order?.status && !['SHIPPED', 'DELIVERED', 'CANCELLED'].includes(order.status)

  if (loading) {
    return <div className="loading">Loading order details...</div>
  }

  if (!order) {
    return (
      <div className="error">
        Order not found. <button onClick={() => navigate('/')}>Go Back</button>
      </div>
    )
  }

  return (
    <div className="order-details">
      <div style={{ marginBottom: '1rem' }}>
        <button className="btn btn-secondary" onClick={() => navigate('/')}>
          ← Back to Orders
        </button>
      </div>

      {error && <div className="error">{error}</div>}

      <div className="card">
        <div className="order-header">
          <div>
            <h2>Order Details</h2>
            <p className="order-id">ID: {order.id}</p>
          </div>
          <span className={getStatusClass(order.status)}>{order.status}</span>
        </div>

        <div className="order-info">
          <div className="info-section">
            <h3>Customer Information</h3>
            <p><strong>Name:</strong> {order.customerName}</p>
            <p><strong>ID:</strong> {order.customerId}</p>
            {order.customerEmail && (
              <p><strong>Email:</strong> {order.customerEmail}</p>
            )}
            {order.shippingAddress && (
              <p><strong>Shipping Address:</strong> {order.shippingAddress}</p>
            )}
          </div>

          <div className="info-section">
            <h3>Order Summary</h3>
            <p><strong>Items:</strong> {order.items?.length || 0}</p>
            <p><strong>Total Amount:</strong> {formatCurrency(order.totalAmount)}</p>
            <p><strong>Created:</strong> {formatDate(order.createdAt)}</p>
            {order.updatedAt && (
              <p><strong>Last Updated:</strong> {formatDate(order.updatedAt)}</p>
            )}
          </div>
        </div>
      </div>

      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
          <h2>Items</h2>
          {canAddItems && (
            <button
              className="btn btn-primary"
              onClick={() => setShowAddItem(!showAddItem)}
            >
              {showAddItem ? 'Cancel' : '+ Add Item'}
            </button>
          )}
        </div>

        {showAddItem && (
          <div className="add-item-form">
            <h3>Add New Item</h3>
            <div className="form-row">
              <div className="form-group">
                <label>Product ID *</label>
                <input
                  type="text"
                  value={itemForm.productId}
                  onChange={(e) =>
                    setItemForm({ ...itemForm, productId: e.target.value })
                  }
                  placeholder="e.g., PROD-001"
                  required
                />
              </div>
              <div className="form-group">
                <label>Product Name *</label>
                <input
                  type="text"
                  value={itemForm.productName}
                  onChange={(e) =>
                    setItemForm({ ...itemForm, productName: e.target.value })
                  }
                  placeholder="e.g., iPhone 15 Pro"
                  required
                />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Quantity *</label>
                <input
                  type="number"
                  min="1"
                  value={itemForm.quantity}
                  onChange={(e) =>
                    setItemForm({
                      ...itemForm,
                      quantity: parseInt(e.target.value) || 1,
                    })
                  }
                  required
                />
              </div>
              <div className="form-group">
                <label>Price *</label>
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  value={itemForm.price}
                  onChange={(e) =>
                    setItemForm({ ...itemForm, price: e.target.value })
                  }
                  placeholder="e.g., 999.99"
                  required
                />
              </div>
            </div>
            <button
              className="btn btn-success"
              onClick={() => handleAction('addItem')}
              disabled={actionLoading || !itemForm.productId || !itemForm.productName || !itemForm.price}
            >
              Add Item
            </button>
          </div>
        )}

        {order.items && order.items.length > 0 ? (
          <table className="table">
            <thead>
              <tr>
                <th>Product ID</th>
                <th>Product Name</th>
                <th>Quantity</th>
                <th>Price</th>
                <th>Subtotal</th>
                {canAddItems && <th>Actions</th>}
              </tr>
            </thead>
            <tbody>
              {order.items.map((item, index) => (
                <tr key={index}>
                  <td>{item.productId}</td>
                  <td>{item.productName}</td>
                  <td>{item.quantity}</td>
                  <td>{formatCurrency(item.price)}</td>
                  <td>{formatCurrency(item.price * item.quantity)}</td>
                  {canAddItems && (
                    <td>
                      <button
                        className="btn btn-danger"
                        style={{ fontSize: '0.875rem', padding: '0.5rem 1rem' }}
                        onClick={() =>
                          handleAction('removeItem', { productId: item.productId })
                        }
                        disabled={actionLoading}
                      >
                        Remove
                      </button>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <div className="empty-state">
            <p>No items in this order. Add items to proceed.</p>
          </div>
        )}
      </div>

      <div className="card">
        <h2>Order Actions</h2>
        <div className="actions">
          {canConfirm && (
            <button
              className="btn btn-success"
              onClick={() => handleAction('confirm')}
              disabled={actionLoading}
            >
              Confirm Order
            </button>
          )}
          {canProcess && (
            <button
              className="btn btn-warning"
              onClick={() => handleAction('process')}
              disabled={actionLoading}
            >
              Process Order
            </button>
          )}
          {canShip && (
            <>
              {!showShipForm ? (
                <button
                  className="btn btn-primary"
                  onClick={() => setShowShipForm(true)}
                  disabled={actionLoading}
                >
                  Ship Order
                </button>
              ) : (
                <div className="action-form">
                  <input
                    type="text"
                    placeholder="Tracking Number"
                    value={shipForm.trackingNumber}
                    onChange={(e) =>
                      setShipForm({ trackingNumber: e.target.value })
                    }
                    style={{ marginRight: '0.5rem', padding: '0.5rem' }}
                  />
                  <button
                    className="btn btn-primary"
                    onClick={() => handleAction('ship')}
                    disabled={actionLoading || !shipForm.trackingNumber}
                  >
                    Ship
                  </button>
                  <button
                    className="btn btn-secondary"
                    onClick={() => {
                      setShowShipForm(false)
                      setShipForm({ trackingNumber: '' })
                    }}
                  >
                    Cancel
                  </button>
                </div>
              )}
            </>
          )}
          {canCancel && (
            <>
              {!showCancelForm ? (
                <button
                  className="btn btn-danger"
                  onClick={() => setShowCancelForm(true)}
                  disabled={actionLoading}
                >
                  Cancel Order
                </button>
              ) : (
                <div className="action-form">
                  <input
                    type="text"
                    placeholder="Cancellation Reason"
                    value={cancelForm.reason}
                    onChange={(e) =>
                      setCancelForm({ reason: e.target.value })
                    }
                    style={{ marginRight: '0.5rem', padding: '0.5rem' }}
                  />
                  <button
                    className="btn btn-danger"
                    onClick={() => handleAction('cancel')}
                    disabled={actionLoading || !cancelForm.reason}
                  >
                    Cancel
                  </button>
                  <button
                    className="btn btn-secondary"
                    onClick={() => {
                      setShowCancelForm(false)
                      setCancelForm({ reason: '' })
                    }}
                  >
                    Back
                  </button>
                </div>
              )}
            </>
          )}
          {order.status === 'SHIPPED' && order.trackingNumber && (
            <div className="info-box">
              <strong>Tracking Number:</strong> {order.trackingNumber}
            </div>
          )}
          {order.status === 'CANCELLED' && order.cancellationReason && (
            <div className="info-box">
              <strong>Cancellation Reason:</strong> {order.cancellationReason}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default OrderDetails

