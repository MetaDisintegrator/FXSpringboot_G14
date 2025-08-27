import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createRouter, createMemoryHistory } from 'vue-router'

// 这里导入你的 router 配置
import routerConfig from '@/router/index'

// Mock 组件，避免加载真实 .vue 文件
vi.mock('@/pages/TrainBooking.vue', () => ({ default: { template: '<div>TrainBooking</div>' } }))
vi.mock('@/pages/OrderHistory.vue', () => ({ default: { template: '<div>OrderHistory</div>' } }))
vi.mock('@/pages/TrainMeal.vue', () => ({ default: { template: '<div>TrainMeal</div>' } }))
vi.mock('@/pages/TrainHome.vue', () => ({ default: { template: '<div>TrainHome</div>' } }))
vi.mock('@/pages/HotelHome.vue', () => ({ default: { template: '<div>HotelHome</div>' } }))
vi.mock('@/pages/HotelSearch.vue', () => ({ default: { template: '<div>HotelSearch</div>' } }))
vi.mock('@/pages/HotelDetail.vue', () => ({ default: { template: '<div>HotelDetail</div>' } }))
vi.mock('@/pages/BookingForm.vue', () => ({ default: { template: '<div>BookingForm</div>' } }))
vi.mock('@/pages/TrainSearchResult.vue', () => ({ default: { template: '<div>TrainSearchResult</div>' } }))
vi.mock('@/pages/auth-page.vue', () => ({ default: { template: '<div>AuthPage</div>' } }))
vi.mock('@/pages/BookingSuccess.vue', () => ({ default: { template: '<div>BookingSuccess</div>' } }))
vi.mock('@/pages/AboutUs.vue', () => ({ default: { template: '<div>AboutUs</div>' } }))
vi.mock('@/pages/MessageCenter.vue', () => ({ default: { template: '<div>MessageCenter</div>' } }))
vi.mock('@/pages/CustomerService.vue', () => ({ default: { template: '<div>CustomerService</div>' } }))

describe('router/index.js', () => {
    let router

    beforeEach(() => {
        // 用内存路由代替浏览器路由，避免对真实 history 的依赖
        router = createRouter({
            history: createMemoryHistory(),
            routes: routerConfig.options.routes // routerConfig 是 createRouter 返回的对象，options.routes 是配置
        })
    })

    it('包含首页、搜索、登录等路由', () => {
        const paths = router.getRoutes().map(r => r.path)
        expect(paths).toContain('/')
        expect(paths).toContain('/auth')
        expect(paths).toContain('/train')
        expect(paths).toContain('/orders')
        expect(paths).toContain('/meal')
    })

    it('所有火车相关路由都带 meta.requiresAuth', () => {
        const protectedRoutes = ['/train', '/booking', '/orders', '/meal', '/train-result', '/booking-success']
        protectedRoutes.forEach(path => {
            const r = router.getRoutes().find(r => r.path === path)
            expect(r.meta.requiresAuth).toBe(true)
        })
    })

    it('可以导航到不需要鉴权的路由', async () => {
        await router.push('/')
        await router.isReady()
        expect(router.currentRoute.value.path).toBe('/')
    })
})
