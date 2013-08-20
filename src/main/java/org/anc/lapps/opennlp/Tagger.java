package org.anc.lapps.opennlp;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.Types;

import java.io.IOException;
import java.io.InputStream;

import org.anc.resource.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Keith Suderman
 */
public class Tagger implements WebService
{
   private static final Logger logger = LoggerFactory.getLogger(Tagger.class);

   protected POSTagger tagger;
   protected String error;

   public Tagger()
   {
      logger.info("Creating OpenNLP tagger.");
//      ResourceLoader loader = new ResourceLoader();
      InputStream stream = ResourceLoader.open("en-pos-maxent.bin");
      if (stream == null)
      {
         error = "Unable to load tagger model.";
      }
      try
      {
         try
         {
            POSModel model = new POSModel(stream);
            tagger = new POSTaggerME(model);
         }
         finally
         {
            stream.close();
         }
      }
      catch (IOException e)
      {
         logger.error("Unable to create the tagger",e );
         if (tagger == null)
         {
            error = e.getMessage();
         }
      }
   }

   @Override
   public long[] requires()
   {
      return new long[] { Types.OPENNLP, Types.SENTENCE, Types.TOKEN };
   }

   @Override
   public long[] produces()
   {
      return new long[] { Types.OPENNLP, Types.SENTENCE, Types.TOKEN, Types.POS };
   }

   @Override
   public Data execute(Data input)
   {
      logger.info("Executing OpenNLP tagger.");
      if (tagger == null)
      {
         return DataFactory.error(error);
      }

      if (input.getDiscriminator() != Types.OPENNLP)
      {
         return DataFactory.error("Invalid input type. Expected OPENNLP");
      }
      String[] sentences = input.getPayload().split("\\n+");
      String[] tagged = tagger.tag(sentences);
      logger.info("Tagger complete.");
      return DataFactory.stringList(tagged);
   }

   @Override
   public Data configure(Data data)
   {
      return DataFactory.ok();
   }
}
