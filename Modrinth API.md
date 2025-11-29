Modrinth提供了清晰且功能丰富的API，非常适合用来实现模组的查询与下载。其官方API文档结构清晰，下面我将为你梳理如何利用这些接口，并提供一个清晰的流程指南。

### 🧭 API 基础与核心概念

开始调用API前，有几个核心概念需要你了解：

- **Base URL**: Modrinth API v2 的基地址是 `https://api.modrinth.com/v2/`
- **项目标识**: 在Modrinth上，每个项目（模组、资源包等）有两种主要标识符：
  - **ID或Slug**: `P7dR8mSH` 或 `fabric-api`。Slug更具可读性，但可能改变，而ID是永久不变的。
  - **版本ID**: 每个版本都有其唯一ID，例如 `nOI7bsDO`。
- **User-Agent**: Modrinth要求请求设置正确的`User-Agent`头部，以便识别请求来源。建议格式为`用户名/应用版本 (联系邮箱或网站)`。

### 🔍 查询与搜索模组

搜索模组是探索Modrinth生态的起点，你可以使用 `/search` 端点。

**GET** `https://api.modrinth.com/v2/search`

搜索时，可以使用以下参数来精确查找：

| 参数     | 描述                                                         | 示例           |
| :------- | :----------------------------------------------------------- | :------------- |
| `query`  | 搜索关键词。                                                 | `query=sodium` |
| `facets` | 用于过滤结果的**核心工具**，需要按照特定格式构建查询字符串。 | 见下方详解     |
| `limit`  | 返回结果数量 (默认20, 最大100)。                             | `limit=10`     |
| `offset` | 用于分页，跳过前N个结果。                                    | `offset=20`    |

**理解Facets过滤**
Facets允许你通过多种维度（如项目类型、支持的Minecraft版本、模组加载器）筛选结果。它们需要以URL编码的JSON数组格式提供。

例如，如果你想**搜索Fabric加载器、支持1.20.1版本的模组**，可以这样构建facets参数：

http

```
facets=[["project_type:mod"],["versions:1.20.1"],["categories:fabric"]]
```



搜索响应将包含一个项目列表，每个项目都包含`project_id`、`title`、`slug`、`description`等基本信息，帮助你初步了解模组。

### 📖 获取模组详细信息

通过搜索得到模组ID或Slug后，你可以进一步获取其详细信息。

- **获取项目详情**
  **GET** `https://api.modrinth.com/v2/project/{id|slug}`
  这个请求会返回项目的完整详情，包括描述、徽章、团队ID、下载数、更新日期、主页链接等，这些信息对你的模组管理器非常有用。
- **获取项目的版本列表**
  **GET** `https://api.modrinth.com/v2/project/{id|slug}/version`
  此端点返回该项目所有的版本列表。你还可以通过`loaders`和`game_versions`参数来过滤出符合特定加载器和游戏版本的版本。

### 📦 处理版本与文件

选择一个版本后，你需要关注其文件信息以便下载。

- **获取特定版本信息**
  **GET** `https://api.modrinth.com/v2/version/{id}`
  响应中会包含该版本的详细信息，其中最重要的部分是`files`数组。通常，一个版本的主要文件是`files`数组中的第一个元素。
- **版本文件信息解读**
  版本的文件信息通常包含以下关键字段：
  - `url`: 文件的直接下载链接。
  - `filename`: 文件的名称。
  - `hashes`: 包含文件哈希值的对象，用于校验文件完整性。
  - `primary`: 一个布尔值，标识此文件是否是该版本的主要文件。

### 💾 下载模组文件

获取到文件的直接下载链接后，你有几种方式可以完成下载：

1. **通过文件URL直接下载**
   使用`files`数组中提供的`url`，你可以直接通过HTTP GET请求下载文件。
2. **使用Modrinth的下载端点**
   Modrinth也提供了一个专门的下载端点：
   **GET** `https://api.modrinth.com/v2/version_file/{hash}/download`
   这个端点允许你通过文件的哈希值（SHA1、SHA512等）来下载文件。

### 实践

🔍 Modrinth API 核心概念与准备
开始编码前，有几个关键点需要你留意：

API 基地址：Modrinth API v2的基地址是 https://api.modrinth.com/v2/。

项目标识：每个Modrinth项目（模组）有ID（如 P7dR8mSH）和slug（如 fabric-api）两种标识符。Slug更易读但可能变更，ID则永久不变。

User-Agent 头：Modrinth要求请求必须包含一个能清晰标识你项目的User-Agent头部，例如：MyModpackManager/1.0.0 (your-email@example.com)。这是强制性的，出于礼貌和问题追踪的目的。

依赖库：在Java中，你可以使用如OkHttp作为HTTP客户端，并使用Gson或Jackson来处理JSON数据。你需要在项目的构建配置（如build.gradle）中添加这些依赖。

🛠️ Java代码实现步骤
以下是如何用Java代码一步步实现Modrinth API的调用。

