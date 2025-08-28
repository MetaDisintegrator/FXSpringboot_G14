import { setActivePinia, createPinia } from 'pinia'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { useUserStore } from '@/store/user'

// mock 后端 API
const api = {
    getCurrentUser: vi.fn(),
    login: vi.fn(),
    logout: vi.fn(),
    register: vi.fn(),
}

vi.mock('@/api/register', () => ({
    getCurrentUser: (...args) => api.getCurrentUser(...args),
    login: (...args) => api.login(...args),
    logout: (...args) => api.logout(...args),
    register: (...args) => api.register(...args),
}))

const mockUser = {
    id: 1,
    email: 'test@example.com',
    username: 'Tester',
    verified: true,
    gender: 'M',
    role: 'user',
}

describe('store/user', () => {
    beforeEach(() => {
        setActivePinia(createPinia())
        vi.restoreAllMocks()
        api.getCurrentUser.mockReset()
        api.login.mockReset()
        api.logout.mockReset()
        api.register.mockReset()
        localStorage.clear()
        vi.spyOn(window.localStorage.__proto__, 'setItem')
        vi.spyOn(window.localStorage.__proto__, 'removeItem')
        vi.spyOn(window.localStorage.__proto__, 'getItem')
    })

    afterEach(() => {
        vi.resetAllMocks()
    })

    it('login 成功', async () => {
        const store = useUserStore()
        api.login.mockResolvedValueOnce()
        api.getCurrentUser.mockResolvedValueOnce({ data: mockUser })

        const ok = await store.login({ email: 'test@example.com', password: '123456' })
        expect(ok).toBe(true)
        expect(api.login).toHaveBeenCalledWith({ email: 'test@example.com', password: '123456' })
        expect(api.getCurrentUser).toHaveBeenCalledTimes(1)
        expect(store.isLoggedIn).toBe(true)
        expect(store.userInfo).toEqual(mockUser)
        expect(localStorage.setItem).toHaveBeenCalled()
    })

    it('login 失败', async () => {
        const store = useUserStore()
        api.login.mockRejectedValueOnce(new Error('bad credentials'))

        const ok = await store.login({ email: 'x@y.z', password: 'wrong' })
        expect(ok).toBe(false)
        expect(store.isLoggedIn).toBe(false)
        expect(store.userInfo).toEqual({
            id: null, email: '', username: '', verified: false, gender: '', role: ''
        })
        expect(localStorage.removeItem).toHaveBeenCalledWith('userStore')
    })

    it('register 成功', async () => {
        const store = useUserStore()
        api.register.mockResolvedValueOnce()
        api.getCurrentUser.mockResolvedValueOnce({ data: mockUser })

        const ok = await store.register({
            email: 'test@example.com',
            username: 'Tester',
            password: '123456',
            gender: 'M',
            role: 'user',
        })
        expect(ok).toBe(true)
        expect(api.register).toHaveBeenCalled()
        expect(store.isLoggedIn).toBe(true)
        expect(store.userInfo).toEqual(mockUser)
    })

    it('logout 本地清空', async () => {
        const store = useUserStore()
        store.isLoggedIn = true
        store.userInfo = { ...mockUser }
        api.logout.mockRejectedValueOnce(new Error('server error'))
        await store.logout()
        expect(store.isLoggedIn).toBe(false)
        expect(store.userInfo).toEqual({
            id: null, email: '', username: '', verified: false, gender: '', role: ''
        })
        expect(localStorage.removeItem).toHaveBeenCalledWith('userStore')
    })

    it('fetchCurrentUser 成功', async () => {
        const store = useUserStore()
        api.getCurrentUser.mockResolvedValueOnce({ data: mockUser })
        await store.fetchCurrentUser()
        expect(store.isLoggedIn).toBe(true)
        expect(store.userInfo).toEqual(mockUser)
        expect(localStorage.setItem).toHaveBeenCalled()
    })

    it('fetchCurrentUser 失败', async () => {
        const store = useUserStore()
        localStorage.setItem('userStore', 'dummy')
        api.getCurrentUser.mockRejectedValueOnce(new Error('unauthorized'))
        await store.fetchCurrentUser()
        expect(store.isLoggedIn).toBe(false)
        expect(store.userInfo).toEqual({
            id: null, email: '', username: '', verified: false, gender: '', role: ''
        })
        expect(localStorage.removeItem).toHaveBeenCalledWith('userStore')
    })

    it('resetState', () => {
        const store = useUserStore()
        store.isLoggedIn = true
        store.userInfo = { ...mockUser }
        store.resetState()
        expect(store.isLoggedIn).toBe(false)
    })
})
