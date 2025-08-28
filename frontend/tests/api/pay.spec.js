// tests/api/pay.spec.js
import { describe, it, expect, beforeEach, vi } from 'vitest'

const ax = vi.hoisted(() => {
    const get = vi.fn()
    const post = vi.fn()
    const create = vi.fn(() => ({ get, post }))
    return { get, post, create }
})
vi.mock('axios', () => ({ default: { create: ax.create } }))

// 注意：从 '@/api/pay' 导入（不要写成 train）
import { complete, fail, finish } from '@/api/pay'

describe('api/pay.js', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('complete：POST & 透传返回', async () => {
        const fake = { data: { ok: true } }
        ax.post.mockResolvedValueOnce(fake)

        const res = await complete({ orderNumber: 'NO123' })
        expect(ax.post).toHaveBeenCalledTimes(1)
        expect(res).toBe(fake)
    })

    it('complete：异常向上传递', async () => {
        ax.post.mockRejectedValueOnce(new Error('complete err'))
        await expect(complete({ orderNumber: 'NO' })).rejects.toThrow('complete err')
    })

    it('fail：POST & 透传返回', async () => {
        const fake = { data: { status: 'FAILED' } }
        ax.post.mockResolvedValueOnce(fake)

        const res = await fail({ orderNumber: 'NO999' })
        expect(ax.post).toHaveBeenCalledTimes(1)
        expect(res).toBe(fake)
    })

    it('fail：异常向上传递', async () => {
        ax.post.mockRejectedValueOnce(new Error('cancel err'))
        await expect(fail({ orderNumber: 'NO' })).rejects.toThrow('cancel err')
    })

    it('finish：POST or GET（按你实现）& 透传返回', async () => {
        const fake = { data: { done: true } }
        // 不确定你用 get 还是 post，这里先按 post，若实现是 get 就把这两行换成 ax.get
        ax.post.mockResolvedValueOnce(fake)

        const res = await finish({ orderNumber: 'NO777' })
        expect(ax.post).toHaveBeenCalledTimes(1)
        expect(res).toBe(fake)
    })
})
