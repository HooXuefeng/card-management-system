<template>
  <div class="card-detail-container">
    <!-- 顶部导航栏 -->
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-button 
            type="text" 
            @click="goBack"
            class="back-btn"
          >
            <el-icon><arrow-left /></el-icon>
            返回
          </el-button>
          <h1>卡片详情</h1>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" icon="User" />
              <span class="username">{{ user.userName || '用户' }}</span>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="reports">统计报表</el-dropdown-item>
                <el-dropdown-item command="settings">设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <!-- 卡片信息 -->
        <el-card v-if="cardInfo" class="card-info-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>卡片信息</span>
              <el-tag 
                :type="getStatusType(cardInfo.status)"
                effect="dark"
                size="large"
              >
                {{ cardInfo.status }}
              </el-tag>
            </div>
          </template>
          
          <el-descriptions :column="2" border>
            <el-descriptions-item label="卡号" :span="2">
              <el-text class="card-number">{{ cardInfo.cardNumber }}</el-text>
            </el-descriptions-item>
            <el-descriptions-item label="余额">
              <el-text class="balance" type="primary">¥{{ cardInfo.balance }}</el-text>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(cardInfo.status)" effect="light">
                {{ cardInfo.status }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="注册日期" :span="2">
              {{ cardInfo.registrationDate }}
            </el-descriptions-item>
          </el-descriptions>
          
          <div class="status-actions">
            <el-button 
              v-if="cardInfo.status === '正常' || cardInfo.status === 'active'" 
              type="danger" 
              @click="freezeCard"
              :icon="Lock"
            >
              冻结卡片
            </el-button>
            <el-button 
              v-if="cardInfo.status === '冻结'" 
              type="success" 
              @click="unfreezeCard"
              :icon="Unlock"
            >
              解冻卡片
            </el-button>
          </div>
        </el-card>

        <!-- 无卡片信息时的提示 -->
        <el-empty v-else description="暂无卡片信息" class="empty-card">
          <el-button type="primary" @click="goBack">返回首页</el-button>
        </el-empty>

        <!-- 操作区域 -->
        <div class="operations-container">
          <!-- 充值模块 -->
          <el-card class="operation-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon class="card-icon"><credit-card /></el-icon>
                <span>充值操作</span>
              </div>
            </template>
            
            <el-form 
              ref="rechargeFormRef" 
              :model="rechargeForm" 
              :rules="rechargeRules" 
              label-width="100px"
              @submit.prevent="recharge"
            >
              <el-form-item label="充值金额" prop="amount">
                <el-input-number 
                  v-model="rechargeForm.amount" 
                  :precision="2" 
                  :step="0.01" 
                  :min="0.01"
                  placeholder="0.00"
                  style="width: 100%"
                />
              </el-form-item>
              
              <el-form-item label="充值地点" prop="place">
                <el-input 
                  v-model="rechargeForm.place" 
                  placeholder="例如：第一食堂"
                  clearable
                />
              </el-form-item>
              
              <el-form-item label="支付方式" prop="method">
                <el-select v-model="rechargeForm.method" placeholder="选择支付方式" style="width: 100%">
                  <el-option label="微信支付" value="wechat">
                    <span style="float: left">微信支付</span>
                    <span style="float: right; color: #8492a6; font-size: 13px">
                      <el-icon><wechat-filled /></el-icon>
                    </span>
                  </el-option>
                  <el-option label="支付宝" value="alipay">
                    <span style="float: left">支付宝</span>
                    <span style="float: right; color: #8492a6; font-size: 13px">
                      <el-icon><alipay-circle-filled /></el-icon>
                    </span>
                  </el-option>
                  <el-option label="现金" value="cash">
                    <span style="float: left">现金</span>
                    <span style="float: right; color: #8492a6; font-size: 13px">
                      <el-icon><money /></el-icon>
                    </span>
                  </el-option>
                </el-select>
              </el-form-item>
              
              <el-form-item>
                <el-button 
                  type="primary" 
                  @click="recharge" 
                  :loading="rechargeLoading"
                  style="width: 100%"
                >
                  {{ rechargeLoading ? '充值中...' : '确认充值' }}
                </el-button>
              </el-form-item>
            </el-form>
            
            <el-alert 
              v-if="rechargeMessage" 
              :title="rechargeMessage" 
              :type="rechargeSuccess ? 'success' : 'error'"
              :closable="false"
              show-icon
              class="operation-message"
            />
          </el-card>

          <!-- 消费模块 -->
          <el-card class="operation-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon class="card-icon"><shopping-cart /></el-icon>
                <span>消费操作</span>
              </div>
            </template>
            
            <el-form 
              ref="consumeFormRef" 
              :model="consumeForm" 
              :rules="consumeRules" 
              label-width="100px"
              @submit.prevent="consume"
            >
              <el-form-item label="消费金额" prop="amount">
                <el-input-number 
                  v-model="consumeForm.amount" 
                  :precision="2" 
                  :step="0.01" 
                  :min="0.01"
                  :max="cardInfo?.balance || 0"
                  placeholder="0.00"
                  style="width: 100%"
                />
              </el-form-item>
              
              <el-form-item label="消费地点" prop="place">
                <el-input 
                  v-model="consumeForm.place" 
                  placeholder="例如：第二食堂"
                  clearable
                />
              </el-form-item>
              
              <el-form-item label="商户名称" prop="merchant">
                <el-input 
                  v-model="consumeForm.merchant" 
                  placeholder="例如：食堂一楼"
                  clearable
                />
              </el-form-item>
              
              <el-form-item>
                <el-button 
                  type="primary" 
                  @click="consume" 
                  :loading="consumeLoading"
                  style="width: 100%"
                  :disabled="cardInfo?.status !== '正常' && cardInfo?.status !== 'active'"
                >
                  {{ consumeLoading ? '处理中...' : '确认消费' }}
                </el-button>
              </el-form-item>
            </el-form>
            
            <el-alert 
              v-if="consumeMessage" 
              :title="consumeMessage" 
              :type="consumeSuccess ? 'success' : 'error'"
              :closable="false"
              show-icon
              class="operation-message"
            />
          </el-card>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const router = useRouter()
const route = useRoute()
const cardId = route.params.cardId
const user = JSON.parse(localStorage.getItem('user')) || {}

// 表单引用
const rechargeFormRef = ref()
const consumeFormRef = ref()

// 卡片信息
const cardInfo = ref(null)

// 充值表单
const rechargeForm = reactive({
  amount: null,
  place: '',
  method: 'wechat'
})

// 消费表单
const consumeForm = reactive({
  amount: null,
  place: '',
  merchant: ''
})

// 加载状态
const rechargeLoading = ref(false)
const consumeLoading = ref(false)

// 消息状态
const rechargeMessage = ref('')
const rechargeSuccess = ref(false)
const consumeMessage = ref('')
const consumeSuccess = ref(false)

// 表单验证规则
const rechargeRules = {
  amount: [
    { required: true, message: '请输入充值金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '充值金额必须大于0', trigger: 'blur' }
  ],
  place: [
    { required: true, message: '请输入充值地点', trigger: 'blur' },
    { min: 2, max: 50, message: '充值地点长度在2到50个字符', trigger: 'blur' }
  ],
  method: [
    { required: true, message: '请选择支付方式', trigger: 'change' }
  ]
}

const consumeRules = {
  amount: [
    { required: true, message: '请输入消费金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '消费金额必须大于0', trigger: 'blur' }
  ],
  place: [
    { required: true, message: '请输入消费地点', trigger: 'blur' },
    { min: 2, max: 50, message: '消费地点长度在2到50个字符', trigger: 'blur' }
  ],
  merchant: [
    { required: true, message: '请输入商户名称', trigger: 'blur' },
    { min: 2, max: 50, message: '商户名称长度在2到50个字符', trigger: 'blur' }
  ]
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    '正常': 'success',
    '冻结': 'danger',
    '禁用': 'warning'
  }
  return typeMap[status] || 'info'
}

// 获取卡片信息
const getCardInfo = () => {
  if (!cardId) {
    ElMessage.error('卡片ID不存在，请重新选择')
    router.push('/')
    return
  }
  
  request.get(`/card/info/${cardId}`).then(res => {
    if (res.code === 200) {
      cardInfo.value = res.data
      // 确保状态显示为中文
      if (cardInfo.value.status === 'active') {
        cardInfo.value.status = '正常'
      } else if (cardInfo.value.status === 'frozen') {
        cardInfo.value.status = '冻结'
      } else if (cardInfo.value.status === 'disabled') {
        cardInfo.value.status = '禁用'
      }
      console.log('卡片详情页 - 卡片信息:', cardInfo.value) // 添加调试日志
    } else {
      ElMessage.error(res.msg || '获取卡片信息失败')
    }
  }).catch(err => {
    console.error('获取卡片信息出错：', err)
    ElMessage.error('网络异常，获取卡片信息失败')
  })
}

// 充值
const recharge = () => {
  rechargeFormRef.value.validate((valid) => {
    if (!valid) return
    
    // 校验卡片状态 - 同时支持中文和英文状态
    const cardStatus = cardInfo.value.status
    console.log('卡片详情页 - 当前卡片状态:', cardStatus) // 添加调试日志
    
    if (cardStatus !== '正常' && cardStatus !== 'active') {
      ElMessage.warning(`卡片状态为"${cardStatus}"，非正常状态，无法充值`)
      return
    }
    
    rechargeLoading.value = true
    rechargeMessage.value = ''
    
    // 确保cardId是数字类型
    const numericCardId = parseInt(cardId)
    if (isNaN(numericCardId)) {
      ElMessage.error('卡片ID无效')
      return
    }
    
    request.post('/card/recharge', null, {
      params: { 
        cardId: numericCardId,
        amount: rechargeForm.amount,
        operator: user.userName || '用户',
        place: rechargeForm.place,
        paymentMethod: rechargeForm.method
      }
    }).then(res => {
      if (res.code === 200) {
        rechargeMessage.value = '充值成功'
        rechargeSuccess.value = true
        // 重置表单
        rechargeForm.amount = null
        rechargeForm.place = ''
        rechargeForm.method = 'wechat'
        // 刷新卡片信息
        getCardInfo()
      } else {
        rechargeMessage.value = res.msg || '充值失败'
        rechargeSuccess.value = false
      }
    }).catch(err => {
      console.error('充值出错：', err)
      rechargeMessage.value = '网络异常，充值失败'
      rechargeSuccess.value = false
    }).finally(() => {
      rechargeLoading.value = false
    })
  })
}

// 消费
const consume = () => {
  consumeFormRef.value.validate((valid) => {
    if (!valid) return
    
    // 校验卡片状态 - 同时支持中文和英文状态
    if (cardInfo.value.status !== '正常' && cardInfo.value.status !== 'active') {
      ElMessage.warning('卡片非正常状态，无法消费')
      return
    }
    
    // 校验余额是否充足
    if (consumeForm.amount > cardInfo.value.balance) {
      ElMessage.error('余额不足，无法消费')
      return
    }
    
    consumeLoading.value = true
    consumeMessage.value = ''
    
    // 确保cardId是数字类型
    const numericCardId = parseInt(cardId)
    if (isNaN(numericCardId)) {
      ElMessage.error('卡片ID无效')
      return
    }
    
    request.post('/card/consume', null, {
      params: { 
        cardId: numericCardId,
        amount: consumeForm.amount,
        operator: user.userName || '用户',
        place: consumeForm.place,
        merchant: consumeForm.merchant
      }
    }).then(res => {
      if (res.code === 200) {
        consumeMessage.value = '消费成功'
        consumeSuccess.value = true
        // 重置表单
        consumeForm.amount = null
        consumeForm.place = ''
        consumeForm.merchant = ''
        // 刷新卡片信息
        getCardInfo()
      } else {
        consumeMessage.value = res.msg || '消费失败'
        consumeSuccess.value = false
      }
    }).catch(err => {
      console.error('消费出错：', err)
      consumeMessage.value = '网络异常，消费失败'
      consumeSuccess.value = false
    }).finally(() => {
      consumeLoading.value = false
    })
  })
}

// 冻结卡片
const freezeCard = () => {
  ElMessageBox.confirm(
    '确定要冻结卡片吗？冻结后将无法进行消费操作！',
    '操作确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    request.post('/card/status/freeze', null).then(res => {
      if (res.code === 200) {
        ElMessage.success('冻结成功')
        getCardInfo()
      } else {
        ElMessage.error(res.msg || '冻结失败')
      }
    }).catch(err => {
      console.error('冻结卡片出错：', err)
      ElMessage.error('网络异常，冻结失败')
    })
  }).catch(() => {
    // 用户取消操作
  })
}

