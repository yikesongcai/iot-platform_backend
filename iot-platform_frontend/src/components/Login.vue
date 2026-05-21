<template>
  <div class="login-container">
    <div class="login-form">
      <div class="logo-area">
        <span class="logo-text">温室大棚环境数据监控系统</span>
      </div>
      <div class="form-content">
        <div class="form-title">
          <h1>欢迎回来</h1>
          <h4>请登录您的账户</h4>
        </div>
        <div class="form-inputs">
          <div class="input-item">
            <input
                type="text"
                class="input-field"
                placeholder=" "
                v-model="username"
                @focus="handleInputFocus"
                @blur="handleInputBlur"
            />
            <label class="input-label">用户名</label>
          </div>
          <div class="input-item">
            <input
                type="password"
                class="input-field"
                placeholder=" "
                v-model="password"
                @focus="handleInputFocus"
                @blur="handleInputBlur"
            />
            <label class="input-label">密码</label>
          </div>
          <button class="login-btn" @click="login">登 录</button>
        </div>
      </div>
    </div>
    <div class="login-bg">
      <img src="../assets/greenhouse-illustration.png" alt="温室大棚监控">
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const userStore = useUserStore();
const username = ref('');
const password = ref('');

function handleInputFocus(event) {
  event.target.parentNode.classList.add('focused');
}

function handleInputBlur(event) {
  event.target.parentNode.classList.remove('focused');
}

async function login() {
  if (!username.value || !password.value) {
    alert('请输入用户名和密码');
    return;
  }

  try {
    const response = await axios.post('/api/user/login', {
      userId: null,
      username: username.value,
      password: password.value
    });

    if (response.status === 200 && response.data?.code === 0) {
      const { token, user } = response.data.data;
      userStore.setAuth(token, user);
      router.push('/dashboard');
    } else {
      alert(response.data?.msg || '登录失败');
    }
  } catch (error) {
    console.error('登录错误:', error);
    alert('登录失败，请稍后重试');
  }
}
</script>

<style lang="less" scoped>
/* 确保引入修改后的CSS文件 */
@import url("../assets/css/login.css");
</style>
