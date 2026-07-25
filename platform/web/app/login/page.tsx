"use client";

import { Suspense, useEffect, useRef, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { isStaff } from "@/lib/roles";

type OrgSuggestion = { id: string; name: string; fullName: string };
type Mode = "INSTITUTE" | "EMAIL" | "OTP";

function LoginForm() {
  const router = useRouter();
  const search = useSearchParams();

  const [mode, setMode] = useState<Mode>("INSTITUTE");

  // Institute mode: pick an institute by name (carry its id), then enrollment id.
  const [orgName, setOrgName] = useState("");
  const [orgId, setOrgId] = useState("");
  const [memberId, setMemberId] = useState("");

  // Email mode.
  const [email, setEmail] = useState("");

  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  // OTP mode.
  const [phone, setPhone] = useState("");
  const [otpCode, setOtpCode] = useState("");
  const [otpSent, setOtpSent] = useState(false);
  const [otpInfo, setOtpInfo] = useState("");

  // Autocomplete state.
  const [suggestions, setSuggestions] = useState<OrgSuggestion[]>([]);
  const [showSuggest, setShowSuggest] = useState(false);
  const [instFrozen, setInstFrozen] = useState(false);
  const boxRef = useRef<HTMLDivElement>(null);

  // Deep link: /login?org=<id>&inst=<name> pre-selects and freezes the institute
  // so an org can hand out its own branded login URL (legacy micrositeByOrgId).
  useEffect(() => {
    const oid = search.get("org");
    if (oid) {
      setMode("INSTITUTE");
      setOrgId(oid);
      setOrgName(search.get("inst") || "Your institute");
      setInstFrozen(true);
    }
  }, [search]);

  // Debounced institute name suggestions.
  useEffect(() => {
    if (instFrozen) return;
    const q = orgName.trim();
    if (q.length < 1) {
      setSuggestions([]);
      return;
    }
    const t = setTimeout(async () => {
      try {
        const res = await fetch(`/api/orgs/suggest?q=${encodeURIComponent(q)}`);
        const d = await res.json();
        setSuggestions(d.orgs || []);
      } catch {
        setSuggestions([]);
      }
    }, 200);
    return () => clearTimeout(t);
  }, [orgName, instFrozen]);

  // Close the suggestion dropdown on outside click.
  useEffect(() => {
    function onClick(e: MouseEvent) {
      if (boxRef.current && !boxRef.current.contains(e.target as Node)) setShowSuggest(false);
    }
    document.addEventListener("mousedown", onClick);
    return () => document.removeEventListener("mousedown", onClick);
  }, []);

  function pickOrg(o: OrgSuggestion) {
    setOrgId(o.id);
    setOrgName(o.name);
    setShowSuggest(false);
  }

  function resetInstitute() {
    setInstFrozen(false);
    setOrgId("");
    setOrgName("");
  }

  function finishLogin(data: any) {
    const result = data.result ?? data;
    try {
      sessionStorage.setItem("uprep_session", JSON.stringify(result));
    } catch {}
    const staff = isStaff(result?.profile);
    const next = new URLSearchParams(window.location.search).get("next");
    if (staff) {
      router.push(next && next.startsWith("/cmds") ? next : "/cmds");
    } else {
      router.push("/learn/library");
    }
  }

  async function requestOtp() {
    setError("");
    setOtpInfo("");
    if (phone.replace(/\D/g, "").length < 6) {
      setError("Enter a valid phone number.");
      return;
    }
    setLoading(true);
    try {
      const res = await fetch("/api/auth/otp/request", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ phone, orgId: orgId || undefined }),
      });
      const d = await res.json().catch(() => ({}));
      if (!res.ok) {
        setError(d.error || "Could not send code");
        return;
      }
      setOtpSent(true);
      // Dev/demo convenience when OTP_DEV_ECHO=1 on the server.
      if (d.devCode) setOtpInfo(`Demo code: ${d.devCode}`);
    } catch {
      setError("Network error — please try again");
    } finally {
      setLoading(false);
    }
  }

  async function verifyOtp() {
    setError("");
    if (!otpCode.trim()) {
      setError("Enter the code you received.");
      return;
    }
    setLoading(true);
    try {
      const res = await fetch("/api/auth/otp/verify", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ phone, code: otpCode.trim(), orgId: orgId || undefined }),
      });
      const d = await res.json().catch(() => ({}));
      if (!res.ok || d.errorCode) {
        setError(d.error || d.errorMessage || "Verification failed");
        return;
      }
      finishLogin(d);
    } catch {
      setError("Network error — please try again");
    } finally {
      setLoading(false);
    }
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");

    if (mode === "OTP") {
      if (!otpSent) return requestOtp();
      return verifyOtp();
    }

    // Build the identifier the API expects: "orgId:memberId" for institute
    // login, or a bare email for global login.
    let identifier = "";
    if (mode === "INSTITUTE") {
      if (!memberId.trim()) {
        setError("Please enter your Institute (enrollment) ID.");
        return;
      }
      // Institute is optional: if picked, scope to it (orgId:memberId);
      // otherwise send the bare id and let the server resolve it across orgs
      // (it returns AMBIGUOUS_LOGIN asking for the institute only on a clash).
      identifier = orgId ? `${orgId}:${memberId.trim()}` : memberId.trim();
    } else {
      if (!email.trim()) {
        setError("Please enter your email.");
        return;
      }
      identifier = email.trim();
    }

    setLoading(true);
    try {
      const res = await fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ identifier, password }),
      });
      const data = await res.json();
      if (!res.ok || data.errorCode) {
        setError(data.errorMessage || data.errorCode || "Login failed");
        return;
      }
      finishLogin(data);
    } catch {
      setError("Network error — please try again");
    } finally {
      setLoading(false);
    }
  }

  const tab = (m: Mode, label: string) => (
    <button
      type="button"
      onClick={() => {
        setMode(m);
        setError("");
      }}
      className={`flex-1 rounded-md px-3 py-2 text-sm font-medium transition ${
        mode === m ? "bg-blue-600 text-white shadow" : "bg-slate-100 text-slate-600 hover:bg-slate-200"
      }`}
    >
      {label}
    </button>
  );

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-50 to-slate-200 px-4">
      <div className="w-full max-w-md rounded-2xl bg-white shadow-xl ring-1 ring-black/5 p-8">
        <div className="mb-6 flex items-center gap-2">
          <div className="h-9 w-9 rounded-lg bg-blue-600 flex items-center justify-center text-white font-bold">
            U
          </div>
          <span className="text-xl font-semibold text-slate-800">UPrep</span>
        </div>

        <h1 className="text-lg font-semibold text-slate-700 tracking-wide">ENTER YOUR DETAILS</h1>

        {/* Mode chooser — mirrors legacy "Using Institute ID" / "Using Email ID". */}
        <div className="mt-5 flex gap-2">
          {tab("INSTITUTE", "Institute ID")}
          {tab("EMAIL", "Email ID")}
          {tab("OTP", "Phone OTP")}
        </div>

        <form onSubmit={onSubmit} className="mt-6 space-y-5">
          {mode === "INSTITUTE" ? (
            <>
              {/* Institute name autocomplete -> hidden org id */}
              <div ref={boxRef} className="relative">
                <label className="block text-sm font-medium text-slate-600">
                  Institute Name <span className="text-xs font-normal text-slate-400">(optional)</span>
                </label>
                {instFrozen ? (
                  <div className="mt-1 flex items-center justify-between rounded-md border border-slate-300 bg-slate-50 px-3 py-2">
                    <span className="text-slate-800">{orgName}</span>
                    <button
                      type="button"
                      onClick={resetInstitute}
                      className="text-xs text-blue-600 hover:underline"
                    >
                      Change
                    </button>
                  </div>
                ) : (
                  <>
                    <input
                      type="text"
                      autoComplete="off"
                      placeholder="Optional — type & pick your institute"
                      value={orgName}
                      onChange={(e) => {
                        setOrgName(e.target.value);
                        setOrgId(""); // must re-pick from the list
                        setShowSuggest(true);
                      }}
                      onFocus={() => setShowSuggest(true)}
                      className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                    />
                    {showSuggest && suggestions.length > 0 && (
                      <div className="absolute z-10 mt-1 w-full overflow-hidden rounded-md border border-slate-200 bg-white shadow-lg">
                        {suggestions.map((o) => (
                          <button
                            key={o.id}
                            type="button"
                            onClick={() => pickOrg(o)}
                            className="block w-full px-3 py-2 text-left text-sm hover:bg-blue-50"
                          >
                            <span className="text-slate-800">{o.name}</span>
                            {o.fullName && o.fullName !== o.name && (
                              <span className="ml-1 text-xs text-slate-400">{o.fullName}</span>
                            )}
                          </button>
                        ))}
                      </div>
                    )}
                    <p className="mt-1 text-xs text-slate-400">
                      Leave blank unless your ID exists in more than one institute.
                    </p>
                  </>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-600">
                  Institute ID <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  autoComplete="username"
                  placeholder="Your enrollment / institute ID"
                  value={memberId}
                  onChange={(e) => setMemberId(e.target.value)}
                  className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                />
              </div>
            </>
          ) : mode === "EMAIL" ? (
            <div>
              <label className="block text-sm font-medium text-slate-600">
                Email <span className="text-red-500">*</span>
              </label>
              <input
                type="email"
                autoComplete="username"
                placeholder="you@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
              />
            </div>
          ) : (
            <>
              <div>
                <label className="block text-sm font-medium text-slate-600">
                  Phone Number <span className="text-red-500">*</span>
                </label>
                <div className="mt-1 flex gap-2">
                  <input
                    type="tel"
                    autoComplete="tel"
                    placeholder="+91 90000 00000"
                    value={phone}
                    disabled={otpSent}
                    onChange={(e) => setPhone(e.target.value)}
                    className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 disabled:bg-slate-50"
                  />
                  <button
                    type="button"
                    onClick={requestOtp}
                    disabled={loading}
                    className="whitespace-nowrap rounded-md border border-blue-200 bg-blue-50 px-3 text-sm font-medium text-blue-700 hover:bg-blue-100 disabled:opacity-60"
                  >
                    {otpSent ? "Resend" : "Send code"}
                  </button>
                </div>
              </div>
              {otpSent && (
                <div>
                  <label className="block text-sm font-medium text-slate-600">
                    Verification Code <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    inputMode="numeric"
                    placeholder="6-digit code"
                    value={otpCode}
                    onChange={(e) => setOtpCode(e.target.value)}
                    className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 tracking-widest text-slate-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                  />
                </div>
              )}
              {otpInfo && <p className="text-xs text-emerald-600">{otpInfo}</p>}
            </>
          )}

          {mode !== "OTP" && (
            <>
              <div>
                <label className="block text-sm font-medium text-slate-600">
                  Password <span className="text-red-500">*</span>
                </label>
                <input
                  type={showPassword ? "text" : "password"}
                  required
                  autoComplete="current-password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                />
              </div>

              <div className="flex items-center justify-between text-sm">
                <label className="flex items-center gap-2 text-slate-600 select-none">
                  <input
                    type="checkbox"
                    checked={showPassword}
                    onChange={(e) => setShowPassword(e.target.checked)}
                  />
                  Show Password
                </label>
                <a href="/forgot-password" className="text-slate-500 hover:text-blue-600">
                  Forgot Password?
                </a>
              </div>

              <p className="text-xs text-slate-400">* Required Fields</p>
            </>
          )}

          {error && (
            <div className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 ring-1 ring-red-200">
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full rounded-md bg-blue-600 py-3 font-semibold text-white transition hover:bg-blue-700 disabled:opacity-60"
          >
            {loading
              ? "Please wait…"
              : mode === "OTP"
              ? otpSent
                ? "VERIFY & SIGN IN"
                : "SEND CODE"
              : "LOGIN"}
          </button>
        </form>
      </div>
    </div>
  );
}

export default function LoginPage() {
  return (
    <Suspense fallback={null}>
      <LoginForm />
    </Suspense>
  );
}
