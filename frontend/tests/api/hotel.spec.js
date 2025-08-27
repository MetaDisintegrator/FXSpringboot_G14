// tests/api/hotel.spec.js
import '@testing-library/jest-dom'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import ax from 'axios'
import * as api from '../../src/api/hotel'

// jsdom: 给 matchMedia 一个 mock（放在 vi 可用之后）
if (!window.matchMedia) {
    Object.defineProperty(window, 'matchMedia', {
        writable: true,
        value: vi.fn().mockImplementation((query) => ({
            matches: false,
            media: query,
            onchange: null,
            addListener: vi.fn(),
            removeListener: vi.fn(),
            addEventListener: vi.fn(),
            removeEventListener: vi.fn(),
            dispatchEvent: vi.fn(),
        })),
    })
}

// 统一 mock axios（既有 default 也有具名导出；注意：不要写 : any）
vi.mock('axios', () => {
    const m = {
        // axios.create() 返回同一个“实例”对象 m，这样 request.get/post 就是 m.get/post
        create: vi.fn(() => m),
        get: vi.fn(),
        post: vi.fn(),
        put: vi.fn(),
        delete: vi.fn(),
        defaults: {},
        interceptors: {
            request: { use: vi.fn(), eject: vi.fn() },
            response: { use: vi.fn(), eject: vi.fn() },
        },
    }
    return { default: m, ...m }
})

describe('api/hotel.js', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('searchHotels：POST & 透传返回', async () => {
        const params = { city: '上海', date: '2025-10-01' }
        const fake = { data: [{ id: 1, name: 'HX Hotel' }] }
        ax.post.mockResolvedValueOnce(fake)

        const res = await api.searchHotels(params)
        expect(ax.post).toHaveBeenCalledTimes(1)
        expect(ax.post).toHaveBeenCalledWith('/hotel/room/by-dest', params)
        expect(res).toBe(fake)
    })

    it('searchHotels：异常向上传递（POST）', async () => {
        ax.post.mockRejectedValueOnce(new Error('net'))
        await expect(api.searchHotels({})).rejects.toThrow('net')
    })

    it('getOrderRooms：GET & 透传返回', async () => {
        const fake = { data: [{ id: 11, type: '大床房' }] }
        ax.get.mockResolvedValueOnce(fake)

        const res = await api.getOrderRooms('order-1')
        expect(ax.get).toHaveBeenCalledTimes(1)
        expect(ax.get).toHaveBeenCalledWith('/hotel/orders/order-1')
        expect(res).toBe(fake)
    })

    it('getOrderRooms：异常向上传递（GET）', async () => {
        ax.get.mockRejectedValueOnce(new Error('boom'))
        await expect(api.getOrderRooms('order-1')).rejects.toThrow('boom')
    })
})
