// src/api/trainMeal.js
import axios from 'axios'

const request = axios.create({
    baseURL: '/api',
    timeout: 5000,
    withCredentials: true
})

export const searchByTrain = (trainId) => request.get(`train/meal/${trainId}`)

export const searchTrainMealOrder = (userId) => request.get(`train/meal/orders/${userId}`)

export const searchTrainMealOrderBySeatOrder = (seatOrderId) => request.get(`train/meal/orders/by-ticket/${userId}`)

export const startPayment = (params) => request.post('/train/meal/get', params)

export const doAsync = (orderId) => request.get('/train/meal/status', { params: orderId} )

export const refundMeal = (params) => request.post(`train/meal/refund`, params)
