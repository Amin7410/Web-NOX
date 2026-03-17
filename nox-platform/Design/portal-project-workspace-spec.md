## NOX Portal – Project & Workspace Design Spec

### C.0. Phạm vi & backend liên quan

- **Mục tiêu**: Thiết kế UI/UX cho:
  - Quản lý `Project` (dự án) trong organization.
  - Quản lý `Workspace` (môi trường làm việc / không gian trong project).
  - Liên kết với `Organization` (đã mô tả trong file Organization spec) và `User` (creator/owner).
- **Backend tham chiếu (engine module)**:
  - `com.nox.platform.module.engine.domain.Project`
  - `com.nox.platform.module.engine.domain.Workspace`
  - `com.nox.platform.module.engine.domain.CoreSnapshot` (sẽ chỉ chạm nhẹ).
  - Controller & Service:
    - `ProjectController`, `WorkspaceController`, `SnapshotController`
    - `ProjectService`, `WorkspaceService`, `EngineSnapshotService`

---

### C.1. Sitemap & Route Structure (Project/Workspace)

- **Authenticated**
  - `/projects`
    - Danh sách mọi project mà user có quyền xem (trong các organization).
  - `/projects/create`
    - Tạo project mới, gắn với 1 organization.
  - `/projects/[projectId]`
    - Overview project (thuộc 1 org cụ thể).
  - `/projects/[projectId]/settings`
    - Thiết lập project (tên, slug, visibility, status).
  - `/projects/[projectId]/workspaces`
    - Danh sách workspace thuộc project.
  - `/projects/[projectId]/workspaces/create`
    - Tạo workspace mới.
  - `/projects/[projectId]/workspaces/[workspaceId]`
    - Overview workspace, entry point sang Studio (React/Vite) nếu cần.

> Ghi chú: Có thể lồng route dưới organization (`/organizations/[orgId]/projects/...`) nhưng trong spec này dùng `/projects` độc lập để đơn giản cho frontend; backend vẫn enforce org trong payload.

---

## C.2. `/projects` – Project List

### C.2.1. Mục đích

- Hiển thị tất cả project mà user có quyền xem (across organizations).
- Cho phép filter theo organization, visibility, status.
- Cho phép tạo project mới.

### C.2.2. Layout

- **Header page**
  - Title: `"Projects"`
  - Subtitle: `"Browse and manage all your projects."`
  - Right side:
    - Primary button: `New project` → `/projects/create`

- **Filter bar**
  - Dropdown `Organization`:
    - `All organizations` + list org từ spec B.
  - Dropdown `Visibility`:
    - `All` / `Private` / `Org` / `Public` (map từ `ProjectVisibility`).
  - Dropdown `Status`:
    - `All` / `Active` / `Archived` / `Draft` (map từ `ProjectStatus`).
  - Search input:
    - Placeholder: `"Search by name or slug..."`

- **Project list**
  - **Option A**: Cards grid.
  - **Option B**: Table (nếu muốn hiển thị nhiều thông tin).

**Đề xuất cột / nội dung card**:
- Project name (`Project.name`)
- Slug (`Project.slug`)
- Organization (`Organization.name`)
- Visibility (badge `Private` / `Org` / `Public`)
- Status (badge `Active` / `Archived` / `Draft`)
- Created by (`User.fullName`)
- Updated at (`Project.updatedAt`)

Row/card click → `/projects/[projectId]`.

### C.2.3. States

- **Loading**
  - Skeleton cards / rows.

- **Empty**
  - Khi không có project:
    - `"You don't have any projects yet."`
    - Button: `Create your first project`.
  - Khi filter không ra kết quả:
    - `"No projects match your filters."` + button `Clear filters`.

### C.2.4. API (gợi ý)

- **Get projects list**
  - `GET /api/v1/projects`
  - Query params gợi ý:
    - `orgId`, `visibility`, `status`, `search`, `page`, `size`.
  - Response:
    ```json
    {
      "success": true,
      "data": {
        "items": [
          {
            "id": "uuid",
            "name": "Payment Engine",
            "slug": "payment-engine",
            "organization": {
              "id": "uuid-org",
              "name": "Acme Inc"
            },
            "visibility": "ORG",
            "status": "ACTIVE",
            "createdBy": {
              "id": "uuid-user",
              "fullName": "John Doe"
            },
            "createdAt": "2024-01-01T10:00:00Z",
            "updatedAt": "2024-01-02T10:00:00Z"
          }
        ],
        "page": 0,
        "size": 20,
        "total": 5
      }
    }
    ```

---

## C.3. `/projects/create` – Create Project

### C.3.1. Mục đích

- Tạo project mới trong 1 organization cụ thể.
- Set visibility & status ban đầu.

### C.3.2. Layout

- Title: `"Create project"`
- High-level step:
  - Chọn organization.
  - Nhập thông tin project.

### C.3.3. Fields

- **Organization**
  - Label: `Organization`
  - Type: dropdown.
  - Options: danh sách org user có quyền (từ spec B).
  - Required.

- **Project name**
  - Label: `Project name`
  - Type: `text`
  - Placeholder: `"Payment Engine"`
  - Required.