// 解冻卡片
const unfreezeCard = () => {
  ElMessageBox.confirm(
    '确定要解冻卡片吗？',
    '操作确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    }
  ).then(() => {
    request.post('/card/status/unfreeze', null).then(res => {
      if (res.code === 200) {
        ElMessage.success('解冻成功')
        getCardInfo()
      } else {
        ElMessage.error(res.msg || '解冻失败')
      }
    }).catch(err => {
      console.error('解冻卡片出错：', err)
      ElMessage.error('网络异常，解冻失败')
    })
  }).catch(() => {
    // 用户取消操作
  })
}

// 返回上一页
const goBack = () => {
  router.push('/')
}

// 处理用户下拉菜单命令
const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人中心功能开发中')
      break
    case 'reports':
      router.push('/reports')
      break
    case 'settings':
      ElMessage.info('设置功能开发中')
      break
    case 'logout':
      logout()
      break
  }
}

// 退出登录
const logout = () => {
  ElMessageBox.confirm(
    '确定要退出登录吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    }
  ).then(() => {
    localStorage.removeItem('user')
    router.push('/login')
  }).catch(() => {
    // 用户取消操作
  })
}

// 页面加载时获取卡片信息
onMounted(() => {
  if (!user || !user.userId || !user.userName) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  getCardInfo()
})
</script>

