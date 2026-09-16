# FoldMotion 스토어 리스팅 (초안)

브랜드: **FoldMotion**  
패키지: `com.foldmotion.app`

Apple, iPhone, Duo를 언급하지 않는다.

## 짧은 설명 (80자 이내)

Galaxy Z Fold 힌지에 맞춰 접히는 순간에만 시각 효과를 얹습니다.

## 긴 설명

FoldMotion은 One UI Home을 바꾸지 않습니다. Galaxy Z Fold의 힌지 각도에 맞춰, 접히는 순간에만 투명 Overlay로 그림자와 암전을 합성합니다.

- 런처 교체 없음. 위젯·검색·태스크바를 그대로 둡니다.
- 터치는 통과합니다. 다른 앱을 평소처럼 사용할 수 있습니다.
- 스타일: Fluid, Hinge Shadow, Fade, Haptic
- 강도 조절과 햅틱 온/오프
- 최근 앱에서 쓸어도 알림의 상주 서비스가 효과를 유지합니다. 알림 권한이 있으면 그 알림에서 바로 끌 수 있습니다. 알림을 거부한 경우에는 앱을 다시 열어 HUD 토글로 끕니다.

이 앱은 다른 앱의 화면을 실제로 접거나 왜곡하지 않습니다. 접힘 느낌을 위한 합성 효과입니다.

## 영문 short

Interactive animations synchronized with your Galaxy Fold's folding motion.

## 영문 long

FoldMotion adds a brief visual effect as you fold your Galaxy Z Fold. It does not replace One UI Home.

The overlay is touch-through. Choose Fluid, Hinge Shadow, Fade, or Haptic, then set strength. A persistent notification can keep the effect available after you leave the app. If notification permission is granted, you can turn the effect off from that notification. Otherwise open FoldMotion and use the overlay switch.

FoldMotion composites lighting and dimming. It cannot physically warp other apps' surfaces.

## 스크린샷 순서 (권장)

1. HUD에서 효과 ON (`docs/m4-styles-hud.png`)
2. Home 위 90° Fluid
3. Chrome 위 Overlay (M2 `docs/m2-overlay-chrome.png`)
4. 알림에서 끄기 (`docs/m3-notification.png`)
5. 커버 화면 Presentation (M1 커버 샷)
