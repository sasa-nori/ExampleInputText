package com.example.exampleinputtext

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.example.exampleinputtext.databinding.InputTextViewBinding

class InputTextView : ConstraintLayout {
    lateinit var binding: InputTextViewBinding

    /** 入力された文字 */
    var inputText: String
        set(value) {
            binding.editText.setText(value)
            updateBorderColor(false, errorLength, inputText, binding.frame, binding.errorText)
            resizeHeight()
        }
        get() = binding.editText.text?.toString() ?: ""

    /** 最大文字数 */
    var errorLength: Int = 100

    /** 現在の行数 最低でも1になる */
    private val lineCount: Int
        get() = if (binding.editText.lineCount > 0) binding.editText.lineCount else 1

    private val margin: Int
        get() {
            val errorHeight = if (binding.errorText.visibility == View.VISIBLE) {
                getDimensionPixelSize(R.dimen.input_text_error_height)
            } else {
                0
            }
            return getDimensionPixelSize(R.dimen.space_28dp) + errorHeight
        }

    private val lineHeight: Int
        get() = (lineCount * getDimensionPixelSize(R.dimen.input_text_line_height)) + margin

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        setListener()
    }

    override fun dispatchKeyEventPreIme(event: KeyEvent?): Boolean {
        // フォーム外タップはキーボード閉じる
        if (event?.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            closedKeyboard()
        }
        return super.dispatchKeyEventPreIme(event)
    }

    @SuppressLint("CustomViewStyleable")
    fun init(attributeSet: AttributeSet?, defStyle: Int) {
        binding = InputTextViewBinding.inflate(LayoutInflater.from(context), this, true)
        // エラー文字数や各種初期定義はstyleで受け取ってここで定義の方がスマート
    }

    private fun setListener() {
        binding.editText.setOnFocusChangeListener { _, hasFocus ->
            updateBorderColor(
                hasFocus = hasFocus,
                errorLength = errorLength,
                inputText = inputText,
                frame = binding.frame,
                errorText = binding.errorText
            )
            updateTextColor(binding.editText.text, errorLength)
            resizeHeight()
        }
        binding.editText.doAfterTextChanged {
            if (!binding.editText.isEnabled) return@doAfterTextChanged
            updateBorderColor(
                errorLength = errorLength,
                inputText = inputText,
                frame = binding.frame,
                errorText = binding.errorText
            )
            updateTextColor(it, errorLength)
            resizeHeight()
        }
    }

    private fun updateTextColor(inputText: Editable?, errorLength: Int) {
        if (inputText == null || inputText.isEmpty()) return

        val start = if (inputText.length > errorLength) errorLength else 0
        val color = if (inputText.length > errorLength) R.color.error else R.color.black

        if (start >= inputText.length) return

        inputText.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, color)),
            start, inputText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    private fun closedKeyboard() {
        updateBorderColor(false, errorLength, inputText, binding.frame, binding.errorText)
        binding.editText.clearFocus()
    }

    private fun updateBorderColor(hasFocus: Boolean = true, errorLength: Int, inputText: String, frame: ViewGroup, errorText: TextView) {
        val isLengthError = inputText.length > errorLength

        frame.isSelected = isLengthError
        frame.isEnabled = hasFocus

        errorText.visibility = if (isLengthError && hasFocus) View.VISIBLE else View.GONE
    }

    private fun resizeHeight() {
        if (measuredHeight == 0) return
        // EditTextをwrapしているので、高さは自分でリサイズしていく必要がある
        layoutParams.height = lineHeight + (getDimensionPixelSize(R.dimen.line_1dp) * 2)
        requestLayout()
    }

    private fun getDimensionPixelSize(res: Int): Int = context.resources.getDimensionPixelSize(res)
}