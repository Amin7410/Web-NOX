## NOX Portal – Auth & Account Design Spec

### 0. Phạm vi

- **Mục tiêu**: Đặc tả UI/UX và luồng cho phần **Auth & Account** của NOX Portal (Next.js 14).
- **Đối tượng sử dụng tài liệu**:
  - **Designer**: vẽ Figma/wireframe.
  - **Frontend dev**: implement page, gọi API backend.
- **Backend tham chiếu**: `AuthController`, `AuthenticationService`, `PasswordRecoveryService`, `Mfa*Service`, `UserSessionService`, `User` / `UserSecurity` / `UserSession` / `UserMfaBackupCode` entities.

---

## 1. Sitemap & Route Structure

- **Public (không login)**
  - `/auth/login`
  - `/auth/register`
  - `/auth/verify-email?token=...`
  - `/auth/forgot-password`
  - `/auth/reset-password?token=...`

- **Authenticated (yêu cầu đăng nhập)**
  - `/account/profile`
  - `/account/security`
  - `/account/sessions`
  - `/account/mfa`

**Layout gợi ý (Next.js App Router)**:
- `app/(public)/auth/*` – layout public (không sidebar, có logo giữa).
- `app/(app)/account/*` – layout chính sau login (header + sidebar account).

---

## 2. `/auth/login`

### 2.1. Mục đích

- Cho phép user đăng nhập vào NOX Portal.
- Tạo session, nhận access token / refresh token từ backend.
- Xử lý trường hợp cần MFA (nếu backend trả yêu cầu MFA).

### 2.2. Layout (Figma)

- **Page layout**
  - Background sáng, full height.
  - Center 1 **auth card** rộng ~400–480px.
  - Trên cùng: logo NOX + title.

- **Auth card**
  - Title: **"Sign in to NOX Portal"**
  - Subtitle nhỏ: `"Access your projects and organizations."`
  - Form dọc, field full width.
  - Spacer dưới là links phụ (Forgot password, Register).

### 2.3. Fields

- **Email**
  - Label: `Email`
  - Input type: `email`
  - Placeholder: `you@example.com`
  - Required.

- **Password**
  - Label: `Password`
  - Input type: `password`
  - Placeholder: `••••••••`
  - Required.

- **Remember this device** (optional)
  - Label: `Remember this device`
  - Checkbox.
  - Chỉ là flag client-side cho UX (tùy cách lưu token).

### 2.4. Actions

- **Primary button**: `Sign in`
  - Full width.
  - State:
    - **Default**
    - **Loading**: disabled, spinner, label `"Signing in..."`.
    - **Success**: redirect.
    - **Error**: hiển thị error message.

- **Secondary links**
  - `Forgot your password?` → `/auth/forgot-password`
  - `Don’t have an account? Sign up` → `/auth/register`

### 2.5. State & Error Handling

- **Loading state**
  - Khi submit, disable form + button.

- **Error types (gợi ý mapping từ backend)**
  - Sai email/password:
    - Message: `"Email or password is incorrect."`
    - Vị trí: alert nhỏ phía trên form hoặc dưới title.
  - Tài khoản bị khóa / disabled:
    - Message: `"Your account is disabled. Please contact support."`
  - Lỗi server chung:
    - Message: `"Something went wrong. Please try again later."`

- **Success**
  - Nếu login thành công + không yêu cầu MFA:
    - Redirect đến default app route: `/` hoặc `/projects` (tuỳ portal).
  - Nếu backend yêu cầu MFA (tùy cách implement):
    - Chuyển sang màn MFA riêng (có thể là `/auth/mfa` – ngoài phạm vi file này, chỉ cần chừa chỗ).

### 2.6. API Contract (gợi ý)

> Tên trường có thể điều chỉnh theo `AuthRequest` trong backend, đây là format tham chiếu.

- **Endpoint**: `POST /api/v1/auth/login`
- **Request body example**:
  ```json
  {
    "email": "user@example.com",
    "password": "Password123!"
  }
  ```
- **Success response (gợi ý)**:
  ```json
  {
    "success": true,
    "data": {
      "accessToken": "jwt-access-token",
      "refreshToken": "jwt-refresh-token",
      "user": {
        "id": "uuid",
        "email": "user@example.com",
        "fullName": "John Doe",
        "status": "ACTIVE",
        "isEmailVerified": true
      }
    }
  }
  ```
- **Error response (gợi ý)**:
  ```json
  {
    "success": false,
    "error": {
      "code": "INVALID_CREDENTIALS",
      "message": "Email or password is incorrect."
    }
  }
  ```

---

## 3. `/auth/register`

### 3.1. Mục đích

