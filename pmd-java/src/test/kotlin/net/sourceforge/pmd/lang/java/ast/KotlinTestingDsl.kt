package net.sourceforge.pmd.lang.java.ast

import io.kotlintest.Matcher
import io.kotlintest.Result
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldThrow
import io.kotlintest.specs.AbstractFunSpec
import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.ast.test.*
import net.sourceforge.pmd.lang.java.ParserTstUtil
import io.kotlintest.should as kotlintestShould


/**
 * Represents the different Java language versions.
 */
enum class JavaVersion : Comparable<JavaVersion> {
    J1_3, J1_4, J1_5, J1_6, J1_7, J1_8, J9, J10, J11;

    /** Name suitable for use with e.g. [ParserTstUtil.parseAndTypeResolveJava] */
    val pmdName: String = name.removePrefix("J").replace('_', '.')

    /**
     * Overloads the range operator, e.g. (`J9..J11`).
     * If both operands are the same, a singleton list is returned.
     */
    operator fun rangeTo(last: JavaVersion): List<JavaVersion> =
            when {
                last == this -> listOf(this)
                last.ordinal > this.ordinal -> values().filter { ver -> ver >= this && ver <= last }
                else -> values().filter { ver -> ver <= this && ver >= last }
            }

    companion object {
        val Latest = values().last()
        val Earliest = values().first()
    }
}


/**
 * Specify several tests at once for different java versions.
 * One test will be generated per version in [javaVersions].
 * Use [focusOn] to execute one test in isolation.
 *
 * @param name Name of the test. Will be postfixed by the specific
 *             java version used to run it
 * @param javaVersions Language versions for which to generate tests
 * @param focusOn Sets the java version of the test to isolate
 * @param assertions Assertions and further configuration
 *                   to perform with the parsing context
 */
fun AbstractFunSpec.parserTest(name: String,
                               javaVersions: List<JavaVersion>,
                               focusOn: JavaVersion? = null,
                               assertions: ParserTestCtx.() -> Unit) {

    javaVersions.forEach {

        val focus = if (focusOn != null && focusOn == it) "f:" else ""

        test("$focus$name (Java ${it.pmdName})") {
            ParserTestCtx(it).assertions()
        }
    }
}

/**
 * Specify a new test for a single java version. To execute the test in isolation,
 * prefix the name with `"f:"`.
 *
 * @param name Name of the test. Will be postfixed by the [javaVersion]
 * @param javaVersion Language version to use when parsing
 * @param assertions Assertions and further configuration
 *                   to perform with the parsing context
 */
fun AbstractFunSpec.parserTest(name: String,
                               javaVersion: JavaVersion = JavaVersion.Latest,
                               assertions: ParserTestCtx.() -> Unit) {
    parserTest(name, listOf(javaVersion), null, assertions)
}


/**
 * Defines a group of tests that should be named similarly,
 * executed on several java versions. Calls to "should" in
 * the block are intercepted to create a new test, with the
 * given [name] as a common prefix.
 *
 * This is useful to make a batch of grammar specs for grammar
 * regression tests without bothering to find a name.
 *
 * @param name Common prefix for the test names
 * @param javaVersions Language versions for which to generate tests
 * @param spec Assertions. Each call to [io.kotlintest.should] on a string
 *             receiver is replaced by a [GroupTestCtx.should], which creates a
 *             new parser test.
 */
fun AbstractFunSpec.testGroup(name: String,
                              javaVersions: List<JavaVersion>,
                              spec: GroupTestCtx.() -> Unit) {
    javaVersions.forEach {
        testGroup(name, it, spec)
    }
}


/**
 * Defines a group of tests that should be named similarly.
 * Calls to "should" in the block are intercepted to create
 * a new test, with the given [name] as a common prefix.
 *
 * This is useful to make a batch of grammar specs for grammar
 * regression tests without bothering to find a name.
 *
 * @param name Common prefix for the test names
 * @param javaVersion Language versions to use when parsing
 * @param spec Assertions. Each call to [io.kotlintest.should] on a string
 *             receiver is replaced by a [GroupTestCtx.should], which creates a
 *             new parser test.
 *
 */
fun AbstractFunSpec.testGroup(name: String,
                              javaVersion: JavaVersion = JavaVersion.Latest,
                              spec: GroupTestCtx.() -> Unit) {
    GroupTestCtx(this, name, javaVersion).spec()
}

class GroupTestCtx(private val funspec: AbstractFunSpec, private val groupName: String, javaVersion: JavaVersion) : ParserTestCtx(javaVersion) {

