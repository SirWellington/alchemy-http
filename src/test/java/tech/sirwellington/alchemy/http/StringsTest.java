package tech.sirwellington.alchemy.http;

import org.hamcrest.Matchers;
import org.junit.*;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(AlchemyTestRunner.class)
@Repeat
public class StringsTest
{

    @GenerateString
    private String string;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();

    }

    @Test
    public void testNullToEmpty() throws Exception
    {
        String result = Strings.nullToEmpty(string);
        assertThat(result, equalTo(string));

        result = Strings.nullToEmpty(null);
        assertThat(result, notNullValue());
        assertThat(result, equalTo(""));
    }

    @Test
    public void testIsNullOrEmpty() throws Exception
    {
        assertFalse(Strings.isNullOrEmpty(string));
        assertTrue(Strings.isNullOrEmpty(null));
        assertTrue(Strings.isNullOrEmpty(""));
    }

    private void setupData() throws Exception
    {

    }

    private void setupMocks() throws Exception
    {

    }
}