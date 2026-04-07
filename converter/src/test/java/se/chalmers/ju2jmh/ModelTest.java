package se.chalmers.ju2jmh;


import org.junit.jupiter.api.Test;
import se.chalmers.ju2jmh.model.FixtureMethod;
import se.chalmers.ju2jmh.model.TestRule;
import se.chalmers.ju2jmh.model.UnitTest;
import se.chalmers.ju2jmh.model.UnitTestClass;

import static org.junit.jupiter.api.Assertions.*;

public class ModelTest {

    @Test
    public void testUnitTestEqualsAndHashCode() {
        UnitTest t1 = UnitTest.test("myTest");
        UnitTest t2 = UnitTest.test("myTest");
        UnitTest t3 = UnitTest.test("otherTest");
        UnitTest t4 = UnitTest.exceptionTest("myTest", "java.lang.Exception");

        // Test equality and hash code consistency
        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());

        // Test difference
        assertNotEquals(t1, t3);
        assertNotEquals(t1, t4);
        assertNotEquals(t1, null);
        assertNotEquals(t1, new Object());

        // Test toString
        assertEquals("@Test myTest();", t1.toString());
        assertEquals("@Test(expected=java.lang.Exception) myTest();", t4.toString());
    }

    @Test
    public void testFixtureMethodEqualsAndHashCode() {
        FixtureMethod f1 = FixtureMethod.before("setup");
        FixtureMethod f2 = FixtureMethod.before("setup");
        FixtureMethod f3 = FixtureMethod.after("teardown");
        FixtureMethod f4 = FixtureMethod.beforeClass("globalSetup");

        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());
        assertNotEquals(f1, f3);

        // Test isStatic() branches
        assertFalse(f1.isStatic());
        assertFalse(f3.isStatic());
        assertTrue(f4.isStatic());
        assertTrue(FixtureMethod.afterClass("globalTeardown").isStatic());
    }

    @Test
    public void testTestRuleEqualsAndHashCode() {
        TestRule r1 = TestRule.fromField("myRule");
        TestRule r2 = TestRule.fromField("myRule");
        TestRule r3 = TestRule.fromMethod("myRuleMethod");
        TestRule r4 = TestRule.fromStaticField("myClassRule");

        assertEquals(r1, r2);
        assertNotEquals(r1, r3);

        // Test isStatic() e source() branches
        assertFalse(r1.isStatic());
        assertTrue(r4.isStatic());
        assertEquals(TestRule.Source.FIELD, r1.source());
        assertEquals(TestRule.Source.METHOD, r3.source());
    }

    @Test
    public void testUnitTestClassBuilderExceptions() {
        UnitTestClass.Builder builder = UnitTestClass.Builder.forClass("MyClass");

        UnitTestClass superClass = UnitTestClass.Builder.forClass("SuperClass").build();
        builder.withSuperclass(superClass);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            builder.withSuperclass(superClass);
        });
        assertTrue(thrown.getMessage().contains("already set"));
    }

    @Test
    public void testUnitTestClassBuilderDuplicateRules() {
        UnitTestClass.Builder builder = UnitTestClass.Builder.forClass("MyClass");
        builder.withTest("testA");

        builder.withTest("testA");

        IllegalStateException thrown = assertThrows(IllegalStateException.class, builder::build);
        assertTrue(thrown.getMessage().contains("Duplicate test"));
    }

    @Test
    public void testTestRuleFullCoverage() {
        TestRule r1 = TestRule.fromField("myRule");
        TestRule rStatic = TestRule.fromStaticField("myRule");
        TestRule rMethod = TestRule.fromMethod("myRule");
        TestRule rStaticMethod = TestRule.fromStaticMethod("myRule");

        // uncover equals() branches
        assertNotEquals(r1, null);
        assertNotEquals(r1, new Object());
        assertNotEquals(r1, TestRule.fromField("otherRule"));
        assertNotEquals(r1, rStatic); //

        // toString() branches
        String s1 = r1.toString();
        assertTrue(s1.contains("Rule") && s1.contains("myRule"));

        String sStatic = rStatic.toString();
        assertTrue(sStatic.contains("ClassRule") && sStatic.contains("myRule"));

        String sMethod = rMethod.toString();
        assertTrue(sMethod.contains("Rule") && sMethod.contains("myRule()"));

        String sStaticMethod = rStaticMethod.toString();
        assertTrue(sStaticMethod.contains("ClassRule") && sStaticMethod.contains("myRule()"));
    }

    @Test
    public void testUnitTestClassFullCoverage() {
        // Create two identical classes
        UnitTestClass.Builder b1 = UnitTestClass.Builder.forClass("MyClass");
        b1.withTest("testA");
        UnitTestClass c1 = b1.build();

        UnitTestClass.Builder b2 = UnitTestClass.Builder.forClass("MyClass");
        b2.withTest("testA");
        UnitTestClass c2 = b2.build();

        // new class with different name
        UnitTestClass.Builder b3 = UnitTestClass.Builder.forClass("OtherClass");
        UnitTestClass c3 = b3.build();

        // Hash code and equals()
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());

        // Test differences
        assertNotEquals(c1, c3);
        assertNotEquals(c1, null);
        assertNotEquals(c1, new Object());

        // main getters
        assertEquals("MyClass", c1.name());
        assertEquals(1, c1.tests().size());

        // Test toString()
        assertTrue(c1.toString().contains("MyClass"));
    }
}