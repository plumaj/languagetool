package de.danielnaber.languagetool.tagging.disambiguation.pl;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.tagging.disambiguation.Disambiguator;
import java.util.HashMap;

/**
 * Mainly punctuation chunker for Polish. 
 * Probably could be made more universal.
 *   
 * 
 * @author Marcin Miłkowski
 */
public class PolishChunker implements Disambiguator {
      
    /**
     * Implements punctuation chunking: 
     * SENT_END for sentence end,
     * PARA_END for paragraph end, 
     * &lt;ELLIPSIS&gt; for ellipsis (...) start, 
     * and &lt;/ELLIPSIS&gt; for ellipsis end.
     * @param input The tokens to be chunked.
     * @return AnalyzedSentence with additional markers.
     */
  private static final String TOKEN_DEFINITIONS = "...|ELLIPSIS\n" 
      + "to znaczy|TO_ZNACZY\nTo znaczy|TO_ZNACZY\n" 
      + "to jest|TO_JEST\nTo jest|TO_JEST";
  
    public final AnalyzedSentence disambiguate(final AnalyzedSentence input) {
        
      HashMap <String, String> mStartSpace = new HashMap <String, String>();
      HashMap <String, String> mStartNoSpace = new HashMap <String, String>();
      HashMap<String, String> mFull = new HashMap <String, String>();      
      
        String[] posTokens = TOKEN_DEFINITIONS.split("\n");
        for (String posToken : posTokens) {
          String[] tokenAndTag = posToken.split("\\|");
          boolean containsSpace = tokenAndTag[0].indexOf(" ") > 0;
          String firstToken = "";
          String[] firstTokens;
          if (!containsSpace) {
            firstTokens = new String[tokenAndTag[0].length()];
            firstToken = tokenAndTag[0].substring(0, 1);
            for (int i = 1; i < tokenAndTag[0].length(); i++) {
              firstTokens [i] = tokenAndTag[0].substring(0 + (i-1), i);
            }
            if (!mStartNoSpace.containsKey(firstToken)) {
              mStartNoSpace.put(firstToken, Integer.toString(firstTokens.length));
            } else {
              if (Integer.parseInt(mStartNoSpace.get(firstToken)) < firstTokens.length) {
                mStartNoSpace.put(firstToken, Integer.toString(firstTokens.length));
              }
            }
          } else {            
            firstTokens = tokenAndTag[0].split(" ");
            firstToken = firstTokens[0];                                            
          
            if (!mStartSpace.containsKey(firstToken)) {
              mStartSpace.put(firstToken, Integer.toString(firstTokens.length));
            } else {
              if (Integer.parseInt(mStartSpace.get(firstToken)) < firstTokens.length) {
                mStartSpace.put(firstToken, Integer.toString(firstTokens.length));
            }
            }  
          }
          mFull.put(tokenAndTag[0], tokenAndTag[1]);
        }
        
        AnalyzedTokenReadings[] anTokens = input.getTokens();
        AnalyzedTokenReadings[] output = 
          new AnalyzedTokenReadings[anTokens.length];
        
        output = anTokens;        
        
        for (int i = 0; i < anTokens.length; i++) {
          String tok = output[i].getToken();          
          StringBuffer tokens = new StringBuffer();
          
          if (mStartSpace.containsKey(tok)) {
            int len = Integer.parseInt(mStartSpace.get(tok)); 
            if (i + len * 2 < anTokens.length) {
              int j = i;
              int lenCounter = 0;
                while (j <= i + len && j < anTokens.length) {
                    if (!anTokens[j].getToken().trim().equals("")) {
                        tokens.append(anTokens[j].getToken());                        
                        lenCounter++;
                        if (lenCounter == len) {
                          break;
                        } else {
                          tokens.append(" ");
                        }
                     }
                    j++;
                }
            }            
          if (mFull.containsKey(tokens.toString())) {            
               AnalyzedToken tokenStart = 
                 new AnalyzedToken(tok, "<"+mFull.get(tokens.toString())+">", tokens.toString());
               output[i].addReading(tokenStart);
               AnalyzedToken tokenEnd = 
                 new AnalyzedToken(tok, "</"+mFull.get(tokens.toString())+">", tokens.toString());
               output[i + 2].addReading(tokenEnd);
          }          
        }
               
        if (mStartNoSpace.containsKey(tok)) {
          int len = Integer.parseInt(mStartNoSpace.get(tok)); 
          if (i + len < anTokens.length) {            
          for (int j = i; j < i + len; j++) {            
            tokens.append(anTokens[j].getToken());            
            }
          }          
          if (mFull.containsKey(tokens.toString())) {            
               AnalyzedToken tokenStart = 
                 new AnalyzedToken(tok, "<"+mFull.get(tokens.toString())+">", tokens.toString());
               output[i].addReading(tokenStart);
               AnalyzedToken tokenEnd = 
                 new AnalyzedToken(tok, "</"+mFull.get(tokens.toString())+">", tokens.toString());
               output[i + 2].addReading(tokenEnd);
          }          
        }
        }
        
        return new AnalyzedSentence(output);
     }

}
