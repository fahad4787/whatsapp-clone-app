package com.aivora.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivora.app.ui.icons.AivoraIcons
import com.aivora.app.ui.motion.pressScale
import com.aivora.app.ui.theme.AivoraColors
import com.aivora.app.ui.theme.AivoraDimens

data class SettingsItem(
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector? = null,
    @DrawableRes val iconRes: Int? = null,
    val trailing: String? = null,
    val badge: String? = null,
    val toggle: Boolean? = null,
    val selected: Boolean = false,
    val danger: Boolean = false,
    val onClick: (() -> Unit)? = null,
    val onToggle: ((Boolean) -> Unit)? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AivoraScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
    fab: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    titleContent: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    if (titleContent != null) {
                        titleContent()
                    } else {
                        Text(
                            title,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = { actions() },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        floatingActionButton = fab,
        bottomBar = bottomBar,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding(),
            content = content,
        )
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = AivoraColors.Primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Composable
fun SettingsRow(item: SettingsItem) {
    val click = item.onClick
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (click != null) Modifier.clickable(onClick = click) else Modifier)
            .height(AivoraDimens.SettingsRowHeight)
            .padding(horizontal = AivoraDimens.Space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            item.iconRes != null -> {
                Icon(
                    painter = painterResource(item.iconRes),
                    contentDescription = null,
                    tint = AivoraColors.Ink,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(14.dp))
            }
            item.icon != null -> {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AivoraColors.Primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(item.icon, contentDescription = null, tint = AivoraColors.Primary, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(14.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal,
                color = if (item.danger) AivoraColors.Danger else AivoraColors.Ink,
            )
            if (item.subtitle != null) {
                Text(
                    item.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (item.badge != null) {
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(AivoraColors.Primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    item.badge,
                    color = AivoraColors.OnPrimary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        when {
            item.toggle != null -> Switch(
                checked = item.toggle,
                onCheckedChange = { item.onToggle?.invoke(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AivoraColors.OnPrimary,
                    checkedTrackColor = AivoraColors.Primary,
                    checkedBorderColor = AivoraColors.Primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            )
            item.selected -> Icon(Icons.Default.Check, contentDescription = null, tint = AivoraColors.Primary)
            item.trailing != null -> Text(item.trailing, color = MaterialTheme.colorScheme.onSurfaceVariant)
            click != null -> Icon(
                painter = painterResource(AivoraIcons.ChevronRight),
                contentDescription = null,
                tint = AivoraColors.IconSecondary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** Non-lazy settings card — safe inside verticalScroll parents (Group info). */
@Composable
fun SettingsCard(
    items: List<SettingsItem>,
    modifier: Modifier = Modifier,
    header: String? = null,
) {
    if (items.isEmpty()) return
    Column(modifier = modifier.padding(horizontal = AivoraDimens.CardHorizontal, vertical = AivoraDimens.Space2)) {
        if (header != null) {
            Text(
                header,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AivoraDimens.RadiusMd))
                .background(AivoraColors.SurfaceMuted),
        ) {
            items.forEachIndexed { index, row ->
                SettingsRow(row)
                if (index < items.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(
                            start = if (row.iconRes != null || row.icon != null) 54.dp else 16.dp,
                        ),
                        color = AivoraColors.Divider,
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsList(
    sections: List<Pair<String?, List<SettingsItem>>>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = AivoraDimens.CardHorizontal,
            vertical = AivoraDimens.Space2,
        ),
        verticalArrangement = Arrangement.spacedBy(AivoraDimens.Space2),
    ) {
        sections.forEach { (header, items) ->
            item {
                Column {
                    if (header != null) {
                        Text(
                            header,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                        )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(AivoraDimens.RadiusMd))
                            .background(AivoraColors.SurfaceMuted),
                    ) {
                        items.forEachIndexed { index, row ->
                            SettingsRow(row)
                            if (index < items.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(
                                        start = if (row.iconRes != null || row.icon != null) 54.dp else 16.dp,
                                    ),
                                    color = AivoraColors.Divider,
                                )
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
fun RadioGroup(
    options: List<Pair<String, String?>>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    navigableIndices: Set<Int> = emptySet(),
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AivoraDimens.CardHorizontal)
            .clip(RoundedCornerShape(AivoraDimens.RadiusMd))
            .background(AivoraColors.SurfaceMuted),
    ) {
        options.forEachIndexed { index, (title, subtitle) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(index) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = selected == index,
                    onClick = { onSelect(index) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = AivoraColors.Primary,
                        unselectedColor = MaterialTheme.colorScheme.outline,
                    ),
                )
                Column(modifier = Modifier.weight(1f).padding(vertical = 10.dp)) {
                    Text(title, style = MaterialTheme.typography.bodyLarge)
                    if (subtitle != null) {
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (index in navigableIndices) {
                    Icon(
                        painter = painterResource(AivoraIcons.ChevronRight),
                        contentDescription = null,
                        tint = AivoraColors.IconSecondary,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(20.dp),
                    )
                }
            }
            if (index < options.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 16.dp),
                    color = AivoraColors.Divider,
                )
            }
        }
    }
}

data class AivoraFilterOption(
    val label: String,
    val badge: Int? = null,
)

/**
 * Figma conversation filters (`15:905`): muted track, white elevated active segment.
 * Inactive = no fill / no outline; active = white + soft shadow + ink semibold.
 */
@Composable
fun AivoraFilterBar(
    options: List<AivoraFilterOption>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AivoraDimens.CardHorizontal)
            .height(44.dp)
            .clip(RoundedCornerShape(AivoraDimens.RadiusSm))
            .background(AivoraColors.SurfaceMuted)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEachIndexed { index, option ->
            val selected = index == selectedIndex
            val interaction = remember { MutableInteractionSource() }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .then(
                        if (selected) {
                            Modifier
                                .shadow(
                                    elevation = 3.dp,
                                    shape = RoundedCornerShape(4.dp),
                                    ambientColor = Color(0xFF292341).copy(alpha = 0.08f),
                                    spotColor = Color(0xFF292341).copy(alpha = 0.08f),
                                )
                                .clip(RoundedCornerShape(4.dp))
                                .background(AivoraColors.LightSurface)
                        } else {
                            Modifier.clip(RoundedCornerShape(14.dp))
                        },
                    )
                    .clickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = { onSelect(index) },
                    ),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    option.label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (selected) AivoraColors.InkMuted else Color(0xFF72748D),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val badge = option.badge
                if (badge != null && badge > 0) {
                    Spacer(Modifier.width(5.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(AivoraColors.Primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (badge > 99) "99+" else "$badge",
                            color = AivoraColors.OnPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Search",
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
    )
}

private fun avatarBrush(name: String): Brush {
    val hues = listOf(
        AivoraColors.Primary to AivoraColors.PrimaryDark,
        AivoraColors.Blue to AivoraColors.BlueLight,
        Color(0xFF0D9488) to Color(0xFF115E59),
        Color(0xFF4F46E5) to Color(0xFF312E81),
        Color(0xFFDB2777) to Color(0xFF9D174D),
    )
    val pair = hues[kotlin.math.abs(name.hashCode()) % hues.size]
    return Brush.linearGradient(listOf(pair.first, pair.second))
}

@Composable
fun Avatar(
    name: String,
    size: Int = 48,
    color: Color = AivoraColors.Primary,
    modifier: Modifier = Modifier,
) {
    val brush = remember(name) { avatarBrush(name.ifBlank { "A" }) }
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(brush),
        contentAlignment = Alignment.Center,
    ) {
        if (name.isBlank()) {
            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
        } else {
            Text(
                name.trim().first().uppercaseChar().toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
}

@Composable
fun PersonRow(
    name: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interaction)
            .clickable(
                interactionSource = interaction,
                indication = ripple(),
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(name)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun EmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(
                icon,
                contentDescription = null,
                tint = AivoraColors.Ink,
                modifier = Modifier.size(48.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @DrawableRes iconRes: Int? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interaction,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .pressScale(interaction, pressedScale = 0.98f),
        colors = ButtonDefaults.buttonColors(
            containerColor = AivoraColors.Primary,
            contentColor = AivoraColors.OnPrimary,
            disabledContainerColor = Color(0xFFE9ECEA),
            disabledContentColor = AivoraColors.TextTertiary,
        ),
        shape = RoundedCornerShape(AivoraDimens.RadiusMd),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(10.dp))
        }
        Text(text, fontWeight = FontWeight.Medium, fontSize = 16.sp)
    }
}

@Composable
fun LinkButton(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(text, color = AivoraColors.Primary)
    }
}

@Composable
fun InfoBanner(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

