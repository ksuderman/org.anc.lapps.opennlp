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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.anc.resource.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Keith Suderman
 */
public class Tagger implements WebService
{
   private static final Logger logger = LoggerFactory.getLogger(Tagger.class);
   private static final int POOL_SIZE = 4;

//   protected POSTagger tagger;
   protected String error;
   protected BlockingQueue<POSTagger> pool;

   public Tagger()
   {
      logger.info("Creating OpenNLP tagger.");
      InputStream stream = ResourceLoader.open("en-pos-maxent.bin");
      if (stream == null)
      {
         error = "Unable to load tagger model.";
      }
      try
      {
         pool = new ArrayBlockingQueue<POSTagger>(POOL_SIZE);
         try
         {
            POSModel model = new POSModel(stream);
            for (int i = 0; i < POOL_SIZE; ++i)
            {
               pool.add(new POSTaggerME(model));
            }
         }
         finally
         {
            stream.close();
         }
      }
      catch (IOException e)
      {
         logger.error("Unable to create the tagger",e );
         error = e.getMessage();
      }
   }

   @Override
   public long[] requires()
   {
      return new long[] { Types.OPENNLP, Types.TOKEN };
   }

   @Override
   public long[] produces()
   {
      return new long[] { Types.OPENNLP, Types.TOKEN, Types.POS };
   }

   @Override
   public Data execute(Data input)
   {
      logger.info("Executing OpenNLP tagger.");
      if (error != null)
      {
         return DataFactory.error(error);
      }

      if (input.getDiscriminator() != Types.OPENNLP)
      {
         return DataFactory.error("Invalid input type. Expected OPENNLP");
      }
      String[] sentences = input.getPayload().split("\\n+");
      POSTagger tagger = null;
      Data data = null;
      try
      {
         tagger = pool.take();
         String[] tagged = tagger.tag(sentences);
//         List<String> combined = new ArrayList<String>();
//         for (int i = 0; i < sentences.length; ++i)
//         {
//            combined.add(sentences[i] +"/" + tagged[i]);
//         }
         logger.info("Tagger complete.");
         data = DataFactory.stringList(tagged);
         data.setDiscriminator(Types.OPENNLP);
      }
      catch (InterruptedException e)
      {
         data = DataFactory.error(e.getMessage());
      }
      finally
      {
         if (tagger != null)
         {
            pool.add(tagger);
         }
      }
      return data;
   }

   @Override
   public Data configure(Data data)
   {
      return DataFactory.error("Unsupported operation.");
   }
}
