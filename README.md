# 🎮 PacMan - Classic Arcade Game

A faithful recreation of the classic PacMan arcade game built with Java Swing, featuring authentic gameplay mechanics, ghost AI, power-ups, and sound effects.

![Java](https://img.shields.io/badge/Java-17%2B-orange)
![License](https://img.shields.io/badge/license-MIT-blue)
![Platform](https://img.shields.io/badge/platform-Windows%20%7C%20macOS%20%7C%20Linux-lightgrey)

## ✨ Features

- **Classic Gameplay**: Navigate through the maze, eat pellets, and avoid ghosts
- **Authentic Ghost AI**: Four unique ghost personalities (Blinky, Pinky, Inky, Clyde)
  - Scatter and chase modes with alternating behavior
  - Individual targeting strategies per ghost
- **Power-Ups**: 
  - Power pellets that make ghosts vulnerable
  - Cherry bonus fruits for extra points
- **Progressive Difficulty**: Multiple levels with adjusted power-up duration
- **Audio**: 
  - Continuous background music
  - Dynamic sound effects for collisions and events
- **Score System**: 
  - Points for pellets, power-ups, scared ghosts, and fruits
  - High score tracking across sessions
- **Smooth Animation**: 
  - Directional Pac-Man mouth animation
  - Alternating cherry sprite frames
  - Ghost state transitions

## 🎯 Game Mechanics

- **Movement**: Grid-aligned with buffered turning
- **Scoring**:
  - Regular pellets: 10 points
  - Power pellets: 50 points
  - Scared ghosts: 200 points
  - Cherries: 100 points
- **Lives System**: Start with 3 lives
- **Ghost Behavior**:
  - Normal mode: Chase player with unique strategies
  - Scatter mode: Return to corners periodically
  - Scared mode: Random movement, can be eaten
  - Eyes mode: Return to ghost pen after being eaten

## 🚀 Quick Start

### Download & Play (No Java Required)
Download the latest Windows installer from the [releases](../../releases) section:
- **PacMan-1.0.0.exe** (bundled JRE): https://github.com/AnshuNandi/PacMan-java/releases/tag/v1.0.0

### Run from JAR (Java Required)

If you have Java 8+ installed:
```bash
java -jar PacMan.jar
```

Or double-click `PacMan.jar` on Windows.

## 🛠️ Building from Source

### Prerequisites
- JDK 17 or higher
- Windows: WiX Toolset (for creating installer)

### Build Steps

**1. Compile and package JAR:**
```bash
build.bat
```

Or manually:
```bash
javac -cp src -d bin src\*.java
xcopy /Y src\*.png bin\
xcopy /Y src\*.wav bin\
xcopy /Y src\*.ico bin\
jar cfm PacMan.jar MANIFEST.MF -C bin .
```

**2. Create Windows installer (optional):**
```bash
package-win.bat
```

Output: `dist/PacMan-1.0.0.exe`

**3. Create portable app image (no installer):**
```bash
package-win-appimage.bat
```

Output: `dist/PacMan/PacMan.exe` (portable folder)

## 🎮 Controls

- **Arrow Keys** - Move Pac-Man (Up, Down, Left, Right)
- **Objective** - Eat all pellets to advance to the next level

## 📁 Project Structure

```
PacMan/
├── src/
│   ├── App.java              # Main application launcher
│   ├── PacMan.java            # Game logic and rendering
│   ├── *.png                  # Sprite images
│   ├── *.wav                  # Sound effects
│   └── pacman.ico             # Window icon
├── build.bat                  # Build script
├── package-win.bat            # Windows installer packager
├── run.bat                    # Quick launcher
├── MANIFEST.MF                # JAR manifest
└── README.md                  # This file
```

## 🎨 Game Assets

All game assets are embedded in the JAR:
- Pac-Man sprites (4 directions)
- Ghost sprites (4 colors + scared state)
- Wall tiles, pellets, power-ups
- Background music and sound effects

## 🧩 Technical Details

- **Language**: Java
- **GUI Framework**: Swing/AWT
- **Game Loop**: Timer-based at 20 FPS
- **Grid System**: 19×21 tiles, 32px each
- **Audio**: javax.sound.sampled + procedural tone synthesis
- **Packaging**: jpackage with bundled JRE

## 🐛 Known Issues

- Ghost speed remains constant across levels to prevent oscillation bugs

## 📝 License

- Code: Licensed under the MIT License. See [LICENSE](LICENSE).
- Assets: Images and audio in this repository (e.g., files under `src/` such as *.png, *.wav, *.ico) are included for educational/personal use only and are not licensed for redistribution or commercial use. If you plan to redistribute or publish builds, replace these assets with ones you own or that are properly licensed for your use.
- Trademark: “Pac‑Man” is a registered trademark of Bandai Namco. This is a fan-made educational project and is not affiliated with or endorsed by Bandai Namco.

## 🙏 Acknowledgments

- Inspired by the original Pac-Man arcade game by Namco
- Sound effects generated procedurally using sine wave synthesis

## 🤝 Contributing

Contributions are welcome! Feel free to:
- Report bugs
- Suggest new features
- Submit pull requests

## 📧 Contact

Created by Anshu Nandi

---

⭐ If you enjoy this project, please give it a star!