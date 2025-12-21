package com.leawsic.autoupdate.render.screen;

import com.leawsic.autoupdate.AutoUpdate;
import com.leawsic.autoupdate.data.mod.ModInfo;
import com.leawsic.autoupdate.render.ToastManager;
import com.leawsic.autoupdate.render.widget.ModUpdateListWidget;
import com.leawsic.autoupdate.tool.ModDownloadManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModUpdateSelectionScreen extends Screen {
    private final Screen parentScreen;
    private final List<ModInfo> modsToUpdate;
    private ModUpdateListWidget modListWidget;
    private ButtonWidget updateButton;
    private ButtonWidget selectAllButton;
    private ButtonWidget deselectAllButton;
    private Set<ModInfo> savedSelectedMods; // 保存选择状态
    private int listTop; // 列表顶部位置
    private int listBottom; // 列表底部位置

    public ModUpdateSelectionScreen(Text title, Screen parent, List<ModInfo> modsToUpdate) {
        super(title);
        this.parentScreen = parent;
        this.modsToUpdate = modsToUpdate;
        this.savedSelectedMods = new HashSet<>(modsToUpdate); // 默认全选
        this.listTop = 60; // 默认值
        this.listBottom = this.height - 80; // 默认值
    }

    @Override
    protected void init() {
        super.init();
    
        // 计算最小安全边距
        int minMargin = 10;
        int buttonWidth = 90;
        int buttonHeight = 25;
        int buttonSpacing = 10;
        
        // 动态计算布局参数
        this.listTop = Math.max(60, this.height / 6); // 动态顶部间距
        this.listBottom = Math.min(this.height - 80, this.height - this.height / 6); // 动态底部间距
        int listHeight = Math.max(100, listBottom - listTop); // 确保最小高度
        int listWidth = Math.max(200, this.width - 40); // 确保最小宽度
        
        // 创建模组列表Widget - 使用动态布局
        this.modListWidget = new ModUpdateListWidget(this.client, listWidth, listHeight, listTop, listBottom, 40, modsToUpdate);
        this.modListWidget.setSelectedMods(savedSelectedMods); // 恢复选择状态
        
        // 设置选择状态变化回调
        this.modListWidget.setOnSelectionChanged(selectedCount -> {
            updateButtonText();
            // 保存选择状态
            this.savedSelectedMods = new HashSet<>(modListWidget.getSelectedMods());
        });
        
        this.addSelectableChild(this.modListWidget);
    
        // 更新按钮 - 动态宽度和位置
        int updateButtonWidth = Math.min(240, this.width - 40);
        int updateButtonX = (this.width - updateButtonWidth) / 2;
        this.updateButton = ButtonWidget.builder(
                Text.translatable(AutoUpdate.MOD_ID + ".updateScreen.updateButton", modListWidget.getSelectedCount(),
                        modsToUpdate.size()),
                button -> startUpdate()
        ).dimensions(updateButtonX, this.height - 50, updateButtonWidth, buttonHeight).build();
        this.addDrawableChild(this.updateButton);
    
        // 全选按钮 - 动态位置
        int leftButtonX = minMargin;
        this.selectAllButton = ButtonWidget.builder(
                Text.translatable(AutoUpdate.MOD_ID + ".updateScreen.selectAll"),
                button -> {
                    modListWidget.selectAll();
                    updateButtonText();
                    this.savedSelectedMods = new HashSet<>(modListWidget.getSelectedMods());
                }
        ).dimensions(leftButtonX, 40, buttonWidth, buttonHeight).build();
        this.addDrawableChild(this.selectAllButton);
    
        // 取消全选按钮 - 动态位置
        int deselectButtonX = leftButtonX + buttonWidth + buttonSpacing;
        this.deselectAllButton = ButtonWidget.builder(
                Text.translatable(AutoUpdate.MOD_ID + ".updateScreen.deselectAll"),
                button -> {
                    modListWidget.deselectAll();
                    updateButtonText();
                    this.savedSelectedMods = new HashSet<>(modListWidget.getSelectedMods());
                }
        ).dimensions(deselectButtonX, 40, buttonWidth, buttonHeight).build();
        this.addDrawableChild(this.deselectAllButton);
    
        // 返回按钮 - 动态位置
        int backButtonX = Math.max(this.width - buttonWidth - minMargin, deselectButtonX + buttonWidth + buttonSpacing);
        ButtonWidget backButton = ButtonWidget.builder(
                Text.translatable(AutoUpdate.MOD_ID + ".updateScreen.backButton"),
                button -> this.close()
        ).dimensions(backButtonX, 40, buttonWidth, buttonHeight).build();
        this.addDrawableChild(backButton);
    
        updateButtonText();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 渐变背景
        this.renderBackgroundTexture(context);

        // 动态计算文本位置
        int titleY = Math.max(15, this.height / 20);
        int descriptionY = titleY + 15;
        int statsY = Math.max(this.height - 80, this.height - this.height / 10);
        
        // 标题 - 动态位置
        context.drawCenteredTextWithShadow(this.textRenderer, 
                Text.translatable(AutoUpdate.MOD_ID + ".updateScreen.title", modsToUpdate.size()),
                this.width / 2, titleY, 0xFFFFFF);

        // 说明文字 - 动态位置和自适应文本
        String description = !modsToUpdate.isEmpty() ?
                String.format("已发现 %d 个模组需要更新，请选择要更新的模组", modsToUpdate.size()) :
                "没有发现需要更新的模组";
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal(description),
                this.width / 2, descriptionY, 0xCCCCCC);

        // 选择统计信息 - 动态位置
        int selectedCount = modListWidget.getSelectedCount();
        String stats = String.format("已选择: %d/%d", selectedCount, modsToUpdate.size());
        int statsX = Math.max(this.width - 150, this.width / 2 + 50);
        context.drawTextWithShadow(this.textRenderer,
                Text.literal(stats),
                statsX, statsY, 
                selectedCount > 0 ? 0xFF4CAF50 : 0xFF9E9E9E);

        // 渲染模组列表
        this.modListWidget.render(context, mouseX, mouseY, delta);

        // 添加边框和装饰 - 动态位置
        int decorationMargin = Math.min(15, this.width / 20);
        context.fill(decorationMargin, listTop - 5, this.width - decorationMargin, listTop, 0x33FFFFFF); // 顶部装饰线
        context.fill(decorationMargin, listBottom + 5, this.width - decorationMargin, listBottom + 10, 0x33FFFFFF); // 底部装饰线

        super.render(context, mouseX, mouseY, delta);
    }

    private void updateButtonText() {
        int selectedCount = modListWidget.getSelectedCount();
        this.updateButton.setMessage(Text.translatable(
                AutoUpdate.MOD_ID + ".updateScreen.updateButton", 
                selectedCount, modsToUpdate.size()
        ));
        this.updateButton.active = selectedCount > 0;
        
        // 根据选择状态改变按钮颜色
        if (selectedCount > 0) {
            this.updateButton.setAlpha(1.0f);
        } else {
            this.updateButton.setAlpha(0.6f);
        }
    }

    private void startUpdate() {
        List<ModInfo> selectedMods = modListWidget.getSelectedMods();
        if (selectedMods.isEmpty()) {
            return;
        }
    
        // 禁用按钮防止重复点击
        this.updateButton.active = false;
        this.selectAllButton.active = false;
        this.deselectAllButton.active = false;
        
        // 更新按钮文本为"正在更新..."
        this.updateButton.setMessage(Text.translatable(AutoUpdate.MOD_ID + ".updateScreen.updating"));
        
        // 在开始下载前显示Toast提示
        this.client.getToastManager().add(ToastManager.getToast(this.client, 
                AutoUpdate.MOD_ID + ".download.autoDownloadStarted"));
    
        // 开始下载选中的模组
        CompletableFuture.supplyAsync(() -> {
            ModDownloadManager downloadManager = new ModDownloadManager();
            return downloadManager.downloadMissingMods(selectedMods);
        }).thenAccept(downloadFuture -> downloadFuture.thenAccept(downloadResult -> this.client.execute(() -> {
            if (downloadResult.overallSuccess() && downloadResult.successCount() > 0) {
                // 下载成功且有模组被下载，显示成功消息并返回
                this.client.setScreen(new UpdateResultScreen(
                        Text.translatable(AutoUpdate.MOD_ID + ".resultScreen.title"),
                        this.parentScreen,
                        downloadResult.successCount(),
                        true
                ));

                // 显示下载完成Toast
                this.client.getToastManager().add(ToastManager.getToastWithArgs(this.client,
                        AutoUpdate.MOD_ID + ".download.autoDownloadCompleted", downloadResult.successCount()));
            } else if (downloadResult.successCount() == 0) {
                // 没有模组被成功下载，只显示Toast提示
                this.client.getToastManager().add(ToastManager.getToast(this.client,
                        AutoUpdate.MOD_ID + ".download.noModsDownloaded"));

                // 返回上级界面
                this.client.setScreen(this.parentScreen);
            } else {
                // 下载失败，显示错误消息
                this.client.setScreen(new UpdateResultScreen(
                        Text.translatable(AutoUpdate.MOD_ID + ".resultScreen.title"),
                        this.parentScreen,
                        downloadResult.successCount(),
                        false
                ));
            }
        })));
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parentScreen);
        }
    }
}