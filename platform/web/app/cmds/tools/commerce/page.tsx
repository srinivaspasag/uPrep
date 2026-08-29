"use client";

import { useEffect, useState } from "react";
import CmdsShell from "@/components/CmdsShell";

type Tab = "products" | "coupons" | "invoices";
type Course = { id: string; name: string };
type Product = { id: string; courseId: string; name: string; priceCents: number; currency: string };
type Coupon = {
  id: string;
  code: string;
  percentOff: number | null;
  amountOffCents: number | null;
  active: boolean;
  maxRedemptions: number | null;
  validUntil: string | null;
  isExpired?: boolean;
  redeemed: number;
};
type Invoice = {
  id: string;
  number: string;
  buyerName: string;
  courseName: string;
  amount: string;
  status: string;
  couponCode: string | null;
  gateway: string;
  createdAt: number;
};

export default function CommercePage() {
  const [tab, setTab] = useState<Tab>("products");

  return (
    <CmdsShell>
      <div className="mx-auto max-w-[1000px] px-8 py-6">
        <h1 className="text-2xl font-light text-slate-700">Payments & Coupons</h1>
        <p className="mt-1 text-sm text-slate-500">
          Sell courses, issue discount coupons, and reconcile invoices. Payment mode is configurable
          (manual confirmation by default; connect a gateway via env).
        </p>

        <div className="mt-5 flex gap-2">
          {(["products", "coupons", "invoices"] as Tab[]).map((t) => (
            <button
              key={t}
              onClick={() => setTab(t)}
              className={`rounded-full px-3 py-1 text-xs font-medium capitalize ${
                tab === t ? "bg-slate-800 text-white" : "bg-slate-100 text-slate-600 hover:bg-slate-200"
              }`}
            >
              {t}
            </button>
          ))}
        </div>

        <div className="mt-5">
          {tab === "products" && <Products />}
          {tab === "coupons" && <Coupons />}
          {tab === "invoices" && <Invoices />}
        </div>
      </div>
    </CmdsShell>
  );
}

