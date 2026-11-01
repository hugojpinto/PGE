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
java Build.java demo         # compile everything and run games/demo
java Build.java platformer   # ... or the platformer
java Build.java              # just compile
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

### Levels and the camera

| You want | Use |
|---|---|
| a level made of tiles | `TileMap.load("assets/level1.csv", tileSheet)`, then `map.draw(canvas)` |
| walls | `map.setSolid(tileNumber, true)` |
| "would I hit a wall there?" | `map.overlapsSolid(x, y, width, height)` |
| the tile at a position | `map.getTileAt(x, y)`, or `map.getTile(column, row)` |
| change the level while playing | `map.setTile(column, row, tileNumber)` (use `TileMap.EMPTY` to clear) |
| scenery behind the level | a second `TileMap`, drawn first |
| a camera that follows the hero | `camera.follow(x, y)` in `update()`, `camera.applyTo(canvas)` in `draw()` |
| a camera that stays inside the level | `camera.setLimits(map.getWidth(), map.getHeight())` |
| a softer camera | `camera.setSmoothness(0.15)` |

Level files are CSV: one line per row, tile numbers separated by commas, `-1` for
empty. The free [Tiled](https://www.mapeditor.org/) editor can export this format
(File > Export As > CSV, one file per layer). `games/platformer` shows how to stop a
character at walls: move one pixel at a time and ask the map before every step.

`640, 360` is the size of the game's picture in pixels. The window shows it at a
whole-number scale (3x on a 1080p screen), so pixels stay crisp. `480, 360` gives a 4:3
game.

## Project layout

```
Build.java              build and run script
engine/src/pge/         Engine (the game loop), Game, Window
engine/src/pge/gfx/     Canvas (all drawing), Color, Bitmap, SpriteSheet, Animation, Font (+ font.png)
engine/src/pge/input/   Input, Key
engine/src/pge/world/   TileMap, Camera
games/demo/             top-down demo: sprites, animation, text, camera
games/platformer/       platformer: tile maps, layers, gravity, jumping, wall collisions
tools/MakeAssets.java   draws the built-in art and the platformer level with code
```

The built-in art (font, sprites, tiles) and the platformer's level are made by `tools/MakeAssets.java`. Run
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
- [x] **M2:** `TileMap` from CSV (Tiled-compatible), layers, solid tiles and wall queries, `Camera` with follow, limits and smoothing
- [ ] **M3:** `Scene` and `GameObject`, UI (`Box`, `Label`, `Button`), mouse and pointer input, draw depth
- [ ] **M4:** an example game and a short guide
- [ ] **M5:** sound
