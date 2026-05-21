<template>
  <!-- 悬浮按钮 - 只在折叠状态显示 -->
  <div
      class="float-collapse-btn"
      v-show="isCollapsed && isFloatMode"
      @click="toggleCollapse"
  >
    <el-icon :size="24">
      <Expand />
    </el-icon>
  </div>

  <!-- 主导航栏 -->
  <div
      class="sidebar-container"
      :class="{
      'collapsed': isCollapsed,
      'float-mode': isFloatMode
    }"
  >
    <div class="sidebar-toggle" @click="toggleCollapse">
      <el-icon :size="20">
        <component :is="isCollapsed ? 'Expand' : 'Fold'" />
      </el-icon>
    </div>
    <div class="sidebar-logo">
      <span v-show="!isCollapsed">温室大棚监控</span>
    </div>
    <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        background-color="#2e7d32"
        text-color="#e8f5e9"
        active-text-color="#ffffff"
        :collapse="isCollapsed"
        router
    >
      <el-menu-item index="/dashboard">
        <el-icon><HomeFilled /></el-icon>
        <template #title>首页</template>
      </el-menu-item>
      <el-menu-item index="/management">
        <el-icon><Monitor /></el-icon>
        <template #title>设备模块</template>
      </el-menu-item>
      <el-menu-item index="/user-management">
        <el-icon><User /></el-icon>
        <template #title>用户管理</template>
      </el-menu-item>
      <el-menu-item index="/alarm/list">
        <el-icon><Bell /></el-icon>
        <template #title>告警列表</template>
      </el-menu-item>
      <el-menu-item index="/systemLog">
        <el-icon><Document /></el-icon>
        <template #title>系统日志</template>
      </el-menu-item>
    </el-menu>
    <div class="sidebar-footer">
      <el-button
          type="text"
          @click="handleLogout"
          class="logout-btn"
          :title="isCollapsed ? (userStore.isGuest ? '登录' : '退出') : (userStore.isGuest ? '游客访问中，点击登录' : '退出登录')"
      >
        <el-icon><SwitchButton /></el-icon>
        <span v-show="!isCollapsed">{{ userStore.isGuest ? '游客访问' : '退出登录' }}</span>
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import {
  HomeFilled,
  Monitor,
  User,
  Bell,
  Document,
  SwitchButton,
  Expand,
  Fold
} from '@element-plus/icons-vue';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const isCollapsed = ref(false);
const isFloatMode = ref(false); // 新增：控制是否启用悬浮模式

const activeMenu = computed(() => {
  return route.path;
});

const toggleCollapse = () => {
  if (!isCollapsed.value) {
    // 第一次点击：准备进入悬浮模式
    isFloatMode.value = true;
    setTimeout(() => {
      isCollapsed.value = true;
    }, 300);
  } else {
    // 第二次点击：退出悬浮模式
    isCollapsed.value = false;
    setTimeout(() => {
      isFloatMode.value = false;
    }, 300);
  }
};

const handleLogout = () => {
  if (userStore.isGuest) {
    router.push('/login');
  } else {
    userStore.clearAuth();
    router.push('/login');
  }
};
</script>

<style scoped>
.sidebar-container {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  width: 220px;
  height: 100vh;
  background-color: #2e7d32;
  color: #e8f5e9;
  transition: all 0.3s ease;
  z-index: 1000;
  display: flex;
  flex-direction: column;
}

.sidebar-container.collapsed {
  width: 64px;
}

/* 新增：悬浮模式样式 */
.sidebar-container.collapsed.float-mode {
  transform: translateX(-100%);
  opacity: 0;
  pointer-events: none;
}

.float-collapse-btn {
  position: fixed;
  left: 10px;
  top: 10px;
  width: 40px;
  height: 40px;
  background-color: #2e7d32;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 1001;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}

.float-collapse-btn:hover {
  background-color: #1b5e20;
  transform: scale(1.1);
}

.sidebar-toggle {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #e8f5e9;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-toggle:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.sidebar-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  color: #ffffff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  padding: 0 16px;
  white-space: nowrap;
  overflow: hidden;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 100%;
}

.el-menu-item {
  height: 56px;
  line-height: 56px;
}

.el-menu-item.is-active {
  background-color: #1b5e20 !important;
}

.el-menu-item:hover {
  background-color: rgba(255, 255, 255, 0.1) !important;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.logout-btn {
  width: 100%;
  color: #e8f5e9;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logout-btn:hover {
  color: #ffffff;
}

.el-icon {
  margin-right: 8px;
  font-size: 18px;
}

.sidebar-container.collapsed .el-icon {
  margin-right: 0;
}
</style>
