import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import QrCodeCard from '@/components/QrCodeCard.vue'

describe('QrCodeCard.vue', () => {
    it('默认价格为 ¥120', () => {
        const wrapper = mount(QrCodeCard)
        expect(wrapper.text()).toContain('¥120')
        // 基础区块存在
        expect(wrapper.find('.qr-card').exists()).toBe(true)
        expect(wrapper.find('.fake-qrcode').exists()).toBe(true)
    })

    it('自定义价格渲染', () => {
        const wrapper = mount(QrCodeCard, {
            props: { price: 256.5 },
        })
        expect(wrapper.text()).toContain('¥256.5')
    })
})
