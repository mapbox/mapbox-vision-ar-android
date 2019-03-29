package com.mapbox.vision.ar

import com.mapbox.vision.ar.core.models.Color
import com.mapbox.vision.mobile.core.models.world.WorldCoordinate

/**
 * Parameters used by [com.mapbox.vision.ar.view.gl.VisionArView] to draw AR lane.
 *
 * @property color RGBA color of a lane
 * @property width width of lane in meters
 * @property light position of light source
 * @property lightColor RGBA color of a light source
 * @property ambientColor ambient color
 */
class LaneVisualParams(
    val color: Color,
    val width: Double,
    val light: WorldCoordinate?,
    val lightColor: Color,
    val ambientColor: Color
) {
    companion object {
        @JvmStatic
        fun isValid(color: Color): Boolean {
            if (color.a !in (0f..1f)) return false
            if (color.r !in (0f..1f)) return false
            if (color.g !in (0f..1f)) return false
            if (color.b !in (0f..1f)) return false

            return true
        }

        @JvmStatic
        fun isValid(width: Double): Boolean = width >= 0.0
    }
}
