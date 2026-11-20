package com.aivora.app.ui.perf

import android.content.Intent
import android.graphics.Outline
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.SingletonImageLoader
import coil3.asDrawable
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.SuccessResult
import coil3.size.Size
import com.aivora.app.data.local.LocalMediaKind
import com.aivora.app.data.local.LocalMessage
import com.aivora.app.data.local.MessageDeliveryStatus
import com.aivora.app.ui.icons.AivoraIcons
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Native message recycle — Views only.
 * Voice / photo match Figma `284:6482` Voice bubble + `284:6473` Photo bubble.
 */
class ChatMessageAdapter(
    private val isGroup: Boolean,
    private val dark: Boolean,
    private val forceSingleTick: Boolean,
) : ListAdapter<LocalMessage, RecyclerView.ViewHolder>(DIFF) {

    var onLongPress: (LocalMessage) -> Unit = {}
    /** Open unopened view-once photo (receiver). */
    var onViewOnce: (LocalMessage) -> Unit = {}
    /** Open normal image / video in the in-app viewer (not the browser). */
    var onOpenImage: (LocalMessage) -> Unit = {}
    /** Poll option tap → (message, optionId). */
    var onVote: (LocalMessage, String) -> Unit = { _, _ -> }
    /** Event RSVP tap → (message, going|maybe|not_going). */
    var onRsvp: (LocalMessage, String) -> Unit = { _, _ -> }
    /** 1:1 peer display name / avatar — letter fallback when URL null. */
    var peerTitle: String = ""
    var peerAvatarUrl: String? = null

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).system) TYPE_SYSTEM else TYPE_BUBBLE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val ctx = parent.context
        val d = ctx.resources.displayMetrics.density
        fun dp(v: Int) = (v * d).toInt()
        return when (viewType) {
            TYPE_SYSTEM -> {
                val row = LinearLayout(ctx).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER
                    layoutParams = RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                    setPadding(dp(24), dp(6), dp(24), dp(6))
                }
                val pill = TextView(ctx).apply {
                    setTextColor(NativeColors.TextSecondary)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f)
                    gravity = Gravity.CENTER
                    setPadding(dp(12), dp(5), dp(12), dp(5))
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = 10f * d
                        setColor(NativeColors.ChatDivider)
                    }
                }
                row.addView(pill)
                SystemHolder(row, pill)
            }
            else -> BubbleHolder.create(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = getItem(position)
        when (holder) {
            is BubbleHolder -> holder.bind(
                msg = msg,
                isGroup = isGroup,
                dark = dark,
                forceSingleTick = forceSingleTick,
                peerTitle = peerTitle,
                peerAvatarUrl = peerAvatarUrl,
                onLongPress = onLongPress,
                onViewOnce = onViewOnce,
                onOpenImage = onOpenImage,
                onVote = onVote,
                onRsvp = onRsvp,
            )
            is SystemHolder -> holder.bind(msg.body)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is BubbleHolder) holder.recycle()
        super.onViewRecycled(holder)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        if (holder is BubbleHolder) holder.stopVoice()
        super.onViewDetachedFromWindow(holder)
    }

    class SystemHolder(
        itemView: View,
        private val pill: TextView,
    ) : RecyclerView.ViewHolder(itemView) {
        fun bind(body: String) {
            pill.text = body
        }
    }

    class BubbleHolder private constructor(
        private val row: LinearLayout,
        private val avatarFrame: FrameLayout,
        private val avatarLetter: TextView,
        private val avatarImage: ImageView,
        private val bubble: LinearLayout,
        private val sender: TextView,
        private val reply: TextView,
        private val mediaFrame: FrameLayout,
        private val mediaImage: ImageView,
        private val playOverlay: ImageView,
        private val voiceBlock: LinearLayout,
        private val voicePlay: ImageView,
        private val waveform: VoiceWaveformView,
        private val voiceDur: TextView,
        private val voiceSpeed: TextView,
        private val voiceTime: TextView,
        private val voiceTicks: TextView,
        private val meta: LinearLayout,
        private val viewOnceRow: LinearLayout,
        private val viewOnceIcon: ImageView,
        private val viewOnceTitle: TextView,
        private val viewOnceSub: TextView,
        private val fileRow: LinearLayout,
        private val fileName: TextView,
        private val mapBlock: LinearLayout,
        private val mapPreview: ImageView,
        private val mapTitle: TextView,
        private val mapSub: TextView,
        private val linkBlock: LinearLayout,
        private val linkTitle: TextView,
        private val linkSite: TextView,
        private val richBlock: LinearLayout,
        private val body: TextView,
        private val time: TextView,
        private val ticks: TextView,
    ) : RecyclerView.ViewHolder(row) {

        private var player: MediaPlayer? = null
        private var loadJob: Job? = null
        private var avatarJob: Job? = null
        private var boundId: String? = null
        private var voiceSecs: Int = 0
        private var voiceRate: Float = 1f
        private val handler = Handler(Looper.getMainLooper())
        private val tickProgress = object : Runnable {
            override fun run() {
                val mp = player ?: return
                if (!mp.isPlaying) return
                val dur = runCatching { mp.duration }.getOrDefault(0).coerceAtLeast(1)
                val pos = runCatching { mp.currentPosition }.getOrDefault(0)
                waveform.progress = pos.toFloat() / dur
                voiceDur.text = "${formatDuration(pos / 1000)} / ${formatDuration(voiceSecs)}"
                handler.postDelayed(this, 200)
            }
        }

        fun recycle() {
            stopVoice()
            loadJob?.cancel()
            loadJob = null
            avatarJob?.cancel()
            avatarJob = null
            mediaImage.setImageDrawable(null)
            mapPreview.setImageDrawable(null)
            avatarImage.setImageDrawable(null)
            boundId = null
        }

        fun stopVoice() {
            handler.removeCallbacks(tickProgress)
            runCatching {
                player?.reset()
                player?.release()
            }
            player = null
            waveform.progress = 0f
            setPlayIcon(playing = false)
            if (voiceSecs > 0) {
                voiceDur.text = "0:00 / ${formatDuration(voiceSecs)}"
            }
        }

        fun bind(
            msg: LocalMessage,
            isGroup: Boolean,
            dark: Boolean,
            forceSingleTick: Boolean,
            peerTitle: String,
            peerAvatarUrl: String?,
            onLongPress: (LocalMessage) -> Unit,
            onViewOnce: (LocalMessage) -> Unit,
            onOpenImage: (LocalMessage) -> Unit,
            onVote: (LocalMessage, String) -> Unit,
            onRsvp: (LocalMessage, String) -> Unit,
        ) {
            if (boundId != msg.id) stopVoice()
            boundId = msg.id
            val density = itemView.resources.displayMetrics.density
            fun dp(v: Int) = (v * density).toInt()
            // Incoming left + avatar; outgoing right (WA).
            row.gravity = if (msg.mine) Gravity.END else Gravity.START

            val bg = if (msg.mine) NativeColors.BubbleMine
            else if (dark) NativeColors.BubbleOtherDark
            else NativeColors.BubbleOtherLight
            bubble.background = bubbleDrawable(bg, msg.mine, !msg.mine && !dark, density)

            val maxW = (itemView.resources.displayMetrics.widthPixels * 0.72f).toInt()
                .coerceAtMost(dp(280))
            body.maxWidth = maxW
            // Figma Photo bubble: 8px pad, shared photo ~196h, 12dp radius.
            mediaImage.layoutParams = FrameLayout.LayoutParams(maxW - dp(8), dp(196))

            if (msg.mine) {
                avatarFrame.visibility = View.GONE
                avatarJob?.cancel()
                avatarJob = null
                avatarImage.setImageDrawable(null)
            } else {
                avatarFrame.visibility = View.VISIBLE
                val label = when {
                    isGroup && !msg.senderName.isNullOrBlank() -> msg.senderName!!
                    peerTitle.isNotBlank() -> peerTitle
                    else -> "?"
                }
                val letter = label.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                avatarLetter.text = letter
                avatarLetter.background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(avatarColor(label))
                }
                avatarImage.setImageDrawable(null)
                avatarJob?.cancel()
                val url = if (isGroup) null else peerAvatarUrl
                if (!url.isNullOrBlank()) {
                    avatarJob = CoroutineScope(Dispatchers.Main.immediate).launch {
                        val result = withContext(Dispatchers.IO) {
                            runCatching {
                                val loader = SingletonImageLoader.get(itemView.context)
                                loader.execute(
                                    ImageRequest.Builder(itemView.context)
                                        .data(url)
                                        .size(Size(84, 84))
                                        .build(),
                                )
                            }.getOrNull()
                        }
                        if (result is SuccessResult && boundId == msg.id) {
                            avatarImage.setImageDrawable(
                                result.image.asDrawable(itemView.context.resources),
                            )
                        }
                    }
                }
            }

            if (!msg.mine && isGroup && !msg.senderName.isNullOrBlank()) {
                sender.visibility = View.VISIBLE
                sender.text = msg.senderName
            } else {
                sender.visibility = View.GONE
            }

            reply.visibility = View.GONE
            mediaFrame.visibility = View.GONE
            playOverlay.visibility = View.GONE
            voiceBlock.visibility = View.GONE
            viewOnceRow.visibility = View.GONE
            fileRow.visibility = View.GONE
            mapBlock.visibility = View.GONE
            linkBlock.visibility = View.GONE
            richBlock.visibility = View.GONE
            richBlock.removeAllViews()
            richBlock.minimumWidth = 0
            richBlock.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            body.visibility = View.GONE
            mediaImage.setImageDrawable(null)
            viewOnceRow.setOnClickListener(null)
            // Default bubble pad; voice overrides to Figma 12, photo to 8.
            bubble.setPadding(dp(10), dp(8), dp(10), dp(8))

            msg.replyTo?.let { q ->
                reply.visibility = View.VISIBLE
                val who = when {
                    q.mine -> "You"
                    isGroup -> bindingAdapter?.let { ad ->
                        (ad as? ChatMessageAdapter)?.currentList
                            ?.firstOrNull { it.id == q.id }?.senderName
                    }.orEmpty().ifBlank { "" }
                    else -> peerTitle
                }
                if (who.isBlank()) {
                    reply.text = q.body
                } else {
                    val sp = android.text.SpannableStringBuilder(who).apply {
                        setSpan(android.text.style.StyleSpan(Typeface.BOLD), 0, length, 0)
                        setSpan(android.text.style.ForegroundColorSpan(NativeColors.Primary), 0, length, 0)
                        append("\n")
                        append(q.body)
                    }
                    reply.text = sp
                }
            }

            val ink = if (dark && !msg.mine) NativeColors.White else NativeColors.Ink
            body.setTextColor(ink)

            when {
                msg.deletedForEveryone || msg.disappeared -> {
                    body.visibility = View.VISIBLE
                    body.setTextColor(NativeColors.TextSecondary)
                    body.text = msg.body
                    body.setTypeface(body.typeface, Typeface.ITALIC)
                }
                msg.viewOnce -> {
                    val opened = msg.mediaUri == null ||
                        msg.body.equals("Opened", ignoreCase = true)
                    viewOnceRow.visibility = View.VISIBLE
                    viewOnceIcon.background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(
                            if (opened) 0x5900BE3C.toInt() else NativeColors.Primary,
                        )
                    }
                    val eye = ContextCompat.getDrawable(
                        itemView.context,
                        if (opened) {
                            com.aivora.app.R.drawable.ic_view_once_opened
                        } else {
                            com.aivora.app.R.drawable.ic_view_once
                        },
                    )?.mutate()
                    if (eye != null) {
                        DrawableCompat.setTint(eye, NativeColors.White)
                        viewOnceIcon.setImageDrawable(eye)
                    }
                    viewOnceTitle.text = if (opened) "Opened" else "Photo"
                    viewOnceSub.text = if (opened) "View once · opened" else "View once photo"
                    if (!msg.mine && !opened && !msg.mediaUri.isNullOrBlank()) {
                        viewOnceRow.setOnClickListener { onViewOnce(msg) }
                    }
                }
                msg.location != null -> {
                    val loc = msg.location!!
                    mapBlock.visibility = View.VISIBLE
                    mapTitle.text = when {
                        loc.live -> "Live location"
                        loc.ended -> loc.name?.takeIf { it.isNotBlank() } ?: "Live location ended"
                        else -> loc.name?.takeIf { it.isNotBlank() } ?: "Location"
                    }
                    mapSub.text = loc.address?.takeIf { it.isNotBlank() }
                        ?: "%.4f, %.4f".format(java.util.Locale.US, loc.lat, loc.lng)
                    bubble.setPadding(dp(4), dp(4), dp(4), dp(8))
                    loadMapInto(mapPreview, loc.lat, loc.lng)
                    mapBlock.setOnClickListener {
                        val label = Uri.encode(loc.name ?: "Location")
                        val geo = Uri.parse(
                            "geo:${loc.lat},${loc.lng}?q=${loc.lat},${loc.lng}($label)",
                        )
                        runCatching {
                            itemView.context.startActivity(Intent(Intent.ACTION_VIEW, geo))
                        }.onFailure {
                            runCatching {
                                itemView.context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(
                                            "https://www.google.com/maps/search/?api=1&query=${loc.lat},${loc.lng}",
                                        ),
                                    ),
                                )
                            }
                        }
                    }
                }
                msg.voiceDurationSec != null || msg.mediaKind == LocalMediaKind.Audio -> {
                    // Figma Voice bubble: p-12, play+wave, then duration/speed | time.
                    // Cap width — never stretch to full row (WA-sized ~200dp).
                    val voiceW = (itemView.resources.displayMetrics.widthPixels * 0.55f).toInt()
                        .coerceIn(dp(168), dp(200))
                    voiceBlock.layoutParams = LinearLayout.LayoutParams(
                        voiceW,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                    bubble.setPadding(dp(12), dp(12), dp(12), dp(12))
                    voiceBlock.visibility = View.VISIBLE
                    voiceSecs = msg.voiceDurationSec ?: 1
                    waveform.progress = 0f
                    voiceDur.text = "0:00 / ${formatDuration(voiceSecs)}"
                    voiceRate = 1f
                    voiceSpeed.text = rateLabel(voiceRate)
                    voiceSpeed.setOnClickListener { cycleVoiceRate() }
                    setPlayIcon(playing = false)
                    voicePlay.setOnClickListener { toggleVoice(msg.mediaUri) }
                    waveform.setOnClickListener { toggleVoice(msg.mediaUri) }
                }
                msg.mediaKind == LocalMediaKind.File -> {
                    fileRow.visibility = View.VISIBLE
                    fileName.text = msg.fileName ?: msg.body.ifBlank { "Document" }
                    fileRow.setOnClickListener {
                        val url = msg.mediaUri ?: return@setOnClickListener
                        runCatching {
                            itemView.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                    }
                }
                msg.mediaKind == LocalMediaKind.Video && !msg.mediaUri.isNullOrBlank() -> {
                    bubble.setPadding(dp(8), dp(8), dp(8), dp(8))
                    mediaFrame.visibility = View.VISIBLE
                    playOverlay.visibility = View.VISIBLE
                    loadImage(msg.mediaUri!!)
                    mediaFrame.setOnClickListener {
                        // Videos still open externally; images use in-app viewer.
                        runCatching {
                            itemView.context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(msg.mediaUri)),
                            )
                        }
                    }
                    if (msg.body.isNotBlank() && msg.body != "Video") {
                        body.visibility = View.VISIBLE
                        body.setTypeface(null, Typeface.NORMAL)
                        body.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                        body.text = msg.body
                    }
                }
                (msg.mediaKind == LocalMediaKind.Image || msg.mediaKind == null) &&
                    !msg.mediaUri.isNullOrBlank() -> {
                    // Figma Photo bubble: p-8 around media.
                    bubble.setPadding(dp(8), dp(8), dp(8), dp(8))
                    mediaFrame.visibility = View.VISIBLE
                    loadImage(msg.mediaUri!!)
                    mediaFrame.setOnClickListener { onOpenImage(msg) }
                    if (msg.body.isNotBlank() && msg.body != "Photo") {
                        body.visibility = View.VISIBLE
                        body.setTypeface(null, Typeface.NORMAL)
                        body.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                        body.text = msg.body
                    }
                }
                msg.isSticker -> {
                    body.visibility = View.VISIBLE
                    body.setTypeface(null, Typeface.NORMAL)
                    body.setTextSize(TypedValue.COMPLEX_UNIT_SP, 42f)
                    body.text = msg.body
                    bubble.setPadding(dp(10), dp(6), dp(10), dp(6))
                }
                msg.poll != null -> {
                    val p = msg.poll!!
                    richBlock.visibility = View.VISIBLE
                    richBlock.removeAllViews()
                    richBlock.minimumWidth = dp(250)
                    val ctx = itemView.context
                    richBlock.addView(
                        TextView(ctx).apply {
                            text = p.question
                            setTextColor(ink)
                            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                            typeface = Typeface.DEFAULT_BOLD
                            setPadding(0, dp(2), 0, dp(2))
                        },
                    )
                    richBlock.addView(
                        TextView(ctx).apply {
                            text = if (p.multiple) "Select one or more" else "Select one"
                            setTextColor(NativeColors.TextSecondary)
                            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                            setPadding(0, 0, 0, dp(6))
                        },
                    )
                    val total = p.options.sumOf { it.votes }.coerceAtLeast(1)
                    p.options.forEach { opt ->
                        val pct = opt.votes * 10000 / total
                        val rowOpt = TextView(ctx).apply {
                            val mark = if (opt.mine) "✓  " else "○  "
                            text = "$mark${opt.text}"
                            setTextColor(if (opt.mine) NativeColors.Primary else ink)
                            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                            minHeight = dp(44)
                            gravity = Gravity.CENTER_VERTICAL
                            setPadding(dp(12), dp(8), dp(52), dp(8))
                            val track = GradientDrawable().apply {
                                cornerRadius = 10f * density
                                setColor(0x14000000)
                            }
                            val fill = android.graphics.drawable.ClipDrawable(
                                GradientDrawable().apply {
                                    cornerRadius = 10f * density
                                    setColor(if (opt.mine) 0x3300BE3C else 0x1F000000)
                                },
                                Gravity.START,
                                android.graphics.drawable.ClipDrawable.HORIZONTAL,
                            ).also { it.level = pct }
                            background = android.graphics.drawable.LayerDrawable(arrayOf(track, fill))
                            layoutParams = LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                            ).also { it.topMargin = dp(6) }
                            isEnabled = !p.closed
                            alpha = if (p.closed) 0.55f else 1f
                            setOnClickListener {
                                if (!p.closed) onVote(msg, opt.id)
                            }
                        }
                        val frame = FrameLayout(ctx).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                            )
                            addView(rowOpt)
                            addView(
                                TextView(ctx).apply {
                                    text = opt.votes.toString()
                                    setTextColor(NativeColors.TextSecondary)
                                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                                    layoutParams = FrameLayout.LayoutParams(
                                        ViewGroup.LayoutParams.WRAP_CONTENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT,
                                        Gravity.END or Gravity.CENTER_VERTICAL,
                                    ).also { it.topMargin = dp(6); it.marginEnd = dp(14) }
                                },
                            )
                        }
                        richBlock.addView(frame)
                    }
                    richBlock.addView(
                        TextView(ctx).apply {
                            text = "${p.totalVoters} vote${if (p.totalVoters == 1) "" else "s"}"
                            setTextColor(NativeColors.TextSecondary)
                            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                            gravity = Gravity.CENTER
                            setPadding(0, dp(8), 0, dp(2))
                        },
                    )
                }
                msg.contact != null -> {
                    val c = msg.contact!!
                    val ctx = itemView.context
                    richBlock.visibility = View.VISIBLE
                    richBlock.removeAllViews()
                    richBlock.layoutParams = LinearLayout.LayoutParams(
                        dp(240), ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                    val phone = c.phones.firstOrNull().orEmpty()
                    val avatar = TextView(ctx).apply {
                        text = c.name.trim().firstOrNull()?.uppercase() ?: "?"
                        gravity = Gravity.CENTER
                        setTextColor(NativeColors.White)
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                        typeface = Typeface.DEFAULT_BOLD
                        background = GradientDrawable().apply {
                            shape = GradientDrawable.OVAL
                            setColor(0xFF8696A0.toInt())
                        }
                        layoutParams = LinearLayout.LayoutParams(dp(44), dp(44))
                    }
                    val nameTv = TextView(ctx).apply {
                        text = c.name
                        setTextColor(ink)
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                        typeface = Typeface.DEFAULT_BOLD
                        maxLines = 1
                        ellipsize = android.text.TextUtils.TruncateAt.END
                    }
                    val phoneTv = TextView(ctx).apply {
                        text = phone
                        setTextColor(NativeColors.TextSecondary)
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                        maxLines = 1
                    }
                    val col = LinearLayout(ctx).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(dp(10), 0, 0, 0)
                        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                        addView(nameTv)
                        if (phone.isNotBlank()) addView(phoneTv)
                    }
                    richBlock.addView(
                        LinearLayout(ctx).apply {
                            orientation = LinearLayout.HORIZONTAL
                            gravity = Gravity.CENTER_VERTICAL
                            setPadding(0, dp(2), 0, dp(6))
                            addView(avatar)
                            addView(col)
                        },
                    )
                    richBlock.addView(
                        View(ctx).apply {
                            setBackgroundColor(0x33808080)
                            layoutParams = LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, dp(1),
                            )
                        },
                    )
                    richBlock.addView(
                        TextView(ctx).apply {
                            text = "Add to contacts"
                            gravity = Gravity.CENTER
                            setTextColor(NativeColors.Primary)
                            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                            typeface = Typeface.DEFAULT_BOLD
                            setPadding(0, dp(8), 0, dp(4))
                            setOnClickListener {
                                runCatching {
                                    ctx.startActivity(
                                        Intent(android.provider.ContactsContract.Intents.Insert.ACTION).apply {
                                            type = android.provider.ContactsContract.RawContacts.CONTENT_TYPE
                                            putExtra(android.provider.ContactsContract.Intents.Insert.NAME, c.name)
                                            putExtra(android.provider.ContactsContract.Intents.Insert.PHONE, phone)
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        },
                                    )
                                }
                            }
                        },
                    )
                }
                msg.event != null -> {
                    val e = msg.event!!
                    richBlock.visibility = View.VISIBLE
                    richBlock.removeAllViews()
                    richBlock.addView(
                        TextView(itemView.context).apply {
                            text = e.title
                            setTextColor(NativeColors.Ink)
                            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                            typeface = Typeface.DEFAULT_BOLD
                        },
                    )
                    val whenText = runCatching {
                        java.time.format.DateTimeFormatter.ofPattern("EEE, d MMM · h:mm a", java.util.Locale.getDefault())
                            .format(java.time.Instant.parse(e.startsAt).atZone(java.time.ZoneId.systemDefault()))
                    }.getOrNull()
                    val details = listOfNotNull(
                        whenText?.let { "🗓 $it" },
                        e.location?.takeIf { it.isNotBlank() }?.let { "📍 $it" },
                        e.description?.takeIf { it.isNotBlank() },
                        if (e.cancelled) "Cancelled" else null,
                    ).joinToString("\n")
                    if (details.isNotEmpty()) {
                        richBlock.addView(
                            TextView(itemView.context).apply {
                                text = details
                                setTextColor(NativeColors.Ink)
                                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                                setPadding(0, dp(4), 0, 0)
                            },
                        )
                    }
                    richBlock.addView(
                        TextView(itemView.context).apply {
                            text = "Going ${e.going} · Maybe ${e.maybe} · Can't ${e.notGoing}"
                            setTextColor(NativeColors.TextSecondary)
                            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                            setPadding(0, dp(4), 0, dp(6))
                        },
                    )
                    if (!e.cancelled) {
                        fun rsvpChip(label: String, answer: String, selected: Boolean): TextView {
                            return TextView(itemView.context).apply {
                                text = label
                                setTextColor(if (selected) NativeColors.White else NativeColors.Ink)
                                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                                gravity = Gravity.CENTER
                                maxLines = 1
                                setPadding(dp(6), dp(9), dp(6), dp(9))
                                background = GradientDrawable().apply {
                                    cornerRadius = 8f * density
                                    setColor(if (selected) NativeColors.Primary else 0x14000000)
                                }
                                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                                    .also { it.marginEnd = dp(4) }
                                setOnClickListener { onRsvp(msg, answer) }
                            }
                        }
                        val rowRsvp = LinearLayout(itemView.context).apply {
                            orientation = LinearLayout.HORIZONTAL
                            addView(rsvpChip("Going", "going", e.myAnswer == "going"))
                            addView(rsvpChip("Maybe", "maybe", e.myAnswer == "maybe"))
                            addView(rsvpChip("Can't", "not_going", e.myAnswer == "not_going"))
                        }
                        richBlock.minimumWidth = dp(250)
                        richBlock.addView(rowRsvp)
                    }
                }
                else -> {
                    if (msg.body.isNotBlank()) {
                        body.visibility = View.VISIBLE
                        body.setTypeface(null, Typeface.NORMAL)
                        body.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                        body.text = msg.body
                    }
                }
            }

            msg.linkPreview?.let { card ->
                linkBlock.visibility = View.VISIBLE
                linkTitle.text = card.title ?: card.site ?: card.url
                linkSite.text = card.site ?: card.url
                linkBlock.setOnClickListener {
                    runCatching {
                        itemView.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(card.url)))
                    }
                }
            }

            val reactionPrefix = (if (msg.starred) "★ " else "") +
                (if (!msg.editedAtIso.isNullOrBlank() && !msg.deletedForEveryone) "Edited · " else "")
            val chipBox = bubble.getChildAt(bubble.childCount - 1) as LinearLayout
            val chip = chipBox.getChildAt(0) as TextView
            if (msg.reactions.isEmpty() || msg.deletedForEveryone) {
                chipBox.visibility = View.GONE
                row.setPadding(dp(6), dp(2), dp(10), dp(2))
            } else {
                chipBox.visibility = View.VISIBLE
                chip.text = buildString {
                    msg.reactions.forEach { r ->
                        if (isNotEmpty()) append(' ')
                        append(r.emoji)
                        if (r.count > 1) append(r.count)
                    }
                }
                chipBox.gravity = if (msg.mine) Gravity.END else Gravity.START
                chip.translationY = dp(1).toFloat()
                row.setPadding(dp(6), dp(2), dp(10), dp(16))
            }
            time.text = reactionPrefix + msg.time
            voiceTime.text = reactionPrefix + msg.time
            val isVoice = voiceBlock.visibility == View.VISIBLE
            meta.visibility = if (isVoice) View.GONE else View.VISIBLE

            val status = if (forceSingleTick) MessageDeliveryStatus.Sent else msg.status
            val tickStr = when (status) {
                MessageDeliveryStatus.Sending -> "○"
                MessageDeliveryStatus.Sent -> "✓"
                else -> "✓✓"
            }
            val tickColor = when (status) {
                MessageDeliveryStatus.Read -> NativeColors.TickRead
                MessageDeliveryStatus.Delivered -> NativeColors.TextSecondary
                else -> 0x8C727272.toInt()
            }
            if (msg.mine) {
                ticks.visibility = View.VISIBLE
                ticks.text = tickStr
                ticks.setTextColor(tickColor)
                voiceTicks.visibility = if (isVoice) View.VISIBLE else View.GONE
                voiceTicks.text = tickStr
                voiceTicks.setTextColor(tickColor)
            } else {
                ticks.visibility = View.GONE
                voiceTicks.visibility = View.GONE
            }

            val longPress = View.OnLongClickListener {
                onLongPress(msg)
                true
            }
            itemView.setOnLongClickListener(longPress)
            // Children with their own tap handlers would otherwise swallow the long press.
            listOf(mapBlock, viewOnceRow, fileRow, mediaFrame, richBlock, linkBlock, voicePlay, waveform, voiceSpeed)
                .forEach { it.setOnLongClickListener(longPress) }
        }

        private fun setPlayIcon(playing: Boolean) {
            val res = if (playing) android.R.drawable.ic_media_pause else AivoraIcons.Play
            val d = ContextCompat.getDrawable(itemView.context, res)?.mutate()
            if (d != null) {
                DrawableCompat.setTint(d, NativeColors.White)
                voicePlay.setImageDrawable(d)
            }
            // Figma Play glyph is 32 inside 44 circle → ~6dp inset.
            voicePlay.scaleType = ImageView.ScaleType.CENTER
            val inset = (6 * itemView.resources.displayMetrics.density).toInt()
            voicePlay.setPadding(inset, inset, inset, inset)
        }

        private fun loadImage(url: String) = loadInto(mediaImage, url)

        /** OSM raster tile (static-map service is defunct) with a pin drawn at the exact point. */
        private fun loadMapInto(target: ImageView, lat: Double, lng: Double) {
            loadJob?.cancel()
            val ctx = itemView.context
            target.setBackgroundColor(0xFFE8EDE9.toInt())
            val z = 15
            val n = 1 shl z
            val xf = (lng + 180.0) / 360.0 * n
            val latRad = Math.toRadians(lat)
            val yf = (1.0 - Math.log(Math.tan(latRad) + 1.0 / Math.cos(latRad)) / Math.PI) / 2.0 * n
            val tx = Math.floor(xf).toInt().coerceIn(0, n - 1)
            val ty = Math.floor(yf).toInt().coerceIn(0, n - 1)
            val fx = (xf - tx).toFloat()
            val fy = (yf - ty).toFloat()
            // 2x2 tiles chosen so the pin's +-0.5 tile window is always fully covered.
            val x0 = if (fx < 0.5f) tx - 1 else tx
            val y0 = if (fy < 0.5f) ty - 1 else ty
            loadJob = CoroutineScope(Dispatchers.Main.immediate).launch {
                val res = withContext(Dispatchers.IO) {
                    runCatching {
                        val tile = 256
                        val out = android.graphics.Bitmap.createBitmap(tile * 2, tile * 2, android.graphics.Bitmap.Config.ARGB_8888)
                        val c = android.graphics.Canvas(out)
                        c.drawColor(0xFFE8EDE9.toInt())
                        var ok = false
                        for (dx in 0..1) for (dy in 0..1) {
                            val X = (x0 + dx).coerceIn(0, n - 1)
                            val Y = (y0 + dy).coerceIn(0, n - 1)
                            val req = ImageRequest.Builder(ctx)
                                .data("https://tile.openstreetmap.org/$z/$X/$Y.png")
                                .httpHeaders(
                                    coil3.network.NetworkHeaders.Builder()
                                        .add("User-Agent", "AivoraApp/1.0 (Android)").build(),
                                )
                                .size(Size(tile, tile))
                                .allowHardware(false)
                                .build()
                            val r = SingletonImageLoader.get(ctx).execute(req)
                            if (r is SuccessResult) {
                                val dr = r.image.asDrawable(ctx.resources)
                                dr.setBounds(dx * tile, dy * tile, (dx + 1) * tile, (dy + 1) * tile)
                                dr.draw(c)
                                ok = true
                            }
                        }
                        if (!ok) return@runCatching null
                        val px = ((tx - x0) + fx) * tile
                        val py = ((ty - y0) + fy) * tile
                        val ring = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
                        ring.color = 0x33000000
                        c.drawCircle(px, py + 2f, 12f, ring)
                        ring.color = android.graphics.Color.WHITE
                        c.drawCircle(px, py, 11f, ring)
                        ring.color = 0xFFE64C53.toInt()
                        c.drawCircle(px, py, 7.5f, ring)
                        Triple(out, px, py)
                    }.getOrNull()
                }
                if (res != null) {
                    val (b, px, py) = res
                    target.setImageBitmap(b)
                    target.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    target.scaleType = ImageView.ScaleType.MATRIX
                    val vw = target.layoutParams.width.toFloat()
                    val vh = target.layoutParams.height.toFloat()
                    val sc = maxOf(vw / 256f * 0.9f, vh / 256f * 0.9f)
                    val m = android.graphics.Matrix()
                    m.postScale(sc, sc)
                    m.postTranslate(vw / 2f - px * sc, vh / 2f - py * sc)
                    target.imageMatrix = m
                }
            }
        }

        private fun loadInto(target: ImageView, url: String) {
            loadJob?.cancel()
            val ctx = itemView.context
            target.setBackgroundColor(0xFFE8EDE9.toInt())
            loadJob = CoroutineScope(Dispatchers.Main.immediate).launch {
                val result = withContext(Dispatchers.IO) {
                    runCatching {
                        val loader = SingletonImageLoader.get(ctx)
                        val req = ImageRequest.Builder(ctx)
                            .data(url)
                            .size(Size(720, 720))
                            .build()
                        loader.execute(req)
                    }.getOrNull()
                }
                target.clearColorFilter()
                if (result is SuccessResult) {
                    target.setImageDrawable(result.image.asDrawable(ctx.resources))
                    target.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    target.scaleType = ImageView.ScaleType.CENTER_CROP
                } else {
                    // Media missing on the server (e.g. lost upload): show a clear placeholder, not a blank box.
                    target.setBackgroundColor(0xFF2A3942.toInt())
                    target.scaleType = ImageView.ScaleType.CENTER
                    target.setImageResource(android.R.drawable.ic_menu_report_image)
                    target.setColorFilter(0xFF8696A0.toInt())
                }
            }
        }

        private fun rateLabel(r: Float) = if (r == 1f) "1×" else if (r == 2f) "2×" else "1.5×"

        private fun applyRate(mp: MediaPlayer) {
            runCatching {
                if (mp.isPlaying) {
                    mp.playbackParams = android.media.PlaybackParams().setSpeed(voiceRate)
                }
            }
        }

        private fun cycleVoiceRate() {
            voiceRate = when (voiceRate) { 1f -> 1.5f; 1.5f -> 2f; else -> 1f }
            voiceSpeed.text = rateLabel(voiceRate)
            player?.let { applyRate(it) }
        }

        private fun toggleVoice(url: String?) {
            if (url.isNullOrBlank()) return
            val playing = player?.isPlaying == true
            if (playing) {
                runCatching { player?.pause() }
                handler.removeCallbacks(tickProgress)
                setPlayIcon(playing = false)
                return
            }
            val existing = player
            if (existing != null) {
                runCatching { existing.start() }
                applyRate(existing)
                setPlayIcon(playing = true)
                handler.post(tickProgress)
                return
            }
            val mp = MediaPlayer()
            player = mp
            try {
                mp.setDataSource(itemView.context, Uri.parse(url))
                mp.setOnPreparedListener {
                    setPlayIcon(playing = true)
                    runCatching { mp.start() }
                    applyRate(mp)
                    handler.post(tickProgress)
                }
                mp.setOnCompletionListener {
                    setPlayIcon(playing = false)
                    waveform.progress = 0f
                    voiceDur.text = "0:00 / ${formatDuration(voiceSecs)}"
                    handler.removeCallbacks(tickProgress)
                }
                mp.setOnErrorListener { _, _, _ ->
                    setPlayIcon(playing = false)
                    true
                }
                mp.prepareAsync()
            } catch (_: Exception) {
                stopVoice()
            }
        }

        companion object {
            fun create(parent: ViewGroup): BubbleHolder {
                val ctx = parent.context
                val d = ctx.resources.displayMetrics.density
                fun dp(v: Int) = (v * d).toInt()
                val row = LinearLayout(ctx).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                    setPadding(dp(6), dp(2), dp(10), dp(2))
                    gravity = Gravity.BOTTOM
                }
                // Small peer avatar (28dp) — left of incoming only.
                val avatarLetter = TextView(ctx).apply {
                    gravity = Gravity.CENTER
                    setTextColor(NativeColors.White)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    typeface = Typeface.DEFAULT_BOLD
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                }
                val avatarImage = ImageView(ctx).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    clipToOutline = true
                    outlineProvider = object : ViewOutlineProvider() {
                        override fun getOutline(view: View, outline: Outline) {
                            outline.setOval(0, 0, view.width, view.height)
                        }
                    }
                }
                val avatarFrame = FrameLayout(ctx).apply {
                    layoutParams = LinearLayout.LayoutParams(dp(28), dp(28)).also {
                        it.marginEnd = dp(6)
                        it.gravity = Gravity.BOTTOM
                        it.bottomMargin = dp(2)
                    }
                    visibility = View.GONE
                    addView(avatarLetter)
                    addView(avatarImage)
                }
                val bubble = LinearLayout(ctx).apply {
                    orientation = LinearLayout.VERTICAL
                    // Figma Voice bubble padding 12; Photo uses 8 around media.
                    setPadding(dp(10), dp(8), dp(10), dp(8))
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                }
                val sender = TextView(ctx).apply {
                    setTextColor(NativeColors.Primary)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f)
                    typeface = Typeface.DEFAULT_BOLD
                    visibility = View.GONE
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    ).also { it.bottomMargin = dp(6) }
                }
                val reply = TextView(ctx).apply {
                    setTextColor(NativeColors.TextSecondary)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    maxLines = 3
                    ellipsize = android.text.TextUtils.TruncateAt.END
                    minWidth = dp(190)
                    setPadding(dp(14), dp(8), dp(10), dp(8))
                    background = android.graphics.drawable.LayerDrawable(
                        arrayOf(
                            GradientDrawable().apply {
                                setColor(0x2400BE3C)
                                cornerRadius = 8f * d
                            },
                            android.graphics.drawable.InsetDrawable(
                                GradientDrawable().apply {
                                    setColor(NativeColors.Primary)
                                    cornerRadius = 2f * d
                                },
                                0, 0, 0, 0,
                            ),
                        ),
                    ).also { ld ->
                        ld.setLayerSize(1, dp(4), ViewGroup.LayoutParams.MATCH_PARENT)
                        ld.setLayerGravity(1, Gravity.START or Gravity.FILL_VERTICAL)
                    }
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    ).also { it.bottomMargin = dp(4) }
                    visibility = View.GONE
                }
                val mediaImage = ImageView(ctx).apply {
                    adjustViewBounds = true
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    clipToOutline = true
                    outlineProvider = object : ViewOutlineProvider() {
                        override fun getOutline(view: View, outline: Outline) {
                            // Figma Shared photo rounded-[12px]
                            val r = 12f * d
                            outline.setRoundRect(0, 0, view.width, view.height, r)
                        }
                    }
                }
                val playOverlay = ImageView(ctx).apply {
                    val pd = ContextCompat.getDrawable(ctx, AivoraIcons.Play)?.mutate()
                    if (pd != null) {
                        DrawableCompat.setTint(pd, NativeColors.White)
                        setImageDrawable(pd)
                    }
                    setBackgroundColor(0x73000000)
                    layoutParams = FrameLayout.LayoutParams(dp(48), dp(48), Gravity.CENTER)
                    setPadding(dp(12), dp(12), dp(12), dp(12))
                    visibility = View.GONE
                    clipToOutline = true
                    outlineProvider = object : ViewOutlineProvider() {
                        override fun getOutline(view: View, outline: Outline) {
                            outline.setOval(0, 0, view.width, view.height)
                        }
                    }
                }
                val mediaFrame = FrameLayout(ctx).apply {
                    visibility = View.GONE
                    addView(mediaImage)
                    addView(playOverlay)
                    // Figma Photo details gap-[8px] under shared photo.
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    ).also { it.bottomMargin = dp(8) }
                }

                // Figma Play action 44×44 Primary circle + white Play glyph.
                val voicePlay = ImageView(ctx).apply {
                    layoutParams = LinearLayout.LayoutParams(dp(44), dp(44))
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(NativeColors.Primary)
                    }
                    scaleType = ImageView.ScaleType.CENTER
                    setPadding(dp(6), dp(6), dp(6), dp(6))
                    val pd = ContextCompat.getDrawable(ctx, AivoraIcons.Play)?.mutate()
                    if (pd != null) {
                        DrawableCompat.setTint(pd, NativeColors.White)
                        setImageDrawable(pd)
                    }
                }
                val waveform = VoiceWaveformView(ctx).apply {
                    layoutParams = LinearLayout.LayoutParams(0, dp(36), 1f).also {
                        it.marginStart = dp(10)
                        it.gravity = Gravity.CENTER_VERTICAL
                    }
                }
                val audioPlayback = LinearLayout(ctx).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                    addView(voicePlay)
                    addView(waveform)
                }
                // Figma Audio details: duration #07833a Medium 12 / speed 11 secondary / time end.
                val voiceDur = TextView(ctx).apply {
                    setTextColor(NativeColors.VoiceDuration)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
                val voiceSpeed = TextView(ctx).apply {
                    text = "1×"
                    setTextColor(NativeColors.TextSecondary)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    gravity = Gravity.CENTER
                    minWidth = dp(40)
                    setPadding(dp(8), dp(3), dp(8), dp(3))
                    background = android.graphics.drawable.GradientDrawable().apply {
                        cornerRadius = dp(12).toFloat()
                        setColor(0x33808080)
                    }
                    (this as View).layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    ).also { it.marginStart = dp(10) }
                }
                val voiceTime = TextView(ctx).apply {
                    setTextColor(NativeColors.TextSecondary)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                }
                val voiceTicks = TextView(ctx).apply {
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                    visibility = View.GONE
                    setPadding(dp(4), 0, 0, 0)
                }
                val audioDetails = LinearLayout(ctx).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    ).also { it.topMargin = dp(8) }
                    addView(voiceDur)
                    addView(voiceSpeed)
                    addView(
                        View(ctx).apply {
                            layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
                        },
                    )
                    addView(voiceTime)
                    addView(voiceTicks)
                }
                val voiceBlock = LinearLayout(ctx).apply {
                    orientation = LinearLayout.VERTICAL
                    visibility = View.GONE
                    // Width set in bind (~168–200dp); avoid full-row stretch.
                    layoutParams = LinearLayout.LayoutParams(dp(190), ViewGroup.LayoutParams.WRAP_CONTENT)
                    addView(audioPlayback)
                    addView(audioDetails)
                }

                val viewOnceIcon = ImageView(ctx).apply {
                    layoutParams = LinearLayout.LayoutParams(dp(40), dp(40))
                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                    setPadding(dp(8), dp(8), dp(8), dp(8))
                }
                val viewOnceTitle = TextView(ctx).apply {
                    setTextColor(NativeColors.Ink)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    typeface = Typeface.DEFAULT_BOLD
                }
                val viewOnceSub = TextView(ctx).apply {
                    setTextColor(NativeColors.TextSecondary)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                }
                val viewOnceTextCol = LinearLayout(ctx).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                        .also { it.marginStart = dp(12) }
                    addView(viewOnceTitle)
                    addView(viewOnceSub)
                }
                val viewOnceRow = LinearLayout(ctx).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    visibility = View.GONE
                    minimumWidth = dp(168)
                    setPadding(dp(4), dp(2), dp(4), dp(2))
                    addView(viewOnceIcon)
                    addView(viewOnceTextCol)
                }

                val fileIcon = ImageView(ctx).apply {
                    layoutParams = LinearLayout.LayoutParams(dp(40), dp(40))
                    background = GradientDrawable().apply {
                        cornerRadius = 8f * d
                        setColor(0x2600BE3C)
                    }
                    setPadding(dp(8), dp(8), dp(8), dp(8))
                    val ad = ContextCompat.getDrawable(ctx, AivoraIcons.Attach)?.mutate()
                    if (ad != null) {
                        DrawableCompat.setTint(ad, NativeColors.Primary)
                        setImageDrawable(ad)
                    }
                }
                val fileNameTv = TextView(ctx).apply {
                    setTextColor(NativeColors.Ink)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    maxLines = 2
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                        .also { it.marginStart = dp(10) }
                }
                val fileRow = LinearLayout(ctx).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    visibility = View.GONE
                    setPadding(0, dp(4), 0, dp(4))
                    minimumWidth = dp(180)
                    addView(fileIcon)
                    addView(fileNameTv)
                }

                val mapPreview = ImageView(ctx).apply {
                    layoutParams = LinearLayout.LayoutParams(dp(240), dp(150))
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    clipToOutline = true
                    outlineProvider = object : ViewOutlineProvider() {
                        override fun getOutline(view: View, outline: Outline) {
                            outline.setRoundRect(0, 0, view.width, view.height, 8f * d)
                        }
                    }
                    background = GradientDrawable().apply {
                        cornerRadius = 10f * d
                        colors = intArrayOf(0xFFE8F0E9.toInt(), 0xFFD6E4F0.toInt())
                        orientation = GradientDrawable.Orientation.TL_BR
                    }
                    contentDescription = "Map"
                }
                val mapTitle = TextView(ctx).apply {
                    setTextColor(NativeColors.Ink)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    typeface = Typeface.DEFAULT_BOLD
                    setPadding(dp(6), dp(8), dp(6), 0)
                    maxLines = 1
                    ellipsize = android.text.TextUtils.TruncateAt.END
                }
                val mapSub = TextView(ctx).apply {
                    setTextColor(NativeColors.TextSecondary)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    setPadding(dp(6), dp(1), dp(6), dp(2))
                    maxLines = 2
                    ellipsize = android.text.TextUtils.TruncateAt.END
                }
                val mapBlock = LinearLayout(ctx).apply {
                    orientation = LinearLayout.VERTICAL
                    visibility = View.GONE
                    addView(mapPreview)
                    addView(mapTitle)
                    addView(mapSub)
                }

                val linkTitle = TextView(ctx).apply {
                    setTextColor(NativeColors.Ink)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                    typeface = Typeface.DEFAULT_BOLD
                    maxLines = 2
                }
                val linkSite = TextView(ctx).apply {
                    setTextColor(NativeColors.Primary)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                    maxLines = 1
                }
                val linkBlock = LinearLayout(ctx).apply {
                    orientation = LinearLayout.VERTICAL
                    visibility = View.GONE
                    setPadding(dp(10), dp(8), dp(10), dp(8))
                    background = GradientDrawable().apply {
                        cornerRadius = 8f * d
                        setColor(0x1A00BE3C)
                    }
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    ).also { it.topMargin = dp(6) }
                    addView(linkTitle)
                    addView(linkSite)
                }
                val richBlock = LinearLayout(ctx).apply {
                    orientation = LinearLayout.VERTICAL
                    visibility = View.GONE
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                }
                val body = TextView(ctx).apply {
                    setTextColor(NativeColors.Ink)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                    setLineSpacing(0f, 1.15f)
                    visibility = View.GONE
                }
                val meta = LinearLayout(ctx).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.END or Gravity.CENTER_VERTICAL
                    setPadding(0, dp(4), 0, 0)
                }
                val time = TextView(ctx).apply {
                    setTextColor(NativeColors.TextSecondary)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                }
                val ticks = TextView(ctx).apply {
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                    visibility = View.GONE
                }
                meta.addView(time)
                meta.addView(
                    ticks.apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                        ).also { it.marginStart = dp(4) }
                    },
                )
                bubble.addView(sender)
                bubble.addView(reply)
                bubble.addView(mediaFrame)
                bubble.addView(voiceBlock)
                bubble.addView(viewOnceRow)
                bubble.addView(fileRow)
                bubble.addView(mapBlock)
                bubble.addView(linkBlock)
                bubble.addView(richBlock)
                bubble.addView(body)
                bubble.addView(meta)
                val reactionChip = TextView(ctx).apply {
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                    setTextColor(NativeColors.Ink)
                    setPadding(dp(7), dp(2), dp(7), dp(2))
                    background = GradientDrawable().apply {
                        cornerRadius = 14f * d
                        setColor(NativeColors.SurfaceMuted)
                        setStroke(dp(1), NativeColors.ChatDivider)
                    }
                    elevation = 2f * d
                }
                val chipBox = LinearLayout(ctx).apply {
                    orientation = LinearLayout.HORIZONTAL
                    visibility = View.GONE
                    clipChildren = false
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(10),
                    ).also { it.topMargin = 0 }
                    addView(reactionChip, LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, dp(24)))
                }
                bubble.addView(chipBox)
                bubble.clipChildren = false
                bubble.clipToPadding = false
                row.clipChildren = false
                row.clipToPadding = false
                row.addView(avatarFrame)
                row.addView(bubble)
                return BubbleHolder(
                    row, avatarFrame, avatarLetter, avatarImage, bubble, sender, reply,
                    mediaFrame, mediaImage, playOverlay,
                    voiceBlock, voicePlay, waveform, voiceDur, voiceSpeed, voiceTime, voiceTicks, meta,
                    viewOnceRow, viewOnceIcon, viewOnceTitle, viewOnceSub,
                    fileRow, fileNameTv, mapBlock, mapPreview, mapTitle, mapSub,
                    linkBlock, linkTitle, linkSite, richBlock, body, time, ticks,
                )
            }
        }
    }

    companion object {
        const val TYPE_SYSTEM = 1
        const val TYPE_BUBBLE = 0

        private fun formatDuration(secs: Int): String {
            val m = secs / 60
            val s = secs % 60
            return "%d:%02d".format(m, s)
        }

        private fun avatarColor(name: String): Int {
            val hues = intArrayOf(
                0xFF00BE3C.toInt(),
                0xFF2C4A8C.toInt(),
                0xFF09C26F.toInt(),
                0xFFAA9EE8.toInt(),
                0xFF078649.toInt(),
                0xFFF44649.toInt(),
            )
            return hues[kotlin.math.abs(name.hashCode()) % hues.size]
        }

        private fun bubbleDrawable(
            color: Int,
            mine: Boolean,
            stroke: Boolean,
            density: Float,
        ): GradientDrawable {
            val r = 16f * density
            val tip = 4f * density
            val radii = if (mine) {
                floatArrayOf(r, r, r, r, tip, tip, r, r)
            } else {
                floatArrayOf(r, r, r, r, r, r, tip, tip)
            }
            return GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(color)
                cornerRadii = radii
                if (stroke) {
                    setStroke((1 * density).toInt().coerceAtLeast(1), NativeColors.ChatDivider)
                }
            }
        }

        private val DIFF = object : DiffUtil.ItemCallback<LocalMessage>() {
            override fun areItemsTheSame(a: LocalMessage, b: LocalMessage) = a.id == b.id
            override fun areContentsTheSame(a: LocalMessage, b: LocalMessage) =
                a.body == b.body &&
                    a.time == b.time &&
                    a.status == b.status &&
                    a.mine == b.mine &&
                    a.senderName == b.senderName &&
                    a.starred == b.starred &&
                    a.editedAtIso == b.editedAtIso &&
                    a.reactions == b.reactions &&
                    a.mediaUri == b.mediaUri &&
                    a.mediaKind == b.mediaKind &&
                    a.voiceDurationSec == b.voiceDurationSec &&
                    a.fileName == b.fileName &&
                    a.linkPreview == b.linkPreview &&
                    a.location == b.location &&
                    a.poll == b.poll &&
                    a.event == b.event &&
                    a.deletedForEveryone == b.deletedForEveryone &&
                    a.disappeared == b.disappeared
        }
    }
}

/** Load older history near the visual top (chronological list). */
class ChatOlderScrollListener(
    private val nearStart: Boolean = false,
    private val onNearStart: () -> Unit,
) : RecyclerView.OnScrollListener() {
    private var loading = false
    fun setLoading(v: Boolean) {
        loading = v
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (loading || dy == 0) return
        maybeLoad(recyclerView)
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (loading) return
        if (newState == RecyclerView.SCROLL_STATE_IDLE) maybeLoad(recyclerView)
    }

    private fun maybeLoad(recyclerView: RecyclerView) {
        val lm = recyclerView.layoutManager as? LinearLayoutManager ?: return
        val total = lm.itemCount
        if (total <= 0) return
        if (nearStart) {
            if (lm.findFirstVisibleItemPosition() <= 3) onNearStart()
        } else {
            if (lm.findLastVisibleItemPosition() >= total - 4) onNearStart()
        }
    }
}
