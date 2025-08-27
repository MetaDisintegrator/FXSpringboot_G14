<!-- 文件：src/components/TrainList.vue -->
<template>
  <div class="train-list">
    <div v-if="trains.length === 0" class="no-result">
      🚄 暂无匹配的车次
    </div>
    <div v-else class="train-list-wrapper">
      <TrainCard
          v-for="item in trains"
          :key="item.train.mealId"
          :train="item.train"
          :seats="item.trainseats"
          class="train-item"
      />
    </div>
  </div>
</template>

<script setup>
import TrainCard from './TrainCard.vue'

defineProps({
  trains: {
    type: Array,
    default: () => []
  }
})
</script>

<style scoped>
.train-list {
  width: 100%;
}

.train-list-wrapper {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.train-item {
  transition: transform 0.2s ease;
}
.train-item:hover {
  transform: translateY(-2px);
}

.no-result {
  text-align: center;
  padding: 48px 16px;
  color: #999;
  font-size: 16px;
  background-color: #fdfdfd;
  border-radius: 12px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
  margin-top: 16px;
}
</style>