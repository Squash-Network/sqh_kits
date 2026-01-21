package com.example.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Complete demonstration page for Hytale Common Assets.
 * 
 * This page demonstrates:
 * - Decorated containers with runic headers
 * - All button variants (Primary, Secondary, Tertiary, Cancel, Small)
 * - Input elements (TextField, NumberField, Slider, Checkbox, Dropdown)
 * - Decorative elements (Separators, Spinner)
 * - Progress bars with animation
 * - Tooltips
 * - Modal popups
 * - Color pickers
 * - UI Events (clicks, value changes)
 * - Dynamic UI updates from Java
 * 
 * Uses native Hytale API: InteractiveCustomUIPage, UICommandBuilder, UIEventBuilder
 * 
 * Path: src/main/java/com/example/pages/TestUIPage.java
 */
public class TestUIPage extends InteractiveCustomUIPage<TestUIPage.TestUIData> {

    private final PlayerRef playerRef;

    // State variables
    private String inputText = "Player";
    private int sliderValue = 75;
    private boolean checkboxValue = true;
    private boolean checkboxValue2 = false;
    private String dropdownValue = "warrior";
    private boolean popupVisible = false;
    private int progressValue = 0;
    private boolean progressAnimating = false;
    private ScheduledFuture<?> progressTask;
    
    // Scheduler for progress animation
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public TestUIPage(@Nonnull PlayerRef playerRef, CustomPageLifetime lifetime) {
        super(playerRef, lifetime, TestUIData.CODEC);
        this.playerRef = playerRef;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder uiCommandBuilder,
                      @Nonnull UIEventBuilder uiEventBuilder,
                      @Nonnull Store<EntityStore> store) {

        // ===== LOAD UI FILE =====
        uiCommandBuilder.append("Pages/TestUIPage.ui");

        // ===== SET INITIAL VALUES =====
        uiCommandBuilder.set("#InputText.Value", this.inputText);
        uiCommandBuilder.set("#TestSlider.Value", this.sliderValue);
        uiCommandBuilder.set("#SliderValue.Text", this.sliderValue + "%");
        uiCommandBuilder.set("#TestCheckbox1.Value", this.checkboxValue);
        uiCommandBuilder.set("#TestCheckbox2.Value", this.checkboxValue2);
        uiCommandBuilder.set("#PopupOverlay.Visible", this.popupVisible);

        // ===== DROPDOWN SETUP =====
        // IMPORTANT: Dropdown entries MUST be set via Java - cannot be defined in .ui files
        ObjectArrayList<DropdownEntryInfo> dropdownOptions = new ObjectArrayList<>();
        dropdownOptions.add(new DropdownEntryInfo(LocalizableString.fromString("Warrior"), "warrior"));
        dropdownOptions.add(new DropdownEntryInfo(LocalizableString.fromString("Mage"), "mage"));
        dropdownOptions.add(new DropdownEntryInfo(LocalizableString.fromString("Archer"), "archer"));
        dropdownOptions.add(new DropdownEntryInfo(LocalizableString.fromString("Rogue"), "rogue"));
        dropdownOptions.add(new DropdownEntryInfo(LocalizableString.fromString("Healer"), "healer"));
        uiCommandBuilder.set("#TestDropdown.Entries", (List<DropdownEntryInfo>) dropdownOptions);
        uiCommandBuilder.set("#TestDropdown.Value", this.dropdownValue);

        // ===== BUTTON EVENT BINDINGS =====
        // Main buttons
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnPrimary", EventData.of("Button", "Primary"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnSecondary", EventData.of("Button", "Secondary"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnTertiary", EventData.of("Button", "Tertiary"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnDestructive", EventData.of("Button", "Destructive"), false);
        
        // Small buttons
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnSmallSec", EventData.of("Button", "SmallSec"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnSmallTer", EventData.of("Button", "SmallTer"), false);
        
        // Action buttons
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnShowMessage", EventData.of("Button", "ShowMessage"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnClose", EventData.of("Button", "Close"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnReset", EventData.of("Button", "Reset"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnExport", EventData.of("Button", "Export"), false);

        // Popup buttons
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnOpenPopup", EventData.of("Button", "OpenPopup"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#PopupConfirmBtn", EventData.of("Button", "PopupConfirm"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#PopupCloseBtn", EventData.of("Button", "PopupClose"), false);

        // Progress bar buttons
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnStartProgress", EventData.of("Button", "StartProgress"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BtnResetProgress", EventData.of("Button", "ResetProgress"), false);

