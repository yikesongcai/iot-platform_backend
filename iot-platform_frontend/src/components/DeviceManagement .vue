<template>
  <div class="management-container">
    <div class="content-header">
      <h2>设备管理</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>设备模块</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <el-card class="search-card">
      <div class="search-bar">
        <el-form :inline="true" :model="searchParams" class="search-form">
          <el-form-item label="产品ID">
            <el-input
                v-model="searchParams.deviceID"
                placeholder="请输入产品ID"
                clearable
            />
          </el-form-item>
          <el-form-item label="产品标题">
            <el-input
                v-model="searchParams.title"
                placeholder="请输入产品标题"
                clearable
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="searchDevices">搜索</el-button>
          </el-form-item>
        </el-form>
        <el-button v-if="!userStore.isGuest" type="primary" :icon="CirclePlus" @click="showDialog = true">
          添加设备
        </el-button>
      </div>
    </el-card>

    <el-card>
      <DeviceList :devices="devices" @deviceRemoved="fetchDevices(currentPage, pageSize)" />

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
    </el-card>

    <el-dialog v-model="showDialog" title="添加设备" width="500px">
      <el-form :model="newDevice" label-width="100px" :rules="rules" ref="deviceForm">
        <el-form-item label="设备标题" prop="title">
          <el-input v-model="newDevice.title" placeholder="请输入设备标题" />
        </el-form-item>
        <el-form-item label="设备名称" prop="deviceName">
          <el-input v-model="newDevice.deviceName" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备密码" prop="password">
          <el-input v-model="newDevice.password" placeholder="请输入设备密码" type="password" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="confirmPassword" placeholder="请确认设备密码" type="password" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="addDevice">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, onMounted, nextTick, onBeforeUnmount} from 'vue';
import axios from 'axios';
import * as echarts from 'echarts';
import { Search, CirclePlus, Box, Connection, Bell, DataLine } from '@element-plus/icons-vue';
import DeviceList from './DeviceList.vue';
import {ElMessage} from "element-plus";
import { useUserStore } from '@/stores/user';

const userStore = useUserStore();
const searchParams = ref({
  deviceID: '',
  title: ''
});

const devices = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const showDialog = ref(false);
const newDevice = ref({
  title: '',
  deviceName: '',
  password: ''
});
const confirmPassword = ref('');
const deviceForm = ref(null);

const rules = {
  title: [{ required: true, message: '请输入设备标题', trigger: 'blur' }],
  deviceName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  password: [{ required: true, message: '请输入设备密码', trigger: 'blur' }],
  // confirmPassword: [
  //   { required: true, message: '请确认设备密码', trigger: 'blur' },
  //   {
  //     validator: (rule, value, callback) => {
  //       if (value !== newDevice.value.password) {
  //         callback(new Error('两次输入的密码不一致'));
  //       } else {
  //         callback();
  //       }
  //     },
  //     trigger: 'blur'
  //   }
  // ]
};

const deviceStats = ref({
  totalDevices: 0,
  onlineDevices: 0,
  upMessages: 0,
  warnMessages: 0
});

let chartInstance = null;

const initChart = () => {
  const chartDom = document.getElementById('deviceStatsChart');
  if (!chartDom) return;

  chartInstance = echarts.init(chartDom);
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: 10,
      data: ['总设备数', '在线设备', '数据消息', '告警消息'],
      textStyle: {
        color: '#666'
      }
    },
    series: [
      {
        name: '设备统计',
        type: 'pie',
        radius: ['50%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 18,
            fontWeight: 'bold',
            formatter: '{b}\n{c} ({d}%)'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: deviceStats.value.totalDevices, name: '总设备数', itemStyle: { color: '#2e7d32' } },
          { value: deviceStats.value.onlineDevices, name: '在线设备', itemStyle: { color: '#81c784' } },
          { value: deviceStats.value.upMessages, name: '数据消息', itemStyle: { color: '#4fc3f7' } },
          { value: deviceStats.value.warnMessages, name: '告警消息', itemStyle: { color: '#ff7043' } }
        ]
      }
    ]
  };

  chartInstance.setOption(option);
};

const fetchDevices = async (page = currentPage.value, size = pageSize.value) => {
  try {
    const response = await axios.get('/api/device/page', {
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

const fetchDeviceStats = async () => {
  try {
    const response = await axios.get('/api/device/panel');
    if (response.status === 200 && response.data && response.data.data) {
      const [totalDevices, onlineDevices, upMessages, warnMessages] = response.data.data;
      deviceStats.value = {
        totalDevices,
        onlineDevices,
        upMessages,
        warnMessages
      };
      nextTick(() => {
        initChart();
      });
    } else {
      console.error('获取设备统计信息失败');
    }
  } catch (error) {
    console.error('获取设备统计信息错误:', error);
  }
};

const searchDevices = () => {
  if (searchParams.value.deviceID || searchParams.value.title) {
    searchWithFilter();
  } else {
    currentPage.value = 1;
    fetchDevices(1, pageSize.value);
  }
};

const searchWithFilter = async () => {
  try {
    const response = await axios.post('/api/device/list', {
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

const handlePageChange = (page) => {
  currentPage.value = page;
  fetchDevices(page, pageSize.value);
};

const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;
  fetchDevices(1, size);
};

function formatDateTime(dateTimeString) {
  if (!dateTimeString) return '';
  const date = new Date(dateTimeString);
  return date.toLocaleString();
}

const addDevice = async () => {
  try {
    await deviceForm.value.validate();

    if (newDevice.value.password !== confirmPassword.value) {
      throw new Error('两次输入的密码不一致');
    }

    const response = await axios.post('/api/device/register', {
      title: newDevice.value.title,
      deviceName: newDevice.value.deviceName,
      password: newDevice.value.password
    });

    if (response.status === 200) {
      ElMessage.success('设备添加成功');
      showDialog.value = false;
      fetchDevices(currentPage.value, pageSize.value);
      fetchDeviceStats();
      resetForm();
    } else {
      throw new Error('添加设备失败');
    }
  } catch (error) {
    console.error('添加设备错误:', error);
    ElMessage.error(error.message || '添加设备失败');
  }
};

const resetForm = () => {
  newDevice.value = {
    title: '',
    deviceName: '',
    password: ''
  };
  confirmPassword.value = '';
};

onMounted(() => {
  fetchDevices(1, pageSize.value);
  fetchDeviceStats();
  window.addEventListener('resize', handleResize);
});

const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize();
  }
};

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  if (chartInstance) {
    chartInstance.dispose();
  }
});
</script>

<style lang="less" scoped>
@import url("../assets/css/management.css");
</style>
