import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import ProgressBar from '@/components/ProgressBar.vue'

describe('ProgressBar.vue', () => {
    it('默认 currentStep=1，第一步高亮，共3步', () => {
        const wrapper = mount(ProgressBar) // 未传入 props，使用默认值 1
        const steps = wrapper.findAll('.progress-step')
        expect(steps.length).toBe(3)

        // 第一步高亮
        expect(steps[0].classes()).toContain('active')
        expect(steps[1].classes()).not.toContain('active')
        expect(steps[2].classes()).not.toContain('active')

        // 文案检查
        expect(wrapper.text()).toContain('填写')
        expect(wrapper.text()).toContain('支付')
        expect(wrapper.text()).toContain('完成')
    })

    it('当 currentStep=2 时，第二步高亮', () => {
        const wrapper = mount(ProgressBar, {
            props: { currentStep: 2 },
        })
        const steps = wrapper.findAll('.progress-step')
        expect(steps[1].classes()).toContain('active')
        expect(steps[0].classes()).not.toContain('active')
        expect(steps[2].classes()).not.toContain('active')
    })
})
