<template>
  <a-config-provider :locale="zhCN">
    <router-view />
  </a-config-provider>
</template>

<script setup lang="ts">
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import { AppConfig } from '@/utils/env'

// 设置页面标题
document.title = AppConfig.title
</script>