- **Slug**
  - Label: `Slug`
  - Type: `text`
  - Placeholder: `"payment-engine"`
  - Autogenerate từ name, cho phép chỉnh.

- **Description**
  - Label: `Description`
  - Type: `textarea`
  - Optional.

- **Visibility**
  - Label: `Visibility`
  - Type: radio group hoặc dropdown.
  - Options (từ `ProjectVisibility`):
    - `Private` – chỉ owner hoặc người được share.
    - `Organization` – mọi member trong org.
    - `Public` – public (nếu hệ thống hỗ trợ).

- **Status**
  - Label: `Status`
  - Type: dropdown.
  - Options (từ `ProjectStatus`):
    - `Active`
    - `Draft`
    - `Archived` (ít khi dùng lúc tạo, có thể ẩn).

### C.3.4. Actions & States

- **Primary**: `Create project`
  - Loading state.
  - On success:
    - Redirect → `/projects/[projectId]`.

- **Validation errors**
  - Tên trống, org chưa chọn: message field-specific.
  - Slug trùng trong cùng org:
    - `"A project with this slug already exists in this organization."`

### C.3.5. API (gợi ý)

- **Endpoint**: `POST /api/v1/projects`
  - Request:
    ```json
    {
      "organizationId": "uuid-org",
      "name": "Payment Engine",
      "slug": "payment-engine",
      "description": "Core payment processing logic",
      "visibility": "ORG",
      "status": "ACTIVE"
    }
    ```
  - Response:
    ```json
    {
      "success": true,
      "data": {
        "id": "uuid-project",
        "name": "Payment Engine",
        "slug": "payment-engine"
      }
    }
    ```

---

## C.4. `/projects/[projectId]` – Project Overview

### C.4.1. Mục đích

- Trang tổng quan 1 project:
  - Thông tin cơ bản.
  - Tóm tắt workspaces.
  - Link nhanh sang Studio.

### C.4.2. Layout

- **Header**
  - Project name.
  - Organization name (link → org dashboard).
  - Badges:
    - Visibility (`Private` / `Org` / `Public`).
    - Status (`Active` / `Archived` / `Draft`).
  - Actions:
    - `Open in Studio` (link sang app `studio` với projectId/workspaceId).
    - `Settings` → `/projects/[projectId]/settings`.

- **Tabs / section**
  - `Overview`
  - `Workspaces`
  - Có thể thêm `Snapshots` sau này.

- **Overview content**
  - Left:
    - Description (text, nếu trống hiển thị `"No description provided."`).
  - Right:
    - Cards:
      - `"Organization"` – name + link.
      - `"Created by"` – user name + time.
      - `"Last updated"` – `updatedAt`.
  - Below:
    - `"Recent workspaces"`: list 3–5 workspace gần đây.

### C.4.3. API (gợi ý)

- **Get project detail**
  - `GET /api/v1/projects/{projectId}`
  - Response:
    ```json
    {
      "success": true,
      "data": {
        "id": "uuid-project",
        "name": "Payment Engine",
        "slug": "payment-engine",
        "description": "Core payment processing logic",
        "visibility": "ORG",
        "status": "ACTIVE",
        "organization": {
          "id": "uuid-org",
          "name": "Acme Inc"
        },
        "createdBy": {
          "id": "uuid-user",
          "fullName": "John Doe"
        },
        "createdAt": "2024-01-01T10:00:00Z",
        "updatedAt": "2024-01-02T10:00:00Z"
      }
    }
    ```

---

## C.5. `/projects/[projectId]/settings` – Project Settings

### C.5.1. Mục đích

- Chỉnh sửa thông tin project:
  - Name, slug, description.
  - Visibility, status.
- Chỉ cho phép user có quyền manage project (ví dụ `workspace:manage`).

### C.5.2. Layout

- Title: `"Project settings"`
- Card `"General"`
  - Fields:
    - Name
    - Slug
    - Description
  - Button: `Save general settings`

- Card `"Access & visibility"`
  - Fields:
    - Visibility (radio/dropdown)
    - Status (dropdown)
  - Button: `Save access settings`

### C.5.3. Fields

- **Name**
  - `Project name`, text, required.

- **Slug**
  - `Slug`, text, required, unique per org.

- **Description**
  - `Description`, textarea, optional.

- **Visibility**
  - Radio: `Private` / `Organization` / `Public`.

- **Status**
  - Dropdown: `Active` / `Draft` / `Archived`.

### C.5.4. Actions & API (gợi ý)

- **Update project**
  - `PUT /api/v1/projects/{projectId}`
  - Body:
    ```json
    {
      "name": "Payment Engine v2",
      "slug": "payment-engine",
      "description": "New description",
      "visibility": "ORG",
      "status": "ACTIVE"
    }
    ```

- **Feedback**
  - Success: `"Project updated."`
  - Error: `"Could not update project."`

---

## C.6. `/projects/[projectId]/workspaces` – Workspace List

### C.6.1. Mục đích

- Hiển thị tất cả workspace thuộc 1 project.
- Cho phép tạo workspace mới.
- Cho phép mở workspace (sang Studio).

