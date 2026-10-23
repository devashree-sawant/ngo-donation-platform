import axios from "axios";

const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
    headers: { "Content-Type": "application/json" },
});

api.interceptors.request.use((config) => {
    // Login and registration must work even if the browser has an expired token stored.
    const isPublicAuthCall = config.url?.startsWith("/auth/");
    const token = !isPublicAuthCall ? localStorage.getItem("token") : null;
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
});

export function getApiError(error, fallback = "Something went wrong. Please try again.") {
    return error?.response?.data?.message || error?.response?.data?.error || error?.message || fallback;
}

export default api;

api.interceptors.response.use(
    (response) => response,
    (error) => {
        const isAuthCall = error.config?.url?.startsWith("/auth/");
        if (error.response?.status === 401 && !isAuthCall && window.location.pathname !== "/") {
            localStorage.clear();
            window.location.replace("/");
        }
        return Promise.reject(error);
    }
);
