/*
Copyright (C) 2010 Haowen Ning

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/
package org.liberty.android.fantastischmemo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AMUtil{
    public static boolean isInteger(String s){
        try{
            Integer.parseInt(s);
            return true;
        }
        catch(Exception e){
            return false;
        }
    }

    public static boolean isHTML(String s){
        Pattern htmlPattern1 = Pattern.compile("<[a-zA-Z]+[0-9]*(\\s[a-zA-Z]+[0-9]*=.*)*\\s*/??>");
        Pattern htmlPattern2 = Pattern.compile("&#?[a-z0-9]+;");
        Matcher m1 = htmlPattern1.matcher(s);
        Matcher m2 = htmlPattern2.matcher(s);
        return m1.find() || m2.find();
    }

}
