package com.arvifox.remysaw.ext

import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import com.arvifox.remysaw.SubstrateOptionsProvider
import java.math.BigDecimal
import java.util.Locale
import java.util.regex.Pattern

fun String.parseOtpCode(): String {
    val pattern = Pattern.compile("(\\d{4})")
    val matcher = pattern.matcher(this)

    if (matcher.find()) {
        return matcher.group(0)
    }

    return ""
}

fun String.getInitials(): String {
    val names = this.trim().split(" ")

    return if (names.size < 2) {
        ""
    } else {
        "${names.first().first().uppercaseChar()}${names.last().first().uppercaseChar()}"
    }
}

fun String.isErc20Address(): Boolean {
    return this.split(" ").size == 1 && this.startsWith(SubstrateOptionsProvider.hex)
}

fun String.didToAccountId(): String {
    return this.replace(":", "_") + "@sora"
}

fun String.removeHexPrefix(): String = this.removePrefix(SubstrateOptionsProvider.hex)

fun String.addHexPrefix(): String = "${SubstrateOptionsProvider.hex}$this"

fun String.removeWebPrefix(): String =
    this.removePrefix("http://").removePrefix("https://").removePrefix("www.")

fun String.truncateHash(): String = if (this.isNotEmpty() && this.length > 10) "${
    this.substring(
        0,
        5
    )
}...${this.substring(this.lastIndex - 4, this.lastIndex + 1)}" else this

fun String.truncateUserAddress(): String = if (this.isNotEmpty() && this.length > 10) "${
    this.substring(
        0,
        5
    )
}...${this.substring(this.lastIndex - 4, this.lastIndex + 1)}" else this

fun String.decimalPartSized(decimalSeparator: String = ".", ticker: String = ""): SpannableString {
    val decimalPointIndex = this.indexOf(decimalSeparator)

    val endIndex = this.indexOf(ticker).let { index ->
        if (ticker.isEmpty() || index == -1) {
            this.length
        } else {
            index
        }
    }

    val ss = SpannableString(this)

    if (decimalPointIndex != -1) {
        ss.setSpan(RelativeSizeSpan(0.7f), decimalPointIndex, endIndex, 0)
    }

    return ss
}

@Composable
fun String.highlightWordsCompose(
    colors: List<Int>,
    clickableAnnotation: List<String>,
    underlined: Boolean = false
): AnnotatedString {
    return buildAnnotatedString {
        val delimiter = "%%"
        var currentIndex = 0

        val wordsCount = this@highlightWordsCompose.windowed(2, 1).count { it == delimiter }
        if (wordsCount % 2 != 0 || wordsCount / 2 != colors.size || wordsCount / 2 != clickableAnnotation.size) {
            currentIndex += this@highlightWordsCompose.length
            append(this@highlightWordsCompose)
        }

        val strings = this@highlightWordsCompose.split(delimiter)
        var indexHighlighted = 0

        strings.forEachIndexed { index, s ->
            if (index % 2 != 0) {
                val startIndex = currentIndex
                val endIndex = startIndex + s.length

                colors.getOrNull(indexHighlighted)?.let {
                    addStyle(
                        style = SpanStyle(
                            color = Color(it),
                            textDecoration = if (underlined) TextDecoration.Underline else TextDecoration.None
                        ),
                        start = startIndex,
                        end = endIndex
                    )
                }

                clickableAnnotation.getOrNull(indexHighlighted)?.let {
                    addStringAnnotation(
                        tag = "",
                        annotation = it,
                        start = startIndex,
                        end = endIndex
                    )
                }
                indexHighlighted++
            }
            currentIndex += s.length
            append(s)
        }
    }
}

fun String?.getBigDecimal(groupingSymbol: Char = ' '): BigDecimal? {
    if (this.isNullOrEmpty() || this.first() == '.')
        return null

    return BigDecimal(this.replace(groupingSymbol.toString(), ""))
}

fun String.snakeCaseToCamelCase(): String {
    return split("_").mapIndexed { index, segment ->
        if (index > 0) { // do not capitalize first segment
            segment.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
        } else {
            segment
        }
    }.joinToString(separator = "")
}

private val nameBytesLimit = 32
fun String.isAccountNameLongerThen32Bytes() = this.toByteArray().size > nameBytesLimit

private const val passwordLength = 6
fun String.isPasswordSecure() = this.length >= passwordLength
