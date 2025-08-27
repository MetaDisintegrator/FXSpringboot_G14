import { describe, it, expect, vi, beforeEach } from 'vitest'
import ax from 'axios'
import * as api from '../../src/api/trainMeal'

// 统一 mock axios（提供 default + 具名导出）
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

describe('api/trainMeal.js', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('searchByTrain：GET train/meal/:trainId & 透传返回', async () => {
        const fake = { data: [{ id: 1, name: 'A 套餐' }] }
        ax.get.mockResolvedValueOnce(fake)

        const res = await api.searchByTrain(9)
        expect(ax.get).toHaveBeenCalledTimes(1)
        expect(ax.get).toHaveBeenCalledWith('train/meal/9')
        expect(res).toBe(fake)
    })

    it('searchByTrain：异常向上传递', async () => {
        ax.get.mockRejectedValueOnce(new Error('net'))
        await expect(api.searchByTrain(9)).rejects.toThrow('net')
    })

    it('searchTrainMealOrder：GET train/meal/orders/:userId', async () => {
        const fake = { data: { orderId: 'M-1' } }
        ax.get.mockResolvedValueOnce(fake)

        const res = await api.searchTrainMealOrder('U-1')
        expect(ax.get).toHaveBeenCalledWith('train/meal/orders/U-1')
        expect(res).toBe(fake)
    })

    it('searchTrainMealOrder：异常向上传递', async () => {
        ax.get.mockRejectedValueOnce(new Error('meal err'))
        await expect(api.searchTrainMealOrder('U-1')).rejects.toThrow('meal err')
    })

    it('searchTrainMealOrderBySeatOrder：GET by ticket id', async () => {
        const fake = { data: { orderId: 'M-2' } }
        ax.get.mockResolvedValueOnce(fake)

        const res = await api.searchTrainMealOrderBySeatOrder('T-9')
        expect(ax.get).toHaveBeenCalledWith('train/meal/orders/by-ticket/T-9')
        expect(res).toBe(fake)
    })

    it('startPayment：POST /train/meal/get & 透传返回', async () => {
        const payload = { trainId: 9, mealId: 3 }
        const fake = { data: { payId: 'PM-1' } }
        ax.post.mockResolvedValueOnce(fake)

        const res = await api.startPayment(payload)
        expect(ax.post).toHaveBeenCalledTimes(1)
        expect(ax.post).toHaveBeenCalledWith('/train/meal/get', payload)
        expect(res).toBe(fake)
    })

    it('startPayment：异常向上传递', async () => {
        ax.post.mockRejectedValueOnce(new Error('pay fail'))
        await expect(api.startPayment({})).rejects.toThrow('pay fail')
    })

    it('doAsync：GET /train/meal/status with { params: orderId }', async () => {
        const fake = { data: { status: 'OK' } }
        ax.get.mockResolvedValueOnce(fake)

        const res = await api.doAsync('ORD-1')
        expect(ax.get).toHaveBeenCalledTimes(1)
        expect(ax.get).toHaveBeenCalledWith('/train/meal/status', { params: 'ORD-1' })
        expect(res).toBe(fake)
    })

    it('refundMeal：POST train/meal/refund', async () => {
        const payload = { orderId: 'ORD-1' }
        const fake = { data: { refunded: true } }
        ax.post.mockResolvedValueOnce(fake)

        const res = await api.refundMeal(payload)
        expect(ax.post).toHaveBeenCalledWith('train/meal/refund', payload)
        expect(res).toBe(fake)
    })
})
