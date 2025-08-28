// ---- Mock element-plus（完全隔离，避免 hoist 变量提升冲突）----
vi.mock('element-plus', () => ({
    ElMessage: {
        success: vi.fn(),
        error: vi.fn(),
        warning: vi.fn(),
        info: vi.fn(),
    },
}), { virtual: true });

// ---- Mock 路由 ----
const push = vi.fn();
vi.mock('vue-router', () => ({
    useRoute: () => ({ query: { orderId: 'T-001' } }),
    useRouter: () => ({ push }),
}));

// ---- Mock 支付/轮询 API ----
vi.mock('../../src/api/pay', () => ({
    complete: vi.fn().mockResolvedValue({ data: { ok: true } }),
    fail: vi.fn().mockResolvedValue({ data: { ok: true } }),
}));
vi.mock('../../src/api/train', () => ({
    doAsync: vi.fn().mockResolvedValue({
        data: { currentStatus: 'PENDING', remainingTimeSeconds: 300 },
    }),
}));

import { render } from '@testing-library/vue';
import TrainBooking from '../../src/pages/TrainBooking.vue';

describe('TrainBooking.vue', () => {
    it('页面可挂载', () => {
        render(TrainBooking);
        expect(true).toBe(true);
    });
});
