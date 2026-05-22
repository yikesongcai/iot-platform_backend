# Device Management Pagination

## Goal
Add traditional pagination (el-pagination) to the device management page, replacing the current full-load pattern.

## Design

### Backend
No changes. Use existing `GET /device/page?page=1&size=10` endpoint.

### Frontend — `DeviceManagement .vue`
1. Replace `POST /device/list` with `GET /device/page?page=N&size=N`
2. Add pagination refs: `currentPage`, `pageSize`, `total`
3. Add `<el-pagination>` below device cards (matching UserManagement style)
4. Search becomes client-side filter on loaded page data (or triggers re-fetch)
5. Device stats chart unchanged

### Files
- Modify: `iot-platform_frontend/src/components/DeviceManagement .vue`
