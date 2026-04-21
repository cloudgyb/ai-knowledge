<template>
  <div class="ai-model-select">
    <a-select v-model:value="selectedAiId" placeholder="请选择模型" allowClear
              :bordered="false" @change="handleChange">
      <a-select-option
          v-for="item in options"
          :key="item.id"
          :value="item.id"
      ><img :src="item.provider?.logoUrl" alt="logo" style="width: 20px; height: 20px; margin-right: 8px;">
        {{ item.customName }}
      </a-select-option>
    </a-select>
  </div>
</template>
<script setup lang="ts">
import {onMounted, ref} from "vue";
import type {AiModel} from "@/api/model/aiModelTypes.ts";
import {modelApi} from "@/api/model";

const emits = defineEmits(['change'])
const selectedAiId = ref<number>()
const options = ref<AiModel[]>([])
const handleChange = (value: number) => {
  console.log(`selected ${value}`)
  emits('change', value)
}
const loadAiModels = async () => {
  const res = await modelApi.getList({type: 'LANG', pageSize: 100, pageNum: 1})
  if (res.code === '200') {
    options.value = res.data.records
    if (options.value.length > 0) {
      selectedAiId.value = options.value[0].id
    }
  }
}
onMounted(() => {
  loadAiModels()
})
</script>