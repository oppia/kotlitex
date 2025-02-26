package io.github.karino2.kotlitex

import java.util.concurrent.ConcurrentHashMap

data class CharacterMetrics(
    val depth: Double,
    val height: Double,
    val italic: Double,
    val skew: Double,
    val width: Double
)

// We add these Latin-1 letters as symbols for backwards-compatibility,
// but they are not actually in the font, nor are they supported by the
// Unicode accent mechanism, so they fall back to Times font and look ugly.
// TODO(edemaine): Fix this.
const val extraLatin = "ÇÐÞçþ"

object Symbols {
    val mathMap: MutableMap<String, CharInfo> by lazy { ConcurrentHashMap() }
    val textMap: MutableMap<String, CharInfo> by lazy { ConcurrentHashMap() }

    fun get(mode: Mode) = if (mode == Mode.MATH) mathMap else textMap

    // These are very rough approximations.  We default to Times New Roman which
    // should have Latin-1 and Cyrillic characters, but may not depending on the
    // operating system.  The metrics do not account for extra height from the
    // accents.  In the case of Cyrillic characters which have both ascenders and
    // descenders we prefer approximations with ascenders, primarily to prevent
    // the fraction bar or root line from intersecting the glyph.
    // TODO(kevinb) allow union of multiple glyph metrics for better accuracy.
    val extraCharacterMap by lazy { mapOf(
        // Latin-1
        'Å' to  'A',
    'Ç' to  'C',
    'Ð' to  'D',
    'Þ' to  'o',
    'å' to  'a',
    'ç' to  'c',
    'ð' to  'd',
    'þ' to  'o',

    // Cyrillic
    'А' to  'A',
    'Б' to  'B',
    'В' to  'B',
    'Г' to  'F',
    'Д' to  'A',
    'Е' to  'E',
    'Ж' to  'K',
    'З' to  '3',
    'И' to  'N',
    'Й' to  'N',
    'К' to  'K',
    'Л' to  'N',
    'М' to  'M',
    'Н' to  'H',
    'О' to  'O',
    'П' to  'N',
    'Р' to  'P',
    'С' to  'C',
    'Т' to  'T',
    'У' to  'y',
    'Ф' to  'O',
    'Х' to  'X',
    'Ц' to  'U',
    'Ч' to  'h',
    'Ш' to  'W',
    'Щ' to  'W',
    'Ъ' to  'B',
    'Ы' to  'X',
    'Ь' to  'B',
    'Э' to  '3',
    'Ю' to  'X',
    'Я' to  'R',
    'а' to  'a',
    'б' to  'b',
    'в' to  'a',
    'г' to  'r',
    'д' to  'y',
    'е' to  'e',
    'ж' to  'm',
    'з' to  'e',
    'и' to  'n',
    'й' to  'n',
    'к' to  'n',
    'л' to  'n',
    'м' to  'm',
    'н' to  'n',
    'о' to  'o',
    'п' to  'n',
    'р' to  'p',
    'с' to  'c',
    'т' to  'o',
    'у' to  'y',
    'ф' to  'b',
    'х' to  'x',
    'ц' to  'n',
    'ч' to  'n',
    'ш' to  'w',
    'щ' to  'w',
    'ъ' to  'a',
    'ы' to  'm',
    'ь' to  'a',
    'э' to  'e',
    'ю' to  'm',
    'я' to  'r'
    )}

    // TODO:
    @Suppress("UNUSED_PARAMETER")
    fun supportedCodepoint(ch: Int) = true

    fun getCharacterMetrics(character: String, font: String, mode: Mode): CharacterMetrics? {
        val metmap = MetricMap.metricMap[font] ?: throw Exception("Font metrics not found for font: $font.")

        val chInt = (extraCharacterMap[character[0]] ?: character[0]).code
        val ch = chInt.toString()
        var metric = metmap[ch]

        if (metric == null && mode == Mode.TEXT) {
            // We don't typically have font metrics for Asian scripts.
            // But since we support them in text mode, we need to return
            // some sort of metrics.
            // So if the character is in a script we support but we
            // don't have metrics for it, just use the metrics for
            // the Latin capital letter M. This is close enough because
            // we (currently) only care about the height of the glpyh
            // not its width.
            if (supportedCodepoint(chInt)) {
                metric = metmap["77"] // 77 is the charcode for 'M'
            }
        }

        if (metric == null) {
            return null
        }

        return CharacterMetrics(metric[0], metric[1], metric[2], metric[3], metric[4])
    }

