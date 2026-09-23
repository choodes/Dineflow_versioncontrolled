# QR Restaurant Ordering System — Working Demo

A complete, runnable implementation of the workflow diagram: customer QR
ordering, kitchen display, waiter panel, and manager dashboard, backed by a
plain Spring Boot + H2 in-memory database. No external database, Docker, or
API keys required to run it locally.

## Important — please read before running

This project was written and carefully cross-checked (entity fields vs.
repository query methods, every frontend `fetch()` call vs. every backend
endpoint) in an environment **without internet access to Maven Central**, so
it could not actually be compiled or run here. Everything has been checked
by hand for consistency, but there is a real chance a small compile error
slipped through. If `mvn spring-boot:run` fails, copy the exact error here
and it can be fixed immediately.

## Requirements

- Java 17 or newer (`java -version`)
- Maven 3.8+ (`mvn -version`) — or use your IDE's built-in Maven support
- Internet access on first run (Maven needs to download Spring Boot's
  dependencies once; they're cached afterwards)

## Staff login (new)

Kitchen, Waiter, and Manager dashboards now require login — only the
Customer pages stay open, since a real diner should never need an account.
Three fixed demo accounts (change these in `SecurityConfig.java` for
anything beyond a demo):

| Role | Username | Password | Can also access |
|---|---|---|---|
| Kitchen | `kitchen` | `kitchen123` | — |
| Waiter | `waiter` | `waiter123` | — |
| Manager | `manager` | `manager123` | Kitchen + Waiter views too |

Logging in uses Spring Security's default login form — just open
`/kitchen.html`, `/waiter.html`, or `/manager.html` and you'll be redirected
to log in automatically. A **Logout** link is in the header of each
dashboard.

## How to run

```bash
cd qr-ordering-system
mvn spring-boot:run
```

Then open **http://localhost:8080** — this is the landing page linking to
all four interfaces:

| Interface | URL | Role |
|---|---|---|
| Customer | `/customer.html` | Scan a table, order, pay, call waiter |
| Kitchen Display | `/kitchen.html` | Accept/reject orders, mark ready |
| Waiter Panel | `/waiter.html` | Deliver food, handle calls, collect payment |
| Manager Dashboard | `/manager.html` | Edit menu, view bills, override sessions, disputes |

The database is **in-memory** (H2) — all data resets every time you restart
the app. Six demo tables and a full menu (with starter/dessert pairings) are
seeded automatically on first run. You can inspect the live database at
`http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:qrordering`,
user `sa`, no password).

## Trying the full flow

1. Open **Customer** in one tab, pick a table.
2. Add a main dish — a recommendation popup offers a starter/dessert pairing.
3. Place the order.
4. Open **Kitchen Display** in a second tab — the order appears, tagged
   `PLACED`. Accept it (or reject it, to see the customer's Reorder button),
   then mark it Ready.
5. Open **Waiter Panel** in a third tab — the ready order appears; mark it
   Served.
6. Back in Customer, after the first order you'll be asked **Pay Now or Pay
   Later**. Try Pay Later, keep ordering, then tap **Request Bill** and
   choose UPI/Card/Cash.
7. If Card or Cash was chosen, a **payment-collect call** appears in the
   Waiter Panel — click **Mark Paid & Close Bill** to finish the session.
8. Open **Manager Dashboard** any time to edit the menu, see every open
   session's running bill, force-close a session (walkout write-off), or
   resolve a dispute.

### Testing real QR scanning

The **Manager Dashboard → Tables / QR Codes** tab shows a real, scannable QR
code per table (generated server-side with ZXing) that encodes that table's
live ordering URL. Print that page, or just point a phone camera at the
screen — most phone camera apps decode QR codes natively and will offer to
open the link directly, no extra app needed.

For testing without a printer, the landing page (`/`) also has a **"Scan a
Table's QR Code"** button that opens your device's camera right in the
browser (using the `jsQR` library) and redirects automatically once it reads
a code. Camera access needs a secure context, so this works on `localhost`
out of the box; if you open the app via your machine's LAN IP from a phone
instead, most browsers will block camera access over plain HTTP.

### Mobile number capture

Right after a session is created (first scan), the customer is asked for a
10-digit mobile number before the menu unlocks — this happens once per
session, not on every rescan. It's validated for format only (`SessionService.setCustomerPhone`)
with no real OTP/SMS gateway wired in. The number appears in the Manager
Dashboard's Sessions/Bills table.

### Mobile number + party size, and picking up the bill at the counter

The same gate now also asks **number of members**, not just phone number.
Both are shown to staff on the Waiter Panel's Tables Overview and Manager
Dashboard's Sessions tab. A customer can tell counter staff their phone
number at the end of the meal, and staff can look their bill up instantly
via **Waiter Panel → "Find a Bill by Phone Number"**
(`GET /api/waiter/sessions/search?phone=...`) instead of hunting for the
right table/session manually.

### Suspicious session flag (25+ minutes, no order, unpaid)

The **Waiter Panel → Tables Overview** table highlights any open session in
**red** with a ⚠ flag if it's been open 25+ minutes, has zero orders placed,
and isn't marked paid — a session that was started (QR scanned, phone/party
size given) but never actually used, which is worth a staff member walking
over to check on. This is computed live on each request
(`WaiterController.toOverview`), not stored, and is intentionally a
*different* signal from the 20-minute "regular check" scheduler: that one
covers a pay-later session that *did* order and then went quiet; this one
covers a session that never ordered at all. Lower the
`SUSPICIOUS_THRESHOLD_MINUTES` constant in `WaiterController.java` for a
faster demo.

