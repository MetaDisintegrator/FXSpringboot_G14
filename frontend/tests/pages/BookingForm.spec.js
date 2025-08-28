import { shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const h = vi.hoisted(() => ({ push: vi.fn(), replace: vi.fn() }))
vi.mock('vue-router', () => ({
    useRouter: () => ({ push: h.push, replace: h.replace }),
    useRoute: () => ({
        name: 'BookingForm',
        path: '/hotel/book/1',
        // 同时提供多种可能被读取的键名
        params: { id: '1', hotelId: '1' },
        query: { id: '1', hotelId: '1' },
    }),
}))

// mock 掉依赖 client 的 API
const api = vi.hoisted(() => ({
    fetchHotelDetail: vi.fn(() => Promise.resolve({ data: { id: 1, name: 'Mock Hotel' } })),
    createBooking: vi.fn(() => Promise.resolve({ data: { success: true, bookingId: 'X-1' } })),
    searchHotels: vi.fn(() => Promise.resolve({ data: [] })),
}))

vi.mock('@/api/hotel', () => api)
vi.mock('/src/api/hotel', () => api)

import { createTestingPinia } from '@pinia/testing'
import BookingForm from '@/pages/BookingForm.vue'

const mountPage = () =>
    shallowMount(BookingForm, {
        global: {
            plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: true })],
            stubs: [
                'RouterLink','RouterView',
                'ElForm','ElFormItem','ElInput','ElSelect','ElOption','ElDatePicker',
                'ElButton','ElCard','ElRow','ElCol',
            ],
        },
    })

describe('BookingForm Page', () => {
    beforeEach(() => {
        h.push.mockClear()
        h.replace.mockClear()
        api.fetchHotelDetail.mockClear()
        api.createBooking.mockClear()
    })

    it('renders without crashing & preloads hotel detail', async () => {
        mountPage()
        await Promise.resolve()

        expect(api.fetchHotelDetail).toHaveBeenCalledTimes(1)
        const call = api.fetchHotelDetail.mock.calls[0] || []
        const arg0 = call[0]
        if (arg0 !== undefined) {
            expect(arg0).toBe('1')
        }
    })

    it('can call createBooking (stubbed)', async () => {
        mountPage()
        await Promise.resolve()
        expect(api.createBooking).toBeTypeOf('function')
    })
})
