import { createApp } from 'vue'
import App from './App.vue'
import router from "@/router/index.js"
import "./assets/css/index.css"
import {createPinia} from "pinia";
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
const pinia = createPinia()


const app = createApp(App)
app.use(router)
app.use(ElementPlus)
// 使用Pinia
app.use(pinia)
app.mount('#app')
