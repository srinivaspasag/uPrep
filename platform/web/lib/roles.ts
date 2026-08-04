// Role model — mirrors the legacy CMDS access rule (Security.checkAccess):
// only staff profiles may enter the institute console; STUDENT / OFFLINE_USER
// are redirected to the learn app. Kept dependency-free so it can be imported
// from client components, server routes, and edge middleware alike.

export const STAFF_PROFILES = ["MANAGER", "TEACHER", "EDITOR", "SALESPERSON"] as const;

const STAFF_SET = new Set<string>(STAFF_PROFILES);

// True if the profile may access CMDS (institute/admin console).
export function isStaff(profile: string | null | undefined): boolean {
  return !!profile && STAFF_SET.has(profile.trim().toUpperCase());
}

// Content/resources management — legacy explicitly excludes SALESPERSON
// from content views (orgUserProfile != "SALESPERSON" checks in
// QrResources/QrPrograms templates); the other three staff profiles are
// not otherwise differentiated for content access in legacy.
const CONTENT_MANAGER_PROFILES = ["MANAGER", "TEACHER", "EDITOR"] as const;
const CONTENT_MANAGER_SET = new Set<string>(CONTENT_MANAGER_PROFILES);

export function canManageContent(profile: string | null | undefined): boolean {
  return !!profile && CONTENT_MANAGER_SET.has(profile.trim().toUpperCase());
}

// Super admin = MANAGER + isSuperAdmin flag (legacy Widgets._amISuperAdmin).
export function isSuperAdmin(profile: string | null | undefined, superFlag: boolean | undefined): boolean {
  return !!superFlag && (profile || "").trim().toUpperCase() === "MANAGER";
}

export const SESSION_COOKIE = "uprep_auth";
