完整仓库地址：https://github.com/cyh-2136/task_manage_system
数据库脚本地址：taskManageSystem\backend\src\main\resources\db

# 内部任务管理系统

一个支持导师和实习生协作的简易任务管理平台，基于 Spring Boot 3 + Vue 3 全栈实现。

## 技术栈

| 层 | 技术 |
|----|------|
| 后端 | Java 21 + Spring Boot 3.2 + MyBatis-Plus 3.5 |
| 数据库 | MySQL 8 |
| 前端 | Vue 3 (Composition API) + Vite 8 + Element Plus |
| 图表 | ECharts + Vue-ECharts |
| 认证 | JWT + MD5 + Cookie |
| 权限 | 自定义 @RequireRole 注解 + AOP |
| API文档 | Knife4j (Swagger UI) |
| 其他 | Axios, EasyExcel, Rome RSS, Lombok |

## 快速启动

### 1. 数据库初始化

```sql
-- 在 MySQL 中执行
source backend/src/main/resources/db/schema-mysql.sql
source backend/src/main/resources/db/data-mysql.sql
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端启动在 `http://localhost:8080`，API文档 `http://localhost:8080/doc.html`

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动在 `http://localhost:3000`

### 4. 测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| mentor | 123456 | 导师 |
| alice | 123456 | 实习生 |
| bob | 123456 | 实习生 |

## 功能清单

### 核心功能
- 用户注册/登录（JWT + Cookie）
- 任务 CRUD（创建/查看/编辑/删除）
- 任务状态流转（待办→进行中→已完成，可自由切换）
- 任务筛选搜索（状态/负责人/截止日期/关键词）
- 任务列表分页
- 实时资讯（多 RSS 源聚合 + 搜索 + 刷新）

### 加分功能
- 任务优先级（高/中/低）+ 颜色标签
- 仪表盘统计图表（ECharts 饼图）
- 截止日期倒计时提醒
- 导师/实习生权限分离（@RequireRole 注解 + AOP）
- 导出任务为 Excel
- 任务详情关联实时资讯
- 注册页面

## 项目结构

```
backend/               # Spring Boot 后端
frontend/              # Vue 3 前端
README.md              # 本文件
```

## AI 辅助说明

本项目使用 opencode 作为主要 AI 开发助手，协助完成了代码生成、bug 调试、架构设计等工作。
