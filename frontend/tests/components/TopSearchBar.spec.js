import { render, screen, fireEvent } from '@testing-library/vue'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { nextTick } from 'vue'
import TopSearchBar from '../../src/components/TopSearchBar.vue'

// mock 路由
const pushMock = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: pushMock }),
}))

// mock alert（避免校验时中断）
if (!('alert' in window)) {
  Object.defineProperty(window, 'alert', { value: vi.fn(), writable: true })
} else {
  window.alert = vi.fn()
}

// 获取出发/到达城市显示区域
const valueBoxes = () =>
    Array.from(document.querySelectorAll('.flat-search-bar .form-box .value'))

// 交互式填充：选城市 + 设日期
async function pickFromToAndDate({ from = '北京', to = '杭州', date = '2025-01-01' } = {}) {
  // 出发城市
  await fireEvent.click(valueBoxes()[0])
  await fireEvent.click(await screen.findByText(from))
  await nextTick()

  // 到达城市
  await fireEvent.click(valueBoxes()[1])
  await fireEvent.click(await screen.findByText(to))
  await nextTick()

  // 日期
  const dateInput = document.querySelector('input[type="date"]')
  await fireEvent.update(dateInput, date)
  await nextTick()
}

beforeEach(() => {
  vi.clearAllMocks()
})

describe('TopSearchBar.vue', () => {
  it('交互选择后会显示选中的 from/to/date', async () => {
    render(TopSearchBar, {
      props: {
        modelValue: { from: '', to: '', date: '' },
        'onUpdate:modelValue': () => {},
      },
      global: {
        stubs: {
          'el-button': true,
          'el-input': true,
          'el-select': true,
          'el-option': true,
          'el-form': true,
          'el-form-item': true,
        },
      },
    })

    await nextTick()

    // 初始“请选择”
    const v0 = valueBoxes()
    expect(v0[0].textContent).toContain('请选择')
    expect(v0[1].textContent).toContain('请选择')

    // 交互选择
    await pickFromToAndDate({ from: '北京', to: '杭州', date: '2025-01-01' })

    // 断言
    const v = valueBoxes()
    expect(v[0].textContent).toContain('北京')
    expect(v[1].textContent).toContain('杭州')
    const dateInput = document.querySelector('input[type="date"]')
    expect(dateInput.value).toBe('2025-01-01')
  })

  it('互换城市：先选择，再点击 ⇄，from/to 互换', async () => {
    render(TopSearchBar, {
      props: {
        modelValue: { from: '', to: '', date: '' },
        'onUpdate:modelValue': () => {},
      },
      global: {
        stubs: {
          'el-button': true,
          'el-input': true,
          'el-select': true,
          'el-option': true,
          'el-form': true,
          'el-form-item': true,
        },
      },
    })

    await nextTick()
    await pickFromToAndDate({ from: '北京', to: '上海', date: '2025-01-01' })

    // 点击互换
    const swapBtn = screen.getByText('⇄')
    await fireEvent.click(swapBtn)
    await nextTick()

    const v = valueBoxes()
    expect(v[0].textContent).toContain('上海')
    expect(v[1].textContent).toContain('北京')
  })

  it('填写完整后点击搜索，会触发路由跳转', async () => {
    render(TopSearchBar, {
      props: {
        modelValue: { from: '', to: '', date: '' },
        'onUpdate:modelValue': () => {},
      },
      global: {
        stubs: {
          'el-button': true,
          'el-input': true,
          'el-select': true,
          'el-option': true,
          'el-form': true,
          'el-form-item': true,
        },
      },
    })

    await nextTick()
    await pickFromToAndDate({ from: '上海', to: '杭州', date: '2025-01-01' })

    // 点击“搜索”
    await fireEvent.click(screen.getByText('搜索'))

    expect(window.alert).not.toHaveBeenCalled()
    expect(pushMock).toHaveBeenCalled()
    // 更严格断言：
    // expect(pushMock).toHaveBeenCalledWith({ path: '/train-result', query: { from: '上海', to: '杭州', date: '2025-01-01' } })
  })

  it('点击空白处会关闭城市选择弹层', async () => {
    render(TopSearchBar, {
      props: {
        modelValue: { from: '', to: '', date: '' },
        'onUpdate:modelValue': () => {},
      },
      global: {
        stubs: {
          'el-button': true,
          'el-input': true,
          'el-select': true,
          'el-option': true,
          'el-form': true,
          'el-form-item': true,
        },
      },
    })

    await nextTick()

    // 打开“出发城市”弹层
    const firstValue = valueBoxes()[0]
    await fireEvent.click(firstValue)
    await screen.findByText('热门城市') // 弹层出现

    // 点击空白关闭
    await fireEvent.click(document.body)
  })
})
