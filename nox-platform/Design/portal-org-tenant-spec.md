## NOX Portal – Organization & Member Design Spec

_(Được tách ra từ `portal-auth-spec.md` để dễ đọc hơn. Tài liệu này chỉ tập trung vào phần B – Organization & Member.)_

### B.0. Phạm vi & backend liên quan

- **Mục tiêu**: Thiết kế UI/UX cho quản lý:
  - `Organization` (doanh nghiệp / team).
  - `Role` (vai trò trong tổ chức).
  - `OrgMember` (thành viên trong tổ chức).
- **Backend tham chiếu**:
  - `com.nox.platform.module.tenant.domain.Organization`
  - `com.nox.platform.module.tenant.domain.Role`
  - `com.nox.platform.module.tenant.domain.OrgMember`
  - Controller & Service tương ứng: `OrganizationController`, `RoleController`, `OrgMemberController`, `OrganizationService`, `RoleService`, `OrgMemberService`.

---

### B.1. Sitemap & Route Structure (Tenant/Organization)

- **Authenticated**
  - `/organizations`
    - List tổ chức mà user là member/owner.
  - `/organizations/create`
    - Tạo tổ chức mới.
  - `/organizations/[orgId]`
    - Dashboard tổ chức (overview).
  - `/organizations/[orgId]/settings`
    - Cấu hình cơ bản (tên, slug, settings JSON đơn giản hóa cho UI).
  - `/organizations/[orgId]/members`
    - Quản lý thành viên (`OrgMember`).
  - `/organizations/[orgId]/roles`
    - Quản lý vai trò (`Role`) & phân quyền.

> Lưu ý: Có thể dùng `slug` thay cho `orgId` trong URL nếu backend hỗ trợ, nhưng doc này dùng `[orgId]` cho rõ ràng.

---

## B.2. `/organizations` – Organization List

### B.2.1. Mục đích

- Hiển thị danh sách tổ chức mà user có tham gia.
- Cho phép chọn 1 org để vào dashboard.
- Cho phép tạo tổ chức mới.

### B.2.2. Layout

- **Header page**
  - Title: `"Organizations"`
  - Subtitle: `"Manage your teams and workspaces."`
  - Right side:
    - Primary button: `New organization` → `/organizations/create`

- **Content**
  - Nếu user có ít org:
    - Dùng **cards grid**: mỗi card là 1 organization.
  - Nếu nhiều org:
    - Có thể dùng table hoặc mix (table dạng đơn giản).

**Card contents (per organization)**:
- Name (`Organization.name`)
- Slug (`Organization.slug`)
- Created at (nếu có field ở backend, nếu không thì ẩn).
- Số members (lấy từ `OrgMember` count nếu backend expose, nếu không có thì hiển thị `Members: —`).
- Badge:
  - `Owner` / `Member` (dựa trên role/permission của current user).

### B.2.3. States

- **Loading**
  - Skeleton cards hoặc spinner.

- **Empty state**
  - Icon + text: `"You don't belong to any organization yet."`
  - Button: `Create your first organization` → `/organizations/create`.

### B.2.4. Actions & API (gợi ý)

- **Get organizations of current user**
  - `GET /api/v1/orgs/my` (hoặc tương đương, thực tế phụ thuộc backend; nếu không có thì có thể là `GET /api/v1/orgs` với filter).
  - Response:
    ```json
    {
      "success": true,
      "data": [
        {
          "id": "uuid",
          "name": "Acme Inc",
          "slug": "acme",
          "memberCount": 10,
          "role": "OWNER"
        }
      ]
    }
    ```

---

## B.3. `/organizations/create` – Create Organization

### B.3.1. Mục đích

- Tạo mới `Organization`.
- Sau khi tạo xong, chuyển user vào org dashboard đó.

### B.3.2. Layout

- Title: `"Create organization"`
- Form ở giữa, card width ~480px.

### B.3.3. Fields

- **Organization name**
  - Label: `Organization name`
  - Type: `text`
  - Placeholder: `"Acme Inc"`
  - Required.

- **Slug**
  - Label: `Slug`
  - Type: `text`
  - Placeholder: `"acme"`
  - Description: `"Used in URLs and integrations. Lowercase, no spaces."`
  - Có thể auto-generate từ name và cho phép chỉnh.

### B.3.4. Actions & States

- **Primary**: `Create organization`
  - Loading state.
  - On success: redirect → `/organizations/[orgId]` (hoặc slug).

- **Validation errors**
  - Tên để trống: `"Organization name is required."`
  - Slug trùng:
    - `"This slug is already taken."`

