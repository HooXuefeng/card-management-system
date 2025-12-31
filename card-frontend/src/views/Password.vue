<template>
  <div class="password-container">
    <h2>修改密码</h2>
    <div class="form-item">
      <label>原密码：</label>
      <input v-model="oldPwd" type="password" placeholder="请输入原密码" />
    </div>
    <div class="form-item">
      <label>新密码：</label>
      <input v-model="newPwd" type="password" placeholder="请输入新密码（不少于6位）" />
    </div>
    <div class="form-item">
      <label>确认新密码：</label>
      <input v-model="confirmPwd" type="password" placeholder="请再次输入新密码" />
    </div>
    <button class="submit-btn" @click="handleChangePwd" :disabled="loading">
      {{ loading ? '修改中...' : '确认修改' }}
    </button>
    <button class="back-btn" @click="router.push('/home')">返回首页</button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const oldPwd = ref('')
const newPwd = ref('')
const confirmPwd = ref('')
const loading = ref(false)

const handleChangePwd = async () => {
  // 前端校验
  if (!oldPwd.value) {
    alert('请输入原密码！')
    return
  }
  if (newPwd.value.length < 6) {
    alert('新密码不少于6位！')
    return
  }
  if (newPwd.value !== confirmPwd.value) {
    alert('两次密码输入不一致！')
    return
  }

  loading.value = true
  try {
    const token = localStorage.getItem('token')
    const res = await axios.post(
      '/api/user/changePwd',
      { oldPwd: oldPwd.value, newPwd: newPwd.value },
      { headers: { 'Authorization': 'Bearer ' + token } }
    )
    if (res.data.code === 200) {
      alert('密码修改成功，请重新登录！')
      localStorage.removeItem('token')
      router.push('/login')
    } else {
      alert('修改失败：' + res.data.msg)
    }
  } catch (error) {
    alert('修改请求失败！')
    console.error('密码修改失败：', error)
  } finally {
    loading.value = false
  }
}

// 校验登录状态
onMounted(() => {
  if (!localStorage.getItem('token')) {
    router.push('/login')
  }
})
</script>

<style scoped>
.password-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: #f5f5f5;
}
.password-container h2 {
  margin-bottom: 20px;
  color: #333;
}
.form-item {
  margin-bottom: 15px;
  width: 400px;
}
.form-item label {
  display: block;
  margin-bottom: 5px;
  color: #666;
}
.form-item input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
}
.submit-btn {
  width: 400px;
  padding: 10px;
  background: #1677ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  margin-bottom: 10px;
}
.submit-btn:hover {
  background: #0958d9;
}
.submit-btn:disabled {
  background: #8cb4ff;
  cursor: not-allowed;
}
.back-btn {
  padding: 8px 16px;
  background: #f0f0f0;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
}
.back-btn:hover {
  background: #e0e0e0;
}
</style>