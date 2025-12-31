<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <h1>饭卡管理系统</h1>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" icon="User" />
              <span class="username">用户</span>
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

      <el-main>
        <!-- 概览卡片区域 -->
        <el-row :gutter="20" class="overview-cards">
          <el-col :xs="24" :sm="12" :md="6">
            <el-card shadow="hover" class="overview-card">
              <div class="card-content">
                <div class="card-icon balance-icon">
                  <el-icon size="24"><wallet /></el-icon>
                </div>
                <div class="card-info">
                  <div class="card-value">¥{{ cardInfo.balance || '0.00' }}</div>
                  <div class="card-label">当前余额</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-card shadow="hover" class="overview-card">
              <div class="card-content">
                <div class="card-icon status-icon" :class="getStatusClass(cardInfo.status)">
                  <el-icon size="24"><credit-card /></el-icon>
                </div>
                <div class="card-info">
                  <div class="card-value">{{ getStatusText(cardInfo.status) }}</div>
                  <div class="card-label">卡片状态</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-card shadow="hover" class="overview-card">
              <div class="card-content">
                <div class="card-icon recharge-icon">
                  <el-icon size="24"><money /></el-icon>
                </div>
                <div class="card-info">
                  <div class="card-value">{{ monthlyRecharge || '0.00' }}</div>
                  <div class="card-label">本月充值</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-card shadow="hover" class="overview-card">
              <div class="card-content">
                <div class="card-icon consume-icon">
                  <el-icon size="24"><shopping-cart /></el-icon>
                </div>
                <div class="card-info">
                  <div class="card-value">{{ monthlyConsume || '0.00' }}</div>
                  <div class="card-label">本月消费</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 功能区域 -->
        <el-row :gutter="20" class="function-area">
          <!-- 左侧：卡片信息和操作 -->
          <el-col :xs="24" :lg="12">
            <!-- 卡片信息 -->
            <el-card class="card-info-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>我的饭卡</span>
                  <el-button type="primary" size="small" @click="viewCardDetail" :disabled="!cardInfo.cardNumber">
                    查看详情
                  </el-button>
                </div>
              </template>
              <div class="card-detail">
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="卡号">{{ cardInfo.cardNumber || '加载中...' }}</el-descriptions-item>
                  <el-descriptions-item label="余额">
                    <span class="balance-text">¥{{ cardInfo.balance || '0.00' }}</span>
                  </el-descriptions-item>
                  <el-descriptions-item label="状态">
                    <el-tag :type="getStatusTagType(cardInfo.status)">{{ getStatusText(cardInfo.status) }}</el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="注册日期">{{ cardInfo.registrationDate || '加载中...' }}</el-descriptions-item>
                </el-descriptions>
                
                <div class="card-actions">
                  <el-button 
                    type="danger" 
                    v-if="cardInfo.status === '正常' || cardInfo.status === 'active'" 
                    @click="handleStatusChange"
                    :loading="statusLoading"
                  >
                    挂失饭卡
                  </el-button>
                  <el-button 
                    type="success" 
                    v-else-if="cardInfo.status === '冻结'" 
                    @click="handleStatusChange"
                    :loading="statusLoading"
                  >
                    解冻饭卡
                  </el-button>
                </div>
              </div>
            </el-card>

            <!-- 充值模块 -->
            <el-card class="recharge-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>饭卡充值</span>
                </div>
              </template>
              <el-form :model="rechargeForm" label-width="80px">
                <el-form-item label="充值金额">
                  <el-input-number 
                    v-model="rechargeForm.amount" 
                    :precision="2" 
                    :step="0.01" 
                    :min="0.01" 
                    :max="1000"
                    placeholder="请输入充值金额"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button 
                    type="primary" 
                    @click="handleRecharge" 
                    :loading="rechargeLoading"
                    style="width: 100%"
                  >
                    立即充值
                  </el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-col>

          <!-- 右侧：消费图表 -->
          <el-col :xs="24" :lg="12">
            <el-card class="chart-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>消费趋势</span>
                  <el-radio-group v-model="chartPeriod" size="small">
                    <el-radio-button label="week">本周</el-radio-button>
                    <el-radio-button label="month">本月</el-radio-button>
                  </el-radio-group>
                </div>
              </template>
              <div id="consume-chart" class="chart-container"></div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 记录表格区域 -->
        <el-row :gutter="20" class="record-area">
          <el-col :xs="24" :lg="12">
            <el-card class="record-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>充值记录</span>
                  <el-button type="text" @click="viewAllRechargeRecords">查看全部</el-button>
                </div>
              </template>
              <el-table :data="rechargeRecords.slice(0, 5)" style="width: 100%" empty-text="暂无充值记录">
                <el-table-column prop="rechargeTime" label="充值时间" width="150" />
                <el-table-column prop="amount" label="充值金额">
                  <template #default="scope">
                    <span>¥{{ scope.row.amount }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态">
                  <template #default="scope">
                    <el-tag :type="scope.row.status === '成功' ? 'success' : 'danger'">
                      {{ scope.row.status }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
          
          <el-col :xs="24" :lg="12">
            <el-card class="record-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>消费记录</span>
                  <el-button type="text" @click="viewAllConsumeRecords">查看全部</el-button>
                </div>
              </template>
              <el-table :data="consumeRecords.slice(0, 5)" style="width: 100%" empty-text="暂无消费记录">
                <el-table-column prop="consumeTime" label="消费时间" width="150" />
                <el-table-column prop="amount" label="消费金额">
                  <template #default="scope">
                    <span>¥{{ scope.row.amount }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="location" label="消费地点" />
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'

const router = useRouter()
const cardInfo = ref({})
const rechargeRecords = ref([])
const consumeRecords = ref([])
const monthlyRecharge = ref('0.00')
const monthlyConsume = ref('0.00')
const chartPeriod = ref('week')
const rechargeLoading = ref(false)
const statusLoading = ref(false)
const rechargeForm = ref({
  amount: null
})

// 查询饭卡信息
const getCardInfo = async () => {
  try {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token')
    const response = await fetch('/api/user/info', {
      headers: { 'Authorization': 'Bearer ' + token }
    })
    const res = await response.json()
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
      console.log('卡片信息:', cardInfo.value) // 添加调试日志
    }
  } catch (error) {
    ElMessage.error('查询饭卡信息失败，请重新登录！')
    localStorage.removeItem('token')
    router.push('/login')
    console.error('查询失败：', error)
  }
}

// 查询充值记录
const getRechargeRecords = async () => {
  try {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token')
    const response = await fetch('/api/user/recharge/records', {
      headers: { 'Authorization': 'Bearer ' + token }
    })
    const res = await response.json()
    if (res.code === 200) {
      rechargeRecords.value = res.data || []
      // 计算本月充值总额
      const currentMonth = new Date().getMonth()
      const currentYear = new Date().getFullYear()
      monthlyRecharge.value = rechargeRecords.value
        .filter(record => {
          const recordDate = new Date(record.rechargeTime)
          return recordDate.getMonth() === currentMonth && recordDate.getFullYear() === currentYear
        })
        .reduce((sum, record) => sum + parseFloat(record.amount), 0)
        .toFixed(2)
    }
  } catch (error) {
    ElMessage.error('查询充值记录失败！')
    console.error('查询记录失败：', error)
  }
}

// 查询消费记录
const getConsumeRecords = async () => {
  try {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token')
    const response = await fetch('/api/user/consume/records', {
      headers: { 'Authorization': 'Bearer ' + token }
    })
    const res = await response.json()
    if (res.code === 200) {
      consumeRecords.value = res.data || []
      // 计算本月消费总额
      const currentMonth = new Date().getMonth()
      const currentYear = new Date().getFullYear()
      monthlyConsume.value = consumeRecords.value
        .filter(record => {
          const recordDate = new Date(record.consumeTime)
          return recordDate.getMonth() === currentMonth && recordDate.getFullYear() === currentYear
        })
        .reduce((sum, record) => sum + parseFloat(record.amount), 0)
        .toFixed(2)
    }
  } catch (error) {
    ElMessage.error('查询消费记录失败！')
    console.error('查询记录失败：', error)
  }
}

// 初始化消费趋势图表
const initConsumeChart = () => {
  nextTick(() => {
    const chartDom = document.getElementById('consume-chart')
    if (!chartDom) return
    
    const myChart = echarts.init(chartDom)
    
    // 准备图表数据
    const dates = []
    const amounts = []
    
    // 根据选择的周期生成数据
    const now = new Date()
    const days = chartPeriod.value === 'week' ? 7 : 30
    
    for (let i = days - 1; i >= 0; i--) {
      const date = new Date(now)
      date.setDate(now.getDate() - i)
      dates.push(date.toLocaleDateString())
      
      // 模拟数据，实际应该从API获取
      amounts.push(Math.floor(Math.random() * 50) + 10)
    }
    
    const option = {
      tooltip: {
        trigger: 'axis',
        formatter: function (params) {
          return `${params[0].name}<br/>消费金额: ¥${params[0].value}`
        }
      },
      xAxis: {
        type: 'category',
        data: dates,
        axisLabel: {
          formatter: function (value) {
            return chartPeriod.value === 'week' 
              ? value.substring(5) // 显示月-日
              : value.substring(5, 10) // 显示月-日
          }
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: '¥{value}'
        }
      },
      series: [
        {
          name: '消费金额',
          type: 'line',
          smooth: true,
          data: amounts,
          itemStyle: {
            color: '#409EFF'
          },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
                { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
              ]
            }
          }
        }
      ]
    }
    
    myChart.setOption(option)
    
    // 监听窗口大小变化
    window.addEventListener('resize', () => {
      myChart.resize()
    })
  })
}

