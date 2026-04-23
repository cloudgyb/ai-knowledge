<script setup lang="ts">
import {ref, computed, type VNode, watchEffect} from "vue";

const props = defineProps<{
  highlightVnode: VNode;
  language: string;
}>();

const copied = ref(false);
const contentRef = ref<HTMLElement>();

function copyHandle() {
  if (!contentRef.value) return;
  navigator.clipboard.writeText(contentRef.value.textContent || "");
  copied.value = true;
  setTimeout(() => (copied.value = false), 2000);
}

watchEffect(() => {
  console.log(props.language, props.highlightVnode);
});
const langLabel = computed(() => props.language?.toUpperCase() || "TEXT");
</script>

<template>
  <div class="" style="background: #f5f5f5; border: 1px #f5f5f5 solid; border-radius: 5px">
    <!-- 顶部标签行 -->
    <div class="">
      <span class="">
        {{ langLabel }}
      </span>
      <div class="" @click="copyHandle">
        <template v-if="copied">
          <div class="">
            <pre class="">Copied!</pre>
          </div>

        </template>
        <template v-else>

        </template>
      </div>
    </div>

    <!-- 高亮代码区域 -->
    <div ref="contentRef" class="">
      <component :is="highlightVnode"/>
    </div>
  </div>
</template>