        // ===== INPUT EVENT BINDINGS =====
        // Text field - ValueChanged event with @ prefix to read value
        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.ValueChanged,
            "#InputText",
            EventData.of("@TextInput", "#InputText.Value"),
            false
        );

        // Slider - ValueChanged event
        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.ValueChanged,
            "#TestSlider",
            EventData.of("@SliderValue", "#TestSlider.Value"),
            false
        );

        // Checkbox 1
        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.ValueChanged,
            "#TestCheckbox1",
            EventData.of("@CheckboxValue", "#TestCheckbox1.Value"),
            false
        );

        // Checkbox 2
        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.ValueChanged,
            "#TestCheckbox2",
            EventData.of("@CheckboxValue2", "#TestCheckbox2.Value"),
            false
        );

        // Dropdown - ValueChanged event
        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.ValueChanged,
            "#TestDropdown",
            EventData.of("@DropdownValue", "#TestDropdown.Value"),
            false
        );
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull TestUIData data) {
        super.handleDataEvent(ref, store, data);

        Player player = store.getComponent(ref, Player.getComponentType());

        // ===== HANDLE BUTTON EVENTS =====
        if (data.button != null) {
            switch (data.button) {
                case "Primary" -> {
                    playerRef.sendMessage(Message.raw("§6[UI Demo] §fYou clicked the §ePRIMARY§f button!"));
                }
                case "Secondary" -> {
                    playerRef.sendMessage(Message.raw("§6[UI Demo] §fYou clicked the §bSECONDARY§f button!"));
                }
                case "Tertiary" -> {
                    playerRef.sendMessage(Message.raw("§6[UI Demo] §fYou clicked the §7TERTIARY§f button!"));
                }
                case "Destructive" -> {
                    playerRef.sendMessage(Message.raw("§6[UI Demo] §fYou clicked the §cDESTRUCTIVE§f button!"));
                }
                case "SmallSec" -> {
                    playerRef.sendMessage(Message.raw("§6[UI Demo] §fSmall §bSecondary§f button!"));
                }
                case "SmallTer" -> {
                    playerRef.sendMessage(Message.raw("§6[UI Demo] §fSmall §7Tertiary§f button!"));
                }
                case "ShowMessage" -> {
                    playerRef.sendMessage(Message.raw(
                        "§6[UI Demo] §fHello, §e" + this.inputText + "§f! " +
                        "Slider: §b" + this.sliderValue + "§f, " +
                        "Checkbox: §a" + (this.checkboxValue ? "Yes" : "No") + "§f, " +
                        "Class: §e" + this.dropdownValue
                    ));
                }
                case "Close" -> {
                    playerRef.sendMessage(Message.raw("§6[UI Demo] §fGoodbye!"));
                }
                case "Reset" -> {
                    this.inputText = "Player";
                    this.sliderValue = 50;
                    this.checkboxValue = false;
                    this.dropdownValue = "warrior";
                    playerRef.sendMessage(Message.raw("§6[UI Demo] §eValues reset to defaults!"));
                }
                case "Export" -> {
                    playerRef.sendMessage(Message.raw("§6[UI Demo] §aExporting configuration..."));
                    playerRef.sendMessage(Message.raw("§7Text: §f" + this.inputText));
                    playerRef.sendMessage(Message.raw("§7Slider: §f" + this.sliderValue));
                    playerRef.sendMessage(Message.raw("§7Checkbox: §f" + this.checkboxValue));
                    playerRef.sendMessage(Message.raw("§7Dropdown: §f" + this.dropdownValue));
                }
                case "OpenPopup" -> {
                    popupVisible = true;
                    UICommandBuilder builder = new UICommandBuilder();
                    builder.set("#PopupOverlay.Visible", true);
                    sendUpdate(builder);
                    playerRef.sendMessage(Message.raw("§6[UI Demo] §aPopup opened!"));
                }
                case "PopupConfirm" -> {
                    popupVisible = false;
                    UICommandBuilder builder = new UICommandBuilder();
                    builder.set("#PopupOverlay.Visible", false);
                    sendUpdate(builder);
                    playerRef.sendMessage(Message.raw("§6[UI Demo] §aAction confirmed! §fPopup closed."));
                }
                case "PopupClose" -> {
                    popupVisible = false;
                    UICommandBuilder builder = new UICommandBuilder();
                    builder.set("#PopupOverlay.Visible", false);
                    sendUpdate(builder);
                    playerRef.sendMessage(Message.raw("§6[UI Demo] §7Popup closed."));
                }
                case "StartProgress" -> {
                    if (!progressAnimating) {
                        startProgressAnimation();
                        playerRef.sendMessage(Message.raw("§6[UI Demo] §aStarting progress animation..."));
                    } else {
                        playerRef.sendMessage(Message.raw("§6[UI Demo] §cAnimation already in progress!"));
                    }
                }
                case "ResetProgress" -> {
                    resetProgress();
                    playerRef.sendMessage(Message.raw("§6[UI Demo] §eProgress bar reset to 0%!"));
                }
            }
        }

        // ===== HANDLE INPUT VALUE CHANGES =====
        if (data.textInput != null) {
            this.inputText = data.textInput;
        }

        if (data.sliderValue != null) {
            this.sliderValue = data.sliderValue.intValue();
            // Update slider label dynamically
            UICommandBuilder builder = new UICommandBuilder();
            builder.set("#SliderValue.Text", this.sliderValue + "%");
            sendUpdate(builder);
        }

        if (data.checkboxValue != null) {
            this.checkboxValue = data.checkboxValue;
        }

        if (data.checkboxValue2 != null) {
            this.checkboxValue2 = data.checkboxValue2;
        }

        if (data.dropdownValue != null) {
            this.dropdownValue = data.dropdownValue;
            String className = switch (this.dropdownValue) {
                case "warrior" -> "Warrior";
                case "mage" -> "Mage";
                case "archer" -> "Archer";
                case "rogue" -> "Rogue";
                case "healer" -> "Healer";
                default -> this.dropdownValue;
            };
            playerRef.sendMessage(Message.raw("§6[UI Demo] §fClass selected: §e" + className));
        }
    }

    // ===== PROGRESS BAR ANIMATION =====
    
    private void startProgressAnimation() {
        progressAnimating = true;
        progressValue = 0;

        // Animate from 0 to 100 over ~4 seconds (40ms interval = 100 steps)
        progressTask = scheduler.scheduleAtFixedRate(() -> {
            if (progressValue >= 100) {
                progressAnimating = false;
                if (progressTask != null) {
                    progressTask.cancel(false);
                }
                playerRef.sendMessage(Message.raw("§6[UI Demo] §aAnimation complete! 100%"));
                return;
            }

            progressValue++;
            updateProgressBar();
        }, 0, 40, TimeUnit.MILLISECONDS);
    }

    private void resetProgress() {
        if (progressTask != null) {
            progressTask.cancel(false);
        }
        progressAnimating = false;
        progressValue = 0;
        updateProgressBar();
    }

    private void updateProgressBar() {
        // ProgressBar.Value expects a float from 0.0 to 1.0
        float progressFloat = (float) progressValue / 100.0f;
        UICommandBuilder builder = new UICommandBuilder();
        builder.set("#AnimatedProgressBar.Value", progressFloat);
        builder.set("#ProgressLabel1.Text", progressValue + "%");
        sendUpdate(builder);
    }

    /**
     * Data class for UI events.
     * Each field corresponds to an event that can be sent by the UI.
     * The BuilderCodec serializes/deserializes data between client and server.
     */
    public static class TestUIData {
        // Event keys - must match EventData.of() first parameter
        static final String KEY_BUTTON = "Button";
        static final String KEY_TEXT_INPUT = "@TextInput";
        static final String KEY_SLIDER_VALUE = "@SliderValue";
        static final String KEY_CHECKBOX_VALUE = "@CheckboxValue";
        static final String KEY_CHECKBOX_VALUE_2 = "@CheckboxValue2";
        static final String KEY_DROPDOWN_VALUE = "@DropdownValue";

        // Codec for serialization/deserialization
        public static final BuilderCodec<TestUIData> CODEC = BuilderCodec
            .<TestUIData>builder(TestUIData.class, TestUIData::new)
            .addField(
                new KeyedCodec<>(KEY_BUTTON, Codec.STRING),
                (data, s) -> data.button = s,
                data -> data.button
            )
            .addField(
                new KeyedCodec<>(KEY_TEXT_INPUT, Codec.STRING),
                (data, s) -> data.textInput = s,
                data -> data.textInput
            )
            .addField(
                new KeyedCodec<>(KEY_SLIDER_VALUE, Codec.DOUBLE),
                (data, d) -> data.sliderValue = d,
                data -> data.sliderValue
            )
            .addField(
                new KeyedCodec<>(KEY_CHECKBOX_VALUE, Codec.BOOLEAN),
                (data, b) -> data.checkboxValue = b,
                data -> data.checkboxValue
            )
            .addField(
                new KeyedCodec<>(KEY_CHECKBOX_VALUE_2, Codec.BOOLEAN),
                (data, b) -> data.checkboxValue2 = b,
                data -> data.checkboxValue2
            )
            .addField(
                new KeyedCodec<>(KEY_DROPDOWN_VALUE, Codec.STRING),
                (data, s) -> data.dropdownValue = s,
                data -> data.dropdownValue
            )
            .build();

        // Data fields
        private String button;
        private String textInput;
        private Double sliderValue;
        private Boolean checkboxValue;
        private Boolean checkboxValue2;
        private String dropdownValue;
    }
}
