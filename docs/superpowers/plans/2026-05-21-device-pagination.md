# Device Management Pagination — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace full device list loading with paginated loading in DeviceManagement.vue

**Architecture:** Replace `POST /device/list` with existing `GET /device/page?page=N&size=N`, add pagination state and el-pagination component. Search resets to page 1. Device stats chart unchanged.

**Tech Stack:** Vue 3, Element Plus el-pagination, existing backend endpoint

---

### Task 1: Add pagination to DeviceManagement.vue

**Files:**
- Modify: `iot-platform_frontend/src/components/DeviceManagement .vue`

- [ ] **Step 1: Add pagination refs in script**

After the `devices` ref declaration (line 80), add:

```javascript
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
```

- [ ] **Step 2: Replace fetchDevices to use paginated endpoint**

Replace the entire `fetchDevices` function (lines 175-199) with:

```javascript
const fetchDevices = async (page = currentPage.value, size = pageSize.value) => {
  try {
    const response = await axios.get('/device/page', {
      params: { page, size }
    });

    if (response.status === 200 && response.data?.code === 0) {
      const pageData = response.data.data;
      devices.value = (pageData.records || []).map(device => {
        return {
          ...device,
          createTime: formatDateTime(device.createTime),
          updateTime: formatDateTime(device.updateTime)
        };
      });
      total.value = pageData.total || 0;
      currentPage.value = pageData.current || page;
    } else {
      console.error('获取设备列表失败');
    }
  } catch (error) {
    console.error('获取设备列表错误:', error);
  }
};
```

- [ ] **Step 3: Replace fetchDevices call in onMounted**

Change line 272 from `fetchDevices()` to `fetchDevices(1, pageSize.value)`:

```javascript
onMounted(() => {
  fetchDevices(1, pageSize.value);
  fetchDeviceStats();
  window.addEventListener('resize', handleResize);
});
```

- [ ] **Step 4: Update addDevice success callback**

In the `addDevice` function (line 250), change `fetchDevices()` to `fetchDevices(currentPage.value, pageSize.value)`:

```javascript
if (response.status === 200) {
  ElMessage.success('设备添加成功');
  showDialog.value = false;
  fetchDevices(currentPage.value, pageSize.value);
  fetchDeviceStats();
  resetForm();
}
```

- [ ] **Step 5: Update searchDevices to search with page reset**

Replace the `searchDevices` function (line 223-225) to filter on the current server-returned data and be compatible with pagination. Since the backend `/device/page` doesn't support search params, we do a full fetch with page=1 and let the user visually scan. If search params are filled, we call a non-paginated search; otherwise, paginated:

```javascript
const searchDevices = () => {
  // If search criteria provided, use list endpoint (no pagination needed for filtered results)
  if (searchParams.value.deviceID || searchParams.value.title) {
    searchWithFilter();
  } else {
    currentPage.value = 1;
    fetchDevices(1, pageSize.value);
  }
};

const searchWithFilter = async () => {
  try {
    const response = await axios.post('/device/list', {
      deviceId: searchParams.value.deviceID,
      title: searchParams.value.title || '',
      productKey: '',
      deviceName: '',
      online: ''
    });

    if (response.status === 200 && response.data && Array.isArray(response.data.data)) {
      devices.value = response.data.data.map(device => ({
        ...device,
        createTime: formatDateTime(device.createTime),
        updateTime: formatDateTime(device.updateTime)
      }));
      total.value = devices.value.length;
      currentPage.value = 1;
    }
  } catch (error) {
    console.error('搜索设备错误:', error);
  }
};
```

- [ ] **Step 6: Add pagination handler**

Add a `handlePageChange` function after `searchDevices`:

```javascript
const handlePageChange = (page) => {
  currentPage.value = page;
  fetchDevices(page, pageSize.value);
};

const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;
  fetchDevices(1, size);
};
```

- [ ] **Step 7: Add el-pagination component in template**

After the `</DeviceList>` closing tag (line 39) and before `</el-card>` (line 40), add:

```html
<div class="pagination-container">
  <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handlePageChange"
  />
</div>
```

- [ ] **Step 8: Commit**

```bash
git add iot-platform_frontend/src/components/DeviceManagement\ .vue
git commit -m "feat: add pagination to device management page"
```
