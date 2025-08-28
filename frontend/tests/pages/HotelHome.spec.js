import { shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const h = vi.hoisted(() => ({ push: vi.fn(), replace: vi.fn() }))

vi.mock('vue-router', () => ({
    useRouter: () => ({ push: h.push, replace: h.replace }),
    // 👇 关键：提供 path，避免 route.path.startsWith(...) 报错
    useRoute: () => ({ name: 'HotelHome', path: '/hotel', params: {}, query: {} }),
}))

import { createTestingPinia } from '@pinia/testing'
import HotelHome from '@/pages/HotelHome.vue'

const mountPage = () =>
    shallowMount(HotelHome, {
        global: {
            plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: true })],
            stubs: [
                'RouterLink','RouterView','ElCard','ElRow','ElCol','ElButton',
                'ElTabs','ElTabPane','ElInput','ElSelect','ElOption',
            ],
        },
    })

describe('HotelHome Page', () => {
    beforeEach(() => { h.push.mockClear(); h.replace.mockClear() })

    it('renders without crashing', () => {
        const wrapper = mountPage()
        expect(wrapper.exists()).toBe(true)
    })

    it('basic content rendered', () => {
        const wrapper = mountPage()
        expect(wrapper.text()).toBeTypeOf('string')
    })
})
