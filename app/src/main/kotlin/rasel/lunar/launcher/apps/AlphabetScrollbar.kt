/*
 * Lunar Launcher
 * Copyright (C) 2022 Md Rasel Hossain
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package rasel.lunar.launcher.apps


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import rasel.lunar.launcher.R
import rasel.lunar.launcher.apps.AppDrawer.Companion.letterPreview
import kotlin.math.roundToInt
internal class AlphabetScrollbar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val itemList = mutableListOf<String>()

    private var bounds = Rect()
    private var itemHeight : Int
    private var itemWidth : Int

    private var selectedIndex : Int

    var defaultItemColor : Int
    var selectedItemColor : Int

    var defaultItemSize : Int
    var selectedItemSize : Int

    var paddingInBetween : Int

    fun interface OnItemSelectedListener {
        fun onItemSelected(it : String)
    }
    private var onItemSelectedListener : OnItemSelectedListener? = null


    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AlphabetScrollbar)

        itemList.addAll((typedArray.getString(R.styleable.AlphabetScrollbar_item) ?: "").split(','))

        defaultItemColor = typedArray.getColor(R.styleable.AlphabetScrollbar_defaultItemColor, Color.GRAY)
        selectedItemColor = typedArray.getColor(R.styleable.AlphabetScrollbar_selectedItemColor, Color.WHITE)

        defaultItemSize = typedArray.getDimensionPixelOffset(R.styleable.AlphabetScrollbar_defaultItemSize, 16)
        selectedItemSize = typedArray.getDimensionPixelOffset(R.styleable.AlphabetScrollbar_selectedItemSize, 20)

        paddingInBetween = typedArray.getDimensionPixelOffset(R.styleable.AlphabetScrollbar_paddingInBetween, 10)

        typedArray.recycle()

        itemHeight = 0
        itemWidth = 0

        paint.textSize = selectedItemSize.toFloat()

        for (item in itemList) {
            paint.getTextBounds(item, 0, item.length, bounds)
            itemHeight = maxOf(itemHeight, bounds.height())
            itemWidth = maxOf(itemWidth, bounds.width())
        }

        selectedIndex = -1
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
                MeasureSpec.getSize(widthMeasureSpec)
            } else {
                paddingRight + itemWidth + paddingLeft
            },

            if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
                MeasureSpec.getSize(heightMeasureSpec)
            } else {
                paddingTop + (itemHeight * itemList.size + paddingInBetween * (itemList.size - 1)) + paddingBottom
            }
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var yA = paddingTop.toFloat()

        itemList.indices.forEach {
            when(it) {
                selectedIndex -> {
                    paint.color = selectedItemColor
                    paint.textSize = selectedItemSize.toFloat()
                }
                else -> {
                    paint.color = defaultItemColor
                    paint.textSize = defaultItemSize.toFloat()
                }
            }
            paint.getTextBounds(itemList[it], 0, itemList[it].length, bounds)

            val x = width.toFloat() / 2f - bounds.width().toFloat() / 2f
            val y = yA + bounds.height()

            canvas.drawText(itemList[it], x, y, paint)

            yA += itemHeight.toFloat() + paddingInBetween.toFloat()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val y = event.y - paddingTop

                var index = (y / (itemHeight + paddingInBetween)).toInt()

                if (index < 0) {
                    index = 0
                } else if (index >= itemList.size) {
                    index = itemList.size - 1
                }

                if (index != selectedIndex) {
                    selectedIndex = index
                    Log.d("", "$selectedIndex, $y")
                    invalidate()
                    letterPreview?.text = itemList[selectedIndex]
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                onItemSelectedListener?.onItemSelected(itemList[selectedIndex])
                invalidate()
                selectedIndex = -1
            }
        }
        return true
    }


    fun addItem(items : List<String>) {
        for (item in items) {
            paint.getTextBounds(item, 0, item.length, bounds)
            itemHeight = maxOf(itemHeight, bounds.height())
            itemWidth = maxOf(itemWidth, bounds.width())
        }

        itemList.addAll(items)
        requestLayout()
    }

    fun clearItem() {
        itemList.clear()
        requestLayout()
    }

    fun setOnItemSelected(isl : OnItemSelectedListener) {
        onItemSelectedListener = isl
    }
}