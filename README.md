# TechByte Android - Auth API

## Cau hinh

- Base URL: `http://127.0.0.1:8000/api/techbyte/`
- Duoc khai bao trong `BuildConfig.API_BASE_URL`.

## Mock Debug

Trong build debug, Auth API se tra ve mock response de test UI nhanh.
- De goi backend that, goi `MockConfig.setMockEnabled(false)` o man hinh khoi dong.

## Luu y

- Token duoc luu trong `SharedPreferences` thong qua `SessionStore`.
- Logout se xoa token local va goi API de revoke token tren server.
