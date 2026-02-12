const sharedConfig = require("@nox/tailwind-config");

/** @type {import('tailwindcss').Config} */
module.exports = {
    ...sharedConfig,
    content: ["./src/**/*.{ts,tsx}"],
};