function Products() {
  const [products, setProducts] = useState<Product[]>([]);
  const [courses, setCourses] = useState<Course[]>([]);
  const [courseId, setCourseId] = useState("");
  const [name, setName] = useState("");
  const [price, setPrice] = useState("");
  const [error, setError] = useState("");

  async function load() {
    const d = await fetch("/api/cmds/tools/commerce/products").then((r) => r.json());
    setProducts(d.products || []);
    setCourses(d.courses || []);
  }
  useEffect(() => {
    load();
  }, []);

  async function add() {
    setError("");
    const res = await fetch("/api/cmds/tools/commerce/products", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ courseId, name, price: Number(price) }),
    });
    const d = await res.json().catch(() => ({}));
    if (!res.ok) return setError(d.error || "Failed");
    setName("");
    setPrice("");
    setCourseId("");
    load();
  }

  async function remove(id: string) {
    await fetch(`/api/cmds/tools/commerce/products?id=${id}`, { method: "DELETE" });
    load();
  }

  return (
    <div>
      <div className="flex flex-wrap items-end gap-2 rounded border border-slate-200 bg-slate-50 p-3">
        <label className="text-sm">
          <span className="mb-1 block text-slate-600">Course</span>
          <select
            value={courseId}
            onChange={(e) => setCourseId(e.target.value)}
            className="w-56 rounded border border-slate-300 px-2 py-1.5 text-sm"
          >
            <option value="">Select…</option>
            {courses.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </label>
        <label className="text-sm">
          <span className="mb-1 block text-slate-600">Product name</span>
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="w-56 rounded border border-slate-300 px-2 py-1.5 text-sm"
          />
        </label>
        <label className="text-sm">
          <span className="mb-1 block text-slate-600">Price (₹)</span>
          <input
            type="number"
            value={price}
            onChange={(e) => setPrice(e.target.value)}
            className="w-28 rounded border border-slate-300 px-2 py-1.5 text-sm"
          />
        </label>
        <button
          onClick={add}
          className="rounded bg-emerald-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-emerald-700"
        >
          Add product
        </button>
      </div>
      {error && <div className="mt-2 text-sm text-red-600">{error}</div>}

      <table className="mt-4 w-full text-sm">
        <thead>
          <tr className="border-b border-slate-200 text-left text-xs uppercase text-slate-500">
            <th className="px-3 py-2">Product</th>
            <th className="px-3 py-2">Price</th>
            <th className="px-3 py-2"></th>
          </tr>
        </thead>
        <tbody>
          {products.map((p) => (
            <tr key={p.id} className="border-b border-slate-100">
              <td className="px-3 py-2 text-slate-700">{p.name}</td>
              <td className="px-3 py-2 text-slate-600">
                ₹{(p.priceCents / 100).toFixed(2)}
              </td>
              <td className="px-3 py-2 text-right">
                <button onClick={() => remove(p.id)} className="text-xs text-red-500 hover:underline">
                  Delete
                </button>
              </td>
            </tr>
          ))}
          {products.length === 0 && (
            <tr>
              <td colSpan={3} className="px-3 py-8 text-center text-slate-400">
                No products yet.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

function Coupons() {
  const [coupons, setCoupons] = useState<Coupon[]>([]);
  const [code, setCode] = useState("");
  const [percentOff, setPercentOff] = useState("");
  const [amountOff, setAmountOff] = useState("");
  const [validUntil, setValidUntil] = useState("");
  const [error, setError] = useState("");

  async function load() {
    const d = await fetch("/api/cmds/tools/commerce/coupons").then((r) => r.json());
    setCoupons(d.coupons || []);
  }
  useEffect(() => {
    load();
  }, []);

  async function add() {
    setError("");
    const res = await fetch("/api/cmds/tools/commerce/coupons", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        code,
        percentOff: percentOff ? Number(percentOff) : undefined,
        amountOff: amountOff ? Number(amountOff) : undefined,
        validUntil: validUntil || undefined,
      }),
    });
    const d = await res.json().catch(() => ({}));
    if (!res.ok) return setError(d.error || "Failed");
    setCode("");
    setPercentOff("");
    setAmountOff("");
    setValidUntil("");
    load();
  }

  async function remove(id: string) {
    await fetch(`/api/cmds/tools/commerce/coupons?id=${id}`, { method: "DELETE" });
    load();
  }

  return (
    <div>
      <div className="flex flex-wrap items-end gap-2 rounded border border-slate-200 bg-slate-50 p-3">
        <label className="text-sm">
          <span className="mb-1 block text-slate-600">Code</span>
          <input
            value={code}
            onChange={(e) => setCode(e.target.value.toUpperCase())}
            placeholder="e.g. SUMMER50"
            className="w-36 rounded border border-slate-300 px-2 py-1.5 font-mono text-sm uppercase"
          />
        </label>
        <label className="text-sm">
          <span className="mb-1 block text-slate-600">% off</span>
          <input
            type="number"
            value={percentOff}
            onChange={(e) => setPercentOff(e.target.value)}
            className="w-20 rounded border border-slate-300 px-2 py-1.5 text-sm"
          />
        </label>
        <label className="text-sm">
          <span className="mb-1 block text-slate-600">or ₹ off</span>
          <input
            type="number"
            value={amountOff}
            onChange={(e) => setAmountOff(e.target.value)}
            className="w-20 rounded border border-slate-300 px-2 py-1.5 text-sm"
          />
        </label>
        <label className="text-sm">
          <span className="mb-1 block text-slate-600">Valid until</span>
          <input
            type="date"
            value={validUntil}
            onChange={(e) => setValidUntil(e.target.value)}
            className="w-36 rounded border border-slate-300 px-2 py-1.5 text-sm"
          />
        </label>
        <button
          onClick={add}
          className="rounded bg-emerald-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-emerald-700"
        >
          Create coupon
        </button>
      </div>
      {error && <div className="mt-2 text-sm text-red-600">{error}</div>}

      <table className="mt-4 w-full text-sm">
        <thead>
          <tr className="border-b border-slate-200 text-left text-xs uppercase text-slate-500">
            <th className="px-3 py-2">Code</th>
            <th className="px-3 py-2">Discount</th>
            <th className="px-3 py-2">Validity</th>
            <th className="px-3 py-2">Redeemed</th>
            <th className="px-3 py-2"></th>
          </tr>
        </thead>
        <tbody>
          {coupons.map((c) => {
            const isExpired =
              c.isExpired || (c.validUntil ? new Date(c.validUntil).getTime() < Date.now() : false);
            return (
              <tr
                key={c.id}
                className={`border-b border-slate-100 ${
                  !c.active || isExpired ? "bg-slate-50/50 opacity-75" : ""
                }`}
              >
                <td className="px-3 py-2 font-mono text-slate-700">
                  <span>{c.code}</span>
                  {isExpired ? (
                    <span className="ml-2 inline-block rounded bg-red-100 px-1.5 py-0.5 text-[10px] font-semibold text-red-700">
                      EXPIRED
                    </span>
                  ) : !c.active ? (
                    <span className="ml-2 inline-block rounded bg-slate-200 px-1.5 py-0.5 text-[10px] font-semibold text-slate-600">
                      DISABLED
                    </span>
                  ) : (
                    <span className="ml-2 inline-block rounded bg-emerald-100 px-1.5 py-0.5 text-[10px] font-semibold text-emerald-700">
                      ACTIVE
                    </span>
                  )}
                </td>
                <td className="px-3 py-2 text-slate-600">
                  {c.percentOff ? `${c.percentOff}%` : `₹${((c.amountOffCents || 0) / 100).toFixed(2)}`}
                </td>
                <td className="px-3 py-2 text-slate-600">
                  {c.validUntil ? (
                    <span className={isExpired ? "text-red-500 line-through" : "text-slate-700 font-medium"}>
                      {new Date(c.validUntil).toLocaleDateString(undefined, {
                        year: "numeric",
                        month: "short",
                        day: "numeric",
                      })}
                    </span>
                  ) : (
                    <span className="text-xs text-slate-400">Lifetime</span>
                  )}
                </td>
                <td className="px-3 py-2 text-slate-600">{c.redeemed}</td>
                <td className="px-3 py-2 text-right">
                  {c.active && (
                    <button onClick={() => remove(c.id)} className="text-xs text-red-500 hover:underline">
                      Disable
                    </button>
                  )}
                </td>
              </tr>
            );
          })}
          {coupons.length === 0 && (
            <tr>
              <td colSpan={5} className="px-3 py-8 text-center text-slate-400">
                No coupons yet.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

function Invoices() {
  const [invoices, setInvoices] = useState<Invoice[]>([]);

  async function load() {
    const d = await fetch("/api/cmds/tools/commerce/invoices").then((r) => r.json());
    setInvoices(d.invoices || []);
  }
  useEffect(() => {
    load();
  }, []);

  async function act(id: string, action: "markPaid" | "cancel") {
    await fetch("/api/cmds/tools/commerce/invoices", {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ id, action }),
    });
    load();
  }

  return (
    <table className="w-full text-sm">
      <thead>
        <tr className="border-b border-slate-200 text-left text-xs uppercase text-slate-500">
          <th className="px-3 py-2">Invoice</th>
          <th className="px-3 py-2">Buyer</th>
          <th className="px-3 py-2">Course</th>
          <th className="px-3 py-2">Amount</th>
          <th className="px-3 py-2">Status</th>
          <th className="px-3 py-2"></th>
        </tr>
      </thead>
      <tbody>
        {invoices.map((i) => (
          <tr key={i.id} className="border-b border-slate-100">
            <td className="px-3 py-2 font-mono text-xs text-slate-500">{i.number}</td>
            <td className="px-3 py-2 text-slate-700">{i.buyerName}</td>
            <td className="px-3 py-2 text-slate-600">{i.courseName}</td>
            <td className="px-3 py-2 text-slate-600">
              {i.amount}
              {i.couponCode && <span className="ml-1 text-xs text-indigo-500">({i.couponCode})</span>}
            </td>
            <td className="px-3 py-2">
              <span
                className={`rounded-full px-2 py-0.5 text-xs ${
                  i.status === "PAID"
                    ? "bg-emerald-100 text-emerald-700"
                    : i.status === "PENDING"
                    ? "bg-amber-100 text-amber-700"
                    : "bg-slate-100 text-slate-500"
                }`}
              >
                {i.status}
              </span>
            </td>
            <td className="px-3 py-2 text-right">
              {i.status === "PENDING" && (
                <div className="flex justify-end gap-2">
                  <button
                    onClick={() => act(i.id, "markPaid")}
                    className="text-xs text-emerald-600 hover:underline"
                  >
                    Mark paid
                  </button>
                  <button
                    onClick={() => act(i.id, "cancel")}
                    className="text-xs text-red-500 hover:underline"
                  >
                    Cancel
                  </button>
                </div>
              )}
            </td>
          </tr>
        ))}
        {invoices.length === 0 && (
          <tr>
            <td colSpan={6} className="px-3 py-8 text-center text-slate-400">
              No invoices yet.
            </td>
          </tr>
        )}
      </tbody>
    </table>
  );
}
