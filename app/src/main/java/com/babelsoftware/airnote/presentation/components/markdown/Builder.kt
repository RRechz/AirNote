package com.babelsoftware.airnote.presentation.components.markdown

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import java.text.BreakIterator
import androidx.compose.ui.text.withStyle

class MarkdownBuilder(internal val lines: List<String>, private var lineProcessors: List<MarkdownLineProcessor>) {
    var lineIndex = -1

    internal val content = mutableListOf<MarkdownElement>()

    fun add(element: MarkdownElement) {
        content.add(element)
    }

    fun parse() {
        while (hasNextLine()) {
            val line = nextLine()
            val processor = lineProcessors.find { it.canProcessLine(line) }
            if (processor != null) {
                processor.processLine(line, this)
            } else {
                add(NormalText(line))
            }
        }
    }

    private fun hasNextLine(): Boolean = lineIndex + 1 < lines.size

    private fun nextLine(): String {
        lineIndex++
        return lines[lineIndex]
    }
}

/**
 * Splits the input string by the specified delimiter and returns a list of index pairs.
 * Each pair represents the start and end indices of segments between delimiters.
 */
fun splitByDelimiter(input: String, delimiter: String): List<Pair<Int, Int>> {
    val segments = mutableListOf<Pair<Int, Int>>()
    var startIndex = 0
    var delimiterIndex = input.indexOf(delimiter, startIndex)

    while (delimiterIndex != -1) {
        if (startIndex != delimiterIndex) {
            segments.add(Pair(startIndex, delimiterIndex))
        } else {
            segments.add(Pair(startIndex, startIndex))
        }
        startIndex = delimiterIndex + delimiter.length
        delimiterIndex = input.indexOf(delimiter, startIndex)
    }

    if (startIndex < input.length) {
        segments.add(Pair(startIndex, input.length))
    } else if (startIndex == input.length) {
        segments.add(Pair(startIndex, startIndex))
    }

    // Only keep segments that are odd-indexed (i.e., inside delimiters)
    return segments.filterIndexed { index, _ -> index % 2 == 1 }
}

/**
 * Checks if a given index is within any of the provided segments.
 */
fun isInSegments(index: Int, segments: List<Pair<Int, Int>>): Boolean {
    return segments.any { segment -> index in segment.first until segment.second }
}

fun buildString(input: String, defaultFontWeight: FontWeight = FontWeight.Normal): AnnotatedString {
    val textStyleSegments: List<TextStyleSegment> = listOf(
        BoldSegment(),
        ItalicSegment(),
        HighlightSegment(),
        Strikethrough(),
        Underline(),
        CodeSegment()
    )

    val allSegments = textStyleSegments.associateWith { splitByDelimiter(input, it.delimiter) }

    fun getSpanStyle(index: Int): SpanStyle {
        val styles = textStyleSegments.filter { segment -> isInSegments(index, allSegments[segment]!!) }
        return styles.fold(SpanStyle(fontWeight = defaultFontWeight)) { acc, segment -> acc.merge(segment.getSpanStyle()) }
    }

    val annotatedString = buildAnnotatedString {
        val iterator = BreakIterator.getWordInstance()
        iterator.setText(input)

        var start = iterator.first()
        var end = iterator.next()

        while (end != BreakIterator.DONE) {
            val substring = input.substring(start, end)

            // Skip delimiters and check Arabic substrings
            val isDelimiter = textStyleSegments.any { segment -> 
                // Only consider it a delimiter if it's the exact delimiter string
                // This ensures single characters like +, -, = are not filtered out
                segment.delimiter == substring
            }
            
            if (!isDelimiter) {
                withStyle(style = getSpanStyle(start)) {
                    append(substring) // Append full words or graphemes
                }
            }

            start = end
            end = iterator.next()
        }
    }
    return annotatedString
}