### B.3.5. API Contract (gợi ý)

- **Endpoint**: `POST /api/v1/orgs`
  - Request:
    ```json
    {
      "name": "Acme Inc",
      "slug": "acme"
    }
    ```
  - Response:
    ```json
    {
      "success": true,
      "data": {
        "id": "uuid",
        "name": "Acme Inc",
        "slug": "acme"
      }
    }
    ```

---

## B.4. `/organizations/[orgId]` – Organization Dashboard

### B.4.1. Mục đích

- Trang tổng quan cho 1 tổ chức.
- Từ đây người dùng điều hướng sang Projects, Members, Roles, Settings.

### B.4.2. Layout

- **Header**
  - Tên tổ chức.
  - Slug.
  - Badge `Owner` / `Admin` / `Member`.
  - Action: `Settings` (link → `/organizations/[orgId]/settings`).

- **Tabs hoặc navigation secondary**
  - `Overview`
  - `Projects` (link sang phần Project & Workspace nếu có)
  - `Members`
  - `Roles`
  - `Settings`

- **Overview content (suggested)**
  - Card 1: `"Members"` – số lượng thành viên.
  - Card 2: `"Projects"` – số lượng project thuộc org.
  - Card 3: `"Created at"` – ngày tạo.
  - List ngắn:
    - Recent members (3–5 người).
    - Recent projects (3–5 project).

### B.4.3. API (gợi ý)

- **Get organization detail**
  - `GET /api/v1/orgs/{orgId}`
  - Response:
    ```json
    {
      "success": true,
      "data": {
        "id": "uuid",
        "name": "Acme Inc",
        "slug": "acme",
        "settings": {},
        "createdAt": "2024-01-01T10:00:00Z"
      }
    }
    ```

---

## B.5. `/organizations/[orgId]/settings` – Organization Settings

### B.5.1. Mục đích

- Cho phép owner/admin chỉnh sửa thông tin cơ bản của tổ chức.
- Chỉ những user có quyền (ví dụ `workspace:manage` hoặc tương đương trong tenant module) mới thấy được.

### B.5.2. Layout

- Title: `"Organization settings"`
- Card `"General"`
  - Fields:
    - Name
    - Slug
  - Button `Save changes`

- Card `"Advanced"` (optional / future)
  - Một số flag từ `Organization.settings` nếu expose ra UI (ví dụ: default visibility, feature toggles).

### B.5.3. Fields

- **Name**
  - Label: `Organization name`
  - Type: `text`

- **Slug**
  - Label: `Slug`
  - Type: `text`
  - Chỉ cho phép chỉnh nếu business rule cho phép (cẩn trọng vì ảnh hưởng URL).

### B.5.4. Actions & API (gợi ý)

- **Update organization**
  - `PUT /api/v1/orgs/{orgId}`
  - Body:
    ```json
    {
      "name": "New Name",
      "slug": "new-slug"
    }
    ```

- **Feedback**
  - Success: `"Organization updated."`
  - Error: `"Could not update organization."` + chi tiết slug trùng nếu có.

---

## B.6. `/organizations/[orgId]/members` – Members Management

### B.6.1. Mục đích

- Quản lý `OrgMember`: xem, thêm, đổi role, xoá thành viên.

### B.6.2. Layout

- Header:
  - Title: `"Members"`
  - Subtitle: `"Manage who can access this organization."`
  - Right side:
    - Button: `Invite member`

- Table members:
  - Columns:
    - Name (từ `User.fullName`)
    - Email (`User.email`)
    - Role (`Role.name`)
    - Joined at (`OrgMember.joinedAt`)
    - Invited by (tên user)
    - Actions (Role dropdown, Remove)

### B.6.3. Invite member flow

- Khi click `Invite member`:
  - Modal:
    - Title: `"Invite member"`
    - Fields:
      - Email
      - Role (dropdown nhanh)
    - Actions:
      - `Send invite`
      - `Cancel`

- States:
  - Success: `"Invitation sent to user@example.com."`
  - Error:
    - `"This user is already a member."`
    - `"You don't have permission to invite members."`

### B.6.4. Change role

- Tại mỗi dòng trong table:
  - Cột Role có Dropdown:
    - `Owner` (nếu policy cho phép)
    - `Admin`
    - `Member`
    - Custom roles khác từ `Role` module.
  - Khi chọn role mới:
    - Gọi API update `OrgMember.role`.
    - Toast `"Member role updated."`

### B.6.5. Remove member

- Action `Remove` (icon trash hoặc menu `...` → `Remove`).
- Modal confirm:
  - Title: `"Remove member?"`
  - Body: `"This user will lose access to this organization and its projects."`
  - Actions: `Cancel` / `Remove`

