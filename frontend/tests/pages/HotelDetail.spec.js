import { shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const h = vi.hoisted(() => ({ push: vi.fn(), replace: vi.fn() }))
vi.mock('vue-router', () => ({
    useRouter: () => ({ push: h.push, replace: h.replace }),
    useRoute: () => ({
        name: 'HotelDetail',
        path: '/hotel/1',
        params: { id: '1', hotelId: '1' },
        query: { id: '1', hotelId: '1' },
    }),
}))

const api = vi.hoisted(() => ({
    fetchHotelDetail: vi.fn(() => Promise.resolve({ data: { id: 1, name: 'Mock Hotel', rating: 4.5 } })),
    createBooking: vi.fn(() => Promise.resolve({ data: { success: true } })),
    searchHotels: vi.fn(() => Promise.resolve({ data: [] })),
}))

vi.mock('@/api/hotel', () => api)
vi.mock('/src/api/hotel', () => api)

import { createTestingPinia } from '@pinia/testing'
import HotelDetail from '@/pages/HotelDetail.vue'

const mountPage = () =>
    shallowMount(HotelDetail, {
        global: {
            plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: true })],
            stubs: [
                'RouterLink','RouterView','ElCard','ElButton','ElIcon','ElImage',
                'ElTabs','ElTabPane','ElRate','ElTag'
            ],
        },
    })

describe('HotelDetail Page', () => {
    beforeEach(() => {
        h.push.mockClear()
        h.replace.mockClear()
        api.fetchHotelDetail.mockClear()
    })

    it('renders without crashing & calls fetchHotelDetail', async () => {
        mountPage()
        await Promise.resolve()

        expect(api.fetchHotelDetail).toHaveBeenCalledTimes(1)
        const call = api.fetchHotelDetail.mock.calls[0] || []
        const arg0 = call[0]
        if (arg0 !== undefined) {
            expect(arg0).toBe('1')
        }
    })
})