// 查看卡片详情
const viewCardDetail = () => {
  if (cardInfo.value.cardNumber) {
    router.push(`/card/${cardInfo.value.cardNumber}`)
  }
}

// 充值操作
const handleRecharge = async () => {
  if (!rechargeForm.value.amount || rechargeForm.value.amount <= 0) {
    ElMessage.warning('请输入合法的充值金额（大于0）！')
    return
  }

  // 检查卡片状态 - 支持中文和英文状态值
  const cardStatus = cardInfo.value.status
  console.log('当前卡片状态:', cardStatus) // 添加调试日志
  
  if (cardStatus !== '正常' && cardStatus !== 'active') {
    ElMessage.warning(`卡片状态为"${cardStatus}"，非正常状态，无法充值`)
    return
  }

  rechargeLoading.value = true
  try {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token')
    // 使用正确的API接口和参数格式
    const response = await fetch(`/api/user/recharge?amount=${rechargeForm.value.amount}`, {
      method: 'POST',
      headers: { 'Authorization': 'Bearer ' + token }
    })
    const res = await response.json()
    if (res.code === 200) {
      ElMessage.success('充值成功！')
      rechargeForm.value.amount = null
      getCardInfo() // 刷新余额
      getRechargeRecords() // 刷新记录
    } else {
      ElMessage.error('充值失败：' + (res.msg || '未知错误'))
    }
  } catch (error) {
    ElMessage.error('充值请求失败！')
    console.error('充值失败：', error)
  } finally {
    rechargeLoading.value = false
  }
}

