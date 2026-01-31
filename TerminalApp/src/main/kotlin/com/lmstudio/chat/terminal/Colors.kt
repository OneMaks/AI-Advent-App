package com.lmstudio.chat.terminal

/**
 * ANSI color codes for terminal output.
 */
object Colors {
    // Reset
    const val RESET = "\u001B[0m"

    // Regular colors
    const val BLACK = "\u001B[30m"
    const val RED = "\u001B[31m"
    const val GREEN = "\u001B[32m"
    const val YELLOW = "\u001B[33m"
    const val BLUE = "\u001B[34m"
    const val PURPLE = "\u001B[35m"
    const val CYAN = "\u001B[36m"
    const val WHITE = "\u001B[37m"

    // Bold colors
    const val BOLD = "\u001B[1m"
    const val BOLD_BLACK = "\u001B[1;30m"
    const val BOLD_RED = "\u001B[1;31m"
    const val BOLD_GREEN = "\u001B[1;32m"
    const val BOLD_YELLOW = "\u001B[1;33m"
    const val BOLD_BLUE = "\u001B[1;34m"
    const val BOLD_PURPLE = "\u001B[1;35m"
    const val BOLD_CYAN = "\u001B[1;36m"
    const val BOLD_WHITE = "\u001B[1;37m"

    // Dim
    const val DIM = "\u001B[2m"

    // Italic
    const val ITALIC = "\u001B[3m"

    // Underline
    const val UNDERLINE = "\u001B[4m"

    // Background colors
    const val BG_BLACK = "\u001B[40m"
    const val BG_RED = "\u001B[41m"
    const val BG_GREEN = "\u001B[42m"
    const val BG_YELLOW = "\u001B[43m"
    const val BG_BLUE = "\u001B[44m"
    const val BG_PURPLE = "\u001B[45m"
    const val BG_CYAN = "\u001B[46m"
    const val BG_WHITE = "\u001B[47m"
}

// Extension functions for colored output
fun String.colored(color: String) = "$color$this${Colors.RESET}"
fun String.red() = colored(Colors.RED)
fun String.green() = colored(Colors.GREEN)
fun String.yellow() = colored(Colors.YELLOW)
fun String.blue() = colored(Colors.BLUE)
fun String.purple() = colored(Colors.PURPLE)
fun String.cyan() = colored(Colors.CYAN)
fun String.white() = colored(Colors.WHITE)
fun String.bold() = colored(Colors.BOLD)
fun String.dim() = colored(Colors.DIM)
fun String.italic() = colored(Colors.ITALIC)