    infix fun String.should(matcher: Matcher<String>) {
        funspec.parserTest("$groupName: '$this'") {
            this@should kotlintestShould matcher
        }
    }

}


/**
 * Extensible environment to describe parse/match testing workflows in a concise way.
 * Can be used inside of a [io.kotlintest.specs.FunSpec] with [parserTest].
 *
 * Parsing contexts allow to parse a string containing only the node you're interested
 * in instead of writing up a full class that the parser can handle. See [parseAstExpression],
 * [parseAstStatement].
 *
 * The methods [parseExpression] and [parseStatement] add some sugar to those by skipping
 * some nodes we're not interested in to find the node of interest using their reified type
 * parameter.
 *
 * These are implicitly used by [matchExpr] and [matchStmt], which specify a matcher directly
 * on the strings, using their type parameter and the info in this test context to parse, find
 * the node, and execute the matcher in a single call. These may be used by [io.kotlintest.should],
 * e.g.
 *
 *      parserTest("Test ShiftExpression operator") {
 *          "1 >> 2" should matchExpr<ASTShiftExpression>(ignoreChildren = true) {
 *              it.operator shouldBe ">>"
 *          }
 *      }
 *
 *
 * Import statements in the parsing contexts can be configured by adding types to [importedTypes],
 * or strings to [otherImports].
 *
 * Technically the utilities provided by this class may be used outside of [io.kotlintest.specs.FunSpec]s,
 * e.g. in regular JUnit tests, but I think we should strive to uniformize our testing style,
 * especially since KotlinTest defines so many.
 *
 * TODO allow to reference an existing type as the parsing context, for full type resolution
 *
 * @property javaVersion The java version that will be used for parsing.
 * @property importedTypes Types to import at the beginning of parsing contexts
 * @property otherImports Other imports, without the `import` and semicolon
 */
