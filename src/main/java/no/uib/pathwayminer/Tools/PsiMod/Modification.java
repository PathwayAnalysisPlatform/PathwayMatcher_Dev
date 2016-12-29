/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uib.pathwayminer.Tools.PsiMod;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Modification {
    int id;
    int freq;
    String name;
    LinkedList<Integer> is_a;

    public Modification() {
        id = -1;
        freq = 0;
        this.is_a = new LinkedList<Integer>();
    }
}
