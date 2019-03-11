package com.mapbox.vision.ar

import com.mapbox.vision.ar.models.Color
import com.mapbox.vision.mobile.models.world.WorldCoordinate

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
    val light: WorldCoordinate,
    val lightColor: Color,
    val ambientColor: Color
)
