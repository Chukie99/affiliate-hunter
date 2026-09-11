/** @type {import("next").NextConfig} */
const nextConfig = { images: { remotePatterns: [{ protocol: "https", hostname: "down-id.img.susercontent.com" }] } };
module.exports = nextConfig;
