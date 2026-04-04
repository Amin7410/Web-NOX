import axios from 'axios';

// Khởi tạo một Axios instance mới dùng đặc quyền cho NOX
export const apiClient = axios.create({
    // Sử dụng proxy từ vite (`/api` -> sẽ tự forward sang 8081)
    baseURL: '/api', 
    headers: {
        'Content-Type': 'application/json',
    },
    timeout: 10000, // Timeout sau 10 giây
});

// Request Interceptor: Nơi lý tưởng để tự động gắn Bearer Token (JWT) sau này
apiClient.interceptors.request.use(
    (config) => {
        // 1. Kiểm tra URL xem có token/orgId chuyển tiếp từ Portal không
        if (typeof window !== 'undefined') {
            const urlParams = new URLSearchParams(window.location.search);
            const urlToken = urlParams.get('token');
            const urlOrgId = urlParams.get('orgId');
            
            if (urlToken && urlToken !== 'null') {
                localStorage.setItem('nox_token', urlToken);
            }
            if (urlOrgId && urlOrgId !== 'null') {
                localStorage.setItem('nox_org_id', urlOrgId);
            }

            // Xóa token khỏi URL để bảo mật và làm sạch thanh địa chỉ (chỉ khi có token/orgId mới xóa)
            if (urlToken || urlOrgId) {
                const url = new URL(window.location.href);
                url.searchParams.delete('token');
                url.searchParams.delete('orgId');
                window.history.replaceState({}, '', url.pathname + url.search);
            }
        }

        // 2. Lấy Token & OrgId từ LocalStorage
        const token = typeof window !== 'undefined' ? localStorage.getItem('nox_token') : null;
        const orgId = typeof window !== 'undefined' ? localStorage.getItem('nox_org_id') : null;

        if (token && token !== 'null') {
            config.headers.Authorization = `Bearer ${token}`;
        }
        if (orgId && orgId !== 'null') {
            config.headers['X-Org-Id'] = orgId;
        }
        
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Response Interceptor: Bắt lỗi chung từ Server (như 401 Unauthorized, 500)
apiClient.interceptors.response.use(
    (response) => {
        return response;
    },
    (error) => {
        if (error.response) {
            // Lỗi trả về từ server
            console.error('API Error:', error.response.status, error.response.data);
            if (error.response.status === 401) {
                // Xử lý mất phiên đăng nhập (Token hết hạn) -> Đẩy ra trang chủ Portal
                // window.location.href = '/login';
            }
        } else if (error.request) {
            // Server không phản hồi
            console.error('Network Error: Server không phản hồi.');
        } else {
            console.error('Error', error.message);
        }
        return Promise.reject(error);
    }
);
