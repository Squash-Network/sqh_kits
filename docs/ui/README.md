# Hytale Common UI - Technical Reference

> **AI-Ready Documentation** for building native Hytale UI panels using Common Assets.

## Table of Contents

1. [Overview](#overview)
2. [File Structure](#file-structure)
3. [Asset Paths](#asset-paths)
4. [Common.ui Templates Reference](#commonui-templates-reference)
5. [UI Elements Syntax](#ui-elements-syntax)
6. [Layout System](#layout-system)
7. [Styling System](#styling-system)
8. [Interactive Components](#interactive-components)
9. [Java Integration](#java-integration)
10. [Complete Examples](#complete-examples)
11. [Known Issues & Workarounds](#known-issues--workarounds)
12. [Quick Reference Cheatsheet](#quick-reference-cheatsheet)

---

## Overview

Hytale uses a custom declarative UI markup language (`.ui` files) combined with Java backend code. The game provides a comprehensive style library in `Common.ui` that **must** be used to maintain visual consistency with Hytale's design language.

### Key Concepts

| Concept              | Description                                                           |
| -------------------- | --------------------------------------------------------------------- |
| **UI Files (`.ui`)** | Declarative markup defining layout, styling, and structure            |
| **Java Pages**       | Server-side classes extending `InteractiveCustomUIPage<T>` for events |
| **Common.ui**        | Game's standard library with styles, templates, and components        |
| **Asset Resolution** | Game auto-resolves `@2x` textures; always reference WITHOUT suffix    |

### Critical Rules

1. **Always import Common.ui**: `$C = "../Common.ui";`
2. **Never use @2x suffix** in texture paths
3. **Dropdown entries MUST be set via Java** - cannot be defined in .ui files
4. **Use element IDs** (`#ElementId`) for Java event binding
5. **Texture paths are relative** to `Common/UI/Custom/`

---

## File Structure

### Required Directory Layout

```
src/main/resources/
├── manifest.json              # Must include "IncludesAssetPack": true
└── Common/
    └── UI/
        └── Custom/
            └── Pages/
                └── YourPage.ui    # Custom UI files go here
```

### manifest.json

```json
{
  "Name": "YourMod",
  "Version": "1.0.0",
  "IncludesAssetPack": true
}
```

---

## Asset Paths

### From Pages/ Directory

When your `.ui` file is in `Pages/`, use relative paths:

```
$C = "../Common.ui";                                    // Import styles
Background: "../Common/ContainerFullPatch.png";         // Texture
Background: (TexturePath: "../Common/ProgressBar.png", Border: 4);
```

### Texture Path Resolution

```
// ✅ CORRECT - No @2x suffix
Background: (TexturePath: "../Common/ContainerFullPatch.png", Border: 20);
TexturePath: "../Common/Spinner.png";

// ❌ INCORRECT - Never use @2x
Background: (TexturePath: "../Common/ContainerFullPatch@2x.png", Border: 20);
```

### Available Texture Assets

| Texture              | Path                                                   | Border | Usage               |
| -------------------- | ------------------------------------------------------ | ------ | ------------------- |
| Container Full       | `../Common/ContainerFullPatch.png`                     | 20     | Main panels, modals |
| Container Patch      | `../Common/ContainerPatch.png`                         | 12     | Secondary panels    |
| Progress Bar BG      | `../Common/ProgressBar.png`                            | 4      | Progress background |
| Progress Bar Fill    | `../Common/ProgressBarFill.png`                        | 4      | Progress fill       |
| Spinner              | `../Common/Spinner.png`                                | -      | Loading animation   |
| Separator Line       | `../Common/ContainerPanelSeparatorFancyLine.png`       | -      | Decorative line     |
| Separator Decoration | `../Common/ContainerPanelSeparatorFancyDecoration.png` | -      | Center ornament     |
| Vertical Separator   | `../Common/ContainerVerticalSeparator.png`             | -      | Column divider      |
| Tooltip Background   | `../Common/TooltipDefaultBackground.png`               | 12-24  | Popups, tooltips    |
| WIP Icon             | `../Common/WIPIcon.png`                                | -      | Placeholder icon    |

---

## Common.ui Templates Reference

### Importing

```
$C = "../Common.ui";
```

### Page Containers

| Template                 | Description                                | Required Children    |
| ------------------------ | ------------------------------------------ | -------------------- |
| `$C.@PageOverlay`        | Semi-transparent fullscreen overlay (root) | Content elements     |
| `$C.@DecoratedContainer` | Container with runic header decoration     | `#Title`, `#Content` |
| `$C.@Container`          | Container without runes                    | `#Title`, `#Content` |
| `$C.@Panel`              | Simple bordered panel                      | Any                  |

#### Usage Example

```
$C.@PageOverlay #MyPage {
  $C.@DecoratedContainer #MainContainer {
    Anchor: (Width: 600, Height: 400);

    #Title {
      $C.@Title {
        @Text = "PAGE TITLE";
      }
    }

    #Content {
      LayoutMode: Top;
      // Content here
    }
  }
}
```

### Buttons

| Template                       | Description                        | Default Height |
| ------------------------------ | ---------------------------------- | -------------- |
| `$C.@TextButton`               | Primary button (gold/orange)       | 44px           |
| `$C.@SecondaryTextButton`      | Secondary button (blue-gray)       | 44px           |
| `$C.@TertiaryTextButton`       | Tertiary button (dark/transparent) | 44px           |
| `$C.@CancelTextButton`         | Destructive/cancel button (red)    | 44px           |
| `$C.@SmallSecondaryTextButton` | Small secondary                    | 32px           |
| `$C.@SmallTertiaryTextButton`  | Small tertiary                     | 32px           |
| `$C.@BackButton`               | Standard back button (bottom-left) | Fixed          |

#### Usage Example

```
$C.@TextButton #BtnSubmit {
  @Text = "SUBMIT";
  @Anchor = (Width: 150, Right: 10);
}

$C.@CancelTextButton #BtnCancel {
  @Text = "CANCEL";
  @Anchor = (Width: 120);
}

// Back button - place at root level
$C.@BackButton {}
```

### Input Elements

| Template          | Description         | Value Type            |
| ----------------- | ------------------- | --------------------- |
| `$C.@TextField`   | Text input field    | String                |
| `$C.@NumberField` | Numeric input field | Number                |
| `$C.@CheckBox`    | Checkbox toggle     | Boolean               |
| `$C.@DropdownBox` | Dropdown selector   | String (set via Java) |

#### Usage Example

```
$C.@TextField #InputName {
  @Anchor = (Height: 38);
  Value: "Default text";
  PlaceholderText: "Enter name...";
}

$C.@NumberField #InputAge {
  @Anchor = (Height: 38);
  Value: 25;
}

$C.@CheckBox #ToggleOption {
  Value: true;
}

$C.@DropdownBox #SelectClass {
  @Anchor = (Width: 280);
  // Entries MUST be set via Java!
}
```

### Slider

```
Slider #VolumeSlider {
  Anchor: (Height: 20, Width: 220);
  Style: $C.@DefaultSliderStyle;
  Min: 0;
  Max: 100;
  Value: 75;
  Step: 5;
}
```

### Color Picker

```
ColorPickerDropdownBox #ColorPicker {
  Anchor: (Width: 120, Height: 32);
  Style: $C.@DefaultColorPickerDropdownBoxStyle;
  Color: #e8a83c;
}
```

### Decorative Elements

| Template                      | Description                               |
| ----------------------------- | ----------------------------------------- |
| `$C.@PanelSeparatorFancy`     | Decorative separator with center ornament |
| `$C.@DefaultScrollbarStyle`   | Scrollbar style for scrollable areas      |
| `$C.@DefaultTextTooltipStyle` | Tooltip style for hover text              |

#### Fancy Separator Usage

```
// Using template
$C.@PanelSeparatorFancy {
  @Anchor = (Bottom: 12);
}

// Manual construction (more reliable)
Group {
  LayoutMode: Left;
  Anchor: (Height: 8);

  Group {
    FlexWeight: 1;
    Background: "../Common/ContainerPanelSeparatorFancyLine.png";
  }

  Group {
    Anchor: (Width: 11);
    Background: "../Common/ContainerPanelSeparatorFancyDecoration.png";
  }

  Group {
    FlexWeight: 1;
    Background: "../Common/ContainerPanelSeparatorFancyLine.png";
  }
}
```

### Styles

| Style                                    | Usage                                      |
| ---------------------------------------- | ------------------------------------------ |
| `$C.@InputBoxBackground`                 | Background style for input-like containers |
| `$C.@DefaultSliderStyle`                 | Complete slider styling                    |
| `$C.@DefaultScrollbarStyle`              | Scrollbar styling                          |
| `$C.@DefaultColorPickerDropdownBoxStyle` | Color picker styling                       |
| `$C.@DefaultTextTooltipStyle`            | Tooltip styling                            |

---

## UI Elements Syntax

### Basic Structure

```
ElementType #ElementId {
  Property: Value;

  ChildElement {
    // Nested content
  }
}
```

### Element Types

| Type                     | Description                     |
| ------------------------ | ------------------------------- |
| `Group`                  | Container for grouping children |
| `Label`                  | Text display element            |
| `Sprite`                 | Animated or static image        |
| `Slider`                 | Value slider control            |
| `ProgressBar`            | Progress indicator              |
| `ColorPickerDropdownBox` | Color selector                  |

### Common Properties

| Property       | Description            | Example                                                 |
| -------------- | ---------------------- | ------------------------------------------------------- |
| `Anchor`       | Size and position      | `(Width: 200, Height: 50, Right: 10)`                   |
| `Background`   | Color or texture       | `(Color: #2b3542)` or `(TexturePath: "...", Border: 4)` |
| `LayoutMode`   | Child layout direction | `Top`, `Left`, `Middle`, `TopScrolling`                 |
| `Padding`      | Inner spacing          | `(Full: 10)` or `(Horizontal: 5, Vertical: 10)`         |
| `FlexWeight`   | Flex grow factor       | `1`                                                     |
| `Visible`      | Visibility toggle      | `true` / `false`                                        |
| `OutlineSize`  | Border width           | `2`                                                     |
| `OutlineColor` | Border color           | `#ffffff(0.3)`                                          |

### Label Element

```
Label #MyLabel {
  Style: (FontSize: 14, TextColor: #96a9be, RenderBold: true);
  Text: "Hello World";
  Anchor: (Height: 20);
}
```

### Label Style Properties

| Property              | Values                          |
| --------------------- | ------------------------------- |
| `FontSize`            | Integer (10-28 typical)         |
| `TextColor`           | `#RRGGBB` or `#RRGGBB(opacity)` |
| `RenderBold`          | `true` / `false`                |
| `RenderUppercase`     | `true` / `false`                |
| `HorizontalAlignment` | `Left`, `Center`, `Right`       |
| `VerticalAlignment`   | `Top`, `Center`, `Bottom`       |
| `LetterSpacing`       | Integer                         |
| `Wrap`                | `true` / `false`                |

### Sprite Element (Animated)

```
Sprite #LoadingSpinner {
  Anchor: (Width: 32, Height: 32);
  TexturePath: "../Common/Spinner.png";
  Frame: (Width: 32, Height: 32, PerRow: 8, Count: 72);
  FramesPerSecond: 30;
}
```

### Progress Bar

```
// Native ProgressBar element
ProgressBar #MyProgress {
  Anchor: (Width: 300, Height: 12);
  BarTexturePath: "../Common/ProgressBarFill.png";
  Value: 0.5;  // 0.0 to 1.0
}

// With background container
Group {
  Anchor: (Width: 300, Height: 12);
  Background: (TexturePath: "../Common/ProgressBar.png", Border: 4);

  ProgressBar #AnimatedProgress {
    Anchor: (Width: 300, Height: 12);
    BarTexturePath: "../Common/ProgressBarFill.png";
    Value: 0;
  }
}
```

### Tooltip

```
Group #HoverableElement {
  Anchor: (Width: 44, Height: 44);
  Background: "../Common/WIPIcon.png";
  TooltipText: "This is a tooltip that appears on hover";
  TextTooltipStyle: $C.@DefaultTextTooltipStyle;
}
```

---

## Layout System

### Layout Modes

| Mode             | Description                   |
| ---------------- | ----------------------------- |
| `Top`            | Stack children top to bottom  |
| `Bottom`         | Stack children bottom to top  |
| `Left`           | Stack children left to right  |
| `Right`          | Stack children right to left  |
| `Middle`         | Center children               |
| `Center`         | Center horizontally           |
| `TopScrolling`   | Vertical scrollable container |
| `LeftCenterWrap` | Horizontal wrap, centered     |

### Anchor System

```
Anchor: (
  Top: 10,        // Margin from top
  Bottom: 10,     // Margin from bottom
  Left: 10,       // Margin from left
  Right: 10,      // Margin from right
  Width: 200,     // Fixed width
  Height: 100,    // Fixed height
  Horizontal: 10, // Left + Right
  Vertical: 10,   // Top + Bottom
  Full: 10        // All sides
);
```

### Flex Layout

```
Group {
  LayoutMode: Left;

  Group {
    FlexWeight: 1;  // Takes remaining space
  }

  Group {
    Anchor: (Width: 100);  // Fixed width
  }

  Group {
    FlexWeight: 2;  // Takes 2x space vs FlexWeight: 1
  }
}
```

### Scrollable Area

```
Group #ScrollArea {
  LayoutMode: TopScrolling;
  ScrollbarStyle: $C.@DefaultScrollbarStyle;
  Anchor: (Height: 400);

  // Scrollable content here
}
```

---

## Styling System

### Custom Label Styles

```
@PageTitleStyle = LabelStyle(
  FontSize: 28,
  RenderBold: true,
  RenderUppercase: true,
  TextColor: #F0E68C,
  HorizontalAlignment: Center,
  LetterSpacing: 2
);

@DescriptionStyle = LabelStyle(
  FontSize: 12,
  TextColor: #8899aa,
  Wrap: true
);

// Usage
Label {
  Style: @PageTitleStyle;
  Text: "MY TITLE";
}
```

### Inline Styles

```
Label {
  Style: (FontSize: 14, TextColor: #96a9be, RenderBold: true);
  Text: "Inline styled text";
}
```

### Color Format

```
TextColor: #F0E68C;           // RGB hex
TextColor: #96a9be;           // RGB hex
Background: #000000(0.82);    // RGB with 82% opacity
Background: (Color: #2b3542); // Color background
OutlineColor: #ffffff(0.3);   // 30% opacity white
```

### Common Hytale Colors

| Color           | Hex       | Usage               |
| --------------- | --------- | ------------------- |
| Gold/Title      | `#F0E68C` | Main titles         |
| Gold/Accent     | `#e8a83c` | Highlights, primary |
| Gold/Header     | `#ccb588` | Section headers     |
| Blue-Gray/Text  | `#96a9be` | Body text           |
| Light Blue      | `#b4c8c9` | Subsections         |
| Dark Gray/Muted | `#8899aa` | Descriptions        |
| Separator       | `#2b3542` | Line separators     |
| Green/Success   | `#7dd87d` | Online, success     |
| Yellow/Warning  | `#d8d87d` | Away, warning       |
| Red/Error       | `#d87d7d` | Offline, error      |
| Blue/Info       | `#5a8dd4` | Info, secondary     |

---

## Interactive Components

### Popup/Modal Pattern

```
// Main page content
$C.@PageOverlay #Root {
  $C.@DecoratedContainer #MainContainer {
    // Main content
  }

  // Popup overlay (initially hidden)
  Group #PopupOverlay {
    Visible: false;
    Background: #000000(0.85);
    LayoutMode: Middle;

    Group #PopupContainer {
      Anchor: (Width: 500);
      LayoutMode: Top;
      Background: (TexturePath: "../Common/TooltipDefaultBackground.png", Border: 24);
      Padding: (Full: 24);

      Label {
        Style: (FontSize: 22, RenderBold: true, TextColor: #e1a738, HorizontalAlignment: Center);
        Text: "POPUP TITLE";
      }

      // Popup content...

      Group {
        LayoutMode: Center;
        Anchor: (Top: 24, Height: 45);

        $C.@TextButton #PopupConfirmBtn {
          @Text = "CONFIRM";
          @Anchor = (Width: 140, Right: 10);
        }

        $C.@CancelTextButton #PopupCloseBtn {
          @Text = "CLOSE";
          @Anchor = (Width: 140, Left: 10);
        }
      }
    }
  }
}
```

### Status Indicators

```
Group {
  LayoutMode: Left;
  Anchor: (Height: 30);

  // Green - Online
  Group {
    Anchor: (Width: 20, Height: 20, Right: 5);
    Background: (Color: #7dd87d);
  }
  Label {
    Style: (FontSize: 12, TextColor: #7dd87d);
    Text: "Online";
    Anchor: (Width: 60, Right: 15);
  }

  // Yellow - Away
  Group {
    Anchor: (Width: 20, Height: 20, Right: 5);
    Background: (Color: #d8d87d);
  }
  Label {
    Style: (FontSize: 12, TextColor: #d8d87d);
    Text: "Away";
    Anchor: (Width: 60, Right: 15);
  }

  // Red - Offline
  Group {
    Anchor: (Width: 20, Height: 20, Right: 5);
    Background: (Color: #d87d7d);
  }
  Label {
    Style: (FontSize: 12, TextColor: #d87d7d);
    Text: "Offline";
    Anchor: (Width: 60);
  }
}
```

---

## Java Integration

### Page Class Structure

```java
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

public class MyPage extends InteractiveCustomUIPage<MyPage.MyPageData> {
    private final PlayerRef playerRef;

    // State variables
    private String inputText = "Default";
    private int sliderValue = 50;
    private boolean checkboxValue = false;
    private String dropdownValue = "option1";
    private boolean popupVisible = false;

    public MyPage(@Nonnull PlayerRef playerRef, CustomPageLifetime lifetime) {
        super(playerRef, lifetime, MyPageData.CODEC);
        this.playerRef = playerRef;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder uiCommandBuilder,
                      @Nonnull UIEventBuilder uiEventBuilder,
                      @Nonnull Store<EntityStore> store) {

        // Load UI file
        uiCommandBuilder.append("Pages/MyPage.ui");

        // Set initial values
        uiCommandBuilder.set("#InputText.Value", this.inputText);
        uiCommandBuilder.set("#TestSlider.Value", this.sliderValue);
        uiCommandBuilder.set("#SliderLabel.Text", this.sliderValue + "%");
        uiCommandBuilder.set("#TestCheckbox.Value", this.checkboxValue);
        uiCommandBuilder.set("#PopupOverlay.Visible", this.popupVisible);

        // Dropdown setup (REQUIRED - cannot be set in .ui)
        ObjectArrayList<DropdownEntryInfo> options = new ObjectArrayList<>();
        options.add(new DropdownEntryInfo(LocalizableString.fromString("Option 1"), "option1"));
        options.add(new DropdownEntryInfo(LocalizableString.fromString("Option 2"), "option2"));
        options.add(new DropdownEntryInfo(LocalizableString.fromString("Option 3"), "option3"));
        uiCommandBuilder.set("#MyDropdown.Entries", (List<DropdownEntryInfo>) options);
        uiCommandBuilder.set("#MyDropdown.Value", this.dropdownValue);

        // Button event bindings
        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#BtnSubmit",
            EventData.of("Button", "Submit"),
            false
        );

        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#BtnOpenPopup",
            EventData.of("Button", "OpenPopup"),
            false
        );

        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#PopupCloseBtn",
            EventData.of("Button", "ClosePopup"),
            false
        );

        // Input event bindings
        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.ValueChanged,
            "#InputText",
            EventData.of("@TextInput", "#InputText.Value"),
            false
        );

        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.ValueChanged,
            "#TestSlider",
            EventData.of("@SliderValue", "#TestSlider.Value"),
            false
        );

        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.ValueChanged,
            "#TestCheckbox",
            EventData.of("@CheckboxValue", "#TestCheckbox.Value"),
            false
        );

        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.ValueChanged,
            "#MyDropdown",
            EventData.of("@DropdownValue", "#MyDropdown.Value"),
            false
        );
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull MyPageData data) {
        super.handleDataEvent(ref, store, data);

        // Handle button clicks
        if (data.button != null) {
            switch (data.button) {
                case "Submit" -> {
                    playerRef.sendMessage(Message.raw("§aSubmitted!"));
                }
                case "OpenPopup" -> {
                    popupVisible = true;
                    UICommandBuilder builder = new UICommandBuilder();
                    builder.set("#PopupOverlay.Visible", true);
                    sendUpdate(builder);
                }
                case "ClosePopup" -> {
                    popupVisible = false;
                    UICommandBuilder builder = new UICommandBuilder();
                    builder.set("#PopupOverlay.Visible", false);
                    sendUpdate(builder);
                }
            }
        }

        // Handle value changes
        if (data.textInput != null) {
            this.inputText = data.textInput;
        }

        if (data.sliderValue != null) {
            this.sliderValue = data.sliderValue.intValue();
            // Update label
            UICommandBuilder builder = new UICommandBuilder();
            builder.set("#SliderLabel.Text", this.sliderValue + "%");
            sendUpdate(builder);
        }

        if (data.checkboxValue != null) {
            this.checkboxValue = data.checkboxValue;
        }

        if (data.dropdownValue != null) {
            this.dropdownValue = data.dropdownValue;
        }
    }

    // Data codec class
    public static class MyPageData {
        public static final BuilderCodec<MyPageData> CODEC = BuilderCodec
            .<MyPageData>builder(MyPageData.class, MyPageData::new)
            .addField(new KeyedCodec<>("Button", Codec.STRING),
                (d, s) -> d.button = s, d -> d.button)
            .addField(new KeyedCodec<>("@TextInput", Codec.STRING),
                (d, s) -> d.textInput = s, d -> d.textInput)
            .addField(new KeyedCodec<>("@SliderValue", Codec.DOUBLE),
                (d, v) -> d.sliderValue = v, d -> d.sliderValue)
            .addField(new KeyedCodec<>("@CheckboxValue", Codec.BOOLEAN),
                (d, b) -> d.checkboxValue = b, d -> d.checkboxValue)
            .addField(new KeyedCodec<>("@DropdownValue", Codec.STRING),
                (d, s) -> d.dropdownValue = s, d -> d.dropdownValue)
            .build();

        private String button;
        private String textInput;
        private Double sliderValue;
        private Boolean checkboxValue;
        private String dropdownValue;
    }
}
```

### Event Binding Types

| Type           | Description               | Use Case                               |
| -------------- | ------------------------- | -------------------------------------- |
| `Activating`   | Element clicked/activated | Buttons                                |
| `ValueChanged` | Value modified            | Inputs, sliders, checkboxes, dropdowns |

### Reading UI Values

```java
// Static value
EventData.of("Button", "Submit")

// Read current UI element value
EventData.of("@InputValue", "#MyInput.Value")

// Multiple values
new EventData()
    .append("Action", "Save")
    .append("@Name", "#NameInput.Value")
    .append("@Age", "#AgeInput.Value")
```

### Dynamic Updates

```java
// Update UI from Java
UICommandBuilder builder = new UICommandBuilder();
builder.set("#MyLabel.Text", "New text");
builder.set("#MyElement.Visible", true);
builder.set("#ProgressBar.Value", 0.5f);
sendUpdate(builder);
```

### Opening a Page

```java
Player player = store.getComponent(ref, Player.getComponentType());
player.getPageManager().openCustomPage(
    player.getReference(),
    player.getWorld().getEntityStore().getStore(),
    new MyPage(player.getPlayerRef(), CustomPageLifetime.UntilClosed)
);
```

---

## Complete Examples

### Minimal Page

**UI File** (`Pages/MinimalPage.ui`):

```
$C = "../Common.ui";

$C.@PageOverlay {
  $C.@DecoratedContainer {
    Anchor: (Width: 400, Height: 300);

    #Title {
      $C.@Title {
        @Text = "MINIMAL PAGE";
      }
    }

    #Content {
      LayoutMode: Top;
      Padding: (Full: 15);

      Label {
        Style: (FontSize: 14, TextColor: #96a9be);
        Text: "Hello World!";
        Anchor: (Height: 30);
      }

      $C.@TextButton #BtnClose {
        @Text = "CLOSE";
        @Anchor = (Width: 120);
      }
    }
  }
}

$C.@BackButton {}
```

### Form Page

```
$C = "../Common.ui";

@LabelStyle = LabelStyle(FontSize: 13, TextColor: #b4c8c9, RenderBold: true);
@DescStyle = LabelStyle(FontSize: 12, TextColor: #8899aa);

$C.@PageOverlay #FormPage {
  $C.@DecoratedContainer #MainContainer {
    Anchor: (Width: 500, Height: 450);

    #Title {
      $C.@Title {
        @Text = "USER SETTINGS";
      }
    }

    #Content {
      LayoutMode: Top;
      Padding: (Full: 20);

      // Name field
      Group {
        LayoutMode: Top;
        Anchor: (Bottom: 15);

        Label {
          Style: @LabelStyle;
          Text: "Username:";
          Anchor: (Height: 20, Bottom: 5);
        }

        $C.@TextField #InputUsername {
          @Anchor = (Height: 38);
          PlaceholderText: "Enter username...";
        }
      }

      // Volume slider
      Group {
        LayoutMode: Top;
        Anchor: (Bottom: 15);

        Label {
          Style: @LabelStyle;
          Text: "Volume:";
          Anchor: (Height: 20, Bottom: 5);
        }

        Group {
          LayoutMode: Left;
          Anchor: (Height: 30);

          Slider #VolumeSlider {
            Anchor: (Height: 20, Width: 300, Right: 15);
            Style: $C.@DefaultSliderStyle;
            Min: 0;
            Max: 100;
            Value: 80;
            Step: 5;
          }

          Label #VolumeLabel {
            Style: @DescStyle;
            Text: "80%";
            Anchor: (Width: 50);
          }
        }
      }

      // Notifications toggle
      Group {
        LayoutMode: Left;
        Anchor: (Height: 40, Bottom: 15);

        $C.@CheckBox #NotificationsToggle {
          Value: true;
        }

        Label {
          Style: @DescStyle;
          Text: "Enable notifications";
          Anchor: (Left: 10);
        }
      }

      // Theme dropdown
      Group {
        LayoutMode: Top;
        Anchor: (Bottom: 20);

        Label {
          Style: @LabelStyle;
          Text: "Theme:";
          Anchor: (Height: 20, Bottom: 5);
        }

        $C.@DropdownBox #ThemeDropdown {
          @Anchor = (Width: 200);
        }
      }

      // Separator
      Group {
        Anchor: (Height: 1, Bottom: 20);
        Background: (Color: #2b3542);
      }

      // Action buttons
      Group {
        LayoutMode: Left;
        Anchor: (Height: 50);

        $C.@TextButton #BtnSave {
          @Text = "SAVE";
          @Anchor = (Width: 120, Right: 10);
        }

        $C.@CancelTextButton #BtnCancel {
          @Text = "CANCEL";
          @Anchor = (Width: 120);
        }
      }
    }
  }
}

$C.@BackButton {}
```

---

## Known Issues & Workarounds

### Square Button Templates (DO NOT USE)

These templates have bugs and should **NOT** be used:

- `$C.@Button`
- `$C.@SecondaryButton`
- `$C.@TertiaryButton`
- `$C.@CancelButton`

**Workaround**: Use TextButton variants or manual background:

```
// Manual square button (no click events)
Group #ManualButton {
  Anchor: (Width: 44, Height: 44);
  Background: (TexturePath: "../Common/Buttons/Primary.png", Border: 8);
}
```

### PanelSeparatorFancy Rendering Issues

May not render in all contexts.

**Workaround**: Build manually (see Decorative Elements section).

### ContentSeparator Template

May not render.

**Workaround**: Use color directly:

```
Group {
  Anchor: (Height: 1);
  Background: (Color: #2b3542);
}
```

### DefaultSpinner Template

May not work as template.

**Workaround**: Use Sprite directly:

```
Sprite #Spinner {
  Anchor: (Width: 32, Height: 32);
  TexturePath: "../Common/Spinner.png";
  Frame: (Width: 32, Height: 32, PerRow: 8, Count: 72);
  FramesPerSecond: 30;
}
```

---

## Quick Reference Cheatsheet

### Page Template

```
$C = "../Common.ui";

// Custom styles
@TitleStyle = LabelStyle(FontSize: 16, RenderBold: true, TextColor: #ccb588);
@TextStyle = LabelStyle(FontSize: 12, TextColor: #8899aa);

$C.@PageOverlay #Root {
  $C.@DecoratedContainer #Main {
    Anchor: (Width: 600, Height: 400);

    #Title {
      $C.@Title { @Text = "TITLE"; }
    }

    #Content {
      LayoutMode: Top;
      // Content here
    }
  }
}

$C.@BackButton {}
```

### Common Patterns

```
// Section header
Label {
  Style: (FontSize: 16, RenderBold: true, RenderUppercase: true, TextColor: #ccb588);
  Text: "SECTION NAME";
  Anchor: (Height: 25, Bottom: 8);
}

// Simple separator
Group {
  Anchor: (Height: 1, Bottom: 15);
  Background: (Color: #2b3542);
}

// Horizontal button row
Group {
  LayoutMode: Left;
  Anchor: (Height: 50);

  $C.@TextButton #Btn1 {
    @Text = "ACTION 1";
    @Anchor = (Width: 140, Right: 10);
  }

  $C.@SecondaryTextButton #Btn2 {
    @Text = "ACTION 2";
    @Anchor = (Width: 140);
  }
}

// Labeled input
Group {
  LayoutMode: Top;
  Anchor: (Bottom: 15);

  Label {
    Style: (FontSize: 13, RenderBold: true, TextColor: #b4c8c9);
    Text: "Label:";
    Anchor: (Height: 20, Bottom: 5);
  }

  $C.@TextField #Input {
    @Anchor = (Height: 38);
  }
}
```

### Java Codec Template

```java
public static class PageData {
    public static final BuilderCodec<PageData> CODEC = BuilderCodec
        .<PageData>builder(PageData.class, PageData::new)
        .addField(new KeyedCodec<>("Button", Codec.STRING),
            (d, s) -> d.button = s, d -> d.button)
        .addField(new KeyedCodec<>("@StringValue", Codec.STRING),
            (d, s) -> d.stringValue = s, d -> d.stringValue)
        .addField(new KeyedCodec<>("@NumberValue", Codec.DOUBLE),
            (d, v) -> d.numberValue = v, d -> d.numberValue)
        .addField(new KeyedCodec<>("@BoolValue", Codec.BOOLEAN),
            (d, b) -> d.boolValue = b, d -> d.boolValue)
        .build();

    private String button;
    private String stringValue;
    private Double numberValue;
    private Boolean boolValue;
}
```

---

## Debugging

Enable **Diagnostic Mode** in Hytale client settings (General tab) for detailed UI error messages:

- Line numbers in `.ui` files
- Unknown node types
- Missing textures
- Template resolution errors

---

_Documentation synced with TestUIPage implementation - January 2026_
