<template>
  <div class="reports-container">
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
          <h1>统计报表</h1>
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
                <el-dropdown-item command="home">首页</el-dropdown-item>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="settings">设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <!-- 时间筛选器 -->
        <el-card class="filter-card" shadow="hover">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="6">
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                @change="handleDateChange"
                style="width: 100%"
              />
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-select v-model="reportType" placeholder="报表类型" style="width: 100%">
                <el-option label="消费统计" value="consume" />
                <el-option label="充值统计" value="recharge" />
                <el-option label="综合统计" value="comprehensive" />
              </el-select>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-button type="primary" @click="generateReport" :loading="loading">
                <el-icon><search /></el-icon>
                生成报表
              </el-button>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-button type="success" @click="exportReport" :disabled="!reportData">
                <el-icon><download /></el-icon>
                导出报表
              </el-button>
            </el-col>
          </el-row>
        </el-card>

        <!-- 统计概览卡片 -->
        <el-row :gutter="20" class="stats-row">
          <el-col :xs="24" :sm="12" :md="6">
            <el-card class="stats-card" shadow="hover">
              <div class="stats-content">
                <div class="stats-icon consume">
                  <el-icon><shopping-cart /></el-icon>
                </div>
                <div class="stats-info">
                  <div class="stats-value">¥{{ stats.totalConsume || 0 }}</div>
                  <div class="stats-label">总消费金额</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-card class="stats-card" shadow="hover">
              <div class="stats-content">
                <div class="stats-icon recharge">
                  <el-icon><credit-card /></el-icon>
                </div>
                <div class="stats-info">
                  <div class="stats-value">¥{{ stats.totalRecharge || 0 }}</div>
                  <div class="stats-label">总充值金额</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-card class="stats-card" shadow="hover">
              <div class="stats-content">
                <div class="stats-icon count">
                  <el-icon><list /></el-icon>
                </div>
                <div class="stats-info">
                  <div class="stats-value">{{ stats.totalTransactions || 0 }}</div>
                  <div class="stats-label">交易次数</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-card class="stats-card" shadow="hover">
              <div class="stats-content">
                <div class="stats-icon average">
                  <el-icon><data-analysis /></el-icon>
                </div>
                <div class="stats-info">
                  <div class="stats-value">¥{{ stats.avgConsume || 0 }}</div>
                  <div class="stats-label">平均消费</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 图表区域 -->
        <el-row :gutter="20" class="charts-row">
          <el-col :xs="24" :lg="12">
            <el-card class="chart-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>消费趋势图</span>
                  <el-radio-group v-model="consumeChartPeriod" size="small">
                    <el-radio-button label="week">本周</el-radio-button>
                    <el-radio-button label="month">本月</el-radio-button>
                  </el-radio-group>
                </div>
              </template>
              <div id="consume-trend-chart" class="chart-container"></div>
            </el-card>
          </el-col>
          <el-col :xs="24" :lg="12">
            <el-card class="chart-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>消费分布图</span>
                  <el-radio-group v-model="consumeChartType" size="small">
                    <el-radio-button label="pie">饼图</el-radio-button>
                    <el-radio-button label="bar">柱状图</el-radio-button>
                  </el-radio-group>
                </div>
              </template>
              <div id="consume-distribution-chart" class="chart-container"></div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 数据表格 -->
        <el-card class="table-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>交易明细</span>
              <div>
                <el-button size="small" @click="refreshTable">
                  <el-icon><refresh /></el-icon>
                  刷新
                </el-button>
              </div>
            </div>
          </template>
          
          <el-table 
            :data="tableData" 
            stripe 
            style="width: 100%"
            v-loading="tableLoading"
            @sort-change="handleSortChange"
          >
            <el-table-column prop="date" label="日期" width="120" sortable />
            <el-table-column prop="type" label="类型" width="80">
              <template #default="scope">
                <el-tag :type="scope.row.type === '消费' ? 'danger' : 'success'">
                  {{ scope.row.type }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="金额" width="100" sortable>
              <template #default="scope">
                <span :class="scope.row.type === '消费' ? 'consume-amount' : 'recharge-amount'">
                  {{ scope.row.type === '消费' ? '-' : '+' }}¥{{ scope.row.amount }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="location" label="地点" width="150" />
            <el-table-column prop="merchant" label="商户" width="150" />
            <el-table-column prop="balance" label="余额" width="100" sortable>
              <template #default="scope">
                <span class="balance-amount">¥{{ scope.row.balance }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" />
          </el-table>
          
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :small="false"
              :disabled="false"
              :background="true"
              layout="total, sizes, prev, pager, next, jumper"
              :total="total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import request from '../utils/request'

const router = useRouter()
const user = JSON.parse(localStorage.getItem('user')) || {}

// 筛选条件
const dateRange = ref([])
const reportType = ref('consume')
const loading = ref(false)

// 统计数据
const stats = reactive({
  totalConsume: 0,
  totalRecharge: 0,
  totalTransactions: 0,
  avgConsume: 0
})

// 图表配置
const consumeChartPeriod = ref('week')
const consumeChartType = ref('pie')

// 表格数据
const tableData = ref([])
const tableLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 报表数据
const reportData = ref(null)

// 图表实例
let consumeTrendChart = null
let consumeDistributionChart = null

// 返回上一页
const goBack = () => {
  router.push('/home')
}

// 处理用户下拉菜单命令
const handleCommand = (command) => {
  switch (command) {
    case 'home':
      router.push('/home')
      break
    case 'profile':
      ElMessage.info('个人中心功能开发中')
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
  localStorage.removeItem('user')
  localStorage.removeItem('token')
  router.push('/login')
}

// 处理日期变化
const handleDateChange = () => {
  // 日期变化时自动生成报表
  generateReport()
}

// 生成报表
const generateReport = () => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('请选择日期范围')
    return
  }
  
  loading.value = true
  
  // 模拟API调用
  setTimeout(() => {
    // 生成模拟数据
    const mockStats = {
      totalConsume: 1286.50,
      totalRecharge: 1500.00,
      totalTransactions: 42,
      avgConsume: 30.63
    }
    
    Object.assign(stats, mockStats)
    
    // 生成表格数据
    generateTableData()
    
    // 生成图表
    nextTick(() => {
      initCharts()
    })
    
    reportData.value = mockStats
    loading.value = false
    ElMessage.success('报表生成成功')
  }, 1000)
}

// 生成表格数据
const generateTableData = () => {
  tableLoading.value = true
  
  // 模拟API调用
  setTimeout(() => {
    const mockData = []
    const startDate = new Date(dateRange.value[0])
    const endDate = new Date(dateRange.value[1])
    const daysDiff = Math.ceil((endDate - startDate) / (1000 * 60 * 60 * 24))
    
    for (let i = 0; i < Math.min(daysDiff * 2, 50); i++) {
      const date = new Date(startDate)
      date.setDate(date.getDate() + Math.floor(i / 2))
      
      const isConsume = i % 2 === 0
      const amount = isConsume 
        ? (Math.random() * 50 + 10).toFixed(2)
        : (Math.random() * 200 + 50).toFixed(2)
      
      mockData.push({
        date: date.toISOString().split('T')[0],
        type: isConsume ? '消费' : '充值',
        amount: amount,
        location: isConsume 
          ? ['第一食堂', '第二食堂', '超市', '水果店'][Math.floor(Math.random() * 4)]
          : ['充值机', '网上充值', '人工充值'][Math.floor(Math.random() * 3)],
        merchant: isConsume
          ? ['食堂一楼', '食堂二楼', '校园超市', '水果摊'][Math.floor(Math.random() * 4)]
          : ['财务处', '校园卡中心', '自助终端'][Math.floor(Math.random() * 3)],
        balance: (Math.random() * 500 + 100).toFixed(2),
        remark: isConsume ? '日常消费' : '账户充值'
      })
    }
    
    tableData.value = mockData
    total.value = mockData.length
    tableLoading.value = false
  }, 500)
}

// 初始化图表
const initCharts = () => {
  // 初始化消费趋势图
  if (!consumeTrendChart) {
    consumeTrendChart = echarts.init(document.getElementById('consume-trend-chart'))
  }
  
  // 初始化消费分布图
  if (!consumeDistributionChart) {
    consumeDistributionChart = echarts.init(document.getElementById('consume-distribution-chart'))
  }
  
  // 更新图表数据
  updateCharts()
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    consumeTrendChart?.resize()
    consumeDistributionChart?.resize()
  })
}

