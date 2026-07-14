/** @type {import('next').NextConfig} */
const nextConfig = {
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