### B.6.6. API Contract (gợi ý)

- **Get members**
  - `GET /api/v1/orgs/{orgId}/members`
  - Response:
    ```json
    {
      "success": true,
      "data": [
        {
          "id": "uuid-orgMember",
          "user": {
            "id": "uuid-user",
            "fullName": "John Doe",
            "email": "user@example.com"
          },
          "role": {
            "id": "uuid-role",
            "name": "Admin",
            "level": 10
          },
          "invitedBy": {
            "id": "uuid-user2",
            "fullName": "Alice"
          },
          "joinedAt": "2024-01-01T10:00:00Z"
        }
      ]
    }
    ```

- **Invite member**
  - `POST /api/v1/orgs/{orgId}/members/invite`
  - Request:
    ```json
    {
      "email": "user@example.com",
      "roleId": "uuid-role"
    }
    ```

- **Update member role**
  - `PUT /api/v1/orgs/{orgId}/members/{memberId}`
  - Body:
    ```json
    {
      "roleId": "uuid-role"
    }
    ```

- **Remove member**
  - `DELETE /api/v1/orgs/{orgId}/members/{memberId}`

---

## B.7. `/organizations/[orgId]/roles` – Role Management

### B.7.1. Mục đích

- Quản lý `Role` trong 1 organization:
  - Tạo role mới.
  - Chỉnh sửa role (tên, level, permissions).
  - Xoá role (nếu không còn thành viên dùng).

### B.7.2. Layout

- Header:
  - Title: `"Roles"`
  - Subtitle: `"Define what members can do in this organization."`
  - Button: `New role`

- Content:
  - Table hoặc list roles.
  - Mỗi role hiển thị:
    - Name (`Role.name`)
    - Level (`Role.level`) – số càng cao có thể quyền càng mạnh.
    - Permissions (danh sách ngắn, ví dụ `"project:read, project:write, member:manage"` – lấy từ `Role.permissions` là `text[]`).
    - Actions: `Edit`, `Delete`.

### B.7.3. Create/Edit role

- Modal hoặc side panel:
  - Fields:
    - **Name**
      - Type: `text`.
    - **Level**
      - Type: `number`.
      - Description: `"Higher level means higher priority."`
    - **Permissions**
      - Multi-select hoặc tags input.
      - Suggest sẵn danh sách permission phổ biến (ví dụ):
        - `project:read`, `project:write`, `project:delete`
        - `member:read`, `member:invite`, `member:remove`
        - `role:read`, `role:manage`

- Actions:
  - `Save`
  - `Cancel`

- Validation:
  - Tên không rỗng.
  - Level là số hợp lệ.

### B.7.4. Delete role

- Chỉ cho phép nếu không có `OrgMember` nào dùng role đó (hoặc backend handle).
- Modal confirm:
  - `"Delete role 'Admin'?"`
  - Cảnh báo: `"Members using this role may lose access."` (tuỳ logic).

### B.7.5. API Contract (gợi ý)

- **Get roles**
  - `GET /api/v1/orgs/{orgId}/roles`

- **Create role**
  - `POST /api/v1/orgs/{orgId}/roles`
  - Body:
    ```json
    {
      "name": "Admin",
      "level": 10,
      "permissions": [
        "project:read",
        "project:write",
        "member:manage"
      ]
    }
    ```

- **Update role**
  - `PUT /api/v1/orgs/{orgId}/roles/{roleId}`

- **Delete role**
  - `DELETE /api/v1/orgs/{orgId}/roles/{roleId}`

---

## B.8. Ghi chú UX chung cho phần Organization & Member

- **Quyền hạn (authorization)**
  - Một số actions chỉ cho phép:
    - Owner / Admin:
      - Invite/remove members.
      - Create/edit/delete roles.
      - Edit organization settings.
  - UI nên ẩn hoặc disable button nếu user không có quyền, kèm tooltip `"You don't have permission to perform this action."`

- **Context switching giữa organizations**
  - Có thể có **organization switcher** ở header (dropdown chọn org hiện tại).
  - Khi đổi org, redirect tới `/organizations/[orgId]/...` phù hợp.

- **Empty & error states**
  - Luôn thiết kế state trống hợp lý (không chỉ là bảng rỗng).
  - Lỗi load:
    - `"We couldn't load organizations. Please try again."` + nút retry.

- **Consistency**
  - Reuse component:
    - Table, modal, form, dropdown từ `@nox/ui`.
  - Tên actions & text message nhất quán (invite/remove/update).

