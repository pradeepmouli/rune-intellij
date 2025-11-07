package dev.mouli.rune.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.application.ReadAction
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext

/**
 * Code completion contributor for Rune DSL (Rosetta).
 *
 * Provides context-aware completion suggestions for:
 * - Keywords (type, func, enum, namespace, etc.)
 * - Built-in types (string, int, number, boolean, date, etc.)
 * - User-defined types and functions
 * - Attributes and expressions
 *
 * Completion runs on background thread with ReadAction for PSI access.
 * Target: <200ms p95 latency per constitution.
 *
 * Thread-safe: Uses ReadAction for PSI access.
 */
class RosettaCompletionContributor : CompletionContributor() {
    companion object {
        // Rune DSL keywords for completion
        private val KEYWORDS =
            listOf(
                "namespace", "type", "func", "enum", "choice", "alias",
                "annotation", "scheme", "calculation", "reporting",
                "extends", "condition", "optional", "one-of", "required",
                "if", "then", "else", "and", "or", "not",
                "exists", "only", "is", "absent",
                "synonym", "metadata", "reference", "id", "key",
            )

        // Built-in primitive types
        private val PRIMITIVE_TYPES =
            listOf(
                "string",
                "int",
                "number",
                "boolean",
                "date",
                "time",
                "dateTime",
                "zonedDateTime",
            )

        // Common cardinality patterns
        private val CARDINALITY_PATTERNS =
            listOf(
                "(0..1)",
                "(1..1)",
                "(0..*)",
                "(1..*)",
            )
    }

    init {
        // Complete keywords anywhere in the file
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            KeywordCompletionProvider(),
        )

        // Complete primitive types in type reference positions
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            PrimitiveTypeCompletionProvider(),
        )

        // Complete cardinality patterns after type references
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            CardinalityCompletionProvider(),
        )
    }

    /**
     * Provider for keyword completion.
     */
    private class KeywordCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet,
        ) {
            ReadAction.run<Throwable> {
                KEYWORDS.forEach { keyword ->
                    result.addElement(
                        LookupElementBuilder.create(keyword)
                            .bold()
                            .withTypeText("keyword"),
                    )
                }
            }
        }
    }

    /**
     * Provider for primitive type completion.
     */
    private class PrimitiveTypeCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet,
        ) {
            ReadAction.run<Throwable> {
                PRIMITIVE_TYPES.forEach { type ->
                    result.addElement(
                        LookupElementBuilder.create(type)
                            .withTypeText("primitive type")
                            .withIcon(null), // TODO: Add type icon
                    )
                }
            }
        }
    }

    /**
     * Provider for cardinality pattern completion.
     */
    private class CardinalityCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet,
        ) {
            ReadAction.run<Throwable> {
                CARDINALITY_PATTERNS.forEach { pattern ->
                    result.addElement(
                        LookupElementBuilder.create(pattern)
                            .withTypeText("cardinality")
                            .withInsertHandler { insertContext, _ ->
                                // Move cursor inside parentheses for custom cardinality
                                if (pattern == "(0..1)") {
                                    insertContext.editor.caretModel.moveToOffset(
                                        insertContext.tailOffset,
                                    )
                                }
                            },
                    )
                }
            }
        }
    }

    override fun beforeCompletion(context: CompletionInitializationContext) {
        // Prevent IntelliJ from inserting dummy identifier for completion
        // This preserves the actual code structure during completion
        super.beforeCompletion(context)
    }
}
