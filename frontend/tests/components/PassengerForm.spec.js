import { render, screen, fireEvent } from '@testing-library/vue'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import PassengerForm from '@/components/PassengerForm.vue' // 没配别名就用相对路径

describe('PassengerForm.vue', () => {
    beforeEach(() => {
        // mock window.alert，避免测试时真的弹窗
        vi.spyOn(window, 'alert').mockImplementation(() => {})
    })

    it('渲染标题与“新增乘客”按钮', () => {
        render(PassengerForm)
        expect(screen.getByText('乘客信息')).toBeInTheDocument()
        expect(screen.getByRole('button', { name: '➕ 新增乘客' })).toBeInTheDocument()
    })

    it('默认值：区号+86，手机号为空，勾选同步手机号', () => {
        render(PassengerForm)
        const select = screen.getByDisplayValue('+86')
        expect(select).toBeInTheDocument()

        const phone = screen.getByPlaceholderText('请输入手机号')
        expect(phone).toHaveValue('')

        const checkbox = screen.getByRole('checkbox')
        expect(checkbox).toBeChecked()
    })

    it('修改区号、输入手机号、切换勾选状态', async () => {
        render(PassengerForm)

        const select = screen.getByDisplayValue('+86')
        await fireEvent.update(select, '+1')
        // 现在应显示为 +1
        expect(screen.getByDisplayValue('+1')).toBeInTheDocument()

        const phone = screen.getByPlaceholderText('请输入手机号')
        await fireEvent.update(phone, '13800138000')
        expect(phone).toHaveValue('13800138000')

        const checkbox = screen.getByRole('checkbox')
        await fireEvent.click(checkbox)
        expect(checkbox).not.toBeChecked()
        await fireEvent.click(checkbox)
        expect(checkbox).toBeChecked()
    })

    it('点击“新增乘客”会调用 alert（作为示意逻辑）', async () => {
        render(PassengerForm)
        await fireEvent.click(screen.getByRole('button', { name: '➕ 新增乘客' }))
        expect(window.alert).toHaveBeenCalledTimes(1)
    })
})
