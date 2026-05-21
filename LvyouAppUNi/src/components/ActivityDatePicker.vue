<template>
  <div class="activity-date-picker">
    <!-- 遮罩层 -->
    <div 
      v-if="show" 
      class="date-picker-overlay" 
      @click="handleOverlayClick"
    ></div>
    
    <!-- 日历弹窗 -->
    <div v-if="show" class="date-picker-popup">
      <!-- 头部 -->
      <div class="date-picker-header">
        <button class="cancel-btn" @click="handleCancel">取消</button>
        <h3 class="title">{{ title }}</h3>
        <button class="confirm-btn" @click="handleConfirm">确定</button>
      </div>
      
      <!-- 日期选择区域 -->
      <div class="date-picker-content">
        <!-- 年月选择 -->
        <div class="date-selector">
          <div class="year-month-selector">
            <button class="arrow-btn" @click="prevMonth" :disabled="isMinDate">‹</button>
            <span class="current-month">{{ currentYear }}年{{ currentMonth + 1 }}月</span>
            <button class="arrow-btn" @click="nextMonth" :disabled="isMaxDate">›</button>
          </div>
        </div>
        
        <!-- 星期标题 -->
        <div class="weekdays">
          <div class="weekday" v-for="day in weekdays" :key="day">{{ day }}</div>
        </div>
        
        <!-- 日期网格 -->
        <div class="date-grid">
          <div 
            v-for="day in calendarDays" 
            :key="`${day.year}-${day.month}-${day.date}`"
            class="date-cell"
            :class="{
              'other-month': !day.isCurrentMonth,
              'today': day.isToday,
              'selected': day.isSelected,
              'disabled': day.isDisabled
            }"
            @click="selectDate(day)"
          >
            {{ day.date }}
          </div>
        </div>
        
        <!-- 时间选择 -->
        <div v-if="type === 'datetime'" class="time-selector">
          <div class="time-label">时间选择</div>
          <div class="time-inputs">
            <div class="time-input-group">
              <label>时</label>
              <select v-model="selectedHour" class="time-select">
                <option v-for="hour in hours" :key="hour" :value="hour">
                  {{ String(hour).padStart(2, '0') }}
                </option>
              </select>
            </div>
            <div class="time-separator">:</div>
            <div class="time-input-group">
              <label>分</label>
              <select v-model="selectedMinute" class="time-select">
                <option v-for="minute in minutes" :key="minute" :value="minute">
                  {{ String(minute).padStart(2, '0') }}
                </option>
              </select>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  value: {
    type: [String, Date],
    default: ''
  },
  type: {
    type: String,
    default: 'datetime' // date, datetime
  },
  title: {
    type: String,
    default: '选择时间'
  },
  minDate: {
    type: Date,
    default: () => new Date()
  },
  maxDate: {
    type: Date,
    default: () => new Date(2030, 11, 31)
  }
})

const emit = defineEmits(['update:show', 'confirm', 'cancel'])

// 星期标题
const weekdays = ['日', '一', '二', '三', '四', '五', '六']

// 当前显示的年月
const currentYear = ref(new Date().getFullYear())
const currentMonth = ref(new Date().getMonth())

// 选中的日期
const selectedDate = ref(null)
const selectedHour = ref(new Date().getHours())
const selectedMinute = ref(new Date().getMinutes())

// 时间选项
const hours = Array.from({ length: 24 }, (_, i) => i)
const minutes = Array.from({ length: 60 }, (_, i) => i)

// 计算属性
const isMinDate = computed(() => {
  const current = new Date(currentYear.value, currentMonth.value, 1)
  return current <= new Date(props.minDate.getFullYear(), props.minDate.getMonth(), 1)
})

const isMaxDate = computed(() => {
  const current = new Date(currentYear.value, currentMonth.value, 1)
  return current >= new Date(props.maxDate.getFullYear(), props.maxDate.getMonth(), 1)
})

// 计算日历天数
const calendarDays = computed(() => {
  const year = currentYear.value
  const month = currentMonth.value
  
  // 当月第一天
  const firstDay = new Date(year, month, 1)
  const firstDayOfWeek = firstDay.getDay()
  
  // 当月最后一天
  const lastDay = new Date(year, month + 1, 0)
  const lastDate = lastDay.getDate()
  
  // 上个月最后几天
  const prevMonth = new Date(year, month, 0)
  const prevMonthLastDate = prevMonth.getDate()
  
  const days = []
  
  // 添加上个月的日期
  for (let i = firstDayOfWeek - 1; i >= 0; i--) {
    const date = prevMonthLastDate - i
    days.push({
      year: prevMonth.getFullYear(),
      month: prevMonth.getMonth(),
      date,
      isCurrentMonth: false,
      isToday: false,
      isSelected: false,
      isDisabled: false
    })
  }
  
  // 添加当月的日期
  const today = new Date()
  for (let date = 1; date <= lastDate; date++) {
    const isToday = year === today.getFullYear() && 
                   month === today.getMonth() && 
                   date === today.getDate()
    
    const isSelected = selectedDate.value && 
                      selectedDate.value.year === year &&
                      selectedDate.value.month === month &&
                      selectedDate.value.date === date
    
    const isDisabled = isDateDisabled(year, month, date)
    
    days.push({
      year,
      month,
      date,
      isCurrentMonth: true,
      isToday,
      isSelected,
      isDisabled
    })
  }
  
  // 添加下个月的日期（填满6行）
  const remainingDays = 42 - days.length
  const nextMonth = new Date(year, month + 1, 1)
  for (let date = 1; date <= remainingDays; date++) {
    days.push({
      year: nextMonth.getFullYear(),
      month: nextMonth.getMonth(),
      date,
      isCurrentMonth: false,
      isToday: false,
      isSelected: false,
      isDisabled: false
    })
  }
  
  return days
})

