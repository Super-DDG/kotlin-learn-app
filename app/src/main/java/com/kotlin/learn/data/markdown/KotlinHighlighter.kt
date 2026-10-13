package com.kotlin.learn.data.markdown

/** 代码 token 类型（渲染时映射到 VS Code Dark+ 语义的主题色）。 */
enum class TokenType { KEYWORD, STRING, NUMBER, COMMENT, ANNOTATION, PLAIN }

data class CodeToken(val text: String, val type: TokenType)

/** Kotlin 代码正则分词器：token 序列必须无损覆盖输入全文。 */
object KotlinHighlighter {
    private val KEYWORDS = setOf(
        "package", "import", "class", "object", "interface", "fun", "val", "var",
        "if", "else", "when", "for", "while", "do", "return", "break", "continue",
        "is", "in", "as", "true", "false", "null", "this", "super",
        "private", "public", "internal", "protected",
        "data", "sealed", "enum", "companion", "override", "open", "abstract",
        "suspend", "by", "typealias", "constructor", "init",
        "try", "catch", "finally", "throw",
    )

    // 备选顺序即优先级：注释 → 字符串 → 注解 → 数字（含小数）→ 标识符/关键字 → 单字符兜底
    private val TOKEN = Regex(
        "//[^\n]*" +
            "|/\\*[\\s\\S]*?\\*/" +
            "|\"[^\"\\n]*\"" +
            "|@[A-Za-z_]\\w*" +
            "|\\b\\d[\\d_]*(\\.\\d+)?\\b" +
            "|[A-Za-z_][A-Za-z0-9_]*" +
            "|[\\s\\S]",
    )

    fun tokenize(code: String): List<CodeToken> =
        TOKEN.findAll(code).map { m ->
            val t = m.value
            val type = when {
                t.startsWith("//") || t.startsWith("/*") -> TokenType.COMMENT
                t.startsWith("\"") -> TokenType.STRING
                t.startsWith("@") -> TokenType.ANNOTATION
                t[0].isDigit() -> TokenType.NUMBER
                t in KEYWORDS -> TokenType.KEYWORD
                else -> TokenType.PLAIN
            }
            CodeToken(t, type)
        }.fold(mutableListOf<CodeToken>()) { acc, token ->
            // 相邻 PLAIN 合并为一段（含空白与普通字符），保证 "val x" 形如 [KEYWORD, PLAIN]
            val last = acc.lastOrNull()
            if (last != null && last.type == TokenType.PLAIN && token.type == TokenType.PLAIN) {
                acc[acc.size - 1] = CodeToken(last.text + token.text, TokenType.PLAIN)
            } else {
                acc.add(token)
            }
            acc
        }.toList()
}
