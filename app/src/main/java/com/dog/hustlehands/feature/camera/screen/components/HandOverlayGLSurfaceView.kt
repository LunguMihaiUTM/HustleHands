package com.dog.hustlehands.feature.camera.screen.components

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.dog.hustlehands.domain.model.DomainHandLandmark
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class HandOverlayGLSurfaceView(context: Context, attrs: AttributeSet? = null) :
    GLSurfaceView(context, attrs) {

    private val renderer: HandOverlayRenderer

    init {
        // ✅ MUST set EGL config BEFORE setRenderer
        setEGLContextClientVersion(2)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        holder.setFormat(android.graphics.PixelFormat.TRANSLUCENT)
        setZOrderOnTop(true)

        // ✅ Set renderer LAST
        renderer = HandOverlayRenderer()
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun setTransform(viewWidth: Float, imageHeight: Float, offsetY: Float) {
        renderer.setTransform(viewWidth, imageHeight, offsetY)
        requestRender()
    }

    fun setLandmarks(landmarks: List<DomainHandLandmark>) {
        renderer.setLandmarks(landmarks)
        requestRender()
    }
}

class HandOverlayRenderer : GLSurfaceView.Renderer {

    @Volatile
    private var landmarks: List<DomainHandLandmark> = emptyList()

    @Volatile
    private var imageWidth = 1f
    @Volatile
    private var imageHeight = 1f
    @Volatile
    private var verticalOffset = 0f
    @Volatile
    private var viewWidth = 1f
    @Volatile
    private var viewHeight = 1f

    private val connections = listOf(
        0 to 1, 1 to 2, 2 to 3, 3 to 4,
        0 to 5, 5 to 6, 6 to 7, 7 to 8,
        0 to 9, 9 to 10, 10 to 11, 11 to 12,
        0 to 13, 13 to 14, 14 to 15, 15 to 16,
        0 to 17, 17 to 18, 18 to 19, 19 to 20,
        5 to 9, 9 to 13, 13 to 17
    )

    fun setTransform(width: Float, height: Float, offset: Float) {
        imageWidth = width
        imageHeight = height
        verticalOffset = offset
        android.util.Log.d("HandOverlayGL", "Transform: width=$width, height=$height, offset=$offset")
    }

    fun setLandmarks(newLandmarks: List<DomainHandLandmark>) {
        landmarks = newLandmarks
        android.util.Log.d("HandOverlayGL", "Setting ${landmarks.size} landmarks")
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        gl?.apply {
            glClearColor(0f, 0f, 0f, 0f) // Transparent background
            glEnable(GL10.GL_BLEND)
            glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA)
            glDisable(GL10.GL_DEPTH_TEST)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        viewWidth = width.toFloat()
        viewHeight = height.toFloat()

        gl?.apply {
            glViewport(0, 0, width, height)
            glMatrixMode(GL10.GL_PROJECTION)
            glLoadIdentity()
            glOrthof(0f, width.toFloat(), height.toFloat(), 0f, -1f, 1f)
            glMatrixMode(GL10.GL_MODELVIEW)
            glLoadIdentity()
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        gl?.apply {
            glClear(GL10.GL_COLOR_BUFFER_BIT)

            if (landmarks.isEmpty()) {
                android.util.Log.d("HandOverlayGL", "No landmarks to draw")
                return
            }

            android.util.Log.d("HandOverlayGL", "Drawing ${landmarks.size} landmarks")

            val handGroups = landmarks.groupBy { it.handIndex }

            handGroups.forEach { (handIndex, handLandmarks) ->
                // Set color based on hand index
                if (handIndex == 0) {
                    glColor4f(0f, 1f, 0f, 1f) // Green for first hand
                } else {
                    glColor4f(0f, 0f, 1f, 1f) // Blue for second hand
                }

                // Draw points
                glPointSize(16f) // Increased size for visibility
                glEnableClientState(GL10.GL_VERTEX_ARRAY)

                val pointVertices = FloatArray(handLandmarks.size * 2)
                handLandmarks.forEachIndexed { index, landmark ->
                    val x = landmark.x * imageWidth
                    val y = landmark.y * imageHeight + verticalOffset
                    pointVertices[index * 2] = x
                    pointVertices[index * 2 + 1] = y

                    if (index == 0) {
                        android.util.Log.d("HandOverlayGL", "First point: x=$x, y=$y (landmark: ${landmark.x}, ${landmark.y})")
                    }
                }

                // Create native direct buffer
                val pointBuffer = java.nio.ByteBuffer.allocateDirect(pointVertices.size * 4)
                    .order(java.nio.ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(pointVertices)
                pointBuffer.position(0)

                glVertexPointer(2, GL10.GL_FLOAT, 0, pointBuffer)
                glDrawArrays(GL10.GL_POINTS, 0, handLandmarks.size)

                // Draw lines
                glLineWidth(6f) // Increased width for visibility
                val landmarkMap = handLandmarks.associateBy { it.landmarkIndex }

                connections.forEach { (start, end) ->
                    val a = landmarkMap[start]
                    val b = landmarkMap[end]
                    if (a != null && b != null) {
                        val lineVertices = floatArrayOf(
                            a.x * imageWidth,
                            a.y * imageHeight + verticalOffset,
                            b.x * imageWidth,
                            b.y * imageHeight + verticalOffset
                        )

                        // Create native direct buffer for lines
                        val lineBuffer = java.nio.ByteBuffer.allocateDirect(lineVertices.size * 4)
                            .order(java.nio.ByteOrder.nativeOrder())
                            .asFloatBuffer()
                            .put(lineVertices)
                        lineBuffer.position(0)

                        glVertexPointer(2, GL10.GL_FLOAT, 0, lineBuffer)
                        glDrawArrays(GL10.GL_LINES, 0, 2)
                    }
                }

                glDisableClientState(GL10.GL_VERTEX_ARRAY)
            }
        }
    }
}