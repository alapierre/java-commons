package pl.com.softproject.utils.xml;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import org.xml.sax.SAXParseException;

/**
 * Command Line XML Validator
 *
 */
public class CommandLineValidator {

    public static void main(String[] args) throws Exception {
        
        if(args.length != 1) {
            System.out.println("Bad number of parameters, expected one file name!");
            System.exit(1);
        }
        
        String fileName = args[0];
        
        System.out.println("\nCommand Line XML Validator");
        System.out.println("--------------------------");

        Reader reader = new FileReader(new File(fileName));
        Collection<SAXParseException> errors = new ArrayList<SAXParseException>();

        System.out.println("\nStart syntax checking\n");

        boolean valid = XMLValidator.checkSyntax(reader, errors);

        if (!valid) {
            for (SAXParseException error : errors) {
                System.out.println(error.toString());
            }
            System.out.println("\nsyntax checking faliled!");
            System.exit(1);
        } else {
            System.out.println("syntax OK");
        }
        
        System.out.println("\nStart validation\n");
        
        reader = new FileReader(new File(fileName));
        valid = XMLValidator.validate(reader, errors);

        if (!valid) {
            for (SAXParseException error : errors) {
                System.out.println(error.toString());
            }
            System.out.println("\nvalidation faliled!");
            System.exit(1);
        } else {
            System.out.println("validation OK");
        }

    }
}
