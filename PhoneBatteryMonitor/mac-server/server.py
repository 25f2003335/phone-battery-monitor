from datetime import datetime, timezone
import socket
from flask import Flask, request, jsonify, render_template_string

app = Flask(__name__)
TOKEN = "redmi-local-2026"
latest = None

PAGE = r"""<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Phone Battery Monitor</title>
<style>
:root{color-scheme:dark;--bg:#101114;--panel:#1b1d22;--muted:#969ba7;--text:#f4f5f7;--accent:#a6e3a1}
*{box-sizing:border-box}body{margin:0;background:var(--bg);color:var(--text);font:15px -apple-system,BlinkMacSystemFont,"Segoe UI",sans-serif;min-height:100vh;display:grid;place-items:center;padding:24px}
main{width:min(560px,100%)}.top{display:flex;justify-content:space-between;align-items:center;margin-bottom:22px}.eyebrow{color:var(--muted);font-size:12px;letter-spacing:.12em;text-transform:uppercase}.panel{background:var(--panel);border:1px solid #2a2d34;border-radius:24px;padding:28px}
h1{font-size:22px;margin:5px 0 0}.status{color:var(--muted);font-size:13px}.battery{font-size:76px;font-weight:650;letter-spacing:-5px;margin:22px 0 4px}.bar{height:12px;border-radius:20px;background:#343740;overflow:hidden;margin:16px 0 26px}.fill{height:100%;width:0;background:var(--accent);border-radius:20px;transition:width .4s}
.grid{display:grid;grid-template-columns:1fr 1fr;gap:12px}.item{background:#24272e;border-radius:15px;padding:15px}.label{font-size:12px;color:var(--muted);margin-bottom:7px}.value{font-size:17px;font-weight:600}.foot{color:var(--muted);font-size:12px;margin-top:20px;line-height:1.5}.dot{display:inline-block;width:8px;height:8px;border-radius:50%;background:#777;margin-right:7px}.dot.on{background:var(--accent)}.empty{color:var(--muted);padding:20px 0}
</style>
</head>
<body><main>
<div class="top"><div><div class="eyebrow">Local device monitor</div><h1>Redmi 9 Prime</h1></div><div class="status"><span id="dot" class="dot"></span><span id="connection">Waiting for phone</span></div></div>
<section class="panel">
<div class="eyebrow">Battery level</div>
<div class="battery" id="level">--%</div>
<div class="bar"><div class="fill" id="fill"></div></div>
<div class="grid">
<div class="item"><div class="label">Power</div><div class="value" id="charging">—</div></div>
<div class="item"><div class="label">Temperature</div><div class="value" id="temp">—</div></div>
<div class="item"><div class="label">Voltage</div><div class="value" id="voltage">—</div></div>
<div class="item"><div class="label">Last update</div><div class="value" id="updated">—</div></div>
</div>
<div class="foot">Updates automatically every 5 seconds when the phone is sharing. Keep both devices on the same Wi-Fi network.</div>
</section></main>
<script>
async function refresh(){
 try{
  const r=await fetch('/api/status'); const d=await r.json();
  const online=d.received_at && (Date.now()-new Date(d.received_at).getTime()<150000);
  document.getElementById('dot').className='dot '+(online?'on':'');
  document.getElementById('connection').textContent=online?'Connected':'Waiting for phone';
  if(!d.level && d.level!==0){return}
  document.getElementById('level').textContent=d.level+'%';
  document.getElementById('fill').style.width=d.level+'%';
  document.getElementById('charging').textContent=d.charging?'Charging':(d.plugged?'Plugged in':'Not charging');
  document.getElementById('temp').textContent=d.temperature_c==null?'Unavailable':d.temperature_c.toFixed(1)+' °C';
  document.getElementById('voltage').textContent=d.voltage_v==null?'Unavailable':d.voltage_v.toFixed(2)+' V';
  document.getElementById('updated').textContent=new Date(d.received_at).toLocaleTimeString();
 }catch(e){document.getElementById('connection').textContent='Server unavailable'}
}
refresh();setInterval(refresh,5000);
</script></body></html>"""

@app.get("/")
def home():
    return render_template_string(PAGE)

@app.get("/api/status")
def status():
    return jsonify(latest or {})

@app.post("/api/update")
def update():
    global latest
    if request.headers.get("X-Phone-Token") != TOKEN:
        return jsonify(error="unauthorized"), 401
    data = request.get_json(silent=True) or {}
    level = data.get("level")
    if not isinstance(level, int) or not 0 <= level <= 100:
        return jsonify(error="invalid battery level"), 400
    latest = {
        "level": level,
        "charging": bool(data.get("charging", False)),
        "plugged": bool(data.get("plugged", False)),
        "temperature_c": data.get("temperature_c"),
        "voltage_v": data.get("voltage_v"),
        "received_at": datetime.now(timezone.utc).isoformat()
    }
    return jsonify(ok=True)

def local_ips():
    found = set()
    try:
        for info in socket.getaddrinfo(socket.gethostname(), None, socket.AF_INET):
            ip = info[4][0]
            if not ip.startswith("127."):
                found.add(ip)
    except OSError:
        pass
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        found.add(s.getsockname()[0])
        s.close()
    except OSError:
        pass
    return sorted(found)

if __name__ == "__main__":
    print("\\nPhone Battery Monitor")
    print("On this Mac: http://127.0.0.1:8765")
    for ip in local_ips():
        print(f"On your phone, use Mac IP: {ip}")
    print("Shared token: " + TOKEN)
    print("Keep this Terminal window open.\\n")
    app.run(host="0.0.0.0", port=8765, debug=False)
