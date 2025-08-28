import { shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const h = vi.hoisted(() => ({ push: vi.fn(), replace: vi.fn() }))
vi.mock('vue-router', () => ({
    useRouter: () => ({ push: h.push, replace: h.replace }),
    useRoute: () => ({ name: 'TrainMealPayment', params: {}, query: {} }),
}))

import { createTestingPinia } from '@pinia/testing'
import TrainMealPayment from '@/pages/TrainMealPayment.vue'

const mountPage = () =>
    shallowMount(TrainMealPayment, {
        global: {
            plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: true })],
            stubs: [
                'RouterLink','RouterView',
                'ElCard','ElButton','ElInput','ElForm','ElFormItem','ElTable','ElTableColumn',
            ],
        },
    })

describe('TrainMealPayment Page', () => {
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
