// tests/api/register.spec.js
import { describe, it, expect, beforeEach, vi } from 'vitest'

const ax = vi.hoisted(() => {
    const get = vi.fn()
    const post = vi.fn()
    const create = vi.fn(() => ({ get, post }))
    return { get, post, create }
})
vi.mock('axios', () => ({ default: { create: ax.create } }))

import { register, verifyEmail, login } from '@/api/register'

describe('api/register.js', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('register：POST & 透传返回', async () => {
        const fake = { data: { id: 1, email: 'a@b.com' } }
        ax.post.mockResolvedValueOnce(fake)

        const res = await register({ email: 'a@b.com', password: '123' })
        expect(ax.post).toHaveBeenCalledTimes(1)
        expect(res).toBe(fake)
    })

    it('register：异常向上传递', async () => {
        ax.post.mockRejectedValueOnce(new Error('reg err'))
        await expect(register({})).rejects.toThrow('reg err')
    })

    it('verifyEmail：GET/POST（按实现）& 透传返回', async () => {
        const fake = { data: { verified: true } }
        // 如实现用 GET，就改为 ax.get
        ax.post.mockResolvedValueOnce(fake)

        const res = await verifyEmail({ token: 'xyz' })
        expect(ax.post).toHaveBeenCalledTimes(1)
        expect(res).toBe(fake)
    })

    it('login：POST & 透传返回', async () => {
        const fake = { data: { token: 'jwt' } }
        ax.post.mockResolvedValueOnce(fake)

        const res = await login({ email: 'a@b.com', password: '123' })
        expect(ax.post).toHaveBeenCalledTimes(1)
        expect(res).toBe(fake)
    })
})
