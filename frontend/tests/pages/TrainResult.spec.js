import { render, screen, fireEvent } from '@testing-library/vue'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import TrainResult from '../../src/pages/TrainSearchResult.vue'

// ① mock vue-router：提供稳定的 query
vi.mock('vue-router', () => ({
    useRoute: () => ({
        query: { from: '上海', to: '杭州', date: '2025-09-01' },
        params: {},
    }),
    useRouter: () => ({ push: vi.fn(), replace: vi.fn() }),
}))

// ② mock API：页面会用到的函数都给出“有 data 字段”的返回
vi.mock('../../src/api/train', () => ({
    searchByDepartureTime: vi.fn().mockResolvedValue({
        data: {
            data: [
                {
                    train: { trainNo: 'G1', trainType: 'HIGH_SPEED' },
                    trainseats: [{ seatType: 'SECOND_CLASS_SEAT', price: 100, remain: 20 }],
                    departureTime: '08:00',
                },
                {
                    train: { trainNo: 'K2', trainType: 'GREEN_TRAIN' },
                    trainseats: [{ seatType: 'SECOND_CLASS_SEAT', price: 120, remain: 0 }],
                    departureTime: '20:00',
                },
            ],
        },
    }),
    // 万一页面或其子组件触发了轮询，补上 doAsync 的“有 data”返回，避免 undefined 解构
    doAsync: vi.fn().mockResolvedValue({ data: { status: 'OK' } }),
}))

// ③ 直接 stub 掉会触发轮询逻辑的 TrainBooking（如果 TrainResult 里会引入它）
vi.mock('../../src/pages/TrainBooking.vue', () => ({
    default: { name: 'TrainBooking', template: '<div data-test="booking-stub"></div>' },
}))

beforeEach(() => {
    vi.clearAllMocks()
})

describe('TrainResult 页面 - 过滤逻辑', () => {
    it('只看有票 / 车种筛选 / 时间段筛选 会影响 filteredTrains', async () => {
        render(TrainResult, {
            global: {
                plugins: [
                    // ④ 注入 Pinia 测试实例，解决 getActivePinia 报错
                    createTestingPinia({ stubActions: true, createSpy: vi.fn }),
                ],
                stubs: {
                    'el-button': true,
                    'el-input': true,
                    'el-select': true,
                    'el-option': true,
                    'el-form': true,
                    'el-form-item': true,
                    // 若页面还有其它 UI 组件，继续在这里 stub
                },
            },
        })

        // 等待 onMounted 请求 & 渲染
        await Promise.resolve()

        // 你的组件里请确保每条车次卡片有 .train-card 类名
        expect(document.querySelectorAll('.train-card').length).toBe(2)

        // 只看有票
        const onlyLeft = screen.getByText('只看有票').closest('label')
        await fireEvent.click(onlyLeft.querySelector('input'))
        expect(document.querySelectorAll('.train-card').length).toBe(1)

        // 车种：高铁/动车
        const highSpeed = screen.getByText('高铁/动车').closest('label')
        await fireEvent.click(highSpeed.querySelector('input'))
        expect(document.querySelectorAll('.train-card').length).toBe(1)

        // 时间段：06:00 - 12:00
        const am = screen.getByText('06:00 - 12:00').closest('label')
        await fireEvent.click(am.querySelector('input'))
        expect(document.querySelectorAll('.train-card').length).toBe(1)
    })
})
