import { render, screen, fireEvent } from '@testing-library/vue'
import { describe, it, expect } from 'vitest'
import TrainFilter from '@/components/TrainFilter.vue'

const sampleTrains = [
    {
        train: { trainType: 'HIGH_SPEED', fromStation: '上海', toStation: '杭州' },
        trainSeats: [
            { seatType: 'SECOND_CLASS_SEAT' },
            { seatType: 'FIRST_CLASS_SEAT' }
        ]
    },
    {
        train: { trainType: 'GREEN_TRAIN', fromStation: '南京', toStation: '苏州' },
        trainSeats: [
            { seatType: 'SECOND_CLASS_SEAT' },
            { seatType: 'BUSINESS_CLASS_SEAT' }
        ]
    }
]

describe('TrainFilter.vue', () => {
    it('从 trains 计算选项（车型/座席/站点）', () => {
        render(TrainFilter, { props: { trains: sampleTrains, expanded: true } })
        // 车型（labelMap映射展示）
        expect(screen.getByText('高铁/动车')).toBeInTheDocument()
        expect(screen.getByText('绿皮/城际')).toBeInTheDocument()
        // 座席（展开时显示）
        + expect(screen.getByText('FIRST_CLASS_SEAT')).toBeInTheDocument()
        + expect(screen.getByText('SECOND_CLASS_SEAT')).toBeInTheDocument()
        + expect(screen.getByText('BUSINESS_CLASS_SEAT')).toBeInTheDocument()
        // 站点（展开时显示）
        expect(screen.getByText('上海')).toBeInTheDocument()
        expect(screen.getByText('南京')).toBeInTheDocument()
        expect(screen.getByText('杭州')).toBeInTheDocument()
        expect(screen.getByText('苏州')).toBeInTheDocument()
    })

    it('勾选会通过 v-model 事件向上传递', async () => {
        const { emitted } = render(TrainFilter, { props: { trains: sampleTrains, expanded: true } })

        // “只看有票”
        const only = screen.getByLabelText('只看有票')
        await fireEvent.click(only)
        expect(emitted()['update:onlyAvailable']?.[0]?.[0]).toBe(true)

        // 勾选一个车型（用 label 文本匹配）
        const typeLabel = screen.getByText('高铁/动车').closest('label')
        const typeInput = typeLabel.querySelector('input')
        await fireEvent.click(typeInput)
        expect(emitted()['update:selectedTypes']?.at(-1)?.[0]).toEqual(['HIGH_SPEED'])

        // 勾选一个时间段
        const timeLabel = screen.getByText('06:00 - 12:00').closest('label')
        await fireEvent.click(timeLabel.querySelector('input'))
        expect(emitted()['update:selectedTimes']?.at(-1)?.[0]).toEqual(['06:00 - 12:00'])

        // 座席
        const seatLabel = screen.getByText('SECOND_CLASS_SEAT').closest('label')
        await fireEvent.click(seatLabel.querySelector('input'))
        expect(emitted()['update:selectedSeatTypes']?.at(-1)?.[0]).toEqual(['SECOND_CLASS_SEAT'])

        // 出发站
        const depLabel = screen.getByText('上海').closest('label')
        await fireEvent.click(depLabel.querySelector('input'))
        expect(emitted()['update:selectedDepartStations']?.at(-1)?.[0]).toEqual(['上海'])

        // 到达站
        const arrLabel = screen.getByText('杭州').closest('label')
        await fireEvent.click(arrLabel.querySelector('input'))
        expect(emitted()['update:selectedArriveStations']?.at(-1)?.[0]).toEqual(['杭州'])
    })

    it('顶部“全部重置”清空所有选择并触发更新', async () => {
        const { emitted } = render(TrainFilter, {
            props: {
                trains: sampleTrains,
                expanded: true,
                onlyAvailable: true,
                selectedTypes: ['HIGH_SPEED'],
                selectedTimes: ['06:00 - 12:00'],
                selectedSeatTypes: ['SECOND_CLASS_SEAT'],
                selectedDepartStations: ['上海'],
                selectedArriveStations: ['杭州']
            }
        })
        await fireEvent.click(screen.getByRole('button', { name: '全部重置' }))
        expect(emitted()['update:onlyAvailable']?.at(-1)?.[0]).toBe(false)
        expect(emitted()['update:selectedTypes']?.at(-1)?.[0]).toEqual([])
        expect(emitted()['update:selectedTimes']?.at(-1)?.[0]).toEqual([])
        expect(emitted()['update:selectedSeatTypes']?.at(-1)?.[0]).toEqual([])
        expect(emitted()['update:selectedDepartStations']?.at(-1)?.[0]).toEqual([])
        expect(emitted()['update:selectedArriveStations']?.at(-1)?.[0]).toEqual([])
    })

    it('小节“重置”只清空对应一组', async () => {
        const { emitted } = render(TrainFilter, {
            props: {
                trains: sampleTrains,
                expanded: true,
                selectedTypes: ['HIGH_SPEED'],
                selectedTimes: ['12:00 - 18:00']
            }
        })

        // 车型小节的“重置”
        const typeReset = screen.getAllByText('重置')[0]
        await fireEvent.click(typeReset)
        expect(emitted()['update:selectedTypes']?.at(-1)?.[0]).toEqual([])

        // 时间小节的“重置”
        const timeReset = screen.getAllByText('重置')[1]
        await fireEvent.click(timeReset)
        expect(emitted()['update:selectedTimes']?.at(-1)?.[0]).toEqual([])
    })

    it('展开/收起 emit("toggle-expand")', async () => {
        const { emitted } = render(TrainFilter, { props: { trains: sampleTrains, expanded: false } })
        await fireEvent.click(screen.getByText('展开 ▼'))
        expect(emitted()['toggle-expand']).toBeTruthy()
    })
})
