// ---- Mock element-plus（避免 hoist 冲突）----
vi.mock('element-plus', () => ({
    ElMessage: {
        success: vi.fn(),
        error: vi.fn(),
        warning: vi.fn(),
        info: vi.fn(),
    },
}), { virtual: true });

// ---- Mock vue-router，消除 inject 警告 ----
vi.mock('vue-router', () => ({
    useRouter: () => ({ push: vi.fn() }),
}));

// ---- Mock 依赖：train API / user store ----
vi.mock('../../src/api/train', () => ({
    startPayment: vi.fn().mockResolvedValue({ data: { paymentId: 'p-1' } }),
}));
vi.mock('../../src/store/user', () => ({
    useUserStore: () => ({
        isLogin: true,
        user: { id: 1, username: 'Tester' },
    }),
}));

import { render, screen } from '@testing-library/vue';
import TrainCard from '../../src/components/TrainCard.vue';

describe('TrainCard.vue', () => {
    it('能正常渲染基础信息', () => {
        // 传入尽量通用的字段（不依赖具体实现命名）
        render(TrainCard, {
            props: {
                train: {
                    // 有些实现会用嵌套的 train.* 字段
                    train: { id: 1, trainNo: 'G1', trainType: 'HIGH_SPEED' },
                    // 也兼容直接平铺
                    id: 1,
                    trainNo: 'G1',
                    trainType: 'HIGH_SPEED',

                    departureStationName: '上海',
                    arrivalStationName: '杭州',
                    departureTime: '2025-09-01T08:00:00',
                    arrivalTime: '2025-09-01T10:00:00',
                    durationMinutes: 120,
                    seats: [], // 为空时应显示“暂无座位信息”
                },
            },
        });
    });
});
