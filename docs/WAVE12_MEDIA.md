# Wave 12 — Rich media in chat

Date: 2026-10-23  
Status: Shipping

## Features

| # | Feature | How |
|---|---------|-----|
| 12.1 | Voice notes | Mic → record → upload AUDIO → play in bubble |
| 12.2 | Documents | Attach → Document → FILE type; tap opens |
| 12.3 | Video | Gallery ImageAndVideo → VIDEO type; tap plays |
| 12.4 | Shared media gallery | Contact info / chat menu → Media tabs |
| 12.5 | Link preview | Server unfurl on send; card under text |

## API

- `MessageType`: `TEXT` \| `IMAGE` \| `AUDIO` \| `FILE` \| `VIDEO`
- `Message.preview` JSON (link card)
- Multipart / PUT upload max **25MB**
- `GET /api/conversations/:id/media?type=all|images|videos|docs|audio`
- `GET /api/conversations/:id/media/counts`
