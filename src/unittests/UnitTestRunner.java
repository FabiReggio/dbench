package unittests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

// add all unit test classes here :)
@Suite.SuiteClasses({ 
    MongoDBUnitTests.class
})
public class UnitTestRunner {}
