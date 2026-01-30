package com.digia.digiaexpr

import com.digia.digiaexpr.ast.ASTNode
import com.digia.digiaexpr.callable.ExprCallable
import com.digia.digiaexpr.callable.ExprClass
import com.digia.digiaexpr.callable.ExprClassInstance
import com.digia.digiaexpr.context.BasicExprContext
import com.digia.digiaexpr.evaluator.ASTEvaluator
import org.junit.Test
import org.junit.Assert.*

/**
 * Basic Expressions Test Suite
 * Ported from Dart digia_expr tests to ensure exact compatibility
 */
class DigiaExprTest {
    
    @Test
    fun testMathFunctionsSumAndMul() {
        val code = "sum(mul(x,4),y)"
        val context = BasicExprContext(variables = mapOf("x" to 10, "y" to 2))
        val result = Expression.eval(code, context)
        assertEquals(42.0, result)
    }
    
    @Test
    fun testStringConcatenation() {
        val code = "concat('abc', 'xyz')"
        val result = Expression.eval(code, null)
        assertEquals("abcxyz", result)
    }
    
    @Test
    fun testSingleStringInterpolation() {
        val code = "Hello \${aVar}!"
        val result = Expression.eval(code, BasicExprContext(variables = mapOf("aVar" to "World")))
        assertEquals("Hello World!", result)
    }
    
    @Test
    fun testMultipleStringInterpolationCase1() {
        val code = "Hello \${a} & \${b}!"
        val result = Expression.eval(code, BasicExprContext(variables = mapOf("a" to "Alpha", "b" to "Beta")))
        assertEquals("Hello Alpha & Beta!", result)
    }
    
    @Test
    fun testMultipleStringInterpolationCase2() {
        val code = "\${a}"
        val result = Expression.eval(code, BasicExprContext(variables = mapOf("a" to "Alpha", "b" to "Beta")))
        assertEquals("Alpha", result)
    }
    
    @Test
    fun testMultipleStringInterpolationCase3() {
        val code = "\${a}, \${b}"
        val result = Expression.eval(code, BasicExprContext(variables = mapOf("a" to "Alpha", "b" to "Beta")))
        assertEquals("Alpha, Beta", result)
    }
    
    @Test
    fun testAccessFieldFromObject() {
        val code = "Hello \${person.name}!"
        val result = Expression.eval(code, BasicExprContext(variables = mapOf(
            "person" to ExprClassInstance(
                klass = ExprClass(
                    name = "Person",
                    fields = mutableMapOf("name" to "Tushar"),
                    methods = emptyMap()
                )
            )
        )))
        assertEquals("Hello Tushar!", result)
    }
    
    @Test
    fun testExecuteMethodOfObject() {
        val testValue = 10
        val data = mapOf("count" to testValue)
        val code = "\${storage.get('count')}"
        
        val result = Expression.eval(code, BasicExprContext(variables = mapOf(
            "storage" to ExprClassInstance(
                klass = ExprClass(
                    name = "LocalStorage",
                    fields = mutableMapOf(),
                    methods = mapOf(
                        "get" to TestMethod { evaluator, args ->
                            data[evaluator.eval(args.first() as ASTNode)]
                        }
                    )
                )
            )
        )))
        assertEquals(testValue, result)
    }
    
    @Test
    fun testAccessFieldFromNestedObject() {
        val testValue = 10
        val code = "\${sum(a.b.c.d(), a.e)}"
        
        val result = Expression.eval(code, BasicExprContext(variables = mapOf(
            "a" to ExprClassInstance(
                klass = ExprClass(
                    name = "Test",
                    fields = mutableMapOf(
                        "b" to ExprClassInstance(
                            klass = ExprClass(
                                name = "Test",
                                fields = mutableMapOf(
                                    "c" to ExprClassInstance(
                                        klass = ExprClass(
                                            name = "Test",
                                            fields = mutableMapOf(),
                                            methods = mapOf(
                                                "d" to TestMethod { _, _ -> testValue }
                                            )
                                        )
                                    )
                                ),
                                methods = emptyMap()
                            )
                        ),
                        "e" to testValue
                    ),
                    methods = emptyMap()
                )
            )
        )))
        assertEquals((testValue + testValue).toDouble(), result)
    }
    