- Tạo tài khoản mới (`User` + `UserSecurity`).
- Sau khi đăng ký thành công, gửi email verify.

### 3.2. Layout

- Tương tự `/auth/login`, có thể chia 2 cột:
  - Trái: text giới thiệu ngắn (benefit).
  - Phải: registration card.

### 3.3. Fields

- **Full name**
  - Label: `Full name`
  - Input type: `text`
  - Required.

- **Email**
  - Label: `Email`
  - Input type: `email`
  - Required, hiển thị warning nếu trùng.

- **Password**
  - Label: `Password`
  - Type: `password`
  - Rules (hiển thị rõ cho user):
    - Min 8 ký tự.
    - Nên có chữ hoa, chữ thường, số (tuỳ security policy backend).

- **Confirm password**
  - Label: `Confirm password`
  - Type: `password`
  - Required, phải trùng với password.

### 3.4. Actions

- **Primary**: `Create account`
  - State: default / loading / success / error.

- **Secondary**
  - Text: `Already have an account? Sign in`
  - Link → `/auth/login`

### 3.5. State & Error Handling

- **Success**
  - Hiển thị panel:
    - Title: `"Verify your email"`
    - Body: `"We’ve sent a verification link to user@example.com. Please check your inbox."`
  - Option: button `Back to sign in`.

- **Validation errors**
  - Email đã tồn tại:
    - Message: `"An account with this email already exists."`
  - Mật khẩu yếu:
    - Message: `"Password does not meet security requirements."`

- **Server error**
  - `"Something went wrong. Please try again later."`

### 3.6. API Contract (gợi ý)

- **Endpoint**: `POST /api/v1/auth/register`
- **Request body example**:
  ```json
  {
    "fullName": "John Doe",
    "email": "user@example.com",
    "password": "Password123!"
  }
  ```
- **Success**:
  ```json
  {
    "success": true,
    "data": {
      "userId": "uuid",
      "email": "user@example.com",
      "requiresEmailVerification": true
    }
  }
  ```

---

## 4. `/auth/verify-email?token=...`

### 4.1. Mục đích

- Xử lý link verify email được gửi từ backend.

### 4.2. Layout & States

- Single page, không cần form.
- Khi load:
  - Gọi API verify với query param `token`.

**UI state**:
- **Loading**
  - Spinner + text `"Verifying your email..."`.
- **Success**
  - Icon success (checkmark).
  - Title: `"Your email has been verified."`
  - Body: `"You can now sign in to your account."`
  - Button: `Go to sign in` → `/auth/login`.
- **Error**
  - Icon warning.
  - Title: `"Verification link is invalid or expired."`
  - Body: gợi ý `"Request a new verification email from your account settings."` hoặc `"Please try registering again."`

### 4.3. API Contract (gợi ý)

- **Endpoint**: `POST /api/v1/auth/verify-email`
- **Request**:
  ```json
  {
    "token": "verification-token-from-email"
  }
  ```
- **Response success**:
  ```json
  { "success": true }
  ```

---

## 5. `/auth/forgot-password`

### 5.1. Mục đích

- Cho phép user yêu cầu email reset password.

### 5.2. Layout

- Card nhỏ tương tự login, chỉ có 1 field email.
- Text giải thích:
  - `"Enter your email and we'll send you a link to reset your password."`

### 5.3. Fields

- **Email**
  - Label: `Email`
  - Type: `email`
  - Required.

### 5.4. Actions & States

- **Primary**: `Send reset link`
  - On success:
    - Không cần lộ thông tin email có tồn tại hay không.
    - Message: `"If an account with this email exists, we have sent a reset link."`
  - On error (lỗi hệ thống):
    - `"Something went wrong. Please try again later."`

- **Secondary**
  - Link `Back to sign in` → `/auth/login`.

### 5.5. API Contract (gợi ý)

- **Endpoint**: `POST /api/v1/auth/forgot-password`
- **Request**:
  ```json
  {
    "email": "user@example.com"
  }
  ```
- **Response (success)**:
  ```json
  { "success": true }
  ```

---

## 6. `/auth/reset-password?token=...`

### 6.1. Mục đích

- Cho phép user đặt mật khẩu mới bằng link reset.

### 6.2. Layout

- Card với 2 field password.
- Text: `"Choose a new password for your account."`

### 6.3. Fields

- **New password**
  - Label: `New password`
  - Type: `password`
  - Rules hiển thị rõ như phần register.

- **Confirm new password**
  - Label: `Confirm new password`
  - Type: `password`

### 6.4. Actions & States

