package io.github.karino2.kotlitex.view

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import android.widget.TextView
import kotlinx.coroutines.* // ktlint-disable no-wildcard-imports

interface MathSpanHandler {
    fun appendNormal(text: String)
    fun appendInlineMathExp(exp: String)
    fun appendDisplayMathExp(text: String)
    fun appendEndOfLine()
}

// to make this class unit testable.
class MathSpanBuilder(val handler: MathSpanHandler) {
    val mathExpLinePat = "^\\$\\$([^\$]+)\\$\\$\$".toRegex()
    val mathExpPat = "\\$\\$([^$]+)\\$\\$".toRegex()
    val multiLineMathBeginEndPat = "^\\$\\$\$".toRegex()

    var inMultiLineMath = false

    fun oneNormalLineWithoutEOL(line: String) {
        if (line.isEmpty())
            return

        var lastMatchPos = 0
        var res = mathExpPat.find(line)
        if (res == null) {
            handler.appendNormal(line)
            return
        }

        while (res != null) {
            if (lastMatchPos != res.range.start)
                handler.appendNormal(line.substring(lastMatchPos, res.range.start))
            handler.appendInlineMathExp(res.groupValues[1])
            lastMatchPos = res.range.last + 1
            res = res.next()
        }
        if (lastMatchPos != line.length)
            handler.appendNormal(line.substring(lastMatchPos))
    }

    val multiLineMathBuffer = StringBuilder()

    fun oneLineInMultilineMath(line: String) {
        multiLineMathBeginEndPat.matchEntire(line)?.let {
            inMultiLineMath = false
            val exp = multiLineMathBuffer.toString()
            if (exp.isNotEmpty()) {
                handler.appendDisplayMathExp(exp)
            }
            multiLineMathBuffer.clear()
            return
        }
        multiLineMathBuffer.append(line)
        multiLineMathBuffer.append(" ")
    }

    fun oneLine(line: String) {
        if (inMultiLineMath) {
            oneLineInMultilineMath(line)
            return
        }
        multiLineMathBeginEndPat.matchEntire(line)?.let {
            inMultiLineMath = true
            return
        }
        mathExpLinePat.matchEntire(line)?.let {
            handler.appendDisplayMathExp(it.groupValues[1])
            return
        }

        oneNormalLineWithoutEOL(line)
        handler.appendEndOfLine()
    }
}

class SpannableMathSpanHandler(val assetManager: AssetManager, val baseSize: Float) :
    MathSpanHandler {
    fun reset() {
        // spannable.clear() seems slow.
        if (spannable.isNotEmpty())
            spannable = SpannableStringBuilder()
        isMathExist = false
    }

    var isMathExist = false

    val mathExpressionSize: Float
    get() {
        return baseSize * 1.2f
    }

    override fun appendNormal(text: String) {
        spannable.append(text)
    }

    override fun appendInlineMathExp(exp: String) {
        appendMathSpan(exp, false)
    }

    private fun appendMathSpan(exp: String, isMathMode: Boolean) {
        isMathExist = true
        val size = if (isMathMode) mathExpressionSize else baseSize
        val span = MathExpressionSpan(exp, size, assetManager, isMathMode, equationColor = Color.BLACK)
        span.ensureDrawable()
        val begin = spannable.length
        spannable.append("\$\$${exp}\$\$")
        spannable.setSpan(span, begin, spannable.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
    }

    override fun appendDisplayMathExp(text: String) {
        appendMathSpan(text, true)
        spannable.append("\n")
    }

    override fun appendEndOfLine() {
        spannable.append("\n")
    }

    var spannable = SpannableStringBuilder()
}

class MarkdownView(context: Context, attrSet: AttributeSet) : TextView(context, attrSet) {
    companion object {
        var CACHE_ENABLED = true
        var CACHE_SIZE = 1024
        val cache = object : LinkedHashMap<String, Spannable>(128, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Spannable>?): Boolean {
                return this.size > CACHE_SIZE
            }
        }
    }

    var job: Job? = null

    val handler by lazy {
        SpannableMathSpanHandler(context.assets, textSize)
    }

    val builder by lazy {
        MathSpanBuilder(handler)
    }

    fun setMarkdown(text: String) {
        val prevJob = job
        prevJob?.let { it.cancel() }

        if (CACHE_ENABLED) {
            cache[text]?.let {
                setText(it)
                return
            }
        }

        setText(text)
        job = GlobalScope.launch {
            withContext(Dispatchers.IO) {
                prevJob?.let { it.join() }
                handler.reset()

                val lines = text.split("\n")
                repeat(lines.size) {
                    val line = lines[it]
                    builder.oneLine(line)
                }
            }
            if (handler.isMathExist) {
                withContext(Dispatchers.Main) {
                    if (CACHE_ENABLED) {
                        cache[text] = handler.spannable
                    }
                    setText(handler.spannable)
                }
            }
        }
    }
}