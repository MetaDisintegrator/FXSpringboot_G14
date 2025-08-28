// 文件：src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import TrainBooking from '../pages/TrainBooking.vue'
import OrderHistory from '../pages/OrderHistory.vue'
import TrainMeal from '../pages/TrainMeal.vue'
import TrainHome from '../pages/TrainHome.vue'
import HotelHome from '../pages/HotelHome.vue'
import HotelSearch from '../pages/HotelSearch.vue'
import HotelDetail from '../pages/HotelDetail.vue'
import BookingForm from '../pages/BookingForm.vue'
import TrainSearchResult from '../pages/TrainSearchResult.vue'
import AuthPage from '../pages/auth-page.vue'
import BookingSuccess from '../pages/BookingSuccess.vue'
import AboutUs from "../pages/AboutUs.vue"
import MessageCenter from "../pages/MessageCenter.vue"
import CustomerService from '../pages/CustomerService.vue' // 或者 .vue，看你实际用哪种

const routes = [
    // 首页不需要登录
    { path: '/',                    name: 'HotelHome',       component: HotelHome },
    { path: '/search',              name: 'HotelSearch',     component: HotelSearch },
    { path: '/hotels/:id',          name: 'HotelDetail',     component: HotelDetail,   props: true },
    { path: '/bookingHotel/:bookingId?', name: 'BookingForm', component: BookingForm, props: true },
    { path: '/aboutUs', name: "aboutUs", component: AboutUs, props: true},
    // 新增：消息中心
    { path: '/messages', name: 'MessageCenter', component: MessageCenter},
    // 火车模块，需要登录才能访问
    {
        path: '/train',
        name: 'train',
        component: TrainHome,
        meta: { requiresAuth: true }
    },
    {
        path: '/booking',
        name: 'TrainBooking',
        component: TrainBooking,
        meta: { requiresAuth: true }
    },
    {
        path: '/orders',
        name: 'OrderHistory',
        component: OrderHistory,
        meta: { requiresAuth: true }
    },
    {
        path: '/contact',
        name: 'CustomerService',
        component: CustomerService
    },
    {
        path: '/meal',
        name: 'TrainMeal',
        component: TrainMeal,
        meta: { requiresAuth: true }
    },
    {
        path: '/train-result',
        name: 'trainResult',
        component: TrainSearchResult,
        props: true,
        meta: { requiresAuth: true }
    },

    {
        path: '/booking-success',
        name: 'BookingSuccess',
        component: BookingSuccess,
        meta: { requiresAuth: true }
    },

    // 登录/注册/忘记密码，不需要鉴权
    {
        path: '/auth',
        name: 'Authentication',
        component: AuthPage
    },
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router
