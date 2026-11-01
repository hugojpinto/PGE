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
| F3 | show fps and timings in the title bar |

On a Mac, F11 may need the `fn` key.

## Writing a game

Make a folder in `games/` with a `Main.java`:

```java
import pge.Engine;
import pge.Game;
import pge.gfx.Canvas;
import pge.gfx.Color;
import pge.input.Input;
import pge.input.Key;

public class Main implements Game {
    int x = 100;
    int y = 80;

    public static void main(String[] args) {
        Engine.run(new Main(), 640, 360);
    }

    public void init() {
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
        canvas.fillRect(x, y, 16, 16, Color.YELLOW);
    }
}
```

Then run `java Build.java yourfolder`.

`640, 360` is the size of the game's picture in pixels. The window shows it at a
whole-number scale (3x on a 1080p screen), so pixels stay crisp. `480, 360` gives a 4:3
game.

## Project layout

```
Build.java              build and run script
engine/src/pge/         Engine (the game loop), Game, Window
engine/src/pge/gfx/     Canvas (all drawing), Color
engine/src/pge/input/   Input, Key
games/demo/             the demo game
```

## Engine code rules

The engine is meant to be read and extended by someone learning Java.

- Plain classes, interfaces, simple inheritance, arrays and `ArrayList`, loops and `if`s.
- No lambdas, streams, reflection, custom annotations, or clever generics.
- Private fields with getters and setters.
- Comments explain *why*, and every algorithm says where it comes from.
- Errors say what went wrong in plain English.

## Roadmap

- [x] **M0:** window, game loop, fullscreen, `Canvas` primitives (pixels, lines, rectangles, circles), camera, keyboard
- [ ] **M1:** `Bitmap` loading with transparency, flips, `SpriteSheet`, bitmap font and `drawText`, animation
- [ ] **M2:** `TileMap` from CSV (works with the Tiled editor), `Camera` follow and clamp, tile queries
- [ ] **M3:** `Scene` and `GameObject`, UI (`Box`, `Label`, `Button`), mouse and pointer input, draw depth
- [ ] **M4:** an example game and a short guide
- [ ] **M5:** sound
