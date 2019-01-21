package gov.nist.javax.sip.parser.extensions;

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.extensions.MinSE;
import gov.nist.javax.sip.parser.Lexer;
import gov.nist.javax.sip.parser.ParametersParser;
import gov.nist.javax.sip.parser.TokenTypes;
import java.io.PrintStream;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;

public class MinSEParser extends ParametersParser {
    public MinSEParser(String text) {
        super(text);
    }

    protected MinSEParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        MinSE minse = new MinSE();
        if (debug) {
            dbg_enter("parse");
        }
        try {
            headerName(TokenTypes.MINSE_TO);
            minse.setExpires(Integer.parseInt(this.lexer.getNextId()));
            this.lexer.SPorHT();
            super.parse(minse);
            if (debug) {
                dbg_leave("parse");
            }
            return minse;
        } catch (NumberFormatException e) {
            throw createParseException("bad integer format");
        } catch (InvalidArgumentException ex) {
            throw createParseException(ex.getMessage());
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("parse");
            }
        }
    }

    public static void main(String[] args) throws ParseException {
        String[] to = new String[]{"Min-SE: 30\n", "Min-SE: 45;some-param=somevalue\n"};
        for (String minSEParser : to) {
            MinSE t = (MinSE) new MinSEParser(minSEParser).parse();
            PrintStream printStream = System.out;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("encoded = ");
            stringBuilder.append(t.encode());
            printStream.println(stringBuilder.toString());
            printStream = System.out;
            stringBuilder = new StringBuilder();
            stringBuilder.append("\ntime=");
            stringBuilder.append(t.getExpires());
            printStream.println(stringBuilder.toString());
            if (t.getParameter("some-param") != null) {
                printStream = System.out;
                stringBuilder = new StringBuilder();
                stringBuilder.append("some-param=");
                stringBuilder.append(t.getParameter("some-param"));
                printStream.println(stringBuilder.toString());
            }
        }
    }
}