### Testing "different devices, same table, separate bills"

Per your workflow note, scanning the same table's QR from different devices
creates **independent sessions/bills**, not a shared table tab. To simulate
this in one browser: open Customer, pick a table, then click **"Start as new
device"** — this clears that table's saved session token so your next scan
creates a brand-new bill, exactly as a second customer's phone would.

### Testing the 20-minute regular check

The `RegularCheckScheduler` runs every 60 seconds and flags any `ACTIVE`
Pay-Later session with no new order in the last 20 minutes, per the
workflow's sticky note. For a faster demo, lower the threshold temporarily
in `RegularCheckScheduler.java` (`IDLE_THRESHOLD_MINUTES`) to something
like `1`.

## What's simulated vs. what's real integration-ready

| Feature | Status |
|---|---|
| QR scanning | **Real** — each table has an actual scannable QR code (ZXing-generated PNG, served from `/api/manager/tables/{id}/qrcode.png`) encoding its live ordering URL; a phone's native camera app opens it directly. An in-browser camera scanner (jsQR) is also included for testing without printing. |
| Mobile number capture | **Real** — captured once per session right after scan, format-validated server-side, shown to staff on the Manager Dashboard. No OTP/SMS gateway (would need a provider like Twilio/MSG91 wired into `SessionService.setCustomerPhone`). |
| UPI payment | **Mocked** — instantly "succeeds". Real integration point is marked in `PaymentService.java`: replace with the Razorpay Orders API + webhook signature verification |
| Card pre-authorization | Not implemented in this demo (was discussed as a future walkout-fraud mitigation) — would plug into `Session` via the `preauth_*` fields discussed earlier |
| Multi-tenant / multiple restaurants | Deliberately out of scope per your instruction — this is single-restaurant |
| E-ink QR rotation | Out of scope for this software demo — the backend's `qrToken`-to-`table` resolution already supports rotating tokens without any other code changes, since only the QR image itself would need to change on real hardware |

## Project structure

```
qr-ordering-system/
  pom.xml
  src/main/java/com/qrorder/
    QrOrderingApplication.java
    model/       entities + enums (Session, Order, MenuItem, WaiterCall, Dispute, ...)
    repo/        Spring Data JPA repositories
    service/     business logic (SessionService, OrderService, BillingService,
                 PaymentService, WaiterCallService, DisputeService,
                 RegularCheckScheduler)
    web/         REST controllers (Public*, Kitchen, Waiter, Manager)
    config/      DataSeeder (demo tables + menu)
  src/main/resources/
    application.properties
    static/      customer.html, kitchen.html, waiter.html, manager.html, index.html
```

## Design notes carried over from planning

- **Session = one bill.** Each device scanning a table's QR gets its own
  session unless it already holds a valid token for that table
  (`SessionService.getOrCreateSession`).
- **Kitchen never sees an order it hasn't been notified of, and the
  customer never sees a kitchen-only status** — the accept/reject/reorder
  loop matches the diagram's exact branching.
- **Pay Now vs Pay Later** is asked once, right after the first order is
  placed, and doesn't block further ordering either way.
- **Every payment method funnels into a waiter call** before a session
  closes — UPI still notifies the waiter to confirm/clear the table, matching
  the diagram rather than auto-closing on payment alone.
- **Rejected orders never count toward the bill** (`BillingService`
  filters them out), so a rejected order won't accidentally get charged if
  the customer doesn't reorder it.
#   D i n e f l o w _ v e r s i o n c o n t r o l l e d  
 