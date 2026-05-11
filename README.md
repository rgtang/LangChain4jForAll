# Java AI Agent - 智能助手系统

基于 LangChain4j 和 DeepSeek API 的完整 AI Agent 智能助手系统。

## 项目介绍

这是一个功能完整的 Java AI Agent 项目，实现了：

- ✅ **AI 流式聊天**：使用 SSE（Server-Sent Events）实现类似 ChatGPT 的打字机效果
- ✅ **聊天记忆**：支持多轮对话，AI 能记住上下文
- ✅ **Tool Calling**：AI 自动调用 Java 方法（天气查询、时间查询、计算器）
- ✅ **RAG 知识库**：企业知识库问答，支持文档检索和上下文增强
- ✅ **Agent 智能决策**：AI 自动决定是否调用工具或查询知识库

## 技术栈

- **JDK**: 1.8
- **Spring Boot**: 2.7.18
- **Spring WebFlux**: 响应式编程和 SSE 支持
- **LangChain4j**: 0.27.1（AI 应用开发框架）
- **DeepSeek API**: OpenAI 兼容的国产大模型
- **Maven**: 项目构建工具

## 项目结构

```
java-ai-agent/
├── src/
│   ├── main/
│   │   ├── java/com/aiagent/
│   │   │   ├── config/
│   │   │   │   ├── AiConfig.java          # AI 配置（模型、记忆、助手）
│   │   │   │   └── RagConfig.java         # RAG 配置（向量存储、文档加载）
│   │   │   ├── controller/
│   │   │   │   └── ChatController.java    # 聊天接口控制器
│   │   │   ├── service/
│   │   │   │   ├── Assistant.java         # AI 助手接口
│   │   │   │   └── AiChatService.java     # AI 聊天服务
│   │   │   ├── tools/
│   │   │   │   ├── WeatherTools.java      # 天气查询工具
│   │   │   │   ├── TimeTools.java         # 时间查询工具
│   │   │   │   └── CalculatorTools.java   # 计算器工具
│   │   │   ├── rag/
│   │   │   │   └── RagService.java        # RAG 检索服务
│   │   │   └── AiAgentApplication.java    # 启动类
│   │   └── resources/
│   │       ├── static/
│   │       │   └── index.html             # 前端聊天页面
│   │       ├── docs/
│   │       │   └── company.txt            # 知识库文档
│   │       └── application.yml            # 配置文件
│   └── test/
├── pom.xml                                 # Maven 配置
└── README.md                               # 项目文档
```

## 快速开始

### 1. 环境要求

- JDK 8 或更高版本
- Maven 3.6+
- DeepSeek API Key（或其他 OpenAI 兼容 API）

### 2. 配置 API Key

编辑 `src/main/resources/application.yml`：

```yaml
ai:
  # 替换为你的 DeepSeek API Key
  api-key: sk-your-deepseek-api-key-here
  # API 地址
  base-url: https://api.deepseek.com/v1
  # 模型名称
  model: deepseek-chat
```

### 3. 启动项目

```bash
# 方式 1：使用 Maven 启动
mvn spring-boot:run

# 方式 2：打包后启动
mvn clean package
java -jar target/java-ai-agent-1.0.0.jar
```

### 4. 访问应用

打开浏览器访问：http://localhost:8080

## 功能演示

### 1. 普通聊天

```
用户：你好
AI：你好！我是小智，很高兴为你服务。有什么我可以帮你的吗？
```

### 2. Tool Calling - 天气查询

```
用户：北京天气怎么样
AI：[自动调用 WeatherTools.getWeather("北京")]
    北京今天天气晴朗，温度 15-25℃，空气质量良好，适合户外活动。
```

### 3. Tool Calling - 时间查询

```
用户：现在几点了
AI：[自动调用 TimeTools.getCurrentTime()]
    当前时间是：2024年05月09日 14:30:25
```

### 4. Tool Calling - 数学计算

```
用户：123 加 456 等于多少
AI：[自动调用 CalculatorTools.add(123, 456)]
    123 + 456 = 579
```

### 5. RAG 知识库问答

```
用户：公司年假多少天
AI：[自动检索知识库]
    根据公司规定：
    - 工作满 1 年：5 天年假
    - 工作满 5 年：10 天年假
    - 工作满 10 年：15 天年假
```

### 6. 多轮对话记忆

```
用户：我叫张三
AI：你好，张三！很高兴认识你。

用户：我叫什么名字
AI：你叫张三。
```

## 核心原理

### 1. SSE 流式输出原理

SSE（Server-Sent Events）是 HTML5 的一种服务器推送技术：

```java
// 后端：返回 Flux<String>
@PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> chatStream(@RequestBody ChatRequest message) {
    return aiChatService.chatStream(message.getMessage());
}
```

```javascript
// 前端：使用 fetch 接收流式数据
fetch('/api/chat/stream', {
    method: 'POST',
    body: JSON.stringify({ message: userMessage })
})
.then(response => {
    const reader = response.body.getReader();
    // 逐字读取并显示
});
```

### 2. ChatMemory 原理

