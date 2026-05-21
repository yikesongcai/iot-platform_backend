# Guest (游客) Role Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add guest (unauthenticated read-only) access to the IoT greenhouse platform — guests can view all pages but cannot perform any mutation operations (create, update, delete, control).

**Architecture:** Backend adds JWT authentication with an interceptor that allows GET requests through without token (guest), but blocks mutation requests (POST/PUT/DELETE) for guests via a `@RequireAuth` annotation. Frontend stores JWT from login, attaches it to requests via axios interceptor, and conditionally hides action buttons when no user is logged in.

**Tech Stack:** jjwt 0.12.6 (JWT library), Spring Boot 3.3 interceptor, Vue 3 Pinia store, Vue Router guards

---

### Task 1: Add JWT dependency to pom.xml

**Files:**
- Modify: `iot-platform_backend/pom.xml`

- [ ] **Step 1: Add jjwt dependencies**

Add the following inside `<dependencies>` (after any existing dependency, before `</dependencies>`):

```xml
<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

- [ ] **Step 2: Verify dependency resolution**

Run: `cd iot-platform_backend && ./mvnw dependency:resolve -q 2>&1 | tail -5`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add iot-platform_backend/pom.xml
git commit -m "feat: add jjwt dependency for JWT authentication"
```

---

### Task 2: Create JwtUtil tool class

**Files:**
- Create: `iot-platform_backend/src/main/java/com/atchensong/common/JwtUtil.java`

- [ ] **Step 1: Create JwtUtil.java**

```java
package com.atchensong.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET = "IoT-Greenhouse-JWT-Secret-Key-2026!!";
    private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000; // 24 hours

    private static SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public static String generateToken(Integer userId, String username, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + EXPIRATION_MS))
                .signWith(getKey())
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static boolean isTokenValid(String token) {
        try {
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add iot-platform_backend/src/main/java/com/atchensong/common/JwtUtil.java
git commit -m "feat: add JwtUtil for JWT token generation and validation"
```

---

### Task 3: Enhance BaseContext to store role

**Files:**
- Modify: `iot-platform_backend/src/main/java/com/atchensong/common/BaseContext.java`

- [ ] **Step 1: Add role ThreadLocal**

Replace the entire file content:

```java
package com.atchensong.common;

public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();
    private static ThreadLocal<String> roleLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    public static Long getCurrentId() {
        return threadLocal.get();
    }

    public static void setCurrentRole(String role) {
        roleLocal.set(role);
    }

    public static String getCurrentRole() {
        return roleLocal.get();
    }

    public static boolean isGuest() {
        return getCurrentId() == null;
    }

    public static void clear() {
        threadLocal.remove();
        roleLocal.remove();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add iot-platform_backend/src/main/java/com/atchensong/common/BaseContext.java
git commit -m "feat: extend BaseContext with role and guest check support"
```

---

### Task 4: Create @RequireAuth annotation

**Files:**
- Create: `iot-platform_backend/src/main/java/com/atchensong/common/RequireAuth.java`

- [ ] **Step 1: Create RequireAuth.java**

```java
package com.atchensong.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAuth {
}
```

- [ ] **Step 2: Commit**

```bash
git add iot-platform_backend/src/main/java/com/atchensong/common/RequireAuth.java
git commit -m "feat: add @RequireAuth annotation for protecting mutation endpoints"
```

---

### Task 5: Create AuthInterceptor

**Files:**
- Create: `iot-platform_backend/src/main/java/com/atchensong/config/AuthInterceptor.java`

- [ ] **Step 1: Create AuthInterceptor.java**

```java
package com.atchensong.config;

import com.atchensong.common.BaseContext;
import com.atchensong.common.JwtUtil;
import com.atchensong.common.RequireAuth;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (JwtUtil.isTokenValid(token)) {
                Claims claims = JwtUtil.parseToken(token);
                BaseContext.setCurrentId(Long.valueOf(claims.getSubject()));
                BaseContext.setCurrentRole(claims.get("role", String.class));
            }
        }

        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            RequireAuth requireAuth = handlerMethod.getMethodAnnotation(RequireAuth.class);
            if (requireAuth != null && BaseContext.isGuest()) {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(403);
                response.getWriter().write("{\"code\":1,\"msg\":\"游客无权执行此操作，请先登录\"}");
                return false;
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        BaseContext.clear();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add iot-platform_backend/src/main/java/com/atchensong/config/AuthInterceptor.java
git commit -m "feat: add AuthInterceptor for JWT validation and guest blocking"
```

