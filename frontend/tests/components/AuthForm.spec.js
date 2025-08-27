import { render, screen, fireEvent } from '@testing-library/vue'
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest'
import AuthForm from '@/components/AuthForm.vue' // 如果你没配 @ 别名，改成相对路径

// ========== 可变的 Mock 实例（每个用例都会重置） ==========
let mockRouter
let mockUserStore

// mock vue-router 的 useRouter
vi.mock('vue-router', () => ({
    useRouter: () => mockRouter
}))

// mock store：返回当前用例的 mockUserStore
vi.mock('@/store/user', () => ({
    useUserStore: () => mockUserStore
}))

describe('AuthForm.vue', () => {
    beforeEach(() => {
        vi.useFakeTimers()
        mockRouter = { push: vi.fn() }
        mockUserStore = {
            login: vi.fn(),
            register: vi.fn()
        }
    })

    afterEach(() => {
        vi.clearAllMocks()
        vi.useRealTimers()
    })

    // ---------- 登录 ----------
    it('登录：显示用户名与密码，提交成功后发出 success 并跳转 /train', async () => {
        const { emitted } = render(AuthForm, { props: { type: 'login' } })

        // 登陆成功
        mockUserStore.login.mockResolvedValueOnce(true)

        // 界面字段可见性
        expect(screen.queryByPlaceholderText('邮箱')).toBeNull()
        const userInput = screen.getByPlaceholderText('用户名')
        const pwdInput = screen.getByPlaceholderText('密码')
        const btn = screen.getByRole('button', { name: '登 录' })

        await fireEvent.update(userInput, 'user@example.com') // 你的实现里用 username 填到 email
        await fireEvent.update(pwdInput, '123456')
        await fireEvent.click(btn)

        // 调用 userStore.login
        expect(mockUserStore.login).toHaveBeenCalledWith({
            email: 'user@example.com',
            password: '123456'
        })

        // 触发事件 & 跳转
        expect(emitted().success?.[0]?.[0]).toBe('登录成功')
        expect(mockRouter.push).toHaveBeenCalledWith({ name: 'train' })
    })

    it('登录：userStore.login 返回 false 时不触发跳转', async () => {
        render(AuthForm, { props: { type: 'login' } })
        mockUserStore.login.mockResolvedValueOnce(false)

        await fireEvent.update(screen.getByPlaceholderText('用户名'), 'who@x.com')
        await fireEvent.update(screen.getByPlaceholderText('密码'), 'bad')
        await fireEvent.click(screen.getByRole('button', { name: '登 录' }))

        expect(mockUserStore.login).toHaveBeenCalled()
        expect(screen.queryByText('操作失败，请检查信息')).toBeNull() // 组件未显式报错
        expect(mockRouter.push).not.toHaveBeenCalled()
    })

    // ---------- 注册 ----------
    it('注册：缺少必填项时给出“请完整填写所有信息”', async () => {
        render(AuthForm, { props: { type: 'register' } });

        await fireEvent.click(screen.getByRole('button', { name: /注\s*册/i }));
        expect(screen.getByText('请完整填写所有信息')).toBeInTheDocument();
    });


    it('注册：两次密码不一致时提示错误，不调用 userStore.register', async () => {
        render(AuthForm, { props: { type: 'register' } })

        await fireEvent.update(screen.getByPlaceholderText('邮箱'), 'a@b.com')
        await fireEvent.update(screen.getByPlaceholderText('用户名'), 'Alice')
        await fireEvent.update(screen.getByPlaceholderText('密码'), '123456')
        await fireEvent.update(screen.getByPlaceholderText('确认密码'), '654321')

        await fireEvent.click(screen.getByRole('button', { name: '注 册' }))
        expect(await screen.findByText('两次密码不一致')).toBeInTheDocument()
        expect(mockUserStore.register).not.toHaveBeenCalled()
    })

    it('注册：成功时发出 success 并跳转 /train；失败时发出 error', async () => {
        // 成功分支
        const { emitted, rerender } = render(AuthForm, { props: { type: 'register' } })
        mockUserStore.register.mockResolvedValueOnce(true)

        await fireEvent.update(screen.getByPlaceholderText('邮箱'), 'a@b.com')
        await fireEvent.update(screen.getByPlaceholderText('用户名'), 'Bob')
        await fireEvent.update(screen.getByPlaceholderText('密码'), 'pass123')
        await fireEvent.update(screen.getByPlaceholderText('确认密码'), 'pass123')
        // 选择性别（默认 MALE 已选，这里随意点一下）
        const female = screen.getByDisplayValue('FEMALE')
        await fireEvent.click(female)

        await fireEvent.click(screen.getByRole('button', { name: '注 册' }))
        expect(mockUserStore.register).toHaveBeenCalledWith({
            email: 'a@b.com',
            username: 'Bob',
            password: 'pass123',
            gender: 'FEMALE',
            role: 'REGULAR'
        })
        expect(emitted().success?.[0]?.[0]).toBe('注册成功')
        expect(mockRouter.push).toHaveBeenCalledWith({ name: 'train' })

        // 失败分支
        await rerender({ type: 'register' }) // 切 tab 会清空
        mockUserStore.register.mockResolvedValueOnce(false)

        await fireEvent.update(screen.getByPlaceholderText('邮箱'), 'x@y.com')
        await fireEvent.update(screen.getByPlaceholderText('用户名'), 'X')
        await fireEvent.update(screen.getByPlaceholderText('密码'), 'p1')
        await fireEvent.update(screen.getByPlaceholderText('确认密码'), 'p1')
        await fireEvent.click(screen.getByRole('button', { name: '注 册' }))

        // 失败会 emit('error','注册失败')（你的组件这么写的）
        expect(emitted().error?.[0]?.[0]).toBe('注册失败')
    })

    it('切换 type 会清空输入与错误', async () => {
        const { rerender } = render(AuthForm, { props: { type: 'register' } })
        await fireEvent.update(screen.getByPlaceholderText('邮箱'), 'a@b.com')
        await fireEvent.update(screen.getByPlaceholderText('用户名'), 'U')
        await fireEvent.update(screen.getByPlaceholderText('密码'), 'p')
        await fireEvent.update(screen.getByPlaceholderText('确认密码'), 'q')
        await fireEvent.click(screen.getByRole('button', { name: '注 册' })) // 触发错误
        expect(await screen.findByText(/请完整|两次密码/)).toBeInTheDocument()

        await rerender({ type: 'login' })
        // login 模式应只看到 用户名/密码；错误已清空
        expect(screen.getByPlaceholderText('用户名')).toBeInTheDocument()
        expect(screen.getByPlaceholderText('密码')).toBeInTheDocument()
        expect(screen.queryByPlaceholderText('邮箱')).toBeNull()
        expect(screen.queryByText(/请完整|两次密码/)).toBeNull()
    })
})