    @Test
    fun testAccessJsonObjectUsingDotNotation() {
        val code = "\${sum(jsonObject.a.b, jsonObject.a.c)}"
        val result = Expression.eval(code, BasicExprContext(variables = mapOf(
            "jsonObject" to mapOf(
                "a" to mapOf("b" to 10, "c" to 2)
            )
        )))
        assertEquals(12.0, result)
    }
    
    @Test
    fun testJsonGet() {
        val testValue = "https://i.imgur.com/tFUQrOe.png"
        val code = "\${get(dataSource, 'data.liveLearning.img')}"
        val result = Expression.eval(code, BasicExprContext(variables = mapOf(
            "dataSource" to mapOf(
                "data" to mapOf(
                    "liveLearning" to mapOf("img" to testValue)
                )
            )
        )))
        assertEquals(testValue, result)
    }
    
    @Test
    fun testIsEqual() {
        val testValue = true
        val code = "\${eq(10, 10)}"
        val result = Expression.eval(code, null)
        assertEquals(testValue, result)
    }
    
    @Test
    fun testIsNotEqual() {
        val testValue = true
        val code = "\${neq(10, 15)}"
        val result = Expression.eval(code, null)
        assertEquals(testValue, result)
    }
    
    @Test
    fun testIsoFormat() {
        val testValue = "2024-06-03T23:42:36Z"
        val output = "3rd June"
        val result = Expression.eval("\${isoFormat(isoDate, 'Do MMMM')}", 
            BasicExprContext(variables = mapOf("isoDate" to testValue)))
        assertEquals(output, result)
    }
    
    @Test
    fun testIsoFormatLeapYear() {
        val testValue = "2024-02-29T00:00:00Z"
        val output = "29th February"
        val result = Expression.eval("\${isoFormat(isoDate, 'Do MMMM')}", 
            BasicExprContext(variables = mapOf("isoDate" to testValue)))
        assertEquals(output, result)
    }
    
    @Test
    fun testNumberFormat() {
        assertEquals("4,56,786", Expression.eval("\${numberFormat(456786)}", null))
    }
    
    @Test
    fun testCustomNumberFormat1() {
        assertEquals("123,456,789", Expression.eval("\${numberFormat(123456789, '#,###,000')}", null))
    }
    
    @Test
    fun testCustomNumberFormat2() {
        assertEquals("30,000", Expression.eval("\${numberFormat(30000, '##,##,###')}", null))
    }
    
    @Test
    fun testToIntFromInteger() {
        assertEquals(100, Expression.eval("\${toInt(100)}", null))
    }
    
    @Test
    fun testToIntFromFloat() {
        assertEquals(100, Expression.eval("\${toInt(100.1)}", null))
    }
    
    @Test
    fun testToIntFromString() {
        assertEquals(100, Expression.eval("\${toInt('100.1')}", null))
    }
    
    @Test
    fun testToIntFromHex() {
        assertEquals(100, Expression.eval("\${toInt('0x64')}", null))
    }
    
    @Test
    fun testComplexConditionWithNumberFormat() {
        val code = "\${condition(isEqual(a, b), 'Note: NPCI may flag repeat transactions of the same amount as duplicates and might reject them. As a precaution, we will deduct ₹\${numberFormat(b)} from your account.', 'Note: You will receive confirmation emails on each steps')}"
        val result = Expression.eval(code, BasicExprContext(variables = mapOf("a" to 1001, "b" to 1001)))
        assertEquals("Note: NPCI may flag repeat transactions of the same amount as duplicates and might reject them. As a precaution, we will deduct ₹1,001 from your account.", result)
    }
    
    @Test
    fun testStringLength() {
        val code = "\${isEqual(strLength(x), length)}"
        val result = Expression.eval(code, BasicExprContext(variables = mapOf("x" to "hello-world", "length" to 11)))
        assertEquals(true, result)
    }
    
