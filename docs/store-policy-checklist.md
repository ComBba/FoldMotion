# FoldMotion 스토어 정책 체크리스트

대상: Play Store + Galaxy Store 내부/비공개 테스트. Good Lock 업로드는 M6.

## 권한

| 권한 | 목적 | 카피에 쓸 설명 |
|---|---|---|
| `SYSTEM_ALERT_WINDOW` | 힌지 동기 투명 Overlay | “접힘 순간에 시각 효과를 다른 앱 위에 합성합니다. 터치는 통과합니다.” |
| `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_SPECIAL_USE` | 앱을 닫아도 힌지·Overlay 유지 | “접힘 효과를 유지하기 위한 상주 서비스입니다. 알림에서 끌 수 있습니다.” |
| `POST_NOTIFICATIONS` | FGS 지속 알림 | 효과 켜짐 상태와 끄기 액션 |
| `VIBRATE` | Haptic Fold / 햅틱 토글 | 접힘 각도 전환 시 짧은 진동 |
| `WAKE_LOCK` | 커버 Presentation 세션 | 접는 동안 화면 유지 |
| `CONTROL_DEVICE_STATE` | 커버 화면 점등 (hidden API) | Play 장기 경로에서는 제거 또는 면제 재검토 |

Accessibility, 기본 홈(`CATEGORY_HOME`), 알림 접근, 사용 기록 접근은 **선언하지 않는다**.

## Overlay / 시스템 UI

- Overlay는 `FLAG_NOT_TOUCHABLE | FLAG_NOT_FOCUSABLE`이다. 터치를 가로채지 않는다.
- 창 알파를 `maximumObscuringOpacityForTouch` 이하로 둔다 (Android 12+ 터치 차단).
- One UI Home / SystemUI를 모방하는 아이콘·이름·애니메이션 카피를 쓰지 않는다.
- Apple / iPhone / Duo를 브랜드·스토어 문구에 쓰지 않는다.

## Foreground Service

- 유형: `specialUse`
- 속성: `android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE` = hinge-synced visual overlay
- 시작: 사용자 토글(포그라운드 Activity)만. 백그라운드 자동 시작 없음
- 중지: 알림 **끄기**, HUD 토글 OFF
- Play Console FGS 선언은 제출 시 이 문서를 첨부한다

## 개인정보

- 계정, 위치, 광고 ID, 연락처를 수집하지 않는다
- 힌지 각도는 기기 안에서만 쓴다. 서버 전송 없음
- 정책문: `docs/privacy.md`

## 제출 전 확인

- [x] Accessibility 미사용
- [x] 런처 교체 없음
- [x] Overlay 터치 통과
- [x] FGS 알림에서 즉시 종료
- [ ] Play Console 내부 테스트 트랙 업로드 (계정 필요)
- [ ] Galaxy Store Seller Portal 업로드 (계정 필요)
- [ ] Fold8 실기기 스토어 스크린샷 세트 (Preview + Overlay). 초안: `docs/m4-styles-hud.png`