    fun lookupSymbol(in_value: String, fontName: String, mode: Mode): Pair<String, CharacterMetrics?> {
        var value = in_value
        if (Symbols.get(mode)[value] != null && Symbols.get(mode)[value]!!.replace != null) {
            value = Symbols.get(mode)[value]!!.replace!!
        }
        return Pair(value, getCharacterMetrics(value, fontName, mode))
    }

    fun defineSymbol(mode: Mode, font: Font, group: Group, replace: String?, name: String, acceptUnicodeChar: Boolean = false) {
        Symbols.get(mode)[name] = CharInfo(font, group, replace)

        if (acceptUnicodeChar && replace != null) {
            Symbols.get(mode)[replace] = Symbols.get(mode)[name]!!
        }
    }
    init {
        SymbolDefinitions.defineAllSymbols()
    }

    val unicodeAccents by lazy { mapOf(
        '\u0301' to AccentRelation("\\'", "\\acute"),
        '\u0300' to AccentRelation("\\`", "\\grave"),
        '\u0308' to AccentRelation("\\\"", "\\ddot"),
        '\u0303' to AccentRelation("\\~", "\\tilde"),
        '\u0304' to AccentRelation( "\\=",  "\\bar"),
        '\u0306' to AccentRelation( "\\u",  "\\breve"),
        '\u030c' to AccentRelation( "\\v",  "\\check"),
        '\u0302' to AccentRelation( "\\^",  "\\hat"),
        '\u0307' to AccentRelation( "\\.",  "\\dot"),
        '\u030a' to AccentRelation( "\\r",  "\\mathring"),
        '\u030b' to AccentRelation( "\\H", "")
    )}