---

### Task 6: Create WebMvcConfig to register interceptor

**Files:**
- Create: `iot-platform_backend/src/main/java/com/atchensong/config/WebMvcConfig.java`

- [ ] **Step 1: Create WebMvcConfig.java**

```java
package com.atchensong.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/user/login");
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add iot-platform_backend/src/main/java/com/atchensong/config/WebMvcConfig.java
git commit -m "feat: register AuthInterceptor in WebMvcConfig"
```

---

### Task 7: Modify UserController — JWT login + protect write endpoints

**Files:**
- Modify: `iot-platform_backend/src/main/java/com/atchensong/controller/UserController.java`

- [ ] **Step 1: Change login method to return token**

Replace the `login` method (lines 27-41) with:

```java
@PostMapping("/login")
public R<Map<String, Object>> login(@RequestBody User user) {
    log.info("[用户登录]:传入的用户信息{}", user);
    LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(User::getUsername, user.getUsername());
    User one = userService.getOne(queryWrapper);
    if (one == null) {
        return R.error("用户名不存在");
    }
    if (!one.getPassword().equals(user.getPassword())) {
        return R.error("密码错误");
    }
    String token = JwtUtil.generateToken(one.getId(), one.getUsername(), one.getRole());
    Map<String, Object> data = new HashMap<>();
    data.put("token", token);
    data.put("user", one);
    log.info("[用户登录]:登录成功{}", user);
    return R.success(data);
}
```

Add the import at the top (after the existing `import java.util.Map;`):

```java
import com.atchensong.common.JwtUtil;
```

- [ ] **Step 2: Add @RequireAuth to mutation endpoints**

Add `@RequireAuth` annotation to the `save` method:

```java
@RequireAuth
@PostMapping
public R<String> save(@RequestBody User user) {
```

Add `@RequireAuth` to the `update` method:

```java
@RequireAuth
@PutMapping
public R<String> update(@RequestBody User user) {
```

Add `@RequireAuth` to the `delete` method:

```java
@RequireAuth
@DeleteMapping("/{id}")
public R<String> delete(@PathVariable Long id) {
```

Add `@RequireAuth` to the `updateStatus` method:

```java
@RequireAuth
@PutMapping("/status")
public R<String> updateStatus(@RequestParam Long id,
```

Add the import at the top:

```java
import com.atchensong.common.RequireAuth;
```

- [ ] **Step 3: Commit**

```bash
git add iot-platform_backend/src/main/java/com/atchensong/controller/UserController.java
git commit -m "feat: add JWT login response and protect UserController write endpoints"
```

---

### Task 8: Protect DeviceController write endpoints

**Files:**
- Modify: `iot-platform_backend/src/main/java/com/atchensong/controller/DeviceController.java`

- [ ] **Step 1: Add @RequireAuth annotations and import**

Add `@RequireAuth` to `registerDevice`:

```java
@RequireAuth
@PostMapping("/register")
public R<String> registerDevice(@RequestBody Device device) {
```

Add `@RequireAuth` to `updateDevice`:

```java
@RequireAuth
@PutMapping("/update")
public R<String> updateDevice(@RequestBody Device device) {
```

Add `@RequireAuth` to `remove`:

```java
@RequireAuth
@DeleteMapping("/{id}")
public R<Device> remove(@PathVariable Long id) {
```

Add `@RequireAuth` to `send`:

```java
@RequireAuth
@PostMapping("/{deviceId}/send")
public R<Device> send(@PathVariable Long deviceId,@RequestBody String message) {
```

Add imports (after existing imports):

```java
import com.atchensong.common.RequireAuth;
```

- [ ] **Step 2: Commit**

```bash
git add iot-platform_backend/src/main/java/com/atchensong/controller/DeviceController.java
git commit -m "feat: protect DeviceController write endpoints with @RequireAuth"
```

