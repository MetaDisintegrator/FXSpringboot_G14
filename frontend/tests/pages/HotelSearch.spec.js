import { shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const h = vi.hoisted(() => ({ push: vi.fn(), replace: vi.fn() }))
vi.mock('vue-router', () => ({
    useRouter: () => ({ push: h.push, replace: h.replace }),
    useRoute: () => ({ name: 'HotelSearch', params: {}, query: {} }),
}))

import { createTestingPinia } from '@pinia/testing'
import HotelSearch from '@/pages/HotelSearch.vue'

const mountPage = () =>
    shallowMount(HotelSearch, {
        global: {
            plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: true })],
            stubs: [
                'RouterLink','RouterView',
                'ElForm','ElFormItem','ElInput','ElSelect','ElOption','ElButton',
                'ElCard','ElRow','ElCol','ElPagination','ElTable','ElTableColumn'
            ],
        },
    })

describe('HotelSearch Page', () => {
    beforeEach(() => { h.push.mockClear(); h.replace.mockClear() })

    it('renders without crashing', () => {
        const wrapper = mountPage()
        expect(wrapper.exists()).toBe(true)
    })

    it('router mock alive', () => {
        mountPage()
        expect(h.push).not.toHaveBeenCalled()
    })
})
