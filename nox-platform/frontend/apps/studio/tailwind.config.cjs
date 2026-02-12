const sharedConfig = require("@nox/tailwind-config");

/** @type {import('tailwindcss').Config} */
module.exports = {
    ...sharedConfig,
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
        "../../packages/ui/src/**/*.{ts,tsx}",
    ],
};
