<script setup>
defineProps({
  label: {
    type: String,
    required: true
  },
  modelValue: {
    type: [String, Number],
    default: ''
  },
  type: {
    type: String,
    default: 'text'
  },
  placeholder: {
    type: String,
    default: ''
  },
  wrapperStyle: {
    type: [String, Object],
    default: ''
  }
})

defineEmits(['update:modelValue'])
</script>

<template>
  <div class="form-item">
    <label class="form_label">{{ label }}
      <div class="form-input-wrapper" :style="wrapperStyle">
        <input 
          :type="type" 
          class="form-input" 
          :value="modelValue" 
          @input="$emit('update:modelValue', $event.target.value)" 
          :placeholder="placeholder" 
        />
        <slot name="append"></slot>
      </div>
    </label>
  </div>
</template>
