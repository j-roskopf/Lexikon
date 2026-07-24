package com.joetr.lexikon.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object LexikonIcons {
    val Stats: ImageVector by lazy {
        ImageVector.Builder(
            name = "Stats",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF333333))) {
                moveTo(4f, 20f)
                horizontalLineTo(8f)
                verticalLineTo(10f)
                horizontalLineTo(4f)
                close()
                moveTo(10f, 20f)
                horizontalLineTo(14f)
                verticalLineTo(4f)
                horizontalLineTo(10f)
                close()
                moveTo(16f, 20f)
                horizontalLineTo(20f)
                verticalLineTo(13f)
                horizontalLineTo(16f)
                close()
            }
        }.build()
    }

    val Settings: ImageVector by lazy {
        ImageVector.Builder(
            name = "Settings",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF333333))) {
                moveTo(19.14f, 12.94f)
                curveTo(19.18f, 12.63f, 19.2f, 12.32f, 19.2f, 12f)
                curveTo(19.2f, 11.68f, 19.18f, 11.37f, 19.14f, 11.06f)
                lineTo(21.16f, 9.48f)
                curveTo(21.34f, 9.34f, 21.39f, 9.08f, 21.28f, 8.87f)
                lineTo(19.36f, 5.55f)
                curveTo(19.24f, 5.34f, 18.99f, 5.26f, 18.77f, 5.35f)
                lineTo(16.39f, 6.31f)
                curveTo(15.9f, 5.93f, 15.36f, 5.62f, 14.78f, 5.38f)
                lineTo(14.42f, 2.85f)
                curveTo(14.39f, 2.61f, 14.19f, 2.44f, 13.95f, 2.44f)
                lineTo(10.05f, 2.44f)
                curveTo(9.81f, 2.44f, 9.61f, 2.61f, 9.58f, 2.85f)
                lineTo(9.22f, 5.38f)
                curveTo(8.64f, 5.62f, 8.1f, 5.94f, 7.61f, 6.31f)
                lineTo(5.23f, 5.35f)
                curveTo(5.01f, 5.26f, 4.76f, 5.34f, 4.64f, 5.55f)
                lineTo(2.72f, 8.87f)
                curveTo(2.61f, 9.08f, 2.66f, 9.34f, 2.84f, 9.48f)
                lineTo(4.86f, 11.06f)
                curveTo(4.82f, 11.37f, 4.8f, 11.69f, 4.8f, 12f)
                curveTo(4.8f, 12.31f, 4.82f, 12.63f, 4.86f, 12.94f)
                lineTo(2.84f, 14.52f)
                curveTo(2.66f, 14.66f, 2.61f, 14.92f, 2.72f, 15.13f)
                lineTo(4.64f, 18.45f)
                curveTo(4.76f, 18.66f, 5.01f, 18.74f, 5.23f, 18.65f)
                lineTo(7.61f, 17.69f)
                curveTo(8.1f, 18.07f, 8.64f, 18.38f, 9.22f, 18.62f)
                lineTo(9.58f, 21.15f)
                curveTo(9.61f, 21.39f, 9.81f, 21.56f, 10.05f, 21.56f)
                lineTo(13.95f, 21.56f)
                curveTo(14.19f, 21.56f, 14.39f, 21.39f, 14.42f, 21.15f)
                lineTo(14.78f, 18.62f)
                curveTo(15.36f, 18.38f, 15.9f, 18.06f, 16.39f, 17.69f)
                lineTo(18.77f, 18.65f)
                curveTo(18.99f, 18.74f, 19.24f, 18.66f, 19.36f, 18.45f)
                lineTo(21.28f, 15.13f)
                curveTo(21.39f, 14.92f, 21.34f, 14.66f, 21.16f, 14.52f)
                lineTo(19.14f, 12.94f)
                close()
                moveTo(12f, 15.5f)
                curveTo(10.07f, 15.5f, 8.5f, 13.93f, 8.5f, 12f)
                curveTo(8.5f, 10.07f, 10.07f, 8.5f, 12f, 8.5f)
                curveTo(13.93f, 8.5f, 15.5f, 10.07f, 15.5f, 12f)
                curveTo(15.5f, 13.93f, 13.93f, 15.5f, 12f, 15.5f)
                close()
            }
        }.build()
    }

    val Enter: ImageVector by lazy {
        ImageVector.Builder(
            name = "Enter",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(19f, 7f)
                verticalLineTo(11f)
                horizontalLineTo(5.83f)
                lineTo(9.41f, 7.41f)
                lineTo(8f, 6f)
                lineTo(2f, 12f)
                lineTo(8f, 18f)
                lineTo(9.41f, 16.59f)
                lineTo(5.83f, 13f)
                horizontalLineTo(21f)
                verticalLineTo(7f)
                horizontalLineTo(19f)
                close()
            }
        }.build()
    }

    val Backspace: ImageVector by lazy {
        ImageVector.Builder(
            name = "Backspace",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color(0xFF333333))) {
                moveTo(22f, 3f)
                horizontalLineTo(7f)
                curveTo(6.31f, 3f, 5.7f, 3.36f, 5.36f, 3.91f)
                lineTo(0f, 12f)
                lineTo(5.36f, 20.09f)
                curveTo(5.7f, 20.64f, 6.31f, 21f, 7f, 21f)
                horizontalLineTo(22f)
                curveTo(23.1f, 21f, 24f, 20.1f, 24f, 19f)
                verticalLineTo(5f)
                curveTo(24f, 3.9f, 23.1f, 3f, 22f, 3f)
                close()
                moveTo(19f, 15.59f)
                lineTo(17.59f, 17f)
                lineTo(14f, 13.41f)
                lineTo(10.41f, 17f)
                lineTo(9f, 15.59f)
                lineTo(12.59f, 12f)
                lineTo(9f, 8.41f)
                lineTo(10.41f, 7f)
                lineTo(14f, 10.59f)
                lineTo(17.59f, 7f)
                lineTo(19f, 8.41f)
                lineTo(15.41f, 12f)
                lineTo(19f, 15.59f)
                close()
            }
        }.build()
    }
}
