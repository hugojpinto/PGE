# PGE: Pinto's Game Engine

A small 2D game engine in plain Java, written to be read.

- **No dependencies.** Only the JDK.
- **No magic.** The engine draws every pixel itself into an `int[]` array. The JDK only
  opens a window, reports key presses, and shows the finished picture once per frame.
- **Pico-8 simplicity, Unity-style convenience.** Implement `init()`, `update()` and
  `draw()`, and the engine calls them for you, 60 times a second.

## Requirements

Java 25 or newer (a full JDK).

With [SDKMAN](https://sdkman.io), run `sdk env install` in this folder to select the
version listed in `.sdkmanrc`.

## Quick start

```sh
java Build.java demo      # compile everything and run games/demo
java Build.java           # just compile
```

`Build.java` is the whole build system: a plain Java file that works on macOS, Linux
and Windows.

### Keys that work in every game

| Key | Action |
|---|---|
| F11 or Alt+Enter (Option+Enter on a Mac) | toggle fullscreen |
| F3 | show fps and timings on screen |

On a Mac, F11 may need the `fn` key.

## Writing a game

Make a folder in `games/` with a `Main.java`:

```java
import pge.Engine;
import pge.Game;
import pge.gfx.Bitmap;
import pge.gfx.Canvas;
import pge.gfx.Color;
import pge.input.Input;
import pge.input.Key;

public class Main implements Game {
    Bitmap hero;
    int x = 100;
    int y = 80;

    public static void main(String[] args) {
        Engine.run(new Main(), 640, 360);
    }

    public void init() {
        hero = Bitmap.load("assets/hero.png");
    }

    public void update() {
        if (Input.isDown(Key.LEFT)) {
            x = x - 2;
        }
        if (Input.isDown(Key.RIGHT)) {
            x = x + 2;
        }
    }

    public void draw(Canvas canvas) {
        canvas.clear(Color.DARK_BLUE);
        canvas.drawBitmap(hero, x, y);
        canvas.drawText("Hello!", 4, 4, Color.WHITE);
    }
}
```

Put your images in `games/yourfolder/assets/`, then run `java Build.java yourfolder`.

### Pictures and text

| You want | Use |
|---|---|
| one image | `Bitmap.load("assets/hero.png")`. Add a color key such as `0xFF00FF` to make one color see-through |
| many same-sized images in one PNG | `SpriteSheet.load("assets/sprites.png", 16, 16)`, then `sheet.get(5)` |
| an animation | `new Animation(sheet.getFrames(0, 4), 8)`: call `update()` every update, draw `getFrame()` |
| a mirrored sprite | `canvas.drawBitmap(bitmap, x, y, flipX, flipY)` |
| a one-color flash or shadow | `canvas.drawSilhouette(bitmap, x, y, flipX, Color.WHITE)` |
| text | `canvas.drawText("Score 10", x, y, Color.WHITE)`, or add a scale (2, 3...) for titles |
| centered text | `canvas.getTextWidth(text)` tells you how wide it is |
| your own font | `canvas.setFont(Font.load("assets/myfont.png", 8, 8))`, laid out like `engine/src/pge/gfx/font.png` |

`640, 360` is the size of the game's picture in pixels. The window shows it at a
whole-number scale (3x on a 1080p screen), so pixels stay crisp. `480, 360` gives a 4:3
game.

## Project layout

```
Build.java              build and run script
engine/src/pge/         Engine (the game loop), Game, Window
engine/src/pge/gfx/     Canvas (all drawing), Color, Bitmap, SpriteSheet, Animation, Font (+ font.png)
engine/src/pge/input/   Input, Key
games/demo/             the demo game (and its assets/)
tools/MakeAssets.java   draws the default font and the demo sprites with code
```

The built-in art (font and demo sprites) is drawn by `tools/MakeAssets.java`. Run
`java tools/MakeAssets.java` to re-create it, or simply edit the PNGs in any pixel editor.

## Engine code rules

The engine is meant to be read and extended by someone learning Java.

- Plain classes, interfaces, simple inheritance, arrays and `ArrayList`, loops and `if`s.
- No lambdas, streams, reflection, custom annotations, or clever generics.
- Private fields with getters and setters.
- Comments explain *why*, and every algorithm says where it comes from.
- Errors say what went wrong in plain English.

## Roadmap

- [x] **M0:** window, game loop, fullscreen, `Canvas` primitives (pixels, lines, rectangles, circles), camera, keyboard
- [x] **M1:** `Bitmap` loading with transparency, flips, silhouettes, `SpriteSheet`, `Animation`, bitmap font and `drawText`
- [ ] **M2:** `TileMap` from CSV (works with the Tiled editor), `Camera` follow and clamp, tile queries
- [ ] **M3:** `Scene` and `GameObject`, UI (`Box`, `Label`, `Button`), mouse and pointer input, draw depth
- [ ] **M4:** an example game and a short guide
- [ ] **M5:** sound
