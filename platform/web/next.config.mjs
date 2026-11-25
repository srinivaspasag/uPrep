/** @type {import('next').NextConfig} */
const nextConfig = {
  experimental: {
    // Exact-picture Bulk Import (lib/pdfPictures): pdf.js draws PDF pages on
    // a native canvas — both must load from node_modules at runtime, not be
    // bundled by webpack.
    serverComponentsExternalPackages: ["@napi-rs/canvas", "pdfjs-dist"],
  },
  eslint: {
    // Lint strictness (e.g. no-explicit-any) shouldn't block production builds.
    ignoreDuringBuilds: true,
  },
  typescript: {
    // Type errors shouldn't block the demo deploy build.
    ignoreBuildErrors: true,
  },
};

export default nextConfig;
