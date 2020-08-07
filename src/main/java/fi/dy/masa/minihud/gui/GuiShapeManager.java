package fi.dy.masa.minihud.gui;

import javax.annotation.Nullable;
import org.lwjgl.input.Keyboard;
import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.gui.BaseScreen;
import fi.dy.masa.malilib.gui.BaseListScreen;
import fi.dy.masa.malilib.gui.button.BaseButton;
import fi.dy.masa.malilib.gui.button.GenericButton;
import fi.dy.masa.malilib.gui.button.ButtonActionListener;
import fi.dy.masa.malilib.gui.config.ConfigTab;
import fi.dy.masa.malilib.gui.interfaces.ISelectionListener;
import fi.dy.masa.malilib.gui.widget.WidgetDropDownList;
import fi.dy.masa.malilib.message.MessageType;
import fi.dy.masa.malilib.message.MessageUtils;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.minihud.gui.widgets.WidgetListShapes;
import fi.dy.masa.minihud.gui.widgets.WidgetShapeEntry;
import fi.dy.masa.minihud.renderer.shapes.ShapeBase;
import fi.dy.masa.minihud.renderer.shapes.ShapeManager;
import fi.dy.masa.minihud.renderer.shapes.ShapeType;

public class GuiShapeManager extends BaseListScreen<ShapeBase, WidgetShapeEntry, WidgetListShapes>
                             implements ISelectionListener<ShapeBase>
{
    protected final WidgetDropDownList<ShapeType> widgetDropDown;

    public GuiShapeManager()
    {
        super(10, 64);

        this.title = StringUtils.translate("minihud.gui.title.shape_manager");

        // The position will get updated later
        this.widgetDropDown = new WidgetDropDownList<>(0, 0, 160, 20, 200, 10, ImmutableList.copyOf(ShapeType.values()), ShapeType::getDisplayName);
        this.widgetDropDown.setZLevel((int) this.zLevel + 2);
    }

    @Override
    protected int getListWidth()
    {
        return this.width - 20;
    }

    @Override
    protected int getListHeight()
    {
        return this.height - this.getListY() - 6;
    }

    @Override
    public void initGui()
    {
        ConfigScreen.tab = ConfigScreen.SHAPES;

        super.initGui();

        this.clearWidgets();
        this.clearButtons();
        this.createTabButtons();

        Keyboard.enableRepeatEvents(true);
    }

    protected void createTabButtons()
    {
        int x = 10;
        int y = 26;
        int rows = 1;

        for (ConfigTab tab : ConfigScreen.TABS)
        {
            int width = this.getStringWidth(tab.getDisplayName()) + 10;

            if (x >= this.width - width - 10)
            {
                x = 10;
                y += 22;
                rows++;
            }

            x += this.createTabButton(x, y, width, tab);
        }

        this.updateListPosition(this.getListX(), 68 + (rows - 1) * 22);
        //this.reCreateListWidget();

        y += 24;

        x = this.width - 10;
        x -= this.addButton(x, y, ButtonListener.Type.ADD_SHAPE);

        this.widgetDropDown.setPosition(x - this.widgetDropDown.getWidth() - 4, y);

        this.addWidget(this.widgetDropDown);
    }

    protected int createTabButton(int x, int y, int width, ConfigTab tab)
    {
        GenericButton button = new GenericButton(x, y, width, 20, tab.getDisplayName());
        button.setEnabled(ConfigScreen.tab != tab);
        this.addButton(button, new ButtonListenerTab(tab));

        return button.getWidth() + 2;
    }

    protected int addButton(int x, int y, ButtonListener.Type type)
    {
        GenericButton button = new GenericButton(x, y, -1, true, type.getDisplayName());
        this.addButton(button, new ButtonListener(ButtonListener.Type.ADD_SHAPE, this));
        return button.getWidth();
    }

    @Override
    public void onSelectionChange(@Nullable ShapeBase entry)
    {
        ShapeBase old = ShapeManager.INSTANCE.getSelectedShape();
        ShapeManager.INSTANCE.setSelectedShape(old == entry ? null : entry);
    }

    @Override
    protected WidgetListShapes createListWidget(int listX, int listY)
    {
        return new WidgetListShapes(listX, listY, this.getListWidth(), this.getListHeight(), this.zLevel, this);
    }

    private static class ButtonListener implements ButtonActionListener
    {
        private final Type type;
        private final GuiShapeManager gui;

        public ButtonListener(Type type, GuiShapeManager gui)
        {
            this.type = type;
            this.gui = gui;
        }

        @Override
        public void actionPerformedWithButton(BaseButton button, int mouseButton)
        {
            if (this.type == Type.ADD_SHAPE)
            {
                ShapeType type = this.gui.widgetDropDown.getSelectedEntry();

                if (type != null)
                {
                    ShapeManager.INSTANCE.addShape(type.createShape());
                    this.gui.getListWidget().refreshEntries();
                }
                else
                {
                    MessageUtils.showGuiMessage(MessageType.ERROR, "minihud.message.error.shapes.select_shape_from_dropdown");
                }
            }
        }

        public enum Type
        {
            ADD_SHAPE   ("minihud.gui.button.add_shape");

            private final String translationKey;

            Type(String translationKey)
            {
                this.translationKey = translationKey;
            }

            public String getDisplayName()
            {
                return StringUtils.translate(this.translationKey);
            }
        }
    }

    public static class ButtonListenerTab implements ButtonActionListener
    {
        private final ConfigTab tab;

        public ButtonListenerTab(ConfigTab tab)
        {
            this.tab = tab;
        }

        @Override
        public void actionPerformedWithButton(BaseButton button, int mouseButton)
        {
            ConfigScreen.tab = this.tab;
            BaseScreen.openGui(new ConfigScreen());
        }
    }
}