    @Test
    fun testQsEncode() {
        val payload = mapOf(
            "key1" to 11,
            "key2" to "str",
            "key3" to false,
            "key4" to 0,
            "key5" to mapOf("cKey1" to true),
            "key6" to listOf(0, 1),
            "key7" to listOf(
                mapOf("cKey1" to 233),
                mapOf("cKey2" to false)
            )
        )
        val result = Expression.eval("\${qsEncode(payload)}", 
            BasicExprContext(variables = mapOf("payload" to payload)))
        assertEquals("key1=11&key2=str&key3=false&key4=0&key5[cKey1]=true&key6=0&key6=1&key7[cKey1]=233&key7[cKey2]=false", result)
    }
    
    @Test
    fun testIfWithoutElseTruthyCondition() {
        assertEquals(false, Expression.eval("\${if(true, false)}", null))
    }
    
    @Test
    fun testIfWithoutElseFalsyCondition() {
        assertEquals(null, Expression.eval("\${if(false, false)}", null))
    }
    
    @Test
    fun testIfWithElseTruthyCondition() {
        assertEquals(false, Expression.eval("\${if(true, false, true)}", null))
    }
    
    @Test
    fun testIfWithElseFalsyCondition() {
        assertEquals(true, Expression.eval("\${if(false, false, true)}", null))
    }
    
    @Test
    fun testMultiIfWithoutElseFirstTruthy() {
        assertEquals("a", Expression.eval("\${if(true, 'a', true, 'b')}", null))
    }
    
    @Test
    fun testMultiIfWithoutElseFirstFalse() {
        assertEquals("b", Expression.eval("\${if(false, 'a', true, 'b')}", null))
    }
    
    @Test
    fun testMultiIfWithoutElseAllFalse() {
        assertEquals(null, Expression.eval("\${if(false, 'a', false, 'b')}", null))
    }
    
    @Test
    fun testMultiIfWithElseAllFalse() {
        assertEquals("c", Expression.eval("\${if(false, 'a', false, 'b', 'c')}", null))
    }
    
    @Test
    fun testGreaterThanFalse() {
        assertEquals(false, Expression.eval("\${gt(1, 2)}", null))
    }
    
    @Test
    fun testGreaterThanTrue() {
        assertEquals(true, Expression.eval("\${gt(2.1, 1.2)}", null))
    }
    
    @Test
    fun testGreaterThanOrEqualFalse() {
        assertEquals(false, Expression.eval("\${gte(1.2, 2.1)}", null))
    }
    
    @Test
    fun testGreaterThanOrEqualTrue() {
        assertEquals(true, Expression.eval("\${gte(2.1, 2.1)}", null))
    }
    
    @Test
    fun testLessThanTrue() {
        assertEquals(true, Expression.eval("\${lt(1, 2)}", null))
    }
    
    @Test
    fun testLessThanFalse() {
        assertEquals(false, Expression.eval("\${lt(2.1, 1.2)}", null))
    }
    
    @Test
    fun testLessThanOrEqualTrue() {
        assertEquals(true, Expression.eval("\${lte(1.2, 2.1)}", null))
    }
    
    @Test
    fun testLessThanOrEqualTrue2() {
        assertEquals(true, Expression.eval("\${lte(2.1, 2.1)}", null))
    }
    
    @Test
    fun testNotTrueToFalse() {
        assertEquals(false, Expression.eval("\${not(true)}", null))
    }
    
    @Test
    fun testNotFalseToTrue() {
        assertEquals(true, Expression.eval("\${not(false)}", null))
    }
    
    @Test
    fun testLogicalOrFalseTrue() {
        assertEquals(true, Expression.eval("\${or(false, true)}", null))
    }
    
    @Test
    fun testLogicalOrFalseFalse() {
        assertEquals(false, Expression.eval("\${or(false, false)}", null))
    }
    
    @Test
    fun testFallbackValueNullCoalesce() {
        assertEquals("a", Expression.eval("\${or(if(false, false), 'a')}", null))
    }
    
