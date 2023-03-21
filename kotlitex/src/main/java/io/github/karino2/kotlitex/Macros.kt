package io.github.karino2.kotlitex

import java.util.concurrent.ConcurrentHashMap

/*
This file basically contain the code of macros.js in original katex.
 */

sealed class MacroDefinition

data class MacroExpansion(val tokens: List<Token>, val numArgs: Int): MacroDefinition()
data class MacroString(val value: String) : MacroDefinition()
data class MacroFunction(val func: (MacroExpander)-> MacroDefinition) : MacroDefinition()

object Macros {
    val builtinMacros by lazy { ConcurrentHashMap<String, MacroDefinition>() }
    fun defineMacro(name: String, body: MacroDefinition) {
        builtinMacros[name] = body
    }

    fun defineAll() {
        defineMacro("\\\\", MacroString("\\newline"))
    }

}