第1步：添加依赖与初始化客户端
首先，确保你的build.gradle中包含必要的库。
```java
import okhttp3.*;
import com.google.gson.*;
import java.io.*;
import java.util.List;

public class ModrinthAPI {
private final OkHttpClient httpClient;
private final Gson gson;
private final String baseUrl = "https://api.modrinth.com/v2/";
private final String userAgent = "YourModpackManager/1.0.0 (your-email@example.com)"; // 请替换为你的信息

    public ModrinthAPI() {
        this.httpClient = new OkHttpClient();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }
}
```
第2步：搜索模组
搜索功能帮助你发现模组。Modrinth的搜索端点支持多种过滤器。

```java
public String searchMods(String query, String gameVersion, String loader) throws IOException {
// 构建查询字符串，包含搜索词、游戏版本和加载器
HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + "search").newBuilder()
.addQueryParameter("query", query)
.addQueryParameter("limit", "10"); // 限制返回结果数量

    // 构建facets进行过滤（例如，过滤特定游戏版本和加载器）
    JsonArray facetsArray = new JsonArray();
    
    JsonArray gameVersionFacet = new JsonArray();
    gameVersionFacet.add("versions:" + gameVersion);
    facetsArray.add(gameVersionFacet);
    
    JsonArray loaderFacet = new JsonArray();
    loaderFacet.add("categories:" + loader); // 例如 "fabric"
    facetsArray.add(loaderFacet);
    
    urlBuilder.addQueryParameter("facets", facetsArray.toString());
    
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
解析搜索结果：上述搜索请求的响应是一个JSON对象，其中 hits 数组包含了匹配的模组列表。每个模组对象都包含 project_id、title、slug 等基本信息。

第3步：获取模组详细信息
获取到模组ID或slug后，可以请求其详细信息。

```java
public String getModInfo(String projectIdOrSlug) throws IOException {
Request request = new Request.Builder()
.url(baseUrl + "project/" + projectIdOrSlug)
.addHeader("User-Agent", userAgent)
.build();

    try (Response response = httpClient.newCall(request).execute()) {
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        return response.body().string();
    }
}
```
这个请求会返回项目的完整详情，包括描述、团队ID、下载数、更新日期、主页链接等。

第4步：获取模组版本列表并过滤
一个模组有多个版本，你需要根据游戏版本和加载器过滤。

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
响应是一个版本列表的JSON数组。每个版本对象包含其 id、name、version_number、files 数组等。

第5步：下载模组文件
获取到特定版本的信息后，就可以下载模组文件了。

从版本信息中获取文件URL：
解析上一步获得的版本JSON，files 数组中的第一个对象通常是主文件。你需要获取其 url 或 filename。

执行下载：

```java
public void downloadModFile(String fileUrl, String localFilePath) throws IOException {
Request request = new Request.Builder()
.url(fileUrl)
.addHeader("User-Agent", userAgent)
.build();

    try (Response response = httpClient.newCall(request).execute()) {
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        
        // 将响应流写入本地文件
        try (InputStream inputStream = response.body().byteStream();
             FileOutputStream outputStream = new FileOutputStream(localFilePath)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }
}
```
版本的文件信息通常包含 hashes 对象，下载完成后你可以计算本地文件的哈希值（如SHA1）与之比对，以校验文件完整性。

💡 一个完整的工作流程示例
假设你想为Minecraft 1.20.1的Fabric加载器查找并下载"Fabric API"模组：

```java
public static void main(String[] args) {
ModrinthAPI api = new ModrinthAPI();
try {
// 1. 搜索模组
String searchResults = api.searchMods("Fabric API", "1.20.1", "fabric");
// 手动或使用Gson解析searchResults，找到"fabric-api"的project_id

        // 2. 获取模组详细信息 (这里使用slug)
        String modInfo = api.getModInfo("fabric-api");
        // 解析modInfo，例如获取描述、主页等
        
        // 3. 获取适用于1.20.1 Fabric的版本列表
        String versions = api.getModVersions("fabric-api", "1.20.1", "fabric");
        // 使用Gson将versions字符串解析为JsonArray
        JsonArray versionsArray = JsonParser.parseString(versions).getAsJsonArray();
        // 获取第一个版本 (通常是最新的)
        JsonObject latestVersion = versionsArray.get(0).getAsJsonObject();
        // 从版本信息中获取文件URL
        JsonObject mainFile = latestVersion.getAsJsonArray("files").get(0).getAsJsonObject();
        String downloadUrl = mainFile.get("url").getAsString();
        String filename = mainFile.get("filename").getAsString();
        
        // 4. 下载文件
        api.downloadModFile(downloadUrl, "你的本地模组路径/" + filename);
        
        System.out.println("模组下载成功！");
        
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```
⚠️ 重要提示与最佳实践
错误处理：务必对每个API请求进行完善的错误处理（如try-catch），应对网络异常、API限制等情况。

异步操作：考虑到网络请求的延迟，建议在后台线程中执行这些API调用，避免阻塞主线程（如Minecraft的游戏渲染线程）。

版本协商：对于模组包，可以尝试实现自动版本协商逻辑，即分析所有模组共同支持的最高Minecraft版本。

依赖处理：模组可能有依赖关系。在下载模组时，检查版本的dependencies字段，并根据你的策略（如required）决定是否自动下载依赖。

希望这份详细的Java实现指南能帮助你顺利地将Modrinth API集成到你的模组管理器中！如果你在具体的解析或实现过程中遇到问题，可以随时再来提问。