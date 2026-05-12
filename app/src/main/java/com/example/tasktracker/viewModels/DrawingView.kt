package com.example.tasktracker.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

class DrawingView : View {
    private val TAG = "DrawingView"
    private var paint = Paint()
    private var currentTool = "pen"
    private var currentColor = Color.BLACK
    private var currentStrokeWidth = 10f

    private val paths = mutableListOf<Pair<Path, Paint>>()
    private var currentPath = Path()

    private var canvasBitmap: Bitmap? = null
    private var canvasPaint: Paint? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        paint.isAntiAlias = true
        paint.isDither = true
        paint.color = currentColor
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = currentStrokeWidth
        isClickable = true
        isFocusable = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (w > 0 && h > 0) {
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            canvasPaint = Paint()
            val canvas = Canvas(canvasBitmap!!)
            canvas.drawColor(Color.WHITE)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (canvasBitmap != null) {
            canvas.drawBitmap(canvasBitmap!!, 0f, 0f, canvasPaint)
        }

        paths.forEach { (path, paint) ->
            canvas.drawPath(path, paint)
        }

        canvas.drawPath(currentPath, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentPath = Path()
                currentPath.moveTo(x, y)
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                currentPath.lineTo(x, y)
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (currentPath.isNotEmpty) {
                    paths.add(Pair(currentPath, Paint(paint)))
                }
                currentPath = Path()
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun setTool(tool: String) {
        currentTool = tool
        when (tool) {
            "pen" -> {
                paint.color = currentColor
                paint.strokeWidth = currentStrokeWidth
            }
            "eraser" -> {
                paint.color = Color.WHITE
                paint.strokeWidth = currentStrokeWidth * 2
            }
        }
    }

    fun setColor(color: Int) {
        currentColor = color
        if (currentTool == "pen") {
            paint.color = currentColor
        }
    }

    fun setStrokeWidth(width: Float) {
        currentStrokeWidth = width
        paint.strokeWidth = width
    }

    fun saveDrawing(): Bitmap? {
        if (canvasBitmap == null) {
            Log.e(TAG, "canvasBitmap is null")
            return null
        }

        val bitmap = Bitmap.createBitmap(canvasBitmap!!.width, canvasBitmap!!.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawBitmap(canvasBitmap!!, 0f, 0f, null)

        paths.forEach { (path, paint) ->
            canvas.drawPath(path, paint)
        }

        canvas.drawPath(currentPath, paint)

        return bitmap
    }

    fun clearCanvas() {
        paths.clear()
        currentPath = Path()
        if (canvasBitmap != null) {
            val canvas = Canvas(canvasBitmap!!)
            canvas.drawColor(Color.WHITE)
        }
        invalidate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        canvasBitmap?.recycle()
        canvasBitmap = null
    }

    private val Path.isNotEmpty: Boolean
        get() = !isEmpty
}