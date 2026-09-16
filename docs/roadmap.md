# FoldMotion 로드맵

> 출처: [ChatGPT 공유 — 폴드8 애니메이션 구현](https://chatgpt.com/share/6aa9e730-1818-83ee-b67f-323b8c143b8c)  
> 작성일: 2026-09-16  
> 현재 코드베이스: M3 상주 Foreground Service. 다음은 M4 V1 스타일 4종.

## 한 줄 목표

Galaxy Z Fold8의 힌지 각도에 맞춰 **One UI를 바꾸지 않고** 접힘 순간에만 시각 효과를 얹는, Play Store / Galaxy Store에 올릴 수 있는 접힘 이펙트 앱.

## 제품 원칙 (고정)

대화에서 최종 합의된 비협상 조건이다.

1. **One UI Home을 바꾸지 않는다.** 런처 교체는 V1 범위 밖이다.
2. **Root / AccessibilityService / SystemUI 해킹을 쓰지 않는다.**
3. 일반 앱은 다른 앱 Surface를 실제로 휘게 만들 수 없다. **투명 Overlay로 합성**한다.
4. 1차 배포는 **Good Lock 모듈이 아니라 Play Store + Galaxy Store**다.
5. 브랜드/카피는 Apple·iPhone Duo를 언급하지 않는다. 독자 브랜드(FoldMotion)로 간다.
6. V1은 “아이폰과 100% 동일”이 아니라 **접을 때 삼성 기본 기능처럼 느껴지는 것**이 성공이다.

## 기술 전제

| 항목 | 결정 |
|---|---|
| 언어 / UI | Kotlin + Jetpack Compose |
| minSdk | 30 (API `TYPE_HINGE_ANGLE`) |
| 힌지 입력 | `Sensor.TYPE_HINGE_ANGLE` 우선. 없으면 `FoldingFeature` + posture fallback |
| 진행률 | `progress = 1f - (angle / 180f)` (180°=펼침, 0°=닫힘) |
| 시스템 효과 | `SYSTEM_ALERT_WINDOW` + `TYPE_APPLICATION_OVERLAY` |
| Overlay 창 | `FLAG_NOT_FOCUSABLE` + `FLAG_NOT_TOUCHABLE` (터치 통과) |
| 백그라운드 센서 | Foreground Service + 지속 알림. Accessibility 금지 |
| 대상 기기 | Galaxy Z Fold8 실기기. 에뮬레이터만으로는 Go/No-Go 불가 |

## 할 수 있는 것과 없는 것

```text
V1 (스토어 가능)
  Fold8 hinge → FoldMotion → transparent overlay → One UI / 다른 앱 그대로

최종형 (삼성 권한 필요)
  Fold8 hinge → SystemUI / WindowManager → 현재 앱 Surface 자체 변형
```

- **앱 안 Preview:** 거의 완벽하게 가능
- **One UI 위 Overlay:** 그림자·dim·vignette·암전으로 “접히는 느낌”까지 가능. 창 자체 3D 변형은 불가
- **모든 앱 Surface를 실제로 접기:** 일반 APK 권한 밖. Good Lock / 삼성 협업 이후

## 마일스톤 한눈에

```text
M0 센서 Go/No-Go ──► M1 인앱 엔진 ──► M2 Overlay ──► M3 상주 서비스
                                                         │
                                                         ▼
                                              M4 V1 스타일 4종
                                                         │
                                                         ▼
                                              M5 스토어 출시
                                                         │
                                                         ▼
                                              M6 트래션 → Good Lock 제안
```

예상 공수: 솔로 개발자 기준 **약 6~8주** (M0~M5). M6는 출시 이후.

---

## M0 — 힌지 센서 Go/No-Go

**상태: 실측 완료 (연속값 No-Go)** — 2026-09-16 `SM-F971N`. 기록: `docs/m0-hinge-findings.md`

**목표:** Fold8에서 힌지 각도가 **연속값**으로 들어오는지 확인한다.  
**기간:** 0.5일  
**실패 시:** 연속 애니메이션 전제를 버리고 `FLAT / HALF_OPENED` 근사로 재설계한다.

**실측 요약:** 공개 `TYPE_HINGE_ANGLE`은 존재한다. 값은 `180 / 90 / 0`만 온다 (`resolution=90`). 연속 각도는 삼성 `folding_angle`(+`SSENSOR`) 쪽에 있는 것으로 보이며, 스토어 앱 기본 경로로 쓰지 않는다.

**M1 입력 결정:** 공개 3상태 + 시간 보간 (`FoldAngleSmoother`, 280ms ease-in-out). 손 속도와 1:1 동기화는 포기한다.

### 범위

- 빈 Android 앱 (Kotlin, Compose, minSdk 30)
- `Sensor.TYPE_HINGE_ANGLE` null 체크
- Logcat + 화면 큰 숫자로 실시간 각도 표시
- 디버그 프리셋: 180° / 90° / 45° / 0°

### 완료 조건

- [x] Fold8 USB 디버깅으로 설치·실행
- [x] `hingeSensor != null`
- [x] 연속 하강은 **실패** (180/90/0만). 시간 보간으로 M1 진행
- [x] 화면에 `HINGE: n°` 실시간 갱신 (이산값)

### 산출물

- 로컬 APK
- 센서 존재/샘플링 주기/노이즈 메모 (`docs/m0-hinge-findings.md`)

---

## M1 — 인앱 Fold Physics 엔진

**상태: 인앱 Preview 완료** — 2026-09-16 `SM-F971N`. 스크린샷 `docs/m1-180.png` / `m1-90.png` / `m1-45.png` / `m1-0.png`

**목표:** 우리 앱 화면 안에서 힌지 progress에 맞춰 효과가 움직이게 한다.  
**기간:** 3~5일  
**의존:** M0 Go

### 범위

- `angle → progress(0..1)` 단일 소스
- Compose `graphicsLayer`: scale / rotationY / alpha
- 레이어를 하나씩 추가: dim → hinge shadow → vignette → blur 느낌 → 마지막 blackout
- Preview 화면 (홈을 흉내 낸 가짜 아이콘 그리드면 충분)
- 단위 테스트: 각도 구간별 progress 클램프

### 각도 연출 가이드 (대화 원안)

| 각도 | 연출 |
|---:|---|
| 180° | 정상 화면 |
| 150° | 중앙에 약한 힌지 그림자 |
| 120° | 양쪽 dim 시작 |
| 90° | vignette + 약한 scale |
| 45° | 암전 증가, 중앙 수렴 느낌 |
| 15° | 거의 암전 |
| 0° | blackout |

### 완료 조건

- [x] 앱을 연 상태에서 Fold8을 접으면 Preview가 각도에 동기화된다 (3상태 + 280ms 보간)
- [x] Overlay / 권한 / 서비스는 아직 없다 (M2)
- [x] progress 변환이 테스트로 고정된다

**커버 전원 (M1 연장, 2026-09-16):** 접힘 progress ≥ 0.20이면 `DeviceStateManager.requestState(CONCURRENT_INNER_DEFAULT)`로 바깥 화면을 켠 뒤, 커버 디스플레이에 `Presentation`으로 `CoverHomeScreen`을 띄운다. 히스테리시스 OFF는 0.08. `requestState`는 `@TestApi`라 `hiddenapibypass`로 호출한다. Play 심사용 장기 경로는 M5에서 재검토.

### 산출물

- `HingeAngleSource`, `FoldProgress`, `FoldFxRenderer` 경계가 분리된 모듈

---

## M2 — One UI 위 Overlay

**상태: 완료** — 2026-09-16 Home·Chrome 위 터치 통과 Overlay (PR #1).

**목표:** 다른 앱을 쓰는 중에도 접으면 효과가 얹힌다. 터치는 통과한다.  
**기간:** 4~6일  
**의존:** M1

### 범위

- `SYSTEM_ALERT_WINDOW` 권한 온보딩 (“다른 앱 위에 표시”)
- `TYPE_APPLICATION_OVERLAY` 투명 창
- `FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCHABLE`
- 앱 밖(One UI Home, Chrome 등)에서 동일 렌더러 재사용
- 권한이 없으면 Preview만 동작하고, 시스템 효과는 비활성

### 명시적 비범위

- 카카오톡/유튜브 Surface를 실제로 축소·왜곡하지 않음
- 터치 가로채기, 전체화면 인텐트, Accessibility 미사용

### 완료 조건

- [x] 권한 허용 후 Home/다른 앱 위에서 힌지 동기 효과가 보인다 (2026-09-16 Home·Chrome)
- [x] 아이콘·버튼을 평소처럼 누를 수 있다 (터치 통과)
- [x] 권한 거부 시 앱이 죽지 않고 Preview로 안내한다

### 리스크

- Play 정책: 시스템 UI 모방·방해로 보이지 않게 카피/UI를 설계해야 한다
- Android 12+ 신뢰할 수 없는 터치 차단: Overlay를 터치 불가로 유지하는 것이 방어선이다

---

## M3 — 상주 감지 (Foreground Service)

**상태: 구현** — Overlay 소유권은 `FoldOverlayService` (FGS `specialUse`). Activity `onDestroy`에서 Overlay를 떼지 않는다.

**목표:** 앱을 닫아도 힌지를 읽고 효과를 유지한다.  
**기간:** 4~6일  
**의존:** M2

### 범위

- “효과 활성화” 토글 → 지속 알림 + FGS
- Android 14+ `foregroundServiceType` 명시
- 센서 `SENSOR_DELAY_GAME` (또는 배터리 측정 후 조정)
- 비활성 시 센서 해제·Overlay 제거
- 접힘 움직임이 없을 때 업데이트 스로틀

### 완료 조건

- [x] 최근 앱에서 FoldMotion을 쓸어도 효과가 유지된다 (HOME + `am kill` 후에도 FGS·`FoldMotionOverlay` 유지. 2026-09-16)
- [x] 알림에서 즉시 끌 수 있다 (지속 알림 액션 **끄기** → `ACTION_STOP`)
- [ ] 배터리/발열을 Fold8에서 30분 접었다 펴며 기록한다 (병합 비차단. M4 전 기록)

### 리스크

- Android 12+ 백그라운드에서 FGS 시작 제한 → 사용자 제스처(토글)에서만 start
- Play Console에 FGS 사용 목적 설명이 필요 (제출은 M5)

---

## M4 — V1 스타일 4종 + 설정

**목표:** 스토어에 올릴 최소 제품 기능을 닫는다.  
**기간:** 1~2주  
**의존:** M3

대화에서 합의된 V1 기능만 넣는다. 그 외 스타일(Curtain, Gravity 등)은 V1.1.

| 스타일 | 체감 |
|---|---|
| Fluid Fold | 중앙으로 압축되는 느낌 |
| Hinge Shadow | 접힘 깊이·중앙 그림자 |
| Fade Fold | 각도에 따른 자연 암전 |
| Haptic Fold | 특정 각도 미세 진동 |

설정:

- Folding effect: ON / OFF
- Style
- Strength (예: 70%)
- Haptic: ON / OFF

### 완료 조건

- [ ] Fold8 실기기에서 4스타일 모두 접기/펴기 왕복이 자연스럽다
- [ ] 설정 변경이 다음 접힘부터 즉시 반영된다
- [ ] 기본값은 과하지 않은 Fluid + Strength 중간

---

## M5 — 스토어 출시

**목표:** Play Store와 Galaxy Store에 심사 가능한 빌드를 올린다.  
**기간:** 1~2주  
**의존:** M4

### 범위

- 패키지/표시 이름: FoldMotion (가칭). iPhone Duo 언급 금지
- 스토어 카피 예: “Interactive animations synchronized with your Galaxy Fold's folding motion.”
- Overlay 권한 목적 설명, FGS 유형 선언, 개인정보처리방침
- Galaxy Store Seller Portal + Play Console 내부/비공개 테스트
- Fold8 실기기 스크린샷·짧은 데모 영상 (우리 Preview + Overlay)

### 완료 조건

- [ ] 내부 테스트 트랙에 빌드가 올라간다
- [ ] 정책 체크리스트(시스템 모방, Overlay, FGS, 접근성 오용 없음)를 문서화한다
- [ ] 심사 반려 시 수정 포인트가 권한/카피 쪽으로 한정된다

### 명시적 비범위

- Good Lock 업로드
- 기본 홈 앱(`CATEGORY_HOME`) 선언

---

## M6 — 트래션 후 Good Lock 제안

**목표:** 사용자 근거를 들고 삼성 협업을 제안한다.  
**기간:** 출시 이후  
**의존:** M5 + 실제 사용 지표

대화의 4단계 배포 경로:

1. Play Store 프로토타입/출시 (Overlay FX)
2. Galaxy Store에서 Fold 전용 포지셔닝
3. 설치·리텐션·효과 사용률 확보
4. Good Lock 팀에 “Overlay 한계 → SystemUI 변형이 진짜 다음 단계”로 제안

Good Lock은 일반 개발자 모듈 장터가 아니다. ClockFace 등 삼성이 연 플러그인 범위만 공개되어 있다. 파트너십 전제 없이 M0~M5를 막으면 안 된다.

---

## 나중에 해도 되는 것 (YAGNI)

- 커스텀 런처 / 기본 홈 교체
- Depth / Curtain / Gravity 등 추가 스타일
- 아이콘이 물리적으로 떨어지는 연출 (런처를 소유할 때만 의미가 큼)
- 루팅 SystemUI 모드
- Apple 대비 마케팅

## 의사결정 로그

| 결정 | 이유 |
|---|---|
| 런처 기각 | Fold8 사용자는 One UI 위젯·검색·태스크바를 유지하고 싶어 함. 애니메이션 하나 때문에 홈을 바꾸면 이탈 |
| Overlay 채택 | 스토어에 올릴 수 있는 유일한 시스템 위 경로. Surface 변형은 불가임을 제품 카피에 정직하게 반영 |
| Good Lock 후순위 | 공개 등록 절차가 확인되지 않음. 트래션 후 제안이 현실적 |
| 센서 먼저 | WindowManager `FoldingFeature`는 연속 각도가 아님. 엔진 설계가 M0 결과에 종속 |

## 다음 실행 — M4 스타일 4종

M3 완료 후: Fluid Fold / Hinge Shadow / Fade Fold / Haptic Fold + Strength. 커버 `Presentation`과 DeviceState 경로는 그대로 둔다.

건드리지 말 것: Accessibility, 런처 교체, force-stop으로 FGS를 검증하기.
