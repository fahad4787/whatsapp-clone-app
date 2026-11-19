# Wave 55 — short checklist

Full list: [TEST_CHECKLIST.md](TEST_CHECKLIST.md) § BB

1. No SMTP env → email verify still works with `000000` / server log code  
2. Set SMTP_* → request code → inbox receives mail; `000000` rejected  
3. Bad SMTP credentials → client gets send failure (502), not a silent success  
4. Settings → Email → after send with SMTP live → “Check your inbox…” (no staging bypass line)  
