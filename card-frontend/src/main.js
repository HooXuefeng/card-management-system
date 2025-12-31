import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 核心：最简挂载，避免多余代码导致挂载失败
const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 挂载路由和Element Plus
app.use(router)
app.use(ElementPlus)
// 挂载到#app（确保App.vue里有这个元素）
app.mount('#app')