// 更新图表数据
const updateCharts = () => {
  // 更新消费趋势图
  const trendOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      }
    },
    legend: {
      data: ['消费金额', '充值金额']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: [
      {
        type: 'category',
        boundaryGap: false,
        data: generateDateLabels()
      }
    ],
    yAxis: [
      {
        type: 'value'
      }
    ],
    series: [
      {
        name: '消费金额',
        type: 'line',
        stack: 'Total',
        areaStyle: {},
        emphasis: {
          focus: 'series'
        },
        data: generateConsumeData()
      },
      {
        name: '充值金额',
        type: 'line',
        stack: 'Total',
        areaStyle: {},
        emphasis: {
          focus: 'series'
        },
        data: generateRechargeData()
      }
    ]
  }
  
  consumeTrendChart.setOption(trendOption)
  
  // 更新消费分布图
  const distributionOption = consumeChartType.value === 'pie'
    ? {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 10
        },
        series: [
          {
            name: '消费分布',
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
                fontSize: '40',
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: [
              { value: 1048, name: '第一食堂' },
              { value: 735, name: '第二食堂' },
              { value: 580, name: '超市' },
              { value: 484, name: '水果店' },
              { value: 300, name: '其他' }
            ]
          }
        ]
      }
    : {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'value'
        },
        yAxis: {
          type: 'category',
          data: ['第一食堂', '第二食堂', '超市', '水果店', '其他']
        },
        series: [
          {
            name: '消费金额',
            type: 'bar',
            stack: 'total',
            label: {
              show: true
            },
            emphasis: {
              focus: 'series'
            },
            data: [320, 302, 301, 334, 390]
          }
        ]
      }
  
  consumeDistributionChart.setOption(distributionOption)
}

