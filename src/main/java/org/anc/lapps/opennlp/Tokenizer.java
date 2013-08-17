package org.anc.lapps.opennlp;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.anc.resource.ResourceLoader;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.Types;

import java.io.InputStream;
import java.io.IOException;
/**
 * @author Keith Suderman
 */
public class Tokenizer implements WebService
{
    protected opennlp.tools.tokenize.Tokenizer tokenizer;
    protected String error;

    public Tokenizer()
    {
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
                tokenizer = new TokenizerME(model);
            }
            finally
            {
                stream.close();
            }
        }
        catch (IOException e)
        {
            if (tokenizer == null)
            {
                error = e.getMessage();
            }
        }
    }

    @Override
    public long[] requires()
    {
        return new long[] { Types.OPENNLP, Types.SENTENCE };
    }

    @Override
    public long[] produces()
    {
        return new long[] { Types.OPENNLP, Types.SENTENCE, Types.TOKEN };
    }

    @Override
    public Data execute(Data input)
    {
        if (tokenizer == null)
        {
            if (error == null)
            {
                error = "Unable to instantiate the tokenizer.";
            }
            return DataFactory.error(error);
        }
        String[] tokens = tokenizer.tokenize(input.getPayload());
        return DataFactory.stringList(tokens);
    }

    @Override
    public Data configure(Data config)
    {
        return DataFactory.ok();
    }
}
