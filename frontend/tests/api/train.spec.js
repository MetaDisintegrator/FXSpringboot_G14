import { describe, it, expect, vi, beforeEach } from 'vitest'
import ax from 'axios'
import * as api from '../../src/api/train'

// 统一 mock axios（提供 default + 具名导出；注意：不要写 : any）
vi.mock('axios', () => {
    const m = {
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

describe('api/train.js', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('searchByDepartureTime：POST & 透传返回', async () => {
        const params = { from: '北京', to: '广州', date: '2025-09-11' }
        const fake = { data: { data: [{ train: { id: 1 } }] } }
        ax.post.mockResolvedValueOnce(fake)

        const res = await api.searchByDepartureTime(params)
        expect(ax.post).toHaveBeenCalledTimes(1)
        expect(ax.post).toHaveBeenCalledWith('/train/seat/by-departure-time', params)
        expect(res).toBe(fake)
    })

    it('searchByDepartureTime：异常向上传递', async () => {
        ax.post.mockRejectedValueOnce(new Error('net'))
        await expect(api.searchByDepartureTime({})).rejects.toThrow('net')
    })

    it('searchByDuration：POST & 透传返回', async () => {
        const params = { from: '北京', to: '广州', date: '2025-09-11' }
        const fake = { data: { data: [{ train: { id: 2 } }] } }
        ax.post.mockResolvedValueOnce(fake)

        const res = await api.searchByDuration(params)
        expect(ax.post).toHaveBeenCalledTimes(1)
        expect(ax.post).toHaveBeenCalledWith('/train/seat/by-duration-time', params)
        expect(res).toBe(fake)
    })

    it('searchByDuration：异常向上传递', async () => {
        ax.post.mockRejectedValueOnce(new Error('boom'))
        await expect(api.searchByDuration({})).rejects.toThrow('boom')
    })

    it('startPayment：POST /train/ticket/get & 透传返回', async () => {
        const payload = { trainId: 9, seatType: 'SECOND_CLASS_SEAT', price: 123 }
        const fake = { data: { paymentId: 'P-1' } }
        ax.post.mockResolvedValueOnce(fake)

        const res = await api.startPayment(payload)
        expect(ax.post).toHaveBeenCalledTimes(1)
        expect(ax.post).toHaveBeenCalledWith('/train/ticket/get', payload)
        expect(res).toBe(fake)
    })

    it('startPayment：异常向上传递', async () => {
        ax.post.mockRejectedValueOnce(new Error('pay err'))
        await expect(api.startPayment({})).rejects.toThrow('pay err')
    })

    it('doAsync：GET /train/ticket/:orderId & 透传返回', async () => {
        const fake = { data: { ok: true } }
        ax.get.mockResolvedValueOnce(fake)

        const res = await api.doAsync('task-1')
        expect(ax.get).toHaveBeenCalledTimes(1)
        expect(ax.get).toHaveBeenCalledWith('/train/ticket/task-1')
        expect(res).toBe(fake)
    })

    it('doAsync：异常向上传递', async () => {
        ax.get.mockRejectedValueOnce(new Error('net'))
        await expect(api.doAsync('task-1')).rejects.toThrow('net')
    })

    it('searchTrainSeatOrder：GET train/order/get/:userId', async () => {
        const fake = { data: { orders: [] } }
        ax.get.mockResolvedValueOnce(fake)

        const res = await api.searchTrainSeatOrder('u-1')
        expect(ax.get).toHaveBeenCalledWith('train/order/get/u-1')
        expect(res).toBe(fake)
    })

    it('refundSeat：POST train/refund', async () => {
        const payload = { orderId: 'o-1' }
        const fake = { data: { refunded: true } }
        ax.post.mockResolvedValueOnce(fake)

        const res = await api.refundSeat(payload)
        expect(ax.post).toHaveBeenCalledWith('train/refund', payload)
        expect(res).toBe(fake)
    })

    it('getTrainById：GET train/by-id/:id', async () => {
        const fake = { data: { id: 9 } }
        ax.get.mockResolvedValueOnce(fake)

        const res = await api.getTrainById(9)
        expect(ax.get).toHaveBeenCalledWith('train/by-id/9')
        expect(res).toBe(fake)
    })
})
