// src/api/train.js
import axios from 'axios'

const request = axios.create({
    baseURL: '/api',
    timeout: 5000,
    withCredentials: true
})

/* ------------------------ 车票购买模块 ------------------------ */

// 按出发时间查询车次
export const searchByDepartureTime = (params) => request.post('/train/seat/by-departure-time', params)

export const searchByDuration = (params) => request.post('/train/seat/by-duration-time', params)

export const startPayment = (params) => request.post('/train/ticket/get', params)

export const doAsync = (orderId) => request.get(`/train/ticket/${orderId}`)

export const searchTrainSeatOrder = (userId) => request.get(`train/order/get/${userId}`)

export const refundSeat = (params) => request.post(`train/refund`, params)

export const getTrainById = (id) => request.get(`train/by-id/${id}`)