- **Primary**: `Reset password`
  - `Loading` state.
  - On success:
    - Message: `"Your password has been updated."`
    - Button: `Sign in` → `/auth/login`.
  - On invalid/expired token:
    - Title: `"Reset link is invalid or expired."`
    - Suggest: `"Please request a new reset link."` (link tới `/auth/forgot-password`).

### 6.5. API Contract (gợi ý)

- **Endpoint**: `POST /api/v1/auth/reset-password`
- **Request**:
  ```json
  {
    "token": "reset-token-from-email",
    "newPassword": "NewPassword123!"
  }
  ```

---

## 7. Layout sau đăng nhập cho Account pages

### 7.1. Account layout chung

- Sử dụng cho:
  - `/account/profile`
  - `/account/security`
  - `/account/sessions`
  - `/account/mfa`

- **Header**
  - Logo nhỏ.
  - Tên user + avatar + menu (Profile, Sign out).

- **Sidebar (Account menu)**
  - `Profile`
  - `Security`
  - `Sessions`
  - `Multi-factor authentication`

- **Content area**
  - Tiêu đề theo page.
  - Card / form / table tương ứng.

---

## 8. `/account/profile`

### 8.1. Mục đích

- Cho phép user xem & chỉnh sửa thông tin cơ bản (`User`).

### 8.2. Layout

- Tiêu đề: `"Profile"`
- Card `"Personal information"`:
  - Avatar + form đơn giản.

### 8.3. Fields

- **Avatar**
  - Hình tròn, chữ cái đầu nếu chưa có hình.
  - Button `Change` (nếu sau này có upload, hiện tại có thể để placeholder).

- **Full name**
  - Label: `Full name`
  - Type: `text`

- **Email**
  - Label: `Email`
  - Type: `email`
  - Read-only (nếu backend không cho đổi).
  - Badge `Verified` / `Unverified` dựa trên `isEmailVerified`.

- **Account status**
  - Badge: `Active` / `Pending` / `Disabled` (từ `UserStatus`).
  - Chỉ hiển thị, không chỉnh sửa từ đây.

### 8.4. Actions

- **Primary**: `Save changes`
  - Chỉ enable khi có thay đổi.

- **Feedback**
  - Success: toast `"Profile updated."`
  - Error: toast `"Could not update profile."`

### 8.5. API Contract (gợi ý)

- **Get profile**: `GET /api/v1/account/me`
- **Update profile**: `PUT /api/v1/account/me`
  - Body:
    ```json
    {
      "fullName": "New Name"
    }
    ```

---

## 9. `/account/security`

### 9.1. Mục đích

- Đổi mật khẩu.
- Tóm tắt trạng thái bảo mật (email verified, MFA, last login).

### 9.2. Layout

- Hai block chính:
  1. `Change password`
  2. `Security overview`

### 9.3. Fields – Change password

- **Current password**
  - Label: `Current password`
  - Type: `password`

- **New password**
  - Label: `New password`
  - Type: `password`

- **Confirm new password**
  - Label: `Confirm new password`
  - Type: `password`

### 9.4. Actions

- **Primary**: `Change password`
  - Loading state.
  - Success: `"Password updated."`
  - Fail (current password sai): `"Current password is incorrect."`

### 9.5. Security overview (read-only)

- Email verification:
  - `"Email: Verified"` hoặc `"Email: Not verified"`.

- MFA status:
  - `"Multi-factor authentication: Enabled/Disabled"` (link `Manage` → `/account/mfa`).

- Last login:
  - Lấy từ `UserSession` mới nhất: hiển thị `Last login at`, `IP`, `device`.

### 9.6. API Contract (gợi ý)

- **Change password**: `POST /api/v1/auth/change-password`
  - Request:
    ```json
    {
      "currentPassword": "OldPass123!",
      "newPassword": "NewPass123!"
    }
    ```

---

## 10. `/account/sessions`

### 10.1. Mục đích

- Cho phép user xem & quản lý các phiên đăng nhập (`UserSession`).

### 10.2. Layout

- Title: `"Sessions"`
- Table:
  - Columns:
    - Device (rút gọn từ user agent, ví dụ: `"Chrome on macOS"`)
    - IP address
    - Location (optional, nếu có)
    - Last active
    - Created at
    - Status (`Current session` badge / `Active` / `Revoked`)
  - Row action: `Sign out` (nếu không phải current session).

- Top-right:
  - Button: `Sign out from all other devices`

### 10.3. States

- **Empty state**
  - `"No active sessions found."`

- **Loading**
  - Skeleton rows hoặc spinner.

### 10.4. Actions & API (gợi ý)

