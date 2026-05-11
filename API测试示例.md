# AI Agent 多用户上下文隔离 - API 测试示例

## 问题说明

**原问题**：不同的实例和 AI agent 交互时，保存的都是同一份上下文。

**原因**：使用了全局单例的 `ChatMemory`，所有用户共享同一个对话历史。

**解决方案**：使用 `ChatMemoryProvider` + `ChatMemoryStore`，为每个用户/会话创建独立的上下文。

---

## 核心改动

### 1. 添加会话 ID 支持

请求体现在需要包含 `sessionId` 字段：

```json
{
  "message": "你好",
  "sessionId": "user-123"
}
```

### 2. 使用 ChatMemoryProvider

- 每个 `sessionId` 对应一个独立的 `ChatMemory`
- 不同用户的对话历史完全隔离
- 同一用户的多轮对话可以保持上下文

---

## API 测试示例

### 测试场景：两个用户同时对话

#### 用户 A（sessionId: user-alice）

**第 1 轮对话**：
```bash
curl -X POST http://localhost:8080/api/chat/stream \
  -H "Content-Type: application/json" \
  -d '{
    "message": "我叫 Alice，我喜欢编程",
    "sessionId": "user-alice"
  }'
```

**第 2 轮对话**：
```bash
curl -X POST http://localhost:8080/api/chat/stream \
  -H "Content-Type: application/json" \
  -d '{
    "message": "我刚才说我叫什么？",
    "sessionId": "user-alice"
  }'
```

**预期响应**：AI 应该回答 "你叫 Alice"

---

#### 用户 B（sessionId: user-bob）

**第 1 轮对话**：
```bash
curl -X POST http://localhost:8080/api/chat/stream \
  -H "Content-Type: application/json" \
  -d '{
    "message": "我叫 Bob，我喜欢音乐",
    "sessionId": "user-bob"
  }'
```

**第 2 轮对话**：
```bash
curl -X POST http://localhost:8080/api/chat/stream \
  -H "Content-Type: application/json" \
  -d '{
    "message": "我刚才说我叫什么？",
    "sessionId": "user-bob"
  }'
```

**预期响应**：AI 应该回答 "你叫 Bob"（而不是 Alice）

---

## 前端集成示例

### JavaScript / Fetch API

```javascript
// 为每个用户生成唯一的 sessionId
const sessionId = `user-${Date.now()}-${Math.random()}`;

async function sendMessage(message) {
  const response = await fetch('http://localhost:8080/api/chat/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      message: message,
      sessionId: sessionId  // 同一用户使用相同的 sessionId
    })
  });

  const reader = response.body.getReader();
  const decoder = new TextDecoder();

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;
    
    const chunk = decoder.decode(value);
    console.log(chunk);  // 流式输出
  }
}
```

