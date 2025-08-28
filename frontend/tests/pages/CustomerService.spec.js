import { shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const h = vi.hoisted(() => ({ push: vi.fn(), replace: vi.fn() }))
vi.mock('vue-router', () => ({
    useRouter: () => ({ push: h.push, replace: h.replace }),
    useRoute: () => ({ name: 'CustomerService', params: {}, query: {} }),
}))

import { createTestingPinia } from '@pinia/testing'
import CustomerService from '@/pages/CustomerService.vue'

const mountPage = () =>
    shallowMount(CustomerService, {
        global: {
            plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: true })],
            stubs: ['RouterLink','RouterView','ElCard','ElButton','ElForm','ElFormItem','ElInput','ElIcon'],
        },
    })

describe('CustomerService Page', () => {
    beforeEach(() => { h.push.mockClear(); h.replace.mockClear() })

    it('renders without crashing', () => {
        const wrapper = mountPage()
        expect(wrapper.exists()).toBe(true)
    })

    it('router mock alive', async () => {
        const wrapper = mountPage()
        await wrapper.vm.$nextTick()
        expect(h.push).not.toHaveBeenCalled()
    })
})