// 检查日期是否被禁用
const isDateDisabled = (year, month, date) => {
  const currentDate = new Date(year, month, date)
  return currentDate < props.minDate || currentDate > props.maxDate
}

// 选择日期
const selectDate = (day) => {
  if (day.isDisabled || !day.isCurrentMonth) return
  
  selectedDate.value = {
    year: day.year,
    month: day.month,
    date: day.date
  }
}

// 上一个月
const prevMonth = () => {
  if (currentMonth.value === 0) {
    currentYear.value--
    currentMonth.value = 11
  } else {
    currentMonth.value--
  }
}

// 下一个月
const nextMonth = () => {
  if (currentMonth.value === 11) {
    currentYear.value++
    currentMonth.value = 0
  } else {
    currentMonth.value++
  }
}

// 确认选择
const handleConfirm = () => {
  if (!selectedDate.value) {
    showToast('请选择日期')
    return
  }
  
  const year = selectedDate.value.year
  const month = selectedDate.value.month
  const date = selectedDate.value.date
  
  let resultDate
  if (props.type === 'datetime') {
    resultDate = new Date(year, month, date, selectedHour.value, selectedMinute.value)
  } else {
    resultDate = new Date(year, month, date)
  }
  
  const formattedValue = formatDateTime(resultDate)
  
  emit('confirm', {
    value: formattedValue,
    date: resultDate,
    rawValue: resultDate
  })
  
  emit('update:show', false)
}

// 取消选择
const handleCancel = () => {
  emit('cancel')
  emit('update:show', false)
}

// 点击遮罩层
const handleOverlayClick = () => {
  handleCancel()
}

// 格式化日期时间
const formatDateTime = (date) => {
  if (!date || !(date instanceof Date)) {
    return ''
  }
  
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  
  if (props.type === 'datetime') {
    return `${year}-${month}-${day}T${hours}:${minutes}:00`
  } else {
    return `${year}-${month}-${day}`
  }
}

// 初始化
const initDate = () => {
  if (props.value) {
    let date
    if (props.value instanceof Date) {
      date = props.value
    } else if (typeof props.value === 'string') {
      date = new Date(props.value)
    } else {
      date = new Date()
    }
    
    if (!isNaN(date.getTime())) {
      currentYear.value = date.getFullYear()
      currentMonth.value = date.getMonth()
      selectedDate.value = {
        year: date.getFullYear(),
        month: date.getMonth(),
        date: date.getDate()
      }
      selectedHour.value = date.getHours()
      selectedMinute.value = date.getMinutes()
    }
  } else {
    const now = new Date()
    currentYear.value = now.getFullYear()
    currentMonth.value = now.getMonth()
    selectedDate.value = {
      year: now.getFullYear(),
      month: now.getMonth(),
      date: now.getDate()
    }
    selectedHour.value = now.getHours()
    selectedMinute.value = now.getMinutes()
  }
}

// 监听显示状态
watch(() => props.show, (newShow) => {
  if (newShow) {
    initDate()
  }
})

// 组件挂载时初始化
onMounted(() => {
  initDate()
})
</script>

<style scoped>
.activity-date-picker {
  position: relative;
}

.date-picker-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: var(--z-overlay);
}

.date-picker-popup {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  border-radius: 16px 16px 0 0;
  z-index: var(--z-modal);
  max-height: 80vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.date-picker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
  background: white;
}

.cancel-btn, .confirm-btn {
  background: none;
  border: none;
  font-size: 16px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 6px;
  transition: background-color 0.2s;
}

.cancel-btn {
  color: #666;
}

.cancel-btn:hover {
  background: #f5f5f5;
}

.confirm-btn {
  color: #1989fa;
  font-weight: 500;
}

.confirm-btn:hover {
  background: #f0f8ff;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.date-picker-content {
  flex: 1;
  overflow-y: auto;
  padding: 0 20px 20px;
}

.date-selector {
  margin: 20px 0;
}

.year-month-selector {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.arrow-btn {
  background: none;
  border: none;
  font-size: 20px;
  color: #1989fa;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.arrow-btn:hover:not(:disabled) {
  background: #f0f8ff;
}

.arrow-btn:disabled {
  color: #ccc;
  cursor: not-allowed;
}

.current-month {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  margin-bottom: 8px;
}

.weekday {
  text-align: center;
  font-size: 14px;
  color: #666;
  padding: 8px 0;
  font-weight: 500;
}

.date-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.date-cell {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.2s;
  position: relative;
}

.date-cell:hover:not(.disabled) {
  background: #f0f8ff;
}

.date-cell.other-month {
  color: #ccc;
}

.date-cell.today {
  color: #1989fa;
  font-weight: 600;
}

.date-cell.selected {
  background: #1989fa;
  color: white;
  font-weight: 600;
}

.date-cell.disabled {
  color: #ccc;
  cursor: not-allowed;
}

.time-selector {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.time-label {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.time-inputs {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.time-input-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.time-input-group label {
  font-size: 12px;
  color: #666;
}

.time-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 16px;
  background: white;
  cursor: pointer;
  min-width: 60px;
}

.time-select:focus {
  outline: none;
  border-color: #1989fa;
}

.time-separator {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-top: 20px;
}
</style>