open class ParserTestCtx(val javaVersion: JavaVersion = JavaVersion.Latest,
                         val importedTypes: MutableList<Class<*>> = mutableListOf(),
                         val otherImports: MutableList<String> = mutableListOf()) {

    /** Imports to add to the top of the parsing contexts. */
    internal val imports: List<String>
        get() {
            val types = importedTypes.mapNotNull { it.canonicalName }.map { "import $it;" }
            return types + otherImports.map { "import $it;" }
        }

    inline fun <reified N : Node> makeMatcher(nodeParsingCtx: NodeParsingCtx<*>, ignoreChildren: Boolean, noinline nodeSpec: NWrapper<N>.() -> Unit): Matcher<String> =
            object : Matcher<String> {
                override fun test(value: String): Result =
                        matchNode(ignoreChildren, nodeSpec).test(nodeParsingCtx.parseAndFind<N>(value))
            }


    /**
     * Returns a String matcher that parses the node using [parseExpression] with
     * type param [N], then matches it against the [nodeSpec] using [matchNode].
     *
     */
    inline fun <reified N : Node> matchExpr(ignoreChildren: Boolean = false,
                                            noinline nodeSpec: NWrapper<N>.() -> Unit): Matcher<String> =
            makeMatcher(ExpressionParsingCtx(this), ignoreChildren, nodeSpec)

    /**
     * Returns a String matcher that parses the node using [parseStatement] with
     * type param [N], then matches it against the [nodeSpec] using [matchNode].
     */
    inline fun <reified N : Node> matchStmt(ignoreChildren: Boolean = false,
                                            noinline nodeSpec: NWrapper<N>.() -> Unit) =
            makeMatcher(StatementParsingCtx(this), ignoreChildren, nodeSpec)


    /**
     * Returns a String matcher that parses the node using [parseType] with
     * type param [N], then matches it against the [nodeSpec] using [matchNode].
     */
    inline fun <reified N : Node> matchType(ignoreChildren: Boolean = false,
                                            noinline nodeSpec: NWrapper<N>.() -> Unit) =
            makeMatcher(TypeParsingCtx(this), ignoreChildren, nodeSpec)


    /**
     * Expect a parse exception to be thrown by [block].
     * The message is asserted to contain [messageContains].
     */
    fun expectParseException(messageContains: String, block: () -> Unit) {

        val thrown = shouldThrow<ParseException>(block)

        thrown.message.shouldContain(messageContains)

    }


    fun parseAstExpression(expr: String): ASTExpression = ExpressionParsingCtx(this).parseNode(expr)


    fun parseAstStatement(statement: String): ASTBlockStatement = StatementParsingCtx(this).parseNode(statement)

    fun parseAstType(type: String): ASTType = TypeParsingCtx(this).parseNode(type)


    inline fun <reified N : Node> parseExpression(expr: String): N = ExpressionParsingCtx(this).parseAndFind(expr)

    // don't forget the semicolon
    inline fun <reified N : Node> parseStatement(stmt: String): N = StatementParsingCtx(this).parseAndFind(stmt)

    inline fun <reified N : Node> parseType(type: String): N = TypeParsingCtx(this).parseAndFind(type)


    companion object {


        /**
         * Finds the first descendant of type [N] of [this] node which is
         * accessible in a straight line. The descendant must be accessible
         * from the [this] on a path where each node has a single child.
         *
         * If one node has another child, the search is aborted and the method
         * returns null.
         */
        fun <N : Node> Node.findFirstNodeOnStraightLine(klass: Class<N>): N? {
            return when {
                klass.isInstance(this) -> {
                    @Suppress("UNCHECKED_CAST")
                    val n = this as N
                    n
                }
                this.numChildren == 1 -> getChild(0).findFirstNodeOnStraightLine(klass)
                else -> null
            }
        }

        /**
         * Describes a kind of node that can be found commonly in the same contexts.
         * This type defines some machinery to parse a string to this kind of node
         * without much ado by placing it in a specific parsing context.
         */
        abstract class NodeParsingCtx<T : Node>(val constructName: String, protected val ctx: ParserTestCtx) {

            abstract fun getTemplate(construct: String): String

            abstract fun retrieveNode(acu: ASTCompilationUnit): T

            /**
             * Parse the string in the context described by this object. The parsed node is usually
             * the child of the returned [T] node. Note that [parseAndFind] can save you some keystrokes
             * because it finds a descendant of the wanted type.
             *
             * @param construct The construct to parse
             *
             * @return A [T] whose child is the given statement
             *
             * @throws ParseException If the argument is no valid construct of this kind (mind the language version)
             */
            fun parseNode(construct: String): T {
                val root = ParserTstUtil.parseAndTypeResolveJava(ctx.javaVersion.pmdName, getTemplate(construct))

                return retrieveNode(root)
            }

            /**
             * Parse the string the context described by this object, and finds the first descendant of type [N].
             * The descendant is searched for by [findFirstNodeOnStraightLine], to prevent accidental
             * mis-selection of a node. In such a case, a [NoSuchElementException] is thrown, and you
             * should fix your test case.
             *
             * @param construct The construct to parse
             * @param N The type of node to find
             *
             * @return The first descendant of type [N] found in the parsed expression
             *
             * @throws NoSuchElementException If no node of type [N] is found by [findFirstNodeOnStraightLine]
             * @throws ParseException If the argument is no valid construct of this kind
             *
             */
            inline fun <reified N : Node> parseAndFind(construct: String): N =
                    parseNode(construct).findFirstNodeOnStraightLine(N::class.java)
                    ?: throw NoSuchElementException("No node of type ${N::class.java.simpleName} in the given $constructName:\n\t$construct")

        }


        class ExpressionParsingCtx(ctx: ParserTestCtx) : NodeParsingCtx<ASTExpression>("expression", ctx) {

            override fun getTemplate(construct: String): String =
                """
                ${ctx.imports.joinToString(separator = "\n")}
                class Foo {
                    {
                        Object o = $construct;
                    }
                }
                """.trimIndent()


            override fun retrieveNode(acu: ASTCompilationUnit): ASTExpression = acu.getFirstDescendantOfType(ASTVariableInitializer::class.java).getChild(0) as ASTExpression
        }

        class StatementParsingCtx(ctx: ParserTestCtx) : NodeParsingCtx<ASTBlockStatement>("statement", ctx) {

            override fun getTemplate(construct: String): String =
                """
                ${ctx.imports.joinToString(separator = "\n")}
                class Foo {
                    {
                        $construct
                    }
                }
                """.trimIndent()


            override fun retrieveNode(acu: ASTCompilationUnit): ASTBlockStatement = acu.getFirstDescendantOfType(ASTBlockStatement::class.java)
        }

        class TypeParsingCtx(ctx: ParserTestCtx) : NodeParsingCtx<ASTType>("type", ctx) {
            override fun getTemplate(construct: String): String =
                """
                ${ctx.imports.joinToString(separator = "\n")}
                class Foo {
                    $construct foo;
                }
                """.trimIndent()

            override fun retrieveNode(acu: ASTCompilationUnit): ASTType = acu.getFirstDescendantOfType(ASTType::class.java)
        }

    }
}