// 挂失/解冻
const handleStatusChange = async () => {
  const action = (cardInfo.value.status === '正常' || cardInfo.value.status === 'active') ? '挂失' : '解冻'
  try {
    await ElMessageBox.confirm(
      `确定要${action}饭卡吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    statusLoading.value = true
    const token = localStorage.getItem('token') || sessionStorage.getItem('token')
    const response = await fetch('/api/user/status/change', {
      method: 'POST',
      headers: { 'Authorization': 'Bearer ' + token }
    })
    const res = await response.json()
    if (res.code === 200) {
      ElMessage.success(`${action}成功！`)
      getCardInfo() // 刷新状态
    } else {
      ElMessage.error(`${action}失败：` + (res.msg || '未知错误'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}请求失败！`)
      console.error('状态修改失败：', error)
    }
  } finally {
    statusLoading.value = false
  }
}

// 处理下拉菜单命令
const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('token')
    ElMessage.success('已退出登录！')
    router.push('/login')
  } else if (command === 'profile') {
    ElMessage.info('个人中心功能开发中...')
  } else if (command === 'reports') {
    router.push('/reports')
  } else if (command === 'settings') {
    ElMessage.info('设置功能开发中...')
  }
}

// 查看全部充值记录
const viewAllRechargeRecords = () => {
  ElMessage.info('查看全部充值记录功能开发中...')
}

// 查看全部消费记录
const viewAllConsumeRecords = () => {
  ElMessage.info('查看全部消费记录功能开发中...')
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    '正常': '正常',
    '冻结': '冻结',
    '禁用': '禁用'
  }
  return statusMap[status] || '未知'
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  const typeMap = {
    '正常': 'success',
    '冻结': 'danger',
    '禁用': 'info'
  }
  return typeMap[status] || 'info'
}

// 获取状态样式类
const getStatusClass = (status) => {
  const classMap = {
    '正常': 'status-active',
    '冻结': 'status-frozen',
    '禁用': 'status-disabled'
  }
  return classMap[status] || ''
}

// 监听图表周期变化
const updateChart = () => {
  initConsumeChart()
}

// 页面加载初始化
onMounted(() => {
  const token = localStorage.getItem('token') || sessionStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return
  }
  
  Promise.all([
    getCardInfo(),
    getRechargeRecords(),
    getConsumeRecords()
  ]).then(() => {
    initConsumeChart()
  })
})
</script>

<style scoped>
.home-container {
  height: 100vh;
  overflow: auto;
  background-color: #f5f7fa;
}

.header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.header-left h1 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.username {
  margin: 0 8px;
  color: #303133;
}

.overview-cards {
  margin-bottom: 20px;
}

.overview-card {
  height: 100px;
  margin-bottom: 20px;
}

.card-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 15px;
  color: white;
}

.balance-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.status-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.status-active.status-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.status-frozen.status-icon,
.status-disabled.status-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.recharge-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.consume-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.card-info {
  flex: 1;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
}

.card-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.function-area {
  margin-bottom: 20px;
}

.card-info-card,
.recharge-card,
.chart-card {
  margin-bottom: 20px;
  height: fit-content;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-detail {
  margin-bottom: 15px;
}

.balance-text {
  font-weight: bold;
  color: #409EFF;
  font-size: 16px;
}

.card-actions {
  margin-top: 15px;
  text-align: center;
}

.chart-container {
  height: 300px;
  width: 100%;
}

.record-area {
  margin-bottom: 20px;
}

.record-card {
  height: 400px;
  display: flex;
  flex-direction: column;
}

.record-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.record-card :deep(.el-table) {
  flex: 1;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .overview-card {
    height: 90px;
  }
  
  .card-icon {
    width: 50px;
    height: 50px;
  }
  
  .card-value {
    font-size: 20px;
  }
  
  .chart-container {
    height: 250px;
  }
}
</style>