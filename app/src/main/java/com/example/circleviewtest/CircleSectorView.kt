package com.example.circleviewtest

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
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

    private var selectedSectorPosition: Int? = null
    private var sectorsState: List<SectorState> = emptyList()
    private var listener: OnSectorSelectListener? = null

    private var squareSize = 0

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return when (event?.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val newSector = getSector(event.x, event.y)
                if (selectedSectorPosition != newSector) {
                    sectorsState[newSector].focused = true
                    selectedSectorPosition?.also {
                        sectorsState[it].focused = false
                    }
                    selectedSectorPosition = newSector
                    invalidate()
                }
                true
            }
            MotionEvent.ACTION_UP -> {
                selectedSectorPosition?.let { sectorsState[it] }?.apply {
                    selected = !selected
                    focused = false
                    animate(this)
                    if (selected)
                        listener?.onSectorSelected(sectorItem.name)
                    else
                        listener?.onSectorUnselected(sectorItem.name)
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

    fun setSectorsItems(sectors: List<SectorItem>) {
        sectorsState = sectors.map { SectorState(it, smallRadrius, it.defaultColor) }
    }

    fun setOnSectorSelectListener(listener: OnSectorSelectListener) {
        this.listener = listener
    }

    private fun animate(sectorState: SectorState) {
        sectorState.animator?.cancel()
        val targetRadius =
            if (sectorState.selected) selectedRadius.toFloat() else smallRadrius.toFloat()
        val animationDuration =
            400 * abs(sectorState.radius - targetRadius) / (selectedRadius - smallRadrius)

        val colorAnimator = ValueAnimator.ofArgb(
            sectorState.color,
            if (sectorState.selected) sectorState.sectorItem.selectedColor else sectorState.sectorItem.defaultColor
        )
            .apply {
                duration = animationDuration.toLong()
                addUpdateListener {
                    sectorState.color = it.animatedValue as Int
                }
            }
        val radiusAnimator =
            ValueAnimator.ofFloat(sectorState.radius.toFloat(), targetRadius).apply {
                duration = animationDuration.toLong()
                addUpdateListener {
                    sectorState.radius = (it.animatedValue as Float).toDouble()
                    invalidate()
                }
            }
        sectorState.animator = AnimatorSet().apply {
            playTogether(radiusAnimator, colorAnimator)
            start()
        }
    }

    private fun getSector(touchX: Float, touchY: Float): Int {
        val centerPositionX = touchX - (squareSize / 2)
        val centerPositionY = touchY - (squareSize / 2)
        val tg = centerPositionY / centerPositionX
        var eng = atan(tg.toDouble()) + (Math.PI / 2)
        val sectorSize = (2 * Math.PI) / if (sectorsState.isNotEmpty()) sectorsState.size else 1

        if (centerPositionX < 0)
            eng += Math.PI

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
        sectorsState.forEachIndexed { i, sector ->
            paint.color = sector.color
            val ang =
                (((2 * Math.PI) / (if (sectorsState.isNotEmpty()) sectorsState.size else 1)) / Math.PI * 180).toFloat()
            canvas.drawArc(
                (squareSize * (1 - sector.radius)).toFloat() / 2,
                (squareSize * (1 - sector.radius)).toFloat() / 2,
                (squareSize * sector.radius + (squareSize * (1 - sector.radius)) / 2).toFloat(),
                (squareSize * sector.radius + (squareSize * (1 - sector.radius)) / 2).toFloat(),
                ang * i - 90,
                ang,
                true,
                paint
            )
            sectorsState[i].sectorItem.icon?.also {
                val sectorCenterAngle = ang * (-i - 0.5) + 90
                canvas.drawBitmap(
                    it,
                    (squareSize / 2 + squareSize * iconsRadiusOffset * sector.radius * cos(
                        sectorCenterAngle * Math.PI / 180
                    ) - it.width / 2).toFloat(),
                    (squareSize / 2 - squareSize * iconsRadiusOffset * sector.radius * sin(
                        sectorCenterAngle * Math.PI / 180
                    ) - it.height / 2).toFloat(),
                    paint
                )
            }
        }
        paint.color = Color.BLACK
        sectorsState.forEachIndexed { i, sector ->
            val maxRadius =
                max(
                    sector.radius,
                    if (i == 0) sectorsState.last().radius else sectorsState[i - 1].radius
                )
            val angle =
                ((2 * Math.PI) / (if (sectorsState.isNotEmpty()) sectorsState.size else 1) * i - Math.PI / 2)

            canvas.drawLine(
                (squareSize / 2).toFloat(), (squareSize / 2).toFloat(),
                (squareSize / 2 * (1 + cos(angle) * maxRadius)).toFloat(),
                (squareSize / 2 * (1 + sin(angle) * maxRadius)).toFloat(),
                paint
            )
        }
        super.onDraw(canvas)
    }

    private class SectorState(val sectorItem: SectorItem, var radius: Double, var color: Int) {
        var selected = false
        var focused = false
        var animator: AnimatorSet? = null
    }

    class SectorItem(
        val name: String,
        val defaultColor: Int = Color.RED,
        val selectedColor: Int = Color.BLUE,
        val icon: Bitmap? = null
    )

    private fun cancelAnimation() {
        sectorsState.forEach {
            it.animator?.cancel()
            it.radius = if (it.selected) selectedRadius else smallRadrius
        }
    }
}