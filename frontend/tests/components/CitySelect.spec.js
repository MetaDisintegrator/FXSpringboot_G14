import { render, screen, fireEvent } from '@testing-library/vue'
import { describe, it, expect, vi } from 'vitest'
import CitySelect from '@/components/CitySelect.vue'

// 一个简单的父组件，用来测试 @click.stop 是否阻止冒泡到外层
const Parent = {
    components: { CitySelect },
    props: ['field'],
    template: `
    <div data-testid="outside" @click="onOutsideClick">
      <CitySelect :field="field" />
    </div>
  `,
    methods: {
        onOutsideClick() {
            this.$emit('outside-click')
        }
    }
}

describe('CitySelect.vue', () => {
    it('渲染 12 个热门城市并包含常见城市名称', () => {
        render(CitySelect, { props: { field: 'from' } })
        // grid 里应有 12 个 span
        const items = screen.getAllByText(/北京|上海|广州|深圳|杭州|成都|重庆|南京|武汉|西安|郑州|长沙/)
        // 注意：上面用的是 OR 匹配，会匹配到其中若干；更稳妥的是按 role/标签来数
        // 这里我们直接根据样式结构：所有城市是 .city-grid 内的 span
        const grid = document.querySelector('.city-grid')
        const spans = grid.querySelectorAll('span')
        expect(spans.length).toBe(12)

        // 抽查几个城市
        expect(screen.getByText('北京')).toBeInTheDocument()
        expect(screen.getByText('上海')).toBeInTheDocument()
        expect(screen.getByText('广州')).toBeInTheDocument()
    })

    it('点击城市会 emit("select", { field, city })', async () => {
        const { emitted } = render(CitySelect, { props: { field: 'to' } })
        const city = screen.getByText('杭州')
        await fireEvent.click(city)

        expect(emitted().select).toBeTruthy()
        const payload = emitted().select[0][0]
        expect(payload).toEqual({ field: 'to', city: '杭州' })
    })

    it('点击关闭按钮会 emit("close")', async () => {
        const { emitted } = render(CitySelect, { props: { field: 'from' } })
        const closeBtn = screen.getByRole('button', { name: '×' })
        await fireEvent.click(closeBtn)

        expect(emitted().close).toBeTruthy()
        expect(emitted().close.length).toBe(1)
    })

    it('@click.stop：点击内部元素不会冒泡到外层', async () => {
        const outsideSpy = vi.fn()
        const { emitted } = render(Parent, {
            props: { field: 'from' },
            attrs: { onOutsideClick: outsideSpy }
        })

        // 点击一个城市（事件会在 CitySelect 根元素被 stop）
        await fireEvent.click(screen.getByText('成都'))

        expect(outsideSpy).not.toHaveBeenCalled()

        // 点击真正的外层区域，父级应该能收到
        await fireEvent.click(screen.getByTestId('outside'))
        expect(outsideSpy).toHaveBeenCalledTimes(1)

        // 另外也确认子组件确实发出了 select
        // 注意：这里 emitted() 是 Parent 的，不能直接读到子组件的。
        // 所以前一个用例已经验证了 select 事件，这里只关心冒泡行为。
    })
})