### C.6.2. Layout

- Header:
  - Title: `"Workspaces"`
  - Subtitle: `"Different environments inside this project."`
  - Right side:
    - Button: `New workspace` → `/projects/[projectId]/workspaces/create`

- Table / cards:
  - Columns:
    - Name (`Workspace.name`)
    - Type (`WorkspaceType` – ví dụ: `MAIN`, `SANDBOX`, `EXPERIMENT`)
    - Created by (user)
    - Created at
    - Deleted at (nếu cần hiển thị archive)
    - Actions:
      - `Open` (đi sang Studio).
      - `Archive` / `Delete` (tùy policy).

### C.6.3. States

- Loading / empty tương tự dự án:
  - `"This project has no workspaces yet."` + button `Create workspace`.

### C.6.4. API (gợi ý)

- **Get workspaces for project**
  - `GET /api/v1/projects/{projectId}/workspaces`
  - Response:
    ```json
    {
      "success": true,
      "data": [
        {
          "id": "uuid-workspace",
          "name": "Main workspace",
          "type": "MAIN",
          "createdBy": {
            "id": "uuid-user",
            "fullName": "John Doe"
          },
          "createdAt": "2024-01-01T10:00:00Z",
          "deletedAt": null
        }
      ]
    }
    ```

---

## C.7. `/projects/[projectId]/workspaces/create` – Create Workspace

### C.7.1. Mục đích

- Tạo workspace mới trong project (thường dùng để thử nghiệm hoặc phân tách môi trường).

### C.7.2. Layout

- Title: `"Create workspace"`
- Form card đơn giản.

### C.7.3. Fields

- **Name**
  - Label: `Workspace name`
  - Type: `text`
  - Placeholder: `"Main workspace"`, `"Experiment A"`, ...
  - Required.

- **Type**
  - Label: `Workspace type`
  - Type: dropdown.
  - Options (từ `WorkspaceType` enum; ví dụ):
    - `MAIN`
    - `SANDBOX`
    - `EXPERIMENT`

### C.7.4. Actions & API (gợi ý)

- **Create**
  - `POST /api/v1/projects/{projectId}/workspaces`
  - Request:
    ```json
    {
      "name": "Main workspace",
      "type": "MAIN"
    }
    ```
  - On success:
    - Redirect → `/projects/[projectId]/workspaces/[workspaceId]`
    - Hoặc mở trực tiếp Studio.

---

## C.8. `/projects/[projectId]/workspaces/[workspaceId]` – Workspace Overview

### C.8.1. Mục đích

- Entry point để chuyển sang Studio (canvas/graph editor).
- Với Portal, chỉ cần overview nhẹ + nút mở Studio.

### C.8.2. Layout

- Header:
  - Workspace name.
  - Badge type (`MAIN` / `SANDBOX` / ...).
  - Project name (link).
  - Actions:
    - `Open in Studio` – chuyển sang URL bên app `studio`, ví dụ:
      - `http://localhost:5173/?projectId=...&workspaceId=...`

- Content:
  - Card `"Details"`:
    - Name
    - Type
    - Created by
    - Created at
  - Card `"Snapshots"` (optional/future):
    - Danh sách `CoreSnapshot` gần đây (nếu expose qua API).

### C.8.3. API (gợi ý)

- **Get workspace detail**
  - `GET /api/v1/projects/{projectId}/workspaces/{workspaceId}`

---

## C.9. Snapshots (Optional / Future)

> Phần này chỉ gợi ý, chưa cần implement ngay nếu backend/Studio chưa sẵn flow.

- Sitemap gợi ý:
  - `/projects/[projectId]/snapshots`
  - `/projects/[projectId]/snapshots/[snapshotId]`

- Mục đích:
  - Lưu và restore trạng thái logic (CoreSnapshot).
  - UX: bảng snapshot với:
    - Tên / mô tả.
    - Created at.
    - Created by.
    - Actions: `Restore`, `Compare`, `Delete`.

---

## C.10. Ghi chú UX chung cho Project & Workspace

- **Liên kết với Organization**
  - Ở mọi nơi có project, nên hiển thị rõ org chủ quản.
  - Cho phép filter theo org ở `/projects`.

- **Quyền hạn**
  - Tạo/đổi/xoá project & workspace chỉ cho user có permission phù hợp (ví dụ `workspace:manage`).
  - UI nên ẩn/disable actions không có quyền, với tooltip `"You don't have permission to perform this action."`

- **Kết nối với Studio**
  - Rõ ràng 1–2 nút `"Open in Studio"`:
    - Ở Project overview (mở workspace chính).
    - Ở Workspace overview (mở workspace tương ứng).
  - Thống nhất format URL query/body mà Studio mong đợi (`projectId`, `workspaceId`).

- **Empty & error states**
  - Project/workspace list rỗng → có CTA tạo mới.
  - Lỗi load → message + nút `Retry`.

- **Consistency**
  - Dùng cùng style với các spec khác:
    - Header, filter bar, table/card, empty state, toast message.
  - Component tái sử dụng từ `@nox/ui` (button, badge, input, table, modal).