    @Test
    fun testFallbackValueNonNull() {
        assertEquals("b", Expression.eval("\${or('b', 'a')}", null))
    }
    
    @Test
    fun testLogicalAndFalseTrue() {
        assertEquals(false, Expression.eval("\${and(false, true)}", null))
    }
    
    @Test
    fun testLogicalAndTrueTrue() {
        assertEquals(true, Expression.eval("\${and(true, true)}", null))
    }
    
    // ===== Advanced Test Cases =====
    
    @Test
    fun testIsoFormatWithDifferentPatterns() {
        val testValue = "2024-12-25T15:30:45Z"
        
        // Test various date format patterns
        assertEquals("25th December", 
            Expression.eval("\${isoFormat(isoDate, 'Do MMMM')}", 
                BasicExprContext(variables = mapOf("isoDate" to testValue))))
        
        assertEquals("December 2024", 
            Expression.eval("\${isoFormat(isoDate, 'MMMM YYYY')}", 
                BasicExprContext(variables = mapOf("isoDate" to testValue))))
        
        assertEquals("25/12/24", 
            Expression.eval("\${isoFormat(isoDate, 'DD/MM/YY')}", 
                BasicExprContext(variables = mapOf("isoDate" to testValue))))
    }
    
    @Test
    fun testIsoFormatEdgeCases() {
        // Test 1st, 2nd, 3rd, 21st, 22nd, 23rd
        assertEquals("1st January", 
            Expression.eval("\${isoFormat('2024-01-01T00:00:00Z', 'Do MMMM')}", null))
        
        assertEquals("2nd February", 
            Expression.eval("\${isoFormat('2024-02-02T00:00:00Z', 'Do MMMM')}", null))
        
        assertEquals("21st March", 
            Expression.eval("\${isoFormat('2024-03-21T00:00:00Z', 'Do MMMM')}", null))
        
        assertEquals("22nd April", 
            Expression.eval("\${isoFormat('2024-04-22T00:00:00Z', 'Do MMMM')}", null))
        
        assertEquals("23rd May", 
            Expression.eval("\${isoFormat('2024-05-23T00:00:00Z', 'Do MMMM')}", null))
        
        // Test 11th, 12th, 13th (special cases)
        assertEquals("11th June", 
            Expression.eval("\${isoFormat('2024-06-11T00:00:00Z', 'Do MMMM')}", null))
        
        assertEquals("12th July", 
            Expression.eval("\${isoFormat('2024-07-12T00:00:00Z', 'Do MMMM')}", null))
        
        assertEquals("13th August", 
            Expression.eval("\${isoFormat('2024-08-13T00:00:00Z', 'Do MMMM')}", null))
    }
    
    @Test
    fun testNestedFunctionCalls() {
        val code = "\${sum(mul(sum(2, 3), 4), mul(2, sum(1, 2)))}"
        val result = Expression.eval(code, null)
        // (2+3)*4 + 2*(1+2) = 5*4 + 2*3 = 20 + 6 = 26
        assertEquals(26.0, result)
    }
    
    @Test
    fun testComplexConditionalLogic() {
        val code = "\${if(and(gt(x, 10), lt(x, 20)), 'in range', if(gte(x, 20), 'too high', 'too low'))}"
        
        assertEquals("too low", 
            Expression.eval(code, BasicExprContext(variables = mapOf("x" to 5))))
        
        assertEquals("in range", 
            Expression.eval(code, BasicExprContext(variables = mapOf("x" to 15))))
        
        assertEquals("too high", 
            Expression.eval(code, BasicExprContext(variables = mapOf("x" to 25))))
    }
    
    @Test
    fun testComplexStringInterpolationWithExpressions() {
        val code = "Total: ₹\${numberFormat(sum(price, mul(price, divide(tax, 100))))} (including \${tax}% tax)"
        val result = Expression.eval(code, BasicExprContext(variables = mapOf(
            "price" to 1000,
            "tax" to 18
        )))
        assertEquals("Total: ₹1,180 (including 18% tax)", result)
    }
    
