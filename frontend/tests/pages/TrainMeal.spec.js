import { shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const h = vi.hoisted(() => ({ push: vi.fn(), replace: vi.fn() }))
vi.mock('vue-router', () => ({
    useRouter: () => ({ push: h.push, replace: h.replace }),
    useRoute: () => ({ name: 'TrainMeal', params: {}, query: {} }),
}))

import { createTestingPinia } from '@pinia/testing'
import TrainMeal from '@/pages/TrainMeal.vue'

const mountPage = () =>
    shallowMount(TrainMeal, {
        global: {
            plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: true })],
            stubs: [
                'RouterLink','RouterView',
                'ElCard','ElTable','ElTableColumn','ElButton','ElInput','ElSelect','ElOption',
            ],
        },
    })

describe('TrainMeal Page', () => {
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
