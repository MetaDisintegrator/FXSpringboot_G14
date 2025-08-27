// tests/api/notification.spec.js
import { describe, it, expect, beforeEach, vi } from 'vitest'

const ax = vi.hoisted(() => {
    const get = vi.fn()
    const post = vi.fn()
    const create = vi.fn(() => ({ get, post }))
    return { get, post, create }
})
vi.mock('axios', () => ({ default: { create: ax.create } }))

import { triggerNotification, fetchMessageHistory } from '@/api/notification'

describe('api/notification.js', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('triggerNotification：POST & 透传返回', async () => {
        const fake = { data: { sent: true } }
        ax.post.mockResolvedValueOnce(fake)

        const res = await triggerNotification({ to: 1, content: 'hi' })
        expect(ax.post).toHaveBeenCalledTimes(1)
        expect(res).toBe(fake)
    })

    it('fetchMessageHistory：GET & 透传返回', async () => {
        const fake = { data: [{ id: 1, text: 'hello' }] }
        ax.get.mockResolvedValueOnce(fake)

        const res = await fetchMessageHistory(1)
        expect(ax.get).toHaveBeenCalledTimes(1)
        expect(res).toBe(fake)
    })

    it('triggerNotification：异常向上传递', async () => {
        ax.post.mockRejectedValueOnce(new Error('notify err'))
        await expect(triggerNotification({})).rejects.toThrow('notify err')
    })
})
