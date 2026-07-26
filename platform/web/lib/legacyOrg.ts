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
// NOT as a JSON string.
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
    if (value === undefined || value === null || value === "") continue;
    if (Array.isArray(value)) {
      value.forEach((item, i) => form.set(`${key}[${i}]`, String(item)));
    } else {
      form.set(key, String(value));
    }
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
