# M0 힌지 센서 실측 — 2026-09-16

기기: `SM-F971N` (product `h8qksx`, 내부 화면 2448×1848 / 커버 1248×1972)

## 결론

**공개 `Sensor.TYPE_HINGE_ANGLE`은 존재하지만 연속값이 아니다.**  
실측 이벤트는 `180 → 90 → 0` 세 단계뿐이다. M1은 “손의 속도와 1:1 동기화”를 전제로 설계하면 안 된다.

## 공개 센서

`SensorManager.getDefaultSensor(TYPE_HINGE_ANGLE)` 결과:

| 항목 | 값 |
|---|---|
| name | `hinge_angle  Wakeup` |
| vendor | Samsung |
| type | 36 (`android.sensor.hinge_angle`) |
| maxRange | 180.0 |
| **resolution** | **90.0** |
| minDelay | 0 |

앱 로그 (`FoldMotion`)가 접힘 중에 받은 값:

```
180.0
90.0
0.0
90.0
180.0
90.0
180.0
90.0
0.0
90.0
180.0
```

중간값(178, 142, 63 등)은 한 번도 오지 않았다.

## 삼성 비공개 센서 (참고)

`dumpsys sensorservice`에 연속 이벤트로 보이는 센서가 있다.

| name | type | permission |
|---|---|---|
| `Folding Angle` | `com.samsung.sensor.folding_angle` (65686) | `com.samsung.permission.SSENSOR` |
| `lid_angle_fusion` | `com.samsung.sensor.folding_state` (65695) | `com.samsung.permission.SSENSOR` |
| `folding_state_lpm` | `com.samsung.sensor.folding_state_lpm` (65697) | `com.samsung.permission.SSENSOR` |

`Folding Angle`은 last 50 events가 쌓여 연속 샘플링으로 보인다. 값은 시스템에서 masked.  
일반 Play Store 앱이 `SSENSOR`를 받을 수 있는지는 **미확인 · 가능성 낮음**. V1 기본 경로로 쓰지 않는다.

## M1에 미치는 영향

1. **기본 입력:** 공개 힌지 각도 3상태 + `FoldingFeature` posture (`FLAT` / `HALF_OPENED`).
2. **체감 보정:** 상태 전환 사이는 시간 기반 interpolation (손과 완전 동기화는 포기).
3. **후순위 실험:** `SSENSOR` / `folding_angle` 접근 가능 여부만 스파이크로 확인. 되면 고해상도 경로를 옵션으로 둔다.

## 재현

```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb logcat -s FoldMotion:D
```

앱 화면에서 Live를 고른 뒤 Fold를 천천히 접고, Logcat에 0/90/180 이외의 값이 나오는지 확인한다.
