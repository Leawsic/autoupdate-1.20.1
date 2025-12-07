# AutoUpdate 

AutoUpdate 是一个为 Minecraft 1.20.1 设计的 Fabric 
模组，它允许根据提供的模组列表自动判断是否需要更新模组。通过这个模组，服务器所有者或整合包制作者可以轻松分发更新的模组列表，客户端可以使用这些列表自动同步他们的模组。

## 安装方法

1. 下载最新版的 AutoUpdate 模组
2. 将模组文件放入`mods`文件夹
3. 使用`Fabric 1.20.1`启动 Minecraft
4. 首次启动时模组会自动创建其配置目录于版本目录下的`/autoupdate/`

## 使用说明
### 进入模组界面
在游戏主界面点击`U`字按钮(默认、可配置)
### 主界面选项

1. **检查更新**: 比较本地模组与远程模组列表
2. **导出模组列表**: 创建包含当前已安装模组的 JSON 文件

### 导出模组列表

要导出当前模组列表：
1. 在主界面点击"从本地生成模组列表"
2. 输入模组列表的版本
3. 点击"导出"
4. 模组列表将以 JSON 文件形式保存在版本目录下的`/autoupdate/`

### 配置选项

模组会在版本目录下`/autoupdate/config.json`创建配置文件，包含以下选项：

- `mod_list_url`: 远程模组列表 JSON 文件的 URL
- `replace_realms_button`: 是否替换 Realms 按钮（默认：false）
- `auto_download_missing_mod`: 是否自动下载缺失的模组（默认：false）
- `save_mods_list_to_local`: 是否保存模组列表至本地（默认：false）(TODO)

### 从源码构建

1. 克隆仓库
2. 运行 `./gradlew build` 编译
3. 构建的 jar 文件位于 `build/libs/` 目录中

## 贡献

欢迎贡献！请随时提交问题或拉取请求。

## 许可证

此模组采用 CC0-1.0 许可证。
