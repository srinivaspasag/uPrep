import { redirect } from "next/navigation";

// Legacy pre-shell dashboard is retired — the real LMS lives under /learn/*.
export default function DashboardRedirect() {
  redirect("/learn/library");
}