    @Test
    fun testChainedObjectAccess() {
        val code = "\${user.profile.address.city}"
        val result = Expression.eval(code, BasicExprContext(variables = mapOf(
            "user" to mapOf(
                "profile" to mapOf(
                    "address" to mapOf(
                        "city" to "Mumbai",
                        "state" to "Maharashtra"
                    )
                )
            )
        )))
        assertEquals("Mumbai", result)
    }
    
    @Test
    fun testComplexNumberFormatting() {
        // Test large numbers
        assertEquals("12,34,56,789", 
            Expression.eval("\${numberFormat(123456789)}", null))
        
        // Test with decimal places (if supported)
        assertEquals("1,23,457",
            Expression.eval("\${numberFormat(123456.789)}", null))
    }
    
    @Test
    fun testNumberFormatWithDifferentPatterns() {
        // Test with standard Western format pattern
        assertEquals("1,234,567", 
            Expression.eval("\${numberFormat(1234567, '#,###')}", null))
        
        // Test with Indian format pattern (default)
        assertEquals("12,34,567", 
            Expression.eval("\${numberFormat(1234567, '#,##,###')}", null))
        
        // Test with no grouping
        assertEquals("9876543", 
            Expression.eval("\${numberFormat(9876543, '#')}", null))
        
        // Test with minimum digits
        assertEquals("1,234", 
            Expression.eval("\${numberFormat(1234, '#,###,000')}", null))
    }
    
    @Test
    fun testMultipleLogicalOperators() {
        // (true OR false) AND (true AND true) = true AND true = true
        val code = "\${and(or(true, false), and(true, true))}"
        assertEquals(true, Expression.eval(code, null))
        
        // (false OR false) AND (true OR false) = false AND true = false
        val code2 = "\${and(or(false, false), or(true, false))}"
        assertEquals(false, Expression.eval(code2, null))
    }
    
    @Test
    fun testComplexComparisonChains() {
        val code = "\${and(gt(age, 18), eq(status, 'active'))}"
        
        assertEquals(true, Expression.eval(code, BasicExprContext(variables = mapOf(
            "age" to 30,
            "status" to "active"
        ))))
        
        assertEquals(false, Expression.eval(code, BasicExprContext(variables = mapOf(
            "age" to 17,
            "status" to "active"
        ))))
        
        assertEquals(false, Expression.eval(code, BasicExprContext(variables = mapOf(
            "age" to 30,
            "status" to "inactive"
        ))))
    }
    
    @Test
    fun testNullCoalescingChain() {
        val code = "\${or(or(a, b), or(c, 'default'))}"
        
        assertEquals("value", Expression.eval(code, BasicExprContext(variables = mapOf(
            "a" to "value",
            "b" to "backup1",
            "c" to "backup2"
        ))))
        
        assertEquals("backup2", Expression.eval(code, BasicExprContext(variables = mapOf(
            "a" to null,
            "b" to null,
            "c" to "backup2"
        ))))
        
        assertEquals("default", Expression.eval(code, BasicExprContext(variables = mapOf(
            "a" to null,
            "b" to null,
            "c" to null
        ))))
    }
    
    @Test
    fun testMixedTypeComparisons() {
        // Integer and Double comparisons
        assertEquals(true, Expression.eval("\${eq(10.0, 10.0)}", null))
        assertEquals(true, Expression.eval("\${gt(10.5, 10)}", null))
        assertEquals(true, Expression.eval("\${lte(9.9, 10)}", null))
    }
    
    @Test
    fun testComplexStringManipulation() {
        val code = "\${concat(concat('Hello ', name), concat('! You are ', concat(age)))} years old."
        val result = Expression.eval(code, BasicExprContext(variables = mapOf(
            "name" to "John",
            "age" to 25
        )))
        assertEquals("Hello John! You are 25 years old.", result)
    }
    
