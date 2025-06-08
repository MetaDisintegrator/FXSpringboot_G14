<template>
  <div class="about-app">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <ul>
        <li @click="goHotelHome" class="sidebar-item">
          <i class="iconfont">&#xe6b6;</i>酒店
        </li>
        <li @click="goTrainHome" class="sidebar-item">
          <i class="iconfont">&#xe608;</i>火车票
        </li>
      </ul>
    </aside>

    <!-- 主内容区域 -->
    <div class="content-wrapper">
      <!-- 顶部导航栏 -->
      <header class="top-nav">
        <div class="logo-container" @click.stop="goHotelHome">
          <img class="logo" src="/images/ICON.png" alt="风行旅行">
          <span class="site-name">风行旅行</span>
        </div>
        <nav class="nav-links">
          <a @click.prevent="goOrders" class="nav-link">我的订单</a>
          <a @click.prevent="goAbout" class="nav-link active">关于我们</a>
        </nav>
      </header>

      <!-- 主体内容 -->
      <div class="content-body">
        <h2 class="page-title">关于我们</h2>
        <div class="sections">
          <section
            v-for="(sec, idx) in sections"
            :key="idx"
            class="section-block"
            :class="`section-block-${idx}`"
          >
            <h3 class="section-title">{{ sec.title }}</h3>
            <div v-if="sec.type === 'team'">
              <ul class="team-list">
                <li v-for="member in teamMembers" :key="member.name">
                  <a :href="member.github" target="_blank" class="link-style">
                    {{ member.name }}
                  </a>
                </li>
              </ul>
            </div>
            <div v-else-if="sec.type === 'repo'">
              <p>
                <a :href="repositoryUrl" target="_blank" class="link-style repo-link">
                  {{ repositoryUrl }}
                </a>
              </p>
            </div>
            <div v-else>
              <p class="section-content">{{ sec.content }}</p>
            </div>
          </section>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const goHotelHome = () => router.push({ name: 'HotelHome' })
const goTrainHome = () => router.push({ name: 'train' })
const goLogin = () => router.push({ name: 'Authentication' })
//const goRegister = () => router.push({ name: 'LoginPage', query: { mode: 'register' } })
const goOrders = () => router.push({ name: 'OrderHistory' })
const goAbout = () => {}

const teamMembers = ref([
  { name: 'Yurh04', github: 'https://github.com/Yurh04' },
  { name: 'hern121', github: 'https://github.com/hern121' },
  { name: 'EasilyCurious', github: 'https://github.com/EasilyCurious' },
  { name: 'sicheng2005', github: 'https://github.com/sicheng2005' },
  { name: 'MetaDisintegrator', github: 'https://github.com/MetaDisintegrator' }
])

const repositoryUrl = ref('https://github.com/MetaDisintegrator/FXSpringboot_G14')

const sections = ref([
  { title: '团队成员', type: 'team' },
  { title: '项目仓库', type: 'repo' },
  { title: '网站介绍', type: 'text', content: '“风行旅行”是一款基于 Vue.js 和 Spring Boot 的旅游服务系统，提供酒店预订、火车票购买等一站式出行服务。用户可以通过直观的界面，快速查询、预订并管理自己的行程。' },
  { title: '致谢', type: 'text', content: '感谢开源社区提供的 Vue.js、Element Plus、Axios 等前端框架与组件，以及 Spring Boot、MyBatis-Plus 等后端技术支持。本项目由以上团队成员共同研发，感谢大家的辛勤付出。' }
])
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600&family=Noto+Sans+SC:wght@400;600&display=swap');

.about-app { display: flex; height: 100vh; overflow: hidden; font-family: 'Noto Sans SC', sans-serif; }
.sidebar {
  position: fixed; top: 0; bottom: 0; width: 160px;
  background: #2c3e50; padding-top: 30px; color: #ecf0f1;
}
.sidebar ul { list-style: none; margin: 0; padding: 0; }
.sidebar-item {
  display: flex; align-items: center; padding: 16px 20px; cursor: pointer;
  transition: background 0.3s, transform 0.3s;
}
.sidebar-item:hover { background: #34495e; transform: translateX(8px); }
.sidebar-item i { margin-right: 8px; font-size: 20px; }

.content-wrapper {
  margin-left: 160px; width: calc(100% - 160px); display: flex; flex-direction: column; height: 100%;
}

.top-nav {
  position: fixed; top: 0; left: 160px; right: 0;
  height: 70px; background: linear-gradient(90deg, #1abc9c, #16a085);
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 32px; z-index: 10;
  box-shadow: 0 2px 10px rgba(0,0,0,0.15);
}
.logo-container {
  display: flex; align-items: center; cursor: pointer;
}
.logo { height: 45px; }
.site-name {
  margin-left: 12px; font-family: 'Montserrat', sans-serif;
  font-size: 28px; color: #fff; font-weight: 600;
}
.nav-links a {
  margin-left: 24px; color: #ecf0f1; font-weight: 500; transition: color 0.3s;
}
.nav-links a:hover, .nav-links a.active { color: #d1f2eb; }
.nav-links a.active { text-decoration: underline; }

.content-body {
  margin-top: 70px; overflow-y: auto; padding: 40px 60px;
  background: linear-gradient(180deg, #ecf0f1 0%, #bdc3c7 100%);
}
.page-title {
  font-family: 'Montserrat', sans-serif; font-size: 32px; text-align: center;
  color: #2c3e50; margin-bottom: 30px;
}

.sections { display: flex; flex-direction: column; gap: 24px; }

.section-block {
  background: #fff; border-radius: 12px; padding: 24px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  opacity: 0; transform: translateY(30px);
  animation: fadeInUp 0.6s forwards;
}

.section-block-0 { animation-delay: 0.2s; }
.section-block-1 { animation-delay: 0.4s; }
.section-block-2 { animation-delay: 0.6s; }
.section-block-3 { animation-delay: 0.8s; }

@keyframes fadeInUp {
  to { opacity: 1; transform: translateY(0); }
}

.section-title {
  font-family: 'Montserrat', sans-serif; font-size: 22px; color: #16a085;
  margin-bottom: 16px;
}
.section-content {
  font-size: 16px; line-height: 1.6; color: #34495e;
}

.team-list { list-style: none; padding: 0; }
.team-list li { margin-bottom: 8px; }
.link-style {
  font-size: 16px; color: #34495e; position: relative; transition: color 0.3s;
}
.link-style:hover { color: #16a085; }
.link-style::after {
  content: ''; position: absolute; left: 0; bottom: -2px;
  width: 0; height: 2px; background: #16a085; transition: width 0.3s;
}
.link-style:hover::after { width: 100%; }

.repo-link { font-weight: 600; }
</style>
