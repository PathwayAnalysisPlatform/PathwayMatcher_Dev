/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uib.pathwayminer.Tools;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Node {
    String id; //Uniprot Id
    int group;
    
    public Node(String i, int g)
    {
        this.id = i;
        this.group = g;
    }
    
    @Override
    public String toString()
    {
        return "{\"id\": \"" + this.id + "\", \"group\": " + this.group + "}";
    }
}
