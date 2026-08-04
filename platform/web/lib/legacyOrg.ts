import { API, CALLING_APP, CALLING_APP_ID } from "./config";

// Calls the LIVE legacy org-services backend (Play, http://lmsbe:19012 in
// production). This is the real, already-deployed source of truth for
// org/department/program/center/section/member data — see
// legacy/lms-master/organization/organization-services/app/controllers/
// {Organizations,Members,ActivityLogger}.java. Always returns `result`
// unwrapped from the legacy {result, errorCode, errorMessage} envelope, and
// throws on any errorCode (VedantuErrorCode name, e.g. INVALID_ID).
//
// List<String> fields (e.g. centerIds) must be form-encoded as indexed keys
// (`centerIds[0]=..&centerIds[1]=..`) to bind to Play's classic form binder —
// NOT as a JSON string. Nested beans (e.g. socialMedia: SocialInfo) and
// lists of beans (e.g. appInfos: List<AppInfo>) follow the same binder's
// dot-notation convention: `socialMedia.facebook=..`, `appInfos[0].type=..`.
function setFormField(form: URLSearchParams, key: string, value: unknown) {
  if (value === undefined || value === null || value === "") return;
  if (Array.isArray(value)) {
    value.forEach((item, i) => {
      if (item !== null && typeof item === "object") {
        for (const [k, v] of Object.entries(item)) setFormField(form, `${key}[${i}].${k}`, v);
      } else {
        form.set(`${key}[${i}]`, String(item));
      }
    });
  } else if (typeof value === "object") {
    for (const [k, v] of Object.entries(value as Record<string, unknown>)) {
      setFormField(form, `${key}.${k}`, v);
    }
  } else {
    form.set(key, String(value));
  }
}

async function callLegacy<T = any>(
  base: string,
  path: string,
  action: string,
  params: Record<string, unknown>
): Promise<T> {
  const form = new URLSearchParams({
    callingApp: CALLING_APP,
    callingAppId: CALLING_APP_ID,
  });
  for (const [key, value] of Object.entries(params)) {
    setFormField(form, key, value);
  }
  const resp = await fetch(`${base}${path}/${action}`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: form,
  });
  const json = await resp.json().catch(() => ({}));
  if (json.errorCode) {
    throw new Error(json.errorMessage || json.errorCode);
  }
  return (json.result ?? {}) as T;
}

export function callOrgService<T = any>(
  action: string,
  params: Record<string, unknown>
): Promise<T> {
  return callLegacy<T>(API.org, "/organizations", action, params);
}

export function callMembersService<T = any>(
  action: string,
  params: Record<string, unknown>
): Promise<T> {
  return callLegacy<T>(API.org, "/members", action, params);
}

export function callActivityLogger<T = any>(
  action: string,
  params: Record<string, unknown>
): Promise<T> {
  return callLegacy<T>(API.org, "/activityLogger", action, params);
}
