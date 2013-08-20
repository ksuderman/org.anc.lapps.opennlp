package org.anc.lapps.opennlp;

import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import opennlp.tools.sentdetect.SentenceDetector;

import org.anc.resource.ResourceLoader;
import org.lappsgrid.api.*;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.discriminator.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import opennlp.tools.lang.english
public class SentenceSplitter implements WebService
{
   private static final Logger logger = LoggerFactory.getLogger(SentenceSplitter.class);
   protected SentenceDetector splitter;

   public SentenceSplitter() throws IOException
   {
      logger.info("Creating a OpenNLP sentence splitter.");
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      if (loader == null)
      {
         loader = SentenceSplitter.class.getClassLoader();
      }
      logger.debug("Loading en-sent.bin");
      InputStream stream = loader.getResourceAsStream("en-sent.bin");
      if (stream == null)
      {
         throw new IOException("Unable to load sentence model.");
      }
      try
      {
         SentenceModel model = new SentenceModel(stream);
         splitter = new SentenceDetectorME(model);
      }
      finally
      {
         stream.close();
      }
      logger.info("Sentence splitter created.");
   }

   public long[] requires()
   {
      return new long[]{Types.TEXT};
   }

   public long[] produces()
   {
      return new long[]{Types.OPENNLP, Types.SENTENCE};
   }

   public Data execute(Data input)
   {
      logger.info("Executing the OpenNLP splitter.");
      if (input.getDiscriminator() != Types.TEXT)
      {
         String type = DiscriminatorRegistry.get(input.getDiscriminator());
         logger.error("Invalid input. Expected text found {}", type);
         return DataFactory.error("Invalid input type, expected TEXT, found " + type);
      }
      String[] sentences = splitter.sentDetect(input.getPayload());
      logger.info("Execution complete.");
      return DataFactory.stringList(sentences);
   }

   public Data configure(Data config)
   {
      return DataFactory.ok();
   }

   public static void main(String[] args)
   {
      InputStream stream = ResourceLoader.open("en-sent.bin");
      if (stream == null)
      {
         System.out.println("Sentence model not found.");
      } else
      {
         System.out.println("Found the sentence model.");
      }
   }

}

