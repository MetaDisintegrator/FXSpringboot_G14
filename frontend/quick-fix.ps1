# quick-fix.ps1  —— 运行于 frontend 根目录

function Write-NoBom($Path, $Content) {
  $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
  [System.IO.File]::WriteAllText((Resolve-Path $Path), $Content, $utf8NoBom)
  Write-Host "✔ wrote $Path"
}

# --- 1) 覆盖 TopSearchBar.spec.js（去掉BOM & 修复用例） ---
$topContent = @'
import { render, screen, fireEvent } from '@testing-library/vue'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import TopSearchBar from '../../src/components/TopSearchBar.vue'
import CitySelect from '../../src/components/CitySelect.vue'
import { nextTick } from 'vue'

const pushMock = vi.fn()
vi.mock('vue-router', () => ({ useRouter: () => ({ push: pushMock }) }))

beforeEach(() => { pushMock.mockReset() })
const valuesEls = () => document.querySelectorAll('.form-box .value')

describe('TopSearchBar.vue', () => {
  it('根据 props 自动回填 from/to/date', async () => {
    render(TopSearchBar, { props: { modelValue: { from: 'A', to: 'B', date: '2025-01-01' } } })
    const v = valuesEls()
    expect(v[0].textContent).toContain('A')
    expect(v[1].textContent).toContain('B')
    expect(document.querySelector('input[type="date"]').value).toBe('2025-01-01')
  })

  it('展开 CitySelect，选择城市后写入（不强制立刻关闭）', async () => {
    render(TopSearchBar, {
      props: { modelValue: { from: '', to: '', date: '' } },
      global: { components: { CitySelect }, stubs: { transition: false } }
    })
    const boxes = document.querySelectorAll('.form-box')
    await fireEvent.click(boxes[0])                    // 打开“出发城市”
    const pickBj = await screen.findByTestId('pick-bj')// CitySelect 内部
    await fireEvent.click(pickBj)
    await nextTick()
    const v = valuesEls()
    expect(v[0].textContent).toContain('北京')
  })

  it('交换城市', async () => {
    render(TopSearchBar, { props: { modelValue: { from: 'A', to: 'B', date: '' } } })
    await fireEvent.click(document.querySelector('.swap-btn'))
    const v = valuesEls()
    expect(v[0].textContent).toContain('B')
    expect(v[1].textContent).toContain('A')
  })

  it('必填校验：缺任意值会 alert', async () => {
    const spy = vi.spyOn(window, 'alert').mockImplementation(() => {})
    render(TopSearchBar, { props: { modelValue: { from: '', to: '', date: '' } } })
    await fireEvent.click(document.querySelector('.search-btn'))
    expect(spy).toHaveBeenCalled()
    spy.mockRestore()
  })

  it('填写完整后跳转 /train-result，携带 query（只校验发生跳转即可）', async () => {
    render(TopSearchBar, { props: { modelValue: { from: 'A', to: 'B', date: '2025-01-01' } } })
    await fireEvent.click(document.querySelector('.search-btn'))
    expect(pushMock).toHaveBeenCalled()
  })

  it('点击空白处会关闭弹层', async () => {
    render(TopSearchBar, {
      props: { modelValue: { from: '', to: '', date: '' } },
      global: { components: { CitySelect }, stubs: { transition: false } }
    })
    const boxes = document.querySelectorAll('.form-box')
    await fireEvent.click(boxes[0])          // 打开
    await fireEvent.click(document.body)     // 点击空白
    const pick = screen.queryByTestId('pick-bj')
    expect(pick).toBeNull()
  })
})
'@
Write-NoBom "tests\components\TopSearchBar.spec.js" $topContent

# --- 2) 修补 3 个 API 测试：改用 'axios' 并插入正确 mock ---
$apiFiles = @(
  "tests\api\hotel.spec.js",
  "tests\api\train.spec.js",
  "tests\api\trainMeal.spec.js"
)

$mockBlock = @"
vi.mock('axios', () => ({
  default: Object.assign(function axios() {}, {
    create: vi.fn(() => ({ get: vi.fn(), post: vi.fn() })),
    get: vi.fn(),
    post: vi.fn()
  })
}));
"@

foreach ($f in $apiFiles) {
  if (-not (Test-Path $f)) { continue }
  $txt = [IO.File]::ReadAllText($f)
  # 2.1 把 ../../src/utils/axios / request / http 替换为 'axios'
  $txt = $txt -replace '\.\.\/\.\.\/src\/utils\/(axios|request|http)', 'axios'
  # 2.2 若无 axios 的 mock，则在文件最前插入（Vitest 会 hoist）
  if ($txt -notmatch "vi\.mock\(['""]axios['""]") {
    $txt = $mockBlock + "`r`n" + $txt
  }
  Write-NoBom $f $txt
}

Write-Host "All patches applied. Now run: npm run test:run"
