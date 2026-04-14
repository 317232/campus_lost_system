<script setup>
const props = defineProps({
  links: {
    type: Array,
    default: () => [],
  },
  title: {
    type: String,
    default: '校园失物招领',
  },
  subtitle: {
    type: String,
    default: '',
  },
})
</script>

<template>
  <header class="site-nav">
    <div>
      <p class="eyebrow">{{ props.subtitle || 'Coursework Demo' }}</p>
      <h1>{{ props.title }}</h1>
    </div>

    <nav>
      <RouterLink
        v-for="link in props.links"
        :key="link.to"
        :to="link.to"
        class="nav-link"
      >
        {{ link.label }}
      </RouterLink>
    </nav>
  </header>
</template>
