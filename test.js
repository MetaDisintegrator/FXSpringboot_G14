import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    // 阶梯式加压，便于触发 HPA 扩容（示例 15 分钟）
    stages: [
        { duration: '3m', target: 20 },   // 预热
        { duration: '5m', target: 80 },   // 拉高负载（观察是否扩容）
        { duration: '4m', target: 150 },  // 峰值（应触发扩容）
        { duration: '3m', target: 0 },    // 降载（观察回收）
    ],
    thresholds: {
        'http_req_failed': ['rate<0.02'],        // 失败率 < 2%
        'http_req_duration{endpoint:train}': ['p(95)<400'],  // p95 < 400ms
        'http_req_duration{endpoint:hotel}': ['p(95)<450'],
        'http_req_duration{endpoint:user}':  ['p(95)<350'],
    },
    // 输出详细结果用来对比“微服务化前后”的性能
    summaryTrendStats: ['avg','min','max','p(90)','p(95)','p(99)'],
};

const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:30080'; // 按需替换
const PATHS = {
    train: '/api/train/search?from=BJ&to=SH&date=2025-09-10',
    hotel: '/api/hotel/search?city=Shanghai&checkin=2025-09-10&nights=2',
    user:  '/api/user/login', // POST 示例
};

export default function () {
    // GET 并行请求（典型读多写少场景）
    const getRes = http.batch([
        ['GET', `${BASE_URL}${PATHS.train}`, null, { tags: { endpoint: 'train' } }],
        ['GET', `${BASE_URL}${PATHS.hotel}`, null, { tags: { endpoint: 'hotel' } }],
    ]);

    // 简单校验
    check(getRes[0], { 'train 200': r => r.status === 200 });
    check(getRes[1], { 'hotel 200': r => r.status === 200 });

    // POST 登录（写请求）
    const loginRes = http.post(
        `${BASE_URL}${PATHS.user}`,
        JSON.stringify({ username: 'test', password: 'test123' }),
        { headers: { 'Content-Type': 'application/json' }, tags: { endpoint: 'user' } }
    );
    check(loginRes, { 'user 200/201/204': r => [200,201,204].includes(r.status) });

    sleep(1);
}
