/** @type {import('tailwindcss').Config} */
module.exports = {
    darkMode: ["class"],
    content: [
        // content paths are defined in the extending configs
    ],
    theme: {
        extend: {
            colors: {
                primary: {
                    DEFAULT: 'rgb(var(--primary))',
                    foreground: 'rgb(var(--primary-foreground))',
                },
                secondary: {
                    DEFAULT: 'rgb(var(--secondary))',
                    foreground: 'rgb(var(--secondary-foreground))',
                },
                destructive: {
                    DEFAULT: 'rgb(var(--destructive))',
                    foreground: 'rgb(var(--destructive-foreground))',
                },
                muted: {
                    DEFAULT: 'rgb(var(--muted))',
                    foreground: 'rgb(var(--muted-foreground))',
                },
                accent: {
                    DEFAULT: 'rgb(var(--accent))',
                    foreground: 'rgb(var(--accent-foreground))',
                },
                border: 'rgb(var(--border))',
                input: 'rgb(var(--input))',
                ring: 'rgb(var(--ring))',
                background: 'rgb(var(--background))',
                card: 'rgb(var(--card))',
                'card-foreground': 'rgb(var(--card-foreground))',
                popover: 'rgb(var(--popover))',
                'popover-foreground': 'rgb(var(--popover-foreground))',
                foreground: 'rgb(var(--foreground))',
            },
            fontFamily: {
                sans: ['"Inter"', 'ui-sans-serif', 'system-ui', 'sans-serif'],
                mono: ['"JetBrains Mono"', 'ui-monospace', 'SFMono-Regular', 'monospace'],
            },
        },
    },
    plugins: [],
};
