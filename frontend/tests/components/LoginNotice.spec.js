import { mount } from '@vue/test-utils'
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'

// 先在顶部用 vi.hoisted 创建可复用的 mock
const h = vi.hoisted(() => {
    return {
        mockPush: vi.fn(),
        mockLogout: vi.fn(() => Promise.resolve()),
    }
})

// ---- Mock 路由 ----
vi.mock('vue-router', () => {
    return {
        useRouter: () => ({ push: h.mockPush }),
    }
})

// ---- Mock pinia 的 user store ----
// 注意：组件内使用的是相对路径 ../store/user，解析后的模块 ID 为 /src/store/user
// 所以这里必须 mock '/src/store/user' 才能命中
vi.mock('/src/store/user', () => {
    // 在 factory 里再引入 vue，避免 hoist 期间顶层 import
    const { ref } = require('vue')
    // 用 ref 保持与 storeToRefs 的兼容
    const loggedIn = ref(false)
    const username = ref('')

    // 暴露便于测试时切换登录状态
    return {
        useUserStore: () => ({
            logout: h.mockLogout,
            loggedIn,
            username,
        }),
        // 额外导出给测试用来修改状态（不会影响组件打包）
        __testing__: { loggedIn, username },
    }
})

// 被测组件
import LoginNotice from '@/components/LoginNotice.vue'

// 拿到我们在上面 mock 里导出的测试辅助引用
const { __testing__ } = await vi.importMock('/src/store/user')

describe('LoginNotice.vue', () => {
    beforeEach(() => {
        h.mockPush.mockClear()
        h.mockLogout.mockClear()
        __testing__.loggedIn.value = false
        __testing__.username.value = ''
    })

    afterEach(() => {
        h.mockPush.mockClear()
        h.mockLogout.mockClear()
    })

    it('未登录：显示“登录 / 注册”按钮，点击后跳转 Authentication', async () => {
        __testing__.loggedIn.value = false
        const wrapper = mount(LoginNotice)

        const btn = wrapper.find('button.btn-login')
        expect(btn.exists()).toBe(true)
        expect(wrapper.find('.welcome-user').exists()).toBe(false)

        await btn.trigger('click')
        expect(h.mockPush).toHaveBeenCalledTimes(1)
        expect(h.mockPush).toHaveBeenCalledWith({ name: 'Authentication' })
    })

    it('已登录：显示欢迎与用户名，点击后调用 logout 并跳转', async () => {
        __testing__.loggedIn.value = true
        __testing__.username.value = 'Alice'
        const wrapper = mount(LoginNotice)

        const welcome = wrapper.find('.welcome-user')
        expect(welcome.exists()).toBe(true)
        expect(wrapper.text()).toContain('欢迎')
        expect(wrapper.text()).toContain('Alice')

        await welcome.trigger('click')
        expect(h.mockLogout).toHaveBeenCalledTimes(1)

        // 等待 then 回调
        await Promise.resolve()

        expect(h.mockPush).toHaveBeenCalledTimes(1)
        expect(h.mockPush).toHaveBeenCalledWith({ name: 'Authentication' })
    })
})
