<template>
  <el-dialog
      v-model="visible"
      :title="mode === 'add' ? '新增用户' : '编辑用户'"
      width="600px"
      destroy-on-close
  >
    <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        label-position="right"
    >
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item label="密码" prop="password" v-if="mode === 'add'">
        <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
        />
      </el-form-item>
      <el-form-item label="姓名" prop="name">
        <el-input v-model="form.name" placeholder="请输入姓名" />
      </el-form-item>
      <el-form-item label="角色" prop="role">
        <el-select v-model="form.role" placeholder="请选择角色">
          <el-option label="管理员" value="admin" />
          <el-option label="操作员" value="operator" />
          <el-option label="观察员" value="viewer" />
        </el-select>
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="请输入邮箱" />
      </el-form-item>
      <el-form-item label="电话" prop="phone">
        <el-input v-model="form.phone" placeholder="请输入电话" />
      </el-form-item>
      <el-form-item label="状态" prop="status" v-if="mode === 'edit'">
        <el-radio-group v-model="form.status">
          <el-radio-button label="active">激活</el-radio-button>
          <el-radio-button label="inactive">禁用</el-radio-button>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="submitForm">确认</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
// const defineProps = defineProps
// const defineEmits = defineEmits


const props = defineProps({
  modelValue: Boolean,
  currentUser: Object,
  mode: String
})

const emit = defineEmits(['update:modelValue', 'submit'])

const visible = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})

const formRef = ref(null)
const form = ref({
  username: '',
  password: '',
  name: '',
  role: 'operator',
  email: '',
  phone: '',
  status: 'active'
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在3到20个字符', trigger: 'blur' }
  ],
  password: [
    { required: props.mode === 'add', message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在6到20个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

watch(
    () => props.currentUser,
    user => {
      if (user) {
        form.value = { ...user, password: '' }
      } else {
        formRef.value?.resetFields()
      }
    },
    { immediate: true }
)

const submitForm = async () => {
  try {
    await formRef.value.validate()
    emit('submit', { ...form.value })
  } catch (error) {
    ElMessage.warning('请完善表单信息')
  }
}
</script>

<style scoped>
.el-form-item {
  margin-bottom: 20px;
}
</style>
