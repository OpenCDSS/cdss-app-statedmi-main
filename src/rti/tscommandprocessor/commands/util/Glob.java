/*
 * Glob.java
 * 
 * Created on Oct 24, 2007, 9:31:12 AM
 * 
 */

//package rti.util;
// FIXME SAM 2008-06-25 Need to move to Java 1.5+ and then can use the newer rti.util package as is.
// For now copy to this package and modify code to work with Java 1.4

package rti.tscommandprocessor.commands.util;

import RTi.Util.String.StringUtil;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *
 * @author iws
 */
public class Glob {
    private static final String PREFIX = "regex:";
    public static final Glob STAR = new Glob("*",false);
    private final Pattern pattern;
    private final boolean extended;
    
    public Glob(String pattern, boolean extended) throws IllegalArgumentException {
        this.pattern = build(pattern,extended);
        this.extended = extended;
    }
    
    public static Glob simple(String pattern) {
        return new Glob(pattern,false);
    }
    
    public static Glob extended(String pattern) {
        return new Glob(pattern,true);
    }
    
    public boolean isExtended() {
        return extended;
    }
    
    public Pattern pattern() {
        return pattern;
    }
    
    public boolean matches(CharSequence chars) {
        return pattern.matcher(chars).matches();
    }
    
    public boolean find(CharSequence chars) {
        return pattern.matcher(chars).find();
    }
    
    public static Glob parse(String spec) throws PatternSyntaxException {
        boolean extended = false;
        if (spec.startsWith(PREFIX)) {
            extended = true;
        }
        return new Glob(spec,extended);
    }
    
    public String toString() {
        return extended ? PREFIX + pattern.pattern() : unbuild(pattern.pattern());
    }

    private static Pattern build(String pattern, boolean extended) {
        Pattern p;
        if (extended) p = Pattern.compile(pattern);
        else {
            //pattern = pattern.replace(".","\\.");
            //pattern = pattern.replace("*", ".*");
            //pattern = pattern.replace("?", ".?");
            pattern = StringUtil.replaceString(pattern,".","\\.");
            pattern = StringUtil.replaceString(pattern,"*", ".*");
            pattern = StringUtil.replaceString(pattern,"?", ".?");
            p = Pattern.compile(pattern);
        }
        return p;
    }

    private String unbuild(String pattern) {
        //pattern = pattern.replace(".*","*");
        //pattern = pattern.replace(".?","?");
        //pattern = pattern.replace("\\.",".");
        pattern = StringUtil.replaceString(pattern,".*","*");
        pattern = StringUtil.replaceString(pattern,".?","?");
        pattern = StringUtil.replaceString(pattern,"\\.",".");
        return pattern;
    }
    
}
