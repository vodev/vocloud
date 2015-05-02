package cz.rk.vocloud.ssap;

import cz.rk.vocloud.ssap.model.IndexedSSAPVotable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author radio.koza
 */
public class VotableParser {
    
    
    
    private VotableParser(){
        //nothing to do here - this class just have to be static factory
    }
    
    
    public static IndexedSSAPVotable parseVotable(InputStream stream) throws UnparseableVotableException {
        try {
            //using SAX parser for handling votable xml in input stream
            //first create sax parser factory
            SAXParserFactory factory = SAXParserFactory.newInstance();
            //create parser from factory
            SAXParser parser = factory.newSAXParser();
            //create new instance of handler
            VotableSSAPHandler handler = new VotableSSAPHandler();
            //parse input stream
            parser.parse(stream, handler);
            //return indexed votable
            return handler.buildIndexedVotable();
        } catch (ParserConfigurationException ex) {
            //invalid sax parser configuration - should not happen
            Logger.getLogger(VotableParser.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (SAXException ex) {
            //exception during parser setup or parsing input stream
            Logger.getLogger(VotableParser.class.getName()).log(Level.INFO, null, ex);
            //rethrow
            throw new UnparseableVotableException("SAXException during parsing");
        } catch (IOException ex) {
            //exception during reading from stream - consider as unparseable tabl
            Logger.getLogger(VotableParser.class.getName()).log(Level.WARNING, null, ex);
            //rethrow
            throw new UnparseableVotableException("IOException during parsing from the specified input stream");
        }
    }
    
    public static IndexedSSAPVotable parseVotable(String input) throws UnparseableVotableException {
        return parseVotable(new ByteArrayInputStream(input.getBytes(Charset.forName("UTF-8"))));
    }
    
    
    
}
