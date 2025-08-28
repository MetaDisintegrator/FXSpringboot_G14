import { shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const h = vi.hoisted(() => ({ push: vi.fn(), replace: vi.fn() }))
vi.mock('vue-router', () => ({
    useRouter: () => ({ push: h.push, replace: h.replace }),
    useRoute: () => ({ name: 'MessageCenter', params: {}, query: {} }),
}))

import { createTestingPinia } from '@pinia/testing'
import MessageCenter from '@/pages/MessageCenter.vue'

const mountPage = () =>
    shallowMount(MessageCenter, {
        global: {
            plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: true })],
            stubs: [
                'RouterLink','RouterView',
                'ElCard','ElButton','ElTabs','ElTabPane','ElBadge','ElPagination','ElIcon'
            ],
        },
    })

describe('MessageCenter Page', () => {
    beforeEach(() => { h.push.mockClear(); h.replace.mockClear() })

    it('renders without crashing', () => {
        const wrapper = mountPage()
        expect(wrapper.exists()).toBe(true)
    })

    it('text renders', () => {
        const wrapper = mountPage()
        expect(wrapper.text()).toBeTypeOf('string')
    })
})
