package com.example.circleviewtest

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import kotlin.math.*


class CircleSectorView : View {

    interface OnSectorSelectListener {
        fun onSectorSelected(name: String)
        fun onSectorUnselected(name: String)
    }

    private var smallRadrius = 0.7
    private var selectedRadius = 1.0

    private val paint = Paint().apply {
        color = Color.BLUE
    }

    private val touchListener = OnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val newSector = getSector(event.x, event.y)
                if (selectedSectorPosition != newSector) {
                    sectors[newSector].focused = true
                    selectedSectorPosition?.also {
                        sectors[it].focused = false
                    }
                    selectedSectorPosition = newSector
                    invalidate()
                }
                true
            }
            MotionEvent.ACTION_UP -> {
                selectedSectorPosition?.also {
                    sectors[it].selected = !sectors[it].selected
                    sectors[it].focused = false
                    animate(it)
                    if (sectors[it].selected)
                        listener?.onSectorSelected(sectors[it].name)
                    else
                        listener?.onSectorUnselected(sectors[it].name)
                }
                selectedSectorPosition = null
                invalidate()
                true
            }
            else -> false
        }
    }

    private var selectedSectorPosition: Int? = null
    private var sectors: List<Sector> = emptyList()
    private var listener: OnSectorSelectListener? = null

    private var iconsBitmaps: List<Bitmap> = emptyList()

    private var squareSize = 0

    constructor(context: Context) : super(context) {
        setOnTouchListener(touchListener)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setOnTouchListener(touchListener)
    }

    fun setSectorsNames(sectorsNames: List<String>) {
        cancelAnimation()
        sectors = sectorsNames.map { Sector(it, smallRadrius) }
        if (isLaidOut)
            invalidate()
    }

    fun setIconsDrawable(icons: List<Int>) {
        iconsBitmaps = icons.map { BitmapFactory.decodeResource(context.resources, it) }
    }

    fun setOnSectorSelectListener(listener: OnSectorSelectListener) {
        this.listener = listener
    }

    private fun animate(position: Int) {
        sectors[position].animator?.cancel()
        val targetRadius =
            if (sectors[position].selected) selectedRadius.toFloat() else smallRadrius.toFloat()
        val animationDuration =
            400 * abs(sectors[position].radius - targetRadius) / (selectedRadius - smallRadrius)
        sectors[position].animator =
            ValueAnimator.ofFloat(sectors[position].radius.toFloat(), targetRadius).apply {
                duration = animationDuration.toLong()
                addUpdateListener {
                    sectors[position].radius = (it.animatedValue as Float).toDouble()
                    invalidate()
                }
                start()
            }
    }

    private fun getSector(touchX: Float, touchY: Float): Int {
        val centerPositionX = touchX - (squareSize / 2)
        val centerPositionY = touchY - (squareSize / 2)
        val tg = centerPositionY / centerPositionX
        var eng = atan(tg.toDouble()) + (Math.PI / 2)
        if (centerPositionX < 0)
            eng += Math.PI
        val sectorSize = (2 * Math.PI) / if (sectors.isNotEmpty()) sectors.size else 1
        return (eng / sectorSize).nextUp().toInt()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = min(measuredHeight, measuredWidth)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) {
            super.onDraw(canvas)
            return
        }
        squareSize = min(width, height)
        val dif = selectedRadius - smallRadrius
        sectors.forEachIndexed { i, sector ->
            paint.color = Color.rgb(
                (255 * (sector.radius - smallRadrius) / dif).toInt(),
                130,
                (255 * (selectedRadius - sector.radius) / dif).toInt()
            )
            val ang =
                (((2 * Math.PI) / (if (sectors.isNotEmpty()) sectors.size else 1)) / Math.PI * 180).toFloat()
            canvas.drawArc(
                RectF(
                    (squareSize * (1 - sector.radius)).toFloat() / 2,
                    (squareSize * (1 - sector.radius)).toFloat() / 2,
                    (squareSize * sector.radius + (squareSize * (1 - sector.radius)) / 2).toFloat(),
                    (squareSize * sector.radius + (squareSize * (1 - sector.radius)) / 2).toFloat()
                ),
                ang * i - 90,
                ang,
                true,
                paint
            )
            canvas.drawBitmap(
                iconsBitmaps[i],
                (squareSize * 0.45 + squareSize * 0.4 * sector.radius * cos(ang * (-i + 0.7) * Math.PI / 180)).toFloat(),
                (squareSize * 0.45 - squareSize * 0.4 * sector.radius * sin(ang * (-i + 0.7) * Math.PI / 180)).toFloat(),
                paint
            )
        }
        /*selectedSectorPosition?.also {
            canvas.drawArc(
                RectF(
                    (squareSize * (1 - sectors[it].radius)).toFloat() / 2,
                    (squareSize * (1 - sectors[it].radius)).toFloat() / 2,
                    (squareSize * sectors[it].radius + (squareSize * (1 - sectors[it].radius)) / 2).toFloat(),
                    (squareSize * sectors[it].radius + (squareSize * (1 - sectors[it].radius)) / 2).toFloat()
                ),
                ((((2 * Math.PI) / (if (sectors.isNotEmpty()) sectors.size else 1) * it) / Math.PI * 180) - 90).toFloat(),
                (((2 * Math.PI) / (if (sectors.isNotEmpty()) sectors.size else 1)) / Math.PI * 180).toFloat(),
                true,
                paint
            )
        }*/
        paint.color = Color.BLACK
        sectors.forEachIndexed { i, sector ->
            val maxRadius =
                max(sector.radius, if (i == 0) sectors.last().radius else sectors[i - 1].radius)
            val angle =
                ((2 * Math.PI) / (if (sectors.isNotEmpty()) sectors.size else 1) * i - Math.PI / 2)
            canvas.drawLine(
                (squareSize / 2).toFloat(), (squareSize / 2).toFloat(),
                (squareSize / 2 * (1 + cos(angle) * maxRadius)).toFloat(),
                (squareSize / 2 * (1 + sin(angle) * maxRadius)).toFloat(),
                paint
            )
        }
        super.onDraw(canvas)
    }

    private class Sector(val name: String, var radius: Double) {
        var selected = false
        var focused = false
        var animator: ValueAnimator? = null
    }

    private fun cancelAnimation() {
        sectors.forEach {
            it.animator?.cancel()
            it.radius = if (it.selected) selectedRadius else smallRadrius
        }
    }
}