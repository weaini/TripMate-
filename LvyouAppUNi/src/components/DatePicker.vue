<template>
  <van-popup 
    v-model:show="show" 
    position="bottom" 
    round
    :close-on-click-overlay="true"
    @close="onCancel"
  >
    <van-date-picker
      v-model="pickerValue"
      :type="type"
      :title="title"
      :min-date="minDate"
      :max-date="maxDate"
      @confirm="onConfirm"
      @cancel="onCancel"
      :columns-type="getColumnsType()"
    />
  </van-popup>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { showToast } from 'vant'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  value: {
    type: [String, Date, Array],
    default: ''
  },
  type: {
    type: String,
    default: 'date' // date, datetime, time
  },
  title: {
    type: String,
    default: '选择日期'
  },
  minDate: {
    type: Date,
    default: () => new Date(1900, 0, 1)
  },
  maxDate: {
    type: Date,
    default: () => new Date(2030, 11, 31)
  }
})

const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

const show = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 内部选择器值，使用数组格式
const pickerValue = ref([])

// 监听外部值变化，初始化选择器值
watch(() => props.value, (newValue) => {
  if (newValue) {
    pickerValue.value = parseValueToArray(newValue)
  }
}, { immediate: true })

// 监听显示状态，初始化选择器值
watch(show, (newShow) => {
  console.log('DatePicker显示状态变化:', newShow)
  if (newShow) {
    if (props.value) {
      pickerValue.value = parseValueToArray(props.value)
    } else {
      // 如果没有初始值，使用当前时间
      const now = new Date()
      pickerValue.value = [
        now.getFullYear(),
        now.getMonth() + 1,
        now.getDate(),
        now.getHours(),
        now.getMinutes()
      ]
    }
    console.log('初始化选择器值:', pickerValue.value)
  }
})

// 解析各种格式的值到数组
const parseValueToArray = (value) => {
  if (!value) {
    const now = new Date()
    return [
      now.getFullYear(),
      now.getMonth() + 1,
      now.getDate(),
      now.getHours(),
      now.getMinutes()
    ]
  }
  
  let date
  if (value instanceof Date) {
    date = value
  } else if (typeof value === 'string') {
    date = new Date(value)
  } else if (Array.isArray(value)) {
    return value
  } else {
    date = new Date()
  }
  
  if (isNaN(date.getTime())) {
    const now = new Date()
    return [
      now.getFullYear(),
      now.getMonth() + 1,
      now.getDate(),
      now.getHours(),
      now.getMinutes()
    ]
  }
  
  const result = [
    date.getFullYear(),
    date.getMonth() + 1,
    date.getDate()
  ]
  
  if (props.type === 'datetime' || props.type === 'time') {
    result.push(date.getHours(), date.getMinutes())
  }
  
  return result
}

// 格式化数组值为字符串
const formatArrayToString = (value) => {
  if (!value || !Array.isArray(value) || value.length === 0) {
    return ''
  }
  
  const [year, month, day, hour = 0, minute = 0] = value
  
  if (props.type === 'date') {
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
  } else if (props.type === 'datetime') {
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}T${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}:00`
  } else if (props.type === 'time') {
    return `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`
  }
  
  return ''
}

// 确认选择
const onConfirm = (value) => {
  console.log('DatePicker确认:', value)
  
  let selectedValues = []
  
  // 处理Vant 4.x DatePicker的返回值
  if (value && typeof value === 'object') {
    if (value.selectedValues && Array.isArray(value.selectedValues)) {
      selectedValues = value.selectedValues
    } else if (Array.isArray(value)) {
      selectedValues = value
    }
  } else if (Array.isArray(value)) {
    selectedValues = value
  } else if (Array.isArray(pickerValue.value)) {
    selectedValues = pickerValue.value
  }
  
  // 如果无法获取选择的值，使用当前pickerValue
  if (selectedValues.length === 0 && Array.isArray(pickerValue.value) && pickerValue.value.length > 0) {
    selectedValues = pickerValue.value
  }
  
  if (selectedValues.length === 0) {
    console.error('无法获取选择的值')
    showToast('时间选择失败，请重试')
    return
  }
  
  const formattedValue = formatArrayToString(selectedValues)
  console.log('格式化后的值:', formattedValue)
  
  emit('confirm', {
    value: formattedValue,
    rawValue: selectedValues,
    date: new Date(selectedValues[0], selectedValues[1] - 1, selectedValues[2], selectedValues[3] || 0, selectedValues[4] || 0)
  })
  
  show.value = false
}

// 取消选择
const onCancel = () => {
  console.log('DatePicker取消')
  emit('cancel')
  show.value = false
}

// 获取列类型配置
const getColumnsType = () => {
  if (props.type === 'datetime') {
    return ['year', 'month', 'day', 'hour', 'minute']
  } else if (props.type === 'date') {
    return ['year', 'month', 'day']
  } else if (props.type === 'time') {
    return ['hour', 'minute']
  }
  return ['year', 'month', 'day']
}
</script>

<style scoped>
:deep(.van-date-picker) {
  background: white;
}

:deep(.van-picker__toolbar) {
  background: white;
  border-bottom: 1px solid #eee;
  padding: 12px 16px;
}

:deep(.van-picker__confirm) {
  color: #1989fa;
  font-weight: 500;
}

:deep(.van-picker__cancel) {
  color: #969799;
}

:deep(.van-picker__title) {
  font-weight: 600;
  color: #333;
}

:deep(.van-picker-column__item) {
  font-size: 16px;
  color: #333;
}

:deep(.van-picker-column__item--selected) {
  color: #1989fa;
  font-weight: 500;
}

:deep(.van-popup) {
  border-radius: 12px 12px 0 0;
}

:deep(.van-picker__frame) {
  border-top: 1px solid #eee;
  border-bottom: 1px solid #eee;
}
</style>
