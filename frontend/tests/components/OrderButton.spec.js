import { render, screen, fireEvent } from '@testing-library/vue'
import { describe, it, expect } from 'vitest'
import OrderButton from '@/components/OrderButton.vue'

describe('OrderButton.vue', () => {
    it('渲染按钮文案「立即预订」', () => {
        render(OrderButton)
        const btn = screen.getByRole('button', { name: '立即预订' })
        expect(btn).toBeInTheDocument()
        expect(btn).toBeEnabled()
    })

    it('点击按钮会 emit("order") 一次', async () => {
        const { emitted } = render(OrderButton)
        const btn = screen.getByRole('button', { name: '立即预订' })

        await fireEvent.click(btn)

        expect(emitted().order).toBeTruthy()
        expect(emitted().order.length).toBe(1)
    })

    it('点击外层容器不触发 order（只有按钮绑定了事件）', async () => {
        const { emitted, container } = render(OrderButton)
        const wrapper = container.querySelector('.order-button-wrapper')

        await fireEvent.click(wrapper)

        expect(emitted().order).toBeUndefined()
    })
})