// 生成日期标签
const generateDateLabels = () => {
  const labels = []
  const startDate = new Date(dateRange.value[0])
  const days = consumeChartPeriod.value === 'week' ? 7 : 30
  
  for (let i = 0; i < days; i++) {
    const date = new Date(startDate)
    date.setDate(date.getDate() + i)
    labels.push(date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' }))
  }
  
  return labels
}

// 生成消费数据
const generateConsumeData = () => {
  const data = []
  const days = consumeChartPeriod.value === 'week' ? 7 : 30
  
  for (let i = 0; i < days; i++) {
    data.push(Math.floor(Math.random() * 100) + 20)
  }
  
  return data
}

// 生成充值数据
const generateRechargeData = () => {
  const data = []
  const days = consumeChartPeriod.value === 'week' ? 7 : 30
  
  for (let i = 0; i < days; i++) {
    data.push(Math.floor(Math.random() * 200) + 50)
  }
  
  return data
}

// 导出报表
const exportReport = () => {
  ElMessage.success('报表导出功能开发中')
}

// 刷新表格
const refreshTable = () => {
  generateTableData()
}

// 处理排序变化
const handleSortChange = ({ prop, order }) => {
  // 实现排序逻辑
  console.log('排序变化:', prop, order)
}

// 处理分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  generateTableData()
}

// 处理当前页变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  generateTableData()
}

// 监听图表类型变化
watch(consumeChartType, () => {
  updateCharts()
})

// 监听图表周期变化
watch(consumeChartPeriod, () => {
  updateCharts()
})

// 页面加载时初始化
onMounted(() => {
  // 设置默认日期范围为最近30天
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(startDate.getDate() - 30)
  
  dateRange.value = [
    startDate.toISOString().split('T')[0],
    endDate.toISOString().split('T')[0]
  ]
  
  // 自动生成报表
  generateReport()
})
</script>

<style scoped>
.reports-container {
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
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

.filter-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.stats-row {
  margin-bottom: 20px;
}

.stats-card {
  border-radius: 12px;
  height: 100%;
  transition: transform 0.3s;
}

.stats-card:hover {
  transform: translateY(-5px);
}

.stats-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stats-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
  color: white;
}

.stats-icon.consume {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stats-icon.recharge {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stats-icon.count {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stats-icon.average {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stats-info {
  flex: 1;
}

.stats-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 5px;
}

.stats-label {
  font-size: 14px;
  color: #909399;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 12px;
  height: 400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #303133;
}

.chart-container {
  width: 100%;
  height: 320px;
}

.table-card {
  border-radius: 12px;
}

.consume-amount {
  color: #f56c6c;
  font-weight: 600;
}

.recharge-amount {
  color: #67c23a;
  font-weight: 600;
}

.balance-amount {
  color: #409eff;
  font-weight: 600;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    padding: 15px;
  }
  
  .header-left h1 {
    font-size: 18px;
  }
  
  .username {
    display: none;
  }
  
  .chart-card {
    height: 300px;
  }
  
  .chart-container {
    height: 220px;
  }
}

@media (max-width: 480px) {
  .header {
    padding: 0 10px;
  }
  
  .main-content {
    padding: 10px;
  }
  
  .stats-content {
    flex-direction: column;
    text-align: center;
  }
  
  .stats-icon {
    margin-right: 0;
    margin-bottom: 10px;
  }
}

/* Element Plus 样式覆盖 */
:deep(.el-card__header) {
  background-color: #f8f9fa;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-table th) {
  background-color: #f8f9fa;
}

:deep(.el-pagination) {
  justify-content: flex-end;
}
</style>