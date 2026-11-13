# 原项目视觉迁移约定

页面样式以 `D:\Code\Java\newbee-mall-pro-master\src\main\resources\static` 与 `templates` 为准。本项目复用原商城的 `mall/css/common.css`、`mall/styles/*.css`、图片和字体，以及管理后台的 Bootstrap 4、AdminLTE 3 和原页面 CSS。商城主色为 `#1baeae`，购物操作沿用原项目的橙色；后台沿用深色侧栏和原有表格、按钮、表单类名。

新增 Vue 组件时，先对照对应的 Thymeleaf 模板保留 DOM 层次和 class，复用 `public/mall`、`public/admin` 的样式与静态资源。只有品牌文字和标志替换为 haoxiang。页面结构或视觉需要调整时，应明确记录与原项目的差异。

`src/styles/legacyStyles.js` 按路由加载原页面的样式，避免商城与后台的全局 CSS 互相覆盖。资源路径保持原始的 `/mall/...`、`/admin/...`；商品上传图由 Vite 代理到旧后端。
