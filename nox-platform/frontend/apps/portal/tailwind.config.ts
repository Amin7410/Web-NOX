import type { Config } from "tailwindcss";
import sharedConfig from "@nox/tailwind-config";

const config: Config = {
    ...sharedConfig,
    content: [
        "./app/**/*.{js,ts,jsx,tsx,mdx}",
        "../../packages/ui/src/**/*.{ts,tsx}", // Important: Include UI package
    ],
};
export default config;
