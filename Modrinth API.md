# Modrinth API 实践指南

基于实际开发经验整理的 Modrinth API 使用指南，包含常见问题解决方案和最佳实践。

## API 调用关键问题

### 1. 版本列表端点
**用法**: `/project/{id}/version` (单数形式)

### 2. 查询参数格式
**用法**: 直接使用 JSON 数组格式
```java
// 正确示例
.addQueryParameter("game_versions", "[\"1.20.1\"]")
.addQueryParameter("loaders", "[\"fabric\"]")
```

### 3. 参数名称使用复数形式
- `game_versions` (不是 `game_version`)
- `loaders` (不是 `loader`)

## 🧭 API 基础与核心概念

开始调用API前，有几个核心概念需要你了解：

- **Base URL**: Modrinth API v2 的基地址是 `https://api.modrinth.com/v2/`
- **项目标识**: 在Modrinth上，每个项目（模组、资源包等）有两种主要标识符：
  - **ID或Slug**: `P7dR8mSH` 或 `fabric-api`。Slug更具可读性，但可能改变，而ID是永久不变的。
  - **版本ID**: 每个版本都有其唯一ID，例如 `nOI7bsDO`。
- **User-Agent**: Modrinth要求请求设置正确的`User-Agent`头部，以便识别请求来源。建议格式为`用户名/应用版本 (联系邮箱或网站)`。

## 🔍 查询与搜索模组

搜索模组是探索Modrinth生态的起点，你可以使用 `/search` 端点。

**GET** `https://api.modrinth.com/v2/search`

### 搜索参数

| 参数       | 描述                               | 示例             |
|:---------|:---------------------------------|:---------------|
| `query`  | 搜索关键词。                           | `query=sodium` |
| `facets` | 用于过滤结果的**核心工具**，需要按照特定格式构建查询字符串。 | 见下方详解          |
| `limit`  | 返回结果数量 (默认20, 最大100)。            | `limit=10`     |
| `offset` | 用于分页，跳过前N个结果。                    | `offset=20`    |

### Facets 过滤的正确格式

Facets 需要以 JSON 数组格式提供，每个子数组代表一个过滤条件：

```json
[
  ["project_type:mod"],
  ["versions:1.20.1"], 
  ["categories:fabric"]
]
```

**实际代码实现**:
```java
JsonArray facetsArray = new JsonArray();

JsonArray projectTypeFacet = new JsonArray();
projectTypeFacet.add("project_type:mod");
facetsArray.add(projectTypeFacet);

JsonArray versionFacet = new JsonArray(); 
versionFacet.add("versions:1.20.1");
facetsArray.add(versionFacet);

JsonArray loaderFacet = new JsonArray();
loaderFacet.add("categories:fabric");
facetsArray.add(loaderFacet);

urlBuilder.addQueryParameter("facets", gson.toJson(facetsArray));
```

## 📖 获取模组详细信息

### 获取项目详情
**GET** `https://api.modrinth.com/v2/project/{id|slug}`

返回项目的完整详情，包括描述、徽章、团队ID、下载数、更新日期、主页链接等。

### 获取项目的版本列表
**GET** `https://api.modrinth.com/v2/project/{id|slug}/version` (注意是单数形式)

此端点返回该项目所有的版本列表。可以使用以下参数过滤：

- `game_versions`: 游戏版本过滤（复数形式）
- `loaders`: 加载器过滤（复数形式）

**正确实现示例**:
```java
public String getModVersions(String projectIdOrSlug, String gameVersion, String loader) throws IOException {
    HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + "project/" + projectIdOrSlug + "/version").newBuilder()
            .addQueryParameter("game_versions", "[\"" + gameVersion + "\"]")
            .addQueryParameter("loaders", "[\"" + loader + "\"]");

    Request request = new Request.Builder()
            .url(urlBuilder.build())
            .addHeader("User-Agent", userAgent)
            .build();
    
    try (Response response = httpClient.newCall(request).execute()) {
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        return response.body().string();
    }
}
```

## 📦 处理版本与文件

### 获取特定版本信息
**GET** `https://api.modrinth.com/v2/version/{id}`

响应中包含 `files` 数组，通常第一个元素是主要文件。

### 版本文件关键字段
- `url`: 文件的直接下载链接
- `filename`: 文件名称
- `hashes`: 文件哈希值（用于完整性校验）
- `primary`: 是否为主要文件

## 💾 下载模组文件

### 下载方式
1. **通过文件URL直接下载**
2. **使用Modrinth下载端点**: `https://api.modrinth.com/v2/version_file/{hash}/download`

### 完整下载流程示例
```java
public boolean downloadModFile(String fileUrl, File outputFile) throws IOException {
    Request request = new Request.Builder()
            .url(fileUrl)
            .addHeader("User-Agent", userAgent)
            .build();

    try (Response response = httpClient.newCall(request).execute()) {
        if (!response.isSuccessful()) {
            AutoUpdate.LOGGER.error("下载失败: HTTP {}", response.code());
            return false;
        }
        
        try (InputStream inputStream = response.body().byteStream();
             FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        
        AutoUpdate.LOGGER.info("成功下载模组到: {}", outputFile.getAbsolutePath());
        return true;
    }
}
```

## ⚠️ 常见问题与解决方案

### 问题1: 404 错误
**症状**: 调用版本列表接口返回 404  
**原因**: 使用了错误的端点 `/versions`（复数形式）  
**解决**: 使用正确的端点 `/version`（单数形式）

### 问题2: 参数格式错误
**症状**: 查询结果不符合预期  
**原因**: 参数格式不正确或双重编码  
**解决**: 使用正确的 JSON 数组格式，避免双重编码

### 问题3: User-Agent 缺失
**症状**: 请求被拒绝  
**原因**: 未设置 User-Agent 头部  
**解决**: 添加正确的 User-Agent 头部

## 🔧 最佳实践

1. **错误处理**: 对所有 API 调用进行完善的错误处理
2. **异步操作**: 在网络线程中执行 API 调用，避免阻塞主线程
3. **版本协商**: 实现自动版本协商逻辑
4. **依赖处理**: 检查并处理模组依赖关系
5. **文件校验**: 下载后验证文件哈希值

通过遵循这些指南，可以避免常见的 API 调用问题，确保模组下载功能的稳定运行。