import { shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const h = vi.hoisted(() => ({ push: vi.fn(), replace: vi.fn() }))
vi.mock('vue-router', () => ({
    useRouter: () => ({ push: h.push, replace: h.replace }),
    useRoute: () => ({ name: 'HotelBooking', params: {}, query: {} }),
}))

import { createTestingPinia } from '@pinia/testing'
import HotelBooking from '@/pages/HotelBooking.vue'

const mountPage = () =>
    shallowMount(HotelBooking, {
        global: {
            plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: true })],
            stubs: [
                'RouterLink','RouterView',
                'ElForm','ElFormItem','ElInput','ElSelect','ElOption','ElDatePicker',
                'ElButton','ElCard','ElRow','ElCol','ElTable','ElTableColumn'
            ],
        },
    })

describe('HotelBooking Page', () => {
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
