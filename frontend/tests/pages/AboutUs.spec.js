import { shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

// router mock（使用 hoisted，避免 hoist 报错）
const h = vi.hoisted(() => ({
    push: vi.fn(),
    replace: vi.fn(),
}))
vi.mock('vue-router', () => ({
    useRouter: () => ({ push: h.push, replace: h.replace }),
    useRoute: () => ({ name: 'AboutUs', params: {}, query: {} }),
}))

import { createTestingPinia } from '@pinia/testing'
import AboutUs from '@/pages/AboutUs.vue'

const mountPage = () =>
    shallowMount(AboutUs, {
        global: {
            plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: true })],
            stubs: [
                'RouterLink', 'RouterView',
                'ElButton','ElForm','ElFormItem','ElInput','ElSelect','ElOption',
                'ElTable','ElTableColumn','ElCard','ElIcon','ElTabs','ElTabPane',
                'ElPagination','ElDatePicker','ElRadio','ElRadioGroup','ElCheckbox',
                'ElCheckboxGroup','ElSwitch','ElDialog','ElDrawer','ElUpload','ElRow','ElCol'
            ],
        },
    })

describe('AboutUs Page', () => {
    beforeEach(() => {
        h.push.mockClear()
        h.replace.mockClear()
    })

    it('renders without crashing', () => {
        const wrapper = mountPage()
        expect(wrapper.exists()).toBe(true)
    })

    it('router is usable', async () => {
        const wrapper = mountPage()
        await wrapper.vm.$nextTick()
        expect(h.push).not.toHaveBeenCalled()
    })
})