LangChain4j 使用 `MessageWindowChatMemory` 保存对话历史：

```java
@Bean
public ChatMemory chatMemory() {
    // 保留最近 10 条消息
    return MessageWindowChatMemory.withMaxMessages(10);
}
```

每次对话时，AI 都能看到之前的消息，从而实现上下文理解。

### 3. Tool Calling 原理

使用 `@Tool` 注解标记方法，AI 会自动决定何时调用：

```java
@Tool("查询指定城市的天气情况")
public String getWeather(String city) {
    return "北京今天天气晴朗...";
}
```

LangChain4j 会：
1. 将工具描述发送给 AI
2. AI 分析用户问题，决定是否需要调用工具
3. 如果需要，AI 生成工具调用参数
4. LangChain4j 执行工具方法
5. 将结果返回给 AI
6. AI 基于结果生成最终回答

### 4. RAG 原理

RAG（Retrieval-Augmented Generation）检索增强生成：

```
用户问题
    ↓
向量化（Embedding）
    ↓
向量数据库检索
    ↓
获取相关文档片段
    ↓
拼接到提示词
    ↓
AI 基于上下文回答
```

实现代码：

```java
// 1. 向量化问题
Embedding queryEmbedding = embeddingModel.embed(query).content();

// 2. 检索相似内容
List<EmbeddingMatch<TextSegment>> matches = 
    embeddingStore.findRelevant(queryEmbedding, 3, 0.5);

// 3. 构建增强提示词
String prompt = "请基于以下知识库内容回答问题：\n" + context + "\n问题：" + query;

// 4. AI 回答
assistant.chat(prompt);
```

### 5. Agent 原理

Agent 是 AI 的智能决策能力：

```java
public Flux<String> chatStream(String userMessage) {
    // Agent 决策：是否需要 RAG
    if (ragService.shouldUseRag(userMessage)) {
        // 使用 RAG
        String context = ragService.retrieveRelevantKnowledge(userMessage, 3);
        userMessage = ragService.buildAugmentedPrompt(userMessage, context);
    }
    
    // AI 自动决策：是否调用 Tools
    return assistant.chat(userMessage);
}
```

Agent 会根据用户问题自动选择：
- 直接回答
- 调用工具
- 查询知识库
- 组合使用

## DeepSeek API 配置

### 获取 API Key

1. 访问 [DeepSeek 官网](https://platform.deepseek.com/)
2. 注册并登录
3. 进入 API Keys 页面
4. 创建新的 API Key

### 配置方法

在 `application.yml` 中配置：

```yaml
ai:
  api-key: sk-xxxxxxxxxxxxxxxx  # 你的 API Key
  base-url: https://api.deepseek.com/v1
  model: deepseek-chat
  temperature: 0.7
  max-tokens: 2000
  timeout: 60
```

### 使用其他模型

本项目支持任何 OpenAI 兼容的 API，例如：

**使用 OpenAI：**
```yaml
ai:
  api-key: sk-xxxxxxxxxxxxxxxx
  base-url: https://api.openai.com/v1
  model: gpt-4
```

**使用智谱 AI：**
```yaml
ai:
  api-key: xxxxxxxxxxxxxxxx
  base-url: https://open.bigmodel.cn/api/paas/v4
  model: glm-4
```

## API 接口

### 流式聊天

```http
POST /api/chat/stream
Content-Type: application/json

{
  "message": "你好"
}
```

响应：`text/event-stream`（流式文本）

### 健康检查

```http
GET /api/chat/health
```

响应：`OK`

## 自定义扩展

### 添加新的 Tool

1. 创建工具类：

```java
@Component
public class MyTools {
    @Tool("工具描述")
    public String myMethod(String param) {
        // 实现逻辑
        return "结果";
    }
}
```

2. 在 `AiConfig` 中注册：

```java
@Bean
public Assistant assistant(..., MyTools myTools) {
    return AiServices.builder(Assistant.class)
        .tools(..., myTools)
        .build();
}
```

### 添加新的知识库文档

1. 将文档放到 `src/main/resources/docs/` 目录
2. 修改 `RagConfig.java` 加载逻辑
3. 重启应用

## 常见问题

### 1. 启动失败：找不到 API Key

确保 `application.yml` 中配置了正确的 API Key。

### 2. AI 响应很慢

- 检查网络连接
- 尝试增加 `timeout` 配置
- 考虑使用国内模型（如 DeepSeek）

### 3. Tool 没有被调用

- 确保工具类使用了 `@Component` 注解
- 确保在 `AiConfig` 中注册了工具
- 检查工具描述是否清晰

### 4. RAG 检索不到内容

- 确保知识库文档存在
- 检查文档编码是否为 UTF-8
- 尝试调整相似度阈值

## 学习资源

- [LangChain4j 官方文档](https://docs.langchain4j.dev/)
- [DeepSeek API 文档](https://platform.deepseek.com/docs)
- [Spring WebFlux 文档](https://docs.spring.io/spring-framework/reference/web/webflux.html)

## 许可证

MIT License

## 联系方式

如有问题，欢迎提 Issue 或 PR。

---

**祝你使用愉快！🎉**
