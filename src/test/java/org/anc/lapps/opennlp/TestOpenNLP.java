package org.anc.lapps.opennlp;

import org.anc.resource.ResourceLoader;
import org.junit.*;
import static org.junit.Assert.*;

import org.lappsgrid.api.*;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.discriminator.Types;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Keith Suderman
 */
@Ignore
public class TestOpenNLP
{
   private String text;

   public TestOpenNLP()
   {

   }

   @Before
   public void setup()
   {
      if (text == null)
      {
         try
         {
            text = ResourceLoader.loadString("Anti-Terrorist.txt");
            text = text.replaceAll("\\n", " ");
         }
         catch (IOException e)
         {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
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
      String[] tokens = data.getPayload().split("\\n");
      System.out.println("Return type: " + name);
      System.out.println("There are " + tokens.length + " tokens.");
      int i = 0;
      for (String token : tokens)
      {
         System.out.println(++i + " " + token);
      }
   }

   @Test
   public void testTagger() throws IOException
   {
      SentenceSplitter splitter = new SentenceSplitter();
      Tokenizer tokenizer = new Tokenizer();
      Tagger tagger = new Tagger();
      Data data = DataFactory.text(text);

      Data result = splitter.execute(data);
      assertTrue(result.getDiscriminator() != Types.ERROR);
      String[] sentences = result.getPayload().split("\\n");
      assertTrue(sentences.length > 1);
      for (String sentence : sentences)
      {
         Data input = DataFactory.text(sentence);
         result = tokenizer.execute(input);
         assertTrue(result.getDiscriminator() != Types.ERROR);
         String[] tokens = result.getPayload().split("\\n");
         Data tokensData = DataFactory.stringList(tokens);
         tokensData.setDiscriminator(Types.OPENNLP);
         result = tagger.execute(tokensData);
         String payload = result.getPayload();
         assertTrue(payload, result.getDiscriminator() != Types.ERROR);
         String[] tags = payload.split("\\s+");
         assertTrue("tags.length != tokens.length", tags.length == tokens.length);
         for (int i = 0; i < tokens.length; ++i)
         {
            System.out.println(i + ". " + tokens[i] + " " + tags[i]);
         }
      }
//      result = tokenizer.execute(result);
//      assertTrue(result.getDiscriminator() != Types.ERROR);
//
//      result = tagger.execute(result);
//      assertTrue(result.getDiscriminator() != Types.ERROR);
   }
}
