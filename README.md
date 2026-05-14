# AFK — Macro Recorder & Player

> Record and replay mouse movements, clicks, and keyboard inputs automatically.

**Created by Sitthan Aromprasert**

---

## Requirements

| Item | Version |
|------|---------|
| JDK  | 21+     |
| JNativeHook | 2.2.2 |

Download JNativeHook: https://github.com/kwhat/jnativehook/releases/tag/2.2.2

---

## Installation

**TH**
1. แตก zip file และเข้า folder `AFK`
2. สร้าง folder ชื่อ `lib`
3. นำไฟล์ต่อไปนี้ใส่ใน `lib\`
   - `JNativeHook.jar`
   - `JNativeHook-2.2.2.x86_64.dll`
4. รัน `run.bat` เพื่อเปิดโปรแกรม
5. รัน `build_exe.bat` เพื่อสร้างไฟล์ `.exe` (ต้องการ JDK เท่านั้น)

**EN**
1. Extract the zip file and open the `AFK` folder
2. Create a folder named `lib`
3. Place the following files inside `lib\`
   - `JNativeHook.jar`
   - `JNativeHook-2.2.2.x86_64.dll`
4. Run `run.bat` to launch the program
5. Run `build_exe.bat` to build an `.exe` installer (requires JDK)

---

## 📂 File Structure

```
AFK\
├── AFK.jar
├── run.bat
├── build_exe.bat
├── MANIFEST.MF
├── 📂 lib\
│   ├── JNativeHook.jar              ← ดาวน์โหลดเอง / download manually
│   └── JNativeHook-2.2.2.x86_64.dll
├── 📂 icons\
│   ├── record.png
│   ├── play.png
│   ├── open.png
│   ├── setting.png
│   └── app.ico
└── 📂 src\
    ├── AppConfig.java
    ├── DpiHelper.java
    ├── HotkeyManager.java
    ├── MainFrame.java
    ├── Player.java
    ├── RecordedEvent.java
    ├── Recorder.java
    └── SettingDialog.java

```

---

## Usage

### Buttons

| Button | TH | EN |
|--------|----|----|
| **Record** | อัดการกระทำทั้งหมดบนหน้าจอ (mouse + keyboard) กดซ้ำเพื่อหยุดและบันทึก | Records all screen actions (mouse + keyboard). Press again to stop and save. |
| **Play** | เล่นซ้ำ Record ที่โหลดอยู่ กดซ้ำระหว่างเล่นเพื่อหยุด | Replays the loaded recording. Press again during playback to stop. |
| **Open** | เลือกไฟล์ `.afk` ที่ต้องการใช้งาน | Select a `.afk` recording file to load. |
| **Setting** | ปรับการตั้งค่าต่างๆ | Adjust program settings. |

### Settings

| Option | TH | EN |
|--------|----|-----|
| **Record Hotkey** | ปุ่มลัดสำหรับ Record (default: F6) | Hotkey for Record (default: F6) |
| **Play Hotkey** | ปุ่มลัดสำหรับ Play (default: F5) | Hotkey for Play (default: F5) |
| **Always on Top** | หน้าต่าง AFK ลอยอยู่เสมอ | Keep AFK window always on top |
| **Continuous Playback** | เล่นซ้ำวนไปเรื่อยๆ จนกดหยุด | Loop playback until stopped |
| **Set Playback Loop** | กำหนดจำนวนรอบที่จะเล่นซ้ำ | Set number of playback repetitions |
| **Display Scale** | ตั้งค่าให้ตรงกับ Windows Display Scale | Match your Windows Display Scale setting |

### ⚠️ Display Scale (Important / สำคัญ)

**TH:** หากหน้าจอไม่ได้ใช้ 100% ต้องตั้งค่านี้ให้ตรง ไม่เช่นนั้น mouse จะเคลื่อนที่ผิดตำแหน่ง
ตรวจสอบได้ที่ `Windows Settings → System → Display → Scale`

**EN:** If your display scale is not 100%, you must set this to match.
Otherwise mouse positions will be offset during playback.
Check at `Windows Settings → System → Display → Scale`

---

## Known Limitations

- **Display Scale** ต้องตั้งค่าด้วยตนเองให้ตรงกับ Windows (Java ไม่สามารถตรวจจับได้อัตโนมัติ)
- **Display Scale** must be set manually to match Windows (Java cannot detect it automatically)
- Hotkeys are **global** — work even when AFK is not in focus (requires `JNativeHook.jar`)
- `.afk` recording files are **not** cross-machine compatible if screen resolutions differ

---

## Disclaimer

โปรแกรมนี้ยังอยู่ในช่วงทดสอบและพัฒนาขึ้นเพื่อใช้งานส่วนตัว ไม่ได้มีวัตถุประสงค์เพื่อใช้ในเชิงพาณิชย์แต่อย่างใด

หากพบข้อผิดพลาดหรือปัญหาในการใช้งาน กรุณาแจ้งให้ผู้พัฒนาทราบเพื่อปรับปรุงในอนาคตต่อไป

**หากท่านนำโปรแกรมนี้ไปใช้งานในเชิงพาณิชย์ ผู้พัฒนาจะไม่รับผิดชอบต่อความเสียหายใดๆ ที่อาจเกิดขึ้น**

---

This program is still in testing and was developed for personal use only. It is not intended for commercial purposes.

If you encounter any bugs or issues, please feel free to report them so improvements can be made.

**If you choose to use this program for commercial purposes, the developer assumes no responsibility for any damages that may occur.**

---

## License

MIT License

Copyright (c) 2025 Sitthan Aromprasert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

---

## Dependencies

- [JNativeHook](https://github.com/kwhat/jnativehook) by Alex Barker — LGPL v3