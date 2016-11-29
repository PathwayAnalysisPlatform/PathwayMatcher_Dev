/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package StringsFunctions;

import java.util.List;

/**
 *
 * @author Optimus Franck
 */
public class Join {
    public static String join(String separator, String...params)
    {
        String result = "";
        if(params.length > 0)
        {
            result += params[0];
            for (int p = 1; p < params.length; p++) {
                result += separator + params[p];
            }
        }
        return result;
    }

    private static String join(String separator, List<String> params) 
    {
        String result = "";
        if(params.size() > 0)
        {
            result += params.get(0);
            for (int p = 1; p < params.size(); p++) {
                result += separator + params.get(p);
            }
        }
        return result;
    }
    
    private static String joinInt(String separator, List<Integer> params) 
    {
        String result = "";
        if(params.size() > 0)
        {
            result += params.get(0);
            for (int p = 1; p < params.size(); p++) {
                result += separator + params.get(p);
            }
        }
        return result;
    }
}
