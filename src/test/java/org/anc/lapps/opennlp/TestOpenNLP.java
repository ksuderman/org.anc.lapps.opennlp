package org.anc.lapps.opennlp;

import org.junit.*;

import org.lappsgrid.api.*;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.discriminator.Types;
import org.lappsgrid.utils.ResourceLoader;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Keith Suderman
 */
public class TestOpenNLP
{
    private String text;

    public TestOpenNLP()
    {

    }

    @Before
    public void setup()
    {
        if (text == null) try
        {
            text = new ResourceLoader().loadString("Anti-Terrorist.txt");
        }
        catch (IOException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Test
    public void testSplitter() throws IOException
    {
        SentenceSplitter splitter = new SentenceSplitter();
        Data data = DataFactory.text(text);
        data = splitter.execute(data);
        if (data.getDiscriminator() == Types.ERROR)
        {
            fail(data.getPayload());
        }

        String name = DiscriminatorRegistry.get(data.getDiscriminator());
        String[] sentences = data.getPayload().split("\n");
        System.out.println("Return type: " + name);
        System.out.println("There are " + sentences.length + " sentences.");
        int i = 0;
        for (String sentence : sentences)
        {
            System.out.println(++i + " " + sentence);
        }
    }

    @Test
    public void testTokenizer()
    {
        Tokenizer tokenizer = new Tokenizer();
        Data data = DataFactory.text(text);
        data = tokenizer.execute(data);
        if (data.getDiscriminator() == Types.ERROR)
        {
            fail(data.getPayload());
        }

        String name = DiscriminatorRegistry.get(data.getDiscriminator());
        String[] tokens = data.getPayload().split("\n");
        System.out.println("Return type: " + name);
        System.out.println("There are " + tokens.length + " tokens.");
        int i = 0;
        for (String token : tokens)
        {
            System.out.println(++i + " " + token);
        }
    }

}
