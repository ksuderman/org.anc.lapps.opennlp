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

/**
 * @author Keith Suderman
 */
public class Tokenizer implements WebService
{
   private static final Logger logger = LoggerFactory.getLogger(Tokenizer.class);

   protected opennlp.tools.tokenize.Tokenizer tokenizer;
   protected String error;

   public Tokenizer()
   {
      logger.info("Creating OpenNLP Tokenizer.");
      InputStream stream = ResourceLoader.open("en-token.bin");
      if (stream == null)
      {
         error = "Unable to load tokenizer model.";
      } else try
      {
         try
         {
            TokenizerModel model = new TokenizerModel(stream);
            tokenizer = new TokenizerME(model);
         }
         finally
         {
            stream.close();
         }
      }
      catch (IOException e)
      {
         logger.error("Unable to create the tokenizer", e);
         if (tokenizer == null)
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
      if (tokenizer == null)
      {
         if (error == null)
         {
            error = "Unable to instantiate the tokenizer.";
         }
         logger.error(error);
         return DataFactory.error(error);
      }
      String[] tokens = tokenizer.tokenize(input.getPayload());
      logger.info("Tokenizer complete.");
      return DataFactory.stringList(tokens);
   }

   @Override
   public Data configure(Data config)
   {
      return DataFactory.ok();
   }
}