---

### Task 9: Protect AlarmController write endpoint

**Files:**
- Modify: `iot-platform_backend/src/main/java/com/atchensong/controller/AlarmController.java`

- [ ] **Step 1: Add @RequireAuth and import**

Add `@RequireAuth` to `createAlarm`:

```java
@RequireAuth
@PostMapping("/create")
public R<Long> createAlarm(
        @RequestBody AlarmMessage alarm) {
    return R.success(alarmService.createAlarm(alarm));
}
```

Add import:

```java
import com.atchensong.common.RequireAuth;
```

- [ ] **Step 2: Commit**

```bash
git add iot-platform_backend/src/main/java/com/atchensong/controller/AlarmController.java
git commit -m "feat: protect AlarmController create endpoint with @RequireAuth"
```

---

### Task 10: Create Pinia user store

**Files:**
- Create: `iot-platform_frontend/src/stores/user.js`

- [ ] **Step 1: Create user.js store**

```javascript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isGuest = computed(() => !token.value)

  function setAuth(authToken, authUser) {
    token.value = authToken
    user.value = authUser
    localStorage.setItem('token', authToken)
    localStorage.setItem('user', JSON.stringify(authUser))
  }

  function clearAuth() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { token, user, isGuest, setAuth, clearAuth }
})
```

- [ ] **Step 2: Commit**

```bash
git add iot-platform_frontend/src/stores/user.js
git commit -m "feat: add Pinia user store with guest detection"
```

---

### Task 11: Update axios interceptor to attach JWT token

**Files:**
- Modify: `iot-platform_frontend/src/utils/axios.js`

- [ ] **Step 1: Add request interceptor**

Replace the entire file content:

```javascript
import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
    baseURL: process.env.VUE_APP_API_URL || '/api',
    timeout: 10000
})

// Request interceptor — attach JWT token
service.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`
        }
        return config
    },
    error => Promise.reject(error)
)

// Response interceptor
service.interceptors.response.use(
    response => {
        return response.data
    },
    error => {
        if (error.response?.status === 403) {
            ElMessage.error(error.response?.data?.msg || '游客无权执行此操作，请先登录')
        } else {
            ElMessage.error(error.response?.data?.msg || error.message)
        }
        return Promise.reject(error)
    }
)

export default service
```

- [ ] **Step 2: Commit**

```bash
git add iot-platform_frontend/src/utils/axios.js
git commit -m "feat: add JWT token to axios request interceptor"
```

---

### Task 12: Add router guard

**Files:**
- Modify: `iot-platform_frontend/src/router/index.js`

- [ ] **Step 1: Add beforeEach guard**

Add after `const router = createRouter({...})` and before `export default router`:

```javascript
router.beforeEach((to, from, next) => {
  // All routes are accessible — router simply doesn't block navigation
  // Guest vs logged-in differentiation happens in each page's UI via userStore.isGuest
  next()
})
```

- [ ] **Step 2: Commit**

```bash
git add iot-platform_frontend/src/router/index.js
git commit -m "feat: add router guard for auth state tracking"
```

---

### Task 13: Update Login.vue to store JWT token

**Files:**
- Modify: `iot-platform_frontend/src/components/Login.vue`

- [ ] **Step 1: Replace script section**

Replace the entire `<script setup>` block (lines 45-86):

```javascript
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
```

- [ ] **Step 2: Commit**

```bash
git add iot-platform_frontend/src/components/Login.vue
git commit -m "feat: store JWT token and user info on login"
```

---

### Task 14: Update NavBar for guest mode

**Files:**
- Modify: `iot-platform_frontend/src/components/NavBar.vue`

- [ ] **Step 1: Update script to use userStore and show "游客" / "退出登录"**

Replace the `logout` function and add userStore usage in the script (lines 73-115):

Change the `<script setup>` imports and the logout logic:

```javascript
<script setup>
import { ref, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useUserStore } from '@/stores/user';
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

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const isCollapsed = ref(false);
const isFloatMode = ref(false);

const activeMenu = computed(() => {
  return route.path;
});

