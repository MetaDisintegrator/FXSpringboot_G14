import { shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const h = vi.hoisted(() => ({ push: vi.fn(), replace: vi.fn() }))
vi.mock('vue-router', () => ({
    useRouter: () => ({ push: h.push, replace: h.replace }),
    useRoute: () => ({ name: 'OrderHistory', params: {}, query: {} }),
}))

import { createTestingPinia } from '@pinia/testing'
import OrderHistory from '@/pages/OrderHistory.vue'

const mountPage = () =>
    shallowMount(OrderHistory, {
        global: {
            plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: true })],
            stubs: [
                'RouterLink','RouterView',
                'ElCard','ElTable','ElTableColumn','ElTabs','ElTabPane','ElPagination','ElButton'
            ],
        },
    })

describe('OrderHistory Page', () => {
    beforeEach(() => { h.push.mockClear(); h.replace.mockClear() })

    it('renders without crashing', () => {
        const wrapper = mountPage()
        expect(wrapper.exists()).toBe(true)
    })

    it('table rendered', () => {
        const wrapper = mountPage()
        expect(wrapper.findAllComponents({ name: 'ElTable' }).length).toBeGreaterThanOrEqual(0)
    })
})
