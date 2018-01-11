package tech.sirwellington.alchemy.http;

import org.hamcrest.Matchers;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(AlchemyTestRunner.class)
public class SynchronousExecutorTest
{

    @Mock
    private Runnable command;

    private SynchronousExecutor instance;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();

        instance = SynchronousExecutor.newInstance();
    }

    @Test
    public void testNewInstance() throws Exception
    {
        instance = SynchronousExecutor.newInstance();
        assertThat(instance, notNullValue());
    }

    @Test
    public void testExecute() throws Exception
    {
        instance.execute(command);

        verify(command).run();
    }

    private void setupData() throws Exception
    {

    }

    private void setupMocks() throws Exception
    {

    }
}