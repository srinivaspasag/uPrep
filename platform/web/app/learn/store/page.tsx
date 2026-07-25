"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import LmsShell, { ZeroState } from "@/components/LmsShell";

type Product = {
  id: string;
  courseId: string;
  name: string;
  price: string;
  priceCents: number;
  owned: boolean;
};

export default function StorePage() {
  const router = useRouter();
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [buying, setBuying] = useState<string | null>(null);
  const [coupon, setCoupon] = useState("");
  const [msg, setMsg] = useState("");

  async function load() {
    setLoading(true);
    try {
      const d = await fetch("/api/learn/checkout").then((r) => r.json());
      setProducts(d.products || []);
    } finally {
      setLoading(false);
    }
  }
  useEffect(() => {
    load();
  }, []);

  async function buy(p: Product) {
    setMsg("");
    setBuying(p.id);
    try {
      const res = await fetch("/api/learn/checkout", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ productId: p.id, couponCode: coupon.trim() || undefined }),
      });
      const d = await res.json().catch(() => ({}));
      if (!res.ok) {
        setMsg(d.error || "Checkout failed");
        return;
      }
      if (d.checkoutUrl) {
        window.location.href = d.checkoutUrl;
        return;
      }
      if (d.status === "PAID") {
        setMsg("Enrolled! Redirecting to your courses…");
        setTimeout(() => router.push("/learn/courses"), 900);
      } else {
        setMsg(
          `Order ${d.number} created (${d.amount}). Payment is pending confirmation by your institute.`
        );
        load();
      }
    } finally {
      setBuying(null);
    }
  }

  return (
    <LmsShell active="courses">
      <h1 className="text-xl font-semibold text-slate-800">Store</h1>
      <p className="mt-1 text-sm text-slate-500">Buy access to additional courses.</p>

      <div className="mt-4 flex items-center gap-2">
        <input
          value={coupon}
          onChange={(e) => setCoupon(e.target.value.toUpperCase())}
          placeholder="Coupon code (optional)"
          className="w-56 rounded border border-slate-300 px-3 py-1.5 font-mono text-sm outline-none focus:border-emerald-500"
        />
        {msg && <span className="text-sm text-slate-500">{msg}</span>}
      </div>

      <div className="mt-6">
        {loading ? (
          <div className="py-16 text-center text-slate-400">Loading…</div>
        ) : products.length === 0 ? (
          <ZeroState img="/legacy/zero/general-no-content.jpg">
            Nothing for sale right now.
          </ZeroState>
        ) : (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {products.map((p) => (
              <div key={p.id} className="rounded-lg border border-slate-200 bg-white p-5">
                <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-emerald-50 text-lg">
                  🛒
                </div>
                <div className="mt-3 font-semibold text-slate-800">{p.name}</div>
                <div className="mt-1 text-lg font-bold text-slate-700">{p.price}</div>
                {p.owned ? (
                  <div className="mt-3 rounded-md bg-slate-100 px-3 py-1.5 text-center text-xs text-slate-500">
                    Already enrolled
                  </div>
                ) : (
                  <button
                    onClick={() => buy(p)}
                    disabled={buying === p.id}
                    className="mt-3 w-full rounded-md bg-emerald-600 px-4 py-1.5 text-sm font-semibold text-white hover:bg-emerald-700 disabled:opacity-60"
                  >
                    {buying === p.id ? "Processing…" : "Buy now"}
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </LmsShell>
  );
}
