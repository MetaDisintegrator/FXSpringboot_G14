// src/api/hotel.js
import axios from 'axios'

// ———————— 创建 Axios 实例 ————————
// baseURL 从环境变量里读取，如果不存在就默认 '/api'
const request = axios.create({
    baseURL: '/api',
    timeout: 5000,
    withCredentials: true
})

export const getOrderRooms = (userId) => request.get(`/hotel/orders/${userId}`)

export const searchHotels = (params) => request.post('/hotel/room/by-dest', params)

export const bookRoom = (params) => request.post('/hotel/room/get', params)

export const doAsync = (orderId) => request.get(`/hotel/${orderId}`)

export const refundRoom = (params) => request.post('/hotel/refund', params )

// 过时
export function advancedSearch(filters) {
  return client.post('/hotels/search', filters)
}
export function fetchHotelDetail(id) {
  return client.get(`/hotels/${id}`)
}
export function createBooking(bookingData) {
  return client.post('/hotel_bookings', bookingData)
}


export default {
  searchHotels,
  advancedSearch,
  fetchHotelDetail,
  createBooking,
}