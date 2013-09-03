package org.anc.lapps.opennlp;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.anc.resource.ResourceLoader;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author Keith Suderman
 */
public class Tokenizer implements WebService
{
   private static final Logger logger = LoggerFactory.getLogger(Tokenizer.class);
   private static final int POOL_SIZE = 4;

   //protected opennlp.tools.tokenize.Tokenizer tokenizer;
   protected BlockingQueue<TokenizerME> pool;
   protected String error;

   public Tokenizer()
   {
      logger.info("Creating OpenNLP Tokenizer.");
      InputStream stream = ResourceLoader.open("en-token.bin");
      if (stream == null)
      {
         error = "Unable to load tokenizer model.";
      }
      else try
      {
         try
         {
            TokenizerModel model = new TokenizerModel(stream);
            pool = new ArrayBlockingQueue<TokenizerME>(POOL_SIZE);
            for (int i = 0; i < POOL_SIZE; ++i)
            {
               pool.add(new TokenizerME(model));
            }
         }
         finally
         {
            stream.close();
         }
      }
      catch (IOException e)
      {
         logger.error("Unable to create the tokenizer", e);
         if (error == null)
         {
            error = e.getMessage();
         }
      }
   }

   @Override
   public long[] requires()
   {
      return new long[]{Types.OPENNLP, Types.SENTENCE};
   }

   @Override
   public long[] produces()
   {
      return new long[]{Types.OPENNLP, Types.SENTENCE, Types.TOKEN};
   }

   @Override
   public Data execute(Data input)
   {
      logger.info("Executing OpenNLP tokenizer.");
      if (error != null)
      {
         logger.error(error);
         return DataFactory.error(error);
      }
      TokenizerME tokenizer = null;
      Data data = null;
      try
      {
         tokenizer = pool.take();
         String[] tokens = tokenizer.tokenize(input.getPayload());
         logger.info("Tokenizer complete.");
         data = DataFactory.stringList(tokens);
         data.setDiscriminator(Types.OPENNLP);
      }
      catch (Exception e)
      {
         data = DataFactory.error(e.getMessage());
      }
      finally
      {
         if (tokenizer != null)
         {
            pool.add(tokenizer);
         }
      }
      return data;
   }

   @Override
   public Data configure(Data config)
   {
      return DataFactory.error("Unsupported operation.");
   }
}
