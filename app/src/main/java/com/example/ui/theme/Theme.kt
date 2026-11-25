package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = OnEmeraldPrimary,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = OnEmeraldContainer,
    secondary = GoldSecondary,
    onSecondary = OnGoldSecondary,
    secondaryContainer = GoldContainer,
    onSecondaryContainer = OnGoldContainer,
    tertiary = SlateTertiary,
    onTertiary = OnSlateTertiary,
    tertiaryContainer = SlateContainer,
    onTertiaryContainer = OnSlateContainer,
    background = SurfaceIvory,
    onBackground = OnSurfaceDark,
    surface = SurfaceCard,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineSubtle,
    error = StatusAbsent,
    onError = OnEmeraldPrimary,
    errorContainer = StatusAbsentContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkEmeraldPrimary,
    onPrimary = DarkOnEmeraldPrimary,
    primaryContainer = DarkEmeraldContainer,
    onPrimaryContainer = DarkOnEmeraldContainer,
    secondary = DarkGoldSecondary,
    onSecondary = DarkOnGoldSecondary,
    secondaryContainer = DarkGoldContainer,
    onSecondaryContainer = DarkOnGoldContainer,
    background = DarkSurface,
    onBackground = DarkOnSurface,
    surface = DarkSurfaceCard,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceMuted,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = StatusAbsent,
    errorContainer = StatusAbsentContainer
)

val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun JalalAbadCollegeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
