import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

export const orderService = {
  // Get all orders
  getAllOrders: async () => {
    const response = await api.get('/orders')
    return response.data
  },

  // Get order by ID
  getOrderById: async (orderId) => {
    const response = await api.get(`/orders/${orderId}`)
    return response.data
  },

  // Create order
  createOrder: async (orderData) => {
    const response = await api.post('/orders', orderData)
    return response.data
  },

  // Add item to order
  addItemToOrder: async (orderId, itemData) => {
    const response = await api.post(`/orders/${orderId}/items`, itemData)
    return response.data
  },

  // Remove item from order
  removeItemFromOrder: async (orderId, productId) => {
    const response = await api.delete(`/orders/${orderId}/items/${productId}`)
    return response.data
  },

  // Confirm order
  confirmOrder: async (orderId) => {
    const response = await api.post(`/orders/${orderId}/confirm`)
    return response.data
  },

  // Process order
  processOrder: async (orderId) => {
    const response = await api.post(`/orders/${orderId}/process`)
    return response.data
  },

  // Ship order
  shipOrder: async (orderId, trackingNumber) => {
    const response = await api.post(`/orders/${orderId}/ship`, {
      trackingNumber,
    })
    return response.data
  },

  // Cancel order
  cancelOrder: async (orderId, reason) => {
    const response = await api.post(`/orders/${orderId}/cancel`, {
      reason,
    })
    return response.data
  },

  // Get orders by customer
  getOrdersByCustomer: async (customerId) => {
    const response = await api.get(`/orders/customer/${customerId}`)
    return response.data
  },

  // Get orders by status
  getOrdersByStatus: async (status) => {
    const response = await api.get(`/orders/status/${status}`)
    return response.data
  },
}

export default api

