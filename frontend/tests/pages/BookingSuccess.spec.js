import { shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const h = vi.hoisted(() => ({ push: vi.fn(), replace: vi.fn() }))
vi.mock('vue-router', () => ({
    useRouter: () => ({ push: h.push, replace: h.replace }),
    useRoute: () => ({ name: 'BookingSuccess', params: {}, query: {} }),
}))

import { createTestingPinia } from '@pinia/testing'
import BookingSuccess from '@/pages/BookingSuccess.vue'

const mountPage = () =>
    shallowMount(BookingSuccess, {
        global: {
            plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: true })],
            stubs: ['RouterLink','RouterView','ElCard','ElButton','ElIcon'],
        },
    })

describe('BookingSuccess Page', () => {
    beforeEach(() => { h.push.mockClear(); h.replace.mockClear() })

    it('renders without crashing', () => {
        const wrapper = mountPage()
        expect(wrapper.exists()).toBe(true)
    })

    it('has basic content rendered', () => {
        const wrapper = mountPage()
        expect(wrapper.text().length).toBeGreaterThan(0)
    })
})
