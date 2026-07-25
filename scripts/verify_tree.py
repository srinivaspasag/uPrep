import os, requests
BASE = os.environ.get("UPREP_BASE", "https://65.2.108.70.sslip.io")
s = requests.Session()
s.post(f"{BASE}/api/auth/login",
       json={"identifier": os.environ["UPREP_USER"], "password": os.environ["UPREP_PASS"]}, timeout=30)

def kids(pid):
    p = {"parentId": pid} if pid else {}
    return s.get(f"{BASE}/api/cmds/content", params=p, timeout=30).json().get("resources", [])

sheet = os.environ.get("SHEET", "Physics XI")
root = next((r for r in kids(None) if r["type"] == "FOLDER" and r["title"] == sheet), None)
print(sheet, "->", root and root["id"])
for ch in [r for r in kids(root["id"]) if r["type"] == "FOLDER"]:
    print(f"  Chapter: {ch['title']}")
    for node in kids(ch["id"]):
        if node["type"] == "FOLDER":
            vids = [x for x in kids(node["id"]) if x["type"] == "VIDEO"]
            print(f"    Session: {node['title']}  ({len(vids)} videos)")
            for v in vids[:2]:
                print(f"        - {v['title']}  {v.get('url')}")
        elif node["type"] == "VIDEO":
            print(f"    (loose video) {node['title']}")
