<template>
  <div class="markdown-table-wrapper">
    <table class="markdown-table">
      <thead>
        <tr>
          <th v-for="(header, index) in headers" :key="index">{{ header }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(row, rowIndex) in tableData" :key="rowIndex">
          <td v-for="(cell, cellIndex) in row" :key="cellIndex">{{ cell }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  // Raw HTML table element
  html?: string
  // Or structured data
  headers?: string[]
  data?: Record<string, string>[]
}

const props = withDefaults(defineProps<Props>(), {
  headers: () => [],
  data: () => []
})

// Sanitize key for table cell access
function sanitizeKey(header: string, index: number): string {
  return header
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '_')
    .replace(/^_+|_+$/g, '') || `col_${index}`
}

// Parse table data from structured format
const tableData = computed(() => {
  if (props.data && props.data.length > 0) {
    return props.data.map(row => {
      return props.headers!.map((h, i) => {
        const key = sanitizeKey(h, i)
        return row[key] || row[h] || ''
      })
    })
  }
  return []
})
</script>

<style scoped>
.markdown-table-wrapper {
  width: 100%;
  margin: 1em 0;
  overflow-x: auto;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.markdown-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9em;
}

.markdown-table th,
.markdown-table td {
  padding: 0.75em 1em;
  border: 1px solid #e5e7eb;
  text-align: left;
}

.markdown-table th {
  background: #f9fafb;
  font-weight: 600;
  color: #374151;
  position: sticky;
  top: 0;
}

.markdown-table tr:nth-child(even) {
  background: #f9fafb;
}

.markdown-table tr:hover {
  background: #f3f4f6;
}

/* Scrollbar styling */
.markdown-table-wrapper::-webkit-scrollbar {
  height: 8px;
}

.markdown-table-wrapper::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.markdown-table-wrapper::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.markdown-table-wrapper::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}
</style>
