package com.example.circleviewtest

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
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

    private var defaultColor = Color.RED
    private var selectedColor = Color.BLUE

    private val paint = Paint().apply {
        color = Color.BLUE
    }

    private var selectedSectorPosition: Int? = null
    private var sectors: List<Sector> = emptyList()
    private var listener: OnSectorSelectListener? = null

    private var iconsBitmaps: List<Bitmap> = emptyList()

    private var squareSize = 0

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return when (event?.action) {
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
                selectedSectorPosition?.let { sectors[it] }?.apply {
                    selected = !selected
                    focused = false
                    animate(this)
                    if (selected)
                        listener?.onSectorSelected(name)
                    else
                        listener?.onSectorUnselected(name)
                }
                selectedSectorPosition = null
                invalidate()
                true
            }
            else -> false
        }
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    fun setSectorsNames(sectorsNames: List<String>) {
        cancelAnimation()
        sectors = sectorsNames.map { Sector(it, smallRadrius, defaultColor) }
        if (isLaidOut)
            invalidate()
    }

    fun setIconsDrawable(icons: List<Int>) {
        iconsBitmaps = icons.map { BitmapFactory.decodeResource(context.resources, it) }
    }

    fun setOnSectorSelectListener(listener: OnSectorSelectListener) {
        this.listener = listener
    }

    private fun animate(sector: Sector) {
        sector.animator?.cancel()
        val targetRadius =
            if (sector.selected) selectedRadius.toFloat() else smallRadrius.toFloat()
        val animationDuration =
            400 * abs(sector.radius - targetRadius) / (selectedRadius - smallRadrius)

        val colorAnimator = ValueAnimator.ofArgb(sector.color, if (sector.selected) selectedColor else defaultColor)
                .apply {
                    duration = animationDuration.toLong()
                    addUpdateListener {
                        sector.color = it.animatedValue as Int
                    }
                }
        val radiusAnimator = ValueAnimator.ofFloat(sector.radius.toFloat(), targetRadius).apply {
            duration = animationDuration.toLong()
            addUpdateListener {
                sector.radius = (it.animatedValue as Float).toDouble()
                invalidate()
            }
        }
        sector.animator = AnimatorSet()
        sector.animator!!.apply {
            playTogether(radiusAnimator,colorAnimator)
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
        val iconsRadiusOffset = 0.4
        sectors.forEachIndexed { i, sector ->
            paint.color = sector.color
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
            val icon = iconsBitmaps[i]
            val sectorCenterAngle = ang * (-i - 0.5) + 90
            canvas.drawBitmap(
                icon,
                (squareSize / 2 + squareSize * iconsRadiusOffset * sector.radius * cos(
                    sectorCenterAngle * Math.PI / 180
                ) - icon.width / 2).toFloat(),
                (squareSize / 2 - squareSize * iconsRadiusOffset * sector.radius * sin(
                    sectorCenterAngle * Math.PI / 180
                ) - icon.height / 2).toFloat(),
                paint
            )
        }
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

    private class Sector(val name: String, var radius: Double, var color: Int) {
        var selected = false
        var focused = false
        var animator: AnimatorSet? = null
    }

    private fun cancelAnimation() {
        sectors.forEach {
            it.animator?.cancel()
            it.radius = if (it.selected) selectedRadius else smallRadrius
        }
    }
}