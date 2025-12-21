package com.leawsic.autoupdate.render.screen;

import com.leawsic.autoupdate.AutoUpdate;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.Objects;

public class UpdateResultScreen extends Screen {
    private final Screen parentScreen;
    private final int updatedModCount;
    private final boolean success;

    public UpdateResultScreen(Text title, Screen parent, int updatedModCount, boolean success) {
        super(title);
        this.parentScreen = parent;
        this.updatedModCount = updatedModCount;
        this.success = success;
    }

    @Override
    protected void init() {
        // 按钮尺寸和间距
        int buttonWidth = 120;
        int buttonHeight = 25;
        int buttonSpacing = 20;
        
        // 计算按钮位置
        int totalWidth = buttonWidth * 2 + buttonSpacing;
        int startX = (this.width - totalWidth) / 2;
        int buttonY = this.height / 2 + 40;

        // 返回到主界面按钮 - 修正为智能返回逻辑
        ButtonWidget backButton = ButtonWidget.builder(
                Text.translatable(AutoUpdate.MOD_ID + ".resultScreen.backButton"),
                button -> this.backToMainMenu()
        ).dimensions(startX, buttonY, buttonWidth, buttonHeight).build();
        this.addDrawableChild(backButton);

        // 退出游戏按钮
        ButtonWidget quitButton = ButtonWidget.builder(
                Text.translatable(AutoUpdate.MOD_ID + ".resultScreen.quitButton"),
                button -> {
                    if (this.client != null) {
                        this.client.scheduleStop();
                    }
                }
        ).dimensions(startX + buttonWidth + buttonSpacing, buttonY, buttonWidth, buttonHeight).build();
        this.addDrawableChild(quitButton);
    }

    private void backToMainMenu() {
        if (this.client != null) {
            if (parentScreen!=null && !(parentScreen instanceof TitleScreen)) {
                this.client.setScreen(new TitleScreen());
            } else {
                this.close();
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 渐变背景
        this.renderBackgroundTexture(context);

        // 图标或装饰
        int centerX = this.width / 2;
        int centerY = this.height / 2 - 60;
        
        // 绘制状态图标
        if (success) {
            // 成功图标 - 绿色对勾
            context.fill(centerX - 30, centerY - 30, centerX + 30, centerY + 30, 0x334CAF50);
            context.fill(centerX - 25, centerY - 25, centerX + 25, centerY + 25, 0x664CAF50);
            context.fill(centerX - 20, centerY - 20, centerX + 20, centerY + 20, 0xFF4CAF50);
            
            // 对勾符号
            context.fill(centerX - 8, centerY, centerX - 4, centerY + 8, 0xFFFFFFFF);
            context.fill(centerX - 4, centerY + 8, centerX + 8, centerY - 8, 0xFFFFFFFF);
        } else {
            // 失败图标 - 红色叉号
            context.fill(centerX - 30, centerY - 30, centerX + 30, centerY + 30, 0x33F44336);
            context.fill(centerX - 25, centerY - 25, centerX + 25, centerY + 25, 0x66F44336);
            context.fill(centerX - 20, centerY - 20, centerX + 20, centerY + 20, 0xFFF44336);
            
            // 叉号符号
            context.fill(centerX - 8, centerY - 8, centerX + 8, centerY + 8, 0xFFFFFFFF);
            context.fill(centerX - 8, centerY + 8, centerX + 8, centerY - 8, 0xFFFFFFFF);
        }

        // 标题
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, centerY + 50, 
                success ? 0xFF4CAF50 : 0xFFF44336);

        // 结果消息
        Text message;
        if (success) {
            message = Text.translatable(AutoUpdate.MOD_ID + ".resultScreen.successMessage", updatedModCount);
        } else {
            message = Text.translatable(AutoUpdate.MOD_ID + ".resultScreen.errorMessage");
        }
        
        context.drawCenteredTextWithShadow(this.textRenderer, message, centerX, centerY + 70,
                success ? 0xFF4CAF50 : 0xFFF44336);

        // 提示文字
        if (success && updatedModCount > 0) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable(AutoUpdate.MOD_ID + ".resultScreen.restartHint"),
                    centerX, centerY + 90, 0xFF9E9E9E);
        }

        // 添加装饰边框
        context.fill(centerX - 200, centerY - 80, centerX + 200, centerY - 78, 0x33FFFFFF);
        context.fill(centerX - 200, centerY + 120, centerX + 200, centerY + 122, 0x33FFFFFF);

        // 添加操作提示
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable(AutoUpdate.MOD_ID + ".resultScreen.actionHint"),
                centerX, this.height / 2 + 70, 0xFF9E9E9E);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        this.client.setScreen(Objects.requireNonNullElseGet(parentScreen, TitleScreen::new));
    }
}