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
                // Added for Nox Design System
                border: '#27272a', // zinc-800
                input: '#27272a',
                ring: '#3b82f6', // blue-500
                background: '#09090b', // zinc-950
                surface: '#09090b', // zinc-950 (user requested nox-surface)
                foreground: '#fafafa', // zinc-50
            },
            fontFamily: {
                sans: ['"Inter"', 'ui-sans-serif', 'system-ui', 'sans-serif'],
                mono: ['"JetBrains Mono"', 'ui-monospace', 'SFMono-Regular', 'monospace'],
            },
        },
    },
    plugins: [],
};
