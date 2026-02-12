/** @type {import('tailwindcss').Config} */
module.exports = {
    content: [
        // content paths are defined in the extending configs
    ],
    theme: {
        extend: {
            colors: {
                primary: {
                    DEFAULT: '#007bff',
                    foreground: '#ffffff',
                },
                secondary: {
                    DEFAULT: '#6c757d',
                    foreground: '#ffffff',
                },
                destructive: {
                    DEFAULT: '#dc3545',
                    foreground: '#ffffff',
                },
                muted: {
                    DEFAULT: '#f8f9fa',
                    foreground: '#212529',
                },
                accent: {
                    DEFAULT: '#ffc107',
                    foreground: '#212529',
                },
            },
        },
    },
    plugins: [],
};
