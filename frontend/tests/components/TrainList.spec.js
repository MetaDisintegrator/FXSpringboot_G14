import { render, screen } from '@testing-library/vue'
import { describe, it, expect, vi } from 'vitest'

// stub TrainCard 以便数数量/检测 props 结构（这里简化显示）
vi.mock('@/components/TrainCard.vue', () => ({
    default: {
        props: ['train', 'seats'],
        template: `<div data-testid="train-card">{{ train.trainNumber }}</div>`
    }
}))

import TrainList from '@/components/TrainList.vue'

const mockTrains = [
    {
        train: { id: 'T1', trainNumber: 'G1' },
        trainseats: [{ id: 'S1' }]
    },
    {
        train: { id: 'T2', trainNumber: 'G2' },
        trainseats: [{ id: 'S2' }]
    }
]

describe('TrainList.vue', () => {
    it('空列表时显示占位提示', () => {
        render(TrainList, { props: { trains: [] } })
        expect(screen.getByText('🚄 暂无匹配的车次')).toBeInTheDocument()
    })

    it('渲染多个 TrainCard（使用 stub）', () => {
        render(TrainList, { props: { trains: mockTrains } })
        const cards = screen.getAllByTestId('train-card')
        expect(cards.length).toBe(2)
        expect(screen.getByText('G1')).toBeInTheDocument()
        expect(screen.getByText('G2')).toBeInTheDocument()
    })
})
