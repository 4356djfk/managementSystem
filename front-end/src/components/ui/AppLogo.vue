<template>
  <div class="app-logo">
    <div class="logo-mark">
      <span>P</span>
    </div>
    <div>
      <div class="logo-name">Project Matrix</div>
      <div class="logo-copy">企业级项目管理工作台</div>
    </div>
  </div>
</template>

<style scoped>
.app-logo {
  display: inline-flex;
  align-items: center;
  gap: 14px;
}

.logo-mark {
  position: relative;
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  color: #fff;
  background: linear-gradient(135deg, var(--accent), var(--accent-secondary));
  box-shadow: var(--shadow-accent);
  overflow: hidden;
}

.logo-mark::before {
  content: '';
  position: absolute;
  inset: 2px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.24);
}

.logo-mark span {
  position: relative;
  z-index: 1;
  font-family: 'Calistoga', Georgia, serif;
  font-size: 1.3rem;
}

.logo-name {
  font-weight: 800;
  letter-spacing: -0.02em;
}

.logo-copy {
  margin-top: 2px;
  color: var(--muted-foreground);
  font-size: 0.82rem;
}
</style>