    @Test
    fun testNestedIfElseChains() {
        val code = "\${if(eq(grade, 'A'), 'Excellent', if(eq(grade, 'B'), 'Good', if(eq(grade, 'C'), 'Average', if(eq(grade, 'D'), 'Below Average', 'Fail'))))}"
        
        assertEquals("Excellent", Expression.eval(code, BasicExprContext(variables = mapOf("grade" to "A"))))
        assertEquals("Good", Expression.eval(code, BasicExprContext(variables = mapOf("grade" to "B"))))
        assertEquals("Average", Expression.eval(code, BasicExprContext(variables = mapOf("grade" to "C"))))
        assertEquals("Below Average", Expression.eval(code, BasicExprContext(variables = mapOf("grade" to "D"))))
        assertEquals("Fail", Expression.eval(code, BasicExprContext(variables = mapOf("grade" to "F"))))
    }
    
    @Test
    fun testComplexQsEncodeWithNestedStructures() {
        val payload = mapOf(
            "user" to mapOf(
                "name" to "John Doe",
                "age" to 30,
                "active" to true
            ),
            "items" to listOf(
                mapOf("id" to 1, "name" to "Item1"),
                mapOf("id" to 2, "name" to "Item2")
            ),
            "meta" to mapOf(
                "timestamp" to 1234567890,
                "version" to "1.0"
            )
        )
        val result = Expression.eval("\${qsEncode(data)}", 
            BasicExprContext(variables = mapOf("data" to payload)))
        
        // Verify it contains expected key-value pairs
        assertTrue(result is String)
        assertTrue((result as String).contains("user[name]=John Doe"))
        assertTrue(result.contains("user[age]=30"))
        assertTrue(result.contains("user[active]=true"))
    }

    @Test(expected = ArithmeticException::class)
    fun testZeroDivisionHandling() {
        val code = "\${divide(10, 0)}"
        Expression.eval(code, null)
    }
    
    @Test
    fun testStringLengthWithSpecialCharacters() {
//        assertEquals(true, Expression.eval("\${eq(strLength('Hello 🌍'), 8)}", null))
        assertEquals(true, Expression.eval("\${eq(strLength(''), 0)}", null))
        assertEquals(true, Expression.eval("\${eq(strLength('   '), 3)}", null))
    }
    
    @Test
    fun testComplexJsonPathAccess() {
        val complexData = mapOf(
            "data" to mapOf(
                "users" to listOf(
                    mapOf("id" to 1, "name" to "Alice", "role" to "admin"),
                    mapOf("id" to 2, "name" to "Bob", "role" to "user")
                ),
                "settings" to mapOf(
                    "theme" to "dark",
                    "notifications" to mapOf(
                        "email" to true,
                        "push" to false
                    )
                )
            )
        )
        
        assertEquals(true, 
            Expression.eval("\${get(data, 'data.settings.notifications.email')}", 
                BasicExprContext(variables = mapOf("data" to complexData))))
    }
    
    @Test
    fun testMultipleConversionsAndOperations() {
        val code = "\${sum(toInt('100'), mul(toInt('5.5'), 2))}"
        val result = Expression.eval(code, null)
        // 100 + (5 * 2) = 100 + 10 = 110
        assertEquals(110.0, result)
    }
    

    
//    @Test
//    fun testMultiLevelTransitiveVariableResolution() {
//        // Test deeper chain: ${a} -> ${b} -> ${c} -> 100
//        val code = "\${a}"
//        val context = BasicExprContext(variables = mapOf(
//            "c" to 100,
//            "b" to "\${c}",
//            "a" to "\${b}"
//        ))
//        val result = Expression.eval(code, context)
//        assertEquals(100, result)
//    }
}

/**
 * Helper class for testing method calls on objects
 * Equivalent to Dart's _TestMethod
 */
class TestMethod(
    private val _arity: Int = 0,
    val f: (ASTEvaluator, List<Any>) -> Any?
) : ExprCallable {
    
    override val name: String = "TestMethod"
    
    override fun arity(): Int = _arity
    
    override fun call(evaluator: ASTEvaluator, arguments: List<Any>): Any? {
        return f(evaluator, arguments)
    }
}
