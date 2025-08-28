import { shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const h = vi.hoisted(() => ({ push: vi.fn(), replace: vi.fn() }))
vi.mock('vue-router', () => ({
    useRouter: () => ({ push: h.push, replace: h.replace }),
    useRoute: () => ({ name: 'Authentication', params: {}, query: {} }),
}))

import { createTestingPinia } from '@pinia/testing'
import AuthPage from '@/pages/auth-page.vue'

const mountPage = () =>
    shallowMount(AuthPage, {
        global: {
            plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: true })],
            stubs: [
                'RouterLink','RouterView',
                'ElButton','ElForm','ElFormItem','ElInput','ElCard','ElIcon',
            ],
        },
    })

describe('auth-page', () => {
    beforeEach(() => { h.push.mockClear(); h.replace.mockClear() })

    it('renders without crashing', () => {
        const wrapper = mountPage()
        expect(wrapper.exists()).toBe(true)
    })

    it('can access pinia store without error', async () => {
        const wrapper = mountPage()
        await wrapper.vm.$nextTick()
        expect(wrapper.exists()).toBe(true)
    })
})
