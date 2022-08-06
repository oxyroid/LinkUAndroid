package com.linku.im.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path

val Icons.Inbox: ImageVector
    get() {
        if (_inbox != null) {
            return _inbox!!
        }
        _inbox = materialIcon(name = "Inbox") {
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                fillAlpha = 1.0F,
                strokeAlpha = 1.0F,
                strokeLineWidth = 0.0F,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 4.0F,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(19.0F, 3.0F)
                horizontalLineTo(5.0F)
                curveTo(3.9F, 3.0F, 3.0F, 3.9F, 3.0F, 5.0F)
                verticalLineToRelative(14.0F)
                curveToRelative(0.0F, 1.1F, 0.9F, 2.0F, 2.0F, 2.0F)
                horizontalLineToRelative(14.0F)
                curveToRelative(1.1F, 0.0F, 2.0F, -0.9F, 2.0F, -2.0F)
                verticalLineTo(5.0F)
                curveTo(21.0F, 3.9F, 20.1F, 3.0F, 19.0F, 3.0F)

                moveTo(19.0F, 5.0F)
                verticalLineToRelative(9.0F)
                horizontalLineToRelative(-3.56F)
                curveToRelative(-0.36F, 0.0F, -0.68F, 0.19F, -0.86F, 0.5F)
                curveTo(14.06F, 15.4F, 13.11F, 16.0F, 12.0F, 16.0F)
                reflectiveCurveToRelative(-2.06F, -0.6F, -2.58F, -1.5F)
                curveTo(9.24F, 14.19F, 8.91F, 14.0F, 8.56F, 14.0F)
                horizontalLineTo(5.0F)
                verticalLineTo(5.0F)
                horizontalLineTo(19.0F)
                close()
            }
        }
        return _inbox!!
    }

private var _inbox: ImageVector? = null
