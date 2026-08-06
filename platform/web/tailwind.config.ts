import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./components/**/*.{js,ts,jsx,tsx,mdx}",
    "./app/**/*.{js,ts,jsx,tsx,mdx}",
    // Bug found live: color classes referenced only from plain .ts modules
    // (e.g. lib/subjectColors.ts's per-subject palette) were never scanned,
    // so Tailwind's JIT silently dropped them from the generated CSS —
    // Chemistry (teal) and Maths (violet) subject cards rendered with no
    // header color/text at all, while Physics/Botany/Zoology "worked" only
    // because those same color classes happened to already be used
    // elsewhere in app/components.
    "./lib/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        background: "var(--background)",
        foreground: "var(--foreground)",
      },
    },
  },
  plugins: [],
};
export default config;
