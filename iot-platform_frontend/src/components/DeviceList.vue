<template>
  <div class="deviceList">
    <DeviceItem v-for="device in devices" :key="device.deviceId" :device="device" @deviceDeleted="handleDeviceRemoved" />
  </div>
</template>

<script setup>
import DeviceItem from './DeviceItem.vue';
import { ref } from 'vue';

const props = defineProps({
  devices: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(['deviceRemoved']);

// 处理设备删除事件
const handleDeviceRemoved = (deviceId) => {
  const index = props.devices.findIndex(device => device.deviceId === deviceId);
  if (index !== -1) {
    const updatedDevices = [...props.devices];
    updatedDevices.splice(index, 1);
    emit('deviceRemoved', updatedDevices); // 触发父组件的 deviceRemoved 事件
  }
};
</script>

<style lang="less" scoped>
.deviceList {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 20px;
}
</style>