<style scoped>
.card-detail-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.header-left h1 {
  margin: 0;
  font-size: 20px;
  color: #303133;
  font-weight: 600;
}

.back-btn {
  color: #409EFF;
  font-size: 16px;
  padding: 8px;
}

.back-btn:hover {
  background-color: rgba(64, 158, 255, 0.1);
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

.username {
  margin: 0 8px;
  font-size: 14px;
  color: #606266;
}

.main-content {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.card-info-card {
  margin-bottom: 20px;
  border-radius: 12px;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #303133;
}

.card-number {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  color: #409EFF;
}

.balance {
  font-size: 18px;
  font-weight: 700;
}

.status-actions {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 15px;
}

.operations-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
}

.operation-card {
  border-radius: 12px;
  overflow: hidden;
  height: fit-content;
}

.card-icon {
  margin-right: 8px;
  color: #409EFF;
}

.operation-message {
  margin-top: 15px;
}

.empty-card {
  margin: 40px auto;
  max-width: 500px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    padding: 15px;
  }
  
  .operations-container {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .header-left h1 {
    font-size: 18px;
  }
  
  .username {
    display: none;
  }
}

@media (max-width: 480px) {
  .header {
    padding: 0 10px;
  }
  
  .main-content {
    padding: 10px;
  }
  
  .status-actions {
    flex-direction: column;
    gap: 10px;
  }
  
  .status-actions .el-button {
    width: 100%;
  }
}

/* Element Plus 样式覆盖 */
:deep(.el-descriptions__label) {
  font-weight: 600;
  color: #606266;
}

:deep(.el-descriptions__content) {
  color: #303133;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #606266;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-input-number .el-input__inner) {
  text-align: left;
}

:deep(.el-card__header) {
  background-color: #f8f9fa;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-select-dropdown__item) {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
