"""生成 Demo 乐园音乐播放器用的八音盒旋律 WAV（可复现，无外部依赖）。"""
import math
import struct
import wave

SR = 22050  # 采样率
NOTE = 0.35  # 每个音的秒数
# C 大调旋律（音名, 拍数）：小星星前两句
MELODY = [
    ("C5", 1), ("C5", 1), ("G5", 1), ("G5", 1), ("A5", 1), ("A5", 1), ("G5", 2),
    ("F5", 1), ("F5", 1), ("E5", 1), ("E5", 1), ("D5", 1), ("D5", 1), ("C5", 2),
]
FREQ = {"C5": 523.25, "D5": 587.33, "E5": 659.25, "F5": 698.46, "G5": 783.99, "A5": 880.0}

samples = []
for name, beats in MELODY:
    dur = NOTE * beats
    f = FREQ[name]
    n = int(SR * dur)
    for i in range(n):
        t = i / SR
        env = math.exp(-3.0 * t / dur)  # 指数衰减包络，八音盒音色
        if t > dur - 0.01:              # 音符间留 10ms 静音防爆音
            env = 0.0
        samples.append(int(32767 * 0.5 * env * math.sin(2 * math.pi * f * t)))

with wave.open("app/src/main/res/raw/demo_melody.wav", "w") as w:
    w.setnchannels(1)
    w.setsampwidth(2)
    w.setframerate(SR)
    w.writeframes(b"".join(struct.pack("<h", s) for s in samples))
print(f"生成完成：{len(samples)} 采样，约 {len(samples)/SR:.1f} 秒")
