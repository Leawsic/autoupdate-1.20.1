package com.leawsic.autoupdate.render.widget;

import com.leawsic.autoupdate.data.mod.ModInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ModUpdateListWidget extends AlwaysSelectedEntryListWidget<ModUpdateListWidget.ModEntry> {
    private final List<ModInfo> modInfoList;
    private final Set<ModInfo> selectedMods;
    private Consumer<Integer> onSelectionChanged; // 选择状态变化回调

    public ModUpdateListWidget(MinecraftClient minecraftClient, int width, int height, int top, int bottom, int itemHeight, List<ModInfo> modInfoList) {
        super(minecraftClient, width, height, top, bottom, itemHeight);
        this.modInfoList = modInfoList;
        this.selectedMods = new HashSet<>(modInfoList); // 默认全选
        this.refreshEntries();
    }

    /**
     * 设置选择状态变化回调
     */
    public void setOnSelectionChanged(Consumer<Integer> callback) {
        this.onSelectionChanged = callback;
    }

    /**
     * 触发选择状态变化回调
     */
    private void notifySelectionChanged() {
        if (onSelectionChanged != null) {
            onSelectionChanged.accept(selectedMods.size());
        }
    }

    public void refreshEntries() {
        this.clearEntries();
        for (ModInfo modInfo : modInfoList) {
            this.addEntry(new ModEntry(modInfo, selectedMods.contains(modInfo)));
        }
    }

    @Override
    public int getRowWidth() {
        return this.width - 20;
    }

    @Override
    protected int getScrollbarPositionX() {
        return this.width - 6;
    }

    /**
     * 获取用户选择的模组列表
     */
    public List<ModInfo> getSelectedMods() {
        return new ArrayList<>(selectedMods);
    }

    /**
     * 获取所有模组列表
     */
    public List<ModInfo> getAllMods() {
        return new ArrayList<>(modInfoList);
    }

    /**
     * 选择所有模组
     */
    public void selectAll() {
        selectedMods.clear();
        selectedMods.addAll(modInfoList);
        refreshEntries();
        notifySelectionChanged(); // 通知选择状态变化
    }

    /**
     * 取消选择所有模组
     */
    public void deselectAll() {
        selectedMods.clear();
        refreshEntries();
        notifySelectionChanged(); // 通知选择状态变化
    }

    /**
     * 获取选择的模组数量
     */
    public int getSelectedCount() {
        return selectedMods.size();
    }

    /**
     * 设置选择的模组列表
     */
    public void setSelectedMods(Set<ModInfo> selectedMods) {
        this.selectedMods.clear();
        this.selectedMods.addAll(selectedMods);
        refreshEntries();
        notifySelectionChanged();
    }

    public class ModEntry extends Entry<ModEntry> {
        private final ModInfo modInfo;
        private final Text modName;
        private final Text modVersion;
        private boolean selected;
        private int entryX; // 存储条目的x坐标
        private int entryY; // 存储条目的y坐标
        private boolean hovered; // 鼠标悬停状态
    
        public ModEntry(ModInfo modInfo, boolean selected) {
            this.modInfo = modInfo;
            this.selected = selected;
            this.modName = Text.literal(modInfo.getName());
            this.modVersion = Text.literal(modInfo.getVersion());
            this.entryX = 0;
            this.entryY = 0;
            this.hovered = false;
        }

        public ModInfo getModInfo() {
            return modInfo;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            if (selected) {
                selectedMods.add(modInfo);
            } else {
                selectedMods.remove(modInfo);
            }
            notifySelectionChanged(); // 通知选择状态变化
        }

        @Override
        public Text getNarration() {
            return Text.literal("Mod " + modInfo.getName() + " Version " + modInfo.getVersion() + (selected ? " Selected" : " Not selected"));
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) { // 左键点击
                // 点击条目任何地方都会切换选择状态
                setSelected(!selected);
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            // 更新条目的坐标和悬停状态
            this.entryX = x;
            this.entryY = y;
            this.hovered = hovered;
            
            TextRenderer textRenderer = client.textRenderer;
    
            // 背景高亮 - 更美观的悬停和选中效果
            if (this.hovered) {
                context.fill(x - 2, y - 2, x + entryWidth - 8, y + entryHeight + 2, 0x22FFFFFF);
            }
            if (this.isFocused()) {
                context.fill(x - 2, y - 2, x + entryWidth - 8, y + entryHeight + 2, 0x33FFFFFF);
            }
    
            // 复选框 - 更美观的现代化设计
            int checkboxX = x + 8;
            int checkboxY = y + 8;
            int checkboxSize = 16; // 稍微增大复选框
    
            // 复选框边框 - 圆角效果和渐变颜色
            int borderColor = selected ? 0xFF4CAF50 : (hovered ? 0xFF64B5F6 : 0xFFCCCCCC);
    
            // 绘制圆角边框（使用填充矩形模拟圆角）
            context.fill(checkboxX, checkboxY, checkboxX + checkboxSize, checkboxY + checkboxSize, borderColor);
            
            // 复选框内部背景 - 渐变效果
            int innerColor = selected ? 0xFF4CAF50 : (hovered ? 0xFFF5F5F5 : 0xFFFAFAFA);
            context.fill(checkboxX + 1, checkboxY + 1, checkboxX + checkboxSize - 1, checkboxY + checkboxSize - 1, innerColor);
            
            // 选中状态的对勾 - 更美观的设计
            if (selected) {
                // 绘制对勾符号
                int checkColor = 0xFFFFFFFF;
                // 对勾的第一部分（左下到右上）
                for (int i = 0; i < 3; i++) {
                    context.fill(checkboxX + 4 + i, checkboxY + 8, checkboxX + 5 + i, checkboxY + 9, checkColor);
                }
                // 对勾的第二部分（右上到右下）
                for (int i = 0; i < 5; i++) {
                    context.fill(checkboxX + 6 + i, checkboxY + 9 - i, checkboxX + 7 + i, checkboxY + 10 - i, checkColor);
                }
                
                // 添加选中状态的发光效果
                context.fill(checkboxX - 1, checkboxY - 1, checkboxX + checkboxSize + 1, checkboxY + checkboxSize + 1, 0x224CAF50);
            }
    
            // 模组信息 - 更好的排版和颜色
            int textX = x + 35; // 增加与复选框的间距
            context.drawTextWithShadow(textRenderer, modName, textX, y + 4, selected ? 0xFF4CAF50 : 0xFFFFFFFF);
            context.drawTextWithShadow(textRenderer, modVersion, textX, y + 16, selected ? 0xFF81C784 : 0xFFBDBDBD);
            context.drawTextWithShadow(textRenderer, Text.literal("ID: " + modInfo.getId()), textX, y + 28, 0xFF9E9E9E);
            
            // 状态指示器 - 更美观的圆形设计
            int statusX = x + entryWidth - 25;
            int statusColor = selected ? 0xFF4CAF50 : 0xFFFF5722;
            // 绘制圆形状态指示器
            for (int i = 0; i < 6; i++) {
                context.fill(statusX + i, y + 10, statusX + i + 1, y + 16, statusColor);
            }
            for (int i = 1; i < 5; i++) {
                context.fill(statusX + i - 1, y + 10 + i, statusX + i + 1, y + 11 + i, statusColor);
            }
            
            // 添加分割线 - 更细更美观
            if (index < modInfoList.size() - 1) {
                context.fill(x + 10, y + entryHeight - 1, x + entryWidth - 20, y + entryHeight, 0x22FFFFFF);
            }
        }

        public ModInfo getSelectedModInfo() {
            ModEntry selected = getSelectedOrNull();
            return selected != null ? selected.getModInfo() : null;
        }

        public int getUpdateModCount() {
            return modInfoList.size();
        }
    }
}