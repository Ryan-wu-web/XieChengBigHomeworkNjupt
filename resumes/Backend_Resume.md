# 后端工程师简历 - 个人项目汇总 (针对携程求职优化)

## 核心技能
- **核心框架**: Java (SpringBoot), SpringMVC, MyBatis-Plus, MyBatis
- **中间件与数据库**: MySQL, Redis (分布式缓存/并发处理), Spring Cloud (微服务理念)
- **Node.js**: Koa, Next.js Route Handlers
- **API 设计**: RESTful API, JWT 鉴权, Axios (Node.js 端)
- **AI 集成**: OpenAI Whisper/GPT-4o, 阿里云 NLS STT/TTS, 智谱 AI GLM-4V
- **系统架构**: 分层架构 (Controller/Service/Mapper), 数据库表设计, 高并发场景优化

## 项目经历

### 1. 得物电商平台 (后端开发)
**核心开发人员 | SpringBoot + MyBatis + MySQL + Redis**
- **项目描述**: 仿得物电商系统的核心业务后端，涵盖商品管理、订单处理及支付流程。
- **核心贡献**:
    - 基于 **SpringBoot + MyBatis** 构建服务端，独立设计并实现商品、订单、支付等核心业务模块。
    - 针对高并发场景（抢购/下单），引入 **Redis** 缓存机制，有效缓解数据库压力，防止超卖现象。
    - 集成 **AlipaySDK** 实现支付闭环，确保交易过程的安全性与原子性。
    - 负责数据库表结构设计与 SQL 优化，通过索引优化提升查询效率。

### 2. 益宿 (Trip Project) - 服务端
**后端负责人 | SpringBoot + MyBatis-Plus + MySQL + JWT**
- **项目描述**: 酒店预订与管理系统的后端支撑，支持酒店搜索、详情展示及用户交互（评论/收藏）。
- **核心贡献**:
    - 基于 **SpringBoot + MyBatis-Plus** 搭建高性能服务端，实现酒店列表多条件筛选（名称、城市、星级）。
    - 独立设计酒店、房型、订单、评论、收藏等 **10+ 张核心数据库表**，支撑复杂业务逻辑。
    - 实现基于 **JWT** 的统一身份认证与授权方案，保障系统安全性。
    - 针对 Android 端展示需求，封装 Map 类型的统计信息返回，优化网络传输与数据展示效率。
    - 集成 **智谱 AI GLM-4** 接口，实现 AI 酒店助手对话服务，提升用户体验。

### 3. 阿佑关怀 AI 平台 (后端侧)
**全栈开发 (后端侧) | Node.js (Next.js Route Handlers) + OpenAI + 阿里云 NLS**
- **项目描述**: 为空巢老人提供 AI 语音陪伴的后端服务，处理语音识别、文本生成及语音合成。
- **核心贡献**:
    - 使用 **Next.js Route Handlers (Node.js)** 构建轻量级 API 服务，处理 AI 交互的核心逻辑。
    - 对接 **OpenAI Whisper/GPT-4o** 接口，实现实时语音转文本与智能对话生成。
    - 集成 **阿里云 NLS STT/TTS**，实现高保真的语音识别与合成，提升老人交互的自然度。
    - 建立多级错误回退机制（阿里云优先，失败回退 OpenAI），保障 AI 服务的高可用性。

### 4. 乡村应用 (服务端)
**核心开发者 | Node.js (Koa) + Multer + Natural (文本分类) + 智谱 AI**
- **项目描述**: 赋能乡村治理与电商的后端系统，支持图片识别审核与文本分类。
- **核心贡献**:
    - 基于 **Koa** 框架实现高效 RESTful API 服务，负责前后端联动开发。
    - 利用 **Natural** 库实现基础文本分类，结合 **智谱 AI GLM-4V-Flash** 实现复杂的视觉识别逻辑（随手拍识别）。
    - 负责电商与积分兑换业务逻辑设计，打通端到端业务流。
