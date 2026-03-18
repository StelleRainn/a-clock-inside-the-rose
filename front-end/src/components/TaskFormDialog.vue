<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? 'Edit Task' : 'Create Task'"
    @update:model-value="$emit('update:visible', $event)"
    width="500px"
  >
    <el-form :model="form" label-width="100px">
      <el-form-item label="Title">
        <el-input v-model="form.title" />
      </el-form-item>
      <el-form-item label="Description">
        <el-input v-model="form.description" type="textarea" />
      </el-form-item>
      
      <!-- Tag Selection -->
      <el-form-item label="Tags">
        <el-select
          v-model="selectedTagIds"
          multiple
          filterable
          allow-create
          default-first-option
          placeholder="Select or create tags"
          @change="handleTagChange"
        >
          <el-option
            v-for="tag in availableTags"
            :key="tag.id"
            :label="tag.name"
            :value="tag.id"
          >
            <span :style="{ color: tag.color }">●</span> {{ tag.name }}
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="Priority">
        <el-select v-model="form.priority">
          <el-option label="Low" value="LOW" />
          <el-option label="Medium" value="MEDIUM" />
          <el-option label="High" value="HIGH" />
        </el-select>
      </el-form-item>
      <el-form-item label="Status">
        <el-select v-model="form.status">
          <el-option label="To Do" value="TODO" />
          <el-option label="In Progress" value="IN_PROGRESS" />
          <el-option label="Done" value="DONE" />
        </el-select>
      </el-form-item>
      <el-form-item label="Due Date">
        <el-date-picker
          v-model="form.dueDate"
          type="datetime"
          placeholder="Select date and time"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="$emit('update:visible', false)">Cancel</el-button>
        <el-button type="primary" @click="handleSubmit">Confirm</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { getTags, createTag } from '@/api/tag'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: Boolean,
  isEdit: Boolean,
  initialData: Object
})

const emit = defineEmits(['update:visible', 'submit', 'tag-created'])

const userStore = useUserStore()
const selectedTagIds = ref([])
const availableTags = ref([])

const fetchTags = async () => {
  if (!userStore.user?.id) return
  try {
    const data = await getTags(userStore.user.id)
    availableTags.value = data || []
  } catch (error) {
    console.error('Failed to fetch tags', error)
  }
}

const form = reactive({
  title: '',
  description: '',
  priority: 'MEDIUM',
  status: 'TODO',
  dueDate: ''
})

watch(() => props.visible, (val) => {
  if (val) {
    fetchTags()
    if (props.isEdit && props.initialData) {
      Object.assign(form, props.initialData)
      selectedTagIds.value = props.initialData.tags ? props.initialData.tags.map(t => t.id) : []
    } else {
      resetForm()
    }
  }
})

const resetForm = () => {
  form.title = ''
  form.description = ''
  form.priority = 'MEDIUM'
  form.status = 'TODO'
  form.dueDate = ''
  selectedTagIds.value = []
}

const handleTagChange = async (val) => {
  const newTagNames = val.filter(v => typeof v === 'string')
  if (newTagNames.length > 0) {
    for (const name of newTagNames) {
      try {
        const newTag = await createTag({
          userId: userStore.user.id,
          name: name,
          color: getRandomColor()
        })
        emit('tag-created', newTag)
        // Replace string ID with real ID
        const index = selectedTagIds.value.indexOf(name)
        if (index !== -1) {
          selectedTagIds.value[index] = newTag.id
        }
      } catch {
        ElMessage.error('Failed to create tag: ' + name)
      }
    }
  }
}

const getRandomColor = () => {
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#a0cfff', '#b3e19d']
  return colors[Math.floor(Math.random() * colors.length)]
}

const handleSubmit = () => {
  if (!form.title) {
    ElMessage.warning('Title is required')
    return
  }
  
  const tagsPayload = selectedTagIds.value.map(id => ({ id }))
  
  const payload = {
    ...form,
    tags: tagsPayload
  }
  
  emit('submit', payload)
}
</script>
