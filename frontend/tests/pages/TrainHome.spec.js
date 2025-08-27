import { render, screen, fireEvent } from '@testing-library/vue'
import { vi, describe, it, expect, beforeEach } from 'vitest'
import TrainHome from '@/pages/TrainHome.vue'

// 子组件简单 stub
vi.mock('@/components/TopSearchBar.vue', () => ({ default: { template: '<div>TopSearchBar</div>' } }))
vi.mock('@/components/LoginNotice.vue', () => ({ default: { template: '<div>LoginNotice</div>' } }))

// 路由 mock
let mockRouter, mockRoute
vi.mock('vue-router', () => ({
    useRouter: () => mockRouter,
    useRoute: () => mockRoute
}))

// API mock
const api = { searchByDepartureTime: vi.fn() }
vi.mock('@/api/train', () => ({
    searchByDepartureTime: (...a) => api.searchByDepartureTime(...a)
}))

describe('TrainHome.vue', () => {
    beforeEach(() => {
        mockRouter = { push: vi.fn() }
        mockRoute = { path: '/train' }
        api.searchByDepartureTime.mockReset()
    })

    it('侧边栏：点击“酒店/火车票”跳转', async () => {
        render(TrainHome)
        const hotel = screen.getByText('酒店')
        const train = screen.getByText('火车票')

        await fireEvent.click(hotel)
        expect(mockRouter.push).toHaveBeenCalledWith('/')

        await fireEvent.click(train)
        expect(mockRouter.push).toHaveBeenCalledWith('/train')
    })

    it('点击推荐卡片跳到 /train-result 并带 query', async () => {
        render(TrainHome)
        // 有一个“京沪高铁”卡片
        const card = screen.getByText('京沪高铁').closest('.card')
        await fireEvent.click(card)
        expect(mockRouter.push).toHaveBeenCalledWith({
            path: '/train-result',
            query: { fromCity: '北京', toCity: '上海' }
        })
    })

    it('切换热门出发城市会请求目的地最低价格', async () => {
        // 模拟 API 返回
        api.searchByDepartureTime.mockResolvedValue({ data: [{ price: 199 }] })

        render(TrainHome)

        // 点击 city-tabs 中“北京”
        const beijingBtn = screen.getAllByRole('button', { name: '北京' })[0]
        await fireEvent.click(beijingBtn)

        // 每个目的地都会调用一次
        expect(api.searchByDepartureTime).toHaveBeenCalled()
        // 校验部分参数（只抽查一次调用）
        const args = api.searchByDepartureTime.mock.calls[0][0]
        expect(args).toMatchObject({
            departureStation: '北京',
            arrivalStation: expect.any(String),
            departureDate: expect.any(String)
        })
    })
})