- **Get sessions**: `GET /api/v1/account/sessions`
  - Response example:
    ```json
    {
      "success": true,
      "data": [
        {
          "id": "uuid",
          "ipAddress": "192.168.1.10",
          "userAgent": "Mozilla/5.0 ...",
          "createdAt": "2024-01-01T10:00:00Z",
          "lastAccessedAt": "2024-01-02T12:00:00Z",
          "current": true
        }
      ]
    }
    ```

- **Sign out a session**: `DELETE /api/v1/account/sessions/{sessionId}`

- **Sign out other sessions**: `POST /api/v1/account/sessions/revoke-others`

---

## 11. `/account/mfa`

### 11.1. Mục đích

- Bật/tắt MFA.
- Hiển thị/generates backup codes.

### 11.2. Layout – concept stepper

- **Header**: `"Multi-factor authentication"`
- 3 khối:
  1. `Status`
  2. `Enable MFA` (QR code + verify code)
  3. `Backup codes`
  4. `Disable MFA`

> Designer có thể thể hiện dạng stepper nếu muốn, nhưng functional chỉ cần các block.

### 11.3. Block 1 – Status

- Hiển thị:
  - `"MFA is currently: Enabled"` (badge xanh) hoặc `"Disabled"` (badge xám).

### 11.4. Block 2 – Enable MFA

- Chỉ hiển thị nếu MFA đang Disabled.

- Bước:
  1. **Get secret + QR**:
     - Button: `Set up authenticator app`.
     - Khi click: gọi API để lấy `secret` & `otpauthUrl`.
  2. **Hiển thị QR code**:
     - QR code từ `otpauthUrl`.
     - Text: `"Scan this QR code with Google Authenticator, 1Password, etc."`
     - Ngoài ra hiển thị secret text dạng code block.
  3. **Input code**:
     - Field: `6-digit verification code`
     - Button: `Verify & enable`

- States:
  - Success: `"MFA has been enabled on your account."`
  - Error (code sai): `"Invalid verification code. Please try again."`

### 11.5. Block 3 – Backup codes

- Hiển thị chỉ khi MFA Enabled.

- Nội dung:
  - List các backup codes (ẩn bớt phần giữa, ví dụ: `ABCD-****-1234` nếu muốn bảo mật).
  - Text warning:
    - `"Store these backup codes in a safe place. Each code can only be used once."`

- Actions:
  - Button: `Show backup codes` (yêu cầu xác nhận password/MFA tùy policy).
  - Button: `Generate new backup codes` (invalidate cũ).

### 11.6. Block 4 – Disable MFA

- Button: `Disable MFA`
  - Bắt buộc confirm modal:
    - Title: `"Disable multi-factor authentication?"`
    - Body: `"Your account will be protected by password only."`
    - Actions: `Cancel` / `Disable`

### 11.7. API Contract (gợi ý)

> Tên endpoint thực tế phụ thuộc `MfaManagementService` / `MfaVerificationService`, đây là gợi ý mapping.

- **Get status**: `GET /api/v1/account/mfa`
  - Response:
    ```json
    {
      "success": true,
      "data": {
        "enabled": true,
        "hasBackupCodes": true
      }
    }
    ```

- **Start setup (get secret)**: `POST /api/v1/account/mfa/setup`
  - Response:
    ```json
    {
      "success": true,
      "data": {
        "secret": "BASE32SECRET",
        "otpauthUrl": "otpauth://totp/NOX:user@example.com?secret=..."
      }
    }
    ```

- **Verify & enable**: `POST /api/v1/account/mfa/enable`
  - Request:
    ```json
    {
      "code": "123456"
    }
    ```

- **Get backup codes**: `GET /api/v1/account/mfa/backup-codes`

- **Regenerate backup codes**: `POST /api/v1/account/mfa/backup-codes/regenerate`

- **Disable MFA**: `POST /api/v1/account/mfa/disable`

---

## 12. Ghi chú chung cho Designer & Dev

- **Tone & ngôn ngữ**:
  - UI text có thể dùng tiếng Anh cho bản đầu:
    - `"Sign in"`, `"Create account"`, `"Multi-factor authentication"`, v.v.
  - Nếu cần bản tiếng Việt, giữ key cấu trúc tương đương.

- **Loading & Error**
  - Luôn có loading khi call API.
  - Error hiển thị gần component liên quan:
    - Form error → dưới field hoặc trên form.
    - Global error → banner trên card.

- **Responsive**
  - Mobile: card full width với padding, stack dọc.
  - Desktop: card width cố định, center, khoảng trắng xung quanh.

- **Consistent components (dùng @nox/ui)**
  - Button, Input, Form, Alert, Badge nên dùng từ `@nox/ui` cho đồng nhất.

