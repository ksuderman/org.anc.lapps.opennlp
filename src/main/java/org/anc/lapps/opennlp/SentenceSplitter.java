package org.anc.lapps.opennlp;

import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import opennlp.tools.sentdetect.SentenceDetector;

import org.lappsgrid.api.*;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.Types;
import org.lappsgrid.utils.ResourceLoader;

//import opennlp.tools.lang.english
public class SentenceSplitter implements WebService
{
    protected SentenceDetector splitter;

    public SentenceSplitter() throws IOException
    {
        ResourceLoader loader = new ResourceLoader();
        InputStream stream = loader.open("en-sent.bin");
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
        if (input.getDiscriminator() != Types.TEXT)
        {
            return DataFactory.error("Invalid input type, expected TEXT.");
        }
        String[] sentences = splitter.sentDetect(input.getPayload());
//        StringBuilder buffer = new StringBuilder(4096);
//        for (String sentence : sentences)
//        {
//            buffer.append(sentence);
//            buffer.append('\n');
//        }
//        return new Data(Types.OPENNLP, buffer.toString());
        return DataFactory.stringList(sentences);
    }

    public Data configure(Data config)
    {
        return DataFactory.ok();
    }

    public static void main(String[] args)
    {
        ResourceLoader loader = new ResourceLoader();
        InputStream stream = loader.open("en-sent.bin");
        if (stream == null)
        {
            System.out.println("Sentence model not found.");
        }
        else
        {
            System.out.println("Found the sentence model.");
        }
    }

}

