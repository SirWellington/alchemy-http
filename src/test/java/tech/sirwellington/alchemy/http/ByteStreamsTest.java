package tech.sirwellington.alchemy.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.*;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.generator.NumberGenerators;
import tech.sirwellington.alchemy.test.junit.runners.*;

@RunWith(AlchemyTestRunner.class)
@Repeat
public class ByteStreamsTest
{

    private Integer binarySize;

    private byte[] binary;

    private InputStream istream;


    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();

    }

    @Test
    public void testToByteArray() throws Exception
    {
        byte[] result = ByteStreams.toByteArray(istream);
        Assert.assertArrayEquals(binary, result);
    }

    private void setupData() throws Exception
    {
        binarySize = NumberGenerators.integers(100, 1000).get();
        binary = new byte[binarySize];
        istream = new ByteArrayInputStream(binary);
    }

    private void setupMocks() throws Exception
    {

    }
}