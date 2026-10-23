# Wave 9 — Receipts & delete

## Backend

| Method | Path | Notes |
|--------|------|--------|
| GET | `/api/conversations/:id/messages` | Hides delete-for-me + cleared; marks peer SENT→DELIVERED |
| POST | `/api/conversations/:id/read` | Peer msgs → READ + WS `receipt` with `message_ids` |
| POST | `/api/messages/delivered` | `{ message_ids: [] }` |
| DELETE | `/api/messages/:id?for=me\|everyone` | Hide for me / tombstone for everyone |
| POST | `/api/conversations/:id/clear` | Sets `ConversationMember.clearedAt` |

Schema: `Message.deletedForEveryoneAt`, `MessageHide`, `ConversationMember.clearedAt`.

## Android

- Ticks: gray double = delivered/sent, blue = read  
- Long-press → Delete for me / Delete for everyone  
- Clear chat → server clear + empty local list  
- WS `receipt` updates ticks; incoming message acks delivered  

## Verify

1. A sends to B (B on home) → A sees delivered ticks.  
2. B opens chat → A ticks turn read (blue).  
3. Long-press → delete for everyone → both see “This message was deleted”.  
4. Clear chat → history gone for you only; peer still has it.  