const toggleCollapse = () => {
  if (!isCollapsed.value) {
    isFloatMode.value = true;
    setTimeout(() => {
      isCollapsed.value = true;
    }, 300);
  } else {
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
```

- [ ] **Step 2: Update the logout button in template to show "游客" vs "退出登录"**

Replace the `<div class="sidebar-footer">` (lines 59-69):

```html
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
```

- [ ] **Step 3: Commit**

```bash
git add iot-platform_frontend/src/components/NavBar.vue
git commit -m "feat: show guest/exit status in NavBar based on auth state"
```

---

### Task 15: Hide action buttons in DeviceManagement for guests

**Files:**
- Modify: `iot-platform_frontend/src/components/DeviceManagement .vue`

- [ ] **Step 1: Add userStore import and wrap "添加设备" button with v-if**

In `<script setup>`, add import:

```javascript
import { useUserStore } from '@/stores/user';
```

And add:

```javascript
const userStore = useUserStore();
```

In template, wrap the "添加设备" button (line 32-34):

```html
<el-button v-if="!userStore.isGuest" type="primary" :icon="CirclePlus" @click="showDialog = true">
  添加设备
</el-button>
```

- [ ] **Step 2: Commit**

```bash
git add iot-platform_frontend/src/components/DeviceManagement\ .vue
git commit -m "feat: hide add-device button for guests"
```

---

### Task 16: Hide action buttons in DeviceItem for guests

**Files:**
- Modify: `iot-platform_frontend/src/components/DeviceItem.vue`

- [ ] **Step 1: Add userStore and wrap action buttons**

In `<script setup>`, add import:

```javascript
import { useUserStore } from '@/stores/user';
```

And add:

```javascript
const userStore = useUserStore();
```

In template, wrap the edit, control, and delete buttons (lines 23-26):

```html
<div class="footer">
  <el-button type="primary" @click="viewDetails">查看</el-button>
  <el-button v-if="!userStore.isGuest" type="success" @click="editDevice">编辑</el-button>
  <el-button type="warning" @click="showDeviceMessages">设备消息</el-button>
  <el-button v-if="!userStore.isGuest" type="danger" :icon="Delete" @click="confirmDeleteDevice" circle/>
</div>
```

Also hide the "控制下发" tab in the device messages dialog. Replace the `<el-tab-pane label="控制下发" name="control">` block (lines 143-155):

```html
<el-tab-pane v-if="!userStore.isGuest" label="控制下发" name="control">
  <el-form label-width="100px" style="margin-top: 20px;">
    <el-form-item label="动作名称:">
      <el-input v-model="controlForm.action" placeholder="请输入动作名称"></el-input>
    </el-form-item>
    <el-form-item label="动作参数:">
      <el-input v-model="controlForm.params" placeholder="请输入参数，如: {key:value}"></el-input>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="sendControlCommand">下发指令</el-button>
    </el-form-item>
  </el-form>
</el-tab-pane>
```

- [ ] **Step 2: Commit**

```bash
git add iot-platform_frontend/src/components/DeviceItem.vue
git commit -m "feat: hide edit/delete/control buttons for guests in DeviceItem"
```

---

### Task 17: Hide action buttons in UserManagement for guests

**Files:**
- Modify: `iot-platform_frontend/src/components/UserManagement.vue`

- [ ] **Step 1: Add userStore and wrap action buttons**

In `<script setup>`, add:

```javascript
import { useUserStore } from '@/stores/user';
```

And add:

```javascript
const userStore = useUserStore();
```

In template, wrap the "新增用户" button (line 37-43):

```html
<el-button
    v-if="!userStore.isGuest"
    type="primary"
    :icon="CirclePlus"
    @click="handleAdd"
>
  新增用户
</el-button>
```

In the status column (line 68-78), wrap the el-switch:

```html
<el-table-column label="状态" width="100">
  <template #default="{ row }">
    <el-switch
        v-if="!userStore.isGuest"
        v-model="row.status"
        active-value="active"
        inactive-value="inactive"
        @change="handleStatusChange(row)"
    />
    <el-tag v-else :type="row.status === 'active' ? 'success' : 'danger'" effect="dark">
      {{ row.status === 'active' ? '启用' : '禁用' }}
    </el-tag>
  </template>
</el-table-column>
```

In the operations column (line 79-93), wrap the edit and delete buttons:

```html
<el-table-column label="操作" width="180" fixed="right">
  <template #default="{ row }">
    <el-button
        v-if="!userStore.isGuest"
        size="small"
        :icon="Edit"
        @click="handleEdit(row)"
    >编辑</el-button>
    <el-button
        v-if="!userStore.isGuest"
        size="small"
        :icon="Delete"
        type="danger"
        @click="handleDelete(row)"
    >删除</el-button>
    <span v-if="userStore.isGuest" style="color: #999">只读</span>
  </template>
</el-table-column>
```

- [ ] **Step 2: Commit**

```bash
git add iot-platform_frontend/src/components/UserManagement.vue
git commit -m "feat: hide add/edit/delete buttons for guests in UserManagement"
```

---

### Task 18: Hide delete buttons in AlarmList for guests

**Files:**
- Modify: `iot-platform_frontend/src/views/alarm/AlarmList.vue`

- [ ] **Step 1: Add userStore and wrap action buttons**

In `<script setup>`, add:

```javascript
import { useUserStore } from '@/stores/user';
```

And add:

```javascript
const userStore = useUserStore();
```

In template, wrap the "批量删除" button (lines 60-67):

```html
<el-button
    v-if="!userStore.isGuest"
    type="danger"
    :icon="Delete"
    @click="handleBatchDelete"
    :disabled="selectedAlarms.length === 0"
>
  批量删除
</el-button>
```

In the operations column (lines 93-111), wrap the delete button:

```html
<el-table-column label="操作" width="150" fixed="right">
  <template #default="{ row }">
    <el-button
        size="small"
        type="text"
        @click.stop="handleViewDetail(row)"
    >
      详情
    </el-button>
    <el-button
        v-if="!userStore.isGuest"
        size="small"
        type="text"
        @click.stop="handleDelete(row)"
        style="color: #f56c6c"
    >
      删除
    </el-button>
  </template>
</el-table-column>
```

- [ ] **Step 2: Commit**

```bash
git add iot-platform_frontend/src/views/alarm/AlarmList.vue
git commit -m "feat: hide delete buttons for guests in AlarmList"
```

---

### Task 19: Verification — start the system and test

- [ ] **Step 1: Start the backend**

```bash
cd iot-platform_backend && ./mvnw spring-boot:run &
```
Wait for "Started IotFinalTrainingApplication" in output (~30s).

- [ ] **Step 2: Start the frontend**

```bash
cd iot-platform_frontend && npm run serve &
```
Wait for dev server to start.

- [ ] **Step 3: Test guest access (no login)**

1. Open browser to `http://localhost:8080/#/dashboard` 
2. Verify dashboard loads with data
3. Navigate to device management — verify "添加设备" button is hidden
4. Navigate to user management — verify "新增用户" / edit / delete buttons are hidden, status shows as tag
5. Navigate to alarm list — verify "批量删除" and individual "删除" buttons are hidden
6. NavBar should show "游客访问"

- [ ] **Step 4: Test login flow**

1. Navigate to `http://localhost:8080/#/login`
2. Login with admin/123456
3. Verify redirect to dashboard
4. Verify NavBar now shows "退出登录"
5. Verify all action buttons are now visible
6. Try adding/deleting a device — should work

- [ ] **Step 5: Test backend API protection**

```bash
# Test that unauthenticated POST to /device/register is rejected
curl -s -X POST http://localhost:8084/device/register \
  -H "Content-Type: application/json" \
  -d '{"title":"test","deviceName":"test","password":"123"}' | jq .
# Expected: {"code":1,"msg":"游客无权执行此操作，请先登录"}
```

- [ ] **Step 6: Test authenticated API works**

```bash
# Login first
TOKEN=$(curl -s -X POST http://localhost:8084/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}' | jq -r '.data.token')

# Test authenticated GET (should work)
curl -s http://localhost:8084/device/panel

# Test authenticated write (should work)
curl -s -X POST http://localhost:8084/device/register \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"test","deviceName":"test2","password":"123"}'
```
