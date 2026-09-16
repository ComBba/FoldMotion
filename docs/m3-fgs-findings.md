# M3 Foreground Service 실측

날짜: 2026-09-16  
기기: SM-F971N (Galaxy Z Fold8)

## 결과

- HUD 토글로 `FoldOverlayService`가 `isForeground=true`, type `specialUse` (`0x40000000`)로 올라간다.
- Overlay 창 title `FoldMotionOverlay` (`TYPE_APPLICATION_OVERLAY` ty=2038).
- `HOME` 이후에도 pid·FGS·Overlay 창이 유지된다.
- `am kill com.foldmotion.app`은 FGS 프로세스를 죽이지 못한다 (force-stop은 제외 — FGS까지 내린다).
- 지속 알림 채널 `fold_overlay`, 액션 **끄기** → `PendingIntent.getService(ACTION_STOP)`.
- 접힘 움직임이 없으면 Overlay 파라미터 publish를 건너뛰고, 새 힌지 샘플이 오면 즉시 보간을 다시 시작한다 (유휴 대기는 interruptible).

## 의도적으로 안 한 것

- 30분 배터리/발열 로그는 M3 병합을 막지 않는다. M4 전에 별도 기록.
- `am start-foreground-service`는 `android:exported=false`라 셸에서 직접 호출하지 않는다. 시작은 앱 포그라운드 토글만.

스크린샷: `docs/m3-fgs-hud.png`, `docs/m3-notification.png`.
