# Wave 13 — Groups as real groups

Date: 2026-10-24  
Status: **Done**

## Scope

| # | Feature | Status |
|---|---------|--------|
| 13.1 | Add / remove members | Done |
| 13.2 | Promote / demote admin | Done |
| 13.3 | Edit title / description (+ avatar API) | Done |
| 13.4 | Leave group / dissolve (admin) | Done |
| 13.5 | Only admins can send / edit info | Done |

## Backend

- Schema: `Conversation.onlyAdminsCanSend`, `onlyAdminsCanEditInfo`
- Routes on `/api/conversations/:id/...`: members DELETE, members PUT role, info PUT, avatar POST, privacy PUT, leave POST, DELETE dissolve
- Message send returns `403 admins_only` when flag set and caller is not admin
- Tests: `conversations.wave13.test.ts`

## Android

- `LocalChat` carries role + privacy flags
- `GroupInfoScreen` (via Group info / ProfileId when `isGroup`)
- Composer shows “Only admins can send messages” when restricted

## Exit checks

- [ ] Admin adds/removes member → both devices update after refresh
- [ ] Promote/demote reflected in Group info
- [ ] Rename / description visible to members
- [ ] Leave removes chat from list; dissolve deletes for all
- [ ] Only-admins send blocks non-admin composer + API
