package com.mapbox.vision.ar.view.gl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import com.mapbox.vision.BuildConfig
import com.mapbox.vision.ar.ARDataProvider
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

internal class GlArRender(
        private val context: Context,
        private val width: Int,
        private val height: Int
) : GLSurfaceView.Renderer {

    interface OnSurfaceChangedListener {
        fun onSurfaceChanged()
    }

    private val lane by lazy { ArLane(context) }
    private val background by lazy { ArBackground(width, height) }
    private var viewAspectRatio : Float? = 0f

    private var arDataProvider: ARDataProvider? = null

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);

        lane.onSurfaceChanged()
        background.onSurfaceChanged()
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        // Adjust the viewport based on geometry changes, such as screen rotation
        GLES20.glViewport(0, 0, width, height)
        viewAspectRatio = width.toFloat() / height
    }

    override fun onDrawFrame(unused: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)

        background.draw()

        val cameraData = arDataProvider?.getCameraParams() ?: return
        val laneParamsData = arDataProvider?.getARRouteData() ?: return

        val camera = ArCamera(
                verticalFOVRadians = cameraData[0],
                aspectRatio = cameraData[1],
                viewAspectRatio = viewAspectRatio!!,
                rotation = Rotation(
                        roll = cameraData[2],
                        pitch = cameraData[3],
                        yaw = cameraData[4]
                ),
                translate = Vector3(
                        x = 0f,
                        y = cameraData[5],
                        z = 0f
                )
        )
        val laneParams = FloatArray(laneParamsData.size) { index ->
            // transform to correct coordinate system x,y,z = x,z,-y
            when (index % 3) {
                1 -> laneParamsData[index + 1] // y = z
                2 -> -laneParamsData[index - 1] // z = -y
                else -> laneParamsData[index]  // x = x
            }.toFloat()
        }

        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        val viewProjMatrix = camera.getViewProjectionMatrix()
        val modelMatrix = Matrix4()
        val normMatrix = modelMatrix.toMatrix3()

        lane.draw(viewProjMatrix, modelMatrix, normMatrix, laneParams)
    }

    internal fun onNewBackgroundSource(byteArray: ByteArray) {
        background.updateTexture(byteArray)
    }

    internal fun setARDataProvider(arDataProvider: ARDataProvider) {
        this.arDataProvider = arDataProvider
    }

    companion object {

        private const val TAG = "GlArRender"

        /**
         * Utility method for compiling a OpenGL shader.
         *
         *
         * **Note:** When developing shaders, use the checkGlError()
         * method to debug shader coding errors.
         *
         * @param type - Vertex or fragment shader type.
         * @param shaderCode - String containing the shader code.
         * @return - Returns an id for the shader.
         */
        @JvmStatic
        fun loadShader(type: Int, shaderCode: String): Int {

            // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
            // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
            val shader = GLES20.glCreateShader(type)

            // add the source code to the shader and compile it
            GLES20.glShaderSource(shader, shaderCode)
            checkGlError("load shader")
            GLES20.glCompileShader(shader)
            checkGlError("compile shader")

            return shader
        }

        /**
         * Utility method for debugging OpenGL calls. Provide the name of the call
         * just after making it:
         *
         * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")
         *
         * MyGLRenderer.checkGlError("glGetUniformLocation")
         *
         * If the operation is not successful, the check throws an error.
         *
         * @param glOperation - Name of the OpenGL call to check.
         */
        @JvmStatic
        fun checkGlError(glOperation: String) {
            if (BuildConfig.DEBUG) {
                var error = 0
                while ({ error = GLES20.glGetError();error }() != GLES20.GL_NO_ERROR) {
                    Log.e(TAG, "$glOperation: glError $error")
                    throw RuntimeException("$glOperation: glError $error")
                }
            }
        }
    }
}
