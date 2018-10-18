package com.mapbox.vision.ar.view.gl

import android.content.Context
import android.graphics.PixelFormat
import android.location.Location
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.mapbox.services.android.navigation.v5.offroute.OffRouteListener
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress
import com.mapbox.vision.VideoStreamListener
import com.mapbox.vision.VisionManager
import com.mapbox.vision.models.route.NavigationRoute
import com.mapbox.vision.models.route.RoutePoint
import com.mapbox.vision.performance.ModelPerformance
import com.mapbox.vision.performance.ModelPerformanceConfig
import com.mapbox.vision.performance.ModelPerformanceMode
import com.mapbox.vision.performance.ModelPerformanceRate

/**
 * Draws AR navigation route on top of the video stream from camera.
 */
class GlArView : GLSurfaceView, VideoStreamListener, ProgressChangeListener, OffRouteListener {

    private val render: GlArRender
    private var needToUpdateRoute: Boolean = true

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val frameSize = VisionManager.getFrameSize()
        render = GlArRender(
                context = context,
                width = frameSize.width,
                height = frameSize.height
        )
        render.setARDataProvider(VisionManager)

        setEGLContextClientVersion(2)

        holder.setFormat(PixelFormat.RGBA_8888)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)

        setRenderer(render)

        renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        VisionManager.setVideoStreamListener(this)
        VisionManager.setModelPerformanceConfig(
                ModelPerformanceConfig.Merged(
                        performance = ModelPerformance.On(ModelPerformanceMode.DYNAMIC, ModelPerformanceRate.LOW)
                )
        )
    }

    override fun onNewFrame(byteArray: ByteArray) {
        render.onNewBackgroundSource(byteArray)
    }

    override fun onProgressChange(location: Location?, routeProgress: RouteProgress?) {
        if (routeProgress == null) {
            VisionManager.stopNavigation()
            return
        }
        if (needToUpdateRoute) {
            VisionManager.startNavigation(routeProgress.toNavigationRoute())
            needToUpdateRoute = false
        }
    }

    override fun userOffRoute(location: Location?) {
        needToUpdateRoute = true
    }

    private fun RouteProgress.toNavigationRoute(): NavigationRoute {
        val routePoints: ArrayList<RoutePoint> = ArrayList()
        this.directionsRoute()?.legs()?.forEach { it ->
            it.steps()?.forEach { step ->
                val maneuverPoint = RoutePoint(
                        latitude = step.maneuver().location().latitude(),
                        longitude = step.maneuver().location().longitude(),
                        isManeuver = true
                )
                routePoints.add(maneuverPoint)

                step.intersections()
                        ?.map {

                            RoutePoint(
                                    latitude = it.location().latitude(),
                                    longitude = it.location().longitude(),
                                    isManeuver = false
                            )
                        }
                        ?.let { stepPoints ->
                            routePoints.addAll(stepPoints)
                        }
            }
        }
        return NavigationRoute(routePoints)
    }
}
