# FoldMotion

Galaxy Z Fold의 힌지 각도에 맞춰 접힘 순간에만 시각 효과를 얹는 앱입니다. One UI Home을 바꾸지 않고, 루트·접근성·SystemUI 해킹도 쓰지 않습니다.

현재 마일스톤: **M4 V1 스타일 4종**. Fluid / Shadow / Fade / Haptic, 강도, 햅틱 토글. 다음은 **M5 스토어 출시**.

## 제품 원칙

- 런처 교체 없음
- Overlay 합성만 (다른 앱 Surface를 실제로 접지 않음)
- Play Store / Galaxy Store 우선, Good Lock은 이후
- 브랜드에 Apple을 쓰지 않음

## 실측 (SM-F971N / Fold8)

공개 `Sensor.TYPE_HINGE_ANGLE`은 존재하지만 값은 **180 / 90 / 0**뿐입니다. 연속 각도는 시간 보간으로 메웁니다. 기록: `docs/m0-hinge-findings.md`.

접히면 `CONCURRENT_INNER_DEFAULT`로 바깥 화면을 켜고 커버 홈 `Presentation`을 띄웁니다.

## 빌드

```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

minSdk 30, compileSdk 36, Kotlin + Jetpack Compose.
