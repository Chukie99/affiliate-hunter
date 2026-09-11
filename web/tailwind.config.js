/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./app/**/*.{js,ts,jsx,tsx}", "./components/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      colors: {
        primary: "#3368A0",
        mid: "#66A3BF",
        mint: "#C8DFDB",
        warm: "#F2EFE7",
        ink: "#25324A",
        muted: "#6B7D94",
        line: "#D6E2E0",
        ok: "#2E7D6B",
        danger: "#C25A4A",
        warn: "#8C5A18",
      },
      borderRadius: { xl: "16px", "2xl": "20px" }
    }
  },
  plugins: []
}