    val unicodeSymbols by lazy { mapOf(
        '\u00e1' to "\u0061\u0301",  // á = \'{a}
        '\u00e0' to "\u0061\u0300",  // à = \`{a}
        '\u00e4' to "\u0061\u0308",  // ä = \"{a}
        '\u01df' to "\u0061\u0308\u0304",  // ǟ = \"\={a}
        '\u00e3' to "\u0061\u0303",  // ã = \~{a}
        '\u0101' to "\u0061\u0304",  // ā = \={a}
        '\u0103' to "\u0061\u0306",  // ă = \u{a}
        '\u1eaf' to "\u0061\u0306\u0301",  // ắ = \u\'{a}
        '\u1eb1' to "\u0061\u0306\u0300",  // ằ = \u\`{a}
        '\u1eb5' to "\u0061\u0306\u0303",  // ẵ = \u\~{a}
        '\u01ce' to "\u0061\u030c",  // ǎ = \v{a}
        '\u00e2' to "\u0061\u0302",  // â = \^{a}
        '\u1ea5' to "\u0061\u0302\u0301",  // ấ = \^\'{a}
        '\u1ea7' to "\u0061\u0302\u0300",  // ầ = \^\`{a}
        '\u1eab' to "\u0061\u0302\u0303",  // ẫ = \^\~{a}
        '\u0227' to "\u0061\u0307",  // ȧ = \.{a}
        '\u01e1' to "\u0061\u0307\u0304",  // ǡ = \.\={a}
        '\u00e5' to "\u0061\u030a",  // å = \r{a}
        '\u01fb' to "\u0061\u030a\u0301",  // ǻ = \r\'{a}
        '\u1e03' to "\u0062\u0307",  // ḃ = \.{b}
        '\u0107' to "\u0063\u0301",  // ć = \'{c}
        '\u010d' to "\u0063\u030c",  // č = \v{c}
        '\u0109' to "\u0063\u0302",  // ĉ = \^{c}
        '\u010b' to "\u0063\u0307",  // ċ = \.{c}
        '\u010f' to "\u0064\u030c",  // ď = \v{d}
        '\u1e0b' to "\u0064\u0307",  // ḋ = \.{d}
        '\u00e9' to "\u0065\u0301",  // é = \'{e}
        '\u00e8' to "\u0065\u0300",  // è = \`{e}
        '\u00eb' to "\u0065\u0308",  // ë = \"{e}
        '\u1ebd' to "\u0065\u0303",  // ẽ = \~{e}
        '\u0113' to "\u0065\u0304",  // ē = \={e}
        '\u1e17' to "\u0065\u0304\u0301",  // ḗ = \=\'{e}
        '\u1e15' to "\u0065\u0304\u0300",  // ḕ = \=\`{e}
        '\u0115' to "\u0065\u0306",  // ĕ = \u{e}
        '\u011b' to "\u0065\u030c",  // ě = \v{e}
        '\u00ea' to "\u0065\u0302",  // ê = \^{e}
        '\u1ebf' to "\u0065\u0302\u0301",  // ế = \^\'{e}
        '\u1ec1' to "\u0065\u0302\u0300",  // ề = \^\`{e}
        '\u1ec5' to "\u0065\u0302\u0303",  // ễ = \^\~{e}
        '\u0117' to "\u0065\u0307",  // ė = \.{e}
        '\u1e1f' to "\u0066\u0307",  // ḟ = \.{f}
        '\u01f5' to "\u0067\u0301",  // ǵ = \'{g}
        '\u1e21' to "\u0067\u0304",  // ḡ = \={g}
        '\u011f' to "\u0067\u0306",  // ğ = \u{g}
        '\u01e7' to "\u0067\u030c",  // ǧ = \v{g}
        '\u011d' to "\u0067\u0302",  // ĝ = \^{g}
        '\u0121' to "\u0067\u0307",  // ġ = \.{g}
        '\u1e27' to "\u0068\u0308",  // ḧ = \"{h}
        '\u021f' to "\u0068\u030c",  // ȟ = \v{h}
        '\u0125' to "\u0068\u0302",  // ĥ = \^{h}
        '\u1e23' to "\u0068\u0307",  // ḣ = \.{h}
        '\u00ed' to "\u0069\u0301",  // í = \'{i}
        '\u00ec' to "\u0069\u0300",  // ì = \`{i}
        '\u00ef' to "\u0069\u0308",  // ï = \"{i}
        '\u1e2f' to "\u0069\u0308\u0301",  // ḯ = \"\'{i}
        '\u0129' to "\u0069\u0303",  // ĩ = \~{i}
        '\u012b' to "\u0069\u0304",  // ī = \={i}
        '\u012d' to "\u0069\u0306",  // ĭ = \u{i}
        '\u01d0' to "\u0069\u030c",  // ǐ = \v{i}
        '\u00ee' to "\u0069\u0302",  // î = \^{i}
        '\u01f0' to "\u006a\u030c",  // ǰ = \v{j}
        '\u0135' to "\u006a\u0302",  // ĵ = \^{j}
        '\u1e31' to "\u006b\u0301",  // ḱ = \'{k}
        '\u01e9' to "\u006b\u030c",  // ǩ = \v{k}
        '\u013a' to "\u006c\u0301",  // ĺ = \'{l}
        '\u013e' to "\u006c\u030c",  // ľ = \v{l}
        '\u1e3f' to "\u006d\u0301",  // ḿ = \'{m}
        '\u1e41' to "\u006d\u0307",  // ṁ = \.{m}
        '\u0144' to "\u006e\u0301",  // ń = \'{n}
        '\u01f9' to "\u006e\u0300",  // ǹ = \`{n}
        '\u00f1' to "\u006e\u0303",  // ñ = \~{n}
        '\u0148' to "\u006e\u030c",  // ň = \v{n}
        '\u1e45' to "\u006e\u0307",  // ṅ = \.{n}
        '\u00f3' to "\u006f\u0301",  // ó = \'{o}
        '\u00f2' to "\u006f\u0300",  // ò = \`{o}
        '\u00f6' to "\u006f\u0308",  // ö = \"{o}
        '\u022b' to "\u006f\u0308\u0304",  // ȫ = \"\={o}
        '\u00f5' to "\u006f\u0303",  // õ = \~{o}
        '\u1e4d' to "\u006f\u0303\u0301",  // ṍ = \~\'{o}
        '\u1e4f' to "\u006f\u0303\u0308",  // ṏ = \~\"{o}
        '\u022d' to "\u006f\u0303\u0304",  // ȭ = \~\={o}
        '\u014d' to "\u006f\u0304",  // ō = \={o}
        '\u1e53' to "\u006f\u0304\u0301",  // ṓ = \=\'{o}
        '\u1e51' to "\u006f\u0304\u0300",  // ṑ = \=\`{o}
        '\u014f' to "\u006f\u0306",  // ŏ = \u{o}
        '\u01d2' to "\u006f\u030c",  // ǒ = \v{o}
        '\u00f4' to "\u006f\u0302",  // ô = \^{o}
        '\u1ed1' to "\u006f\u0302\u0301",  // ố = \^\'{o}
        '\u1ed3' to "\u006f\u0302\u0300",  // ồ = \^\`{o}
        '\u1ed7' to "\u006f\u0302\u0303",  // ỗ = \^\~{o}
        '\u022f' to "\u006f\u0307",  // ȯ = \.{o}
        '\u0231' to "\u006f\u0307\u0304",  // ȱ = \.\={o}
        '\u0151' to "\u006f\u030b",  // ő = \H{o}
        '\u1e55' to "\u0070\u0301",  // ṕ = \'{p}
        '\u1e57' to "\u0070\u0307",  // ṗ = \.{p}
        '\u0155' to "\u0072\u0301",  // ŕ = \'{r}
        '\u0159' to "\u0072\u030c",  // ř = \v{r}
        '\u1e59' to "\u0072\u0307",  // ṙ = \.{r}
        '\u015b' to "\u0073\u0301",  // ś = \'{s}
        '\u1e65' to "\u0073\u0301\u0307",  // ṥ = \'\.{s}
        '\u0161' to "\u0073\u030c",  // š = \v{s}
        '\u1e67' to "\u0073\u030c\u0307",  // ṧ = \v\.{s}
        '\u015d' to "\u0073\u0302",  // ŝ = \^{s}
        '\u1e61' to "\u0073\u0307",  // ṡ = \.{s}
        '\u1e97' to "\u0074\u0308",  // ẗ = \"{t}
        '\u0165' to "\u0074\u030c",  // ť = \v{t}
        '\u1e6b' to "\u0074\u0307",  // ṫ = \.{t}
        '\u00fa' to "\u0075\u0301",  // ú = \'{u}
        '\u00f9' to "\u0075\u0300",  // ù = \`{u}
        '\u00fc' to "\u0075\u0308",  // ü = \"{u}
        '\u01d8' to "\u0075\u0308\u0301",  // ǘ = \"\'{u}
        '\u01dc' to "\u0075\u0308\u0300",  // ǜ = \"\`{u}
        '\u01d6' to "\u0075\u0308\u0304",  // ǖ = \"\={u}
        '\u01da' to "\u0075\u0308\u030c",  // ǚ = \"\v{u}
        '\u0169' to "\u0075\u0303",  // ũ = \~{u}
        '\u1e79' to "\u0075\u0303\u0301",  // ṹ = \~\'{u}
        '\u016b' to "\u0075\u0304",  // ū = \={u}
        '\u1e7b' to "\u0075\u0304\u0308",  // ṻ = \=\"{u}
        '\u016d' to "\u0075\u0306",  // ŭ = \u{u}
        '\u01d4' to "\u0075\u030c",  // ǔ = \v{u}
        '\u00fb' to "\u0075\u0302",  // û = \^{u}
        '\u016f' to "\u0075\u030a",  // ů = \r{u}
        '\u0171' to "\u0075\u030b",  // ű = \H{u}
        '\u1e7d' to "\u0076\u0303",  // ṽ = \~{v}
        '\u1e83' to "\u0077\u0301",  // ẃ = \'{w}
        '\u1e81' to "\u0077\u0300",  // ẁ = \`{w}
        '\u1e85' to "\u0077\u0308",  // ẅ = \"{w}
        '\u0175' to "\u0077\u0302",  // ŵ = \^{w}
        '\u1e87' to "\u0077\u0307",  // ẇ = \.{w}
        '\u1e98' to "\u0077\u030a",  // ẘ = \r{w}
        '\u1e8d' to "\u0078\u0308",  // ẍ = \"{x}
        '\u1e8b' to "\u0078\u0307",  // ẋ = \.{x}
        '\u00fd' to "\u0079\u0301",  // ý = \'{y}
        '\u1ef3' to "\u0079\u0300",  // ỳ = \`{y}
        '\u00ff' to "\u0079\u0308",  // ÿ = \"{y}
        '\u1ef9' to "\u0079\u0303",  // ỹ = \~{y}
        '\u0233' to "\u0079\u0304",  // ȳ = \={y}
        '\u0177' to "\u0079\u0302",  // ŷ = \^{y}
        '\u1e8f' to "\u0079\u0307",  // ẏ = \.{y}
        '\u1e99' to "\u0079\u030a",  // ẙ = \r{y}
        '\u017a' to "\u007a\u0301",  // ź = \'{z}
        '\u017e' to "\u007a\u030c",  // ž = \v{z}
        '\u1e91' to "\u007a\u0302",  // ẑ = \^{z}
        '\u017c' to "\u007a\u0307",  // ż = \.{z}
        '\u00c1' to "\u0041\u0301",  // Á = \'{A}
        '\u00c0' to "\u0041\u0300",  // À = \`{A}
        '\u00c4' to "\u0041\u0308",  // Ä = \"{A}
        '\u01de' to "\u0041\u0308\u0304",  // Ǟ = \"\={A}
        '\u00c3' to "\u0041\u0303",  // Ã = \~{A}
        '\u0100' to "\u0041\u0304",  // Ā = \={A}
        '\u0102' to "\u0041\u0306",  // Ă = \u{A}
        '\u1eae' to "\u0041\u0306\u0301",  // Ắ = \u\'{A}
        '\u1eb0' to "\u0041\u0306\u0300",  // Ằ = \u\`{A}
        '\u1eb4' to "\u0041\u0306\u0303",  // Ẵ = \u\~{A}
        '\u01cd' to "\u0041\u030c",  // Ǎ = \v{A}
        '\u00c2' to "\u0041\u0302",  // Â = \^{A}
        '\u1ea4' to "\u0041\u0302\u0301",  // Ấ = \^\'{A}
        '\u1ea6' to "\u0041\u0302\u0300",  // Ầ = \^\`{A}
        '\u1eaa' to "\u0041\u0302\u0303",  // Ẫ = \^\~{A}
        '\u0226' to "\u0041\u0307",  // Ȧ = \.{A}
        '\u01e0' to "\u0041\u0307\u0304",  // Ǡ = \.\={A}
        '\u00c5' to "\u0041\u030a",  // Å = \r{A}
        '\u01fa' to "\u0041\u030a\u0301",  // Ǻ = \r\'{A}
        '\u1e02' to "\u0042\u0307",  // Ḃ = \.{B}
        '\u0106' to "\u0043\u0301",  // Ć = \'{C}
        '\u010c' to "\u0043\u030c",  // Č = \v{C}
        '\u0108' to "\u0043\u0302",  // Ĉ = \^{C}
        '\u010a' to "\u0043\u0307",  // Ċ = \.{C}
        '\u010e' to "\u0044\u030c",  // Ď = \v{D}
        '\u1e0a' to "\u0044\u0307",  // Ḋ = \.{D}
        '\u00c9' to "\u0045\u0301",  // É = \'{E}
        '\u00c8' to "\u0045\u0300",  // È = \`{E}
        '\u00cb' to "\u0045\u0308",  // Ë = \"{E}
        '\u1ebc' to "\u0045\u0303",  // Ẽ = \~{E}
        '\u0112' to "\u0045\u0304",  // Ē = \={E}
        '\u1e16' to "\u0045\u0304\u0301",  // Ḗ = \=\'{E}
        '\u1e14' to "\u0045\u0304\u0300",  // Ḕ = \=\`{E}
        '\u0114' to "\u0045\u0306",  // Ĕ = \u{E}
        '\u011a' to "\u0045\u030c",  // Ě = \v{E}
        '\u00ca' to "\u0045\u0302",  // Ê = \^{E}
        '\u1ebe' to "\u0045\u0302\u0301",  // Ế = \^\'{E}
        '\u1ec0' to "\u0045\u0302\u0300",  // Ề = \^\`{E}
        '\u1ec4' to "\u0045\u0302\u0303",  // Ễ = \^\~{E}
        '\u0116' to "\u0045\u0307",  // Ė = \.{E}
        '\u1e1e' to "\u0046\u0307",  // Ḟ = \.{F}
        '\u01f4' to "\u0047\u0301",  // Ǵ = \'{G}
        '\u1e20' to "\u0047\u0304",  // Ḡ = \={G}
        '\u011e' to "\u0047\u0306",  // Ğ = \u{G}
        '\u01e6' to "\u0047\u030c",  // Ǧ = \v{G}
        '\u011c' to "\u0047\u0302",  // Ĝ = \^{G}
        '\u0120' to "\u0047\u0307",  // Ġ = \.{G}
        '\u1e26' to "\u0048\u0308",  // Ḧ = \"{H}
        '\u021e' to "\u0048\u030c",  // Ȟ = \v{H}
        '\u0124' to "\u0048\u0302",  // Ĥ = \^{H}
        '\u1e22' to "\u0048\u0307",  // Ḣ = \.{H}
        '\u00cd' to "\u0049\u0301",  // Í = \'{I}
        '\u00cc' to "\u0049\u0300",  // Ì = \`{I}
        '\u00cf' to "\u0049\u0308",  // Ï = \"{I}
        '\u1e2e' to "\u0049\u0308\u0301",  // Ḯ = \"\'{I}
        '\u0128' to "\u0049\u0303",  // Ĩ = \~{I}
        '\u012a' to "\u0049\u0304",  // Ī = \={I}
        '\u012c' to "\u0049\u0306",  // Ĭ = \u{I}
        '\u01cf' to "\u0049\u030c",  // Ǐ = \v{I}
        '\u00ce' to "\u0049\u0302",  // Î = \^{I}
        '\u0130' to "\u0049\u0307",  // İ = \.{I}
        '\u0134' to "\u004a\u0302",  // Ĵ = \^{J}
        '\u1e30' to "\u004b\u0301",  // Ḱ = \'{K}
        '\u01e8' to "\u004b\u030c",  // Ǩ = \v{K}
        '\u0139' to "\u004c\u0301",  // Ĺ = \'{L}
        '\u013d' to "\u004c\u030c",  // Ľ = \v{L}
        '\u1e3e' to "\u004d\u0301",  // Ḿ = \'{M}
        '\u1e40' to "\u004d\u0307",  // Ṁ = \.{M}
        '\u0143' to "\u004e\u0301",  // Ń = \'{N}
        '\u01f8' to "\u004e\u0300",  // Ǹ = \`{N}
        '\u00d1' to "\u004e\u0303",  // Ñ = \~{N}
        '\u0147' to "\u004e\u030c",  // Ň = \v{N}
        '\u1e44' to "\u004e\u0307",  // Ṅ = \.{N}
        '\u00d3' to "\u004f\u0301",  // Ó = \'{O}
        '\u00d2' to "\u004f\u0300",  // Ò = \`{O}
        '\u00d6' to "\u004f\u0308",  // Ö = \"{O}
        '\u022a' to "\u004f\u0308\u0304",  // Ȫ = \"\={O}
        '\u00d5' to "\u004f\u0303",  // Õ = \~{O}
        '\u1e4c' to "\u004f\u0303\u0301",  // Ṍ = \~\'{O}
        '\u1e4e' to "\u004f\u0303\u0308",  // Ṏ = \~\"{O}
        '\u022c' to "\u004f\u0303\u0304",  // Ȭ = \~\={O}
        '\u014c' to "\u004f\u0304",  // Ō = \={O}
        '\u1e52' to "\u004f\u0304\u0301",  // Ṓ = \=\'{O}
        '\u1e50' to "\u004f\u0304\u0300",  // Ṑ = \=\`{O}
        '\u014e' to "\u004f\u0306",  // Ŏ = \u{O}
        '\u01d1' to "\u004f\u030c",  // Ǒ = \v{O}
        '\u00d4' to "\u004f\u0302",  // Ô = \^{O}
        '\u1ed0' to "\u004f\u0302\u0301",  // Ố = \^\'{O}
        '\u1ed2' to "\u004f\u0302\u0300",  // Ồ = \^\`{O}
        '\u1ed6' to "\u004f\u0302\u0303",  // Ỗ = \^\~{O}
        '\u022e' to "\u004f\u0307",  // Ȯ = \.{O}
        '\u0230' to "\u004f\u0307\u0304",  // Ȱ = \.\={O}
        '\u0150' to "\u004f\u030b",  // Ő = \H{O}
        '\u1e54' to "\u0050\u0301",  // Ṕ = \'{P}
        '\u1e56' to "\u0050\u0307",  // Ṗ = \.{P}
        '\u0154' to "\u0052\u0301",  // Ŕ = \'{R}
        '\u0158' to "\u0052\u030c",  // Ř = \v{R}
        '\u1e58' to "\u0052\u0307",  // Ṙ = \.{R}
        '\u015a' to "\u0053\u0301",  // Ś = \'{S}
        '\u1e64' to "\u0053\u0301\u0307",  // Ṥ = \'\.{S}
        '\u0160' to "\u0053\u030c",  // Š = \v{S}
        '\u1e66' to "\u0053\u030c\u0307",  // Ṧ = \v\.{S}
        '\u015c' to "\u0053\u0302",  // Ŝ = \^{S}
        '\u1e60' to "\u0053\u0307",  // Ṡ = \.{S}
        '\u0164' to "\u0054\u030c",  // Ť = \v{T}
        '\u1e6a' to "\u0054\u0307",  // Ṫ = \.{T}
        '\u00da' to "\u0055\u0301",  // Ú = \'{U}
        '\u00d9' to "\u0055\u0300",  // Ù = \`{U}
        '\u00dc' to "\u0055\u0308",  // Ü = \"{U}
        '\u01d7' to "\u0055\u0308\u0301",  // Ǘ = \"\'{U}
        '\u01db' to "\u0055\u0308\u0300",  // Ǜ = \"\`{U}
        '\u01d5' to "\u0055\u0308\u0304",  // Ǖ = \"\={U}
        '\u01d9' to "\u0055\u0308\u030c",  // Ǚ = \"\v{U}
        '\u0168' to "\u0055\u0303",  // Ũ = \~{U}
        '\u1e78' to "\u0055\u0303\u0301",  // Ṹ = \~\'{U}
        '\u016a' to "\u0055\u0304",  // Ū = \={U}
        '\u1e7a' to "\u0055\u0304\u0308",  // Ṻ = \=\"{U}
        '\u016c' to "\u0055\u0306",  // Ŭ = \u{U}
        '\u01d3' to "\u0055\u030c",  // Ǔ = \v{U}
        '\u00db' to "\u0055\u0302",  // Û = \^{U}
        '\u016e' to "\u0055\u030a",  // Ů = \r{U}
        '\u0170' to "\u0055\u030b",  // Ű = \H{U}
        '\u1e7c' to "\u0056\u0303",  // Ṽ = \~{V}
        '\u1e82' to "\u0057\u0301",  // Ẃ = \'{W}
        '\u1e80' to "\u0057\u0300",  // Ẁ = \`{W}
        '\u1e84' to "\u0057\u0308",  // Ẅ = \"{W}
        '\u0174' to "\u0057\u0302",  // Ŵ = \^{W}
        '\u1e86' to "\u0057\u0307",  // Ẇ = \.{W}
        '\u1e8c' to "\u0058\u0308",  // Ẍ = \"{X}
        '\u1e8a' to "\u0058\u0307",  // Ẋ = \.{X}
        '\u00dd' to "\u0059\u0301",  // Ý = \'{Y}
        '\u1ef2' to "\u0059\u0300",  // Ỳ = \`{Y}
        '\u0178' to "\u0059\u0308",  // Ÿ = \"{Y}
        '\u1ef8' to "\u0059\u0303",  // Ỹ = \~{Y}
        '\u0232' to "\u0059\u0304",  // Ȳ = \={Y}
        '\u0176' to "\u0059\u0302",  // Ŷ = \^{Y}
        '\u1e8e' to "\u0059\u0307",  // Ẏ = \.{Y}
        '\u0179' to "\u005a\u0301",  // Ź = \'{Z}
        '\u017d' to "\u005a\u030c",  // Ž = \v{Z}
        '\u1e90' to "\u005a\u0302",  // Ẑ = \^{Z}
        '\u017b' to "\u005a\u0307",  // Ż = \.{Z}
        '\u03ac' to "\u03b1\u0301",  // ά = \'{α}
        '\u1f70' to "\u03b1\u0300",  // ὰ = \`{α}
        '\u1fb1' to "\u03b1\u0304",  // ᾱ = \={α}
        '\u1fb0' to "\u03b1\u0306",  // ᾰ = \u{α}
        '\u03ad' to "\u03b5\u0301",  // έ = \'{ε}
        '\u1f72' to "\u03b5\u0300",  // ὲ = \`{ε}
        '\u03ae' to "\u03b7\u0301",  // ή = \'{η}
        '\u1f74' to "\u03b7\u0300",  // ὴ = \`{η}
        '\u03af' to "\u03b9\u0301",  // ί = \'{ι}
        '\u1f76' to "\u03b9\u0300",  // ὶ = \`{ι}
        '\u03ca' to "\u03b9\u0308",  // ϊ = \"{ι}
        '\u0390' to "\u03b9\u0308\u0301",  // ΐ = \"\'{ι}
        '\u1fd2' to "\u03b9\u0308\u0300",  // ῒ = \"\`{ι}
        '\u1fd1' to "\u03b9\u0304",  // ῑ = \={ι}
        '\u1fd0' to "\u03b9\u0306",  // ῐ = \u{ι}
        '\u03cc' to "\u03bf\u0301",  // ό = \'{ο}
        '\u1f78' to "\u03bf\u0300",  // ὸ = \`{ο}
        '\u03cd' to "\u03c5\u0301",  // ύ = \'{υ}
        '\u1f7a' to "\u03c5\u0300",  // ὺ = \`{υ}
        '\u03cb' to "\u03c5\u0308",  // ϋ = \"{υ}
        '\u03b0' to "\u03c5\u0308\u0301",  // ΰ = \"\'{υ}
        '\u1fe2' to "\u03c5\u0308\u0300",  // ῢ = \"\`{υ}
        '\u1fe1' to "\u03c5\u0304",  // ῡ = \={υ}
        '\u1fe0' to "\u03c5\u0306",  // ῠ = \u{υ}
        '\u03ce' to "\u03c9\u0301",  // ώ = \'{ω}
        '\u1f7c' to "\u03c9\u0300",  // ὼ = \`{ω}
        '\u038e' to "\u03a5\u0301",  // Ύ = \'{Υ}
        '\u1fea' to "\u03a5\u0300",  // Ὺ = \`{Υ}
        '\u03ab' to "\u03a5\u0308",  // Ϋ = \"{Υ}
        '\u1fe9' to "\u03a5\u0304",  // Ῡ = \={Υ}
        '\u1fe8' to "\u03a5\u0306",  // Ῠ = \u{Υ}
        '\u038f' to "\u03a9\u0301",  // Ώ = \'{Ω}
        '\u1ffa' to "\u03a9\u0300"  // Ὼ = \`{Ω}
    )}
}
